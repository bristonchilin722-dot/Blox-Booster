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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DeviceSpecs
import com.example.data.model.SystemTelemetry
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DeviceInfoDialog(
    specs: DeviceSpecs,
    telemetry: SystemTelemetry,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .testTag("device_info_dialog"),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Device & Hardware Specs",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Hardware telemetry & Android 15 status",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_device_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Poco C71 / Xiaomi Compatibility Banner
                if (specs.isPocoC71OrXiaomi) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonPurple.copy(alpha = 0.12f))
                            .border(1.dp, NeonPurple.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⚡",
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Poco / Xiaomi HyperOS Optimization",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = NeonPurple,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• In Developer Options, enable 'USB debugging (Security settings)' so Shizuku can modify display refresh rates & game modes.\n• Set Blox Booster Battery Saver to 'No restrictions' to keep floating FPS active.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Specs Grid
                SpecItem(
                    icon = Icons.Default.PhoneAndroid,
                    label = "Model & OEM",
                    value = "${specs.manufacturer} ${specs.model}",
                    accentColor = NeonCyan
                )

                SpecItem(
                    icon = Icons.Default.Info,
                    label = "OS Version",
                    value = "Android ${specs.androidVersion} (API ${specs.sdkInt})",
                    accentColor = NeonGreen
                )

                SpecItem(
                    icon = Icons.Default.Memory,
                    label = "Physical RAM",
                    value = "${specs.usedRamMb} MB used / ${specs.totalRamMb} MB total (${specs.freeRamMb} MB free)",
                    caption = "Blox Booster uses legitimate kernel cache trimming. No fake virtual RAM tricks.",
                    accentColor = NeonAmber
                )

                SpecItem(
                    icon = Icons.Default.Speed,
                    label = "CPU & Cores",
                    value = "${specs.cpuCores} Cores • ${specs.cpuArch}",
                    accentColor = NeonCyan
                )

                SpecItem(
                    icon = Icons.Default.Speed,
                    label = "Display & Panel",
                    value = "${specs.currentRefreshRate} Hz (Supported: ${specs.supportedRefreshRates.joinToString("Hz, ")}Hz)",
                    accentColor = NeonPurple
                )

                SpecItem(
                    icon = Icons.Default.Thermostat,
                    label = "Battery & Thermals",
                    value = "${telemetry.batteryTemp}°C • ${telemetry.batteryLevel}% (${telemetry.thermalState})",
                    caption = "Source: Battery Thermal Sensor via Android BatteryManager",
                    accentColor = if (telemetry.isThermalElevated) Color(0xFFEF4444) else NeonGreen
                )

                SpecItem(
                    icon = Icons.Default.Memory,
                    label = "Internal Storage",
                    value = "${String.format("%.1f", specs.freeStorageGb)} GB free / ${String.format("%.1f", specs.totalStorageGb)} GB total",
                    accentColor = NeonCyan
                )

                SpecItem(
                    icon = Icons.Default.Info,
                    label = "Graphics Pipeline",
                    value = specs.openGlVersion,
                    accentColor = NeonPurple
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("dismiss_device_info_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceCardLight
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Close",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecItem(
    icon: ImageVector,
    label: String,
    value: String,
    caption: String? = null,
    accentColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = SurfaceCardLight.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                )
                if (caption != null) {
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}
