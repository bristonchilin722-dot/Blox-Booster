package com.example.service

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.view.Display
import com.example.data.model.DeviceSpecs
import com.example.data.model.SystemTelemetry
import com.example.data.model.ThermalState
import com.example.data.model.ThermalTelemetry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SystemMonitor(private val context: Context) {

    private val robloxPackages = listOf(
        "com.roblox.client",
        "com.roblox.client.amazon",
        "com.roblox.client.samsung"
    )

    fun isRobloxRunning(pkg: String): Boolean {
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val processes = am?.runningAppProcesses ?: return false
            processes.any { it.processName.equals(pkg, ignoreCase = true) }
        } catch (_: Exception) {
            false
        }
    }

    fun getThermalTelemetry(): ThermalTelemetry {
        var batteryTemp = 32.0f
        var batteryLevel = 80
        try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, intentFilter)
            batteryStatus?.let {
                val tempRaw = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 320)
                batteryTemp = tempRaw / 10.0f
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    batteryLevel = ((level.toFloat() / scale.toFloat()) * 100).toInt()
                }
            }
        } catch (_: Exception) {}

        var headroom = 0.95f
        var thermalState = ThermalState.NORMAL

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                pm?.let {
                    val status = it.currentThermalStatus
                    thermalState = when (status) {
                        PowerManager.THERMAL_STATUS_NONE,
                        PowerManager.THERMAL_STATUS_LIGHT -> {
                            if (batteryTemp >= 42f) ThermalState.HIGH
                            else if (batteryTemp >= 38f) ThermalState.WARM
                            else ThermalState.NORMAL
                        }
                        PowerManager.THERMAL_STATUS_MODERATE -> ThermalState.WARM
                        PowerManager.THERMAL_STATUS_SEVERE -> ThermalState.HIGH
                        PowerManager.THERMAL_STATUS_CRITICAL,
                        PowerManager.THERMAL_STATUS_EMERGENCY,
                        PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalState.THERMALLY_LIMITED
                        else -> ThermalState.NORMAL
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val hr = it.getThermalHeadroom(30)
                        if (hr >= 0f) {
                            headroom = (1.0f - (hr - 1.0f).coerceAtLeast(0f)).coerceIn(0.0f, 1.0f)
                        }
                    }
                }
            } catch (_: Exception) {}
        } else {
            thermalState = when {
                batteryTemp >= 46f -> ThermalState.THERMALLY_LIMITED
                batteryTemp >= 42f -> ThermalState.HIGH
                batteryTemp >= 38f -> ThermalState.WARM
                else -> ThermalState.NORMAL
            }
        }

        return ThermalTelemetry(
            batteryTempCelsius = batteryTemp,
            batteryLevel = batteryLevel,
            thermalState = thermalState,
            thermalHeadroom = headroom,
            sensorSource = "Battery Thermal Sensor (BatteryManager)",
            apiSource = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "PowerManager Thermal API (Android ${Build.VERSION.RELEASE})" else "Legacy Thermal Estimator",
            isThrottlingAlert = thermalState == ThermalState.HIGH || thermalState == ThermalState.THERMALLY_LIMITED
        )
    }

    fun getTelemetry(): SystemTelemetry {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(memInfo)

        val totalRamMb = (memInfo.totalMem / (1024 * 1024)).coerceAtLeast(1024)
        val freeRamMb = (memInfo.availMem / (1024 * 1024)).coerceAtLeast(128)
        val usedRamMb = (totalRamMb - freeRamMb).coerceAtLeast(0)
        val ramPercent = ((usedRamMb.toDouble() / totalRamMb.toDouble()) * 100).toInt().coerceIn(0, 100)
        val isLowMemory = memInfo.lowMemory

        val thermal = getThermalTelemetry()

        // Display refresh rate
        var refreshRate = 60
        try {
            val dm = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            val display = dm?.getDisplay(Display.DEFAULT_DISPLAY)
            display?.let {
                refreshRate = it.mode.refreshRate.toInt().coerceAtLeast(30)
            }
        } catch (_: Exception) {}

        // Check if Roblox is installed
        var isRobloxInstalled = false
        var detectedPkg: String? = null
        val pm = context.packageManager

        for (pkg in robloxPackages) {
            try {
                pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES)
                isRobloxInstalled = true
                detectedPkg = pkg
                break
            } catch (_: PackageManager.NameNotFoundException) {
                // Continue
            }
        }

        if (detectedPkg == null) {
            detectedPkg = "com.roblox.client"
        }

        val isRunning = isRobloxInstalled && isRobloxRunning(detectedPkg)

        return SystemTelemetry(
            totalRamMb = totalRamMb,
            usedRamMb = usedRamMb,
            freeRamMb = freeRamMb,
            ramPercentage = ramPercent,
            isLowMemory = isLowMemory,
            refreshRate = refreshRate,
            batteryTemp = thermal.batteryTempCelsius,
            batteryLevel = thermal.batteryLevel,
            cpuCores = Runtime.getRuntime().availableProcessors(),
            isRobloxInstalled = isRobloxInstalled,
            isRobloxRunning = isRunning,
            robloxPackageName = detectedPkg,
            thermalState = thermal.thermalState,
            thermalHeadroom = thermal.thermalHeadroom,
            isThermalElevated = thermal.isThrottlingAlert
        )
    }

    fun getDeviceSpecs(): DeviceSpecs {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(memInfo)

        val totalRamMb = (memInfo.totalMem / (1024 * 1024)).coerceAtLeast(1024)
        val freeRamMb = (memInfo.availMem / (1024 * 1024)).coerceAtLeast(128)
        val usedRamMb = (totalRamMb - freeRamMb).coerceAtLeast(0)
        val ramPercent = ((usedRamMb.toDouble() / totalRamMb.toDouble()) * 100).toInt().coerceIn(0, 100)

        // Storage
        var totalStorageGb = 128f
        var freeStorageGb = 64f
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            totalStorageGb = (totalBlocks * blockSize / (1024f * 1024f * 1024f))
            freeStorageGb = (availableBlocks * blockSize / (1024f * 1024f * 1024f))
        } catch (_: Exception) {}

        // Supported refresh rates
        val supportedRates = mutableListOf<Int>()
        var currentRate = 60
        try {
            val dm = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            val display = dm?.getDisplay(Display.DEFAULT_DISPLAY)
            display?.let { d ->
                currentRate = d.mode.refreshRate.toInt()
                d.supportedModes.forEach { mode ->
                    val rate = mode.refreshRate.toInt()
                    if (rate !in supportedRates) {
                        supportedRates.add(rate)
                    }
                }
            }
        } catch (_: Exception) {}
        if (supportedRates.isEmpty()) {
            supportedRates.addAll(listOf(60, 90))
        }
        supportedRates.sort()

        // OpenGL ES version
        var glesVersion = "OpenGL ES 3.2"
        try {
            val configInfo = am?.deviceConfigurationInfo
            if (configInfo != null) {
                glesVersion = "OpenGL ES ${configInfo.glEsVersion}"
            }
        } catch (_: Exception) {}

        val isXiaomiOrPoco = Build.MANUFACTURER.contains("xiaomi", ignoreCase = true) ||
                Build.MODEL.contains("poco", ignoreCase = true) ||
                Build.MODEL.contains("2404ARN45A", ignoreCase = true) ||
                Build.DEVICE.contains("poco", ignoreCase = true)

        val cpuArch = if (Build.SUPPORTED_ABIS.isNotEmpty()) Build.SUPPORTED_ABIS[0] else "arm64-v8a"

        return DeviceSpecs(
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            androidVersion = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT,
            totalRamMb = totalRamMb,
            usedRamMb = usedRamMb,
            freeRamMb = freeRamMb,
            ramPercentage = ramPercent,
            totalStorageGb = totalStorageGb,
            freeStorageGb = freeStorageGb,
            cpuArch = cpuArch,
            cpuCores = Runtime.getRuntime().availableProcessors(),
            supportedRefreshRates = supportedRates,
            currentRefreshRate = currentRate,
            openGlVersion = glesVersion,
            isPocoC71OrXiaomi = isXiaomiOrPoco
        )
    }

    fun pollTelemetry(intervalMs: Long = 3000L): Flow<SystemTelemetry> = flow {
        while (true) {
            emit(getTelemetry())
            delay(intervalMs)
        }
    }
}
