package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.ImageVectorizationProcessor
import com.example.ui.theme.CockpitBackground
import com.example.ui.theme.CockpitCardBg
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.HighContrastWhite
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BikeImageCutoutDialog(
    sourceBitmap: Bitmap,
    imageProcessor: ImageVectorizationProcessor,
    onConfirmCutout: (Bitmap) -> Unit,
    onDismiss: () -> Unit
) {
    val accentColor = MaterialTheme.colorScheme.primary
    val scope = rememberCoroutineScope()

    var tolerance by remember { mutableFloatStateOf(0.38f) }
    var smoothEdges by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var statusMessage by remember { mutableStateOf("Ajusta la tolerancia o usa la API para aislar la moto.") }

    fun processLocal() {
        scope.launch {
            isProcessing = true
            val result = withContext(Dispatchers.Default) {
                imageProcessor.extractTransparentMotorcyclePng(
                    source = sourceBitmap,
                    toleranceFraction = tolerance,
                    smoothEdges = smoothEdges
                )
            }
            processedBitmap = result
            statusMessage = "PNG Transparente aislado con éxito (Tolerancia: ${(tolerance * 100).toInt()}%)"
            isProcessing = false
        }
    }

    fun processWithRestApi() {
        scope.launch {
            isProcessing = true
            statusMessage = "Conectando con RESTful API de recorte de fondo..."
            val result = imageProcessor.removeBackgroundWithRestApi(sourceBitmap)
            processedBitmap = result
            statusMessage = "Fondo eliminado con RESTful API · PNG Transparente Listo"
            isProcessing = false
        }
    }

    LaunchedEffect(Unit) {
        processLocal()
    }

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
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Editor de Recorte PNG",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = HighContrastWhite
                        )
                        Text(
                            text = "Aislar silueta y recuperar solo el PNG",
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Visual Preview: Side-by-side or Large Checkered Cutout Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F1117))
                        .border(1.dp, CockpitCardBorder, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isProcessing) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(color = accentColor, modifier = Modifier.size(32.dp))
                            Text(text = "Procesando recorte...", color = TextSecondary, fontSize = 11.sp)
                        }
                    } else if (processedBitmap != null) {
                        Image(
                            bitmap = processedBitmap!!.asImageBitmap(),
                            contentDescription = "Vista previa PNG Transparente",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    } else {
                        Image(
                            bitmap = sourceBitmap.asImageBitmap(),
                            contentDescription = "Original",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }
                }

                Text(
                    text = statusMessage,
                    fontSize = 11.sp,
                    color = SafeGreen,
                    fontWeight = FontWeight.SemiBold
                )

                // Slider: Cutout Sensitivity / Tolerance
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tolerancia de Fondo:", fontSize = 11.sp, color = HighContrastWhite, fontWeight = FontWeight.Bold)
                        }
                        Text("${(tolerance * 100).toInt()}%", fontSize = 11.sp, color = accentColor, fontWeight = FontWeight.Black)
                    }

                    Slider(
                        value = tolerance,
                        onValueChange = {
                            tolerance = it
                        },
                        onValueChangeFinished = {
                            processLocal()
                        },
                        valueRange = 0.10f..0.85f,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = CockpitCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Smooth Edges Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Suavizado anti-fringe en bordes",
                        fontSize = 12.sp,
                        color = HighContrastWhite
                    )
                    Switch(
                        checked = smoothEdges,
                        onCheckedChange = {
                            smoothEdges = it
                            processLocal()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.4f))
                    )
                }

                // Action buttons: REST API trigger & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { processWithRestApi() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("API RESTful", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val finalBitmap = processedBitmap ?: sourceBitmap
                            onConfirmCutout(finalBitmap)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("save_cutout_png_button")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guardar PNG", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = CockpitCardBg
    )
}
