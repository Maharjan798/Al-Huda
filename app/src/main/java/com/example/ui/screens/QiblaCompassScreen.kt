package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldSecondary
import com.example.ui.viewmodels.QiblaViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaCompassScreen(
    qiblaViewModel: QiblaViewModel,
    onBackClick: () -> Unit
) {
    val qiblaState by qiblaViewModel.qiblaUiState.collectAsState()

    DisposableEffect(Unit) {
        qiblaViewModel.registerSensors()
        onDispose {
            qiblaViewModel.unregisterSensors()
        }
    }

    val animatedDialColor by animateColorAsState(
        targetValue = if (qiblaState.isAligned) GoldSecondary else EmeraldPrimary,
        label = "compassColor"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Qibla Direction",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmeraldPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Heading degree header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (qiblaState.isAligned) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (qiblaState.isAligned) "FACING MAKKAH KAABA" else "ALIGN COMPASS WITH POINTER",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (qiblaState.isAligned) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Qibla Angle: ${String.format("%.1f", qiblaState.targetQiblaAngle)}°",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (qiblaState.isAligned) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (qiblaState.isAligned) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Aligned",
                            tint = GoldSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Compass Visual Graphic
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Compass Ring Dial
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(-qiblaState.compassAzimuthDegrees)
                ) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.width / 2 - 16.dp.toPx()

                    // Background Circle
                    drawCircle(
                        color = animatedDialColor.copy(alpha = 0.15f),
                        radius = radius,
                        center = center
                    )

                    // Dial Border
                    drawCircle(
                        color = animatedDialColor,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 6.dp.toPx())
                    )

                    // Cardinal Directions (N, E, S, W)
                    val cardinalDegreeMap = listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f)
                    for ((label, angle) in cardinalDegreeMap) {
                        val rad = Math.toRadians((angle - 90).toDouble())
                        val x = center.x + (radius - 28.dp.toPx()) * cos(rad).toFloat()
                        val y = center.y + (radius - 28.dp.toPx()) * sin(rad).toFloat()

                        drawCircle(
                            color = if (label == "N") Color.Red else GoldSecondary,
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }

                // Fixed Qibla Pointer Arrow pointing towards Makkah Angle
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(qiblaState.deltaDegrees),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = GoldSecondary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "🕋",
                                    fontSize = 24.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Qibla Direction",
                            tint = GoldSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Sensor Calibration Guidance Footer
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CompassCalibration,
                        contentDescription = "Calibration",
                        tint = GoldSecondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "For optimal accuracy, hold device flat and wave in a Figure-8 motion away from metallic objects.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
