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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Inspection
import com.example.data.model.InspectionStatus
import com.example.data.model.RiskLevel
import com.example.ui.components.StatusBadge
import com.example.ui.components.TrustRiskBadge
import com.example.ui.theme.AccentCyan
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

@Composable
fun InspectionsScreen(
  inspections: List<Inspection>,
  onInspectionSelected: (Inspection) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableStateOf("All") }

  val filterOptions = listOf("All", "Pending", "In Progress", "Completed", "High Risk")

  val filteredInspections = inspections.filter { insp ->
    val matchesSearch = searchQuery.isBlank() ||
        insp.facilityName.contains(searchQuery, ignoreCase = true) ||
        insp.assignedInspector.contains(searchQuery, ignoreCase = true) ||
        insp.id.contains(searchQuery, ignoreCase = true)

    val matchesFilter = when (selectedFilter) {
      "Pending" -> insp.status == InspectionStatus.Pending
      "In Progress" -> insp.status == InspectionStatus.InProgress
      "Completed" -> insp.status == InspectionStatus.Completed
      "High Risk" -> insp.riskLevel == RiskLevel.High
      else -> true
    }

    matchesSearch && matchesFilter
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("inspections_screen")
  ) {
    // Top Title & Subtitle
    Text(
      text = "INSPECTION LEDGER",
      color = AccentCyan,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp
    )
    Text(
      text = "All Facilities & Audits",
      color = TextPrimary,
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Search Field
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search facility, inspector, or audit ID...", color = TextMuted, fontSize = 13.sp) },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted, modifier = Modifier.size(18.dp))
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { searchQuery = "" }) {
            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
          }
        }
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag("inspection_search_field"),
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = DarkBlueCard,
        unfocusedContainerColor = DarkBlueCard,
        focusedBorderColor = PrimaryBlue,
        unfocusedBorderColor = DarkBlueBorder,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
      ),
      shape = RoundedCornerShape(8.dp),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Horizontal Filter Pills
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      filterOptions.forEach { filter ->
        val isSelected = (filter == selectedFilter)
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) PrimaryBlue else DarkBlueSurfaceVariant)
            .border(
              width = 1.dp,
              color = if (isSelected) AccentCyan else DarkBlueBorder,
              shape = RoundedCornerShape(6.dp)
            )
            .clickable { selectedFilter = filter }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("filter_chip_$filter"),
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

    // Responsive Inspection Table
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .border(1.dp, DarkBlueBorder, RoundedCornerShape(8.dp)),
      colors = CardDefaults.cardColors(containerColor = DarkBlueSurface),
      shape = RoundedCornerShape(8.dp)
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Table Header: Dedicated Columns (INSPECTION | ASSIGNED TO | STATUS | TRUST / RISK)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(DarkBlueCard)
            .border(0.dp, DarkBlueDivider)
            .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "INSPECTION",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.weight(2.0f)
          )
          Text(
            text = "ASSIGNED TO",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.weight(1.4f)
          )
          Text(
            text = "STATUS",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.weight(1.3f)
          )
          Text(
            text = "TRUST / RISK",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.weight(1.3f)
          )
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DarkBlueBorder)
        )

        // Inspection Rows
        if (filteredInspections.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "No inspections match your criteria",
                color = TextSecondary,
                fontSize = 14.sp
              )
            }
          }
        } else {
          LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredInspections) { inspection ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { onInspectionSelected(inspection) }
                  .padding(horizontal = 12.dp, vertical = 12.dp)
                  .testTag("inspection_row_${inspection.id}"),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Column 1: INSPECTION (Facility Name & ID)
                Column(
                  modifier = Modifier
                    .weight(2.0f)
                    .padding(end = 6.dp)
                ) {
                  Text(
                    text = inspection.facilityName,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = inspection.id,
                    color = AccentCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                  )
                }

                // Column 2: ASSIGNED TO (Inspector name)
                Column(
                  modifier = Modifier
                    .weight(1.4f)
                    .padding(end = 6.dp)
                ) {
                  Text(
                    text = inspection.assignedInspector,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }

                // Column 3: STATUS (Pending, In Progress, Completed)
                Box(
                  modifier = Modifier
                    .weight(1.3f)
                    .padding(end = 6.dp),
                  contentAlignment = Alignment.CenterStart
                ) {
                  StatusBadge(status = inspection.status)
                }

                // Column 4: TRUST / RISK (T:64 | R:72)
                Box(
                  modifier = Modifier.weight(1.3f),
                  contentAlignment = Alignment.CenterStart
                ) {
                  TrustRiskBadge(
                    trustScore = inspection.trustScore,
                    riskScore = inspection.riskScore
                  )
                }
              }

              // Divider between rows
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(1.dp)
                  .background(DarkBlueDivider)
              )
            }
          }
        }
      }
    }
  }
}
