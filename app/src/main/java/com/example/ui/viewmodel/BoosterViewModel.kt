package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BoostLog
import com.example.data.model.BoostMode
import com.example.data.model.BoostSettings
import com.example.data.model.PlayStyle
import com.example.data.model.ShizukuStatus
import com.example.data.model.SystemTelemetry
import com.example.service.ShizukuManager
import com.example.service.SystemMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BoosterViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences = application.getSharedPreferences("blox_booster_prefs", Context.MODE_PRIVATE)
    val shizukuManager = ShizukuManager(application)
    private val systemMonitor = SystemMonitor(application)

    private val _telemetry = MutableStateFlow(systemMonitor.getTelemetry())
    val telemetry: StateFlow<SystemTelemetry> = _telemetry.asStateFlow()

    val shizukuStatus: StateFlow<ShizukuStatus> = shizukuManager.status

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<BoostSettings> = _settings.asStateFlow()

    private val _isBoosting = MutableStateFlow(false)
    val isBoosting: StateFlow<Boolean> = _isBoosting.asStateFlow()

    private val _boostProgress = MutableStateFlow(0f)
    val boostProgress: StateFlow<Float> = _boostProgress.asStateFlow()

    private val _boostStepText = MutableStateFlow("")
    val boostStepText: StateFlow<String> = _boostStepText.asStateFlow()

    // Side menu state: explicitly requested to open on boost!
    private val _isSideMenuOpen = MutableStateFlow(false)
    val isSideMenuOpen: StateFlow<Boolean> = _isSideMenuOpen.asStateFlow()

    private val _boostLogs = MutableStateFlow<List<BoostLog>>(emptyList())
    val boostLogs: StateFlow<List<BoostLog>> = _boostLogs.asStateFlow()

    private val _showShizukuHelp = MutableStateFlow(false)
    val showShizukuHelp: StateFlow<Boolean> = _showShizukuHelp.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    init {
        // Start telemetry polling
        viewModelScope.launch {
            systemMonitor.pollTelemetry(3000L).collect { updated ->
                _telemetry.value = updated
            }
        }

        // Add initial welcome log
        addLog(
            BoostLog(
                timeFormatted = timeFormat.format(Date()),
                category = "SYSTEM",
                message = "Blox Booster initialized. Shizuku bridge ready.",
                isSuccess = true
            )
        )
    }

    private fun addLog(log: BoostLog) {
        val current = _boostLogs.value.toMutableList()
        current.add(0, log)
        if (current.size > 50) {
            _boostLogs.value = current.take(50)
        } else {
            _boostLogs.value = current
        }
    }

    fun boostRoblox() {
        if (_isBoosting.value) return

        viewModelScope.launch {
            _isBoosting.value = true
            _boostProgress.value = 0.05f
            _boostStepText.value = "Scanning Roblox processes & memory..."

            val now = timeFormat.format(Date())
            addLog(
                BoostLog(
                    timeFormatted = now,
                    category = "BOOST",
                    message = "Starting Roblox Performance Boost sequence...",
                    isSuccess = true
                )
            )

            delay(400)
            _boostProgress.value = 0.30f
            _boostStepText.value = "Killing background processes & freeing RAM..."

            // Execute optimization through Shizuku or fallback
            shizukuManager.applyOptimization(
                settings = _settings.value,
                robloxPkg = _telemetry.value.robloxPackageName ?: "com.roblox.client",
                onLog = { log ->
                    addLog(log)
                }
            )

            delay(350)
            _boostProgress.value = 0.65f
            _boostStepText.value = when (_settings.value.activeMode) {
                BoostMode.POTATO -> "Applying Potato Mode: Reducing textures & disabling overhead..."
                BoostMode.SHADERS -> "Applying Graphics & Shaders: Tuning GPU composition & vibrancy..."
                BoostMode.BALANCED -> "Applying Balanced Profile: Setting 60 FPS lock & cooling..."
            }

            delay(350)
            _boostProgress.value = 0.90f
            _boostStepText.value = "Setting FPS cap to ${_settings.value.fpsCap} FPS & prioritizing CPU..."

            delay(300)
            _boostProgress.value = 1.0f
            _boostStepText.value = "Boost applied! Opening side tuning panel..."

            // Refresh telemetry after boost
            _telemetry.value = systemMonitor.getTelemetry()

            delay(250)
            _isBoosting.value = false
            _boostProgress.value = 0f

            // Open the little menu on the side as requested!
            _isSideMenuOpen.value = true
        }
    }

    fun toggleSideMenu(isOpen: Boolean) {
        _isSideMenuOpen.value = isOpen
    }

    fun setMode(mode: BoostMode) {
        val updated = _settings.value.copy(activeMode = mode)
        _settings.value = updated
        saveSettings(updated)
        addLog(
            BoostLog(
                timeFormatted = timeFormat.format(Date()),
                category = "CONFIG",
                message = "Switched to ${mode.title} (${mode.subtitle})",
                isSuccess = true
            )
        )
    }

    fun setPlayStyle(style: PlayStyle) {
        val updated = _settings.value.copy(
            playStyle = style,
            fpsCap = style.recommendedFps,
            activeMode = style.recommendedMode
        )
        _settings.value = updated
        saveSettings(updated)
        addLog(
            BoostLog(
                timeFormatted = timeFormat.format(Date()),
                category = "CONFIG",
                message = "Applied play style: ${style.title} (Recommended ${style.recommendedFps} FPS)",
                isSuccess = true
            )
        )
    }

    fun setFpsCap(fps: Int) {
        val updated = _settings.value.copy(fpsCap = fps)
        _settings.value = updated
        saveSettings(updated)
        val fpsStr = if (fps == 0) "Uncapped" else "$fps FPS"
        addLog(
            BoostLog(
                timeFormatted = timeFormat.format(Date()),
                category = "CONFIG",
                message = "Target FPS cap adjusted to $fpsStr",
                isSuccess = true
            )
        )
    }

    fun updateBoostIntensity(value: Float) {
        val updated = _settings.value.copy(boostIntensity = value)
        _settings.value = updated
        saveSettings(updated)
    }

    fun updatePotatoIntensity(value: Float) {
        val updated = _settings.value.copy(potatoIntensity = value)
        _settings.value = updated
        saveSettings(updated)
    }

    fun updateShadersIntensity(value: Float) {
        val updated = _settings.value.copy(shadersIntensity = value)
        _settings.value = updated
        saveSettings(updated)
    }

    fun setShowShizukuHelp(show: Boolean) {
        _showShizukuHelp.value = show
    }

    fun requestShizukuPermission() {
        shizukuManager.requestPermission()
    }

    fun checkShizukuStatus() {
        shizukuManager.checkStatus()
    }

    fun launchRoblox(context: Context) {
        val pkg = _telemetry.value.robloxPackageName ?: "com.roblox.client"
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                Toast.makeText(context, "Launching Roblox with Boost active...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    context,
                    "Roblox package ($pkg) is not installed on this device.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not launch: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun clearLogs() {
        _boostLogs.value = emptyList()
    }

    private fun saveSettings(s: BoostSettings) {
        prefs.edit().apply {
            putString("activeMode", s.activeMode.name)
            putString("playStyle", s.playStyle.name)
            putInt("fpsCap", s.fpsCap)
            putFloat("boostIntensity", s.boostIntensity)
            putFloat("potatoIntensity", s.potatoIntensity)
            putFloat("shadersIntensity", s.shadersIntensity)
            apply()
        }
    }

    private fun loadSettings(): BoostSettings {
        val modeName = prefs.getString("activeMode", BoostMode.BALANCED.name) ?: BoostMode.BALANCED.name
        val styleName = prefs.getString("playStyle", PlayStyle.COMPETITIVE_PVP.name) ?: PlayStyle.COMPETITIVE_PVP.name
        val activeMode = try { BoostMode.valueOf(modeName) } catch (_: Exception) { BoostMode.BALANCED }
        val playStyle = try { PlayStyle.valueOf(styleName) } catch (_: Exception) { PlayStyle.COMPETITIVE_PVP }

        return BoostSettings(
            activeMode = activeMode,
            playStyle = playStyle,
            fpsCap = prefs.getInt("fpsCap", 120),
            boostIntensity = prefs.getFloat("boostIntensity", 75f),
            potatoIntensity = prefs.getFloat("potatoIntensity", 60f),
            shadersIntensity = prefs.getFloat("shadersIntensity", 40f)
        )
    }

    override fun onCleared() {
        super.onCleared()
        shizukuManager.cleanup()
    }
}
