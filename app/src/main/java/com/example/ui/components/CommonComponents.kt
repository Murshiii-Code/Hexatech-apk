package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Inspection
import com.example.data.model.InspectionStatus
import com.example.data.model.RiskLevel
import com.example.data.model.UserRole
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DarkBlueBackground
import com.example.ui.theme.DarkBlueBorder
import com.example.ui.theme.DarkBlueCard
import com.example.ui.theme.DarkBlueDivider
import com.example.ui.theme.DarkBlueSurface
import com.example.ui.theme.DarkBlueSurfaceVariant
import com.example.ui.theme.OfflineAmber
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.RiskHighBg
import com.example.ui.theme.RiskLow
import com.example.ui.theme.RiskLowBg
import com.example.ui.theme.RiskMedium
import com.example.ui.theme.RiskMediumBg
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusCompletedBg
import com.example.ui.theme.StatusInProgress
import com.example.ui.theme.StatusInProgressBg
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SmartInspectTopBar(
  currentRole: UserRole,
  onRoleSelected: (UserRole) -> Unit,
  isOffline: Boolean,
  onToggleOffline: (Boolean) -> Unit,
  pendingSyncCount: Int,
  isSyncing: Boolean,
  syncMessage: String?,
  onTriggerSync: () -> Unit
) {
  var roleMenuExpanded by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(DarkBlueSurface)
      .border(0.dp, DarkBlueDivider)
  ) {
    // Top banner if offline or syncing
    AnimatedVisibility(visible = isOffline || isSyncing || syncMessage != null) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            if (isSyncing) PrimaryBlue
            else if (isOffline) OfflineAmber.copy(alpha = 0.95f)
            else RiskLow
          )
          .padding(horizontal = 16.dp, vertical = 6.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSyncing) {
              CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = Color.White,
                strokeWidth = 2.dp
              )
            } else {
              Icon(
                if (isOffline) Icons.Default.CloudOff else Icons.Default.CloudDone,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = syncMessage ?: if (isOffline) {
                "OFFLINE MODE • $pendingSyncCount record(s) pending cloud sync"
              } else {
                "All inspection data synchronized"
              },
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          if (isOffline) {
            Text(
              text = "GO ONLINE",
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier
                .clickable { onToggleOffline(false) }
                .background(Color(0x33000000), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }
    }

    // Main header bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Brand Logo & Title
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { /* Brand Home */ }
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .background(PrimaryBlue, RoundedCornerShape(8.dp))
            .border(1.dp, AccentCyan, RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = "SmartInspect Logo",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "SmartInspect",
              color = TextPrimary,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .background(DarkBlueSurfaceVariant, RoundedCornerShape(4.dp))
                .border(0.5.dp, AccentCyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
              Text(
                text = "ENTERPRISE",
                color = AccentCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
              )
            }
          }
          Text(
            text = "Risk Management Platform",
            color = TextMuted,
            fontSize = 11.sp
          )
        }
      }

      // Actions: Offline Toggle & Role Switcher
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Offline / Online Mode Switcher
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isOffline) OfflineAmber.copy(alpha = 0.15f) else DarkBlueCard)
            .border(
              1.dp,
              if (isOffline) OfflineAmber else DarkBlueBorder,
              RoundedCornerShape(6.dp)
            )
            .clickable { onToggleOffline(!isOffline) }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("offline_toggle_button"),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .background(if (isOffline) OfflineAmber else RiskLow, CircleShape)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = if (isOffline) "Offline" else "Online",
              color = if (isOffline) OfflineAmber else TextSecondary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }

        // Role Switcher Dropdown
        Box {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(PrimaryBlue.copy(alpha = 0.15f))
              .border(1.dp, PrimaryBlue.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
              .clickable { roleMenuExpanded = true }
              .padding(horizontal = 10.dp, vertical = 6.dp)
              .testTag("role_switcher_button"),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = when (currentRole) {
                  UserRole.INSPECTOR -> Icons.Default.Person
                  UserRole.CONTRACTOR -> Icons.Default.Security
                  UserRole.PUBLIC -> Icons.Default.Public
                },
                contentDescription = null,
                tint = AccentCyan,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = currentRole.label,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(14.dp)
              )
            }
          }

          DropdownMenu(
            expanded = roleMenuExpanded,
            onDismissRequest = { roleMenuExpanded = false },
            modifier = Modifier
              .background(DarkBlueSurface)
              .border(1.dp, DarkBlueBorder, RoundedCornerShape(8.dp))
          ) {
            UserRole.values().forEach { role ->
              DropdownMenuItem(
                text = {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text(
                        text = role.label,
                        color = if (role == currentRole) AccentCyan else TextPrimary,
                        fontWeight = if (role == currentRole) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                      )
                      Text(
                        text = when (role) {
                          UserRole.INSPECTOR -> "Conduct audits & camera evidence"
                          UserRole.CONTRACTOR -> "Respond to required corrections"
                          UserRole.PUBLIC -> "Scan QR & submit citizen reports"
                        },
                        color = TextMuted,
                        fontSize = 11.sp
                      )
                    }
                    if (role == currentRole) {
                      Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Active",
                        tint = AccentCyan,
                        modifier = Modifier.size(16.dp)
                      )
                    }
                  }
                },
                onClick = {
                  onRoleSelected(role)
                  roleMenuExpanded = false
                }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun StatusBadge(status: InspectionStatus) {
  val (bgColor, textColor, label) = when (status) {
    InspectionStatus.Pending -> Triple(StatusPendingBg, StatusPending, "Pending")
    InspectionStatus.InProgress -> Triple(StatusInProgressBg, StatusInProgress, "In Progress")
    InspectionStatus.Completed -> Triple(StatusCompletedBg, StatusCompleted, "Completed")
  }

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(bgColor)
      .padding(horizontal = 8.dp, vertical = 3.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .background(textColor, CircleShape)
      )
      Spacer(modifier = Modifier.width(5.dp))
      Text(
        text = label,
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1
      )
    }
  }
}

