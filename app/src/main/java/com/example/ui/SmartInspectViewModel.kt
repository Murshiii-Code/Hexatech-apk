package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AlertItem
import com.example.data.model.Inspection
import com.example.data.model.PublicReport
import com.example.data.model.UserRole
import com.example.data.repository.SmartInspectRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SmartInspectUiState(
  val inspections: List<Inspection> = emptyList(),
  val alerts: List<AlertItem> = emptyList(),
  val publicReports: List<PublicReport> = emptyList(),
  val currentRole: UserRole = UserRole.INSPECTOR,
  val isOffline: Boolean = false,
  val pendingSyncCount: Int = 0,
  val isSyncing: Boolean = false,
  val syncMessage: String? = null
)

private data class SyncInfo(
  val isOffline: Boolean,
  val pendingSyncCount: Int,
  val isSyncing: Boolean,
  val syncMessage: String?
)

class SmartInspectViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = SmartInspectRepository()

  private val syncInfoFlow = combine(
    repository.isOffline,
    repository.pendingSyncCount,
    repository.isSyncing,
    repository.syncMessage
  ) { isOffline, pendingCount, isSyncing, syncMsg ->
    SyncInfo(isOffline, pendingCount, isSyncing, syncMsg)
  }

  val uiState: StateFlow<SmartInspectUiState> = combine(
    repository.inspections,
    repository.alerts,
    repository.publicReports,
    repository.currentRole,
    syncInfoFlow
  ) { inspections, alerts, publicReports, role, syncInfo ->
    SmartInspectUiState(
      inspections = inspections,
      alerts = alerts,
      publicReports = publicReports,
      currentRole = role,
      isOffline = syncInfo.isOffline,
      pendingSyncCount = syncInfo.pendingSyncCount,
      isSyncing = syncInfo.isSyncing,
      syncMessage = syncInfo.syncMessage
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = SmartInspectUiState()
  )

  fun selectRole(role: UserRole) {
    repository.setRole(role)
  }

  fun toggleOffline(offline: Boolean) {
    repository.toggleOfflineMode(offline)
  }

  fun triggerSync() {
    repository.triggerSync()
  }

  fun startInspection(id: String, inspectorName: String = "Alex Menon") {
    repository.startInspection(id, inspectorName)
  }

  fun submitInspection(id: String, inspectorName: String = "Alex Menon") {
    repository.submitInspection(id, inspectorName)
  }

  fun addEvidence(
    inspectionId: String,
    bitmap: Bitmap,
    category: String,
    notes: String,
    inspectorName: String = "Alex Menon"
  ) {
    repository.addEvidencePhoto(inspectionId, bitmap, category, notes, inspectorName)
  }

  fun submitContractorResponse(inspectionId: String, response: String) {
    repository.submitContractorResponse(inspectionId, response)
  }

  fun submitPublicReport(
    facilityName: String,
    qrCode: String,
    category: String,
    description: String,
    reporter: String
  ) {
    val identifier = if (qrCode.isNotBlank()) qrCode else facilityName
    repository.submitPublicReport(identifier, category, description)
  }
}

