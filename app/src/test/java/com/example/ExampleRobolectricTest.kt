package com.example

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.InspectionStatus
import com.example.data.model.RiskLevel
import com.example.data.repository.SmartInspectRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SmartInspect", appName)
  }

  @Test
  fun `verify smartinspect seed inspections and highest risk trigger`() {
    val repository = SmartInspectRepository()
    val inspections = repository.inspections.value
    assertTrue(inspections.isNotEmpty())

    // Highest active risk facility
    val highestRisk = inspections.maxByOrNull { it.riskScore }
    assertNotNull(highestRisk)
    assertEquals("Metro Chemical Refinement", highestRisk!!.facilityName)
    assertEquals(87, highestRisk.riskScore)
    assertEquals(RiskLevel.High, highestRisk.riskLevel)
    assertEquals("Missing Evidence", highestRisk.primaryRiskTrigger)
    assertTrue(highestRisk.additionalRiskFactors.contains("Repeated Complaint"))
    assertTrue(highestRisk.additionalRiskFactors.contains("Late Inspection"))
  }

  @Test
  fun `verify camera evidence capture updates risk and audit trail`() {
    val repository = SmartInspectRepository()
    val inspectionId = "INS-2026-041"

    val before = repository.getInspectionById(inspectionId)!!
    val initialAuditCount = before.auditTrail.size

    val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    repository.addEvidencePhoto(
      inspectionId = inspectionId,
      bitmap = testBitmap,
      category = "Structural Integrity",
      inspectorNotes = "On-site verified crack alignment",
      inspectorName = "Alex Menon"
    )

    val after = repository.getInspectionById(inspectionId)!!
    assertTrue(after.hasEvidence)
    assertEquals(1, after.evidencePhotos.size)
    assertTrue(after.auditTrail.size > initialAuditCount)
    assertTrue(after.auditTrail.any { it.action == "Evidence Captured" })
  }

  @Test
  fun `verify submission requires camera evidence`() {
    val repository = SmartInspectRepository()
    // A pending inspection with no evidence
    val pendingNoEvidence = repository.inspections.value.first { !it.hasEvidence && it.status == InspectionStatus.Pending }
    val (success, message) = repository.submitInspection(pendingNoEvidence.id, "Alex Menon")
    assertFalse(success)
    assertTrue(message.contains("evidence", ignoreCase = true))
  }
}

