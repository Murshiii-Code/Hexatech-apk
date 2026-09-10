package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Inspection
import com.example.data.model.UserRole
import com.example.ui.camera.CameraEvidenceCaptureModal
import com.example.ui.components.InspectionReportDialog
import com.example.ui.components.SmartInspectTopBar
import com.example.ui.screens.ActionType
import com.example.ui.screens.ActionsScreen
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.InspectionDetailScreen
import com.example.ui.screens.InspectionsScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.PublicPortalScreen
import com.example.ui.screens.RiskScreen
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DarkBlueBackground
import com.example.ui.theme.DarkBlueBorder
import com.example.ui.theme.DarkBlueCard
import com.example.ui.theme.DarkBlueDivider
import com.example.ui.theme.DarkBlueSurface
import com.example.ui.theme.DarkBlueSurfaceVariant
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

enum class AppTab(val label: String, val icon: ImageVector) {
  OVERVIEW("Overview", Icons.Default.Dashboard),
  INSPECTIONS("Inspections", Icons.Default.Assignment),
  RISK("Risk", Icons.Default.Warning),
  ALERTS("Alerts", Icons.Default.Notifications),
  ACTIONS("Actions", Icons.Default.PlayArrow),
  PUBLIC("Public", Icons.Default.Public)
}

