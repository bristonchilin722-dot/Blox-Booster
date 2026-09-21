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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardLight
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
        BoostMode.SHADERS -> NeonPurple
        BoostMode.BALANCED -> NeonCyan
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark.copy(alpha = 0.92f))
            .border(1.5.dp, Brush.horizontalGradient(listOf(fpsColor, modeColor)), RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .clickable { onToggleExpand() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("floating_fps_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Live FPS
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
                text = when (settings.activeMode) {
                    BoostMode.POTATO -> "POTATO"
                    BoostMode.SHADERS -> "SHADERS"
                    BoostMode.BALANCED -> "BALANCED"
                },
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
    }
}

@Composable
fun FloatingSideGameMenu(
    fps: Int,
    frameTimeMs: Float,
    settings: BoostSettings,
    onClose: () -> Unit,
    onModeSelect: (BoostMode) -> Unit,
    onFpsCapSelect: (Int) -> Unit,
    onBoostIntensityChange: (Float) -> Unit,
    onPotatoIntensityChange: (Float) -> Unit,
    onShadersIntensityChange: (Float) -> Unit,
    onInstantBoost: () -> Unit,
    onSpeedCompile: () -> Unit,
    onResetResolution: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = BackgroundDark.copy(alpha = 0.96f),
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
                .padding(14.dp)
        ) {
            // Top Bar with Made by Briston
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
                        text = "Made by Briston • Roblox Tuner",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Menu",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
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
                    // Potato Mode Button
                    ModeButton(
                        title = "POTATO",
                        icon = Icons.Default.Speed,
                        isSelected = settings.activeMode == BoostMode.POTATO,
                        accentColor = NeonAmber,
                        subtitle = "Downscale Res",
                        onClick = { onModeSelect(BoostMode.POTATO) },
                        modifier = Modifier.weight(1f)
                    )

                    // Shaders Mode Button
                    ModeButton(
                        title = "SHADERS",
                        icon = Icons.Default.Palette,
                        isSelected = settings.activeMode == BoostMode.SHADERS,
                        accentColor = NeonPurple,
                        subtitle = "4x MSAA / HDR",
                        onClick = { onModeSelect(BoostMode.SHADERS) },
                        modifier = Modifier.weight(1f)
                    )

                    // Balanced Mode Button
                    ModeButton(
                        title = "BALANCED",
                        icon = Icons.Default.Bolt,
                        isSelected = settings.activeMode == BoostMode.BALANCED,
                        accentColor = NeonCyan,
                        subtitle = "Native 60 FPS",
                        onClick = { onModeSelect(BoostMode.BALANCED) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Granular Scales
                Text(
                    text = "LIVE INTENSITY TUNERS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        fontSize = 10.sp
                    )
                )

                // 1. Boost scale
                SliderTile(
                    title = "⚡ SYSTEM BOOST SCALE",
                    value = settings.boostIntensity,
                    onValueChange = onBoostIntensityChange,
                    accentColor = NeonCyan,
                    detail = "${settings.boostIntensity.toInt()}% aggressive RAM/CPU tuning"
                )

                // 2. Potato Downscale scale
                val potatoResDesc = when {
                    settings.potatoIntensity >= 75f -> "50% Resolution (Ultra Potato / 3x FPS)"
                    settings.potatoIntensity >= 45f -> "65% Resolution (High FPS Boost)"
                    settings.potatoIntensity >= 20f -> "80% Resolution (Mild Lag Reduction)"
                    else -> "Native Resolution"
                }
                SliderTile(
                    title = "🥔 POTATO DOWNSCALE SCALE",
                    value = settings.potatoIntensity,
                    onValueChange = onPotatoIntensityChange,
                    accentColor = NeonAmber,
                    detail = potatoResDesc
                )

                // 3. Shaders vibrance scale
                val shadersDesc = when {
                    settings.shadersIntensity >= 75f -> "Max MSAA + Vivid Dynamic HDR Shaders"
                    settings.shadersIntensity >= 45f -> "4x MSAA + Enhanced Contrast & Bloom"
                    else -> "Subtle 2x FXAA edge smoothing"
                }
                SliderTile(
                    title = "✨ SHADERS VIBRANCE SCALE",
                    value = settings.shadersIntensity,
                    onValueChange = onShadersIntensityChange,
                    accentColor = NeonPurple,
                    detail = shadersDesc
                )

                // FPS Cap Selector
                Text(
                    text = "TARGET FPS CAP",
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
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonCyan else SurfaceCard)
                                .clickable { onFpsCapSelect(cap) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
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

                // Quick ADB Tools
                Text(
                    text = "PRIVILEGED QUICK TOOLS",
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
                    Button(
                        onClick = onSpeedCompile,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⚡ Compile DEX",
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
                            .height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "🖥️ Reset Res",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Big Re-Boost Action
            Button(
                onClick = onInstantBoost,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
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
                    text = "Instant Boost & Free RAM",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = BackgroundDark,
                        fontWeight = FontWeight.Black
                    )
                )
            }
        }
    }
}

@Composable
private fun ModeButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) SurfaceCardLight else SurfaceCard)
            .border(
                1.5.dp,
                if (isSelected) accentColor else BorderSubtle,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) accentColor else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontSize = 10.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isSelected) accentColor else TextMuted,
                    fontSize = 8.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SliderTile(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    accentColor: Color,
    detail: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .padding(10.dp)
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
                        color = TextPrimary,
                        fontSize = 10.sp
                    )
                )
                Text(
                    text = "${value.toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        fontFamily = FontFamily.Monospace
                    )
                )
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
                modifier = Modifier.height(26.dp)
            )

            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 9.sp
                )
            )
        }
    }
}
