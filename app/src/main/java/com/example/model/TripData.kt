package com.example.model

data class RoutePoint(
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val timestampEpochMs: Long = timestamp,
    val altitude: Double? = null
)

data class TripRecord(
    val id: String,
    val dateDisplay: String,
    val distanceKm: Double,
    val peakSpeedKmh: Int,
    val avgSpeedKmh: Int,
    val movingTimeText: String,
    val totalTimeText: String,
    val gpsPointsCount: Int,
    val startLocationName: String = "Inicio de ruta",
    val endLocationName: String = "Destino",
    val startLat: Double = 19.432608,
    val startLon: Double = -99.133209,
    val endLat: Double = 19.359812,
    val endLon: Double = -99.162312,
    val routePoints: List<RoutePoint> = emptyList(),
    val speedHistorySamples: List<Float> = emptyList(),
    val isHidden: Boolean = false
)

data class ActiveTripState(
    val isRiding: Boolean = false,
    val currentSpeedKmh: Int = 0,
    val peakSpeedKmh: Int = 0,
    val distanceKm: Double = 0.0,
    val movingSeconds: Long = 0L,
    val totalSeconds: Long = 0L,
    val avgSpeedKmh: Int = 0,
    val headingDegrees: Float = 0f,
    val currentStreet: String = "Av. Insurgentes Sur",
    val nextTurnInstruction: String = "En 200m doble a la derecha",
    val gpsCoordinates: List<RoutePoint> = emptyList()
)
