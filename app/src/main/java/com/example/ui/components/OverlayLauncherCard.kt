package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun OverlayLauncherCard(
    isOverlayActive: Boolean,
    liveFps: Int,
    frameTimeMs: Float,
    onToggleOverlay: (Boolean) -> Unit,
    onLaunchRobloxWithOverlay: () -> Unit,
    onCompileSpeed: () -> Unit,
    onResetResolution: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fpsColor = when {
        liveFps >= 55 -> NeonGreen
        liveFps >= 30 -> NeonAmber
        else -> NeonRed
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(
                        NeonCyan.copy(alpha = 0.5f),
                        NeonGreen.copy(alpha = 0.5f)
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
            .testTag("overlay_launcher_card")
    ) {
        Column {
            // Header Row: Title & Overlay Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "IN-GAME ROBLOX OVERLAY",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Made by Briston • Floating HUD & Live FPS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Switch to activate overlay directly
                Switch(
                    checked = isOverlayActive,
                    onCheckedChange = onToggleOverlay,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BackgroundDark,
                        checkedTrackColor = NeonGreen,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SurfaceDark
                    ),
                    modifier = Modifier.testTag("overlay_toggle_switch")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Explanation & Live FPS Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Real-Time Display FPS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Appears over Roblox so you can tweak Potato Mode, Performance Mode & 60 FPS pacing while playing.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Live FPS readout box
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(fpsColor.copy(alpha = 0.15f))
                        .border(1.dp, fpsColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$liveFps FPS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = fpsColor,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "%.1f ms".format(frameTimeMs),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Big Launch Roblox With Overlay Button
            Button(
                onClick = onLaunchRobloxWithOverlay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("launch_roblox_with_overlay_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = BackgroundDark,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Launch Roblox with Floating Tuner",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = BackgroundDark,
                        fontWeight = FontWeight.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Hardware Optimization Tools
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCompileSpeed,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("compile_speed_button"),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(NeonCyan, NeonPurple))
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Compile DEX",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }

                OutlinedButton(
                    onClick = onResetResolution,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("reset_res_button"),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(NeonAmber, NeonCyan))
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = NeonAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Reset Resolution",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}
