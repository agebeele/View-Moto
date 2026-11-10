package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ActiveTripState
import com.example.model.MediaPlaybackState
import com.example.model.MotoProfile
import com.example.model.TpmsData
import com.example.ui.components.SpeedometerGauge
import com.example.ui.components.TpmsSection
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CockpitCardBg
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.HighContrastWhite
import com.example.ui.theme.SafeGreen

@Composable
fun CockpitScreen(
    profile: MotoProfile,
    tpmsData: TpmsData,
    activeTrip: ActiveTripState,
    mediaState: MediaPlaybackState,
    customBikeBitmap: android.graphics.Bitmap?,
    onToggleRide: () -> Unit,
    onVoiceClick: () -> Unit,
    onEmergencyClick: () -> Unit,
    onToggleMedia: () -> Unit,
    onNextTrack: () -> Unit,
    onSimulateTpmsWarning: () -> Unit,
    onResetTpms: () -> Unit,
    onOpenSpotify: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentColor = MaterialTheme.colorScheme.primary
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("cockpit_screen")
    ) {
        // Cockpit Header matching "Mi 300Z"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Mi ${profile.brandModel.substringAfterLast(" ", "300Z")}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = HighContrastWhite,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "${profile.brandModel.substringBefore(" ", "ITALIKA")} · COMPUTADORA DE VIAJE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.4.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CockpitCardBg)
                    .border(1.dp, CockpitCardBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "300Z",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Motorcycle Graphic Card (Transparent PNG / Vector Studio)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F1017))
                .border(1.dp, CockpitCardBorder, RoundedCornerShape(20.dp))
                .padding(8.dp)
        ) {
            // Bike Image: Custom processed bitmap or bundled transparent 300Z PNG
            if (customBikeBitmap != null) {
                Image(
                    bitmap = customBikeBitmap.asImageBitmap(),
                    contentDescription = "Moto Customizada",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.moto_300z_transparent),
                    contentDescription = "Italika 300Z La Chucha",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bike Label Overlay (Matching screenshot: "LA CHUCHA", "ITALIKA 300Z")
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = profile.nickname,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = HighContrastWhite,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .width(48.dp)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = profile.brandModel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Big Prominent "READY TO RIDE" / "EN VIAJE" Button
        Button(
            onClick = onToggleRide,
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (activeTrip.isRiding) AlertRed else accentColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .testTag("ready_to_ride_button")
        ) {
            Icon(
                imageVector = if (activeTrip.isRiding) Icons.Default.PowerSettingsNew else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (activeTrip.isRiding) "FINALIZAR VIAJE" else "READY TO RIDE",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Ride Status Subtitle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (activeTrip.isRiding) SafeGreen else Color.Gray)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (activeTrip.isRiding) "EN MOVIMIENTO · REGISTRANDO RUTA GPS" else "EN ESPERA · Activa READY para registrar el trayecto",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Speedometer Custom Gauge
        SpeedometerGauge(
            currentSpeedKmh = activeTrip.currentSpeedKmh,
            accentColor = accentColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Stats 2x2 Grid (Distancia, Pico, Tiempo, Media) matching Screenshot 4
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                label = "DISTANCIA",
                value = String.format("%.2f km", activeTrip.distanceKm),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "VELOCIDAD PICO",
                value = "${activeTrip.peakSpeedKmh} km/h",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val movingMinutes = (activeTrip.movingSeconds % 3600) / 60
            val movingSecs = activeTrip.movingSeconds % 60
            StatCard(
                label = "TIEMPO EN RUTA",
                value = String.format("%02d:%02d", movingMinutes, movingSecs),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "VELOCIDAD MEDIA",
                value = "${activeTrip.avgSpeedKmh} km/h",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Real-Time TPMS Section
        TpmsSection(
            tpmsData = tpmsData,
            onSimulateWarning = onSimulateTpmsWarning,
            onResetNormal = onResetTpms
        )

        Spacer(modifier = Modifier.height(16.dp))

        // CarPlay Quick Controls Bar (Music & Navigation & Voice)
        CarPlayMediaBar(
            mediaState = mediaState,
            accentColor = accentColor,
            onToggleMedia = onToggleMedia,
            onNextTrack = onNextTrack,
            onOpenSpotify = onOpenSpotify
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Voice Commands & Emergency SOS Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Voice Assistant Trigger
            Button(
                onClick = onVoiceClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CockpitCardBg),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .testTag("voice_command_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Comando de Voz",
                    tint = accentColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Asistente Voz",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighContrastWhite
                )
            }

            // Emergency SOS Speed Dial Button
            Button(
                onClick = onEmergencyClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AlertRed.copy(alpha = 0.25f)),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .border(1.5.dp, AlertRed, RoundedCornerShape(16.dp))
                    .testTag("emergency_sos_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Emergency,
                    contentDescription = "SOS",
                    tint = AlertRed
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SOS Emergencia",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
        modifier = modifier
            .border(1.dp, CockpitCardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = HighContrastWhite
            )
        }
    }
}

@Composable
fun CarPlayMediaBar(
    mediaState: MediaPlaybackState,
    accentColor: Color,
    onToggleMedia: () -> Unit,
    onNextTrack: () -> Unit,
    onOpenSpotify: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CockpitCardBorder, RoundedCornerShape(20.dp))
            .clickable { onOpenSpotify() }
            .testTag("carplay_media_bar")
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1DB954).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF1DB954), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "Abrir Spotify",
                        tint = Color(0xFF1DB954),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = mediaState.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighContrastWhite,
                        maxLines = 1
                    )
                    Text(
                        text = "${mediaState.artist} • Spotify (Tus me gusta)",
                        fontSize = 11.sp,
                        color = Color(0xFF1DB954),
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .clickable { onToggleMedia() }
                ) {
                    Icon(
                        imageVector = if (mediaState.isPlaying) Icons.Default.PowerSettingsNew else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF252636))
                        .clickable { onNextTrack() }
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Siguiente",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
