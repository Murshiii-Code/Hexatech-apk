package com.example.data.model

import android.graphics.Bitmap

enum class InspectionStatus(val label: String) {
  Pending("Pending"),
  InProgress("In Progress"),
  Completed("Completed")
}

enum class RiskLevel(val label: String) {
  Low("Low"),
  Medium("Medium"),
  High("High")
}

enum class UserRole(val label: String, val badge: String) {
  INSPECTOR("Inspector", "Internal"),
  CONTRACTOR("Contractor", "Partner"),
  PUBLIC("Public Portal", "External")
}

enum class AlertUrgency(val label: String) {
  CRITICAL("Critical"),
  WARNING("Warning"),
  INFO("Informational")
}

data class EvidencePhoto(
  val id: String,
  val inspectionId: String,
  val bitmap: Bitmap? = null,
  val filePath: String? = null,
  val timestamp: String,
  val category: String,
  val gpsStatus: String,
  val isSynced: Boolean = true,
  val inspectorNotes: String = ""
)

data class AuditEntry(
  val id: String,
  val user: String,
  val action: String,
  val timestamp: String,
  val details: String = ""
)

data class ActionHistoryItem(
  val id: String,
  val title: String,
  val actor: String,
  val status: String,
  val timestamp: String
)

data class Inspection(
  val id: String,
  val facilityName: String,
  val facilityType: String,
  val address: String,
  val assignedInspector: String,
  val status: InspectionStatus,
  val trustScore: Int,
  val riskScore: Int,
  val riskLevel: RiskLevel,
  val primaryRiskTrigger: String,
  val additionalRiskFactors: List<String> = emptyList(),
  val recommendedAction: String,
  val inspectionDateTime: String,
  val evidencePhotos: List<EvidencePhoto> = emptyList(),
  val actionHistory: List<ActionHistoryItem> = emptyList(),
  val auditTrail: List<AuditEntry> = emptyList(),
  val contractorAssigned: String = "Apex Utilities LLC",
  val requiredContractorAction: String? = null,
  val contractorResponse: String? = null,
  val qrCodeIdentifier: String = "",
  val reportSummary: String? = null
) {
  val hasEvidence: Boolean get() = evidencePhotos.isNotEmpty()
}

data class AlertItem(
  val id: String,
  val facilityName: String,
  val inspectionId: String,
  val title: String,
  val description: String,
  val urgency: AlertUrgency,
  val timestamp: String,
  val isActionRequired: Boolean = true,
  val actionLabel: String = "Take Action"
)

data class ActionQueueItem(
  val id: String,
  val inspectionId: String,
  val facilityName: String,
  val assignedTo: String,
  val actionType: String,
  val urgency: String,
  val deadline: String,
  val statusText: String
)

data class PublicReport(
  val id: String,
  val facilityId: String,
  val facilityName: String,
  val issueCategory: String,
  val description: String,
  val submittedAt: String,
  val referenceCode: String,
  val status: String = "Under Review"
)
