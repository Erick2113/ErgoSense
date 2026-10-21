package com.example.ergosense.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ergosense.ui.theme.*
import com.example.ergosense.viewmodel.ErgoViewModel
import com.example.ergosense.viewmodel.HapticType

@Composable
fun CalibrationScreen(viewModel: ErgoViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // --- CABECERA DE DIAGNOSTICO ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ErgoSecondary.copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(ErgoSecondary, CircleShape))
                    Text(
                        "DIAGNOSTICO DE TELEMETRIA: NORMAL",
                        color = ErgoSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = ErgoPrimary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "50 Hz",
                        color = ErgoTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Text(
            "Centro de Calibracion",
            color = ErgoTertiary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        // --- TARJETA ACELEROMETRO ---
        SensorCard(
            nombre = "Acelerometro 3 Ejes  -  Bosch BMI260",
            subtitulo = "Sensor Integrado",
            enabled = viewModel.accelEnabled,
            onToggle = { viewModel.accelEnabled = !viewModel.accelEnabled },
            specs = listOf(
                Pair("Precision", "+/- 0.2 grados"),
                Pair("Frecuencia", "50 Hz"),
                Pair("Monitoreo en segundo plano", if (viewModel.accelEnabled) "Activo" else "Inactivo"),
                Pair("Punto de calibracion", "${viewModel.pitchOffset.toInt()} grados")
            )
        )

        // --- TARJETA LUZ AMBIENTAL ---
        SensorCard(
            nombre = "Luz Ambiental  -  Sensor Fotometrico TCS3701",
            subtitulo = "Sensor Integrado",
            enabled = viewModel.lightEnabled,
            onToggle = { viewModel.lightEnabled = !viewModel.lightEnabled },
            specs = listOf(
                Pair("Rango", "0 - 65 000 Lux"),
                Pair("Latencia", "200 ms"),
                Pair("Medicion actual", "${viewModel.luxValue.toInt()} Lux  (${if (viewModel.luxValue >= viewModel.minLux) "Optimo" else "Bajo"})"),
                Pair("Umbral minimo", "${viewModel.minLux.toInt()} Lux")
            )
        )

        // --- VISOR GIROSCOPICO DE NIVEL ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ErgoSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "CALIBRACION ESPACIAL",
                            color = ErgoSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Visor Giroscopico Dinamico",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Surface(
                        color = ErgoPrimary,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            "Listo para calibrar",
                            color = ErgoSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))


                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(ErgoNeutral, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val roll = viewModel.rawRoll
                    val pitch = viewModel.rawPitch

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f

                        // Circulos concentricos de la diana
                        val radii = listOf(size.minDimension * 0.45f, size.minDimension * 0.30f, size.minDimension * 0.15f)
                        radii.forEachIndexed { index, r ->
                            drawCircle(
                                color = ErgoSecondary.copy(alpha = if (index == 0) 0.3f else 0.5f),
                                radius = r,
                                center = Offset(cx, cy),
                                style = Stroke(if (index == 2) 3f else 1.5f)
                            )
                        }

                        // Retícula de ejes cruzados
                        drawLine(Color.Gray.copy(alpha = 0.4f), Offset(cx, 0f), Offset(cx, size.height), 1.5f)
                        drawLine(Color.Gray.copy(alpha = 0.4f), Offset(0f, cy), Offset(size.width, cy), 1.5f)

                        // Burbuja de nivel
                        val maxOffset = 70.dp.toPx()
                        val bx = cx + (roll.coerceIn(-30f, 30f) / 30f) * maxOffset
                        val by = cy + (pitch.coerceIn(-30f, 30f) / 30f) * maxOffset

                        // Anillo exterior de la burbuja
                        drawCircle(
                            color = ErgoTertiary.copy(alpha = 0.4f),
                            radius = 22.dp.toPx(),
                            center = Offset(bx, by)
                        )
                        // Nucleo de la burbuja
                        drawCircle(
                            color = ErgoTertiary,
                            radius = 14.dp.toPx(),
                            center = Offset(bx, by)
                        )
                        // Reflejo interior
                        drawCircle(
                            color = Color.White.copy(alpha = 0.3f),
                            radius = 5.dp.toPx(),
                            center = Offset(bx - 4.dp.toPx(), by - 4.dp.toPx())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Lecturas de coordenadas en vivo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CoordCell(
                        label = "X (Lateral)",
                        value = "%+.1f grados".format(viewModel.rawRoll)
                    )
                    CoordCell(
                        label = "Y (Frontal)",
                        value = "%.1f grados".format(viewModel.rawPitch)
                    )
                    CoordCell(
                        label = "Z (Vertical)",
                        value = "%.1f grados".format(viewModel.rawZ)
                    )
                    CoordCell(
                        label = "Desvio",
                        value = "%.1f grados".format(viewModel.spatialDeviation)
                    )
                }
            }
        }

        // --- AJUSTE DE UMBRALES ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ErgoSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Ajuste de Umbrales",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Slider de angulo cervical
                Text(
                    "Tolerancia de Angulo Cervical: ${viewModel.angleThreshold.toInt()} grados",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
                Slider(
                    value = viewModel.angleThreshold,
                    onValueChange = { viewModel.angleThreshold = it },
                    valueRange = 15f..40f,
                    colors = SliderDefaults.colors(
                        thumbColor = ErgoTertiary,
                        activeTrackColor = ErgoSecondary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Slider de luminosidad minima
                Text(
                    "Luminosidad Minima Requerida: ${viewModel.minLux.toInt()} Lux",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
                Slider(
                    value = viewModel.minLux,
                    onValueChange = { viewModel.minLux = it },
                    valueRange = 100f..800f,
                    colors = SliderDefaults.colors(
                        thumbColor = ErgoTertiary,
                        activeTrackColor = ErgoSecondary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Selector de tipo de vibracion haptica
                Text("Tipo de Vibracion Haptica", color = Color.LightGray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HapticType.entries.forEach { tipo ->
                        val selected = viewModel.hapticType == tipo
                        OutlinedButton(
                            onClick = { viewModel.hapticType = tipo },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) ErgoSecondary.copy(alpha = 0.2f) else Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selected) ErgoSecondary else Color.Gray.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                tipo.name.lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                color = if (selected) ErgoSecondary else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // --- BOTON FIJAR PUNTO CERO ---
        Button(
            onClick = { viewModel.calibrateZero() },
            colors = ButtonDefaults.buttonColors(containerColor = ErgoPrimary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Fijar Punto Cero", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SensorCard(
    nombre: String,
    subtitulo: String,
    enabled: Boolean,
    onToggle: () -> Unit,
    specs: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ErgoSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(subtitulo, color = Color.Gray, fontSize = 11.sp)
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ErgoSecondary,
                        checkedTrackColor = ErgoSecondary.copy(alpha = 0.4f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            specs.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, color = Color.Gray, fontSize = 12.sp)
                    Text(
                        value,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CoordCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = ErgoTertiary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.Gray, fontSize = 9.sp)
    }
}