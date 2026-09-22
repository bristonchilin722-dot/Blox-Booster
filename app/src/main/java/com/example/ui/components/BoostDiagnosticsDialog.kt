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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BoostDiagnosticReport
import com.example.data.model.DiagnosticSnapshot
import com.example.data.model.OperationStatus
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BoostDiagnosticsDialog(
    report: BoostDiagnosticReport?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp)),
            color = SurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Diagnostics",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Boost Diagnostics",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Verified Before & After Telemetry",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted
                                )
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (report == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No diagnostic report available yet.\nTap 'BOOST ROBLOX' to generate a real audit.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                    }
                } else {
                    // Profile & Timestamp banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "PROFILE APPLIED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextMuted,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Text(
                                    text = report.modeApplied.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Text(
                                text = report.timeFormatted,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = TextSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Side-by-side or Before & After comparison
                    Text(
                        text = "BEFORE & AFTER TELEMETRY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SnapshotComparisonTable(before = report.before, after = report.after)

                    Spacer(modifier = Modifier.height(18.dp))

                    // Operations Audit Breakdown
                    Text(
                        text = "HARDWARE & SYSTEM AUDIT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    report.operations.forEach { op ->
                        OperationResultItem(op)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Transparency Notice
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BackgroundDark)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Integrity Guarantee: Blox Booster does not forge performance percentages or fake success statuses. Unsupported OS/hardware calls are truthfully marked.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Close Diagnostics", color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun SnapshotComparisonTable(
    before: DiagnosticSnapshot,
    after: DiagnosticSnapshot?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Metric", style = MaterialTheme.typography.labelMedium.copy(color = TextMuted, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1.2f))
            Text(text = "Before Boost", style = MaterialTheme.typography.labelMedium.copy(color = TextMuted, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            Text(text = "After Boost", style = MaterialTheme.typography.labelMedium.copy(color = NeonCyan, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
        }

        ComparisonRow(label = "RAM In-Use", before = "${before.usedRamMb} MB (${before.ramPercent}%)", after = after?.let { "${it.usedRamMb} MB (${it.ramPercent}%)" } ?: "...")
        ComparisonRow(label = "RAM Free", before = "${before.freeRamMb} MB", after = after?.let { "${it.freeRamMb} MB" } ?: "...")
        ComparisonRow(label = "Temperature", before = "${before.batteryTemp}°C", after = after?.let { "${it.batteryTemp}°C" } ?: "...")
        ComparisonRow(label = "Refresh Rate", before = "${before.refreshRate} Hz", after = after?.let { "${it.refreshRate} Hz" } ?: "...")
        ComparisonRow(label = "FPS (Measured)", before = "${before.fps} FPS", after = after?.let { "${it.fps} FPS" } ?: "...")
        ComparisonRow(label = "Frame Time", before = "${before.frameTimeMs} ms", after = after?.let { "${it.frameTimeMs} ms" } ?: "...")
        ComparisonRow(label = "Thermal Status", before = before.thermalState, after = after?.thermalState ?: "...")
        ComparisonRow(label = "Roblox State", before = if (before.isRobloxRunning) "Active" else "Idle", after = if (after?.isRobloxRunning == true) "Active" else "Idle")
    }
}

@Composable
private fun ComparisonRow(
    label: String,
    before: String,
    after: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp),
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = before,
            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = after,
            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun OperationResultItem(result: com.example.data.model.BoostOperationResult) {
    val (statusColor, statusIcon, statusLabel) = when (result.status) {
        OperationStatus.SUCCESS -> Triple(NeonGreen, Icons.Default.CheckCircle, "APPLIED")
        OperationStatus.UNSUPPORTED -> Triple(NeonAmber, Icons.Default.Warning, "UNSUPPORTED")
        OperationStatus.FAILED -> Triple(NeonRed, Icons.Default.Error, "FAILED")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = statusLabel,
                    tint = statusColor,
                    modifier = Modifier.size(16.dp)
                )
                Column {
                    Text(
                        text = result.name,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = result.detail,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
