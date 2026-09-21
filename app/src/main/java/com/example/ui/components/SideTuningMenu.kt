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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
    onBoostIntensityChange: (Float) -> Unit,
    onPotatoIntensityChange: (Float) -> Unit,
    onShadersIntensityChange: (Float) -> Unit,
    onResetBoostIntensity: () -> Unit = {},
    onResetPotatoIntensity: () -> Unit = {},
    onResetShadersIntensity: () -> Unit = {},
    onReboost: () -> Unit,
    onLaunchRoblox: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Backdrop overlay
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

    // Side Drawer Sliding in from End
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
                .widthIn(max = 420.dp)
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
                                text = "Made by Briston • Verified Optimizations",
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Mode Selector: Potato Mode vs Shaders vs Balanced
                    SectionCard(title = "GAME MODE", subtitle = "Choose rendering profile") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            BoostMode.values().forEach { mode ->
                                val isSelected = settings.activeMode == mode
                                val accentColor = when (mode) {
                                    BoostMode.POTATO -> NeonAmber
                                    BoostMode.SHADERS -> NeonPurple
                                    BoostMode.BALANCED -> NeonCyan
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
                                                BoostMode.SHADERS -> Icons.Default.Palette
                                                BoostMode.BALANCED -> Icons.Default.Bolt
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

                    // 2. Play Style & Custom FPS Caps
                    SectionCard(title = "PLAY STYLES & FPS CAP", subtitle = "Match Roblox game genres") {
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
                                            selectedContainerColor = NeonPurple.copy(alpha = 0.3f),
                                            selectedLabelColor = NeonCyan,
                                            containerColor = SurfaceCard,
                                            labelColor = TextSecondary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = selected,
                                            borderColor = if (selected) NeonPurple else BorderSubtle
                                        ),
                                        modifier = Modifier.testTag("play_style_${style.name.lowercase()}")
                                    )
                                }
                            }

                            Text(
                                text = "Selected Style: ${settings.playStyle.description}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "Target Display Refresh & FPS Lock:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )

                            // FPS Caps Row
                            val fpsOptions = listOf(30, 45, 60, 90, 120, 144, 0)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                fpsOptions.forEach { fps ->
                                    val isSelected = settings.fpsCap == fps
                                    val label = if (fps == 0) "∞ MAX" else "$fps FPS"

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
                                text = "Note: Roblox mobile engine caps internally at 60 FPS. Setting 60 FPS locks Android display VSync for zero frame judder.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // 3. The 3 Verified Intensity Scales (Boost, Potato, Shaders)
                    SectionCard(title = "INTENSITY SCALES & RESET", subtitle = "Fine-tune and verify hardware limits") {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Scale 1: System Boost Scale
                            IntensitySliderWithReset(
                                title = "⚡ SYSTEM BOOST SCALE",
                                value = settings.boostIntensity,
                                onValueChange = onBoostIntensityChange,
                                onReset = onResetBoostIntensity,
                                accentColor = NeonCyan,
                                description = getBoostScaleDescription(settings.boostIntensity),
                                testTag = "boost_intensity_slider"
                            )

                            // Scale 2: Potato Mode Scale
                            IntensitySliderWithReset(
                                title = "🥔 POTATO MODE SCALE",
                                value = settings.potatoIntensity,
                                onValueChange = onPotatoIntensityChange,
                                onReset = onResetPotatoIntensity,
                                accentColor = NeonAmber,
                                description = getPotatoScaleDescription(settings.potatoIntensity),
                                testTag = "potato_intensity_slider"
                            )

                            // Scale 3: Graphics & Shaders Scale
                            IntensitySliderWithReset(
                                title = "✨ GRAPHICS & CLARITY SCALE",
                                value = settings.shadersIntensity,
                                onValueChange = onShadersIntensityChange,
                                onReset = onResetShadersIntensity,
                                accentColor = NeonPurple,
                                description = getShadersScaleDescription(settings.shadersIntensity),
                                testTag = "shaders_intensity_slider"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceCardLight.copy(alpha = 0.5f))
                                    .padding(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Roblox engine does not support PC ReShade. Blox Booster applies 4x MSAA anti-aliasing & GPU SurfaceFlinger safely.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons
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
                            .testTag("side_menu_launch_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = SurfaceDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Play Roblox",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = SurfaceDark,
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
private fun SectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = NeonCyan,
                    letterSpacing = 0.8.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 11.sp
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun IntensitySliderWithReset(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onReset: () -> Unit,
    accentColor: Color,
    description: String,
    testTag: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${value.toInt()}%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = accentColor
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SurfaceCardLight)
                        .clickable { onReset() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset slider",
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        )
                    }
                }
            }
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..100f,
            steps = 19,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = SurfaceCardLight
            ),
            modifier = Modifier.testTag(testTag)
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 11.sp
            )
        )
    }
}

private fun getBoostScaleDescription(intensity: Float): String {
    return when {
        intensity < 25f -> "Mild: Memory page cache trim & basic garbage collection"
        intensity < 50f -> "Standard: am kill-all background tasks & max phantom processes: 8"
        intensity < 75f -> "Aggressive: Max phantom processes: 4, CPU nice -10 priority"
        else -> "Extreme Turbo: Max phantom processes: 2, CPU nice -20 priority & AOT speed compilation"
    }
}

private fun getPotatoScaleDescription(intensity: Float): String {
    return when {
        intensity < 34f -> "Level 1: Disables window animations, disables 4x MSAA, Android Game Mode Performance"
        intensity < 67f -> "Level 2: Level 1 + Game Overlay Downscale (0.7x target) & background task kill"
        else -> "Level 3 Ultra Potato: Level 2 + Game Overlay Downscale (0.5x target) + AOT speed compile"
    }
}

private fun getShadersScaleDescription(intensity: Float): String {
    return when {
        intensity < 34f -> "Native Crisp: 1:1 pixel rendering, standard driver compositing"
        intensity < 67f -> "Enhanced Clarity: SurfaceFlinger direct GPU compositing & 2x MSAA"
        else -> "High Fidelity: Force 4x MSAA hardware anti-aliasing + SurfaceFlinger direct GPU pipeline"
    }
}
