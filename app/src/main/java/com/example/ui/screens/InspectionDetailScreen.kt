package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.EvidencePhoto
import com.example.data.model.Inspection
import com.example.data.model.InspectionStatus
import com.example.data.model.RiskLevel
import com.example.data.model.UserRole
import com.example.ui.components.EvidenceStatusIndicator
import com.example.ui.components.InspectionReportDialog
import com.example.ui.components.RiskBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DarkBlueBorder
import com.example.ui.theme.DarkBlueCard
import com.example.ui.theme.DarkBlueDivider
import com.example.ui.theme.DarkBlueSurface
import com.example.ui.theme.DarkBlueSurfaceVariant
import com.example.ui.theme.OfflineAmber
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.RiskHigh
import com.example.ui.theme.RiskLow
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun InspectionDetailScreen(
  inspection: Inspection,
  currentRole: UserRole,
  onBack: () -> Unit,
  onStartInspection: () -> Unit,
  onContinueInspection: () -> Unit,
  onCaptureEvidence: () -> Unit,
  onSubmitInspection: () -> Unit,
  onContractorResponseSubmitted: (String) -> Unit
) {
  val scrollState = rememberScrollState()
  var showReportDialog by remember { mutableStateOf(false) }
  var selectedEvidencePhoto by remember { mutableStateOf<EvidencePhoto?>(null) }
  var showContractorResponseModal by remember { mutableStateOf(false) }
  var contractorResponseText by remember { mutableStateOf("") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp)
      .testTag("inspection_detail_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Bar with Back Button & Facility ID
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier
          .size(40.dp)
          .testTag("detail_back_button")
      ) {
        Icon(
          Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = TextPrimary
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Column {
        Text(
          text = inspection.id,
          color = AccentCyan,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
        Text(
          text = inspection.facilityName,
          color = TextPrimary,
          fontSize = 19.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // Facility Overview Card (Facility name, Assigned inspector, Status, Trust/Risk, Date/time)
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
          StatusBadge(status = inspection.status)
          RiskBadge(level = inspection.riskLevel, score = inspection.riskScore)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = inspection.facilityType,
          color = TextMuted,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
        Text(
          text = inspection.address,
          color = TextSecondary,
          fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(14.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBlueDivider))
        Spacer(modifier = Modifier.height(14.dp))

        // Grid of Key Details
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text("ASSIGNED INSPECTOR", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(inspection.assignedInspector, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          }
          Column {
            Text("INSPECTION SCHEDULE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(inspection.inspectionDateTime, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text("TRUST SCORE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("T:${inspection.trustScore}/100", color = AccentCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }
          Column {
            Text("RISK SCORE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("R:${inspection.riskScore}/100", color = if (inspection.riskScore >= 70) RiskHigh else StatusCompleted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }
          Column {
            Text("QR CODE ID", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(inspection.qrCodeIdentifier, color = TextSecondary, fontSize = 13.sp)
          }
        }
      }
    }

    // Risk System Card: Understandable at a glance
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, if (inspection.riskLevel == RiskLevel.High) RiskHigh.copy(alpha = 0.4f) else DarkBlueBorder, RoundedCornerShape(10.dp)),
      colors = CardDefaults.cardColors(containerColor = DarkBlueCard),
      shape = RoundedCornerShape(10.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "RISK FACTORS & TRIGGERS",
            color = if (inspection.riskLevel == RiskLevel.High) RiskHigh else AccentCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
          Text(
            text = "Level: ${inspection.riskLevel.label.uppercase()}",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Primary Trigger
        Text(
          text = "Primary Trigger: ${inspection.primaryRiskTrigger}",
          color = TextPrimary,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold
        )

        // Additional Factors
        if (inspection.additionalRiskFactors.isNotEmpty()) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "+${inspection.additionalRiskFactors.size} Additional Risk Factors:",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(4.dp))
          inspection.additionalRiskFactors.forEach { factor ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(vertical = 2.dp)
            ) {
              Box(modifier = Modifier.size(5.dp).background(RiskHigh, CircleShape))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = factor,
                color = TextSecondary,
                fontSize = 12.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "Recommended: ${inspection.recommendedAction}",
          color = AccentCyan,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }

    // Evidence Section (Thumbnails, Missing / Captured status, Capture Button)
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
            text = "EVIDENCE RECORDS",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
          EvidenceStatusIndicator(
            hasEvidence = inspection.hasEvidence,
            count = inspection.evidencePhotos.size
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (inspection.evidencePhotos.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(DarkBlueCard, RoundedCornerShape(6.dp))
              .border(1.dp, DarkBlueBorder, RoundedCornerShape(6.dp))
              .padding(16.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                Icons.Default.CameraAlt,
                contentDescription = null,
                tint = RiskHigh,
                modifier = Modifier.size(32.dp)
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Missing Camera Evidence",
                color = RiskHigh,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Live physical camera verification is required prior to submission.",
                color = TextMuted,
                fontSize = 11.sp
              )
            }
          }
        } else {
          // Evidence Thumbnails Grid
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            inspection.evidencePhotos.forEach { photo ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(DarkBlueCard, RoundedCornerShape(8.dp))
                  .border(0.5.dp, DarkBlueBorder, RoundedCornerShape(8.dp))
                  .clickable { selectedEvidencePhoto = photo }
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Photo Thumbnail or Icon
                Box(
                  modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black),
                  contentAlignment = Alignment.Center
                ) {
                  if (photo.bitmap != null) {
                    Image(
                      bitmap = photo.bitmap.asImageBitmap(),
                      contentDescription = "Evidence Thumbnail",
                      modifier = Modifier.fillMaxSize()
                    )
                  } else {
                    Icon(
                      Icons.Default.CameraAlt,
                      contentDescription = null,
                      tint = AccentCyan,
                      modifier = Modifier.size(24.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = photo.category,
                      color = TextPrimary,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold
                    )
                    if (!photo.isSynced) {
                      Text(
                        text = "Pending Sync",
                        color = OfflineAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }
                  Text(
                    text = photo.timestamp,
                    color = TextMuted,
                    fontSize = 11.sp
                  )
                  Text(
                    text = photo.gpsStatus,
                    color = AccentCyan,
                    fontSize = 10.sp,
                    maxLines = 1
                  )
                }
              }
            }
          }
        }
      }
    }

    // Contractor Required Action & Response (if applicable)
    if (!inspection.requiredContractorAction.isNullOrBlank()) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, DarkBlueBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkBlueSurface),
        shape = RoundedCornerShape(10.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "CONTRACTOR CORRECTIVE ACTION",
            color = AccentCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Assigned to: ${inspection.contractorAssigned}",
            color = TextMuted,
            fontSize = 12.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = inspection.requiredContractorAction,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )

          if (!inspection.contractorResponse.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(DarkBlueCard, RoundedCornerShape(6.dp))
                .padding(10.dp)
            ) {
              Column {
                Text("CONTRACTOR RESPONSE", color = StatusCompleted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(inspection.contractorResponse, color = TextSecondary, fontSize = 12.sp)
              }
            }
          } else {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
              onClick = { showContractorResponseModal = true },
              modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .testTag("contractor_respond_button"),
              colors = ButtonDefaults.buttonColors(containerColor = DarkBlueSurfaceVariant),
              shape = RoundedCornerShape(6.dp)
            ) {
              Text("Respond to Correction", fontSize = 12.sp, color = AccentCyan)
            }
          }
        }
      }
    }

    // Action History & Transparent Audit Trail
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
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.History, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "AUDIT TRAIL & HISTORY",
              color = TextSecondary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp
            )
          }
          Text(
            text = "${inspection.auditTrail.size} events",
            color = TextMuted,
            fontSize = 11.sp
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          inspection.auditTrail.forEach { entry ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(DarkBlueCard, RoundedCornerShape(6.dp))
                .padding(10.dp),
              verticalAlignment = Alignment.Top
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .background(AccentCyan, CircleShape)
                  .padding(top = 4.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(entry.action, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  Text(entry.timestamp, color = TextMuted, fontSize = 10.sp)
                }
                Text("User: ${entry.user}", color = AccentCyan, fontSize = 11.sp)
                if (entry.details.isNotBlank()) {
                  Text(entry.details, color = TextSecondary, fontSize = 11.sp)
                }
              }
            }
          }
        }
      }
    }

    // Bottom Action Bar: Start Inspection / Continue / Capture Evidence / Submit / View Report
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Primary Action depends on status
      when (inspection.status) {
        InspectionStatus.Pending -> {
          Button(
            onClick = onStartInspection,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("start_inspection_button"),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Inspection", fontWeight = FontWeight.Bold)
          }
        }
        InspectionStatus.InProgress -> {
          // Both Capture Evidence & Submit Inspection available
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = onCaptureEvidence,
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("capture_evidence_button"),
              colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Capture Evidence", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Button(
              onClick = onSubmitInspection,
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("submit_inspection_button"),
              colors = ButtonDefaults.buttonColors(containerColor = StatusCompleted),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Submit Inspection", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }
        }
        InspectionStatus.Completed -> {
          Button(
            onClick = { showReportDialog = true },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("view_report_button"),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Certified Report", fontWeight = FontWeight.Bold)
          }
        }
      }

      // Secondary action button if in progress or completed
      if (inspection.status == InspectionStatus.InProgress) {
        OutlinedButton(
          onClick = { showReportDialog = true },
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
          border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBlueBorder)),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("Preview Audit Ledger")
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
  }

  // Report Modal
  if (showReportDialog) {
    InspectionReportDialog(
      inspection = inspection,
      onDismiss = { showReportDialog = false }
    )
  }

  // Evidence Photo Detail Dialog
  if (selectedEvidencePhoto != null) {
    val photo = selectedEvidencePhoto!!
    Dialog(onDismissRequest = { selectedEvidencePhoto = null }) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, DarkBlueBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkBlueSurface),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "EVIDENCE DETAILS",
            color = AccentCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.height(8.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(4f / 3f)
              .clip(RoundedCornerShape(6.dp))
              .background(Color.Black),
            contentAlignment = Alignment.Center
          ) {
            if (photo.bitmap != null) {
              Image(
                bitmap = photo.bitmap.asImageBitmap(),
                contentDescription = "Full Evidence Photo",
                modifier = Modifier.fillMaxSize()
              )
            } else {
              Icon(Icons.Default.CameraAlt, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(40.dp))
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          Text("Category: ${photo.category}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          Text("Captured: ${photo.timestamp}", color = TextSecondary, fontSize = 12.sp)
          Text("Location: ${photo.gpsStatus}", color = AccentCyan, fontSize = 12.sp)
          if (photo.inspectorNotes.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Notes: ${photo.inspectorNotes}", color = TextMuted, fontSize = 12.sp)
          }

          Spacer(modifier = Modifier.height(16.dp))
          Button(
            onClick = { selectedEvidencePhoto = null },
            modifier = Modifier.fillMaxWidth().height(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text("Close")
          }
        }
      }
    }
  }

  // Contractor Response Dialog
  if (showContractorResponseModal) {
    Dialog(onDismissRequest = { showContractorResponseModal = false }) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, DarkBlueBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkBlueSurface),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("CONTRACTOR REMEDIATION RESPONSE", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(6.dp))
          Text(inspection.requiredContractorAction ?: "", color = TextPrimary, fontSize = 13.sp)
          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = contractorResponseText,
            onValueChange = { contractorResponseText = it },
            placeholder = { Text("Enter certification ID, replacement dates, or corrective log...", color = TextMuted, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = DarkBlueCard,
              unfocusedContainerColor = DarkBlueCard,
              focusedBorderColor = PrimaryBlue,
              unfocusedBorderColor = DarkBlueBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )

          Spacer(modifier = Modifier.height(16.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { showContractorResponseModal = false },
              modifier = Modifier.weight(1f).height(42.dp)
            ) {
              Text("Cancel")
            }
            Button(
              onClick = {
                if (contractorResponseText.isNotBlank()) {
                  onContractorResponseSubmitted(contractorResponseText)
                  showContractorResponseModal = false
                }
              },
              modifier = Modifier.weight(1.2f).height(42.dp),
              colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
              Text("Submit Response")
            }
          }
        }
      }
    }
  }
}
