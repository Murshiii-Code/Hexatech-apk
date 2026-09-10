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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertItem
import com.example.data.model.Inspection
import com.example.data.model.InspectionStatus
import com.example.data.model.RiskLevel
import com.example.ui.components.HighestActiveRiskHeroCard
import com.example.ui.components.RiskBadge
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.TrustRiskBadge
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DarkBlueBorder
import com.example.ui.theme.DarkBlueCard
import com.example.ui.theme.DarkBlueSurface
import com.example.ui.theme.DarkBlueSurfaceVariant
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.RiskLow
import com.example.ui.theme.RiskMedium
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun OverviewScreen(
  inspections: List<Inspection>,
  alerts: List<AlertItem>,
  onInspectionSelected: (Inspection) -> Unit,
  onNavigateToTab: (String) -> Unit
) {
  val scrollState = rememberScrollState()

  // Computed metrics
  val totalInspections = inspections.size
  val highRiskCount = inspections.count { it.riskLevel == RiskLevel.High }
  val pendingCount = inspections.count { it.status == InspectionStatus.Pending }
  val completedCount = inspections.count { it.status == InspectionStatus.Completed }
  val missingEvidenceCount = inspections.count { it.status != InspectionStatus.Completed && !it.hasEvidence }
  val alertsActionRequiredCount = alerts.count { it.isActionRequired }
  val fleetAverageRisk = if (inspections.isNotEmpty()) {
    inspections.map { it.riskScore }.average().toInt()
  } else 0

  val highestRiskInspection = inspections.maxByOrNull { it.riskScore }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp)
      .testTag("overview_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Title & Overall Fleet Risk Score Banner
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "OPERATIONAL OVERVIEW",
          color = AccentCyan,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
        Text(
          text = "Executive Inspection Command",
          color = TextPrimary,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold
        )
      }

      // Fleet Risk Meter
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkBlueCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBlueBorder),
        shape = RoundedCornerShape(8.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "FLEET RISK",
              color = TextMuted,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "$fleetAverageRisk/100",
              color = if (fleetAverageRisk >= 60) RiskHigh else if (fleetAverageRisk >= 35) RiskMedium else RiskLow,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // Prominent "Highest Active Risk" Hero Card
    if (highestRiskInspection != null) {
      HighestActiveRiskHeroCard(
        inspection = highestRiskInspection,
        onActionClicked = { onInspectionSelected(it) }
      )
    }

    // Stat Cards Grid
    Text(
      text = "KEY PLATFORM METRICS",
      color = TextMuted,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp
    )

    // Row 1: High Risk & Missing Evidence
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      StatCard(
        title = "High-Risk Facilities",
        value = "$highRiskCount",
        subtitle = "Score ≥ 70",
        isAlert = highRiskCount > 0,
        icon = Icons.Default.Warning,
        modifier = Modifier.weight(1f),
        onClick = { onNavigateToTab("Risk") }
      )
      StatCard(
        title = "Missing Evidence",
        value = "$missingEvidenceCount",
        subtitle = "Camera capture needed",
        isAlert = missingEvidenceCount > 0,
        icon = Icons.Default.Error,
        modifier = Modifier.weight(1f),
        onClick = { onNavigateToTab("Actions") }
      )
    }

    // Row 2: Pending, Completed, Alerts
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      StatCard(
        title = "Pending",
        value = "$pendingCount",
        subtitle = "Audits queued",
        isAlert = false,
        icon = Icons.Default.HourglassEmpty,
        modifier = Modifier.weight(1f),
        onClick = { onNavigateToTab("Inspections") }
      )
      StatCard(
        title = "Completed",
        value = "$completedCount",
        subtitle = "Certified filed",
        isAlert = false,
        icon = Icons.Default.CheckCircle,
        modifier = Modifier.weight(1f),
        onClick = { onNavigateToTab("Inspections") }
      )
      StatCard(
        title = "Active Alerts",
        value = "$alertsActionRequiredCount",
        subtitle = "Require action",
        isAlert = alertsActionRequiredCount > 0,
        icon = Icons.Default.NotificationsActive,
        modifier = Modifier.weight(1f),
        onClick = { onNavigateToTab("Alerts") }
      )
    }

    // Recent Active Inspections List
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, DarkBlueBorder, RoundedCornerShape(10.dp)),
      colors = CardDefaults.cardColors(containerColor = DarkBlueSurface),
      shape = RoundedCornerShape(10.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "PRIORITY INSPECTION QUEUE",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
          Text(
            text = "View All (${inspections.size})",
            color = AccentCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onNavigateToTab("Inspections") }
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          inspections.take(4).forEach { insp ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(DarkBlueCard, RoundedCornerShape(6.dp))
                .border(0.5.dp, DarkBlueBorder, RoundedCornerShape(6.dp))
                .clickable { onInspectionSelected(insp) }
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = insp.facilityName,
                  color = TextPrimary,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1
                )
                Text(
                  text = "${insp.id} • ${insp.assignedInspector}",
                  color = TextMuted,
                  fontSize = 11.sp
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                StatusBadge(insp.status)
                TrustRiskBadge(insp.trustScore, insp.riskScore)
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
