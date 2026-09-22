package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FpsTelemetry
import com.example.data.model.ShizukuStatus
import com.example.data.model.SystemTelemetry
import com.example.data.model.ThermalState
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
fun TelemetryOverview(
    telemetry: SystemTelemetry,
    fpsTelemetry: FpsTelemetry,
    shizukuStatus: ShizukuStatus,
    targetFps: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top 2 Cards: Live FPS & RAM
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Live FPS Card
            val fpsColor = when {
                fpsTelemetry.currentFps >= 55 -> NeonGreen
                fpsTelemetry.currentFps >= 30 -> NeonAmber
                else -> NeonRed
            }

            val fpsProgress = (fpsTelemetry.currentFps / 120f).coerceIn(0f, 1f)

            TelemetryTile(
                modifier = Modifier
                    .weight(1f)
                    .testTag("telemetry_fps_card"),
                title = "FPS (${fpsTelemetry.source.labelBadge})",
                value = "${fpsTelemetry.currentFps} FPS",
                subtext = "Target: ${if (targetFps == 0) "MAX" else "$targetFps"} • ${fpsTelemetry.frameTimeMs.toInt()}ms",
                caption = "${telemetry.refreshRate} Hz Display Pacing",
                icon = Icons.Default.Speed,
                accentColor = fpsColor,
                progress = fpsProgress
            )

            // RAM Card
            TelemetryTile(
                modifier = Modifier
                    .weight(1f)
                    .testTag("telemetry_ram_card"),
                title = "SYSTEM RAM (${telemetry.ramPercentage}%)",
                value = "${telemetry.usedRamMb} MB",
                subtext = "${telemetry.freeRamMb} MB Available",
                caption = if (telemetry.isLowMemory) "Memory Pressure Alert" else "Healthy Allocation",
                icon = Icons.Default.Memory,
                accentColor = if (telemetry.ramPercentage > 85) NeonRed else if (telemetry.ramPercentage > 70) NeonAmber else NeonCyan,
                progress = telemetry.ramPercentage / 100f
            )
        }

        // Bottom 2 Cards: Thermals & Roblox Process
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Thermals Card
            val (tempColor, thermalLabel) = when (telemetry.thermalState) {
                ThermalState.NORMAL -> Pair(NeonGreen, "Normal (${telemetry.batteryLevel}%)")
                ThermalState.WARM -> Pair(NeonAmber, "Warm (${telemetry.batteryLevel}%)")
                ThermalState.HIGH -> Pair(NeonAmber, "High Temp (${telemetry.batteryLevel}%)")
                ThermalState.THERMALLY_LIMITED -> Pair(NeonRed, "Throttled (${telemetry.batteryLevel}%)")
            }

            TelemetryTile(
                modifier = Modifier
                    .weight(1f)
                    .testTag("telemetry_thermal_card"),
                title = "THERMAL STATE",
                value = "%.1f °C".format(telemetry.batteryTemp),
                subtext = thermalLabel,
                caption = "Battery Sensor (BatteryManager)",
                icon = Icons.Default.Thermostat,
                accentColor = tempColor,
                progress = (telemetry.batteryTemp / 50f).coerceIn(0f, 1f)
            )

            // Roblox Process Status
            val (robloxColor, robloxStatusText) = when {
                telemetry.isRobloxRunning -> Pair(NeonGreen, "Active & Running")
                telemetry.isRobloxInstalled -> Pair(NeonCyan, "Installed (Idle)")
                else -> Pair(NeonAmber, "Not Installed")
            }

            TelemetryTile(
                modifier = Modifier
                    .weight(1f)
                    .testTag("telemetry_roblox_card"),
                title = "ROBLOX STATUS",
                value = robloxStatusText,
                subtext = telemetry.robloxPackageName ?: "com.roblox.client",
                caption = if (telemetry.isRobloxRunning) "60 FPS Engine Process" else "Ready for launch",
                icon = Icons.Default.VideogameAsset,
                accentColor = robloxColor,
                progress = if (telemetry.isRobloxRunning) 1f else 0.5f
            )
        }
    }
}

@Composable
fun TelemetryTile(
    title: String,
    value: String,
    subtext: String,
    caption: String,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "telemetry_progress"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(14.dp)
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
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        fontSize = 10.sp
                    )
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    fontSize = 16.sp
                )
            )

            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = accentColor,
                trackColor = SurfaceDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontSize = 9.sp
                )
            )
        }
    }
}
