package com.example.service

import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

object FpsMonitor {
    private var frameCount = 0
    private var lastTimeNanos = 0L
    private val _fps = MutableStateFlow(60)
    val fps: StateFlow<Int> = _fps.asStateFlow()

    private val _frameTimeMs = MutableStateFlow(16.6f)
    val frameTimeMs: StateFlow<Float> = _frameTimeMs.asStateFlow()

    private var isRunning = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isRunning) return

            if (lastTimeNanos == 0L) {
                lastTimeNanos = frameTimeNanos
            } else {
                frameCount++
                val elapsedNanos = frameTimeNanos - lastTimeNanos
                if (elapsedNanos >= 600_000_000L) { // update every ~600ms for smooth live response
                    val calculatedFps = ((frameCount * 1_000_000_000.0) / elapsedNanos).roundToInt()
                    _fps.value = calculatedFps.coerceIn(1, 240)
                    _frameTimeMs.value = if (calculatedFps > 0) 1000f / calculatedFps else 16.6f
                    frameCount = 0
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
            frameCount = 0
            lastTimeNanos = 0L
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
    }

    fun stop() {
        mainHandler.post {
            isRunning = false
            Choreographer.getInstance().removeFrameCallback(frameCallback)
        }
    }
}
