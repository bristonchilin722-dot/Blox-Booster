package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BoostMode
import com.example.data.model.BoostSettings
import com.example.data.model.IntensityLevel
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FloatingPillHud(
    fps: Int,
    frameTimeMs: Float,
    settings: BoostSettings,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCloseCompletely: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val fpsColor = when {
        fps >= 55 -> NeonGreen
        fps >= 30 -> NeonAmber
        else -> NeonRed
    }

    val modeColor = when (settings.activeMode) {
        BoostMode.POTATO -> NeonAmber
        BoostMode.BALANCED -> NeonCyan
        BoostMode.PERFORMANCE -> NeonPurple
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark.copy(alpha = 0.95f))
            .border(1.5.dp, Brush.horizontalGradient(listOf(fpsColor, modeColor)), RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .testTag("floating_fps_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Live FPS & Pill click area to expand
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(fpsColor)
                )

                Text(
                    text = "$fps FPS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = fpsColor,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                )

                Text(
                    text = "|",
                    color = BorderSubtle,
                    fontSize = 11.sp
                )

                Text(
                    text = settings.activeMode.title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = modeColor,
                        fontSize = 10.sp
                    )
                )

                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Expand Tuning Menu",
                    tint = NeonCyan,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Quick Close Completely button directly on floating pill
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(NeonRed.copy(alpha = 0.2f))
                    .clickable { onCloseCompletely() }
                    .testTag("pill_close_overlay_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Overlay Completely",
                    tint = NeonRed,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun FloatingSideGameMenu(
    fps: Int,
    frameTimeMs: Float,
    settings: BoostSettings,
    onClose: () -> Unit,
    onCloseCompletely: () -> Unit,
    onModeSelect: (BoostMode) -> Unit,
    onFpsCapSelect: (Int) -> Unit,
    onPotatoIntensityChange: (IntensityLevel) -> Unit,
    onPerformanceIntensityChange: (IntensityLevel) -> Unit,
    onInstantBoost: () -> Unit,
    onSpeedCompile: () -> Unit,
    onResetResolution: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = BackgroundDark.copy(alpha = 0.98f),
        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxHeight()
            .widthIn(max = 380.dp)
            .testTag("floating_side_menu_roblox")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Top Bar with Made by Briston & Close Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "BLOX OVERLAY",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "⚡",
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "Roblox Live Optimizer",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Minimize Menu",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Button(
                        onClick = onCloseCompletely,
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("top_close_overlay_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed.copy(alpha = 0.25f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = NeonRed,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Close HUD",
                            color = NeonRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live HUD Card: Big FPS + Frame Latency
            val fpsColor = when {
                fps >= 55 -> NeonGreen
                fps >= 30 -> NeonAmber
                else -> NeonRed
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceCard)
                    .border(1.dp, fpsColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LIVE FRAME RATE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$fps",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = fpsColor,
                                    fontSize = 26.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "FPS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = fpsColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "FRAME LATENCY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "%.1f ms".format(frameTimeMs),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Game Mode Selector
                Text(
                    text = "SELECT RENDERING PROFILE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        fontSize = 10.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ModeButton(
                        title = "POTATO",
                        icon = Icons.Default.Speed,
                        isSelected = settings.activeMode == BoostMode.POTATO,
                        accentColor = NeonAmber,
                        subtitle = "Fill-Rate Cut",
                        onClick = { onModeSelect(BoostMode.POTATO) },
                        modifier = Modifier.weight(1f)
                    )

                    ModeButton(
                        title = "BALANCED",
                        icon = Icons.Default.Bolt,
                        isSelected = settings.activeMode == BoostMode.BALANCED,
                        accentColor = NeonCyan,
                        subtitle = "Native 60Hz",
                        onClick = { onModeSelect(BoostMode.BALANCED) },
                        modifier = Modifier.weight(1f)
                    )

                    ModeButton(
                        title = "PERFORMANCE",
                        icon = Icons.Default.Tune,
                        isSelected = settings.activeMode == BoostMode.PERFORMANCE,
                        accentColor = NeonPurple,
                        subtitle = "CPU Boost",
                        onClick = { onModeSelect(BoostMode.PERFORMANCE) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Discrete Intensity Tuners
                Text(
                    text = "LIVE INTENSITY LEVEL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        fontSize = 10.sp
                    )
                )

                // 1. Potato Mode Level
                IntensityChipSelector(
                    title = "🥔 POTATO RENDER SCALE",
                    currentLevel = settings.potatoIntensity,
                    onSelect = onPotatoIntensityChange,
                    accentColor = NeonAmber
                )

                // 2. Performance Mode Level
                IntensityChipSelector(
                    title = "🎮 PERFORMANCE CPU PRIORITY",
                    currentLevel = settings.performanceIntensity,
                    onSelect = onPerformanceIntensityChange,
                    accentColor = NeonPurple
                )

                // Target FPS Cap Selector
                Text(
                    text = "TARGET DISPLAY FPS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        fontSize = 10.sp
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(30, 45, 60, 90, 120, 0).forEach { cap ->
                        val isSelected = settings.fpsCap == cap
                        val label = if (cap == 0) "MAX" else "$cap"

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) NeonCyan else SurfaceCard)
                                .clickable { onFpsCapSelect(cap) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) BackgroundDark else TextPrimary
                                )
                            )
                        }
                    }
                }

                // Action Tools
                Text(
                    text = "SYSTEM ENGINE ACTIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        fontSize = 10.sp
                    )
                )

                Button(
                    onClick = onInstantBoost,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = BackgroundDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Instant Re-Apply Tweak",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = BackgroundDark,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onSpeedCompile,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Compile DEX",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Button(
                        onClick = onResetResolution,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = NeonAmber,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset Res",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) SurfaceCard else SurfaceDark)
            .border(
                1.5.dp,
                if (isSelected) accentColor else BorderSubtle,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) accentColor else TextMuted,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) accentColor else TextPrimary,
                    fontSize = 10.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontSize = 8.sp
                )
            )
        }
    }
}

@Composable
private fun IntensityChipSelector(
    title: String,
    currentLevel: IntensityLevel,
    onSelect: (IntensityLevel) -> Unit,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontSize = 10.sp
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IntensityLevel.values().forEach { level ->
                val isSelected = currentLevel == level
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) accentColor else SurfaceDark)
                        .clickable { onSelect(level) }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = level.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) BackgroundDark else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    )
                }
            }
        }
    }
}
