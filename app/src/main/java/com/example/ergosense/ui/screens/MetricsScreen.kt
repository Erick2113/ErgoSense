package com.example.ergosense.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ergosense.ui.theme.*
import com.example.ergosense.viewmodel.ErgoViewModel

@Composable
fun MetricsScreen(viewModel: ErgoViewModel) {
    var selectedPeriod by remember { mutableStateOf("Hoy") }
    val periods = listOf("Hoy", "Semana", "Mes")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // --- CABECERA ANALITICA ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    "Analisis Biometrico",
                    color = ErgoTertiary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Pill de telemetria sincronizada
                Surface(
                    color = ErgoSecondary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(ErgoSecondary, CircleShape))
                        Text(
                            "TELEMETRIA SINCRONIZADA  Freq: 50 Hz Dual",
                            color = ErgoSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            // Badge de calidad global
            Surface(
                color = ErgoPrimary,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Calidad Global", color = Color.Gray, fontSize = 9.sp)
                    Text("88 / 100", color = ErgoSecondary, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Chips de periodo
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            periods.forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { selectedPeriod = period },
                    label = {
                        Text(
                            period,
                            color = if (selectedPeriod == period) ErgoNeutral else Color.White,
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ErgoSecondary
                    )
                )
            }
        }

        // --- TARJETA DE EVOLUCION TEMPORAL (POSTURA) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ErgoSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Evolucion Temporal  -  Postura Cervical",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Fila de metricas de resumen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricSummaryItem("Promedio", "16.4 grados", ErgoSecondary)
                    MetricSummaryItem("Pico Critico", "32 grados (14:30)", ErgoCritical)
                    MetricSummaryItem("Rango Seguro", "84 %", ErgoTertiary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                val threshold = viewModel.angleThreshold
                val currentAngle = viewModel.currentAngle


                val horaLabels = listOf("09:00", "11:00", "13:00", "14:30", "16:00", "18:00")
                val points = listOf(14f, 16f, 15f, currentAngle.coerceIn(10f, 45f), 18f, 15f)
                val peakIndex = points.indexOf(points.max())

                Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val maxVal = 45f
                        val paddingBottom = 24.dp.toPx()
                        val chartH = h - paddingBottom


                        val thresholdY = chartH * (1f - (threshold / maxVal).coerceIn(0f, 1f))
                        drawLine(
                            color = ErgoCritical,
                            start = Offset(0f, thresholdY),
                            end = Offset(w, thresholdY),
                            strokeWidth = 2.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f)
                        )


                        val path = Path()
                        val coords = points.mapIndexed { i, v ->
                            val x = (w / (points.size - 1)) * i
                            val y = chartH * (1f - (v / maxVal).coerceIn(0f, 1f))
                            Offset(x, y)
                        }
                        path.moveTo(coords[0].x, coords[0].y)
                        for (i in 1 until coords.size) {
                            val prev = coords[i - 1]
                            val curr = coords[i]
                            val cp1 = Offset((prev.x + curr.x) / 2f, prev.y)
                            val cp2 = Offset((prev.x + curr.x) / 2f, curr.y)
                            path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, curr.x, curr.y)
                        }

                        // Relleno degradado bajo la curva
                        val fillPath = Path().apply { addPath(path) }
                        fillPath.lineTo(coords.last().x, chartH)
                        fillPath.lineTo(coords.first().x, chartH)
                        fillPath.close()
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(ErgoSecondary.copy(alpha = 0.35f), Color.Transparent),
                                startY = 0f,
                                endY = chartH
                            )
                        )

                        // Trazar la curva principal
                        drawPath(
                            path = path,
                            color = ErgoSecondary,
                            style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Marcador del pico maximo
                        val peakCoord = coords[peakIndex]
                        drawCircle(color = ErgoCritical, radius = 8f, center = peakCoord)
                        drawCircle(
                            color = ErgoCritical.copy(alpha = 0.3f),
                            radius = 16f,
                            center = peakCoord
                        )
                    }
                }

                // Etiquetas del eje X
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    horaLabels.forEach { hora ->
                        Text(hora, color = Color.Gray, fontSize = 9.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Leyenda del umbral y pico
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "-- Umbral critico: ${threshold.toInt()} grados",
                        color = ErgoCritical,
                        fontSize = 11.sp
                    )
                    Text(
                        "14:30  |  32 grados Max",
                        color = ErgoCritical,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tension acumulada en trapecios: Baja", color = Color.Gray, fontSize = 11.sp)
                    Text("Calibracion: activa", color = ErgoSecondary, fontSize = 11.sp)
                }
            }
        }


        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ErgoSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Distribucion de Luminosidad",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                val lux = viewModel.luxValue
                Row(
                    modifier = Modifier.fillMaxWidth().height(130.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val insuf = lux < 300f
                    val optima = lux in 300f..500f
                    val respl = lux > 500f

                    BarraFranja(
                        label = "Insuficiente",
                        sublabel = "< 300 Lux",
                        heightRatio = if (insuf) 0.9f else 0.25f,
                        color = if (insuf) ErgoTertiary else Color.DarkGray,
                        active = insuf
                    )
                    BarraFranja(
                        label = "Optima",
                        sublabel = "300 - 500 Lux",
                        heightRatio = if (optima) 0.9f else 0.25f,
                        color = if (optima) ErgoSecondary else Color.DarkGray,
                        active = optima
                    )
                    BarraFranja(
                        label = "Resplandor",
                        sublabel = "> 500 Lux",
                        heightRatio = if (respl) 0.9f else 0.25f,
                        color = if (respl) ErgoCritical else Color.DarkGray,
                        active = respl
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Lectura actual: ${lux.toInt()} Lux",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }


        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ErgoSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Diagnostico Contextual Multimodal",
                    color = ErgoTertiary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))

                val diagnosis = viewModel.getCorrelationDiagnosis()
                val isAlert = viewModel.currentAngle > viewModel.angleThreshold || viewModel.luxValue < viewModel.minLux
                Surface(
                    color = if (isAlert) ErgoCritical.copy(alpha = 0.10f) else ErgoSecondary.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = diagnosis,
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun MetricSummaryItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 10.sp)
        Text(value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BarraFranja(
    label: String,
    sublabel: String,
    heightRatio: Float,
    color: Color,
    active: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxHeight()
    ) {
        if (active) {
            Surface(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    "Activo",
                    color = color,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        Box(
            modifier = Modifier
                .width(48.dp)
                .fillMaxHeight(heightRatio)
                .background(color, shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(sublabel, color = Color.Gray, fontSize = 9.sp)
    }
}