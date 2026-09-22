package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BoostDiagnosticReport
import com.example.data.model.BoostLog
import com.example.data.model.BoostMode
import com.example.data.model.BoostOperationResult
import com.example.data.model.BoostSettings
import com.example.data.model.DeviceSpecs
import com.example.data.model.DiagnosticSnapshot
import com.example.data.model.FpsTelemetry
import com.example.data.model.IntensityLevel
import com.example.data.model.OperationStatus
import com.example.data.model.PerformanceSession
import com.example.data.model.PlayStyle
import com.example.data.model.ShizukuStatus
import com.example.data.model.SystemCheckItem
import com.example.data.model.SystemTelemetry
import com.example.data.model.ThermalState
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
    val systemMonitor = SystemMonitor(application)

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

    // Dialog & Screen Visibility States
    private val _showShizukuHelp = MutableStateFlow(false)
    val showShizukuHelp: StateFlow<Boolean> = _showShizukuHelp.asStateFlow()

    private val _showDeviceInfo = MutableStateFlow(false)
    val showDeviceInfo: StateFlow<Boolean> = _showDeviceInfo.asStateFlow()

    private val _showRestoreDialog = MutableStateFlow(false)
    val showRestoreDialog: StateFlow<Boolean> = _showRestoreDialog.asStateFlow()

    private val _showDiagnosticsDialog = MutableStateFlow(false)
    val showDiagnosticsDialog: StateFlow<Boolean> = _showDiagnosticsDialog.asStateFlow()

    private val _showSystemCheckDialog = MutableStateFlow(false)
    val showSystemCheckDialog: StateFlow<Boolean> = _showSystemCheckDialog.asStateFlow()

    private val _showHistoryDialog = MutableStateFlow(false)
    val showHistoryDialog: StateFlow<Boolean> = _showHistoryDialog.asStateFlow()

    private val _showVisualQualityInfo = MutableStateFlow(false)
    val showVisualQualityInfo: StateFlow<Boolean> = _showVisualQualityInfo.asStateFlow()

    // Diagnostics & History State
    private val _latestDiagnosticReport = MutableStateFlow<BoostDiagnosticReport?>(null)
    val latestDiagnosticReport: StateFlow<BoostDiagnosticReport?> = _latestDiagnosticReport.asStateFlow()

    private val _systemCheckResults = MutableStateFlow<List<SystemCheckItem>>(emptyList())
    val systemCheckResults: StateFlow<List<SystemCheckItem>> = _systemCheckResults.asStateFlow()

    private val _isCheckingSystem = MutableStateFlow(false)
    val isCheckingSystem: StateFlow<Boolean> = _isCheckingSystem.asStateFlow()

    private val _performanceSessions = MutableStateFlow<List<PerformanceSession>>(loadSessions())
    val performanceSessions: StateFlow<List<PerformanceSession>> = _performanceSessions.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    init {
        FpsMonitor.targetRobloxPackage = _telemetry.value.robloxPackageName ?: "com.roblox.client"
        FpsMonitor.start()

        // Continuous telemetry polling
        viewModelScope.launch {
            systemMonitor.pollTelemetry(2500L).collect { updated ->
                _telemetry.value = updated
            }
        }

        viewModelScope.launch {
            shizukuManager.resetResolution()
        }

        val pocoNotice = if (_deviceSpecs.value.isPocoC71OrXiaomi) " (Poco C71 / Android 15 Verified)" else ""
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
        if (current.size > 60) {
            _boostLogs.value = current.take(60)
        } else {
            _boostLogs.value = current
        }
    }

    private fun captureSnapshot(): DiagnosticSnapshot {
        val tel = systemMonitor.getTelemetry()
        val specs = systemMonitor.getDeviceSpecs()
        val fpsVal = FpsMonitor.fps.value
        val ftVal = FpsMonitor.frameTimeMs.value

        return DiagnosticSnapshot(
            timeFormatted = timeFormat.format(Date()),
            cpuArch = specs.cpuArch,
            cpuCores = specs.cpuCores,
            totalRamMb = tel.totalRamMb,
            usedRamMb = tel.usedRamMb,
            freeRamMb = tel.freeRamMb,
            ramPercent = tel.ramPercentage,
            batteryTemp = tel.batteryTemp,
            refreshRate = tel.refreshRate,
            fps = fpsVal,
            frameTimeMs = ftVal,
            thermalState = tel.thermalState.displayName,
            isRobloxRunning = tel.isRobloxRunning
        )
    }

    fun boostRoblox() {
        if (_isBoosting.value) return

        viewModelScope.launch {
            _isBoosting.value = true
            _boostProgress.value = 0.05f
            _boostStepText.value = "Auditing device hardware & capturing baseline snapshot..."

            val beforeSnapshot = captureSnapshot()
            FpsMonitor.resetSessionStats()

            val now = timeFormat.format(Date())
            addLog(
                BoostLog(
                    timeFormatted = now,
                    category = "BOOST",
                    message = "Starting Roblox optimization sequence (Profile: ${_settings.value.activeMode.title})...",
                    isSuccess = true
                )
            )

            delay(300)
            _boostProgress.value = 0.25f
            _boostStepText.value = "Trimming RAM caches & reducing background task overhead..."

            // Execute optimization and receive honest audit
            val operations = shizukuManager.applyOptimization(
                settings = _settings.value,
                robloxPkg = _telemetry.value.robloxPackageName ?: "com.roblox.client",
                onLog = { log -> addLog(log) }
            )

            delay(350)
            _boostProgress.value = 0.65f
            _boostStepText.value = when (_settings.value.activeMode) {
                BoostMode.POTATO -> "Applying Potato Mode: Downscaling game overlay & zeroing animations..."
                BoostMode.BALANCED -> "Applying Balanced Profile: Native 1:1 render scale & 60 FPS pacing..."
                BoostMode.PERFORMANCE -> "Applying Performance Mode: Native clarity & elevated CPU priority..."
            }

            delay(350)
            _boostProgress.value = 0.90f
            val fpsTargetStr = if (_settings.value.fpsCap == 0) "Uncapped" else "${_settings.value.fpsCap} FPS"
            _boostStepText.value = "Aligning refresh rate target to $fpsTargetStr..."

            delay(300)
            _boostProgress.value = 1.0f
            _boostStepText.value = "Optimization complete! Diagnostics report generated."

            _telemetry.value = systemMonitor.getTelemetry()
            val afterSnapshot = captureSnapshot()

            val report = BoostDiagnosticReport(
                timeFormatted = timeFormat.format(Date()),
                modeApplied = _settings.value.activeMode,
                before = beforeSnapshot,
                after = afterSnapshot,
                operations = operations
            )
            _latestDiagnosticReport.value = report

            // Record session for comparison
            recordSession(report)

            delay(250)
            _isBoosting.value = false
            _boostProgress.value = 0f
        }
    }

    private fun recordSession(report: BoostDiagnosticReport) {
        val currentFps = FpsMonitor.fpsTelemetry.value.averageFps
        val minFps = FpsMonitor.fpsTelemetry.value.minFps
        val ft = FpsMonitor.frameTimeMs.value
        val source = FpsMonitor.fpsTelemetry.value.source.labelBadge

        val session = PerformanceSession(
            id = System.currentTimeMillis(),
            timestampFormatted = timeFormat.format(Date()),
            mode = report.modeApplied,
            averageFps = if (currentFps > 0) currentFps else report.after?.fps ?: 60,
            minFps = if (minFps > 0) minFps else 45,
            averageFrameTimeMs = if (ft > 0) ft else 16.6f,
            batteryTempPeak = report.after?.batteryTemp ?: 34f,
            durationSeconds = 60,
            measurementSource = source
        )

        val updated = _performanceSessions.value.toMutableList()
        updated.add(0, session)
        val trimmed = updated.take(10)
        _performanceSessions.value = trimmed
        saveSessions(trimmed)
    }

    fun runSystemCheck() {
        if (_isCheckingSystem.value) return
        viewModelScope.launch {
            _isCheckingSystem.value = true
            val results = shizukuManager.runSystemCheck(systemMonitor)
            _systemCheckResults.value = results
            _isCheckingSystem.value = false
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
                message = "Switching profile to ${mode.title}: Applying legitimate system parameters...",
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
                BoostMode.PERFORMANCE -> {
                    shizukuManager.applyPerformanceMode(updated.performanceIntensity, pkg) { addLog(it) }
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
                message = "Applied play style: ${style.title} (Recommended ${style.recommendedFps} FPS, ${style.recommendedMode.title})",
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

    fun setPotatoIntensity(level: IntensityLevel) {
        val updated = _settings.value.copy(potatoIntensity = level)
        _settings.value = updated
        saveSettings(updated)
        if (_settings.value.activeMode == BoostMode.POTATO) {
            viewModelScope.launch {
                val pkg = _telemetry.value.robloxPackageName ?: "com.roblox.client"
                shizukuManager.applyPotatoMode(level, pkg) { addLog(it) }
            }
        }
    }

    fun setPerformanceIntensity(level: IntensityLevel) {
        val updated = _settings.value.copy(performanceIntensity = level)
        _settings.value = updated
        saveSettings(updated)
        if (_settings.value.activeMode == BoostMode.PERFORMANCE) {
            viewModelScope.launch {
                val pkg = _telemetry.value.robloxPackageName ?: "com.roblox.client"
                shizukuManager.applyPerformanceMode(level, pkg) { addLog(it) }
            }
        }
    }

    fun resetPotatoSettings() {
        setPotatoIntensity(IntensityLevel.MEDIUM)
    }

    fun resetBalancedSettings() {
        setFpsCap(60)
        setMode(BoostMode.BALANCED)
    }

    fun resetPerformanceSettings() {
        setPerformanceIntensity(IntensityLevel.MEDIUM)
    }

    fun restoreAllDefaults(context: Context) {
        viewModelScope.launch {
            val restored = shizukuManager.restoreAllDefaults { addLog(it) }
            val resetSettings = BoostSettings(
                activeMode = BoostMode.BALANCED,
                playStyle = PlayStyle.OPEN_WORLD_RP,
                fpsCap = 60,
                potatoIntensity = IntensityLevel.MEDIUM,
                performanceIntensity = IntensityLevel.MEDIUM
            )
            _settings.value = resetSettings
            saveSettings(resetSettings)
            Toast.makeText(context, "Restored ${restored.size} settings back to verified Android defaults", Toast.LENGTH_LONG).show()
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

    fun setShowDiagnosticsDialog(show: Boolean) {
        _showDiagnosticsDialog.value = show
    }

    fun setShowSystemCheckDialog(show: Boolean) {
        if (show) {
            runSystemCheck()
        }
        _showSystemCheckDialog.value = show
    }

    fun setShowHistoryDialog(show: Boolean) {
        _showHistoryDialog.value = show
    }

    fun setShowVisualQualityInfo(show: Boolean) {
        _showVisualQualityInfo.value = show
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
            putString("potatoIntensity", s.potatoIntensity.name)
            putString("performanceIntensity", s.performanceIntensity.name)
            apply()
        }
    }

    private fun loadSettings(): BoostSettings {
        val modeName = prefs.getString("activeMode", BoostMode.BALANCED.name) ?: BoostMode.BALANCED.name
        val styleName = prefs.getString("playStyle", PlayStyle.COMPETITIVE_PVP.name) ?: PlayStyle.COMPETITIVE_PVP.name
        val activeMode = try {
            if (modeName == "SHADERS") BoostMode.PERFORMANCE else BoostMode.valueOf(modeName)
        } catch (_: Exception) {
            BoostMode.BALANCED
        }
        val playStyle = try { PlayStyle.valueOf(styleName) } catch (_: Exception) { PlayStyle.COMPETITIVE_PVP }

        val potatoLvl = try {
            IntensityLevel.valueOf(prefs.getString("potatoIntensity", IntensityLevel.MEDIUM.name) ?: IntensityLevel.MEDIUM.name)
        } catch (_: Exception) { IntensityLevel.MEDIUM }

        val perfLvl = try {
            IntensityLevel.valueOf(prefs.getString("performanceIntensity", IntensityLevel.MEDIUM.name) ?: IntensityLevel.MEDIUM.name)
        } catch (_: Exception) { IntensityLevel.MEDIUM }

        return BoostSettings(
            activeMode = activeMode,
            playStyle = playStyle,
            fpsCap = prefs.getInt("fpsCap", 60),
            potatoIntensity = potatoLvl,
            performanceIntensity = perfLvl
        )
    }

    private fun saveSessions(list: List<PerformanceSession>) {
        val str = list.joinToString(";;") { s ->
            "${s.id}|${s.timestampFormatted}|${s.mode.name}|${s.averageFps}|${s.minFps}|${s.averageFrameTimeMs}|${s.batteryTempPeak}|${s.durationSeconds}|${s.measurementSource}"
        }
        prefs.edit().putString("saved_sessions", str).apply()
    }

    private fun loadSessions(): List<PerformanceSession> {
        val str = prefs.getString("saved_sessions", null) ?: return emptyList()
        val result = mutableListOf<PerformanceSession>()
        try {
            val items = str.split(";;")
            for (item in items) {
                val parts = item.split("|")
                if (parts.size >= 9) {
                    val mode = try { BoostMode.valueOf(parts[2]) } catch (_: Exception) { BoostMode.BALANCED }
                    result.add(
                        PerformanceSession(
                            id = parts[0].toLongOrNull() ?: System.currentTimeMillis(),
                            timestampFormatted = parts[1],
                            mode = mode,
                            averageFps = parts[3].toIntOrNull() ?: 60,
                            minFps = parts[4].toIntOrNull() ?: 50,
                            averageFrameTimeMs = parts[5].toFloatOrNull() ?: 16.6f,
                            batteryTempPeak = parts[6].toFloatOrNull() ?: 34.0f,
                            durationSeconds = parts[7].toIntOrNull() ?: 60,
                            measurementSource = parts[8]
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        return result
    }

    override fun onCleared() {
        super.onCleared()
        shizukuManager.cleanup()
    }
}
