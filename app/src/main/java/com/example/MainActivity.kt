package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.FloatingOverlayService
import com.example.ui.components.BoostCenterCard
import com.example.ui.components.DeviceInfoDialog
import com.example.ui.components.HeaderBar
import com.example.ui.components.LogTerminalCard
import com.example.ui.components.OverlayLauncherCard
import com.example.ui.components.RestoreSettingsDialog
import com.example.ui.components.ShizukuSetupDialog
import com.example.ui.components.SideTuningMenu
import com.example.ui.components.TelemetryOverview
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.BoosterViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BoosterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                BoosterApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkShizukuStatus()
    }
}

@Composable
fun BoosterApp(
    viewModel: BoosterViewModel
) {
    val context = LocalContext.current
    val telemetry by viewModel.telemetry.collectAsState()
    val deviceSpecs by viewModel.deviceSpecs.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val shizukuStatus by viewModel.shizukuStatus.collectAsState()
    val isBoosting by viewModel.isBoosting.collectAsState()
    val boostProgress by viewModel.boostProgress.collectAsState()
    val boostStepText by viewModel.boostStepText.collectAsState()
    val isSideMenuOpen by viewModel.isSideMenuOpen.collectAsState()
    val logs by viewModel.boostLogs.collectAsState()
    val showShizukuHelp by viewModel.showShizukuHelp.collectAsState()
    val showDeviceInfo by viewModel.showDeviceInfo.collectAsState()
    val showRestoreDialog by viewModel.showRestoreDialog.collectAsState()
    val isOverlayActive by viewModel.isOverlayActive.collectAsState()
    val liveFps by viewModel.liveFps.collectAsState()
    val frameTimeMs by viewModel.frameTimeMs.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
        ) {
            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header Bar with Balaclava Sketch & Status Chips
                HeaderBar(
                    shizukuStatus = shizukuStatus,
                    onShizukuClick = { viewModel.setShowShizukuHelp(true) },
                    onDeviceInfoClick = { viewModel.setShowDeviceInfo(true) },
                    onResetDefaultsClick = { viewModel.setShowRestoreDialog(true) },
                    onOpenSideMenu = { viewModel.toggleSideMenu(true) }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Telemetry Overview: RAM, FPS, Thermals, Roblox
                TelemetryOverview(
                    telemetry = telemetry,
                    targetFps = settings.fpsCap
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Center Boost Action Card
                BoostCenterCard(
                    settings = settings,
                    isBoosting = isBoosting,
                    boostProgress = boostProgress,
                    boostStepText = boostStepText,
                    onBoostClick = { viewModel.boostRoblox() },
                    onLaunchRoblox = { viewModel.launchRobloxWithOverlay(context) },
                    onOpenSideMenu = { viewModel.toggleSideMenu(true) },
                    onResetCurrentMode = {
                        when (settings.activeMode) {
                            com.example.data.model.BoostMode.POTATO -> viewModel.resetPotatoSettings()
                            com.example.data.model.BoostMode.BALANCED -> viewModel.resetBalancedSettings()
                            com.example.data.model.BoostMode.SHADERS -> viewModel.resetShadersSettings()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // In-Game Floating HUD & Menu Over Roblox Card
                OverlayLauncherCard(
                    isOverlayActive = isOverlayActive,
                    liveFps = liveFps,
                    frameTimeMs = frameTimeMs,
                    onToggleOverlay = { enabled ->
                        viewModel.toggleOverlay(context, enabled)
                    },
                    onLaunchRobloxWithOverlay = {
                        viewModel.launchRobloxWithOverlay(context)
                    },
                    onCompileSpeed = {
                        viewModel.compileRobloxSpeed(context)
                    },
                    onResetResolution = {
                        viewModel.resetResolution(context)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Live ADB / Shizuku Optimization Log Terminal
                LogTerminalCard(
                    logs = logs,
                    onClearLogs = { viewModel.clearLogs() }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Close App Completely Button (User Request)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Button(
                        onClick = {
                            FloatingOverlayService.stop(context)
                            viewModel.resetResolution(context)
                            (context as? Activity)?.finishAffinity()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("close_app_completely_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceDark
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = NeonRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Close Blox Booster Completely",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = NeonRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Made by Briston Credit
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceCard)
                            .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("made_by_briston_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "⚡",
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Made by Briston",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = "• Roblox Performance Optimizer",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }

            // Side Peek Tab (pull out the side tuner from the right edge anytime)
            if (!isSideMenuOpen) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(NeonCyan, NeonPurple)
                            )
                        )
                        .clickable { viewModel.toggleSideMenu(true) }
                        .padding(horizontal = 6.dp, vertical = 14.dp)
                        .testTag("side_peek_handle"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Open Side Menu",
                            tint = BackgroundDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "T\nU\nN\nE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp,
                                color = BackgroundDark,
                                lineHeight = 10.sp
                            )
                        )
                    }
                }
            }

            // Side Tuning Drawer
            SideTuningMenu(
                isOpen = isSideMenuOpen,
                settings = settings,
                onClose = { viewModel.toggleSideMenu(false) },
                onModeSelect = { viewModel.setMode(it) },
                onPlayStyleSelect = { viewModel.setPlayStyle(it) },
                onFpsCapSelect = { viewModel.setFpsCap(it) },
                onBoostIntensityChange = { viewModel.updateBoostIntensity(it) },
                onPotatoIntensityChange = { viewModel.updatePotatoIntensity(it) },
                onShadersIntensityChange = { viewModel.updateShadersIntensity(it) },
                onResetBoostIntensity = { viewModel.updateBoostIntensity(50f) },
                onResetPotatoIntensity = { viewModel.resetPotatoSettings() },
                onResetShadersIntensity = { viewModel.resetShadersSettings() },
                onReboost = {
                    viewModel.toggleSideMenu(false)
                    viewModel.boostRoblox()
                },
                onLaunchRoblox = { viewModel.launchRoblox(context) },
                modifier = Modifier.align(Alignment.CenterEnd)
            )

            // Shizuku Setup Help Dialog
            if (showShizukuHelp) {
                ShizukuSetupDialog(
                    status = shizukuStatus,
                    onDismiss = { viewModel.setShowShizukuHelp(false) },
                    onRequestPermission = { viewModel.requestShizukuPermission() },
                    onRefreshStatus = { viewModel.checkShizukuStatus() }
                )
            }

            // Device Info Hardware Dialog
            if (showDeviceInfo) {
                DeviceInfoDialog(
                    specs = deviceSpecs,
                    telemetry = telemetry,
                    onDismiss = { viewModel.setShowDeviceInfo(false) }
                )
            }

            // Restore All Defaults Dialog
            if (showRestoreDialog) {
                RestoreSettingsDialog(
                    onConfirmRestore = {
                        viewModel.restoreAllDefaults(context)
                        viewModel.setShowRestoreDialog(false)
                    },
                    onDismiss = { viewModel.setShowRestoreDialog(false) }
                )
            }
        }
    }
}
