package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TripRecord
import com.example.ui.components.LiveRouteMap
import com.example.ui.theme.CockpitCardBg
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.HighContrastWhite
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun TripsScreen(
    tripHistory: List<TripRecord>,
    modifier: Modifier = Modifier
) {
    var selectedTrip by remember { mutableStateOf<TripRecord?>(null) }
    val accentColor = MaterialTheme.colorScheme.primary

    if (selectedTrip != null) {
        TripDetailView(
            trip = selectedTrip!!,
            onBack = { selectedTrip = null },
            accentColor = accentColor,
            modifier = modifier
        )
    } else {
        TripListView(
            trips = tripHistory,
            onSelectTrip = { selectedTrip = it },
            accentColor = accentColor,
            modifier = modifier
        )
    }
}

@Composable
fun TripListView(
    trips: List<TripRecord>,
    onSelectTrip: (TripRecord) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("trips_list_screen")
    ) {
        Text(
            text = "Mis recorridos",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = HighContrastWhite,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(trips) { trip ->
                TripCardItem(
                    trip = trip,
                    accentColor = accentColor,
                    onClick = { onSelectTrip(trip) }
                )
            }
        }
    }
}

@Composable
fun TripCardItem(
    trip: TripRecord,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CockpitCardBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("trip_card_${trip.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.dateDisplay,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = HighContrastWhite
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onClick() }
                ) {
                    Text(
                        text = "Ver mapa",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Ver mapa",
                        tint = accentColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Distancia: ${String.format("%.2f", trip.distanceKm)} km",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = HighContrastWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Pico: ${trip.peakSpeedKmh} km/h  ·  Promedio: ${trip.avgSpeedKmh} km/h",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "En movimiento: ${trip.movingTimeText}  ·  Puntos GPS: ${trip.gpsPointsCount}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Ocultar viaje",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
        }
    }
}

@Composable
fun TripDetailView(
    trip: TripRecord,
    onBack: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("trip_detail_view")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Regresar",
                    tint = HighContrastWhite
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Detalle del Viaje",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = HighContrastWhite
                )
                Text(
                    text = trip.dateDisplay,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        var isMapFullscreen by remember { mutableStateOf(false) }

        // Real interactive map using MotoSpeed MapTileManager & LiveRouteMap
        val routePoints = trip.routePoints
        val centerLat = if (routePoints.isNotEmpty()) routePoints[routePoints.size / 2].latitude else (trip.startLat + trip.endLat) / 2.0
        val centerLon = if (routePoints.isNotEmpty()) routePoints[routePoints.size / 2].longitude else (trip.startLon + trip.endLon) / 2.0

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CockpitCardBorder, RoundedCornerShape(18.dp))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MAPA REAL DE LA RUTA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            letterSpacing = 1.sp
                        )
                    }

                    TextButton(onClick = { isMapFullscreen = !isMapFullscreen }) {
                        Icon(
                            imageVector = if (isMapFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = null,
                            tint = HighContrastWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isMapFullscreen) "Reducir" else "Pantalla Completa",
                            fontSize = 11.sp,
                            color = HighContrastWhite
                        )
                    }
                }

                LiveRouteMap(
                    currentLat = centerLat,
                    currentLon = centerLon,
                    heading = 0f,
                    currentSpeedKmh = 0f,
                    routePoints = routePoints,
                    isRecording = false,
                    showHeatmap = true,
                    isInteractive = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isMapFullscreen) 440.dp else 220.dp)
                )

                // Route summary info bar under the map
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF13141C))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Inicio: ${trip.startLocationName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafeGreen
                        )
                        Text(
                            text = "(${String.format(Locale.US, "%.4f, %.4f", trip.startLat, trip.startLon)})",
                            fontSize = 9.sp,
                            color = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Fin: ${trip.endLocationName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Text(
                            text = "(${String.format(Locale.US, "%.4f, %.4f", trip.endLat, trip.endLon)})",
                            fontSize = 9.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Speed Telemetry Chart matching screenshots 3 and 6
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CockpitCardBorder, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VELOCIDAD · km/h",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Pico: ${trip.peakSpeedKmh} km/h",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighContrastWhite
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chart Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Grid horizontal lines for 0, 60, 120 km/h
                    drawLine(Color(0xFF2A2B3A), Offset(0f, 0f), Offset(w, 0f), 1f)
                    drawLine(Color(0xFF2A2B3A), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), 1f)
                    drawLine(Color(0xFF2A2B3A), Offset(0f, h), Offset(w, h), 1f)

                    val samples = if (trip.speedHistorySamples.isNotEmpty()) {
                        trip.speedHistorySamples
                    } else {
                        listOf(0f, 20f, 45f, 60f, 35f, 80f, 104f, 75f, 50f, 15f, 0f)
                    }

                    val path = Path()
                    samples.forEachIndexed { i, speed ->
                        val x = (w / (samples.size - 1)) * i
                        val y = h - (speed / 120f) * h
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = accentColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("00:00:00", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("00:21:37", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(trip.totalTimeText, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // RESUMEN DEL VIAJE Card matching screenshot 3
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CockpitCardBorder, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "RESUMEN DEL VIAJE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("DISTANCIA", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        Text("${String.format("%.2f", trip.distanceKm)} km", fontSize = 20.sp, fontWeight = FontWeight.Black, color = HighContrastWhite)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PICO GPS", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        Text("${trip.peakSpeedKmh} km/h", fontSize = 20.sp, fontWeight = FontWeight.Black, color = HighContrastWhite)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("VELOCIDAD MEDIA", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        Text("${trip.avgSpeedKmh} km/h", fontSize = 20.sp, fontWeight = FontWeight.Black, color = HighContrastWhite)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("EN MOVIMIENTO", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        Text(trip.movingTimeText, fontSize = 20.sp, fontWeight = FontWeight.Black, color = HighContrastWhite)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${trip.gpsPointsCount} puntos GPS registrados · Tiempo total: ${trip.totalTimeText}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
