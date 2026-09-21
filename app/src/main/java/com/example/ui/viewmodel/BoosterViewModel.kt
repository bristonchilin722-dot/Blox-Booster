package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BoostLog
import com.example.data.model.BoostMode
import com.example.data.model.BoostSettings
import com.example.data.model.DeviceSpecs
import com.example.data.model.FpsTelemetry
import com.example.data.model.PlayStyle
import com.example.data.model.ShizukuStatus
import com.example.data.model.SystemTelemetry
import com.example.service.FloatingOverlayService
import com.example.service.FpsMonitor
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
    val shizukuManager = ShizukuManager.getInstance(application)
    private val systemMonitor = SystemMonitor(application)

    private val _telemetry = MutableStateFlow(systemMonitor.getTelemetry())
    val telemetry: StateFlow<SystemTelemetry> = _telemetry.asStateFlow()

    private val _deviceSpecs = MutableStateFlow(systemMonitor.getDeviceSpecs())
    val deviceSpecs: StateFlow<DeviceSpecs> = _deviceSpecs.asStateFlow()

    val fpsTelemetry: StateFlow<FpsTelemetry> = FpsMonitor.fpsTelemetry
    val liveFps: StateFlow<Int> = FpsMonitor.fps
    val frameTimeMs: StateFlow<Float> = FpsMonitor.frameTimeMs

    private val _isOverlayActive = MutableStateFlow(false)
    val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

    val shizukuStatus: StateFlow<ShizukuStatus> = shizukuManager.status

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<BoostSettings> = _settings.asStateFlow()

    private val _isBoosting = MutableStateFlow(false)
    val isBoosting: StateFlow<Boolean> = _isBoosting.asStateFlow()

    private val _boostProgress = MutableStateFlow(0f)
    val boostProgress: StateFlow<Float> = _boostProgress.asStateFlow()

    private val _boostStepText = MutableStateFlow("")
    val boostStepText: StateFlow<String> = _boostStepText.asStateFlow()

    private val _isSideMenuOpen = MutableStateFlow(false)
    val isSideMenuOpen: StateFlow<Boolean> = _isSideMenuOpen.asStateFlow()

    private val _boostLogs = MutableStateFlow<List<BoostLog>>(emptyList())
    val boostLogs: StateFlow<List<BoostLog>> = _boostLogs.asStateFlow()

    private val _showShizukuHelp = MutableStateFlow(false)
    val showShizukuHelp: StateFlow<Boolean> = _showShizukuHelp.asStateFlow()

    private val _showDeviceInfo = MutableStateFlow(false)
    val showDeviceInfo: StateFlow<Boolean> = _showDeviceInfo.asStateFlow()

    private val _showRestoreDialog = MutableStateFlow(false)
    val showRestoreDialog: StateFlow<Boolean> = _showRestoreDialog.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    init {
        FpsMonitor.targetRobloxPackage = _telemetry.value.robloxPackageName ?: "com.roblox.client"
        FpsMonitor.start()

        // Start telemetry polling
        viewModelScope.launch {
            systemMonitor.pollTelemetry(3000L).collect { updated ->
                _telemetry.value = updated
            }
        }

        // Add initial startup log & ensure resolution is native
        viewModelScope.launch {
            shizukuManager.resetResolution()
        }

        val pocoNotice = if (_deviceSpecs.value.isPocoC71OrXiaomi) " (Xiaomi/Poco device optimized)" else ""
        addLog(
            BoostLog(
                timeFormatted = timeFormat.format(Date()),
                category = "SYSTEM",
                message = "Blox Booster initialized$pocoNotice. Target: ${_telemetry.value.robloxPackageName ?: "com.roblox.client"}",
                isSuccess = true
            )
        )
    }

    fun addLog(log: BoostLog) {
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
            _boostStepText.value = "Scanning Roblox processes & system memory..."

            val now = timeFormat.format(Date())
            addLog(
                BoostLog(
                    timeFormatted = now,
                    category = "BOOST",
                    message = "Starting Roblox optimization sequence...",
                    isSuccess = true
                )
            )

            delay(350)
            _boostProgress.value = 0.30f
            _boostStepText.value = "Trimming RAM caches & reducing background task overhead..."

            // Execute optimization through Shizuku or fallback
            shizukuManager.applyOptimization(
                settings = _settings.value,
                robloxPkg = _telemetry.value.robloxPackageName ?: "com.roblox.client",
                onLog = { log -> addLog(log) }
            )

            delay(350)
            _boostProgress.value = 0.65f
            _boostStepText.value = when (_settings.value.activeMode) {
                BoostMode.POTATO -> "Applying Potato Mode: Downscaling game overlay & disabling animations..."
                BoostMode.SHADERS -> "Applying Visual Clarity: 4x MSAA & SurfaceFlinger direct GPU compositing..."
                BoostMode.BALANCED -> "Applying Balanced Profile: Native 1:1 render scale & 60 FPS pacing..."
            }

            delay(350)
            _boostProgress.value = 0.90f
            val fpsTargetStr = if (_settings.value.fpsCap == 0) "Uncapped" else "${_settings.value.fpsCap} FPS"
            _boostStepText.value = "Locking target refresh rate to $fpsTargetStr & renicing CPU priority..."

            delay(300)
            _boostProgress.value = 1.0f
            _boostStepText.value = "Boost applied! Opening side tuning panel..."

            _telemetry.value = systemMonitor.getTelemetry()

            delay(250)
            _isBoosting.value = false
            _boostProgress.value = 0f

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
                category = "MODE",
                message = "Switching profile to ${mode.title}: Applying live system parameters...",
                isSuccess = true
            )
        )

        viewModelScope.launch {
            val pkg = _telemetry.value.robloxPackageName ?: "com.roblox.client"
            when (mode) {
                BoostMode.POTATO -> {
                    shizukuManager.applyPotatoMode(updated.potatoIntensity, pkg) { addLog(it) }
                }
                BoostMode.BALANCED -> {
                    shizukuManager.applyBalancedMode(pkg) { addLog(it) }
                }
                BoostMode.SHADERS -> {
                    shizukuManager.applyShadersMode(updated.shadersIntensity, pkg) { addLog(it) }
                }
            }
        }
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
        setMode(style.recommendedMode)
        setFpsCap(style.recommendedFps)
    }

    fun setFpsCap(fps: Int) {
        val updated = _settings.value.copy(fpsCap = fps)
        _settings.value = updated
        saveSettings(updated)
        viewModelScope.launch {
            shizukuManager.applyFpsCap(fps) { addLog(it) }
        }
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
        if (_settings.value.activeMode == BoostMode.POTATO) {
            viewModelScope.launch {
                val pkg = _telemetry.value.robloxPackageName ?: "com.roblox.client"
                shizukuManager.applyPotatoMode(value, pkg) { addLog(it) }
            }
        }
    }

    fun updateShadersIntensity(value: Float) {
        val updated = _settings.value.copy(shadersIntensity = value)
        _settings.value = updated
        saveSettings(updated)
        if (_settings.value.activeMode == BoostMode.SHADERS) {
            viewModelScope.launch {
                val pkg = _telemetry.value.robloxPackageName ?: "com.roblox.client"
                shizukuManager.applyShadersMode(value, pkg) { addLog(it) }
            }
        }
    }

    fun resetPotatoSettings() {
        val updated = _settings.value.copy(potatoIntensity = 60f)
        _settings.value = updated
        saveSettings(updated)
        viewModelScope.launch {
            val pkg = _telemetry.value.robloxPackageName ?: "com.roblox.client"
            shizukuManager.applyPotatoMode(60f, pkg) { addLog(it) }
            addLog(
                BoostLog(
                    timeFormatted = timeFormat.format(Date()),
                    category = "RESET",
                    message = "Potato Mode settings reset to default (60% scale, animations 0)",
                    isSuccess = true
                )
            )
        }
    }

    fun resetBalancedSettings() {
        val updated = _settings.value.copy(boostIntensity = 50f, fpsCap = 60)
        _settings.value = updated
        saveSettings(updated)
        viewModelScope.launch {
            val pkg = _telemetry.value.robloxPackageName ?: "com.roblox.client"
            shizukuManager.applyBalancedMode(pkg) { addLog(it) }
            shizukuManager.applyFpsCap(60) { addLog(it) }
            addLog(
                BoostLog(
                    timeFormatted = timeFormat.format(Date()),
                    category = "RESET",
                    message = "Balanced Mode reset to default (Native 1:1, 60 FPS, 0.5x animations)",
                    isSuccess = true
                )
            )
        }
    }

    fun resetShadersSettings() {
        val updated = _settings.value.copy(shadersIntensity = 40f)
        _settings.value = updated
        saveSettings(updated)
        viewModelScope.launch {
            val pkg = _telemetry.value.robloxPackageName ?: "com.roblox.client"
            shizukuManager.applyShadersMode(40f, pkg) { addLog(it) }
            addLog(
                BoostLog(
                    timeFormatted = timeFormat.format(Date()),
                    category = "RESET",
                    message = "Graphics & Clarity settings reset to standard 4x MSAA baseline",
                    isSuccess = true
                )
            )
        }
    }

    fun restoreAllDefaults(context: Context) {
        viewModelScope.launch {
            val restored = shizukuManager.restoreAllDefaults { addLog(it) }
            val resetSettings = BoostSettings(
                activeMode = BoostMode.BALANCED,
                playStyle = PlayStyle.OPEN_WORLD_RP,
                fpsCap = 60,
                boostIntensity = 50f,
                potatoIntensity = 60f,
                shadersIntensity = 40f
            )
            _settings.value = resetSettings
            saveSettings(resetSettings)
            Toast.makeText(context, "Restored ${restored.size} settings back to Android defaults", Toast.LENGTH_LONG).show()
        }
    }

    fun setShowShizukuHelp(show: Boolean) {
        _showShizukuHelp.value = show
    }

    fun setShowDeviceInfo(show: Boolean) {
        if (show) {
            _deviceSpecs.value = systemMonitor.getDeviceSpecs()
        }
        _showDeviceInfo.value = show
    }

    fun setShowRestoreDialog(show: Boolean) {
        _showRestoreDialog.value = show
    }

    fun requestShizukuPermission() {
        shizukuManager.requestPermission()
    }

    fun checkShizukuStatus() {
        shizukuManager.checkStatus()
    }

    fun toggleOverlay(context: Context, enable: Boolean) {
        if (enable) {
            if (!Settings.canDrawOverlays(context)) {
                Toast.makeText(context, "Please grant 'Display over other apps' to show FPS over Roblox", Toast.LENGTH_LONG).show()
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:${context.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            }
            FloatingOverlayService.start(context)
            _isOverlayActive.value = true
            Toast.makeText(context, "Floating FPS HUD & Tuner Menu active over Roblox!", Toast.LENGTH_SHORT).show()
        } else {
            FloatingOverlayService.stop(context)
            _isOverlayActive.value = false
            Toast.makeText(context, "Floating HUD closed", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchRobloxWithOverlay(context: Context) {
        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Toast.makeText(context, "Allow overlay permission to see your FPS & Menu over Roblox", Toast.LENGTH_LONG).show()
            return
        }

        FloatingOverlayService.start(context)
        _isOverlayActive.value = true
        launchRoblox(context)
    }

    fun compileRobloxSpeed(context: Context) {
        viewModelScope.launch {
            val success = shizukuManager.compileRobloxSpeed()
            addLog(
                BoostLog(
                    timeFormatted = timeFormat.format(Date()),
                    category = "DEX",
                    message = if (success) "Compiled Roblox Dalvik/ART bytecode to native AOT speed code!"
                    else "DEX compilation completed",
                    isSuccess = success
                )
            )
            Toast.makeText(context, "Roblox AOT speed compilation executed", Toast.LENGTH_SHORT).show()
        }
    }

    fun resetResolution(context: Context) {
        viewModelScope.launch {
            val success = shizukuManager.resetResolution()
            addLog(
                BoostLog(
                    timeFormatted = timeFormat.format(Date()),
                    category = "DISPLAY",
                    message = "Reset display render target and game overlay to native physical resolution",
                    isSuccess = success
                )
            )
            Toast.makeText(context, "Display resolution reset to native", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchRoblox(context: Context) {
        val pkg = _telemetry.value.robloxPackageName ?: "com.roblox.client"
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                Toast.makeText(context, "Launching Roblox with Blox Booster...", Toast.LENGTH_SHORT).show()
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
            fpsCap = prefs.getInt("fpsCap", 60),
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
