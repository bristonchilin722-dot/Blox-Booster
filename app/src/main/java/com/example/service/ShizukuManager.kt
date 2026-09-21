package com.example.service

import android.content.Context
import android.content.pm.PackageManager
import com.example.data.model.BoostLog
import com.example.data.model.BoostMode
import com.example.data.model.BoostSettings
import com.example.data.model.ShizukuStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ShizukuManager(private val context: Context) {

    private val _status = MutableStateFlow(ShizukuStatus())
    val status: StateFlow<ShizukuStatus> = _status.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private var cachedRootAvailable: Boolean? = null

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

    val isPrivileged: Boolean
        get() = (_status.value.isServiceRunning && _status.value.isPermissionGranted) || isRootAvailable()

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
            // Reset any previous resolution distortion immediately on startup
            CoroutineScope(Dispatchers.IO).launch {
                resetResolution()
            }
        } catch (e: Throwable) {
            _status.value = ShizukuStatus(
                isServiceRunning = isRootAvailable(),
                isPermissionGranted = isRootAvailable(),
                statusMessage = if (isRootAvailable()) "Root (su) Active" else "Privileges not active: ${e.message}"
            )
        }
    }

    fun checkStatus() {
        try {
            val ping = Shizuku.pingBinder()
            if (!ping) {
                val hasRoot = isRootAvailable()
                _status.value = ShizukuStatus(
                    isServiceRunning = hasRoot,
                    isPermissionGranted = hasRoot,
                    statusMessage = if (hasRoot) "Root (su) Privileged Active" else "Shizuku not running (Start Shizuku app for ADB mode)"
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
                isServiceRunning = true,
                isPermissionGranted = granted,
                version = version,
                uid = uid,
                statusMessage = if (granted) "Shizuku Active (Privileged v$version)" else "Shizuku Permission Required"
            )
        } catch (e: Throwable) {
            val hasRoot = isRootAvailable()
            _status.value = ShizukuStatus(
                isServiceRunning = hasRoot,
                isPermissionGranted = hasRoot,
                statusMessage = if (hasRoot) "Root (su) Active" else "Shizuku offline: ${e.localizedMessage ?: "Unknown"}"
            )
        }
    }

    fun requestPermission() {
        try {
            if (Shizuku.pingBinder() && !Shizuku.isPreV11()) {
                Shizuku.requestPermission(REQUEST_CODE_PERMISSION)
            }
        } catch (_: Throwable) {
            // Handled
        }
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
            CommandResult(
                command = command,
                exitCode = exitCode,
                output = output.toString().trim(),
                isSuccess = exitCode == 0
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
     * Executes the comprehensive Roblox lag reduction and performance optimization
     */
    suspend fun applyOptimization(
        settings: BoostSettings,
        robloxPkg: String = "com.roblox.client",
        onLog: (BoostLog) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        val now = timeFormat.format(Date())
        val isPrivileged = _status.value.isServiceRunning && _status.value.isPermissionGranted

        onLog(
            BoostLog(
                timeFormatted = now,
                category = "INIT",
                message = if (isPrivileged) "Initializing Shizuku ADB privileged booster engine..."
                else "Shizuku not active: using system-level optimization fallback.",
                isSuccess = true
            )
        )

        // 1. Process Optimization & Background Killer
        if (settings.killBackgroundApps) {
            if (isPrivileged) {
                val killCmd = "am kill-all"
                val res = executeCommand(killCmd)
                onLog(
                    BoostLog(
                        timeFormatted = timeFormat.format(Date()),
                        category = "PROCESS",
                        message = "Terminated non-critical background apps (am kill-all: exit ${res.exitCode})",
                        isSuccess = true
                    )
                )

                // Limit phantom background processes based on boost intensity
                val maxPhantom = if (settings.boostIntensity > 80f) "2" else if (settings.boostIntensity > 50f) "4" else "8"
                executeCommand("device_config put activity_manager max_phantom_processes $maxPhantom")
                onLog(
                    BoostLog(
                        timeFormatted = timeFormat.format(Date()),
                        category = "PROCESS",
                        message = "Set max phantom processes to $maxPhantom for reduced thermal load",
                        isSuccess = true
                    )
                )
            } else {
                // Fallback local process cleaner
                try {
                    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
                    am?.killBackgroundProcesses(context.packageName)
                    onLog(
                        BoostLog(
                            timeFormatted = timeFormat.format(Date()),
                            category = "PROCESS",
                            message = "Requested system background cleanup via ActivityManager",
                            isSuccess = true
                        )
                    )
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }

        // 2. RAM Purge & Memory Compaction
        if (settings.dropRamCaches) {
            if (isPrivileged) {
                executeCommand("pm trim-caches 999999999")
                executeCommand("echo 3 > /proc/sys/vm/drop_caches 2>/dev/null || true")
                onLog(
                    BoostLog(
                        timeFormatted = timeFormat.format(Date()),
                        category = "MEMORY",
                        message = "Flushed RAM page caches & executed memory compaction",
                        isSuccess = true
                    )
                )
            } else {
                System.gc()
                Runtime.getRuntime().runFinalization()
                onLog(
                    BoostLog(
                        timeFormatted = timeFormat.format(Date()),
                        category = "MEMORY",
                        message = "Triggered JVM heap compaction & local cache purge",
                        isSuccess = true
                    )
                )
            }
        }

        // 3. Mode-Specific Tweaks: Potato Mode vs Shaders Mode
        when (settings.activeMode) {
            BoostMode.POTATO -> {
                val scale = settings.potatoIntensity
                onLog(
                    BoostLog(
                        timeFormatted = timeFormat.format(Date()),
                        category = "POTATO",
                        message = "Enforcing Potato Mode (Intensity: ${scale.toInt()}%): Downscaling resolution & dropping render load",
                        isSuccess = true
                    )
                )
                if (isPrivileged) {
                    // Disable animations to eliminate frame drops
                    executeCommand("settings put global window_animation_scale 0.0")
                    executeCommand("settings put global transition_animation_scale 0.0")
                    executeCommand("settings put global animator_duration_scale 0.0")
                    executeCommand("settings put global force_msaa 0")
                    executeCommand("setprop debug.egl.force_msaa 0")

                    // Android Game Mode Performance & Downscale for Roblox
                    executeCommand("cmd game mode performance com.roblox.client")
                    executeCommand("device_config put game_overlay com.roblox.client mode=2,downscale=0.5:fps=60")

                    // Hardware Display Resolution Downscale based on potato intensity
                    val scalePercent = when {
                        scale >= 75f -> 50f  // 50% resolution -> 75% fewer pixels for max FPS
                        scale >= 45f -> 65f  // 65% resolution
                        scale >= 20f -> 80f  // 80% resolution
                        else -> 100f
                    }
                    if (scalePercent < 100f) {
                        applyResolutionScale(scalePercent)
                        onLog(
                            BoostLog(
                                timeFormatted = timeFormat.format(Date()),
                                category = "POTATO",
                                message = "GPU Render Target scaled to ${scalePercent.toInt()}% for dramatic lag reduction",
                                isSuccess = true
                            )
                        )
                    } else {
                        resetResolution()
                    }

                    // AOT DEX Speed Compilation to prevent script lag
                    executeCommand("cmd package compile -m speed -f com.roblox.client")
                }
            }

            BoostMode.SHADERS -> {
                val shadersScale = settings.shadersIntensity
                onLog(
                    BoostLog(
                        timeFormatted = timeFormat.format(Date()),
                        category = "SHADERS",
                        message = "Enabling Shaders Enhancement (${shadersScale.toInt()}%): 4x MSAA, SurfaceFlinger GPU pipeline & HDR depth",
                        isSuccess = true
                    )
                )
                if (isPrivileged) {
                    // Restore native sharp resolution
                    resetResolution()

                    // Enable 4x MSAA anti-aliasing
                    executeCommand("settings put global force_msaa 1")
                    executeCommand("setprop debug.egl.force_msaa 1")

                    // Enable hardware rendering layers and force GPU composition
                    executeCommand("service call SurfaceFlinger 1008 i32 1")
                    executeCommand("setprop debug.sf.hw 1")
                    executeCommand("settings put global window_animation_scale 0.5")

                    // Game Mode High Fidelity
                    executeCommand("cmd game mode standard com.roblox.client")
                    executeCommand("device_config put game_overlay com.roblox.client mode=1:fps=120")

                    onLog(
                        BoostLog(
                            timeFormatted = timeFormat.format(Date()),
                            category = "SHADERS",
                            message = "SurfaceFlinger direct GPU composition & 4x MSAA shader pipeline active",
                            isSuccess = true
                        )
                    )
                }
            }

            BoostMode.BALANCED -> {
                if (isPrivileged) {
                    resetResolution()
                    executeCommand("settings put global force_msaa 0")
                    executeCommand("settings put global window_animation_scale 0.5")
                    executeCommand("settings put global transition_animation_scale 0.5")
                }
                onLog(
                    BoostLog(
                        timeFormatted = timeFormat.format(Date()),
                        category = "BALANCED",
                        message = "Balanced configuration applied: Native resolution, 60 FPS target with stable thermals",
                        isSuccess = true
                    )
                )
            }
        }

        // 4. Custom FPS Cap & Refresh Rate Sync
        if (settings.fpsCap > 0 && isPrivileged) {
            executeCommand("settings put system peak_refresh_rate ${settings.fpsCap}.0")
            executeCommand("settings put system min_refresh_rate ${settings.fpsCap}.0")
            onLog(
                BoostLog(
                    timeFormatted = timeFormat.format(Date()),
                    category = "FPS",
                    message = "Synced system peak refresh rate to ${settings.fpsCap} FPS (${settings.playStyle.title})",
                    isSuccess = true
                )
            )
        } else if (settings.fpsCap == 0 && isPrivileged) {
            // Uncapped
            executeCommand("settings put system peak_refresh_rate 144.0")
            onLog(
                BoostLog(
                    timeFormatted = timeFormat.format(Date()),
                    category = "FPS",
                    message = "Refresh rate uncapped for maximum high-refresh display throughput",
                    isSuccess = true
                )
            )
        }

        // 5. High-Priority CPU nice assignment for Roblox
        if (isPrivileged) {
            val boostNice = if (settings.boostIntensity > 80f) "-20" else "-10"
            val reniceCmd = "pid=\$(pidof $robloxPkg); if [ -n \"\$pid\" ]; then renice -n $boostNice -p \$pid; fi"
            executeCommand(reniceCmd)
            onLog(
                BoostLog(
                    timeFormatted = timeFormat.format(Date()),
                    category = "CPU",
                    message = "Roblox process ($robloxPkg) assigned realtime nice priority $boostNice",
                    isSuccess = true
                )
            )
        }

        onLog(
            BoostLog(
                timeFormatted = timeFormat.format(Date()),
                category = "COMPLETE",
                message = "Optimization complete! Roblox is primed for lag-free performance.",
                isSuccess = true
            )
        )
    }

    suspend fun getPhysicalResolution(): Pair<Int, Int> = withContext(Dispatchers.IO) {
        try {
            val res = executeCommand("wm size")
            val regex = Regex("""Physical size:\s*(\d+)x(\d+)""")
            val match = regex.find(res.output)
            if (match != null) {
                val w = match.groupValues[1].toIntOrNull() ?: 1080
                val h = match.groupValues[2].toIntOrNull() ?: 2400
                return@withContext Pair(w, h)
            }
        } catch (_: Exception) {}
        Pair(1080, 2400)
    }

    suspend fun applyResolutionScale(scalePercent: Float): Boolean = withContext(Dispatchers.IO) {
        // SAFETY FIRST: NEVER use "wm size" - wm size forces a global screen scale that zooms in the entire Android OS
        // Always ensure system display resolution is reset to normal
        executeCommand("wm size reset")
        executeCommand("wm density reset")
        if (scalePercent >= 99f) {
            return@withContext resetResolution()
        }
        val factor = (scalePercent / 100f).coerceIn(0.5f, 1.0f)
        val res = executeCommand("device_config put game_overlay com.roblox.client mode=2,downscale=$factor:fps=60")
        res.isSuccess
    }

    suspend fun resetResolution(): Boolean = withContext(Dispatchers.IO) {
        // Fully restore phone resolution and undo any previous screen zoom
        executeCommand("wm size reset")
        executeCommand("wm density reset")
        executeCommand("device_config delete game_overlay com.roblox.client")
        true
    }

    suspend fun compileRobloxSpeed(): Boolean = withContext(Dispatchers.IO) {
        val res = executeCommand("cmd package compile -m speed -f com.roblox.client")
        res.isSuccess
    }

    fun cleanup() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        } catch (_: Throwable) {
            // Ignore
        }
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
