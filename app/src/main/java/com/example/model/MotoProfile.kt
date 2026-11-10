package com.example.model

data class MotoProfile(
    val nickname: String = "LA CHUCHA",
    val brandModel: String = "ITALIKA 300Z",
    val plateNumber: String = "94MZB1",
    val currentOdometerKm: Double = 3954.8,
    val targetTirePressureFrontPsi: Double = 29.0,
    val targetTirePressureRearPsi: Double = 33.0,
    val themeMode: VisualThemeMode = VisualThemeMode.NEON_MAGENTA,
    val graphicStyle: BikeGraphicStyle = BikeGraphicStyle.TRANSPARENT_STUDIO_PNG,
    val customImageUri: String? = null,
    val glowColorHex: String = "#FF007F"
)
