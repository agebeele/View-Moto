package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ComponentInterval
import com.example.model.MotoProfile
import com.example.model.ServiceCategory
import com.example.model.ServiceRecord
import com.example.service.PdfReportExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MotoDash", appName)
    }

    @Test
    fun `pdf generation creates valid file`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val exporter = PdfReportExporter(context)

        val profile = MotoProfile()
        val intervals = listOf(
            ComponentInterval(
                id = "i1",
                name = "Aceite",
                category = ServiceCategory.ACEITE,
                remainingKm = 2773.1,
                intervalKm = 3000.0,
                lastServiceKm = 3000.0,
                nextServiceKm = 6000.0
            )
        )
        val records = listOf(
            ServiceRecord(
                id = "r1",
                dateDisplay = "01 nov 2026",
                odometerKm = 3000.0,
                category = ServiceCategory.ACEITE,
                title = "Cambio de Aceite",
                costMxn = 650.0
            )
        )

        val pdfFile = exporter.generateMaintenancePdf(profile, intervals, records)
        assertNotNull(pdfFile)
        assertTrue(pdfFile!!.exists())
        assertTrue(pdfFile.length() > 0)
    }

    @Test
    fun `viewModel navigation back stack returns step by step to previous tabs`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.ui.viewmodel.MotoDashboardViewModel(app)

        assertEquals(0, vm.selectedTab.value)
        assertEquals(false, vm.canNavigateBackTab.value)

        vm.selectTab(1) // Viajes
        vm.selectTab(3) // Gasolina
        vm.selectTab(4) // Datos

        assertEquals(4, vm.selectedTab.value)
        assertTrue(vm.canNavigateBackTab.value)

        assertTrue(vm.navigateBackTab())
        assertEquals(3, vm.selectedTab.value)

        assertTrue(vm.navigateBackTab())
        assertEquals(1, vm.selectedTab.value)

        assertTrue(vm.navigateBackTab())
        assertEquals(0, vm.selectedTab.value)
        assertEquals(false, vm.canNavigateBackTab.value)
    }
}
