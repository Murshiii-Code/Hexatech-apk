package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.model.ActionHistoryItem
import com.example.data.model.ActionQueueItem
import com.example.data.model.AlertItem
import com.example.data.model.AlertUrgency
import com.example.data.model.AuditEntry
import com.example.data.model.EvidencePhoto
import com.example.data.model.Inspection
import com.example.data.model.InspectionStatus
import com.example.data.model.PublicReport
import com.example.data.model.RiskLevel
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SmartInspectRepository(
  private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

  private val _currentRole = MutableStateFlow(UserRole.INSPECTOR)
  val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

  private val _isOffline = MutableStateFlow(false)
  val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

  private val _pendingSyncCount = MutableStateFlow(0)
  val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()

  private val _syncMessage = MutableStateFlow<String?>(null)
  val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

  private val _isSyncing = MutableStateFlow(false)
  val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

  private val _inspections = MutableStateFlow<List<Inspection>>(emptyList())
  val inspections: StateFlow<List<Inspection>> = _inspections.asStateFlow()

  private val _alerts = MutableStateFlow<List<AlertItem>>(emptyList())
  val alerts: StateFlow<List<AlertItem>> = _alerts.asStateFlow()

  private val _publicReports = MutableStateFlow<List<PublicReport>>(emptyList())
  val publicReports: StateFlow<List<PublicReport>> = _publicReports.asStateFlow()

  init {
    loadSeedData()
  }

  fun setRole(role: UserRole) {
    _currentRole.value = role
  }

  fun toggleOfflineMode(offline: Boolean) {
    _isOffline.value = offline
    if (!offline && _pendingSyncCount.value > 0) {
      triggerSync()
    }
  }

  fun triggerSync() {
    if (_isSyncing.value) return
    scope.launch {
      _isSyncing.value = true
      _syncMessage.value = "Synchronizing ${_pendingSyncCount.value} pending record(s)..."
      delay(1500)
      // Mark all evidence as synced
      _inspections.update { list ->
        list.map { insp ->
          insp.copy(
            evidencePhotos = insp.evidencePhotos.map { it.copy(isSynced = true) }
          )
        }
      }
      val syncedCount = _pendingSyncCount.value
      _pendingSyncCount.value = 0
      _syncMessage.value = "All $syncedCount record(s) synchronized to cloud audit ledger."
      delay(3000)
      _syncMessage.value = null
      _isSyncing.value = false
    }
  }

  fun getInspectionById(id: String): Inspection? {
    return _inspections.value.firstOrNull { it.id == id }
  }

  fun startInspection(id: String, inspectorName: String) {
    val now = currentTimestamp()
    _inspections.update { list ->
      list.map { insp ->
        if (insp.id == id) {
          val newAudit = insp.auditTrail + AuditEntry(
            id = UUID.randomUUID().toString(),
            user = inspectorName,
            action = "Inspection Started",
            timestamp = now,
            details = "Inspector initiated on-site checklist"
          )
          val newActions = insp.actionHistory + ActionHistoryItem(
            id = UUID.randomUUID().toString(),
            title = "Inspection Commenced",
            actor = inspectorName,
            status = "In Progress",
            timestamp = now
          )
          insp.copy(
            status = InspectionStatus.InProgress,
            auditTrail = newAudit,
            actionHistory = newActions
          )
        } else insp
      }
    }
  }

  fun addEvidencePhoto(
    inspectionId: String,
    bitmap: Bitmap,
    category: String,
    inspectorNotes: String,
    inspectorName: String
  ) {
    val now = currentTimestamp()
    val offlineNow = _isOffline.value
    val newEvidence = EvidencePhoto(
      id = "EVD-${System.currentTimeMillis() % 10000}",
      inspectionId = inspectionId,
      bitmap = bitmap,
      timestamp = now,
      category = category,
      gpsStatus = "Verified GPS (37.7749° N, 122.4194° W) • ±2.4m precision",
      isSynced = !offlineNow,
      inspectorNotes = inspectorNotes.ifBlank { "Physical verification photo taken with camera hardware." }
    )

    if (offlineNow) {
      _pendingSyncCount.value += 1
    }

    _inspections.update { list ->
      list.map { insp ->
        if (insp.id == inspectionId) {
          val updatedPhotos = insp.evidencePhotos + newEvidence
          // Recalculate risk if missing evidence was the primary trigger
          val updatedTrigger = if (insp.primaryRiskTrigger == "Missing Evidence") {
            "Evidence Uploaded (Awaiting Verification)"
          } else insp.primaryRiskTrigger

          val newRiskScore = if (insp.primaryRiskTrigger == "Missing Evidence") {
            (insp.riskScore - 15).coerceAtLeast(10)
          } else insp.riskScore

          val newRiskLevel = when {
            newRiskScore >= 70 -> RiskLevel.High
            newRiskScore >= 35 -> RiskLevel.Medium
            else -> RiskLevel.Low
          }

          val newAudit = insp.auditTrail + AuditEntry(
            id = UUID.randomUUID().toString(),
            user = inspectorName,
            action = "Evidence Captured",
            timestamp = now,
            details = "Captured $category evidence photo [${newEvidence.id}]. ${if (offlineNow) "(Saved Offline - Pending Sync)" else "(Cloud Ledger Verified)"}"
          )

          insp.copy(
            evidencePhotos = updatedPhotos,
            primaryRiskTrigger = updatedTrigger,
            riskScore = newRiskScore,
            riskLevel = newRiskLevel,
            auditTrail = newAudit
          )
        } else insp
      }
    }

    // Resolve missing evidence alert if present
    _alerts.update { alertList ->
      alertList.filterNot { it.inspectionId == inspectionId && it.title.contains("Missing Evidence", ignoreCase = true) }
    }
  }

  fun submitInspection(inspectionId: String, inspectorName: String): Pair<Boolean, String> {
    val insp = getInspectionById(inspectionId) ?: return Pair(false, "Inspection not found")
    if (insp.evidencePhotos.isEmpty()) {
      return Pair(false, "Cannot submit inspection: At least one fresh camera evidence photo is strictly required.")
    }

    val now = currentTimestamp()
    _inspections.update { list ->
      list.map { item ->
        if (item.id == inspectionId) {
          val newAudit = item.auditTrail + AuditEntry(
            id = UUID.randomUUID().toString(),
            user = inspectorName,
            action = "Inspection Submitted",
            timestamp = now,
            details = "Completed inspection package filed with ${item.evidencePhotos.size} verified evidence photos"
          ) + AuditEntry(
            id = UUID.randomUUID().toString(),
            user = "Compliance Engine",
            action = "Report Generated",
            timestamp = now,
            details = "Official audit certificate #REP-${item.id.replace("INS-", "")} generated"
          )

          val updatedTrust = (item.trustScore + 18).coerceAtMost(100)
          val updatedRisk = (item.riskScore - 20).coerceAtLeast(12)
          val updatedLevel = when {
            updatedRisk >= 70 -> RiskLevel.High
            updatedRisk >= 35 -> RiskLevel.Medium
            else -> RiskLevel.Low
          }

          item.copy(
            status = InspectionStatus.Completed,
            trustScore = updatedTrust,
            riskScore = updatedRisk,
            riskLevel = updatedLevel,
            auditTrail = newAudit,
            reportSummary = "Certified inspection conducted by $inspectorName on $now. Facility compliance meets state municipal standards under audit code SEC-8890."
          )
        } else item
      }
    }

    // Clear alerts for this inspection
    _alerts.update { it.filterNot { a -> a.inspectionId == inspectionId } }

    return Pair(true, "Inspection successfully submitted! Compliance certificate generated.")
  }

  fun submitContractorResponse(inspectionId: String, responseText: String) {
    val now = currentTimestamp()
    _inspections.update { list ->
      list.map { insp ->
        if (insp.id == inspectionId) {
          val newAudit = insp.auditTrail + AuditEntry(
            id = UUID.randomUUID().toString(),
            user = "Contractor (${insp.contractorAssigned})",
            action = "Action Assigned",
            timestamp = now,
            details = "Contractor responded: $responseText"
          )
          insp.copy(
            contractorResponse = responseText,
            auditTrail = newAudit
          )
        } else insp
      }
    }
  }

  fun submitPublicReport(
    facilityIdentifier: String,
    category: String,
    description: String
  ): PublicReport {
    val now = currentTimestamp()
    val matched = _inspections.value.firstOrNull {
      it.qrCodeIdentifier.equals(facilityIdentifier.trim(), ignoreCase = true) ||
          it.id.equals(facilityIdentifier.trim(), ignoreCase = true) ||
          it.facilityName.contains(facilityIdentifier.trim(), ignoreCase = true)
    } ?: _inspections.value.first()

    val refCode = "PUB-${System.currentTimeMillis() % 100000}"
    val newReport = PublicReport(
      id = UUID.randomUUID().toString(),
      facilityId = matched.id,
      facilityName = matched.facilityName,
      issueCategory = category,
      description = description,
      submittedAt = now,
      referenceCode = refCode,
      status = "Under Review"
    )

    _publicReports.update { listOf(newReport) + it }

    // Register an alert in internal system
    val newAlert = AlertItem(
      id = UUID.randomUUID().toString(),
      facilityName = matched.facilityName,
      inspectionId = matched.id,
      title = "Citizen Issue Reported: $category",
      description = "Public ticket [$refCode]: $description",
      urgency = AlertUrgency.WARNING,
      timestamp = now,
      isActionRequired = true,
      actionLabel = "Review Ticket"
    )
    _alerts.update { listOf(newAlert) + it }

    // Update facility audit trail
    _inspections.update { list ->
      list.map { insp ->
        if (insp.id == matched.id) {
          val newAudit = insp.auditTrail + AuditEntry(
            id = UUID.randomUUID().toString(),
            user = "Public Portal (External)",
            action = "Risk Updated",
            timestamp = now,
            details = "Community report received [$refCode] for $category"
          )
          val extraFactors = if (!insp.additionalRiskFactors.contains("Repeated Complaint")) {
            insp.additionalRiskFactors + "Repeated Complaint"
          } else insp.additionalRiskFactors
          insp.copy(
            additionalRiskFactors = extraFactors,
            auditTrail = newAudit
          )
        } else insp
      }
    }

    return newReport
  }

  fun getActionQueue(): List<ActionQueueItem> {
    val items = mutableListOf<ActionQueueItem>()
    _inspections.value.forEach { insp ->
      when (insp.status) {
        InspectionStatus.Pending -> {
          items.add(
            ActionQueueItem(
              id = "ACT-START-${insp.id}",
              inspectionId = insp.id,
              facilityName = insp.facilityName,
              assignedTo = insp.assignedInspector,
              actionType = "Start Inspection",
              urgency = if (insp.riskLevel == RiskLevel.High) "Urgent" else "Pending",
              deadline = "Scheduled Today",
              statusText = "Awaiting Inspector Check-in"
            )
          )
        }
        InspectionStatus.InProgress -> {
          if (!insp.hasEvidence) {
            items.add(
              ActionQueueItem(
                id = "ACT-EVD-${insp.id}",
                inspectionId = insp.id,
                facilityName = insp.facilityName,
                assignedTo = insp.assignedInspector,
                actionType = "Capture Evidence",
                urgency = "Urgent",
                deadline = "Mandatory Prior to Submission",
                statusText = "Missing Evidence • Camera verification required"
              )
            )
          } else {
            items.add(
              ActionQueueItem(
                id = "ACT-CONT-${insp.id}",
                inspectionId = insp.id,
                facilityName = insp.facilityName,
                assignedTo = insp.assignedInspector,
                actionType = "Continue Inspection",
                urgency = "Normal",
                deadline = "In Progress",
                statusText = "${insp.evidencePhotos.size} photo(s) captured • Ready for sign-off"
              )
            )
          }
        }
        InspectionStatus.Completed -> {
          items.add(
            ActionQueueItem(
              id = "ACT-REP-${insp.id}",
              inspectionId = insp.id,
              facilityName = insp.facilityName,
              assignedTo = insp.assignedInspector,
              actionType = "View Report",
              urgency = "Normal",
              deadline = "Filed",
              statusText = "Inspection Complete • Audit report signed"
            )
          )
        }
      }

      if (insp.riskLevel == RiskLevel.High && insp.status != InspectionStatus.Completed) {
        items.add(
          ActionQueueItem(
            id = "ACT-RISK-${insp.id}",
            inspectionId = insp.id,
            facilityName = insp.facilityName,
            assignedTo = insp.assignedInspector,
            actionType = "Review Risk",
            urgency = "Urgent",
            deadline = "High Priority",
            statusText = "Score ${insp.riskScore}/100 • ${insp.primaryRiskTrigger}"
          )
        )
      }

      if (!insp.requiredContractorAction.isNullOrBlank() && insp.contractorResponse.isNullOrBlank()) {
        items.add(
          ActionQueueItem(
            id = "ACT-CONT-RESP-${insp.id}",
            inspectionId = insp.id,
            facilityName = insp.facilityName,
            assignedTo = insp.contractorAssigned,
            actionType = "Respond to Correction",
            urgency = "Urgent",
            deadline = "72h Remediation Window",
            statusText = "Action required: ${insp.requiredContractorAction}"
          )
        )
      }
    }
    return items
  }

  private fun loadSeedData() {
    val seedInspections = listOf(
      Inspection(
        id = "INS-2026-041",
        facilityName = "Metro Chemical Refinement",
        facilityType = "Hazardous Material Processing",
        address = "740 Industrial Parkway, Sector 4",
        assignedInspector = "Alex Menon",
        status = InspectionStatus.InProgress,
        trustScore = 41,
        riskScore = 87,
        riskLevel = RiskLevel.High,
        primaryRiskTrigger = "Missing Evidence",
        additionalRiskFactors = listOf(
          "Repeated Complaint",
          "Late Inspection",
          "Previous Failed Inspection"
        ),
        recommendedAction = "Immediate On-Site Verification & Fresh Evidence Capture",
        inspectionDateTime = "Sep 09, 2026 • 09:30 AM",
        evidencePhotos = emptyList(), // Missing evidence as required!
        actionHistory = listOf(
          ActionHistoryItem("AH-1", "Notice of Non-Compliance Issued", "Compliance Officer", "Active", "Sep 05, 2026"),
          ActionHistoryItem("AH-2", "Inspector Dispatched", "Alex Menon", "In Progress", "Sep 09, 2026")
        ),
        auditTrail = listOf(
          AuditEntry("AT-1", "Alex Menon", "Inspection Started", "Sep 09, 2026 • 09:30 AM", "Inspector arrived on site and opened hazardous checklist"),
          AuditEntry("AT-2", "Risk Matrix Engine", "Risk Updated", "Sep 09, 2026 • 09:35 AM", "Risk score raised to 87/100 due to unverified primary containment valve"),
          AuditEntry("AT-3", "Supervisor Vance", "Action Assigned", "Sep 09, 2026 • 09:40 AM", "Assigned urgent camera evidence capture mandate to Alex Menon")
        ),
        contractorAssigned = "Apex Chemical Solutions",
        requiredContractorAction = "Submit pressure relief certification and calibrate safety shutoffs",
        qrCodeIdentifier = "FAC-MCR-740",
        reportSummary = null
      ),
      Inspection(
        id = "INS-2026-042",
        facilityName = "District Water Facility",
        facilityType = "Public Water Purification",
        address = "1200 Aqua Way, District 3",
        assignedInspector = "Alex Menon",
        status = InspectionStatus.Pending,
        trustScore = 64,
        riskScore = 72,
        riskLevel = RiskLevel.High,
        primaryRiskTrigger = "Unverified Chlorine Intake Valve",
        additionalRiskFactors = listOf("Late Inspection", "Filter Degradation"),
        recommendedAction = "Deploy Inspector for Physical Flow Integrity Check",
        inspectionDateTime = "Sep 10, 2026 • 08:00 AM",
        evidencePhotos = emptyList(),
        actionHistory = listOf(
          ActionHistoryItem("AH-3", "Scheduled Quarterly Audit", "District Board", "Pending", "Sep 01, 2026")
        ),
        auditTrail = listOf(
          AuditEntry("AT-4", "Scheduler Bot", "Inspection Scheduled", "Sep 01, 2026 • 10:00 AM", "Automated regulatory quarterly inspection initiated")
        ),
        contractorAssigned = "CleanFlow Municipal Services",
        requiredContractorAction = "Replace pre-sediment filtration cartridges",
        qrCodeIdentifier = "FAC-DWF-101",
        reportSummary = null
      ),
      Inspection(
        id = "INS-2026-038",
        facilityName = "Municipal Storage Facility",
        facilityType = "Bulk Cold & Dry Warehouse",
        address = "88 Harbor Boulevard, Gate 2",
        assignedInspector = "Saha Qureshi",
        status = InspectionStatus.Completed,
        trustScore = 92,
        riskScore = 18,
        riskLevel = RiskLevel.Low,
        primaryRiskTrigger = "None Identified",
        additionalRiskFactors = emptyList(),
        recommendedAction = "Maintain standard 6-month monitoring cadence",
        inspectionDateTime = "Sep 08, 2026 • 02:15 PM",
        evidencePhotos = listOf(
          EvidencePhoto(
            id = "EVD-3801",
            inspectionId = "INS-2026-038",
            timestamp = "Sep 08, 2026 • 02:40 PM",
            category = "Safety Egress",
            gpsStatus = "Verified GPS (37.7812° N, 122.3991° W) • ±1.8m precision",
            isSynced = true,
            inspectorNotes = "Emergency exits clear, fire dampers fully functional"
          ),
          EvidencePhoto(
            id = "EVD-3802",
            inspectionId = "INS-2026-038",
            timestamp = "Sep 08, 2026 • 03:05 PM",
            category = "Structural Integrity",
            gpsStatus = "Verified GPS (37.7813° N, 122.3993° W) • ±2.1m precision",
            isSynced = true,
            inspectorNotes = "Roof trusses and concrete slab free of cracking"
          )
        ),
        actionHistory = listOf(
          ActionHistoryItem("AH-4", "Inspection Passed", "Saha Qureshi", "Completed", "Sep 08, 2026"),
          ActionHistoryItem("AH-5", "Clearance Certificate Issued", "State Registrar", "Approved", "Sep 08, 2026")
        ),
        auditTrail = listOf(
          AuditEntry("AT-5", "Saha Qureshi", "Inspection Started", "Sep 08, 2026 • 02:15 PM", "Checked in at security gate"),
          AuditEntry("AT-6", "Saha Qureshi", "Evidence Captured", "Sep 08, 2026 • 02:40 PM", "Logged structural camera proofs"),
          AuditEntry("AT-7", "Saha Qureshi", "Inspection Submitted", "Sep 08, 2026 • 03:30 PM", "Zero violations recorded"),
          AuditEntry("AT-8", "Compliance Engine", "Report Generated", "Sep 08, 2026 • 03:31 PM", "Issued municipal clearance certificate #MUNI-982")
        ),
        contractorAssigned = "Harbor Warehousing Logistics",
        requiredContractorAction = null,
        contractorResponse = "All maintenance logs verified and signed off.",
        qrCodeIdentifier = "FAC-MSF-088",
        reportSummary = "Facility fully conforms to municipal fire and structural building codes. Trust score established at 92."
      ),
      Inspection(
        id = "INS-2026-044",
        facilityName = "North Grid Substation",
        facilityType = "High Voltage Energy Distribution",
        address = "550 Transmission Way, North Sector",
        assignedInspector = "Elena Rostova",
        status = InspectionStatus.Pending,
        trustScore = 58,
        riskScore = 68,
        riskLevel = RiskLevel.Medium,
        primaryRiskTrigger = "Thermal Anomaly in Transformer B",
        additionalRiskFactors = listOf("Repeated Complaint", "Heavy Load Cycle"),
        recommendedAction = "Conduct Thermographic Infrared Inspection",
        inspectionDateTime = "Sep 11, 2026 • 11:00 AM",
        evidencePhotos = emptyList(),
        actionHistory = listOf(
          ActionHistoryItem("AH-6", "Citizen Flashing Light Report", "Public Intake", "Logged", "Sep 07, 2026")
        ),
        auditTrail = listOf(
          AuditEntry("AT-9", "System Engine", "Risk Updated", "Sep 07, 2026 • 11:45 PM", "Automated telemetry flag triggered inspection dispatch")
        ),
        contractorAssigned = "GridTech Power Systems",
        requiredContractorAction = "Perform dielectric oil breakdown test",
        qrCodeIdentifier = "FAC-NGS-550",
        reportSummary = null
      ),
      Inspection(
        id = "INS-2026-039",
        facilityName = "Riverside Cold Storage",
        facilityType = "Perishable Food Depot",
        address = "310 Waterfront Rd, Pier 14",
        assignedInspector = "David Chen",
        status = InspectionStatus.Completed,
        trustScore = 88,
        riskScore = 24,
        riskLevel = RiskLevel.Low,
        primaryRiskTrigger = "None Identified",
        additionalRiskFactors = emptyList(),
        recommendedAction = "Standard annual re-evaluation",
        inspectionDateTime = "Sep 06, 2026 • 10:00 AM",
        evidencePhotos = listOf(
          EvidencePhoto(
            id = "EVD-3901",
            inspectionId = "INS-2026-039",
            timestamp = "Sep 06, 2026 • 10:45 AM",
            category = "Refrigeration Unit",
            gpsStatus = "Verified GPS (37.7651° N, 122.3892° W) • ±1.5m precision",
            isSynced = true,
            inspectorNotes = "Ammonia sensors calibrated, temperature records steady at -18°C"
          )
        ),
        actionHistory = emptyList(),
        auditTrail = listOf(
          AuditEntry("AT-10", "David Chen", "Inspection Submitted", "Sep 06, 2026 • 11:30 AM", "Inspection passed without citations")
        ),
        contractorAssigned = "Arctic Cold Systems",
        requiredContractorAction = null,
        qrCodeIdentifier = "FAC-RCS-310",
        reportSummary = "Certified compliance for cold-chain safety regulations."
      ),
      Inspection(
        id = "INS-2026-045",
        facilityName = "Harbor Petroleum Terminal",
        facilityType = "Marine Liquid Fuel Depot",
        address = "18 Dockside Terminal, Berth 9",
        assignedInspector = "Saha Qureshi",
        status = InspectionStatus.InProgress,
        trustScore = 52,
        riskScore = 81,
        riskLevel = RiskLevel.High,
        primaryRiskTrigger = "Late Inspection (Overdue 4 Days)",
        additionalRiskFactors = listOf("Dockside Corrosion", "Previous Failed Inspection"),
        recommendedAction = "Immediate Pressure Test on Fuel Transfer Arms",
        inspectionDateTime = "Sep 09, 2026 • 01:00 PM",
        evidencePhotos = emptyList(),
        actionHistory = listOf(
          ActionHistoryItem("AH-7", "Overdue Escalation Notice", "Harbor Master", "Urgent", "Sep 07, 2026")
        ),
        auditTrail = listOf(
          AuditEntry("AT-11", "Harbor Compliance", "Action Assigned", "Sep 08, 2026 • 08:30 AM", "Assigned priority field verification to Saha Qureshi")
        ),
        contractorAssigned = "Terminal Fueling Maritime",
        requiredContractorAction = "Submit hydrostatic certification for bunkering manifold",
        qrCodeIdentifier = "FAC-HPT-018",
        reportSummary = null
      )
    )

    val seedAlerts = listOf(
      AlertItem(
        id = "ALT-101",
        facilityName = "Metro Chemical Refinement",
        inspectionId = "INS-2026-041",
        title = "Highest Active Risk Facility (Score 87/100)",
        description = "Facility has missing evidence on high-risk chemical storage. Inspection is actively in progress.",
        urgency = AlertUrgency.CRITICAL,
        timestamp = "Today • 09:35 AM",
        isActionRequired = true,
        actionLabel = "Open Inspection"
      ),
      AlertItem(
        id = "ALT-102",
        facilityName = "District Water Facility",
        inspectionId = "INS-2026-042",
        title = "Missing Evidence Before Scheduled Deadline",
        description = "Quarterly chlorine verification requires photographic proof of emergency shutoff valve.",
        urgency = AlertUrgency.CRITICAL,
        timestamp = "Today • 08:15 AM",
        isActionRequired = true,
        actionLabel = "Capture Evidence"
      ),
      AlertItem(
        id = "ALT-103",
        facilityName = "Harbor Petroleum Terminal",
        inspectionId = "INS-2026-045",
        title = "Late Inspection (Overdue 4 Days)",
        description = "Mandatory marine fuel transfer safety check past state statutory deadline.",
        urgency = AlertUrgency.WARNING,
        timestamp = "Yesterday • 05:00 PM",
        isActionRequired = true,
        actionLabel = "Prioritize"
      ),
      AlertItem(
        id = "ALT-104",
        facilityName = "North Grid Substation",
        inspectionId = "INS-2026-044",
        title = "Repeated Citizen Complaints Logged",
        description = "3 community reports logged within 48h regarding buzzing transformer and flashing lights.",
        urgency = AlertUrgency.WARNING,
        timestamp = "Sep 07, 2026 • 11:45 PM",
        isActionRequired = true,
        actionLabel = "Review Complaints"
      ),
      AlertItem(
        id = "ALT-105",
        facilityName = "Metro Chemical Refinement",
        inspectionId = "INS-2026-041",
        title = "Required Contractor Action Pending",
        description = "Apex Chemical Solutions has not yet submitted hydrostatic safety certificate.",
        urgency = AlertUrgency.WARNING,
        timestamp = "Sep 06, 2026 • 02:00 PM",
        isActionRequired = true,
        actionLabel = "Notify Contractor"
      )
    )

    _inspections.value = seedInspections
    _alerts.value = seedAlerts
  }

  private fun currentTimestamp(): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US)
    return sdf.format(Date())
  }
}
