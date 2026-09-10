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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Inspection
import com.example.data.model.RiskLevel
import com.example.ui.components.RiskBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DarkBlueBorder
import com.example.ui.theme.DarkBlueCard
import com.example.ui.theme.DarkBlueSurface
import com.example.ui.theme.DarkBlueSurfaceVariant
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.RiskHighBg
import com.example.ui.theme.RiskLow
import com.example.ui.theme.RiskMedium
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RiskScreen(
  inspections: List<Inspection>,
  onInspectionSelected: (Inspection) -> Unit
) {
  val scrollState = rememberScrollState()

  val highRiskList = inspections.filter { it.riskLevel == RiskLevel.High }.sortedByDescending { it.riskScore }
  val mediumRiskList = inspections.filter { it.riskLevel == RiskLevel.Medium }.sortedByDescending { it.riskScore }
  val lowRiskList = inspections.filter { it.riskLevel == RiskLevel.Low }.sortedByDescending { it.riskScore }

  val total = inspections.size.coerceAtLeast(1)
  val highPct = (highRiskList.size * 100) / total
  val medPct = (mediumRiskList.size * 100) / total
  val lowPct = (lowRiskList.size * 100) / total

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp)
      .testTag("risk_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Title
    Column {
      Text(
        text = "RISK ARCHITECTURE",
        color = AccentCyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
      Text(
        text = "Facility Risk Matrix",
        color = TextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
      )
    }

    // Fleet Risk Distribution Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, DarkBlueBorder, RoundedCornerShape(10.dp)),
      colors = CardDefaults.cardColors(containerColor = DarkBlueSurface),
      shape = RoundedCornerShape(10.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "FLEET RISK DISTRIBUTION",
          color = TextMuted,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Distribution Meter Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
        ) {
          if (highPct > 0) {
            Box(
              modifier = Modifier
                .weight(highPct.toFloat().coerceAtLeast(1f))
                .background(RiskHigh)
            )
          }
          if (medPct > 0) {
            Box(
              modifier = Modifier
                .weight(medPct.toFloat().coerceAtLeast(1f))
                .background(RiskMedium)
            )
          }
          if (lowPct > 0) {
            Box(
              modifier = Modifier
                .weight(lowPct.toFloat().coerceAtLeast(1f))
                .background(RiskLow)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          RiskLegendItem(label = "High Risk", count = highRiskList.size, pct = "$highPct%", color = RiskHigh)
          RiskLegendItem(label = "Medium Risk", count = mediumRiskList.size, pct = "$medPct%", color = RiskMedium)
          RiskLegendItem(label = "Low Risk", count = lowRiskList.size, pct = "$lowPct%", color = RiskLow)
        }
      }
    }

    // High Risk Priority Section
    Text(
      text = "CRITICAL RISK SITES (${highRiskList.size})",
      color = RiskHigh,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.8.sp
    )

    highRiskList.forEach { inspection ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, RiskHigh.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
          .clickable { onInspectionSelected(inspection) }
          .testTag("risk_facility_${inspection.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkBlueCard),
        shape = RoundedCornerShape(8.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          // Top Header: High Risk Badge & Actual Score (e.g. 87/100)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .background(RiskHighBg, RoundedCornerShape(4.dp))
                .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
              Text(
                text = "HIGH RISK",
                color = RiskHigh,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Row(verticalAlignment = Alignment.Bottom) {
              Text(
                text = "${inspection.riskScore}",
                color = RiskHigh,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
              )
              Text(
                text = "/100",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = inspection.facilityName,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${inspection.id} • ${inspection.facilityType}",
            color = TextMuted,
            fontSize = 12.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Primary Trigger
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(DarkBlueSurfaceVariant, RoundedCornerShape(6.dp))
              .padding(8.dp)
          ) {
            Column {
              Text(
                text = "PRIMARY TRIGGER",
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = inspection.primaryRiskTrigger,
                color = if (inspection.primaryRiskTrigger == "Missing Evidence") RiskHigh else TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          // Additional Risk Factors
          if (inspection.additionalRiskFactors.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "+${inspection.additionalRiskFactors.size} additional risk factors: ${inspection.additionalRiskFactors.joinToString(", ")}",
              color = TextSecondary,
              fontSize = 11.sp
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            StatusBadge(inspection.status)
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "View Analysis",
                color = AccentCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
              Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AccentCyan,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }

    // Medium & Low Risk Facilities Section
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "MONITORED SITES (MEDIUM & LOW RISK)",
      color = TextMuted,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.8.sp
    )

    (mediumRiskList + lowRiskList).forEach { inspection ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(DarkBlueSurface, RoundedCornerShape(6.dp))
          .border(0.5.dp, DarkBlueBorder, RoundedCornerShape(6.dp))
          .clickable { onInspectionSelected(inspection) }
          .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = inspection.facilityName,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "${inspection.id} • ${inspection.primaryRiskTrigger}",
            color = TextMuted,
            fontSize = 11.sp,
            maxLines = 1
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          RiskBadge(level = inspection.riskLevel, score = inspection.riskScore)
          Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
fun RiskLegendItem(label: String, count: Int, pct: String, color: androidx.compose.ui.graphics.Color) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
    Spacer(modifier = Modifier.width(6.dp))
    Column {
      Text(label, color = TextSecondary, fontSize = 11.sp)
      Text("$count ($pct)", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
  }
}
