package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Inspection
import com.example.data.model.InspectionStatus
import com.example.data.model.RiskLevel
import com.example.ui.components.RiskBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DarkBlueBorder
import com.example.ui.theme.DarkBlueCard
import com.example.ui.theme.DarkBlueSurface
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.RiskHighBg
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ActionsScreen(
  inspections: List<Inspection>,
  onExecuteAction: (inspection: Inspection, actionType: ActionType) -> Unit
) {
  val scrollState = rememberScrollState()

  val missingEvidenceList = inspections.filter { !it.hasEvidence && it.status != InspectionStatus.Completed }
  val pendingList = inspections.filter { it.status == InspectionStatus.Pending }
  val inProgressList = inspections.filter { it.status == InspectionStatus.InProgress }
  val contractorActionList = inspections.filter { !it.requiredContractorAction.isNullOrBlank() }
  val completedList = inspections.filter { it.status == InspectionStatus.Completed }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp)
      .testTag("actions_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    Column {
      Text(
        text = "ACTION ENGINE",
        color = AccentCyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
      Text(
        text = "Next Required Actions",
        color = TextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
      )
    }

    // Section 1: Missing Evidence -> Capture Evidence
    if (missingEvidenceList.isNotEmpty()) {
      ActionSection(
        title = "MISSING EVIDENCE (${missingEvidenceList.size})",
        subtitle = "Mandatory camera capture required before certification",
        isCritical = true
      ) {
        missingEvidenceList.forEach { insp ->
          ActionItemCard(
            title = insp.facilityName,
            subtitle = "${insp.id} • ${insp.facilityType}",
            status = insp.status,
            riskLevel = insp.riskLevel,
            actionLabel = "Capture Evidence",
            icon = Icons.Default.CameraAlt,
            actionColor = PrimaryBlue,
            onClick = { onExecuteAction(insp, ActionType.CAPTURE_EVIDENCE) }
          )
        }
      }
    }

    // Section 2: In Progress -> Continue Inspection
    if (inProgressList.isNotEmpty()) {
      ActionSection(
        title = "IN PROGRESS AUDITS (${inProgressList.size})",
        subtitle = "Active field audits ready for completion & submission"
      ) {
        inProgressList.forEach { insp ->
          ActionItemCard(
            title = insp.facilityName,
            subtitle = "${insp.id} • ${insp.assignedInspector}",
            status = insp.status,
            riskLevel = insp.riskLevel,
            actionLabel = "Continue Inspection",
            icon = Icons.Default.PlayArrow,
            actionColor = PrimaryBlue,
            onClick = { onExecuteAction(insp, ActionType.CONTINUE_INSPECTION) }
          )
        }
      }
    }

    // Section 3: Pending -> Start Inspection
    if (pendingList.isNotEmpty()) {
      ActionSection(
        title = "SCHEDULED QUEUE (${pendingList.size})",
        subtitle = "Pending physical audits ready to begin"
      ) {
        pendingList.forEach { insp ->
          ActionItemCard(
            title = insp.facilityName,
            subtitle = "${insp.id} • Assigned to ${insp.assignedInspector}",
            status = insp.status,
            riskLevel = insp.riskLevel,
            actionLabel = "Start Inspection",
            icon = Icons.Default.PlayArrow,
            actionColor = PrimaryBlue,
            onClick = { onExecuteAction(insp, ActionType.START_INSPECTION) }
          )
        }
      }
    }

    // Section 4: Contractor Corrections Required
    if (contractorActionList.isNotEmpty()) {
      ActionSection(
        title = "CONTRACTOR REMEDIATIONS (${contractorActionList.size})",
        subtitle = "Mandatory engineering corrective actions assigned to contractors"
      ) {
        contractorActionList.forEach { insp ->
          ActionItemCard(
            title = insp.facilityName,
            subtitle = "Action: ${insp.requiredContractorAction}",
            status = insp.status,
            riskLevel = insp.riskLevel,
            actionLabel = "Review Remediation",
            icon = Icons.Default.Security,
            actionColor = DarkBlueBorder,
            onClick = { onExecuteAction(insp, ActionType.VIEW_DETAILS) }
          )
        }
      }
    }

    // Section 5: Completed -> View Report
    if (completedList.isNotEmpty()) {
      ActionSection(
        title = "COMPLETED LEDGER (${completedList.size})",
        subtitle = "Certified inspections with immutable audit seals"
      ) {
        completedList.forEach { insp ->
          ActionItemCard(
            title = insp.facilityName,
            subtitle = "${insp.id} • Certified by ${insp.assignedInspector}",
            status = insp.status,
            riskLevel = insp.riskLevel,
            actionLabel = "View Report",
            icon = Icons.Default.Description,
            actionColor = DarkBlueBorder,
            onClick = { onExecuteAction(insp, ActionType.VIEW_REPORT) }
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}

enum class ActionType {
  START_INSPECTION,
  CONTINUE_INSPECTION,
  CAPTURE_EVIDENCE,
  VIEW_REPORT,
  VIEW_DETAILS
}

@Composable
fun ActionSection(
  title: String,
  subtitle: String,
  isCritical: Boolean = false,
  content: @Composable () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (isCritical) {
        Box(
          modifier = Modifier
            .background(RiskHighBg, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(title, color = RiskHigh, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
        }
      } else {
        Text(title, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
      }
    }
    Text(subtitle, color = TextSecondary, fontSize = 12.sp)

    content()
  }
}

@Composable
fun ActionItemCard(
  title: String,
  subtitle: String,
  status: InspectionStatus,
  riskLevel: RiskLevel,
  actionLabel: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  actionColor: Color,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, DarkBlueBorder, RoundedCornerShape(8.dp)),
    colors = CardDefaults.cardColors(containerColor = DarkBlueSurface),
    shape = RoundedCornerShape(8.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          StatusBadge(status)
          RiskBadge(riskLevel)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = title,
          color = TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = subtitle,
          color = TextMuted,
          fontSize = 12.sp,
          maxLines = 1
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Button(
        onClick = onClick,
        modifier = Modifier
          .height(38.dp)
          .testTag("action_button_${title.take(10)}"),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (actionColor == DarkBlueBorder) PrimaryBlue else actionColor
        ),
        shape = RoundedCornerShape(6.dp)
      ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}