@Composable
fun SmartInspectApp(
  viewModel: SmartInspectViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  // Navigation State
  var currentTab by remember { mutableStateOf(AppTab.OVERVIEW) }
  var selectedInspectionId by remember { mutableStateOf<String?>(null) }
  var cameraModalInspection by remember { mutableStateOf<Inspection?>(null) }
  var reportDialogInspection by remember { mutableStateOf<Inspection?>(null) }

  // Sync role changes with Public Tab
  val activeInspection = uiState.inspections.find { it.id == selectedInspectionId }

  // Alert and Action Badge counts
  val criticalAlertCount = uiState.alerts.count { it.isActionRequired }
  val pendingActionCount = uiState.inspections.count { !it.hasEvidence && it.status != com.example.data.model.InspectionStatus.Completed }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(DarkBlueBackground)
  ) {
    val isExpandedScreen = maxWidth >= 760.dp

    Row(modifier = Modifier.fillMaxSize()) {
      // Left Navigation Rail for wide screens / tablets
      if (isExpandedScreen && uiState.currentRole != UserRole.PUBLIC) {
        NavigationRail(
          modifier = Modifier
            .fillMaxHeight()
            .border(0.dp, DarkBlueDivider),
          containerColor = DarkBlueSurface,
          contentColor = TextSecondary,
          header = {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .background(PrimaryBlue, RoundedCornerShape(8.dp))
                  .border(1.dp, AccentCyan, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "SMARTINSPECT",
                color = AccentCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )
            }
          }
        ) {
          AppTab.values().forEach { tab ->
            val isSelected = (currentTab == tab && selectedInspectionId == null)
            NavigationRailItem(
              selected = isSelected,
              onClick = {
                selectedInspectionId = null
                currentTab = tab
                if (tab == AppTab.PUBLIC) {
                  viewModel.selectRole(UserRole.PUBLIC)
                }
              },
              icon = {
                if (tab == AppTab.ALERTS && criticalAlertCount > 0) {
                  BadgedBox(badge = { Badge(containerColor = RiskHigh) { Text("$criticalAlertCount") } }) {
                    Icon(tab.icon, contentDescription = tab.label)
                  }
                } else if (tab == AppTab.ACTIONS && pendingActionCount > 0) {
                  BadgedBox(badge = { Badge(containerColor = AccentCyan) { Text("$pendingActionCount") } }) {
                    Icon(tab.icon, contentDescription = tab.label)
                  }
                } else {
                  Icon(tab.icon, contentDescription = tab.label)
                }
              },
              label = { Text(tab.label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
              colors = NavigationRailItemDefaults.colors(
                selectedIconColor = AccentCyan,
                unselectedIconColor = TextMuted,
                selectedTextColor = AccentCyan,
                unselectedTextColor = TextMuted,
                indicatorColor = DarkBlueSurfaceVariant
              )
            )
          }
        }
      }

      // Main Scaffold Content
      Scaffold(
        modifier = Modifier.weight(1f),
        containerColor = DarkBlueBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
          SmartInspectTopBar(
            currentRole = uiState.currentRole,
            onRoleSelected = { newRole ->
              viewModel.selectRole(newRole)
              if (newRole == UserRole.PUBLIC) {
                currentTab = AppTab.PUBLIC
                selectedInspectionId = null
              } else if (currentTab == AppTab.PUBLIC) {
                currentTab = AppTab.OVERVIEW
              }
            },
            isOffline = uiState.isOffline,
            onToggleOffline = { offline ->
              viewModel.toggleOffline(offline)
              scope.launch {
                snackbarHostState.showSnackbar(
                  if (offline) "Switched to Offline Mode. Field data saved locally."
                  else "Online connection restored. Synchronizing ledger..."
                )
              }
            },
            pendingSyncCount = uiState.pendingSyncCount,
            isSyncing = uiState.isSyncing,
            syncMessage = uiState.syncMessage,
            onTriggerSync = { viewModel.triggerSync() }
          )
        },
        bottomBar = {
          // Bottom Navigation Bar for standard mobile viewports
          if (!isExpandedScreen && uiState.currentRole != UserRole.PUBLIC) {
            NavigationBar(
              containerColor = DarkBlueSurface,
              contentColor = TextSecondary,
              modifier = Modifier.border(0.dp, DarkBlueDivider)
            ) {
              val visibleTabs = listOf(AppTab.OVERVIEW, AppTab.INSPECTIONS, AppTab.RISK, AppTab.ALERTS, AppTab.ACTIONS)
              visibleTabs.forEach { tab ->
                val isSelected = (currentTab == tab && selectedInspectionId == null)
                NavigationBarItem(
                  selected = isSelected,
                  onClick = {
                    selectedInspectionId = null
                    currentTab = tab
                  },
                  icon = {
                    if (tab == AppTab.ALERTS && criticalAlertCount > 0) {
                      BadgedBox(badge = { Badge(containerColor = RiskHigh) { Text("$criticalAlertCount") } }) {
                        Icon(tab.icon, contentDescription = tab.label, modifier = Modifier.size(20.dp))
                      }
                    } else if (tab == AppTab.ACTIONS && pendingActionCount > 0) {
                      BadgedBox(badge = { Badge(containerColor = AccentCyan) { Text("$pendingActionCount") } }) {
                        Icon(tab.icon, contentDescription = tab.label, modifier = Modifier.size(20.dp))
                      }
                    } else {
                      Icon(tab.icon, contentDescription = tab.label, modifier = Modifier.size(20.dp))
                    }
                  },
                  label = {
                    Text(
                      text = tab.label,
                      fontSize = 10.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                  },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentCyan,
                    unselectedIconColor = TextMuted,
                    selectedTextColor = AccentCyan,
                    unselectedTextColor = TextMuted,
                    indicatorColor = DarkBlueSurfaceVariant
                  )
                )
              }
            }
          }
        }
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
          // Route Rendering: Either Detail View, Public Portal, or Active Tab
          if (uiState.currentRole == UserRole.PUBLIC || currentTab == AppTab.PUBLIC) {
            PublicPortalScreen(
              inspections = uiState.inspections,
              publicReports = uiState.publicReports,
              onSubmitPublicReport = { facilityName, qrCode, category, description, reporter ->
                viewModel.submitPublicReport(facilityName, qrCode, category, description, reporter)
                scope.launch {
                  snackbarHostState.showSnackbar("Citizen report filed with Municipal Oversight Board.")
                }
              },
              onReturnToDashboard = {
                viewModel.selectRole(UserRole.INSPECTOR)
                currentTab = AppTab.OVERVIEW
              }
            )
          } else if (selectedInspectionId != null && activeInspection != null) {
            // Inspection Detail Screen
            InspectionDetailScreen(
              inspection = activeInspection,
              currentRole = uiState.currentRole,
              onBack = { selectedInspectionId = null },
              onStartInspection = {
                viewModel.startInspection(activeInspection.id)
                scope.launch {
                  snackbarHostState.showSnackbar("Inspection ${activeInspection.id} started.")
                }
              },
              onContinueInspection = {
                cameraModalInspection = activeInspection
              },
              onCaptureEvidence = {
                cameraModalInspection = activeInspection
              },
              onSubmitInspection = {
                if (!activeInspection.hasEvidence) {
                  scope.launch {
                    snackbarHostState.showSnackbar("Cannot submit: Physical camera evidence is mandatory.")
                  }
                  cameraModalInspection = activeInspection
                } else {
                  viewModel.submitInspection(activeInspection.id)
                  scope.launch {
                    snackbarHostState.showSnackbar("Inspection ${activeInspection.id} submitted & certified.")
                  }
                }
              },
              onContractorResponseSubmitted = { response ->
                viewModel.submitContractorResponse(activeInspection.id, response)
                scope.launch {
                  snackbarHostState.showSnackbar("Contractor response recorded to audit ledger.")
                }
              }
            )
          } else {
            // Main Tabs
            AnimatedContent(
              targetState = currentTab,
              transitionSpec = { fadeIn() togetherWith fadeOut() },
              label = "main_tabs"
            ) { targetTab ->
              when (targetTab) {
                AppTab.OVERVIEW -> {
                  OverviewScreen(
                    inspections = uiState.inspections,
                    alerts = uiState.alerts,
                    onInspectionSelected = { insp ->
                      selectedInspectionId = insp.id
                    },
                    onNavigateToTab = { tabName ->
                      when (tabName) {
                        "Inspections" -> currentTab = AppTab.INSPECTIONS
                        "Risk" -> currentTab = AppTab.RISK
                        "Alerts" -> currentTab = AppTab.ALERTS
                        "Actions" -> currentTab = AppTab.ACTIONS
                      }
                    }
                  )
                }

                AppTab.INSPECTIONS -> {
                  InspectionsScreen(
                    inspections = uiState.inspections,
                    onInspectionSelected = { insp ->
                      selectedInspectionId = insp.id
                    }
                  )
                }

                AppTab.RISK -> {
                  RiskScreen(
                    inspections = uiState.inspections,
                    onInspectionSelected = { insp ->
                      selectedInspectionId = insp.id
                    }
                  )
                }

                AppTab.ALERTS -> {
                  AlertsScreen(
                    alerts = uiState.alerts,
                    onAlertActionClicked = { inspectionId ->
                      selectedInspectionId = inspectionId
                    }
                  )
                }

                AppTab.ACTIONS -> {
                  ActionsScreen(
                    inspections = uiState.inspections,
                    onExecuteAction = { inspection, actionType ->
                      when (actionType) {
                        ActionType.CAPTURE_EVIDENCE -> {
                          cameraModalInspection = inspection
                        }
                        ActionType.START_INSPECTION -> {
                          viewModel.startInspection(inspection.id)
                          selectedInspectionId = inspection.id
                        }
                        ActionType.CONTINUE_INSPECTION -> {
                          selectedInspectionId = inspection.id
                        }
                        ActionType.VIEW_REPORT -> {
                          reportDialogInspection = inspection
                        }
                        ActionType.VIEW_DETAILS -> {
                          selectedInspectionId = inspection.id
                        }
                      }
                    }
                  )
                }

                AppTab.PUBLIC -> {
                  // Fallback
                }
              }
            }
          }
        }
      }
    }

    // Fullscreen Camera Evidence Capture Modal (Camera Only, Zero-Gallery)
    if (cameraModalInspection != null) {
      val inspection = cameraModalInspection!!
      CameraEvidenceCaptureModal(
        inspection = inspection,
        onDismiss = { cameraModalInspection = null },
        onPhotoAccepted = { bitmap, category, notes ->
          viewModel.addEvidence(inspection.id, bitmap, category, notes)
          cameraModalInspection = null
          scope.launch {
            snackbarHostState.showSnackbar("Evidence photo captured & attached to ledger.")
          }
        }
      )
    }

    // Certified Report Modal from Actions
    if (reportDialogInspection != null) {
      InspectionReportDialog(
        inspection = reportDialogInspection!!,
        onDismiss = { reportDialogInspection = null }
      )
    }
  }
}
