package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FuelEntry
import com.example.model.FuelStats
import com.example.ui.theme.CockpitCardBg
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.HighContrastWhite
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun FuelScreen(
    fuelStats: FuelStats,
    fuelHistory: List<FuelEntry>,
    onAddFuelEntry: (Double, Double) -> Unit,
    onUpdateEstimatedEfficiency: (Double) -> Unit = {},
    onUpdateFuelEntry: (FuelEntry) -> Unit = {},
    onDeleteFuelEntry: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val accentColor = MaterialTheme.colorScheme.primary

    var litersInput by remember { mutableStateOf("") }
    var costInput by remember { mutableStateOf("") }

    var showEditEfficiencyDialog by remember { mutableStateOf(false) }
    var customEfficiencyInput by remember { mutableStateOf(String.format(Locale.US, "%.1f", fuelStats.estimatedEfficiencyKmPerL)) }

    // Edit Fuel Entry Dialog State
    var editingFuelEntry by remember { mutableStateOf<FuelEntry?>(null) }
    var editLitersInput by remember { mutableStateOf("") }
    var editCostInput by remember { mutableStateOf("") }
    var editOdometerInput by remember { mutableStateOf("") }

    // Summary calculations
    val totalSpent = fuelHistory.sumOf { it.costMxn }
    val totalLiters = fuelHistory.sumOf { it.liters }
    val avgCostPerLiter = if (totalLiters > 0) totalSpent / totalLiters else fuelStats.averageFuelPricePerLiter

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("fuel_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // RENDIMIENTO ESTIMADO Card with editable TextView / Quick Edit
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CockpitCardBorder, RoundedCornerShape(20.dp))
                    .testTag("fuel_efficiency_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RENDIMIENTO ESTIMADO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.2.sp
                        )

                        // Quick Edit Icon
                        IconButton(
                            onClick = {
                                customEfficiencyInput = String.format(Locale.US, "%.1f", fuelStats.estimatedEfficiencyKmPerL)
                                showEditEfficiencyDialog = true
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar rendimiento",
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f", fuelStats.estimatedEfficiencyKmPerL),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = HighContrastWhite
                        )
                        Text(
                            text = "km/l",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Toca para editar rendimiento >",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier
                            .clickable {
                                customEfficiencyInput = String.format(Locale.US, "%.1f", fuelStats.estimatedEfficiencyKmPerL)
                                showEditEfficiencyDialog = true
                            }
                            .testTag("edit_efficiency_button")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Estimación para calcular autonomía y consumo por kilómetro recorrido.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Stats Bar: Inversión Total, Litros, Promedio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = CockpitCardBg,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CockpitCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Total Cargado", fontSize = 10.sp, color = TextSecondary)
                        Text(
                            text = "$${String.format(Locale.US, "%.0f", totalSpent)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = HighContrastWhite
                        )
                    }
                }
                Surface(
                    color = CockpitCardBg,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CockpitCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Litros Totales", fontSize = 10.sp, color = TextSecondary)
                        Text(
                            text = "${String.format(Locale.US, "%.1f", totalLiters)} L",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = SafeGreen
                        )
                    }
                }
                Surface(
                    color = CockpitCardBg,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CockpitCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Precio / Litro", fontSize = 10.sp, color = TextSecondary)
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", avgCostPerLiter)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Form: Registrar Carga de Gasolina (CRUD: Create)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CockpitCardBorder, RoundedCornerShape(20.dp))
                    .testTag("register_fuel_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Registrar carga de gasolina",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = HighContrastWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = litersInput,
                        onValueChange = { litersInput = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Litros cargados") },
                        placeholder = { Text("Ej. 9.50", color = Color.Gray) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedContainerColor = Color(0xFF13141C),
                            unfocusedContainerColor = Color(0xFF13141C),
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fuel_liters_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = costInput,
                        onValueChange = { costInput = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Costo total (MXN)") },
                        placeholder = { Text("Ej. 240.00", color = Color.Gray) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedContainerColor = Color(0xFF13141C),
                            unfocusedContainerColor = Color(0xFF13141C),
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fuel_cost_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val liters = litersInput.toDoubleOrNull() ?: 0.0
                            val cost = costInput.toDoubleOrNull() ?: 0.0
                            if (liters > 0.0 && cost > 0.0) {
                                onAddFuelEntry(liters, cost)
                                litersInput = ""
                                costInput = ""
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_fuel_button")
                    ) {
                        Text(
                            text = "Guardar carga",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Historial de Cargas Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial de cargas (${fuelHistory.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = HighContrastWhite
                )
            }
        }

        // List of fuel entries with full Edit & Delete CRUD
        items(fuelHistory) { entry ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CockpitCardBorder, RoundedCornerShape(16.dp))
                    .testTag("fuel_entry_${entry.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.dateDisplay,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${String.format(Locale.US, "%.2f", entry.liters)} L  ·  $${String.format(Locale.US, "%.2f", entry.costMxn)} MXN",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = HighContrastWhite
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Odómetro: ${String.format(Locale.US, "%.1f", entry.odometerKm)} km",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // CRUD Actions: Edit and Delete
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                editingFuelEntry = entry
                                editLitersInput = entry.liters.toString()
                                editCostInput = entry.costMxn.toString()
                                editOdometerInput = entry.odometerKm.toString()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar carga",
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { onDeleteFuelEntry(entry.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar carga",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Mi 300Z · Rendimiento y Combustible · MotoDash",
                fontSize = 11.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    }

    // Modal: Configurar / Editar Rendimiento Estimado
    if (showEditEfficiencyDialog) {
        AlertDialog(
            onDismissRequest = { showEditEfficiencyDialog = false },
            title = {
                Text(
                    text = "Configurar Rendimiento Estimado",
                    fontWeight = FontWeight.Bold,
                    color = HighContrastWhite,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Introduce el rendimiento promedio en km por cada litro de gasolina:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = customEfficiencyInput,
                        onValueChange = { customEfficiencyInput = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Kilómetros por Litro (km/l)") },
                        placeholder = { Text("Ej. 26.5") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = customEfficiencyInput.toDoubleOrNull()
                        if (parsed != null && parsed > 0.0) {
                            onUpdateEstimatedEfficiency(parsed)
                        }
                        showEditEfficiencyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Guardar Rendimiento", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditEfficiencyDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = CockpitCardBg
        )
    }

    // Modal: Editar Registro de Combustible (CRUD: Update)
    if (editingFuelEntry != null) {
        val entry = editingFuelEntry!!
        AlertDialog(
            onDismissRequest = { editingFuelEntry = null },
            title = {
                Text(
                    text = "Editar Carga de Gasolina",
                    fontWeight = FontWeight.Bold,
                    color = HighContrastWhite,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editLitersInput,
                        onValueChange = { editLitersInput = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Litros") },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editCostInput,
                        onValueChange = { editCostInput = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Costo (MXN)") },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editOdometerInput,
                        onValueChange = { editOdometerInput = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Odómetro (km)") },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val liters = editLitersInput.toDoubleOrNull() ?: entry.liters
                        val cost = editCostInput.toDoubleOrNull() ?: entry.costMxn
                        val odo = editOdometerInput.toDoubleOrNull() ?: entry.odometerKm
                        onUpdateFuelEntry(
                            entry.copy(
                                liters = liters,
                                costMxn = cost,
                                odometerKm = odo
                            )
                        )
                        editingFuelEntry = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Actualizar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingFuelEntry = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = CockpitCardBg
        )
    }
}
