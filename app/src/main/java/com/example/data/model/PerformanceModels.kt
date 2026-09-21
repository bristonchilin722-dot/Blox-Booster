package com.example.data.model

enum class BoostMode(
    val title: String,
    val subtitle: String,
    val badge: String,
    val description: String
) {
    POTATO(
        title = "Potato Mode",
        subtitle = "Hardware render downscale & zero animations for max FPS",
        badge = "MAX FPS",
        description = "Reduces graphics workload by lowering game overlay render scale, disabling MSAA & window animations, and activating Android Game Mode Performance."
    ),
    BALANCED(
        title = "Balanced Mode",
        subtitle = "Native resolution with stable 60 FPS target & cool thermals",
        badge = "STABLE 60",
        description = "Optimal balance between visual fidelity and battery efficiency. Uses 100% native render resolution, 60 FPS display pacing, and mild background process cleanup."
    ),
    SHADERS(
        title = "Graphics & Clarity",
        subtitle = "Hardware 4x MSAA anti-aliasing & SurfaceFlinger GPU composition",
        badge = "HIGH CLARITY",
        description = "Legitimate hardware-level visual enhancement: enables 4x Multi-Sample Anti-Aliasing (MSAA) at driver level and direct SurfaceFlinger GPU composition for sharper polygon edges."
    )
}

enum class PlayStyle(
    val title: String,
    val description: String,
    val recommendedFps: Int,
    val recommendedMode: BoostMode
) {
    COMPETITIVE_PVP(
        title = "PvP & Combat",
        description = "Blox Fruits, Arsenal, BedWars (Low latency, high FPS)",
        recommendedFps = 60,
        recommendedMode = BoostMode.POTATO
    ),
    OPEN_WORLD_RP(
        title = "Roleplay & Social",
        description = "Brookhaven, Adopt Me (Balanced visuals & smoothness)",
        recommendedFps = 60,
        recommendedMode = BoostMode.BALANCED
    ),
    OBBY_SPEEDRUN(
        title = "Obby & Parkour",
        description = "Tower of Hell, Speedrun 4 (Locked frames, zero input delay)",
        recommendedFps = 60,
        recommendedMode = BoostMode.BALANCED
    ),
    BATTERY_SAVER(
        title = "Battery Saver",
        description = "Extended session, cool battery temperature",
        recommendedFps = 45,
        recommendedMode = BoostMode.POTATO
    ),
    CUSTOM(
        title = "Custom Tuner",
        description = "Fine-tuned manual hardware control",
        recommendedFps = 60,
        recommendedMode = BoostMode.SHADERS
    )
}

enum class FpsSource(val displayName: String, val isDirectMeasurement: Boolean) {
    ROBLOX_SURFACE_GFXINFO("Roblox Render Surface (Shizuku gfxinfo)", true),
    DISPLAY_COMPOSITOR("Display VSync Compositor (Choreographer)", false),
    PAUSED("Roblox Inactive / Overlay Idle", false)
}

data class FpsTelemetry(
    val currentFps: Int = 60,
    val frameTimeMs: Float = 16.6f,
    val source: FpsSource = FpsSource.DISPLAY_COMPOSITOR,
    val isRobloxActive: Boolean = false,
    val jankyFramesPercent: Float = 0f
)

data class BoostSettings(
    val activeMode: BoostMode = BoostMode.BALANCED,
    val playStyle: PlayStyle = PlayStyle.COMPETITIVE_PVP,
    val fpsCap: Int = 60,
    val boostIntensity: Float = 75f,      // 0..100% scale
    val potatoIntensity: Float = 60f,    // 0..100% scale
    val shadersIntensity: Float = 40f,   // 0..100% scale
    val killBackgroundApps: Boolean = true,
    val dropRamCaches: Boolean = true,
    val forceGpuComposition: Boolean = true,
    val disableAnimations: Boolean = true,
    val limitPhantomProcesses: Boolean = true,
    val lockMaxRefreshRate: Boolean = true
)

data class ThermalTelemetry(
    val batteryTempCelsius: Float = 33.5f,
    val batteryLevel: Int = 85,
    val thermalState: String = "Normal",
    val isElevated: Boolean = false,
    val sensorSource: String = "Battery Thermal Sensor (BatteryManager)"
)

data class DeviceSpecs(
    val model: String = "Poco C71",
    val manufacturer: String = "Xiaomi",
    val androidVersion: String = "15",
    val sdkInt: Int = 35,
    val totalRamMb: Long = 6144,
    val usedRamMb: Long = 3900,
    val freeRamMb: Long = 2244,
    val ramPercentage: Int = 63,
    val totalStorageGb: Float = 128f,
    val freeStorageGb: Float = 74f,
    val cpuArch: String = "arm64-v8a",
    val cpuCores: Int = 8,
    val supportedRefreshRates: List<Int> = listOf(60, 90),
    val currentRefreshRate: Int = 60,
    val openGlVersion: String = "OpenGL ES 3.2",
    val isPocoC71OrXiaomi: Boolean = true
)

data class SystemTelemetry(
    val totalRamMb: Long = 6144,
    val usedRamMb: Long = 4200,
    val freeRamMb: Long = 1944,
    val ramPercentage: Int = 68,
    val refreshRate: Int = 60,
    val batteryTemp: Float = 33.5f,
    val batteryLevel: Int = 85,
    val cpuCores: Int = 8,
    val isRobloxInstalled: Boolean = true,
    val robloxPackageName: String? = "com.roblox.client",
    val thermalState: String = "Normal",
    val isThermalElevated: Boolean = false
)

data class ShizukuStatus(
    val isInstalled: Boolean = false,
    val isServiceRunning: Boolean = false,
    val isPermissionGranted: Boolean = false,
    val hasRootFallback: Boolean = false,
    val version: Int = 0,
    val uid: Int = -1,
    val statusMessage: String = "Checking Shizuku status..."
)

data class BoostLog(
    val id: Long = System.currentTimeMillis(),
    val timeFormatted: String,
    val message: String,
    val category: String,
    val isSuccess: Boolean = true
)
