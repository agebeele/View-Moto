package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.BikeGraphicStyle
import com.example.model.EmergencyContact
import com.example.model.MotoProfile
import com.example.model.VisualThemeMode
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CockpitCardBg
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.HighContrastWhite
import com.example.ui.theme.NeonMagenta

@Composable
fun BikeCustomizationScreen(
    profile: MotoProfile,
    emergencyContacts: List<EmergencyContact>,
    customBikeBitmap: android.graphics.Bitmap?,
    onThemeSelect: (VisualThemeMode) -> Unit,
    onUpdateBikeDetails: (String, String, BikeGraphicStyle) -> Unit,
    onProcessCustomImage: (Uri, BikeGraphicStyle) -> Unit,
    onExportPdf: () -> Unit,
    onOpenEmergencyDialog: () -> Unit,
    onOpenSpotify: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val accentColor = MaterialTheme.colorScheme.primary

    var nickname by remember { mutableStateOf(profile.nickname) }
    var modelName by remember { mutableStateOf(profile.brandModel) }
    var selectedGraphicStyle by remember { mutableStateOf(profile.graphicStyle) }
    var keepScreenOn by remember { mutableStateOf(true) }
    var floatingBubble by remember { mutableStateOf(true) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onProcessCustomImage(uri, selectedGraphicStyle)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("bike_customization_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Datos y Personalización",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = HighContrastWhite,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Motor IA de Vectorización, Spotify y Temas CarPlay",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.2.sp
            )
        }

        // Section: Spotify CarPlay & Tus Me Gusta
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141E18)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1DB954).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable { onOpenSpotify() }
                    .testTag("spotify_entry_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1DB954)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Headphones,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SPOTIFY CARPLAY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1DB954),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Tus Me Gusta y Cuentas",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = HighContrastWhite
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFF1DB954),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Inicia sesión con tu cuenta, cambia de perfil y reproduce tus canciones favoritas de 'Tus me gusta' con controles grandes adaptados para rodar en moto.",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onOpenSpotify,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Abrir Spotify y Reproductor",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Section: Motor IA de Vectorización y Gráfico de Moto
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CockpitCardBorder, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Motor IA",
                                tint = accentColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MOTOR IA / VECTORIZADOR",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = accentColor,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "PNG TRANSPARENTE",
                                color = accentColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Motorcycle Preview Box
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0C0D12))
                            .border(1.dp, CockpitCardBorder, RoundedCornerShape(16.dp))
                            .padding(8.dp)
                    ) {
                        if (customBikeBitmap != null) {
                            Image(
                                bitmap = customBikeBitmap.asImageBitmap(),
                                contentDescription = "Moto Vectorizada",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.moto_300z_transparent),
                                contentDescription = "Moto Italika 300Z Transparente",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Estilo de Gráfico para el Panel de Control:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighContrastWhite
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Style Switcher Chips
                    BikeGraphicStyle.entries.forEach { style ->
                        val isSelected = selectedGraphicStyle == style
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent)
                                .border(1.dp, if (isSelected) accentColor else CockpitCardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedGraphicStyle = style
                                    onUpdateBikeDetails(nickname, modelName, style)
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = if (isSelected) accentColor else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = style.displayName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) HighContrastWhite else Color.LightGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Button: Subir foto y vectorizar automáticamente
                    Button(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("upload_bike_photo_button")
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Vectorizar Foto de mi Moto",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Section: Personalización de Datos de la Moto
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CockpitCardBorder, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "IDENTIDAD DE TU MOTO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        letterSpacing = 1.2.sp
                    )

                    OutlinedTextField(
                        value = nickname,
                        onValueChange = {
                            nickname = it
                            onUpdateBikeDetails(it, modelName, selectedGraphicStyle)
                        },
                        label = { Text("Apodo de la Moto (ej. LA CHUCHA)") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("bike_nickname_input")
                    )

                    OutlinedTextField(
                        value = modelName,
                        onValueChange = {
                            modelName = it
                            onUpdateBikeDetails(nickname, it, selectedGraphicStyle)
                        },
                        label = { Text("Marca y Modelo (ej. ITALIKA 300Z)") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("bike_model_input")
                    )
                }
            }
        }

        // Section: Opciones de Tema Visual
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CockpitCardBorder, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ColorLens, contentDescription = null, tint = accentColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TEMA VISUAL DE ALTO CONTRASTE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor,
                            letterSpacing = 1.2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    VisualThemeMode.entries.forEach { theme ->
                        val isSelected = profile.themeMode == theme
                        val hexColor = Color(android.graphics.Color.parseColor(theme.hexAccent))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) hexColor.copy(alpha = 0.15f) else Color.Transparent)
                                .border(1.dp, if (isSelected) hexColor else CockpitCardBorder, RoundedCornerShape(12.dp))
                                .clickable { onThemeSelect(theme) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("theme_option_${theme.name}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(hexColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = theme.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = HighContrastWhite
                                )
                            }

                            if (isSelected) {
                                Text("ACTIVO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = hexColor)
                            }
                        }
                    }
                }
            }
        }

        // Section: Contactos de Emergencia SOS
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AlertRed.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Emergency, contentDescription = null, tint = AlertRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CONTACTOS DE EMERGENCIA SOS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = AlertRed,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Text(
                            text = "MANOS LIBRES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlertRed
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    emergencyContacts.forEach { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(contact.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HighContrastWhite)
                                Text(contact.relationship, fontSize = 11.sp, color = Color.Gray)
                            }
                            Text(contact.phone, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = accentColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onOpenEmergencyDialog,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Abrir Panel SOS / Llamada Rápida", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Section: Preferencias de Pantalla (Matching Screenshot 7)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CockpitCardBorder, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Preferencias",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = HighContrastWhite
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mantener pantalla encendida\ndurante el viaje",
                            fontSize = 13.sp,
                            color = HighContrastWhite
                        )
                        Switch(
                            checked = keepScreenOn,
                            onCheckedChange = { keepScreenOn = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Burbuja flotante durante el viaje",
                                fontSize = 13.sp,
                                color = HighContrastWhite
                            )
                            Text(
                                text = "Permiso de superposición concedido.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = floatingBubble,
                            onCheckedChange = { floatingBubble = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onExportPdf,
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Exportar mis datos (PDF)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Guarda una copia de tus reportes de servicio y rutas GPS sin requerir internet.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
