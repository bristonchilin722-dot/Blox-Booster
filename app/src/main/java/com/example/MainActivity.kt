package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.example.ui.components.BoostCenterCard
import com.example.ui.components.HeaderBar
import com.example.ui.components.LogTerminalCard
import com.example.ui.components.ShizukuSetupDialog
import com.example.ui.components.SideTuningMenu
import com.example.ui.components.TelemetryOverview
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
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
    val settings by viewModel.settings.collectAsState()
    val shizukuStatus by viewModel.shizukuStatus.collectAsState()
    val isBoosting by viewModel.isBoosting.collectAsState()
    val boostProgress by viewModel.boostProgress.collectAsState()
    val boostStepText by viewModel.boostStepText.collectAsState()
    val isSideMenuOpen by viewModel.isSideMenuOpen.collectAsState()
    val logs by viewModel.boostLogs.collectAsState()
    val showShizukuHelp by viewModel.showShizukuHelp.collectAsState()

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
                // Top Header Bar
                HeaderBar(
                    shizukuStatus = shizukuStatus,
                    onShizukuClick = { viewModel.setShowShizukuHelp(true) },
                    onOpenSideMenu = { viewModel.toggleSideMenu(true) }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Telemetry Overview: RAM, FPS, Thermals, Roblox
                TelemetryOverview(
                    telemetry = telemetry,
                    targetFps = settings.fpsCap
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Center Boost Action Card
                BoostCenterCard(
                    settings = settings,
                    isBoosting = isBoosting,
                    boostProgress = boostProgress,
                    boostStepText = boostStepText,
                    onBoostClick = { viewModel.boostRoblox() },
                    onLaunchRoblox = { viewModel.launchRoblox(context) },
                    onOpenSideMenu = { viewModel.toggleSideMenu(true) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Live ADB / Shizuku Optimization Log Terminal
                LogTerminalCard(
                    logs = logs,
                    onClearLogs = { viewModel.clearLogs() }
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Side Peek Tab (allows user to easily pull out the side tuner from the right edge anytime)
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

            // The Little Menu on the Side (opens when you press boost or tap side tuner)
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
        }
    }
}
