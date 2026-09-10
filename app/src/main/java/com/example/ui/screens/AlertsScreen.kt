package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertItem
import com.example.data.model.AlertUrgency
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DarkBlueBorder
import com.example.ui.theme.DarkBlueCard
import com.example.ui.theme.DarkBlueDivider
import com.example.ui.theme.DarkBlueSurface
import com.example.ui.theme.DarkBlueSurfaceVariant
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.RiskHighBg
import com.example.ui.theme.RiskMedium
import com.example.ui.theme.RiskMediumBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AlertsScreen(
  alerts: List<AlertItem>,
  onAlertActionClicked: (String) -> Unit
) {
  var selectedUrgencyFilter by remember { mutableStateOf("All") }
  val filters = listOf("All", "Critical", "Warning", "Informational")

  val filteredAlerts = alerts.filter { alert ->
    when (selectedUrgencyFilter) {
      "Critical" -> alert.urgency == AlertUrgency.CRITICAL
      "Warning" -> alert.urgency == AlertUrgency.WARNING
      "Informational" -> alert.urgency == AlertUrgency.INFO
      else -> true
    }
  }.sortedBy {
    when (it.urgency) {
      AlertUrgency.CRITICAL -> 0
      AlertUrgency.WARNING -> 1
      AlertUrgency.INFO -> 2
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("alerts_screen")
  ) {
    // Header
    Text(
      text = "COMPLIANCE & RISK MONITOR",
      color = AccentCyan,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp
    )
    Text(
      text = "Active Operational Alerts",
      color = TextPrimary,
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Filter Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      filters.forEach { filter ->
        val isSelected = (filter == selectedUrgencyFilter)
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) PrimaryBlue else DarkBlueSurfaceVariant)
            .border(
              width = 1.dp,
              color = if (isSelected) AccentCyan else DarkBlueBorder,
              shape = RoundedCornerShape(6.dp)
            )
            .clickable { selectedUrgencyFilter = filter }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("alert_filter_$filter"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = filter,
            color = if (isSelected) TextPrimary else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (filteredAlerts.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(DarkBlueSurface, RoundedCornerShape(8.dp))
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
          Spacer(modifier = Modifier.height(8.dp))
          Text("No active alerts in this category", color = TextSecondary, fontSize = 14.sp)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredAlerts) { alert ->
          val (urgencyColor, urgencyBg) = when (alert.urgency) {
            AlertUrgency.CRITICAL -> Pair(RiskHigh, RiskHighBg)
            AlertUrgency.WARNING -> Pair(RiskMedium, RiskMediumBg)
            AlertUrgency.INFO -> Pair(AccentCyan, Color(0x2B38BDF8))
          }

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, if (alert.urgency == AlertUrgency.CRITICAL) RiskHigh.copy(alpha = 0.4f) else DarkBlueBorder, RoundedCornerShape(8.dp))
              .testTag("alert_item_${alert.id}"),
            colors = CardDefaults.cardColors(containerColor = DarkBlueCard),
            shape = RoundedCornerShape(8.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              // Top line: Urgency badge & timestamp
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .background(urgencyBg, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = when (alert.urgency) {
                        AlertUrgency.CRITICAL -> Icons.Default.Error
                        AlertUrgency.WARNING -> Icons.Default.Warning
                        AlertUrgency.INFO -> Icons.Default.Info
                      },
                      contentDescription = null,
                      tint = urgencyColor,
                      modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = alert.urgency.label.uppercase(),
                      color = urgencyColor,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 0.6.sp
                    )
                  }
                }

                Text(
                  text = alert.timestamp,
                  color = TextMuted,
                  fontSize = 11.sp
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = alert.title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "${alert.facilityName} (${alert.inspectionId})",
                color = AccentCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = alert.description,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
              )

              Spacer(modifier = Modifier.height(12.dp))

              // Action button
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
              ) {
                Button(
                  onClick = { onAlertActionClicked(alert.inspectionId) },
                  modifier = Modifier
                    .height(36.dp)
                    .testTag("alert_action_button_${alert.id}"),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (alert.urgency == AlertUrgency.CRITICAL) PrimaryBlue else DarkBlueSurfaceVariant
                  ),
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = alert.actionLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (alert.urgency == AlertUrgency.CRITICAL) Color.White else AccentCyan
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(14.dp))
                }
              }
            }
          }
        }
      }
    }
  }
}
