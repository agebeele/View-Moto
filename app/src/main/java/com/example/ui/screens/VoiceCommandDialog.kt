package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CockpitCardBg
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.HighContrastWhite

@Composable
fun VoiceCommandDialog(
    lastFeedback: String,
    accentColor: Color,
    onExecuteCommand: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var manualCommandText by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_mic")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val sampleVoiceCommands = listOf(
        "¿Presión de llantas?",
        "Llamar a Emergencias",
        "Próximo servicio",
        "Pausar música",
        "Siguiente canción",
        "¿Cuál es mi velocidad?"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = accentColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ASISTENTE MANOS LIBRES",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = HighContrastWhite,
                    letterSpacing = 1.sp
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Pulsing Mic Indicator
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size((70 * pulseScale).dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f))
                        .border(2.dp, accentColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Escuchando",
                        tint = accentColor,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Escuchando comando de voz...",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighContrastWhite
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = lastFeedback,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "O toca un comando rápido para probar:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.LightGray,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Command Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    sampleVoiceCommands.forEach { cmd ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF161822))
                                .border(1.dp, CockpitCardBorder, RoundedCornerShape(10.dp))
                                .clickable { onExecuteCommand(cmd) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("voice_chip_${cmd.take(10)}")
                        ) {
                            Text(
                                text = "• \"$cmd\"",
                                fontSize = 12.sp,
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Command Input
                OutlinedTextField(
                    value = manualCommandText,
                    onValueChange = { manualCommandText = it },
                    placeholder = { Text("Escribe un comando...", color = Color.Gray, fontSize = 12.sp) },
                    trailingIcon = {
                        IconButton(onClick = {
                            if (manualCommandText.isNotBlank()) {
                                onExecuteCommand(manualCommandText)
                                manualCommandText = ""
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Enviar", tint = accentColor)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = HighContrastWhite,
                        unfocusedTextColor = HighContrastWhite,
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = CockpitCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("manual_voice_input")
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = Color.Gray)
            }
        },
        containerColor = CockpitCardBg,
        modifier = modifier
    )
}
