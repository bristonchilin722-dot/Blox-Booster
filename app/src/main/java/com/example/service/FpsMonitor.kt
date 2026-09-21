package com.example.service

import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import com.example.data.model.FpsSource
import com.example.data.model.FpsTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

object FpsMonitor {
    private var choreographerFrameCount = 0
    private var lastTimeNanos = 0L

    private val _fpsTelemetry = MutableStateFlow(
        FpsTelemetry(
            currentFps = 60,
            frameTimeMs = 16.6f,
            source = FpsSource.DISPLAY_COMPOSITOR,
            isRobloxActive = false,
            jankyFramesPercent = 0f
        )
    )
    val fpsTelemetry: StateFlow<FpsTelemetry> = _fpsTelemetry.asStateFlow()

    private val _fps = MutableStateFlow(60)
    val fps: StateFlow<Int> = _fps.asStateFlow()

    private val _frameTimeMs = MutableStateFlow(16.6f)
    val frameTimeMs: StateFlow<Float> = _frameTimeMs.asStateFlow()

    private var isRunning = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var gfxJob: Job? = null
    private val monitorScope = CoroutineScope(Dispatchers.IO)

    // Roblox package target
    var targetRobloxPackage: String = "com.roblox.client"

    // Shizuku command executor provider (injected by ShizukuManager)
    var shizukuCommandExecutor: (suspend (String) -> CommandResult)? = null

    private var lastGfxFrames = 0L
    private var lastGfxTimestamp = 0L

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isRunning) return

            if (lastTimeNanos == 0L) {
                lastTimeNanos = frameTimeNanos
            } else {
                choreographerFrameCount++
                val elapsedNanos = frameTimeNanos - lastTimeNanos
                if (elapsedNanos >= 600_000_000L) {
                    val calculatedFps = ((choreographerFrameCount * 1_000_000_000.0) / elapsedNanos).roundToInt().coerceIn(1, 240)
                    val frameTime = if (calculatedFps > 0) 1000f / calculatedFps else 16.6f

                    // If gfxinfo is not currently driving telemetry, update from Choreographer
                    if (_fpsTelemetry.value.source != FpsSource.ROBLOX_SURFACE_GFXINFO) {
                        _fps.value = calculatedFps
                        _frameTimeMs.value = frameTime
                        _fpsTelemetry.value = _fpsTelemetry.value.copy(
                            currentFps = calculatedFps,
                            frameTimeMs = frameTime,
                            source = FpsSource.DISPLAY_COMPOSITOR
                        )
                    }

                    choreographerFrameCount = 0
                    lastTimeNanos = frameTimeNanos
                }
            }

            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    fun start() {
        mainHandler.post {
            if (isRunning) return@post
            isRunning = true
            choreographerFrameCount = 0
            lastTimeNanos = 0L
            Choreographer.getInstance().postFrameCallback(frameCallback)
            startGfxSampling()
        }
    }

    private fun startGfxSampling() {
        gfxJob?.cancel()
        gfxJob = monitorScope.launch {
            while (isActive && isRunning) {
                val executor = shizukuCommandExecutor
                if (executor != null) {
                    try {
                        val result = executor("dumpsys gfxinfo $targetRobloxPackage")
                        if (result.isSuccess && result.output.contains("Total frames rendered")) {
                            val framesRegex = Regex("""Total frames rendered:\s*(\d+)""")
                            val jankRegex = Regex("""Janky frames:\s*(\d+)""")

                            val framesMatch = framesRegex.find(result.output)
                            val jankMatch = jankRegex.find(result.output)

                            val totalFrames = framesMatch?.groupValues?.get(1)?.toLongOrNull() ?: 0L
                            val jankyFrames = jankMatch?.groupValues?.get(1)?.toLongOrNull() ?: 0L

                            val now = System.currentTimeMillis()
                            if (lastGfxFrames > 0L && totalFrames >= lastGfxFrames) {
                                val deltaFrames = totalFrames - lastGfxFrames
                                val elapsedSec = (now - lastGfxTimestamp) / 1000.0
                                if (elapsedSec > 0.5) {
                                    val measuredFps = (deltaFrames / elapsedSec).roundToInt().coerceIn(0, 144)
                                    val jankPercent = if (deltaFrames > 0) {
                                        ((jankyFrames.toFloat() / deltaFrames.toFloat()) * 100f).coerceIn(0f, 100f)
                                    } else 0f

                                    val finalFps = if (measuredFps > 0) measuredFps else _fps.value
                                    val frameTime = if (finalFps > 0) 1000f / finalFps else 16.6f

                                    _fps.value = finalFps
                                    _frameTimeMs.value = frameTime
                                    _fpsTelemetry.value = FpsTelemetry(
                                        currentFps = finalFps,
                                        frameTimeMs = frameTime,
                                        source = FpsSource.ROBLOX_SURFACE_GFXINFO,
                                        isRobloxActive = true,
                                        jankyFramesPercent = jankPercent
                                    )
                                }
                            }
                            lastGfxFrames = totalFrames
                            lastGfxTimestamp = now
                        } else {
                            // Roblox not actively rendering or dumpsys unavailable
                            if (_fpsTelemetry.value.source == FpsSource.ROBLOX_SURFACE_GFXINFO) {
                                _fpsTelemetry.value = _fpsTelemetry.value.copy(
                                    source = FpsSource.DISPLAY_COMPOSITOR,
                                    isRobloxActive = false
                                )
                            }
                        }
                    } catch (_: Exception) {
                        // Handled
                    }
                }
                delay(1000L)
            }
        }
    }

    fun stop() {
        mainHandler.post {
            isRunning = false
            Choreographer.getInstance().removeFrameCallback(frameCallback)
            gfxJob?.cancel()
            _fpsTelemetry.value = _fpsTelemetry.value.copy(
                source = FpsSource.PAUSED,
                isRobloxActive = false
            )
        }
    }
}
