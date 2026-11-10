package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HighContrastWhite

@Composable
fun SpeedometerGauge(
    currentSpeedKmh: Int,
    maxSpeedKmh: Int = 160,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = currentSpeedKmh.toFloat(),
        animationSpec = tween(durationMillis = 350),
        label = "speedometer_anim"
    )

    val sweepFraction = (animatedSpeed / maxSpeedKmh).coerceIn(0f, 1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .testTag("speedometer_gauge")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(230.dp, 160.dp)
        ) {
            Canvas(modifier = Modifier.size(220.dp, 220.dp)) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val strokeWidth = 14.dp.toPx()
                val radius = (canvasWidth - strokeWidth) / 2

                val startAngle = 150f
                val totalSweepAngle = 240f

                // Inactive Track
                drawArc(
                    color = Color.White.copy(alpha = 0.12f),
                    startAngle = startAngle,
                    sweepAngle = totalSweepAngle,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Active Gradient Track
                if (sweepFraction > 0.01f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                accentColor.copy(alpha = 0.6f),
                                accentColor,
                                accentColor
                            )
                        ),
                        startAngle = startAngle,
                        sweepAngle = totalSweepAngle * sweepFraction,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            // Digital Readout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "VELOCIDAD GPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.8.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "$currentSpeedKmh",
                    fontSize = 62.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    color = HighContrastWhite,
                    letterSpacing = (-1).sp
                )

                Text(
                    text = "km/h",
                    style = MaterialTheme.typography.titleMedium,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
