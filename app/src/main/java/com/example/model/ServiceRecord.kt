package com.example.model

enum class ServiceCategory(val displayName: String) {
    TODOS("Todos"),
    ACEITE("Aceite de Motor"),
    FRENOS("Frenos"),
    CADENA("Cadena y Transmisión"),
    LLANTAS("Neumáticos"),
    FILTROS("Filtros y Bujía"),
    GENERAL("Servicio General")
}

data class ServiceRecord(
    val id: String,
    val dateDisplay: String,
    val odometerKm: Double,
    val category: ServiceCategory,
    val title: String,
    val costMxn: Double,
    val notes: String = "",
    val workshopName: String = "Taller Autorizado Italika",
    val isUrgent: Boolean = false
)

data class ComponentInterval(
    val id: String,
    val name: String,
    val category: ServiceCategory,
    val remainingKm: Double,
    val intervalKm: Double,
    val lastServiceKm: Double,
    val nextServiceKm: Double,
    val isUrgent: Boolean = false,
    val recommendation: String = "Dentro del intervalo"
)
