package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SingleTireData
import com.example.model.TireStatus
import com.example.model.TpmsData
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CockpitCardBg
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.HighContrastWhite
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.WarningAmber

@Composable
fun TpmsSection(
    tpmsData: TpmsData,
    onSimulateWarning: () -> Unit,
    onResetNormal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CockpitCardBorder, RoundedCornerShape(20.dp))
            .testTag("tpms_section_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (tpmsData.isConnected) SafeGreen else AlertRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SENSORES TPMS EN TIEMPO REAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp
                    )
                }

                Text(
                    text = if (tpmsData.isConnected) "BLE V2.4 CONECTADO" else "DESCONECTADO",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (tpmsData.isConnected) SafeGreen else AlertRed,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TireItem(
                    label = "DELANTERA",
                    tire = tpmsData.frontTire,
                    targetPsi = 29.0,
                    modifier = Modifier.weight(1f)
                )

                TireItem(
                    label = "TRASERA",
                    tire = tpmsData.rearTire,
                    targetPsi = 33.0,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onSimulateWarning,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("tpms_simulate_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Simular Alerta",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Simular Alerta Baja", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onResetNormal,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SafeGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("tpms_reset_button")
                ) {
                    Text(text = "Calibrar OK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TireItem(
    label: String,
    tire: SingleTireData,
    targetPsi: Double,
    modifier: Modifier = Modifier
) {
    val statusColor = when (tire.status) {
        TireStatus.NORMAL -> SafeGreen
        TireStatus.WARNING_LOW, TireStatus.WARNING_HIGH -> WarningAmber
        TireStatus.CRITICAL_LOW -> AlertRed
    }

    val statusText = when (tire.status) {
        TireStatus.NORMAL -> "ÓPTIMA"
        TireStatus.WARNING_LOW -> "BAJA"
        TireStatus.WARNING_HIGH -> "ALTA"
        TireStatus.CRITICAL_LOW -> "CRÍTICA"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF13141C))
            .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighContrastWhite
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.18f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format("%.1f", tire.pressurePsi),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = HighContrastWhite
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "PSI",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Temp: ${tire.temperatureC.toInt()}°C",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
                Text(
                    text = "Obj: ${targetPsi.toInt()} PSI",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
