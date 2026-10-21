package com.example.ergosense.viewmodel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ergosense.model.LogEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.sqrt

enum class HapticType { SUAVE, INTERMITENTE, CONTINUA }

class ErgoViewModel : ViewModel(), SensorEventListener {


    var currentAngle by mutableFloatStateOf(0f)
    var rawPitch by mutableFloatStateOf(0f)
    var rawRoll by mutableFloatStateOf(0f)
    var rawX by mutableFloatStateOf(0f)
    var rawY by mutableFloatStateOf(0f)
    var rawZ by mutableFloatStateOf(0f)
    var luxValue by mutableFloatStateOf(0f)


    var stability by mutableFloatStateOf(97f)
    private var prevAngle = 0f


    var spatialDeviation by mutableFloatStateOf(0f)


    var pitchOffset by mutableFloatStateOf(0f)
    var angleThreshold by mutableFloatStateOf(25f)
    var minLux by mutableFloatStateOf(300f)


    var hapticType by mutableStateOf(HapticType.SUAVE)


    var isSessionPaused by mutableStateOf(false)


    var activeBreakSeconds by mutableStateOf<Int?>(null)
    private var breakJob: Job? = null


    var accelEnabled by mutableStateOf(true)
    var lightEnabled by mutableStateOf(true)


    private var lastVibrationTime = 0L


    val history = mutableStateListOf(
        LogEvent(
            title = "Inclinacion Cervical Excesiva",
            severity = "MODERADO",
            detail = "34 grados detectados",
            time = "15:42",
            description = "Postura cefalica anterior sostenida.",
            type = "Postura",
            exposureTime = "4 min continuos",
            threshold = "< 25 grados",
            diagnosis = "Postura cefalica anterior sostenida que genera sobrecarga en vertebras C5-C7.",
            correctiveAction = "Vibracion haptica suave atendida",
            actionStatus = "Resuelto",
            dateGroup = "Hoy"
        ),
        LogEvent(
            title = "Iluminancia Insuficiente",
            severity = "LEVE",
            detail = "165 Lux medidos",
            time = "14:15",
            description = "Luz ambiental por debajo del umbral.",
            type = "Luz",
            exposureTime = "12 min continuos",
            threshold = ">= 300 Lux",
            diagnosis = "Luz ambiental insuficiente. Riesgo de acomodacion pupilar forzada.",
            correctiveAction = "Sugerido: Subir persiana o encender lampara",
            actionStatus = "Notificado",
            dateGroup = "Hoy"
        ),
        LogEvent(
            title = "Inclinacion Cervical Excesiva",
            severity = "CRITICO",
            detail = "42 grados detectados",
            time = "11:08",
            description = "Inclinacion mayor al umbral critico.",
            type = "Postura",
            exposureTime = "8 min continuos",
            threshold = "< 25 grados",
            diagnosis = "Riesgo alto de tension en musculatura trapezoidal y cervical.",
            correctiveAction = "Vibracion haptica continua activada",
            actionStatus = "Notificado",
            dateGroup = "Ayer"
        ),
        LogEvent(
            title = "Calibracion Manual",
            severity = "RESUELTO",
            detail = "Punto cero fijado",
            time = "09:05",
            description = "El usuario fijo el punto cero de calibracion.",
            type = "Calibracion",
            exposureTime = "",
            threshold = "",
            diagnosis = "Ajuste manual del punto de referencia angular.",
            correctiveAction = "Punto cero registrado correctamente",
            actionStatus = "Resuelto",
            dateGroup = "Ayer"
        )
    )

