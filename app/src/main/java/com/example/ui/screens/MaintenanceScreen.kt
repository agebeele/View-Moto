package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ComponentInterval
import com.example.model.MotoProfile
import com.example.model.ServiceCategory
import com.example.model.ServiceRecord
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CockpitCardBg
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.HighContrastWhite
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.WarningAmber

@Composable
fun MaintenanceScreen(
    profile: MotoProfile,
    componentIntervals: List<ComponentInterval>,
    services: List<ServiceRecord>,
    searchQuery: String,
    selectedCategory: ServiceCategory,
    pdfSuccessToast: String?,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (ServiceCategory) -> Unit,
    onUpdateOdometer: (Double) -> Unit,
    onAddService: (String, ServiceCategory, Double, String) -> Unit,
    onExportPdf: () -> Unit,
    onSendUrgentPushAlert: () -> Unit,
    onClearPdfToast: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentColor = MaterialTheme.colorScheme.primary

    var showOdometerDialog by remember { mutableStateOf(false) }
    var showAddServiceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(pdfSuccessToast) {
        if (pdfSuccessToast != null) {
            Toast.makeText(context, pdfSuccessToast, Toast.LENGTH_LONG).show()
            onClearPdfToast()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("maintenance_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Header: Centro de mantenimiento
            Text(
                text = "Centro de mantenimiento",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = HighContrastWhite,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Odómetro Estimado Card matching Screenshot 5
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CockpitCardBorder, RoundedCornerShape(20.dp))
                    .testTag("odometer_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "ODÓMETRO ESTIMADO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${String.format("%.1f", profile.currentOdometerKm)} km",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        color = HighContrastWhite
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Modificar kilometraje >",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier
                            .clickable { showOdometerDialog = true }
                            .testTag("edit_odometer_button")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // PDF Export and Urgent Push Notification Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onExportPdf,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("export_pdf_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "PDF",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Exportar PDF",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = onSendUrgentPushAlert,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .border(1.dp, WarningAmber, RoundedCornerShape(16.dp))
                        .testTag("push_notification_alert_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Push",
                        tint = WarningAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Alerta Push",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber
                    )
                }
            }
        }

        // Section: Estado de Componentes / Próximos Cambios
        item {
            Text(
                text = "ESTADO DE PRÓXIMOS CAMBIOS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.2.sp
            )
        }

        items(componentIntervals) { interval ->
            ComponentIntervalCard(interval = interval, accentColor = accentColor)
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))

            // Big Action Button: Registrar Servicio
            Button(
                onClick = { showAddServiceDialog = true },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("register_service_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Registrar servicio",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Section: Búsqueda avanzada & Historial de servicios
            Text(
                text = "Historial de servicios",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = HighContrastWhite
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Advanced Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Buscar por servicio, taller o notas...", color = Color.Gray, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar", tint = accentColor)
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = CockpitCardBorder,
                    focusedContainerColor = CockpitCardBg,
                    unfocusedContainerColor = CockpitCardBg,
                    focusedTextColor = HighContrastWhite,
                    unfocusedTextColor = HighContrastWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("service_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips Row
            val scrollFilterState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollFilterState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ServiceCategory.entries.forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) accentColor else CockpitCardBg)
                            .border(1.dp, if (isSelected) accentColor else CockpitCardBorder, RoundedCornerShape(12.dp))
                            .clickable { onCategorySelect(category) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("filter_chip_${category.name}")
                    ) {
                        Text(
                            text = category.displayName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else HighContrastWhite
                        )
                    }
                }
            }
        }

        // List of filtered service records
        if (services.isEmpty()) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    Text(
                        text = "No se encontraron registros de servicio.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(services) { record ->
                ServiceRecordCard(record = record, accentColor = accentColor)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialog: Modificar Kilometraje
    if (showOdometerDialog) {
        var tempOdometer by remember { mutableStateOf(profile.currentOdometerKm.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { showOdometerDialog = false },
            title = { Text("Modificar Kilometraje", fontWeight = FontWeight.Bold, color = HighContrastWhite) },
            text = {
                Column {
                    Text("Ingresa la lectura actual del velocímetro físico de tu moto:", color = Color.LightGray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempOdometer,
                        onValueChange = { tempOdometer = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Kilómetros totales") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val km = tempOdometer.toDoubleOrNull() ?: profile.currentOdometerKm
                        onUpdateOdometer(km)
                        showOdometerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Guardar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOdometerDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = CockpitCardBg
        )
    }

    // Dialog: Registrar Servicio
    if (showAddServiceDialog) {
        AddServiceDialog(
            currentOdometer = profile.currentOdometerKm,
            accentColor = accentColor,
            onDismiss = { showAddServiceDialog = false },
            onConfirm = { title, cat, cost, notes ->
                onAddService(title, cat, cost, notes)
                showAddServiceDialog = false
            }
        )
    }
}

@Composable
fun ComponentIntervalCard(
    interval: ComponentInterval,
    accentColor: Color
) {
    val statusColor = if (interval.isUrgent) AlertRed else SafeGreen
    val progress = (1.0 - (interval.remainingKm / interval.intervalKm)).toFloat().coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (interval.isUrgent) AlertRed.copy(alpha = 0.8f) else CockpitCardBorder,
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = interval.name.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    letterSpacing = 1.sp
                )

                if (interval.isUrgent) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AlertRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("¡URGENTE!", fontSize = 11.sp, fontWeight = FontWeight.Black, color = AlertRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Faltan ${String.format("%.1f", interval.remainingKm)} km",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = HighContrastWhite
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar matching Screenshot 5
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (interval.isUrgent) AlertRed else accentColor,
                trackColor = Color(0xFF2A2B3A)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = interval.recommendation,
                fontSize = 12.sp,
                color = if (interval.isUrgent) AlertRed else SafeGreen,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Último cambio: ${interval.lastServiceKm.toInt()} km  ·  Próximo: ${interval.nextServiceKm.toInt()} km",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ServiceRecordCard(
    record: ServiceRecord,
    accentColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CockpitCardBorder, RoundedCornerShape(16.dp))
            .testTag("service_record_${record.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.dateDisplay,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$${String.format("%.2f", record.costMxn)} MXN",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = record.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = HighContrastWhite
            )

            if (record.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = record.notes,
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Odómetro: ${record.odometerKm.toInt()} km  ·  ${record.workshopName}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceDialog(
    currentOdometer: Double,
    accentColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (String, ServiceCategory, Double, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ServiceCategory.ACEITE) }
    var costText by remember { mutableStateOf("650") }
    var notes by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Nuevo Servicio", fontWeight = FontWeight.Black, color = HighContrastWhite) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Descripción del servicio") },
                    placeholder = { Text("ej. Cambio de Aceite y Filtro") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría de Servicio") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        ServiceCategory.entries.filter { it != ServiceCategory.TODOS }.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    selectedCategory = cat
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Costo Total (MXN)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas técnicas o taller") },
                    placeholder = { Text("ej. Marca de repuesto, garantía...") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val cost = costText.toDoubleOrNull() ?: 0.0
                        onConfirm(title, selectedCategory, cost, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Guardar Registro", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        },
        containerColor = CockpitCardBg
    )
}
