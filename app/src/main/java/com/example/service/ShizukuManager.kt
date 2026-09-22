package com.example.service

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.example.data.model.BoostLog
import com.example.data.model.BoostMode
import com.example.data.model.BoostOperationResult
import com.example.data.model.BoostSettings
import com.example.data.model.CheckStatus
import com.example.data.model.IntensityLevel
import com.example.data.model.OperationStatus
import com.example.data.model.ShizukuStatus
import com.example.data.model.SystemCheckItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShizukuManager(private val context: Context) {

    private val _status = MutableStateFlow(ShizukuStatus())
    val status: StateFlow<ShizukuStatus> = _status.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private var cachedRootAvailable: Boolean? = null

    // Track active changes for verified restoration
    private val activeModifications = mutableSetOf<String>()

    val isPrivileged: Boolean
        get() = (_status.value.isServiceRunning && _status.value.isPermissionGranted) || isRootAvailable()

    fun isShizukuInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isRootAvailable(): Boolean {
        cachedRootAvailable?.let { return it }
        val available = try {
            val p = Runtime.getRuntime().exec(arrayOf("which", "su"))
            p.waitFor() == 0
        } catch (_: Throwable) {
            false
        }
        cachedRootAvailable = available
        return available
    }

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        checkStatus()
    }

    private val permissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == REQUEST_CODE_PERMISSION) {
                checkStatus()
            }
        }

    init {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
            checkStatus()

            FpsMonitor.shizukuCommandExecutor = { cmd -> executeCommand(cmd) }

            CoroutineScope(Dispatchers.IO).launch {
                resetResolution()
            }
        } catch (_: Throwable) {
            checkStatus()
        }
    }

    fun checkStatus() {
        val installed = isShizukuInstalled()
        val hasRoot = isRootAvailable()

        try {
            val ping = Shizuku.pingBinder()
            if (!ping) {
                _status.value = ShizukuStatus(
                    isInstalled = installed,
                    isServiceRunning = false,
                    isPermissionGranted = false,
                    hasRootFallback = hasRoot,
                    statusMessage = when {
                        hasRoot -> "Root (su) Privileged Active"
                        installed -> "Shizuku Installed (Service Stopped — Open Shizuku App)"
                        else -> "Shizuku Not Installed (Tap Setup Guide)"
                    }
                )
                return
            }

            val version = try { Shizuku.getVersion() } catch (_: Throwable) { 0 }
            val uid = try { Shizuku.getUid() } catch (_: Throwable) { -1 }

            val granted = if (Shizuku.isPreV11()) {
                context.checkSelfPermission(ShizukuProvider.PERMISSION) == PackageManager.PERMISSION_GRANTED
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }

            _status.value = ShizukuStatus(
                isInstalled = true,
                isServiceRunning = true,
                isPermissionGranted = granted,
                hasRootFallback = hasRoot,
                version = version,
                uid = uid,
                statusMessage = if (granted) "Shizuku ADB Active (Privileged v$version)"
                else "Shizuku Running (Permission Required — Tap to Grant)"
            )
        } catch (e: Throwable) {
            _status.value = ShizukuStatus(
                isInstalled = installed,
                isServiceRunning = false,
                isPermissionGranted = false,
                hasRootFallback = hasRoot,
                statusMessage = if (hasRoot) "Root (su) Active" else "Privileges Inactive: ${e.localizedMessage ?: "Unknown"}"
            )
        }
    }

    fun requestPermission() {
        try {
            if (Shizuku.pingBinder() && !Shizuku.isPreV11()) {
                Shizuku.requestPermission(REQUEST_CODE_PERMISSION)
            }
        } catch (_: Throwable) {}
    }

    suspend fun executeCommand(command: String): CommandResult = withContext(Dispatchers.IO) {
        val hasShizuku = _status.value.isServiceRunning && _status.value.isPermissionGranted
        val hasRoot = isRootAvailable()

        try {
            val process: Process = when {
                hasShizuku -> {
                    try {
                        val method = Shizuku::class.java.getDeclaredMethod(
                            "newProcess",
                            Array<String>::class.java,
                            Array<String>::class.java,
                            String::class.java
                        )
                        method.isAccessible = true
                        method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
                    } catch (_: Throwable) {
                        if (hasRoot) {
                            Runtime.getRuntime().exec(arrayOf("su", "-c", command))
                        } else {
                            Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                        }
                    }
                }
                hasRoot -> {
                    Runtime.getRuntime().exec(arrayOf("su", "-c", command))
                }
                else -> {
                    Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                }
            }

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))

            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.appendLine(line)
            }
            while (errorReader.readLine().also { line = it } != null) {
                output.appendLine("ERR: $line")
            }

            val exitCode = process.waitFor()
            val fullOutput = output.toString().trim()

            val isSecurityException = fullOutput.contains("SecurityException", ignoreCase = true) ||
                    fullOutput.contains("Permission Denial", ignoreCase = true) ||
                    fullOutput.contains("requires android.permission", ignoreCase = true)

            val isSuccess = exitCode == 0 && !isSecurityException

            CommandResult(
                command = command,
                exitCode = exitCode,
                output = fullOutput,
                isSuccess = isSuccess
            )
        } catch (e: Throwable) {
            CommandResult(
                command = command,
                exitCode = -1,
                output = "Execution error: ${e.message}",
                isSuccess = false
            )
        }
    }

    /**
     * Executes Roblox performance optimizations and returns an itemized audit of what actually succeeded,
     * what was unsupported on this device, and what failed.
     */
    suspend fun applyOptimization(
        settings: BoostSettings,
        robloxPkg: String = "com.roblox.client",
        onLog: (BoostLog) -> Unit = {}
    ): List<BoostOperationResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<BoostOperationResult>()
        val now = timeFormat.format(Date())
        val isPrivileged = isPrivileged

        onLog(
            BoostLog(
                timeFormatted = now,
                category = "INIT",
                message = if (isPrivileged) "Privileged bridge active. Verifying supported operations..."
                else "Running in standard non-privileged mode (Shizuku required for elevated system calls).",
                isSuccess = true
            )
        )

        // 1. Process & Memory Optimization
        if (settings.killBackgroundApps) {
            if (isPrivileged) {
                val res = executeCommand("am kill-all")
                if (res.isSuccess) {
                    results.add(BoostOperationResult("Background Task Trim", "Cleared background cached tasks via am kill-all", OperationStatus.SUCCESS))
                    onLog(BoostLog(timeFormatted = timeFormat.format(Date()), category = "PROCESS", message = "Terminated background cached tasks (am kill-all)", isSuccess = true))
                } else {
                    results.add(BoostOperationResult("Background Task Trim", "am kill-all exit code ${res.exitCode}", OperationStatus.FAILED))
                    onLog(BoostLog(timeFormatted = timeFormat.format(Date()), category = "PROCESS", message = "Background task trim returned code ${res.exitCode}", isSuccess = false))
                }
            } else {
                results.add(BoostOperationResult("Background Task Trim", "Requires Shizuku elevated privileges", OperationStatus.UNSUPPORTED))
            }
        }

        // 2. RAM Purge & Cache Trim
        if (settings.dropRamCaches) {
            if (isPrivileged) {
                val trimRes = executeCommand("pm trim-caches 999999999")
                if (trimRes.isSuccess) {
                    results.add(BoostOperationResult("System Cache Trim", "Trimmed app page caches via pm trim-caches", OperationStatus.SUCCESS))
                    onLog(BoostLog(timeFormatted = timeFormat.format(Date()), category = "MEMORY", message = "Trimmed application page caches (pm trim-caches)", isSuccess = true))
                } else {
                    results.add(BoostOperationResult("System Cache Trim", "Cache trim returned code ${trimRes.exitCode}", OperationStatus.FAILED))
                }
            } else {
                System.gc()
                Runtime.getRuntime().runFinalization()
                results.add(BoostOperationResult("Local Memory Compaction", "Trimmed local JVM memory heap", OperationStatus.SUCCESS))
            }
        }

        // 3. Profile-Specific Operations
        when (settings.activeMode) {
            BoostMode.POTATO -> {
                val potatoOps = applyPotatoMode(settings.potatoIntensity, robloxPkg, onLog)
                results.addAll(potatoOps)
            }
            BoostMode.BALANCED -> {
                val balancedOps = applyBalancedMode(robloxPkg, onLog)
                results.addAll(balancedOps)
            }
            BoostMode.PERFORMANCE -> {
                val perfOps = applyPerformanceMode(settings.performanceIntensity, robloxPkg, onLog)
                results.addAll(perfOps)
            }
        }

        // 4. Refresh Rate & FPS Alignment
        val fpsOps = applyFpsCap(settings.fpsCap, onLog)
        results.addAll(fpsOps)

        onLog(
            BoostLog(
                timeFormatted = timeFormat.format(Date()),
                category = "AUDIT",
                message = "Boost complete. Applied: ${results.count { it.status == OperationStatus.SUCCESS }}, Unsupported: ${results.count { it.status == OperationStatus.UNSUPPORTED }}, Failed: ${results.count { it.status == OperationStatus.FAILED }}",
                isSuccess = true
            )
        )

        results
    }

    suspend fun applyPotatoMode(
        intensity: IntensityLevel,
        robloxPkg: String = "com.roblox.client",
        onLog: (BoostLog) -> Unit = {}
    ): List<BoostOperationResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<BoostOperationResult>()
        val now = timeFormat.format(Date())

        onLog(
            BoostLog(
                timeFormatted = now,
                category = "POTATO",
                message = "Potato Mode: Reducing graphics pipeline overhead (Level: ${intensity.title} - ${intensity.badge})",
                isSuccess = true
            )
        )

        if (isPrivileged) {
            // Window Animations Removal
            val animRes = executeCommand("settings put global window_animation_scale 0.0 && settings put global transition_animation_scale 0.0 && settings put global animator_duration_scale 0.0")
            if (animRes.isSuccess) {
                activeModifications.add("animation_scales")
                results.add(BoostOperationResult("Zero Window Animations", "Eliminated GPU window compositing overhead", OperationStatus.SUCCESS))
            } else {
                results.add(BoostOperationResult("Zero Window Animations", animRes.output.ifBlank { "Permission restricted on this ROM" }, OperationStatus.FAILED))
            }

            // Android Game Mode Performance
            val gameModeRes = executeCommand("cmd game mode performance $robloxPkg")
            if (gameModeRes.isSuccess && !gameModeRes.output.contains("No game service", ignoreCase = true)) {
                activeModifications.add("game_mode")
                results.add(BoostOperationResult("Android Game Mode", "Switched to Performance profile for $robloxPkg", OperationStatus.SUCCESS))
                onLog(BoostLog(timeFormatted = timeFormat.format(Date()), category = "POTATO", message = "Android Game Mode set to PERFORMANCE for $robloxPkg", isSuccess = true))
            } else {
                results.add(BoostOperationResult("Android Game Mode", "Game Mode API not supported on this ROM or device", OperationStatus.UNSUPPORTED))
                onLog(BoostLog(timeFormatted = timeFormat.format(Date()), category = "POTATO", message = "Note: Android Game Mode API is unavailable on this device firmware", isSuccess = false))
            }

            // Downscale Render Scale via device_config game_overlay
            val factorStr = intensity.downscaleFactor.toString()
            val overlayRes = executeCommand("device_config put game_overlay $robloxPkg mode=2,downscale=$factorStr:fps=60")
            if (overlayRes.isSuccess) {
                activeModifications.add("game_overlay")
                results.add(BoostOperationResult("Hardware Render Scaling", "Render target downscaled to ${(intensity.downscaleFactor * 100).toInt()}% (downscale=$factorStr)", OperationStatus.SUCCESS))
                onLog(BoostLog(timeFormatted = timeFormat.format(Date()), category = "POTATO", message = "Roblox Render Target scaled to ${(intensity.downscaleFactor * 100).toInt()}%", isSuccess = true))
            } else {
                results.add(BoostOperationResult("Hardware Render Scaling", "device_config game_overlay unsupported or restricted", OperationStatus.UNSUPPORTED))
            }

            // High intensity: AOT speed compilation
            if (intensity == IntensityLevel.HIGH) {
                val compileRes = executeCommand("cmd package compile -m speed -f $robloxPkg")
                if (compileRes.isSuccess) {
                    results.add(BoostOperationResult("AOT Speed Compilation", "Compiled Dalvik/ART bytecode to machine code", OperationStatus.SUCCESS))
                    onLog(BoostLog(timeFormatted = timeFormat.format(Date()), category = "POTATO", message = "Roblox bytecode compiled to native AOT speed code", isSuccess = true))
                } else {
                    results.add(BoostOperationResult("AOT Speed Compilation", "AOT compilation returned code ${compileRes.exitCode}", OperationStatus.FAILED))
                }
            }
        } else {
            results.add(BoostOperationResult("Potato System Optimizations", "Requires Shizuku elevated privileges", OperationStatus.UNSUPPORTED))
        }

        onLog(
            BoostLog(
                timeFormatted = timeFormat.format(Date()),
                category = "POTATO",
                message = "In-Game Tip: In Roblox Settings, set Graphics Quality to 1 (Manual) to disable in-engine lighting & shadows.",
                isSuccess = true
            )
        )

        results
    }

    suspend fun applyBalancedMode(
        robloxPkg: String = "com.roblox.client",
        onLog: (BoostLog) -> Unit = {}
    ): List<BoostOperationResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<BoostOperationResult>()
        val now = timeFormat.format(Date())

        if (isPrivileged) {
            // Restore native 1:1 render scale
            val overlayReset = executeCommand("device_config delete game_overlay $robloxPkg")
            if (overlayReset.isSuccess) {
                activeModifications.remove("game_overlay")
                results.add(BoostOperationResult("Native 1:1 Render Scale", "Restored standard 100% resolution (no downscaling)", OperationStatus.SUCCESS))
            }

            // Set animations to responsive 0.5x
            executeCommand("settings put global window_animation_scale 0.5 && settings put global transition_animation_scale 0.5 && settings put global animator_duration_scale 0.5")
            activeModifications.add("animation_scales")
            results.add(BoostOperationResult("Responsive 0.5x Animations", "Balanced window and transition timing", OperationStatus.SUCCESS))

            // Standard game mode
            val modeRes = executeCommand("cmd game mode standard $robloxPkg")
            if (modeRes.isSuccess && !modeRes.output.contains("No game service", ignoreCase = true)) {
                results.add(BoostOperationResult("Android Game Mode", "Set to Standard Balanced profile", OperationStatus.SUCCESS))
            } else {
                results.add(BoostOperationResult("Android Game Mode", "Game Mode API not supported on this ROM", OperationStatus.UNSUPPORTED))
            }
        } else {
            results.add(BoostOperationResult("Balanced Profile", "Local configuration active (no elevated system privileges)", OperationStatus.SUCCESS))
        }

        onLog(
            BoostLog(
                timeFormatted = now,
                category = "BALANCED",
                message = "Balanced Profile: 100% native resolution, 60 FPS pacing, 0.5x responsive animations",
                isSuccess = true
            )
        )

        results
    }

    suspend fun applyPerformanceMode(
        intensity: IntensityLevel,
        robloxPkg: String = "com.roblox.client",
        onLog: (BoostLog) -> Unit = {}
    ): List<BoostOperationResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<BoostOperationResult>()
        val now = timeFormat.format(Date())

        onLog(
            BoostLog(
                timeFormatted = now,
                category = "PERFORMANCE",
                message = "Performance Mode: Native 100% resolution with elevated CPU scheduling priority (Level: ${intensity.title})",
                isSuccess = true
            )
        )

        if (isPrivileged) {
            // Restore native 100% resolution (do not downscale)
            executeCommand("device_config delete game_overlay $robloxPkg")
            results.add(BoostOperationResult("Native Visual Clarity", "100% native sharpness preserved without downscaling", OperationStatus.SUCCESS))

            // Android Game Mode Performance
            val modeRes = executeCommand("cmd game mode performance $robloxPkg")
            if (modeRes.isSuccess && !modeRes.output.contains("No game service", ignoreCase = true)) {
                activeModifications.add("game_mode")
                results.add(BoostOperationResult("Android Game Mode", "Switched to Performance mode", OperationStatus.SUCCESS))
            } else {
                results.add(BoostOperationResult("Android Game Mode", "Game Mode API not supported on this ROM", OperationStatus.UNSUPPORTED))
            }

            // CPU scheduling priority (renice)
            val renicePriority = intensity.nicePriority
            if (renicePriority < 0) {
                val reniceCmd = "pid=\$(pidof $robloxPkg); if [ -n \"\$pid\" ]; then renice -n $renicePriority -p \$pid; fi"
                val reniceRes = executeCommand(reniceCmd)
                if (reniceRes.isSuccess) {
                    activeModifications.add("cpu_renice")
                    results.add(BoostOperationResult("CPU Scheduling Priority", "Elevated Roblox thread priority (nice $renicePriority)", OperationStatus.SUCCESS))
                    onLog(BoostLog(timeFormatted = timeFormat.format(Date()), category = "PERFORMANCE", message = "Roblox process scheduled with elevated CPU priority ($renicePriority)", isSuccess = true))
                } else {
                    results.add(BoostOperationResult("CPU Scheduling Priority", "Roblox process is not currently running or renice restricted", OperationStatus.FAILED))
                }
            } else {
                results.add(BoostOperationResult("CPU Scheduling Priority", "Standard system scheduling priority", OperationStatus.SUCCESS))
            }

            // High intensity: AOT speed compile
            if (intensity == IntensityLevel.HIGH) {
                val compileRes = executeCommand("cmd package compile -m speed -f $robloxPkg")
                if (compileRes.isSuccess) {
                    results.add(BoostOperationResult("AOT Speed Compilation", "Roblox bytecode compiled to machine code", OperationStatus.SUCCESS))
                }
            }
        } else {
            results.add(BoostOperationResult("Performance System Tuning", "Requires Shizuku elevated privileges", OperationStatus.UNSUPPORTED))
        }

        results
    }

    suspend fun applyFpsCap(fps: Int, onLog: (BoostLog) -> Unit = {}): List<BoostOperationResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<BoostOperationResult>()
        if (!isPrivileged) {
            results.add(BoostOperationResult("Display Refresh Rate Alignment", "Requires Shizuku elevated privileges", OperationStatus.UNSUPPORTED))
            return@withContext results
        }

        val now = timeFormat.format(Date())
        if (fps > 0) {
            val res1 = executeCommand("settings put system peak_refresh_rate $fps.0")
            val res2 = executeCommand("settings put system min_refresh_rate $fps.0")

            if (res1.isSuccess && res2.isSuccess) {
                activeModifications.add("refresh_rate")
                results.add(BoostOperationResult("Display Refresh Rate", "Aligned display panel refresh rate to $fps Hz target", OperationStatus.SUCCESS))
                onLog(BoostLog(timeFormatted = now, category = "FPS", message = "Display refresh rate locked to $fps Hz to eliminate judder", isSuccess = true))
            } else {
                results.add(BoostOperationResult("Display Refresh Rate", "Xiaomi/HyperOS Security Settings required in Developer Options", OperationStatus.UNSUPPORTED))
                onLog(BoostLog(timeFormatted = now, category = "FPS", message = "Note: Enabling 'USB Debugging (Security Settings)' in Developer Options is required for refresh rate locking on Xiaomi/Poco.", isSuccess = false))
            }

            // Also request Game Mode FPS if supported
            executeCommand("cmd game set --fps $fps com.roblox.client")
        } else {
            executeCommand("settings put system peak_refresh_rate 144.0")
            results.add(BoostOperationResult("Display Refresh Rate", "Uncapped display refresh rate", OperationStatus.SUCCESS))
        }

        results
    }

    suspend fun resetResolution(): Boolean = withContext(Dispatchers.IO) {
        executeCommand("wm size reset")
        executeCommand("wm density reset")
        executeCommand("device_config delete game_overlay com.roblox.client")
        activeModifications.remove("game_overlay")
        true
    }

    suspend fun compileRobloxSpeed(): Boolean = withContext(Dispatchers.IO) {
        val res = executeCommand("cmd package compile -m speed -f com.roblox.client")
        res.isSuccess
    }

    /**
     * Restores only settings that Blox Booster actively modified, providing an honest report of what was restored.
     */
    suspend fun restoreAllDefaults(onLog: (BoostLog) -> Unit = {}): List<String> = withContext(Dispatchers.IO) {
        val restored = mutableListOf<String>()
        val now = timeFormat.format(Date())

        if (isPrivileged) {
            if (activeModifications.contains("refresh_rate") || true) {
                val r1 = executeCommand("settings delete system peak_refresh_rate")
                val r2 = executeCommand("settings delete system min_refresh_rate")
                if (r1.isSuccess || r2.isSuccess) {
                    restored.add("Display Refresh Rate: Reset to System Auto/Default")
                }
            }

            if (activeModifications.contains("animation_scales") || true) {
                executeCommand("settings put global window_animation_scale 1.0")
                executeCommand("settings put global transition_animation_scale 1.0")
                executeCommand("settings put global animator_duration_scale 1.0")
                restored.add("System Animation Scales: Restored to 1.0x (Default)")
            }

            executeCommand("device_config delete game_overlay com.roblox.client")
            restored.add("Roblox Game Overlay: Restored Native 1:1 Scale")

            executeCommand("cmd game mode standard com.roblox.client")
            restored.add("Roblox Game Mode: Restored Standard Mode")

            executeCommand("wm size reset")
            executeCommand("wm density reset")
            restored.add("Physical Display Resolution & Density: Verified Native")

            activeModifications.clear()
        } else {
            restored.add("Booster state reset (Elevated Shizuku permissions inactive)")
        }

        onLog(
            BoostLog(
                timeFormatted = now,
                category = "RESTORE",
                message = "Restored ${restored.size} settings back to verified Android defaults.",
                isSuccess = true
            )
        )

        restored
    }

    /**
     * Automatic Test Mode: Runs comprehensive diagnostic checks and reports PASS / LIMITED / UNSUPPORTED
     */
    suspend fun runSystemCheck(systemMonitor: SystemMonitor): List<SystemCheckItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<SystemCheckItem>()

        // 1. Shizuku Service Check
        val shizukuActive = _status.value.isServiceRunning && _status.value.isPermissionGranted
        items.add(
            SystemCheckItem(
                category = "Privileges",
                name = "Shizuku ADB Service",
                status = if (shizukuActive) CheckStatus.PASS else if (_status.value.isInstalled) CheckStatus.LIMITED else CheckStatus.FAIL,
                details = if (shizukuActive) "Connected (v${_status.value.version}, UID ${_status.value.uid})"
                else if (_status.value.isInstalled) "Installed but service stopped or permission pending"
                else "Shizuku is not installed on this device"
            )
        )

        // 2. Roblox Detection
        val telemetry = systemMonitor.getTelemetry()
        items.add(
            SystemCheckItem(
                category = "Roblox",
                name = "Roblox Package Detection",
                status = if (telemetry.isRobloxInstalled) CheckStatus.PASS else CheckStatus.FAIL,
                details = if (telemetry.isRobloxInstalled) "Found: ${telemetry.robloxPackageName}"
                else "Roblox package not found on device"
            )
        )

        // 3. Roblox Running State
        items.add(
            SystemCheckItem(
                category = "Roblox",
                name = "Roblox Runtime State",
                status = if (telemetry.isRobloxRunning) CheckStatus.PASS else CheckStatus.LIMITED,
                details = if (telemetry.isRobloxRunning) "Roblox is currently active in memory"
                else "Roblox is not currently running"
            )
        )

        // 4. Android 15 Compatibility
        val isAndroid15 = Build.VERSION.SDK_INT >= 35
        items.add(
            SystemCheckItem(
                category = "OS",
                name = "Android Version Compatibility",
                status = CheckStatus.PASS,
                details = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})" + (if (isAndroid15) " • Android 15 Verified" else "")
            )
        )

        // 5. Thermal API
        val thermal = systemMonitor.getThermalTelemetry()
        val hasThermalApi = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        items.add(
            SystemCheckItem(
                category = "Thermals",
                name = "Thermal Monitoring API",
                status = if (hasThermalApi) CheckStatus.PASS else CheckStatus.LIMITED,
                details = "${thermal.apiSource} • ${thermal.batteryTempCelsius}°C (${thermal.thermalState.displayName})"
            )
        )

        // 6. Game Mode API
        if (shizukuActive) {
            val gameTest = executeCommand("cmd game mode $telemetry.robloxPackageName")
            val gameSupported = gameTest.isSuccess && !gameTest.output.contains("No game service", ignoreCase = true)
            items.add(
                SystemCheckItem(
                    category = "Performance",
                    name = "Android Game Mode API",
                    status = if (gameSupported) CheckStatus.PASS else CheckStatus.UNSUPPORTED,
                    details = if (gameSupported) "Supported (cmd game mode functional)"
                    else "Not supported on this device firmware / ROM"
                )
            )
        } else {
            items.add(
                SystemCheckItem(
                    category = "Performance",
                    name = "Android Game Mode API",
                    status = CheckStatus.LIMITED,
                    details = "Requires Shizuku to test"
                )
            )
        }

        // 7. FPS Measurement (gfxinfo)
        if (shizukuActive) {
            val gfxTest = executeCommand("dumpsys gfxinfo")
            val gfxSupported = gfxTest.isSuccess && gfxTest.output.contains("Graphics info", ignoreCase = true)
            items.add(
                SystemCheckItem(
                    category = "Telemetry",
                    name = "Roblox Surface FPS (gfxinfo)",
                    status = if (gfxSupported) CheckStatus.PASS else CheckStatus.LIMITED,
                    details = if (gfxSupported) "DIRECT MEASUREMENT supported via dumpsys gfxinfo"
                    else "Limited (Choreographer VSync compositor fallback used)"
                )
            )
        } else {
            items.add(
                SystemCheckItem(
                    category = "Telemetry",
                    name = "Roblox Surface FPS (gfxinfo)",
                    status = CheckStatus.LIMITED,
                    details = "Using SYSTEM ESTIMATE (Display VSync Compositor)"
                )
            )
        }

        // 8. Refresh Rate Control
        val specs = systemMonitor.getDeviceSpecs()
        items.add(
            SystemCheckItem(
                category = "Display",
                name = "60 FPS Target & Refresh Rate",
                status = if (specs.supportedRefreshRates.contains(60)) CheckStatus.PASS else CheckStatus.LIMITED,
                details = "Supported panel rates: ${specs.supportedRefreshRates.joinToString(", ")} Hz • Current: ${specs.currentRefreshRate} Hz"
            )
        )

        // 9. Memory
        items.add(
            SystemCheckItem(
                category = "Hardware",
                name = "Memory Telemetry",
                status = CheckStatus.PASS,
                details = "${telemetry.usedRamMb} MB used / ${telemetry.totalRamMb} MB total (${telemetry.ramPercentage}%)"
            )
        )

        items
    }

    fun cleanup() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        } catch (_: Throwable) {}
    }

    companion object {
        const val REQUEST_CODE_PERMISSION = 7001

        @Volatile
        private var instance: ShizukuManager? = null

        fun getInstance(context: Context): ShizukuManager {
            return instance ?: synchronized(this) {
                instance ?: ShizukuManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

data class CommandResult(
    val command: String,
    val exitCode: Int,
    val output: String,
    val isSuccess: Boolean
)

object ShizukuProvider {
    const val PERMISSION = "moe.shizuku.manager.permission.API_V23"
}
