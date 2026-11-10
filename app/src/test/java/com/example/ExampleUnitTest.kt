package com.example

import com.example.data.MotoRepository
import com.example.model.ServiceCategory
import com.example.model.ServiceRecord
import com.example.model.TireStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun defaultProfile_isCorrect() {
        val repo = MotoRepository()
        val profile = repo.profile.value
        assertEquals("LA CHUCHA", profile.nickname)
        assertEquals("ITALIKA 300Z", profile.brandModel)
        assertEquals(3954.8, profile.currentOdometerKm, 0.01)
    }

    @Test
    fun tpmsPressure_detectsWarningAndCritical() {
        val repo = MotoRepository()
        // Normal
        repo.updateTpmsPressure(29.0, 33.0)
        assertEquals(TireStatus.NORMAL, repo.tpms.value.frontTire.status)
        assertEquals(TireStatus.NORMAL, repo.tpms.value.rearTire.status)

        // Critical low on rear
        repo.updateTpmsPressure(29.0, 22.5)
        assertEquals(TireStatus.CRITICAL_LOW, repo.tpms.value.rearTire.status)
    }

    @Test
    fun addServiceRecord_updatesComponentInterval() {
        val repo = MotoRepository()
        val initialRecordCount = repo.serviceHistory.value.size

        val newRecord = ServiceRecord(
            id = "test_srv_1",
            dateDisplay = "10 nov 2026",
            odometerKm = 3955.0,
            category = ServiceCategory.ACEITE,
            title = "Cambio de Aceite Sintético",
            costMxn = 750.0
        )
        repo.addServiceRecord(newRecord)

        assertEquals(initialRecordCount + 1, repo.serviceHistory.value.size)
        val oilInterval = repo.componentIntervals.value.first { it.category == ServiceCategory.ACEITE }
        assertEquals(3955.0, oilInterval.lastServiceKm, 0.1)
    }

    @Test
    fun rideToggle_tracksTelemetryAndAddsTrip() {
        val repo = MotoRepository()
        assertFalse(repo.activeTrip.value.isRiding)

        repo.toggleRide(true)
        assertTrue(repo.activeTrip.value.isRiding)

        repo.updateActiveTelemetry(speed = 65, deltaDistance = 1.5)
        assertEquals(65, repo.activeTrip.value.currentSpeedKmh)
        assertEquals(65, repo.activeTrip.value.peakSpeedKmh)
        assertEquals(1.5, repo.activeTrip.value.distanceKm, 0.01)

        val beforeCount = repo.tripHistory.value.size
        repo.toggleRide(false)
        assertFalse(repo.activeTrip.value.isRiding)
        assertEquals(beforeCount + 1, repo.tripHistory.value.size)
    }
}
