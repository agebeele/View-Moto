package com.example.model

enum class TireStatus {
    NORMAL,
    WARNING_LOW,
    WARNING_HIGH,
    CRITICAL_LOW
}

data class SingleTireData(
    val pressurePsi: Double,
    val temperatureC: Double,
    val batteryPercent: Int,
    val status: TireStatus
)

data class TpmsData(
    val isConnected: Boolean = true,
    val frontTire: SingleTireData = SingleTireData(
        pressurePsi = 29.4,
        temperatureC = 27.8,
        batteryPercent = 94,
        status = TireStatus.NORMAL
    ),
    val rearTire: SingleTireData = SingleTireData(
        pressurePsi = 32.8,
        temperatureC = 30.5,
        batteryPercent = 91,
        status = TireStatus.NORMAL
    ),
    val lastUpdateEpochMs: Long = System.currentTimeMillis()
)
