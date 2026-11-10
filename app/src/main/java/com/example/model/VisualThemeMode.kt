package com.example.model

enum class VisualThemeMode(val displayName: String, val hexAccent: String) {
    NEON_MAGENTA("Magenta Neo", "#FF007F"),
    CYBER_CYAN("Cyber Cyan", "#00E5FF"),
    RACING_ORANGE("Racing Orange", "#FF6D00"),
    ACID_LIME("Acid Lime", "#76FF03"),
    SOLAR_DAY("Solar Día (Alto Contraste)", "#D8006C")
}

enum class BikeGraphicStyle(val displayName: String) {
    TRANSPARENT_STUDIO_PNG("PNG Estudio Transparente"),
    VECTOR_NEON_SILHOUETTE("Silueta Vectorial Neón"),
    CONTOUR_OUTLINE("Contorno Wireframe")
}
