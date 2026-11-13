package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.MotoProfile
import com.example.model.VisualThemeMode
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CockpitBackground
import com.example.ui.theme.CockpitCardBg
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.HighContrastWhite
import com.example.ui.theme.TextSecondary

@Composable
fun BikeSelectorDialog(
    currentProfile: MotoProfile,
    garageList: List<MotoProfile>,
    onSelectBike: (String) -> Unit,
    onAddNewBike: (String, String, String, Double, VisualThemeMode) -> Unit,
    onDeleteBike: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val accentColor = MaterialTheme.colorScheme.primary
    var isAddingBike by remember { mutableStateOf(false) }

    BackHandler(enabled = isAddingBike) {
        isAddingBike = false
    }

    // Form inputs for new bike
    var newNickname by remember { mutableStateOf("") }
    var newModel by remember { mutableStateOf("") }
    var newPlate by remember { mutableStateOf("") }
    var newOdoKm by remember { mutableStateOf("") }
    var selectedTheme by remember { mutableStateOf(VisualThemeMode.NEON_MAGENTA) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TwoWheeler,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isAddingBike) "Nueva Motocicleta" else "Mi Garaje de Motos",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = HighContrastWhite
                        )
                        Text(
                            text = if (isAddingBike) "Registrar datos técnicos" else "Escoge la moto activa para rodar",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                }
            }
        },
        text = {
            if (isAddingBike) {
                // Add New Motorcycle Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newNickname,
                        onValueChange = { newNickname = it },
                        label = { Text("Apodo de la Moto") },
                        placeholder = { Text("Ej: LA CHUCHA, LA DIABLA") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bike_input_nickname")
                    )

                    OutlinedTextField(
                        value = newModel,
                        onValueChange = { newModel = it },
                        label = { Text("Marca y Modelo") },
                        placeholder = { Text("Ej: ITALIKA 300Z, YAMAHA MT-03, KTM 390") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bike_input_model")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newPlate,
                            onValueChange = { newPlate = it.uppercase() },
                            label = { Text("Placa") },
                            placeholder = { Text("94MZB1") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = CockpitCardBorder,
                                focusedTextColor = HighContrastWhite,
                                unfocusedTextColor = HighContrastWhite
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bike_input_plate")
                        )

                        OutlinedTextField(
                            value = newOdoKm,
                            onValueChange = { newOdoKm = it },
                            label = { Text("Km Odómetro") },
                            placeholder = { Text("3950") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = CockpitCardBorder,
                                focusedTextColor = HighContrastWhite,
                                unfocusedTextColor = HighContrastWhite
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bike_input_odo")
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tema de Iluminación:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighContrastWhite
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VisualThemeMode.entries.forEach { mode ->
                            val isSel = selectedTheme == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(android.graphics.Color.parseColor(mode.hexAccent)))
                                    .border(
                                        width = if (isSel) 2.5.dp else 0.dp,
                                        color = if (isSel) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedTheme = mode },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isAddingBike = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                val km = newOdoKm.toDoubleOrNull() ?: 0.0
                                if (newModel.isNotBlank()) {
                                    onAddNewBike(newNickname, newModel, newPlate, km, selectedTheme)
                                    isAddingBike = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("confirm_add_bike_button")
                        ) {
                            Text("Guardar Moto", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
            } else {
                // List of Motorcycles in Garage
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(garageList) { bike ->
                            val isSelected = bike.id == currentProfile.id

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) accentColor.copy(alpha = 0.12f) else CockpitCardBg
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) accentColor else CockpitCardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectBike(bike.id) }
                                    .testTag("bike_card_${bike.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(CockpitBackground)
                                                .border(1.dp, CockpitCardBorder, RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsBike,
                                                contentDescription = null,
                                                tint = if (isSelected) accentColor else Color.Gray,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = bike.nickname,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = HighContrastWhite
                                                )
                                                if (isSelected) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(accentColor)
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "ACTIVA",
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = Color.Black
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "${bike.brandModel} · ${bike.plateNumber}",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Speed,
                                                    contentDescription = null,
                                                    tint = accentColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${String.format("%,.1f", bike.currentOdometerKm)} km",
                                                    fontSize = 10.sp,
                                                    color = HighContrastWhite,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Seleccionada",
                                                tint = accentColor,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "Usar",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = accentColor,
                                                modifier = Modifier.clickable { onSelectBike(bike.id) }
                                            )
                                        }

                                        if (garageList.size > 1) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            IconButton(
                                                onClick = { onDeleteBike(bike.id) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Eliminar de garaje",
                                                    tint = AlertRed.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { isAddingBike = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CockpitCardBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_add_bike_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = HighContrastWhite)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ Agregar otra moto al Garaje",
                            color = HighContrastWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            if (!isAddingBike) {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar", color = TextSecondary)
                }
            }
        },
        containerColor = CockpitCardBg
    )
}
