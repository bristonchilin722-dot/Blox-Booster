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
        description = "Prioritizes maximum frame rate and stability on budget hardware. Lowers render target resolution scale via Android Game Overlay (0.50x–0.85x), eliminates window and transition animation overhead, and switches Android Game Mode to Performance."
    ),
    BALANCED(
        title = "Balanced Mode",
        subtitle = "Native 1:1 render scale with stable 60 FPS pacing & cool thermals",
        badge = "STABLE 60",
        description = "Optimal balance between visual fidelity and battery longevity. Uses 100% native render resolution, standard 60 FPS display pacing, mild 0.5x responsive animations, and safe background process trimming."
    ),
    PERFORMANCE(
        title = "Performance Mode",
        subtitle = "Full native resolution with elevated CPU priority & Game Mode",
        badge = "MAX SMOOTH",
        description = "Maintains 100% native resolution while maximizing CPU thread scheduling priority (renice -10/-20), activating Android Game Mode Performance, and locking the display refresh rate to eliminate frame drops without visual degradation."
    )
}

enum class IntensityLevel(
    val title: String,
    val badge: String,
    val description: String,
    val downscaleFactor: Float, // For Potato Mode render scaling
    val nicePriority: Int       // For Performance Mode CPU priority
) {
    LOW(
        title = "Low",
        badge = "MILD",
        description = "Light optimization. 0.85x resolution in Potato Mode; standard CPU priority in Performance Mode.",
        downscaleFactor = 0.85f,
        nicePriority = 0
    ),
    MEDIUM(
        title = "Medium",
        badge = "RECOMMENDED",
        description = "Balanced optimization. 0.70x resolution in Potato Mode; elevated nice -10 CPU priority in Performance Mode.",
        downscaleFactor = 0.70f,
        nicePriority = -10
    ),
    HIGH(
        title = "High",
        badge = "AGGRESSIVE",
        description = "Maximum aggressive tuning. 0.50x resolution scale in Potato Mode; high nice -20 CPU priority & AOT speed compilation.",
        downscaleFactor = 0.50f,
        nicePriority = -20
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
        recommendedMode = BoostMode.PERFORMANCE
    ),
    BATTERY_SAVER(
        title = "Battery Saver",
        description = "Extended session, cool battery temperature",
        recommendedFps = 45,
        recommendedMode = BoostMode.BALANCED
    ),
    CUSTOM(
        title = "Custom Tuner",
        description = "Fine-tuned manual hardware control",
        recommendedFps = 60,
        recommendedMode = BoostMode.PERFORMANCE
    )
}

enum class FpsSource(
    val displayName: String,
    val labelBadge: String,
    val isDirectMeasurement: Boolean
) {
    ROBLOX_SURFACE_GFXINFO("Roblox Render Surface (Shizuku gfxinfo)", "DIRECT MEASUREMENT", true),
    DISPLAY_COMPOSITOR("Display VSync Compositor (Choreographer)", "SYSTEM ESTIMATE", false),
    UNAVAILABLE("Roblox Inactive / Not Measurable", "UNAVAILABLE", false)
}

data class FpsTelemetry(
    val currentFps: Int = 60,
    val averageFps: Int = 60,
    val minFps: Int = 60,
    val frameTimeMs: Float = 16.6f,
    val source: FpsSource = FpsSource.DISPLAY_COMPOSITOR,
    val isRobloxActive: Boolean = false,
    val isRobloxRunning: Boolean = false,
    val jankyFramesPercent: Float = 0f,
    val fpsStabilityPercent: Float = 100f,
    val displayRefreshRate: Int = 60,
    val targetFps: Int = 60
)

data class BoostSettings(
    val activeMode: BoostMode = BoostMode.BALANCED,
    val playStyle: PlayStyle = PlayStyle.COMPETITIVE_PVP,
    val fpsCap: Int = 60,
    val potatoIntensity: IntensityLevel = IntensityLevel.MEDIUM,
    val performanceIntensity: IntensityLevel = IntensityLevel.MEDIUM,
    val killBackgroundApps: Boolean = true,
    val dropRamCaches: Boolean = true,
    val lockMaxRefreshRate: Boolean = true,
    val enableThermalProtection: Boolean = true
)

enum class ThermalState(val displayName: String, val isElevated: Boolean, val isThrottled: Boolean) {
    NORMAL("Normal", false, false),
    WARM("Warm", true, false),
    HIGH("High", true, true),
    THERMALLY_LIMITED("Thermally Limited", true, true)
}

data class ThermalTelemetry(
    val batteryTempCelsius: Float = 33.5f,
    val batteryLevel: Int = 85,
    val thermalState: ThermalState = ThermalState.NORMAL,
    val thermalHeadroom: Float = 0.95f, // 0.0 to 1.0 (1.0 = full headroom, 0.0 = severe throttling)
    val sensorSource: String = "Battery Thermal Sensor (BatteryManager)",
    val apiSource: String = "PowerManager Thermal API",
    val isThrottlingAlert: Boolean = false
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
    val isLowMemory: Boolean = false,
    val refreshRate: Int = 60,
    val batteryTemp: Float = 33.5f,
    val batteryLevel: Int = 85,
    val cpuCores: Int = 8,
    val isRobloxInstalled: Boolean = true,
    val isRobloxRunning: Boolean = false,
    val robloxPackageName: String? = "com.roblox.client",
    val thermalState: ThermalState = ThermalState.NORMAL,
    val thermalHeadroom: Float = 0.95f,
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

enum class OperationStatus {
    SUCCESS,
    UNSUPPORTED,
    FAILED
}

data class BoostOperationResult(
    val name: String,
    val detail: String,
    val status: OperationStatus
)

data class DiagnosticSnapshot(
    val timeFormatted: String,
    val cpuArch: String,
    val cpuCores: Int,
    val totalRamMb: Long,
    val usedRamMb: Long,
    val freeRamMb: Long,
    val ramPercent: Int,
    val batteryTemp: Float,
    val refreshRate: Int,
    val fps: Int,
    val frameTimeMs: Float,
    val thermalState: String,
    val isRobloxRunning: Boolean
)

data class BoostDiagnosticReport(
    val id: Long = System.currentTimeMillis(),
    val timeFormatted: String,
    val modeApplied: BoostMode,
    val before: DiagnosticSnapshot,
    val after: DiagnosticSnapshot?,
    val operations: List<BoostOperationResult>
)

enum class CheckStatus {
    PASS,
    LIMITED,
    UNSUPPORTED,
    FAIL
}

data class SystemCheckItem(
    val category: String,
    val name: String,
    val status: CheckStatus,
    val details: String
)

data class PerformanceSession(
    val id: Long = System.currentTimeMillis(),
    val timestampFormatted: String,
    val mode: BoostMode,
    val averageFps: Int,
    val minFps: Int,
    val averageFrameTimeMs: Float,
    val batteryTempPeak: Float,
    val durationSeconds: Int,
    val measurementSource: String
)

data class BoostLog(
    val id: Long = System.currentTimeMillis(),
    val timeFormatted: String,
    val message: String,
    val category: String,
    val isSuccess: Boolean = true
)
