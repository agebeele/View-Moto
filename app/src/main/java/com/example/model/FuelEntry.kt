package com.example.model

data class FuelEntry(
    val id: String,
    val dateDisplay: String,
    val liters: Double,
    val costMxn: Double,
    val odometerKm: Double,
    val fullTank: Boolean = true
)

data class FuelStats(
    val estimatedEfficiencyKmPerL: Double = 26.3,
    val lastOdometer: Double = 3769.2,
    val averageFuelPricePerLiter: Double = 24.60
)
