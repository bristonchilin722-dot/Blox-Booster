package com.example.service

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Build
import android.view.Display
import com.example.data.model.SystemTelemetry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SystemMonitor(private val context: Context) {

    private val robloxPackages = listOf(
        "com.roblox.client",
        "com.roblox.client.amazon",
        "com.roblox.client.samsung"
    )

    fun getTelemetry(): SystemTelemetry {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(memInfo)

        val totalRamMb = (memInfo.totalMem / (1024 * 1024)).coerceAtLeast(1024)
        val freeRamMb = (memInfo.availMem / (1024 * 1024)).coerceAtLeast(128)
        val usedRamMb = (totalRamMb - freeRamMb).coerceAtLeast(0)
        val ramPercent = ((usedRamMb.toDouble() / totalRamMb.toDouble()) * 100).toInt().coerceIn(0, 100)

        // Battery thermals
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
        } catch (_: Exception) {
            // Ignore
        }

        // Display refresh rate
        var refreshRate = 60
        try {
            val dm = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            val display = dm?.getDisplay(Display.DEFAULT_DISPLAY)
            display?.let {
                refreshRate = it.mode.refreshRate.toInt().coerceAtLeast(30)
            }
        } catch (_: Exception) {
            // Fallback
        }

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

        // If not found in emulator or device, keep default as target so boost still configures for Roblox
        if (detectedPkg == null) {
            detectedPkg = "com.roblox.client"
        }

        return SystemTelemetry(
            totalRamMb = totalRamMb,
            usedRamMb = usedRamMb,
            freeRamMb = freeRamMb,
            ramPercentage = ramPercent,
            refreshRate = refreshRate,
            batteryTemp = batteryTemp,
            batteryLevel = batteryLevel,
            cpuCores = Runtime.getRuntime().availableProcessors(),
            isRobloxInstalled = isRobloxInstalled,
            robloxPackageName = detectedPkg
        )
    }

    fun pollTelemetry(intervalMs: Long = 3000L): Flow<SystemTelemetry> = flow {
        while (true) {
            emit(getTelemetry())
            delay(intervalMs)
        }
    }
}
