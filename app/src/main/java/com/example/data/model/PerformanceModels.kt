package com.example.data.model

enum class BoostMode(val title: String, val subtitle: String, val badge: String) {
    POTATO(
        title = "Potato Mode",
        subtitle = "Ultra low textures & zero post-fx for max FPS",
        badge = "MAX FPS"
    ),
    BALANCED(
        title = "Balanced Mode",
        subtitle = "Optimal balance between smooth frames & visuals",
        badge = "STABLE 60"
    ),
    SHADERS(
        title = "Shaders & Graphics",
        subtitle = "Vibrant lighting, 4x MSAA & enhanced contrast",
        badge = "HIGH FIDELITY"
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
        recommendedFps = 120,
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
        recommendedFps = 90,
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

data class BoostSettings(
    val activeMode: BoostMode = BoostMode.BALANCED,
    val playStyle: PlayStyle = PlayStyle.COMPETITIVE_PVP,
    val fpsCap: Int = 120,
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

data class SystemTelemetry(
    val totalRamMb: Long = 6144,
    val usedRamMb: Long = 4200,
    val freeRamMb: Long = 1944,
    val ramPercentage: Int = 68,
    val refreshRate: Int = 120,
    val batteryTemp: Float = 33.5f,
    val batteryLevel: Int = 85,
    val cpuCores: Int = 8,
    val isRobloxInstalled: Boolean = true,
    val robloxPackageName: String? = "com.roblox.client"
)

data class ShizukuStatus(
    val isServiceRunning: Boolean = false,
    val isPermissionGranted: Boolean = false,
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
