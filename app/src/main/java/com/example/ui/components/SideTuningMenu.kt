package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BoostMode
import com.example.data.model.BoostSettings
import com.example.data.model.IntensityLevel
import com.example.data.model.PlayStyle
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SideTuningMenu(
    isOpen: Boolean,
    settings: BoostSettings,
    onClose: () -> Unit,
    onModeSelect: (BoostMode) -> Unit,
    onPlayStyleSelect: (PlayStyle) -> Unit,
    onFpsCapSelect: (Int) -> Unit,
    onPotatoIntensityChange: (IntensityLevel) -> Unit,
    onPerformanceIntensityChange: (IntensityLevel) -> Unit,
    onResetPotatoIntensity: () -> Unit,
    onResetPerformanceIntensity: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenSystemCheck: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenVisualQualityInfo: () -> Unit,
    onOpenDeviceInfo: () -> Unit,
    onOpenRestoreDefaults: () -> Unit,
    onReboost: () -> Unit,
    onLaunchRoblox: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onClose() }
        )
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.92f)
                .widthIn(max = 430.dp)
                .background(BackgroundDark)
                .border(1.dp, NeonCyan.copy(alpha = 0.3f))
                .testTag("side_tuning_drawer")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ROBLOX TUNING",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Verified Real Hardware Profiles",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("close_side_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Menu",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Mode Selector: Potato vs Balanced vs Performance
                    SectionCard(title = "OPTIMIZATION PROFILE", subtitle = "Legitimate hardware-level profiles") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            BoostMode.values().forEach { mode ->
                                val isSelected = settings.activeMode == mode
                                val accentColor = when (mode) {
                                    BoostMode.POTATO -> NeonAmber
                                    BoostMode.BALANCED -> NeonCyan
                                    BoostMode.PERFORMANCE -> NeonPurple
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) SurfaceCardLight else SurfaceCard)
                                        .border(
                                            1.5.dp,
                                            if (isSelected) accentColor else BorderSubtle,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onModeSelect(mode) }
                                        .padding(12.dp)
                                        .testTag("mode_${mode.name.lowercase()}"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(accentColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (mode) {
                                                BoostMode.POTATO -> Icons.Default.Speed
                                                BoostMode.BALANCED -> Icons.Default.Bolt
                                                BoostMode.PERFORMANCE -> Icons.Default.Tune
                                            },
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = mode.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                            )
                                            Text(
                                                text = mode.badge,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = accentColor,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp
                                                ),
                                                modifier = Modifier
                                                    .background(
                                                        accentColor.copy(alpha = 0.15f),
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = mode.subtitle,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Discrete Intensity Controls
                    SectionCard(title = "PROFILE INTENSITY LEVELS", subtitle = "Concrete, verified parameters") {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Potato Mode Intensity Levels
                            DiscreteIntensitySelector(
                                title = "🥔 POTATO MODE RENDER SCALE",
                                currentLevel = settings.potatoIntensity,
                                onSelect = onPotatoIntensityChange,
                                onReset = onResetPotatoIntensity,
                                accentColor = NeonAmber,
                                lowDesc = "0.85x render scale (Light fill-rate reduction)",
                                medDesc = "0.70x render scale (~50% fill-rate reduction)",
                                highDesc = "0.50x render scale (75% reduction for low-end GPUs)"
                            )

                            // Performance Mode Intensity Levels
                            DiscreteIntensitySelector(
                                title = "🎮 PERFORMANCE CPU PRIORITY",
                                currentLevel = settings.performanceIntensity,
                                onSelect = onPerformanceIntensityChange,
                                onReset = onResetPerformanceIntensity,
                                accentColor = NeonPurple,
                                lowDesc = "Standard scheduling priority & 100% native resolution",
                                medDesc = "Elevated CPU priority (nice -10) for Roblox main thread",
                                highDesc = "Aggressive CPU priority (nice -20) & AOT speed compilation"
                            )
                        }
                    }

                    // 3. Play Styles & 60 FPS Target
                    SectionCard(title = "PLAY STYLES & 60 FPS TARGET", subtitle = "Align display refresh to Roblox engine") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PlayStyle.values().forEach { style ->
                                    val selected = settings.playStyle == style
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onPlayStyleSelect(style) },
                                        label = {
                                            Text(
                                                text = style.title,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                                            selectedLabelColor = NeonCyan,
                                            containerColor = SurfaceCard,
                                            labelColor = TextSecondary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = selected,
                                            borderColor = if (selected) NeonCyan else BorderSubtle
                                        ),
                                        modifier = Modifier.testTag("play_style_${style.name.lowercase()}")
                                    )
                                }
                            }

                            Text(
                                text = "Selected: ${settings.playStyle.description}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "Display Refresh Rate / FPS Target:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )

                            val fpsOptions = listOf(30, 45, 60, 90, 120, 0)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                fpsOptions.forEach { fps ->
                                    val isSelected = settings.fpsCap == fps
                                    val label = if (fps == 0) "UNCAPPED" else "$fps FPS"

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) NeonCyan else SurfaceCard)
                                            .clickable { onFpsCapSelect(fps) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .testTag("fps_chip_$fps"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) BackgroundDark else TextPrimary
                                            )
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Technical Note: Roblox mobile engine enforces an internal 60 FPS tick limit. Locking Android display refresh to 60Hz eliminates judder and micro-stutter.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // 4. Quick Tools & Diagnostics
                    SectionCard(title = "DIAGNOSTICS & SYSTEM TOOLS", subtitle = "Inspect & verify device capabilities") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ToolButton(
                                icon = Icons.Default.Assessment,
                                title = "Boost Diagnostics",
                                subtitle = "Before & after telemetry report",
                                accent = NeonCyan,
                                onClick = onOpenDiagnostics
                            )

                            ToolButton(
                                icon = Icons.Default.Science,
                                title = "System Capability Check",
                                subtitle = "Audit Shizuku, APIs & Poco C71 support",
                                accent = NeonGreen,
                                onClick = onOpenSystemCheck
                            )

                            ToolButton(
                                icon = Icons.Default.History,
                                title = "Performance History",
                                subtitle = "Compare past Roblox sessions",
                                accent = NeonAmber,
                                onClick = onOpenHistory
                            )

                            ToolButton(
                                icon = Icons.Default.Palette,
                                title = "Visual Quality Guide",
                                subtitle = "In-game graphics & anti-cheat transparency",
                                accent = NeonPurple,
                                onClick = onOpenVisualQualityInfo
                            )

                            ToolButton(
                                icon = Icons.Default.Info,
                                title = "Device Specs",
                                subtitle = "Inspect hardware specs and thermals",
                                accent = TextSecondary,
                                onClick = onOpenDeviceInfo
                            )

                            ToolButton(
                                icon = Icons.Default.Refresh,
                                title = "Restore Settings",
                                subtitle = "Revert changes made by Blox Booster",
                                accent = NeonRed,
                                onClick = onOpenRestoreDefaults
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onReboost,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("reboost_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = BackgroundDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Apply Tweaks",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = BackgroundDark,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Button(
                        onClick = onLaunchRoblox,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("drawer_launch_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardLight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Play Roblox",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscreteIntensitySelector(
    title: String,
    currentLevel: IntensityLevel,
    onSelect: (IntensityLevel) -> Unit,
    onReset: () -> Unit,
    accentColor: Color,
    lowDesc: String,
    medDesc: String,
    highDesc: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    letterSpacing = 0.5.sp
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onReset() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Level",
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "RESET",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Discrete Level Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IntensityLevel.values().forEach { level ->
                val isSelected = currentLevel == level
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) accentColor else SurfaceCard)
                        .clickable { onSelect(level) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${level.title} (${level.badge})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) BackgroundDark else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }

        val activeDesc = when (currentLevel) {
            IntensityLevel.LOW -> lowDesc
            IntensityLevel.MEDIUM -> medDesc
            IntensityLevel.HIGH -> highDesc
        }

        Text(
            text = activeDesc,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextMuted,
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        )
    }
}

@Composable
private fun ToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = TextPrimary
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                )
            }
            content()
        }
    }
}
