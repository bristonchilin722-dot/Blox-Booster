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
import com.example.data.model.SystemTelemetry
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
    targetFps: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top 2 Cards: RAM & FPS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // RAM Card
            TelemetryTile(
                modifier = Modifier
                    .weight(1f)
                    .testTag("telemetry_ram_card"),
                title = "RAM USAGE",
                value = "${telemetry.usedRamMb} MB",
                subtext = "${telemetry.freeRamMb} MB Free",
                caption = "Kernel Memory Compaction",
                icon = Icons.Default.Memory,
                accentColor = if (telemetry.ramPercentage > 80) NeonAmber else NeonCyan,
                progress = telemetry.ramPercentage / 100f
            )

            // FPS & Refresh Rate Card
            val fpsDisplay = if (targetFps == 0) "Uncapped" else "$targetFps FPS"
            TelemetryTile(
                modifier = Modifier
                    .weight(1f)
                    .testTag("telemetry_fps_card"),
                title = "FRAME TARGET",
                value = fpsDisplay,
                subtext = "${telemetry.refreshRate} Hz Display",
                caption = "SurfaceFlinger VSync",
                icon = Icons.Default.Speed,
                accentColor = NeonPurple,
                progress = if (targetFps == 0) 1f else (targetFps / 144f).coerceIn(0f, 1f)
            )
        }

        // Bottom 2 Cards: Thermals & Roblox App
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Thermals Card
            val tempColor = when {
                telemetry.batteryTemp >= 46f -> NeonRed
                telemetry.batteryTemp >= 42f -> NeonAmber
                else -> NeonGreen
            }
            val thermalSubtitle = when {
                telemetry.batteryTemp >= 46f -> "Throttling Alert"
                telemetry.batteryTemp >= 42f -> "Elevated Temp"
                else -> "Optimal (${telemetry.batteryLevel}%)"
            }

            TelemetryTile(
                modifier = Modifier
                    .weight(1f)
                    .testTag("telemetry_thermal_card"),
                title = "THERMALS",
                value = "%.1f °C".format(telemetry.batteryTemp),
                subtext = thermalSubtitle,
                caption = "Battery Sensor (BatteryManager)",
                icon = Icons.Default.Thermostat,
                accentColor = tempColor,
                progress = (telemetry.batteryTemp / 50f).coerceIn(0f, 1f)
            )

            // Roblox Status Card
            TelemetryTile(
                modifier = Modifier
                    .weight(1f)
                    .testTag("telemetry_roblox_card"),
                title = "ROBLOX ENGINE",
                value = if (telemetry.isRobloxInstalled) "Detected" else "Ready",
                subtext = telemetry.robloxPackageName ?: "com.roblox.client",
                caption = "60 FPS Native Client Cap",
                icon = Icons.Default.VideogameAsset,
                accentColor = if (telemetry.isRobloxInstalled) NeonGreen else NeonCyan,
                progress = 1f
            )
        }
    }
}

@Composable
private fun TelemetryTile(
    title: String,
    value: String,
    subtext: String,
    caption: String? = null,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "telemetry_progress")

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(12.dp)
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
                        letterSpacing = 0.5.sp
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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )

            if (caption != null) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 9.sp
                    ),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = accentColor,
                trackColor = SurfaceDark,
            )
        }
    }
}
