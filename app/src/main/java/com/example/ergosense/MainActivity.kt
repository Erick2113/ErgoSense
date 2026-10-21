package com.example.ergosense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ergosense.ui.screens.*
import com.example.ergosense.ui.theme.*
import com.example.ergosense.viewmodel.ErgoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = ErgoPrimary, secondary = ErgoSecondary,
                    tertiary = ErgoTertiary, background = ErgoNeutral, surface = ErgoSurface
                )
            ) {
                ErgoMainApp()
            }
        }
    }
}

@Composable
fun ErgoMainApp(viewModel: ErgoViewModel = viewModel()) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        viewModel.startSensors(context)
        onDispose { viewModel.stopSensors(context) }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = ErgoSurface) {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Home, "") }, label = { Text("Dashboard") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.Analytics, "") }, label = { Text("Métricas") })
                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.History, "") }, label = { Text("Registro") })
                NavigationBarItem(selected = selectedTab == 3, onClick = { selectedTab = 3 }, icon = { Icon(Icons.Default.Sensors, "") }, label = { Text("Sensores") })
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(ErgoNeutral)) {
            when (selectedTab) {
                0 -> DashboardScreen(viewModel)
                1 -> MetricsScreen(viewModel)
                2 -> HistoryScreen(viewModel)
                3 -> CalibrationScreen(viewModel)
            }
        }
    }
}