package com.example.ergosense.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ergosense.ui.theme.*
import com.example.ergosense.viewmodel.ErgoViewModel

@Composable
fun DashboardScreen(viewModel: ErgoViewModel) {
    val context = LocalContext.current
    val angle = viewModel.currentAngle
    val isOptimal = angle <= viewModel.angleThreshold
    val breakSecs = viewModel.activeBreakSeconds

    LaunchedEffect(angle) {
        viewModel.checkAndVibrate(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- CABECERA ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hola, Erick",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ErgoSense  |  Dashboard",
                    color = ErgoSecondary,
                    fontSize = 12.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(ErgoPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = ErgoSecondary, modifier = Modifier.size(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fila de estado en vivo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusPill(label = "Zona de Confort Ergonomico", active = isOptimal)
            LiveBadge()
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- TARJETA DEL MEDIDOR CIRCULAR ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ErgoSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Cervical y Espina  -  Acelerometro 3 Ejes (50 Hz)",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = ErgoNeutral,
                            startAngle = 135f, sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 36f, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = if (isOptimal) ErgoSecondary else ErgoCritical,
                            startAngle = 135f,
                            sweepAngle = (angle.coerceIn(0f, 60f) / 60f) * 270f,
                            useCenter = false,
                            style = Stroke(width = 36f, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            color = if (isOptimal) ErgoSecondary.copy(alpha = 0.2f) else ErgoCritical.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isOptimal) "OPTIMA" else "CRITICA",
                                color = if (isOptimal) ErgoSecondary else ErgoCritical,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${angle.toInt()}",
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text("grados", color = Color.Gray, fontSize = 12.sp)
                        Text(
                            text = "Inclinacion Cervical",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Haptico armado < ${viewModel.angleThreshold.toInt()} grados",
                            color = ErgoTertiary,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sub-metricas angulares
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AngleSubMetric(label = "Eje X Lateral", value = "${viewModel.rawX.let { if (it >= 0) "+%.1f".format(it) else "%.1f".format(it) }} grados")
                    VerticalDivider()
                    AngleSubMetric(label = "Eje Y Frontal", value = "%.1f grados".format(viewModel.rawY))
                    VerticalDivider()
                    AngleSubMetric(label = "Estabilidad", value = "${viewModel.stability.toInt()} %")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- TARJETAS DE TELEMETRIA DUAL ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tarjeta Postura
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = ErgoSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("POSTURA", color = ErgoSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        SensorStatusBadge(label = if (isOptimal) "Seguro" else "Alerta", ok = isOptimal)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("POSTURA HOY", color = Color.Gray, fontSize = 10.sp)
                    Text("5 h 24 min", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("88 % erguida", color = ErgoSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Objetivo: > 80 % erguida", color = Color.Gray, fontSize = 10.sp)
                }
            }

            // Tarjeta Luxometro
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = ErgoSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val luxOk = viewModel.luxValue >= viewModel.minLux
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("LUZ", color = ErgoTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        SensorStatusBadge(label = if (luxOk) "Optimo" else "Bajo", ok = luxOk)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("LUXOMETRO", color = Color.Gray, fontSize = 10.sp)
                    Text("${viewModel.luxValue.toInt()} Lux", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Rango ideal", color = ErgoTertiary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("300 - 500 Lux", color = Color.Gray, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- BANNER DE CORRELACION ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isOptimal && viewModel.luxValue >= viewModel.minLux)
                    ErgoSecondary.copy(alpha = 0.12f)
                else
                    ErgoCritical.copy(alpha = 0.12f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isOptimal && viewModel.luxValue >= viewModel.minLux) ErgoSecondary else ErgoCritical,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = viewModel.getCorrelationDiagnosis(),
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- MICRO-PAUSA ACTIVA (si esta corriendo) ---
        if (breakSecs != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ErgoPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = ErgoTertiary, modifier = Modifier.size(20.dp))
                        Column {
                            Text("Micro-Pausa Activa", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = "%d:%02d restantes".format(breakSecs / 60, breakSecs % 60),
                                color = ErgoTertiary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    TextButton(onClick = { viewModel.cancelActiveBreak() }) {
                        Text("Cancelar", color = ErgoCritical, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // --- ACCIONES RAPIDAS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.toggleSession() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.isSessionPaused) ErgoSecondary else ErgoSurface
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = if (viewModel.isSessionPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (viewModel.isSessionPaused) "Reanudar" else "Pausar",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
            Button(
                onClick = { viewModel.startActiveBreak() },
                modifier = Modifier.weight(1f),
                enabled = breakSecs == null,
                colors = ButtonDefaults.buttonColors(containerColor = ErgoTertiary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Micro-Pausa", color = Color.White, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.calibrateZero() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ErgoPrimary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Calibrar Punto Cero", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AngleSubMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.Gray, fontSize = 10.sp)
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(Color.Gray.copy(alpha = 0.3f))
    )
}

@Composable
private fun StatusPill(label: String, active: Boolean) {
    Surface(
        color = if (active) ErgoSecondary.copy(alpha = 0.15f) else ErgoCritical.copy(alpha = 0.15f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (active) ErgoSecondary else ErgoCritical, CircleShape)
            )
            Text(label, color = if (active) ErgoSecondary else ErgoCritical, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun LiveBadge() {
    Surface(
        color = ErgoPrimary,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = Modifier.size(6.dp).background(ErgoCritical, CircleShape))
            Text("EN VIVO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SensorStatusBadge(label: String, ok: Boolean) {
    Surface(
        color = if (ok) ErgoSecondary.copy(alpha = 0.2f) else ErgoCritical.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            color = if (ok) ErgoSecondary else ErgoCritical,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}