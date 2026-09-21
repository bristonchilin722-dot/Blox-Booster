package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.data.model.BoostMode
import com.example.data.model.BoostSettings
import com.example.ui.components.FloatingPillHud
import com.example.ui.components.FloatingSideGameMenu
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class FloatingOverlayService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private val lifecycleOwner = OverlayLifecycleOwner()

    private val settingsFlow = MutableStateFlow(BoostSettings())
    private val isMenuExpanded = MutableStateFlow(false)

    private lateinit var pillParams: WindowManager.LayoutParams
    private lateinit var fullscreenParams: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        lifecycleOwner.onCreate()
        lifecycleOwner.onStart()
        lifecycleOwner.onResume()

        startForegroundNotification()
        FpsMonitor.start()
        setupOverlay()
    }

    private fun startForegroundNotification() {
        val channelId = "blox_booster_hud"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Roblox Performance HUD",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live FPS and game tuning overlay over Roblox"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Blox Booster HUD • Made by Briston")
            .setContentText("FPS counter and tuning menu active over Roblox")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun setupOverlay() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Fullscreen params used when side menu is open or pill is rendered
        fullscreenParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)

            setContent {
                MyApplicationTheme {
                    val fps by FpsMonitor.fps.collectAsState()
                    val frameTimeMs by FpsMonitor.frameTimeMs.collectAsState()
                    val settings by settingsFlow.collectAsState()
                    val expanded by isMenuExpanded.collectAsState()

                    var pillOffsetX by remember { mutableStateOf(40f) }
                    var pillOffsetY by remember { mutableStateOf(160f) }

                    // Root Box
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Fullscreen Shader Visual Filter over Roblox if Shaders mode is active!
                        if (settings.activeMode == BoostMode.SHADERS && settings.shadersIntensity > 15f) {
                            val tintAlpha = (settings.shadersIntensity / 100f * 0.12f).coerceIn(0.02f, 0.15f)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF330066).copy(alpha = tintAlpha))
                            )
                        }

                        // Backdrop dim when side menu is open
                        if (expanded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { isMenuExpanded.value = false }
                            )
                        }

                        // Draggable Floating Pill (only shown when menu is collapsed)
                        if (!expanded) {
                            FloatingPillHud(
                                fps = fps,
                                frameTimeMs = frameTimeMs,
                                settings = settings,
                                isExpanded = expanded,
                                onToggleExpand = { isMenuExpanded.value = true },
                                onDrag = { dx, dy ->
                                    pillOffsetX = (pillOffsetX + dx).coerceIn(10f, 600f)
                                    pillOffsetY = (pillOffsetY + dy).coerceIn(60f, 1800f)
                                },
                                modifier = Modifier.padding(
                                    start = pillOffsetX.dp,
                                    top = pillOffsetY.dp
                                )
                            )
                        }

                        // Floating Side Game Menu over Roblox
                        AnimatedVisibility(
                            visible = expanded,
                            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            FloatingSideGameMenu(
                                fps = fps,
                                frameTimeMs = frameTimeMs,
                                settings = settings,
                                onClose = { isMenuExpanded.value = false },
                                onModeSelect = { mode ->
                                    val updated = settings.copy(activeMode = mode)
                                    settingsFlow.value = updated
                                    applyModeSettings(updated)
                                },
                                onFpsCapSelect = { cap ->
                                    val updated = settings.copy(fpsCap = cap)
                                    settingsFlow.value = updated
                                    applyFpsCap(cap)
                                },
                                onBoostIntensityChange = { boost ->
                                    settingsFlow.value = settings.copy(boostIntensity = boost)
                                },
                                onPotatoIntensityChange = { potato ->
                                    val updated = settings.copy(potatoIntensity = potato)
                                    settingsFlow.value = updated
                                    if (updated.activeMode == BoostMode.POTATO) {
                                        applyPotatoScale(potato)
                                    }
                                },
                                onShadersIntensityChange = { shaders ->
                                    settingsFlow.value = settings.copy(shadersIntensity = shaders)
                                },
                                onInstantBoost = {
                                    serviceScope.launch {
                                        ShizukuManager.getInstance(applicationContext).applyOptimization(
                                            settings = settingsFlow.value
                                        )
                                    }
                                },
                                onSpeedCompile = {
                                    serviceScope.launch {
                                        ShizukuManager.getInstance(applicationContext).compileRobloxSpeed()
                                    }
                                },
                                onResetResolution = {
                                    serviceScope.launch {
                                        ShizukuManager.getInstance(applicationContext).resetResolution()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        overlayView = composeView
        windowManager?.addView(composeView, fullscreenParams)
    }

    private fun applyModeSettings(settings: BoostSettings) {
        serviceScope.launch {
            val sm = ShizukuManager.getInstance(applicationContext)
            when (settings.activeMode) {
                BoostMode.POTATO -> {
                    val scale = settings.potatoIntensity
                    val scalePercent = when {
                        scale >= 75f -> 50f
                        scale >= 45f -> 65f
                        scale >= 20f -> 80f
                        else -> 100f
                    }
                    if (scalePercent < 100f) {
                        sm.applyResolutionScale(scalePercent)
                    } else {
                        sm.resetResolution()
                    }
                    sm.executeCommand("cmd game mode performance com.roblox.client")
                    sm.executeCommand("settings put global window_animation_scale 0")
                    sm.executeCommand("settings put global force_msaa 0")
                }
                BoostMode.SHADERS -> {
                    sm.resetResolution()
                    sm.executeCommand("settings put global force_msaa 1")
                    sm.executeCommand("setprop debug.egl.force_msaa 1")
                    sm.executeCommand("service call SurfaceFlinger 1008 i32 1")
                }
                BoostMode.BALANCED -> {
                    sm.resetResolution()
                    sm.executeCommand("settings put global force_msaa 0")
                    sm.executeCommand("settings put global window_animation_scale 0.5")
                }
            }
        }
    }

    private fun applyPotatoScale(potatoScale: Float) {
        serviceScope.launch {
            val sm = ShizukuManager.getInstance(applicationContext)
            val scalePercent = when {
                potatoScale >= 75f -> 50f
                potatoScale >= 45f -> 65f
                potatoScale >= 20f -> 80f
                else -> 100f
            }
            if (scalePercent < 100f) {
                sm.applyResolutionScale(scalePercent)
            } else {
                sm.resetResolution()
            }
        }
    }

    private fun applyFpsCap(cap: Int) {
        serviceScope.launch {
            val sm = ShizukuManager.getInstance(applicationContext)
            if (cap > 0) {
                sm.executeCommand("settings put system peak_refresh_rate $cap.0")
                sm.executeCommand("settings put system min_refresh_rate $cap.0")
            } else {
                sm.executeCommand("settings put system peak_refresh_rate 144.0")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        FpsMonitor.stop()
        lifecycleOwner.onDestroy()

        overlayView?.let {
            windowManager?.removeView(it)
        }
        overlayView = null

        // Restore resolution when exiting overlay
        CoroutineScope(Dispatchers.IO).launch {
            ShizukuManager.getInstance(applicationContext).resetResolution()
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 8801

        fun start(context: Context) {
            val intent = Intent(context, FloatingOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingOverlayService::class.java)
            context.stopService(intent)
        }
    }
}
