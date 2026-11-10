package com.example.data

import com.example.model.ActiveTripState
import com.example.model.BikeGraphicStyle
import com.example.model.ComponentInterval
import com.example.model.EmergencyContact
import com.example.model.FuelEntry
import com.example.model.FuelStats
import com.example.model.MediaPlaybackState
import com.example.model.MotoProfile
import com.example.model.RoutePoint
import com.example.model.ServiceCategory
import com.example.model.ServiceRecord
import com.example.model.SingleTireData
import com.example.model.TireStatus
import com.example.model.TpmsData
import com.example.model.TripRecord
import com.example.model.VisualThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MotoRepository {

    private val _profile = MutableStateFlow(
        MotoProfile(
            nickname = "LA CHUCHA",
            brandModel = "ITALIKA 300Z",
            plateNumber = "94MZB1",
            currentOdometerKm = 3954.8,
            themeMode = VisualThemeMode.NEON_MAGENTA,
            graphicStyle = BikeGraphicStyle.TRANSPARENT_STUDIO_PNG
        )
    )
    val profile: StateFlow<MotoProfile> = _profile.asStateFlow()

    private val _tpms = MutableStateFlow(
        TpmsData(
            isConnected = true,
            frontTire = SingleTireData(
                pressurePsi = 29.4,
                temperatureC = 28.2,
                batteryPercent = 94,
                status = TireStatus.NORMAL
            ),
            rearTire = SingleTireData(
                pressurePsi = 32.8,
                temperatureC = 31.0,
                batteryPercent = 89,
                status = TireStatus.NORMAL
            )
        )
    )
    val tpms: StateFlow<TpmsData> = _tpms.asStateFlow()

    private val _activeTrip = MutableStateFlow(ActiveTripState())
    val activeTrip: StateFlow<ActiveTripState> = _activeTrip.asStateFlow()

    private fun generateRoute(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
        speeds: List<Float>
    ): List<RoutePoint> {
        val count = speeds.size
        return speeds.mapIndexed { index, speed ->
            val t = index.toFloat() / (count - 1).coerceAtLeast(1)
            val curveOffset = kotlin.math.sin(t * Math.PI) * 0.005
            val lat = startLat + (endLat - startLat) * t + (curveOffset * 0.5)
            val lon = startLon + (endLon - startLon) * t - (curveOffset * 0.7)
            RoutePoint(
                latitude = lat,
                longitude = lon,
                speedKmh = speed,
                timestamp = System.currentTimeMillis() - ((count - index) * 60_000L),
                altitude = 2240.0 + (index * 2.0)
            )
        }
    }

    private val trip1Speeds = listOf(0f, 15f, 32f, 48f, 25f, 55f, 72f, 85f, 42f, 68f, 94f, 104f, 88f, 65f, 40f, 60f, 78f, 52f, 20f, 0f)
    private val trip2Speeds = listOf(0f, 22f, 45f, 63f, 50f, 30f, 15f, 0f)
    private val trip3Speeds = listOf(0f, 18f, 35f, 42f, 28f, 12f, 0f)

    private val _tripHistory = MutableStateFlow(
        listOf(
            TripRecord(
                id = "trip_01",
                dateDisplay = "05 nov, 22:14",
                distanceKm = 33.61,
                peakSpeedKmh = 104,
                avgSpeedKmh = 47,
                movingTimeText = "00:40:08",
                totalTimeText = "00:43:15",
                gpsPointsCount = 2595,
                startLocationName = "Polanco, CDMX",
                endLocationName = "Nezahualcóyotl, Edo Mex",
                startLat = 19.4342,
                startLon = -99.1963,
                endLat = 19.4005,
                endLon = -99.0145,
                routePoints = generateRoute(19.4342, -99.1963, 19.4005, -99.0145, trip1Speeds),
                speedHistorySamples = trip1Speeds
            ),
            TripRecord(
                id = "trip_02",
                dateDisplay = "05 nov, 22:03",
                distanceKm = 4.40,
                peakSpeedKmh = 63,
                avgSpeedKmh = 23,
                movingTimeText = "00:08:32",
                totalTimeText = "00:10:15",
                gpsPointsCount = 689,
                startLocationName = "Condesa, CDMX",
                endLocationName = "Roma Norte",
                startLat = 19.4116,
                startLon = -99.1729,
                endLat = 19.4194,
                endLon = -99.1598,
                routePoints = generateRoute(19.4116, -99.1729, 19.4194, -99.1598, trip2Speeds),
                speedHistorySamples = trip2Speeds
            ),
            TripRecord(
                id = "trip_03",
                dateDisplay = "05 nov, 20:46",
                distanceKm = 1.74,
                peakSpeedKmh = 42,
                avgSpeedKmh = 16,
                movingTimeText = "00:04:44",
                totalTimeText = "00:06:12",
                gpsPointsCount = 398,
                startLocationName = "Narvarte",
                endLocationName = "Del Valle",
                startLat = 19.3958,
                startLon = -99.1561,
                endLat = 19.3789,
                endLon = -99.1672,
                routePoints = generateRoute(19.3958, -99.1561, 19.3789, -99.1672, trip3Speeds),
                speedHistorySamples = trip3Speeds
            )
        )
    )
    val tripHistory: StateFlow<List<TripRecord>> = _tripHistory.asStateFlow()

    private val _componentIntervals = MutableStateFlow(
        listOf(
            ComponentInterval(
                id = "interval_oil",
                name = "Aceite de Motor Sintético",
                category = ServiceCategory.ACEITE,
                remainingKm = 2773.1,
                intervalKm = 3000.0,
                lastServiceKm = 3000.0,
                nextServiceKm = 6000.0,
                isUrgent = false,
                recommendation = "Dentro del intervalo (Próximo: 6000 km)"
            ),
            ComponentInterval(
                id = "interval_chain",
                name = "Ajuste y Lubricación de Cadena",
                category = ServiceCategory.CADENA,
                remainingKm = 145.0,
                intervalKm = 500.0,
                lastServiceKm = 3600.0,
                nextServiceKm = 4100.0,
                isUrgent = true,
                recommendation = "¡Atención! Lubricación requerida en 145 km"
            ),
            ComponentInterval(
                id = "interval_brakes",
                name = "Pastillas de Freno Delanteras",
                category = ServiceCategory.FRENOS,
                remainingKm = 1450.0,
                intervalKm = 5000.0,
                lastServiceKm = 1500.0,
                nextServiceKm = 6500.0,
                isUrgent = false,
                recommendation = "Espesor óptimo (70% vida útil)"
            ),
            ComponentInterval(
                id = "interval_spark",
                name = "Bujía NGK Iridium",
                category = ServiceCategory.FILTROS,
                remainingKm = 4045.2,
                intervalKm = 8000.0,
                lastServiceKm = 0.0,
                nextServiceKm = 8000.0,
                isUrgent = false,
                recommendation = "Calibración y chispa correctas"
            ),
            ComponentInterval(
                id = "interval_tires",
                name = "Neumáticos Michelin Pilot",
                category = ServiceCategory.LLANTAS,
                remainingKm = 6500.0,
                intervalKm = 12000.0,
                lastServiceKm = 0.0,
                nextServiceKm = 12000.0,
                isUrgent = false,
                recommendation = "Profundidad de dibujo 3.8 mm"
            )
        )
    )
    val componentIntervals: StateFlow<List<ComponentInterval>> = _componentIntervals.asStateFlow()

    private val _serviceHistory = MutableStateFlow(
        listOf(
            ServiceRecord(
                id = "srv_01",
                dateDisplay = "26 oct, 2026",
                odometerKm = 3000.0,
                category = ServiceCategory.ACEITE,
                title = "Cambio de Aceite Motul 7100 10W-40 y Filtro",
                costMxn = 680.0,
                notes = "Cambio preventivo de 3,000 km, arandela de cobre nueva",
                workshopName = "Taller MotoZone Performance"
            ),
            ServiceRecord(
                id = "srv_02",
                dateDisplay = "15 sep, 2026",
                odometerKm = 2100.0,
                category = ServiceCategory.CADENA,
                title = "Tensado y Desengrase Profundo de Cadena",
                costMxn = 250.0,
                notes = "Holgura ajustada a 25 mm, lubricante cerámico C4",
                workshopName = "Taller MotoZone Performance"
            ),
            ServiceRecord(
                id = "srv_03",
                dateDisplay = "02 ago, 2026",
                odometerKm = 1000.0,
                category = ServiceCategory.GENERAL,
                title = "Primer Servicio de Asentamiento de Motor",
                costMxn = 1250.0,
                notes = "Revisión de tornillería general, calibración de válvulas y aceite",
                workshopName = "Agencia Oficial Italika"
            )
        )
    )
    val serviceHistory: StateFlow<List<ServiceRecord>> = _serviceHistory.asStateFlow()

    private val _fuelStats = MutableStateFlow(FuelStats())
    val fuelStats: StateFlow<FuelStats> = _fuelStats.asStateFlow()

    private val _fuelHistory = MutableStateFlow(
        listOf(
            FuelEntry(
                id = "fuel_01",
                dateDisplay = "03 nov, 17:41",
                liters = 8.13,
                costMxn = 200.00,
                odometerKm = 3769.2,
                fullTank = true
            ),
            FuelEntry(
                id = "fuel_02",
                dateDisplay = "24 oct, 12:15",
                liters = 10.50,
                costMxn = 260.00,
                odometerKm = 3550.0,
                fullTank = true
            ),
            FuelEntry(
                id = "fuel_03",
                dateDisplay = "12 oct, 18:30",
                liters = 7.90,
                costMxn = 195.00,
                odometerKm = 3280.4,
                fullTank = false
            )
        )
    )
    val fuelHistory: StateFlow<List<FuelEntry>> = _fuelHistory.asStateFlow()

    private val _emergencyContacts = MutableStateFlow(
        listOf(
            EmergencyContact(
                id = "em_01",
                name = "Andrea (Esposa)",
                phone = "5512345678",
                relationship = "Familiar / Contacto ICE",
                isPrimary = true,
                voiceCommandAlias = "Andrea"
            ),
            EmergencyContact(
                id = "em_02",
                name = "Carlos M. (Amigo Rodadas)",
                phone = "5587654321",
                relationship = "Compañero Motociclista",
                isPrimary = false,
                voiceCommandAlias = "Carlos"
            ),
            EmergencyContact(
                id = "em_03",
                name = "Seguro Moto Asistencia",
                phone = "8009110000",
                relationship = "Grúa y Asistencia Vial",
                isPrimary = false,
                voiceCommandAlias = "Seguro"
            )
        )
    )
    val emergencyContacts: StateFlow<List<EmergencyContact>> = _emergencyContacts.asStateFlow()

    private val _mediaState = MutableStateFlow(MediaPlaybackState())
    val mediaState: StateFlow<MediaPlaybackState> = _mediaState.asStateFlow()

    fun updateProfile(transform: (MotoProfile) -> MotoProfile) {
        _profile.update(transform)
    }

    fun updateOdometer(newKm: Double) {
        _profile.update { it.copy(currentOdometerKm = newKm) }
        recalculateIntervals(newKm)
    }

    private fun recalculateIntervals(currentKm: Double) {
        _componentIntervals.update { list ->
            list.map { interval ->
                val remaining = (interval.nextServiceKm - currentKm).coerceAtLeast(0.0)
                val isUrgent = remaining <= 250.0
                interval.copy(
                    remainingKm = remaining,
                    isUrgent = isUrgent
                )
            }
        }
    }

    fun addServiceRecord(record: ServiceRecord) {
        _serviceHistory.update { listOf(record) + it }
        // Update interval if applicable
        _componentIntervals.update { list ->
            list.map { interval ->
                if (interval.category == record.category || record.category == ServiceCategory.GENERAL) {
                    val nextKm = record.odometerKm + interval.intervalKm
                    val remaining = (nextKm - _profile.value.currentOdometerKm).coerceAtLeast(0.0)
                    interval.copy(
                        lastServiceKm = record.odometerKm,
                        nextServiceKm = nextKm,
                        remainingKm = remaining,
                        isUrgent = remaining <= 250.0
                    )
                } else interval
            }
        }
    }

    fun addFuelEntry(entry: FuelEntry) {
        _fuelHistory.update { listOf(entry) + it }
        // Recalculate efficiency
        val history = _fuelHistory.value
        if (history.size >= 2) {
            val deltaKm = history[0].odometerKm - history[1].odometerKm
            val liters = history[0].liters
            if (deltaKm > 0 && liters > 0) {
                val newEfficiency = deltaKm / liters
                _fuelStats.update { it.copy(estimatedEfficiencyKmPerL = String.format("%.1f", newEfficiency).toDoubleOrNull() ?: 26.3) }
            }
        }
    }

    fun updateFuelStatsEstimatedEfficiency(newEfficiency: Double) {
        _fuelStats.update { it.copy(estimatedEfficiencyKmPerL = newEfficiency) }
    }

    fun updateFuelEntry(entry: FuelEntry) {
        _fuelHistory.update { list ->
            list.map { if (it.id == entry.id) entry else it }
        }
    }

    fun deleteFuelEntry(entryId: String) {
        _fuelHistory.update { list ->
            list.filter { it.id != entryId }
        }
    }

    fun addEmergencyContact(contact: EmergencyContact) {
        _emergencyContacts.update { list ->
            if (contact.isPrimary) {
                listOf(contact) + list.map { it.copy(isPrimary = false) }
            } else {
                list + contact
            }
        }
    }

    fun updateEmergencyContact(contact: EmergencyContact) {
        _emergencyContacts.update { list ->
            list.map {
                if (it.id == contact.id) {
                    contact
                } else if (contact.isPrimary && it.isPrimary) {
                    it.copy(isPrimary = false)
                } else {
                    it
                }
            }
        }
    }

    fun deleteEmergencyContact(contactId: String) {
        _emergencyContacts.update { list ->
            list.filter { it.id != contactId }
        }
    }

    fun toggleRide(active: Boolean) {
        if (!active) {
            val current = _activeTrip.value
            if (current.distanceKm > 0.1) {
                val newTrip = TripRecord(
                    id = "trip_${System.currentTimeMillis()}",
                    dateDisplay = "Hoy, en vivo",
                    distanceKm = current.distanceKm,
                    peakSpeedKmh = current.peakSpeedKmh,
                    avgSpeedKmh = current.avgSpeedKmh,
                    movingTimeText = String.format("%02d:%02d:%02d", current.movingSeconds / 3600, (current.movingSeconds % 3600) / 60, current.movingSeconds % 60),
                    totalTimeText = String.format("%02d:%02d:%02d", current.totalSeconds / 3600, (current.totalSeconds % 3600) / 60, current.totalSeconds % 60),
                    gpsPointsCount = (current.distanceKm * 75).toInt() + 10,
                    startLocationName = "Mi ubicación",
                    endLocationName = current.currentStreet
                )
                _tripHistory.update { listOf(newTrip) + it }
            }
            _activeTrip.value = ActiveTripState(isRiding = false)
        } else {
            _activeTrip.value = ActiveTripState(isRiding = true)
        }
    }

    fun updateActiveTelemetry(speed: Int, deltaDistance: Double) {
        _activeTrip.update { state ->
            if (!state.isRiding) return@update state
            val newPeak = maxOf(state.peakSpeedKmh, speed)
            val newDist = state.distanceKm + deltaDistance
            val newMoving = if (speed > 2) state.movingSeconds + 1 else state.movingSeconds
            val newTotal = state.totalSeconds + 1
            val avg = if (newMoving > 0) ((newDist / (newMoving / 3600.0))).toInt() else 0

            state.copy(
                currentSpeedKmh = speed,
                peakSpeedKmh = newPeak,
                distanceKm = newDist,
                movingSeconds = newMoving,
                totalSeconds = newTotal,
                avgSpeedKmh = avg
            )
        }
    }

    fun toggleMediaPlayPause() {
        _mediaState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun updateMediaState(
        title: String,
        artist: String,
        album: String,
        isPlaying: Boolean,
        progressFraction: Float,
        currentPosition: Int,
        duration: Int
    ) {
        _mediaState.update {
            it.copy(
                title = title,
                artist = artist,
                album = album,
                isPlaying = isPlaying,
                progressFraction = progressFraction,
                currentPositionSeconds = currentPosition,
                durationSeconds = duration,
                activeApp = "Spotify"
            )
        }
    }

    fun nextMediaTrack() {
        val tracks = listOf(
            Triple("Midnight Rider", "Cyber Highway", "Neon Asphalt"),
            Triple("Torque & Gasoline", "The Twin Cylinders", "Full Throttle"),
            Triple("Blinding Lights", "The Weeknd", "After Hours"),
            Triple("Speed of Sound", "Synth Riders", "Urban Pulse")
        )
        val next = tracks.random()
        _mediaState.update {
            it.copy(
                title = next.first,
                artist = next.second,
                album = next.third,
                currentPositionSeconds = 0,
                progressFraction = 0f,
                isPlaying = true
            )
        }
    }

    fun updateTpmsPressure(frontPsi: Double, rearPsi: Double) {
        val frontStatus = when {
            frontPsi < 24.0 -> TireStatus.CRITICAL_LOW
            frontPsi < 27.0 -> TireStatus.WARNING_LOW
            frontPsi > 36.0 -> TireStatus.WARNING_HIGH
            else -> TireStatus.NORMAL
        }
        val rearStatus = when {
            rearPsi < 26.0 -> TireStatus.CRITICAL_LOW
            rearPsi < 30.0 -> TireStatus.WARNING_LOW
            rearPsi > 40.0 -> TireStatus.WARNING_HIGH
            else -> TireStatus.NORMAL
        }
        _tpms.update {
            it.copy(
                frontTire = it.frontTire.copy(pressurePsi = frontPsi, status = frontStatus),
                rearTire = it.rearTire.copy(pressurePsi = rearPsi, status = rearStatus),
                lastUpdateEpochMs = System.currentTimeMillis()
            )
        }
    }
}