    fun startSensors(context: Context) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // 50 Hz = periodo de 20 000 microsegundos
        sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sm.registerListener(this, it, 20_000)
        }
        sm.getDefaultSensor(Sensor.TYPE_LIGHT)?.let {
            sm.registerListener(this, it, 20_000)
        }
    }

    fun stopSensors(context: Context) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sm.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                if (!accelEnabled) return
                val ax = event.values[0]
                val ay = event.values[1]
                val az = event.values[2]

                rawX = ax
                rawY = ay
                rawZ = az

                rawPitch = Math.toDegrees(
                    atan2(ay.toDouble(), sqrt((ax * ax + az * az).toDouble()))
                ).toFloat()
                rawRoll = Math.toDegrees(
                    atan2(ax.toDouble(), sqrt((ay * ay + az * az).toDouble()))
                ).toFloat()

                val newAngle = kotlin.math.abs(rawPitch - pitchOffset)
                val variation = kotlin.math.abs(newAngle - prevAngle)
                stability = (100f - (variation * 5f).coerceIn(0f, 30f))
                    .coerceIn(70f, 100f)
                prevAngle = newAngle
                currentAngle = newAngle

                spatialDeviation = sqrt(
                    (rawRoll * rawRoll + rawPitch * rawPitch).toDouble()
                ).toFloat().coerceIn(0f, 90f)
            }
            Sensor.TYPE_LIGHT -> {
                if (!lightEnabled) return
                luxValue = event.values[0]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun calibrateZero() {
        pitchOffset = rawPitch
        history.add(
            0,
            LogEvent(
                title = "Calibracion Manual",
                severity = "RESUELTO",
                detail = "Punto cero fijado a ${rawPitch.toInt()} grados",
                time = "Ahora",
                description = "Ajuste manual del punto de referencia angular.",
                type = "Calibracion",
                exposureTime = "",
                threshold = "",
                diagnosis = "El usuario reestablecio el punto de referencia cero.",
                correctiveAction = "Punto cero registrado correctamente",
                actionStatus = "Resuelto",
                dateGroup = "Hoy"
            )
        )
    }

    fun toggleSession() {
        isSessionPaused = !isSessionPaused
    }

    fun startActiveBreak() {
        if (activeBreakSeconds != null) return
        breakJob?.cancel()
        activeBreakSeconds = 180
        breakJob = viewModelScope.launch {
            while ((activeBreakSeconds ?: 0) > 0) {
                delay(1000)
                activeBreakSeconds = (activeBreakSeconds ?: 0) - 1
            }
            activeBreakSeconds = null
        }
    }

    fun cancelActiveBreak() {
        breakJob?.cancel()
        activeBreakSeconds = null
    }

    fun getCorrelationDiagnosis(): String {
        val badPosture = currentAngle > angleThreshold
        val badLight = luxValue < minLux
        return when {
            badPosture && badLight ->
                "Correlacion detectada: Caida de luz ambiental (${luxValue.toInt()} Lux) provoca inclinacion involuntaria hacia la pantalla por fatiga visual."
            badPosture ->
                "Alerta: Inclinacion cervical excesiva (${currentAngle.toInt()} grados). La luz es optima, corrige postura."
            badLight ->
                "Precaucion: Luz insuficiente (${luxValue.toInt()} Lux). Riesgo de acomodacion pupilar forzada."
            else ->
                "Ambiente y postura balanceados. Parametros dentro de rangos seguros."
        }
    }

    fun checkAndVibrate(context: Context) {
        if (isSessionPaused) return
        if (currentAngle > angleThreshold) {
            val currentTime = System.currentTimeMillis()
            val cooldown = when (hapticType) {
                HapticType.SUAVE -> 8000L
                HapticType.INTERMITENTE -> 3000L
                HapticType.CONTINUA -> 1000L
            }
            if (currentTime - lastVibrationTime > cooldown) {
                val vibrator = getVibrator(context)
                if (vibrator.hasVibrator()) {
                    when (hapticType) {
                        HapticType.SUAVE -> vibrateOnce(vibrator, 250)
                        HapticType.INTERMITENTE -> vibratePattern(vibrator)
                        HapticType.CONTINUA -> vibrateOnce(vibrator, 600)
                    }
                }
                lastVibrationTime = currentTime
            }
        }
    }

    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun vibrateOnce(vibrator: Vibrator, ms: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(ms)
        }
    }

    private fun vibratePattern(vibrator: Vibrator) {
        val pattern = longArrayOf(0, 200, 150, 200)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
}