package com.example.model

data class MotoProfile(
    val id: String = "moto_rt250",
    val nickname: String = "RUTH",
    val brandModel: String = "RT250 SPORT",
    val plateNumber: String = "94MZB1",
    val currentOdometerKm: Double = 1250.0,
    val targetTirePressureFrontPsi: Double = 29.0,
    val targetTirePressureRearPsi: Double = 33.0,
    val themeMode: VisualThemeMode = VisualThemeMode.CYBER_CYAN,
    val graphicStyle: BikeGraphicStyle = BikeGraphicStyle.TRANSPARENT_STUDIO_PNG,
    val customImageUri: String? = null,
    val glowColorHex: String = "#00F0FF"
) {
    val badgeLabel: String
        get() = if (brandModel.contains("RT", ignoreCase = true)) "RT250" else brandModel.substringAfterLast(" ", "RT250").take(6)
}
