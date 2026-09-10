package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Inspection
import com.example.data.model.PublicReport
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DarkBlueBorder
import com.example.ui.theme.DarkBlueCard
import com.example.ui.theme.DarkBlueSurface
import com.example.ui.theme.DarkBlueSurfaceVariant
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusCompletedBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private val ISSUE_CATEGORIES = listOf(
  "Odor / Discoloration",
  "Safety Hazard / Egress Block",
  "Chemical Leak / Vapor",
  "Structural Barrier Failure",
  "Noise & Mechanical Vibration",
  "Other Environmental Concern"
)

@Composable
fun PublicPortalScreen(
  inspections: List<Inspection>,
  publicReports: List<PublicReport>,
  onSubmitPublicReport: (facilityName: String, qrCode: String, category: String, description: String, reporter: String) -> Unit,
  onReturnToDashboard: () -> Unit
) {
  val scrollState = rememberScrollState()

  // Selected Facility State
  var selectedInspection by remember {
    mutableStateOf(inspections.firstOrNull())
  }
  var isScanningQr by remember { mutableStateOf(false) }

  // Form Fields
  var selectedCategory by remember { mutableStateOf(ISSUE_CATEGORIES.first()) }
  var issueDescription by remember { mutableStateOf("") }
  var reporterContact by remember { mutableStateOf("") }

  // Submission State
  var submittedReportId by remember { mutableStateOf<String?>(null) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp)
      .testTag("public_portal_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Public Portal Header (Zero internal data exposed)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, AccentCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
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
            Box(
              modifier = Modifier
                .size(32.dp)
                .background(AccentCyan.copy(alpha = 0.2f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Public, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "CIVIC INTEGRITY PORTAL",
                color = AccentCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )
              Text(
                text = "Public Safety & Issue Reporting",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Text(
            text = "Exit Portal",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
              .clickable { onReturnToDashboard() }
              .padding(4.dp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "Submit community observations directly to municipal oversight boards. All reports are verified and routed to field inspection personnel.",
          color = TextMuted,
          fontSize = 12.sp,
          lineHeight = 17.sp
        )
      }
    }

    if (submittedReportId != null) {
      // Submission Confirmation Screen
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, StatusCompleted, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkBlueCard),
        shape = RoundedCornerShape(10.dp)
      ) {
        Column(
          modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(56.dp)
              .background(StatusCompletedBg, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusCompleted, modifier = Modifier.size(32.dp))
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "Public Report Submitted",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Receipt Tracking Code: $submittedReportId",
            color = AccentCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Thank you for contributing to community safety. Your report for ${selectedInspection?.facilityName} has been logged in the official inspection ledger.",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = {
              submittedReportId = null
              issueDescription = ""
              reporterContact = ""
            },
            modifier = Modifier.fillMaxWidth().height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text("Submit Another Report")
          }
        }
      }
    } else {
      // Step 1: Scan QR or Select Facility
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, DarkBlueBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkBlueSurface),
        shape = RoundedCornerShape(10.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "1. IDENTIFY FACILITY",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Scan QR Button
          OutlinedButton(
            onClick = {
              // Simulate immediate QR scan detection
              val randomFacility = inspections.randomOrNull() ?: inspections.first()
              selectedInspection = randomFacility
              isScanningQr = false
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp)
              .testTag("scan_qr_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(AccentCyan)),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Scan Facility On-Site QR Code", fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Or Select from Facility List
          Text("Or choose from registered municipal sites:", color = TextMuted, fontSize = 11.sp)
          Spacer(modifier = Modifier.height(8.dp))

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            inspections.forEach { facility ->
              val isChosen = (facility.id == selectedInspection?.id)
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isChosen) PrimaryBlue.copy(alpha = 0.2f) else DarkBlueCard)
                  .border(
                    width = 1.dp,
                    color = if (isChosen) AccentCyan else DarkBlueBorder,
                    shape = RoundedCornerShape(6.dp)
                  )
                  .clickable { selectedInspection = facility }
                  .padding(10.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = facility.facilityName,
                      color = if (isChosen) Color.White else TextPrimary,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.SemiBold
                    )
                    Text(
                      text = "${facility.facilityType} • ${facility.address}",
                      color = TextMuted,
                      fontSize = 11.sp
                    )
                  }
                  if (isChosen) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                  }
                }
              }
            }
          }
        }
      }

      // Step 2: Verified Facility Banner (Clean public info only)
      if (selectedInspection != null) {
        val facility = selectedInspection!!
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBlueBorder, RoundedCornerShape(10.dp)),
          colors = CardDefaults.cardColors(containerColor = DarkBlueCard),
          shape = RoundedCornerShape(10.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("FACILITY CONFIRMED", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              Text("QR ID: ${facility.qrCodeIdentifier}", color = TextMuted, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(facility.facilityName, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(facility.address, color = TextSecondary, fontSize = 12.sp)
            }
          }
        }
      }

      // Step 3: Issue Reporting Form
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, DarkBlueBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkBlueSurface),
        shape = RoundedCornerShape(10.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "2. DESCRIBE OBSERVED ISSUE",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          Text("Select Concern Category", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          Spacer(modifier = Modifier.height(6.dp))

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ISSUE_CATEGORIES.forEach { cat ->
              val isSelected = (cat == selectedCategory)
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isSelected) PrimaryBlue else DarkBlueCard)
                  .border(
                    width = 1.dp,
                    color = if (isSelected) AccentCyan else DarkBlueBorder,
                    shape = RoundedCornerShape(6.dp)
                  )
                  .clickable { selectedCategory = cat }
                  .padding(horizontal = 12.dp, vertical = 8.dp)
              ) {
                Text(
                  text = cat,
                  color = if (isSelected) Color.White else TextSecondary,
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text("Detailed Description of Observation", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          Spacer(modifier = Modifier.height(6.dp))

          OutlinedTextField(
            value = issueDescription,
            onValueChange = { issueDescription = it },
            placeholder = { Text("State what you observed, exact building wing, color/smell of plume, or safety barrier condition...", color = TextMuted, fontSize = 12.sp) },
            modifier = Modifier
              .fillMaxWidth()
              .height(110.dp)
              .testTag("public_issue_description_field"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = DarkBlueCard,
              unfocusedContainerColor = DarkBlueCard,
              focusedBorderColor = PrimaryBlue,
              unfocusedBorderColor = DarkBlueBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(6.dp)
          )

          Spacer(modifier = Modifier.height(14.dp))

          Text("Your Contact Information (Optional)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          Spacer(modifier = Modifier.height(4.dp))
          Text("Provide your email or phone if you wish to receive resolution updates.", color = TextMuted, fontSize = 11.sp)
          Spacer(modifier = Modifier.height(6.dp))

          OutlinedTextField(
            value = reporterContact,
            onValueChange = { reporterContact = it },
            placeholder = { Text("e.g. citizen@metro.gov or (555) 019-2831", color = TextMuted, fontSize = 12.sp) },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = DarkBlueCard,
              unfocusedContainerColor = DarkBlueCard,
              focusedBorderColor = PrimaryBlue,
              unfocusedBorderColor = DarkBlueBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(6.dp),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(20.dp))

          // Submit Button
          Button(
            onClick = {
              if (selectedInspection != null && issueDescription.isNotBlank()) {
                val facility = selectedInspection!!
                onSubmitPublicReport(
                  facility.facilityName,
                  facility.qrCodeIdentifier,
                  selectedCategory,
                  issueDescription,
                  reporterContact.ifBlank { "Anonymous Citizen" }
                )
                submittedReportId = "PUB-REP-${System.currentTimeMillis().toString().takeLast(6)}"
              }
            },
            enabled = selectedInspection != null && issueDescription.isNotBlank(),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("submit_public_report_button"),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Submit Public Safety Report", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Recent Community Reports Filed
    if (publicReports.isNotEmpty()) {
      Text(
        text = "RECENT COMMUNITY SUBMISSIONS (${publicReports.size})",
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp
      )

      publicReports.forEach { rep ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBlueBorder, RoundedCornerShape(8.dp)),
          colors = CardDefaults.cardColors(containerColor = DarkBlueCard),
          shape = RoundedCornerShape(8.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(rep.issueCategory, color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Text(rep.status, color = StatusCompleted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(rep.facilityName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(rep.description, color = TextSecondary, fontSize = 12.sp, maxLines = 2)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Filed: ${rep.submittedAt} • Ref: ${rep.referenceCode}", color = TextMuted, fontSize = 10.sp)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}
