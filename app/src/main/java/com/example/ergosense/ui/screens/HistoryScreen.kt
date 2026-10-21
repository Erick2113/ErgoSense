package com.example.ergosense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ergosense.model.LogEvent
import com.example.ergosense.ui.theme.*
import com.example.ergosense.viewmodel.ErgoViewModel

@Composable
fun HistoryScreen(viewModel: ErgoViewModel) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    var searchQuery by remember { mutableStateOf("") }

    val filters = listOf("Todos", "Postura", "Luz", "Acciones", "Calibracion")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // --- CABECERA ---
        Surface(
            color = ErgoSecondary.copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(ErgoSecondary, CircleShape))
                    Text(
                        "BITACORA ERGONOMICA CERTIFICADA",
                        color = ErgoSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(5.dp).background(ErgoTertiary, CircleShape))
                    Text("Sincronizado", color = ErgoTertiary, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Registro de Telemetria",
                    color = ErgoTertiary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${viewModel.history.size} Eventos",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Traza auditable de acelerometria cervical, luxometria ambiental y respuesta postural activa.",
            color = Color.Gray,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- BARRA DE BUSQUEDA ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "Buscar por sensor, grado, lux o accion...",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ErgoSecondary,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                cursorColor = ErgoSecondary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // --- CHIPS DE FILTRO ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filters.forEach { filter ->
                val count = when (filter) {
                    "Todos" -> viewModel.history.size
                    "Acciones" -> viewModel.history.count { it.type == "Accion" }
                    else -> viewModel.history.count { it.type == filter }
                }
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = {
                        Text(
                            if (filter == "Todos") "Todos $count" else "$filter ${if (count > 0) count else ""}".trim(),
                            color = if (selectedFilter == filter) ErgoNeutral else Color.White,
                            fontSize = 11.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ErgoSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- LISTA AGRUPADA POR DIA ---
        val filtered = viewModel.history
            .filter { event ->
                val matchFilter = when (selectedFilter) {
                    "Todos" -> true
                    "Acciones" -> event.type == "Accion"
                    else -> event.type == selectedFilter
                }
                val matchSearch = searchQuery.isBlank() ||
                    event.title.contains(searchQuery, ignoreCase = true) ||
                    event.detail.contains(searchQuery, ignoreCase = true) ||
                    event.diagnosis.contains(searchQuery, ignoreCase = true) ||
                    event.correctiveAction.contains(searchQuery, ignoreCase = true)
                matchFilter && matchSearch
            }

        val grouped = filtered.groupBy { it.dateGroup }
        val dayOrder = listOf("Hoy", "Ayer")

        LazyColumn(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            dayOrder.forEach { day ->
                val events = grouped[day] ?: emptyList()
                if (events.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(0.05f),
                                color = Color.Gray.copy(alpha = 0.3f)
                            )
                            Text(
                                "$day  (${events.size} registros)",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = Color.Gray.copy(alpha = 0.3f)
                            )
                        }
                    }
                    items(events) { event ->
                        Spacer(modifier = Modifier.height(8.dp))
                        LogEventCard(event = event)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogEventCard(event: LogEvent) {
    val severityColor = when (event.severity) {
        "CRITICO" -> ErgoCritical
        "MODERADO" -> ErgoTertiary
        "LEVE" -> ErgoSecondary.copy(alpha = 0.8f)
        "RESUELTO" -> ErgoSecondary
        else -> Color.Gray
    }
    val typeColor = when (event.type) {
        "Postura" -> ErgoTertiary
        "Luz" -> ErgoSecondary
        "Calibracion" -> ErgoSecondary
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ErgoSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(event.time, color = Color.Gray, fontSize = 11.sp)
                    Surface(
                        color = typeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            event.type,
                            color = typeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Surface(
                    color = severityColor.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        event.severity,
                        color = severityColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Titulo del evento
            Text(event.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))

            // Fila de metricas tabuladas (solo si hay datos)
            if (event.detail.isNotBlank() || event.exposureTime.isNotBlank() || event.threshold.isNotBlank()) {
                Surface(
                    color = ErgoNeutral,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        if (event.detail.isNotBlank()) {
                            MetricCell(label = "Medicion", value = event.detail)
                        }
                        if (event.exposureTime.isNotBlank()) {
                            MetricCell(label = "Exposicion", value = event.exposureTime)
                        }
                        if (event.threshold.isNotBlank()) {
                            MetricCell(label = "Umbral", value = event.threshold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Diagnostico clinico
            if (event.diagnosis.isNotBlank()) {
                Text(event.diagnosis, color = Color.Gray, fontSize = 12.sp, lineHeight = 17.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // Pie: accion correctiva + estado
            if (event.correctiveAction.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        event.correctiveAction,
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (event.actionStatus.isNotBlank()) {
                        val statusColor = when (event.actionStatus) {
                            "Resuelto" -> ErgoSecondary
                            "Notificado" -> ErgoTertiary
                            else -> Color.Gray
                        }
                        Surface(
                            color = statusColor.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                event.actionStatus,
                                color = statusColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 9.sp)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}