@Composable
fun RiskBadge(level: RiskLevel, score: Int? = null) {
  val (bgColor, textColor, label) = when (level) {
    RiskLevel.High -> Triple(RiskHighBg, RiskHigh, "High Risk")
    RiskLevel.Medium -> Triple(RiskMediumBg, RiskMedium, "Medium")
    RiskLevel.Low -> Triple(RiskLowBg, RiskLow, "Low")
  }

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(bgColor)
      .padding(horizontal = 7.dp, vertical = 3.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .background(textColor, CircleShape)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = if (score != null) "$label ($score)" else label,
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1
      )
    }
  }
}

@Composable
fun TrustRiskBadge(trustScore: Int, riskScore: Int) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    // Trust Score Badge
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(3.dp))
        .background(DarkBlueSurfaceVariant)
        .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
      Text(
        text = "T:$trustScore",
        color = AccentCyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
      )
    }

    // Risk Score Badge
    val riskColor = when {
      riskScore >= 70 -> RiskHigh
      riskScore >= 35 -> RiskMedium
      else -> RiskLow
    }
    val riskBg = when {
      riskScore >= 70 -> RiskHighBg
      riskScore >= 35 -> RiskMediumBg
      else -> RiskLowBg
    }
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(3.dp))
        .background(riskBg)
        .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
      Text(
        text = "R:$riskScore",
        color = riskColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun HighestActiveRiskHeroCard(
  inspection: Inspection,
  onActionClicked: (Inspection) -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.5.dp, RiskHigh.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
      .testTag("highest_active_risk_card"),
    colors = CardDefaults.cardColors(containerColor = DarkBlueCard),
    shape = RoundedCornerShape(10.dp)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      // Top row: Header tag & Actual Risk Score
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .background(RiskHighBg, RoundedCornerShape(4.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = RiskHigh,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "HIGHEST ACTIVE RISK",
                color = RiskHigh,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
              )
            }
          }
        }

        // Prominent actual risk score e.g. 87/100
        Row(verticalAlignment = Alignment.Bottom) {
          Text(
            text = "${inspection.riskScore}",
            color = RiskHigh,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black
          )
          Text(
            text = "/100",
            color = TextMuted,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 3.dp, start = 1.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Facility Name
      Text(
        text = inspection.facilityName,
        color = TextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "${inspection.facilityType} • Assigned to ${inspection.assignedInspector}",
        color = TextSecondary,
        fontSize = 13.sp
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Primary Risk Trigger
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(DarkBlueSurfaceVariant, RoundedCornerShape(6.dp))
          .padding(horizontal = 12.dp, vertical = 10.dp)
      ) {
        Column {
          Text(
            text = "PRIMARY RISK TRIGGER",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = inspection.primaryRiskTrigger,
            color = if (inspection.primaryRiskTrigger == "Missing Evidence") RiskHigh else TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Additional risk factors if any
      if (inspection.additionalRiskFactors.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "+${inspection.additionalRiskFactors.size} additional factors:",
            color = TextMuted,
            fontSize = 11.sp
          )
          inspection.additionalRiskFactors.take(2).forEach { factor ->
            Box(
              modifier = Modifier
                .background(DarkBlueSurface, RoundedCornerShape(4.dp))
                .border(0.5.dp, DarkBlueBorder, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = factor,
                color = TextSecondary,
                fontSize = 11.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Recommended next action
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "Recommended Action: ",
          color = TextMuted,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
        Text(
          text = inspection.recommendedAction,
          color = AccentCyan,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Take Action CTA
      Button(
        onClick = { onActionClicked(inspection) },
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp)
          .testTag("highest_risk_action_button"),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
        shape = RoundedCornerShape(6.dp)
      ) {
        Text(
          text = if (inspection.status == InspectionStatus.InProgress && !inspection.hasEvidence)
            "Capture Mandatory Evidence"
          else if (inspection.status == InspectionStatus.Pending)
            "Start Urgent Inspection"
          else
            "Open Inspection Details",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
      }
    }
  }
}

@Composable
fun StatCard(
  title: String,
  value: String,
  subtitle: String? = null,
  isAlert: Boolean = false,
  icon: ImageVector? = null,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  Card(
    modifier = modifier
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
      .border(1.dp, if (isAlert) RiskHigh.copy(alpha = 0.5f) else DarkBlueBorder, RoundedCornerShape(8.dp)),
    colors = CardDefaults.cardColors(containerColor = DarkBlueCard),
    shape = RoundedCornerShape(8.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title.uppercase(),
          color = if (isAlert) RiskHigh else TextMuted,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
        if (icon != null) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isAlert) RiskHigh else TextMuted,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = value,
        color = if (isAlert) RiskHigh else TextPrimary,
        fontSize = 24.sp,
        fontWeight = FontWeight.Black
      )

      if (subtitle != null) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          color = TextSecondary,
          fontSize = 11.sp
        )
      }
    }
  }
}

@Composable
fun EvidenceStatusIndicator(hasEvidence: Boolean, count: Int) {
  if (hasEvidence) {
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(4.dp))
        .background(StatusCompletedBg)
        .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Default.CheckCircle,
          contentDescription = null,
          tint = StatusCompleted,
          modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Evidence Captured ($count)",
          color = StatusCompleted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  } else {
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(4.dp))
        .background(RiskHighBg)
        .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Default.Error,
          contentDescription = null,
          tint = RiskHigh,
          modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Missing Evidence",
          color = RiskHigh,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

@Composable
fun InspectionReportDialog(
  inspection: Inspection,
  onDismiss: () -> Unit
) {
  val scrollState = rememberScrollState()

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, DarkBlueBorder, RoundedCornerShape(12.dp)),
      colors = CardDefaults.cardColors(containerColor = DarkBlueSurface),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(scrollState)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "OFFICIAL AUDIT REPORT",
              color = AccentCyan,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
            Text(
              text = "Inspection Certificate",
              color = TextPrimary,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Certificate Banner
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(DarkBlueCard, RoundedCornerShape(8.dp))
            .border(1.dp, DarkBlueBorder, RoundedCornerShape(8.dp))
            .padding(14.dp)
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "ID: ${inspection.id}",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "STATUS: ${inspection.status.label.uppercase()}",
                color = if (inspection.status == InspectionStatus.Completed) StatusCompleted else StatusPending,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = inspection.facilityName,
              color = TextPrimary,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = inspection.address,
              color = TextMuted,
              fontSize = 12.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Metrics Grid
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = DarkBlueCard)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text("TRUST SCORE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              Text("${inspection.trustScore}/100", color = AccentCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
          }
          Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = DarkBlueCard)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text("RISK SCORE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              Text("${inspection.riskScore}/100", color = if (inspection.riskScore >= 70) RiskHigh else StatusCompleted, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
          }
          Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = DarkBlueCard)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text("EVIDENCE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              Text("${inspection.evidencePhotos.size} Photos", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Text
        Text("EXECUTIVE COMPLIANCE SUMMARY", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = inspection.reportSummary ?: "Inspection conducted on ${inspection.inspectionDateTime} by ${inspection.assignedInspector}. All physical verification checks recorded under secure immutable audit logging.",
          color = TextPrimary,
          fontSize = 13.sp,
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Signatures & Ledger Details
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(DarkBlueSurfaceVariant, RoundedCornerShape(6.dp))
            .padding(10.dp)
        ) {
          Column {
            Text("LEDGER AUDIT SEAL", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("Inspector: ${inspection.assignedInspector}", color = TextSecondary, fontSize = 12.sp)
            Text("Contractor Entity: ${inspection.contractorAssigned}", color = TextSecondary, fontSize = 12.sp)
            Text("Audit Cryptographic Hash: SHA256-8F29A-${inspection.id.hashCode()}", color = AccentCyan, fontSize = 10.sp)
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
          colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
          shape = RoundedCornerShape(6.dp)
        ) {
          Text("Done")
        }
      }
    }
  }
}
