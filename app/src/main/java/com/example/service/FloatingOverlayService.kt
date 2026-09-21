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

    private var pillX = 40
    private var pillY = 160

    private lateinit var pillParams: WindowManager.LayoutParams
    private lateinit var fullscreenParams: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_OVERLAY) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

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

        val stopIntent = Intent(this, FloatingOverlayService::class.java).apply {
            action = ACTION_STOP_OVERLAY
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Blox Booster HUD • Made by Briston")
            .setContentText("FPS counter active over Roblox • Tap to open controls")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Close Overlay", stopPendingIntent)
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

        // Pill Layout: ONLY occupies the small HUD size, allowing full touch passthrough everywhere else!
        pillParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = pillX
            y = pillY
        }

        // Fullscreen params: ONLY used when the side tuning drawer is actually open
        fullscreenParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
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

                    if (!expanded) {
                        // In collapsed mode: ComposeView is WRAP_CONTENT, perfectly matching pill size!
                        // Touches anywhere else on the screen pass directly to Roblox & the phone OS!
                        FloatingPillHud(
                            fps = fps,
                            frameTimeMs = frameTimeMs,
                            settings = settings,
                            isExpanded = false,
                            onToggleExpand = { setOverlayExpanded(true) },
                            onCloseCompletely = { stopSelf() },
                            onDrag = { dx, dy ->
                                pillX = (pillX + dx.toInt()).coerceIn(0, 1400)
                                pillY = (pillY + dy.toInt()).coerceIn(30, 2600)
                                pillParams.x = pillX
                                pillParams.y = pillY
                                try {
                                    windowManager?.updateViewLayout(this@apply, pillParams)
                                } catch (_: Throwable) {}
                            }
                        )
                    } else {
                        // Expanded mode: side drawer with backdrop dim
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Backdrop dim when side menu is open
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { setOverlayExpanded(false) }
                            )

                            // Floating Side Game Menu over Roblox
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                                modifier = Modifier.align(Alignment.CenterEnd)
                            ) {
                                FloatingSideGameMenu(
                                    fps = fps,
                                    frameTimeMs = frameTimeMs,
                                    settings = settings,
                                    onClose = { setOverlayExpanded(false) },
                                    onCloseCompletely = { stopSelf() },
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
        }

        overlayView = composeView
        try {
            windowManager?.addView(composeView, pillParams)
        } catch (e: Throwable) {
            // Handled
        }
    }

    private fun setOverlayExpanded(isExpanded: Boolean) {
        isMenuExpanded.value = isExpanded
        val view = overlayView ?: return
        val wm = windowManager ?: return
        try {
            if (isExpanded) {
                wm.updateViewLayout(view, fullscreenParams)
            } else {
                pillParams.x = pillX
                pillParams.y = pillY
                wm.updateViewLayout(view, pillParams)
            }
        } catch (_: Throwable) {}
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
            try {
                windowManager?.removeView(it)
            } catch (_: Throwable) {}
        }
        overlayView = null

        // Restore resolution when exiting overlay
        CoroutineScope(Dispatchers.IO).launch {
            ShizukuManager.getInstance(applicationContext).resetResolution()
        }
    }

    companion object {
        const val ACTION_STOP_OVERLAY = "com.example.service.ACTION_STOP_OVERLAY"
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
