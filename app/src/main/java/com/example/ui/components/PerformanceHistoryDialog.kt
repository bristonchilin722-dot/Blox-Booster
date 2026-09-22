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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PerformanceSession
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

@Composable
fun PerformanceHistoryDialog(
    sessions: List<PerformanceSession>,
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
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Performance History",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Recorded Roblox Gameplay Sessions",
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

                if (sessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recorded sessions yet.\nBoost Roblox or play a session with the Floating HUD active to record real frame benchmarks.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        sessions.forEachIndexed { index, session ->
                            SessionHistoryCard(index = index + 1, session = session)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Honesty Note
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BackgroundDark)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Data Integrity: Only measurements legitimately gathered during active gameplay are logged. Missing hardware sensors are never manufactured.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Close History", color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun SessionHistoryCard(index: Int, session: PerformanceSession) {
    val modeColor = when (session.mode) {
        com.example.data.model.BoostMode.POTATO -> NeonAmber
        com.example.data.model.BoostMode.BALANCED -> NeonCyan
        com.example.data.model.BoostMode.PERFORMANCE -> NeonPurple
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Session $index",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(modeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = session.mode.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = modeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
                Text(
                    text = session.timestampFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "AVERAGE FPS", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
                    Text(
                        text = "${session.averageFps} FPS",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (session.averageFps >= 55) NeonGreen else NeonAmber,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                Column {
                    Text(text = "LOWEST FPS", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
                    Text(
                        text = "${session.minFps} FPS",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                Column {
                    Text(text = "FRAME TIME", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
                    Text(
                        text = "${session.averageFrameTimeMs} ms",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                Column {
                    Text(text = "PEAK TEMP", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
                    Text(
                        text = "${session.batteryTempPeak}°C",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (session.batteryTempPeak >= 42f) NeonAmber else NeonCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }

            // Source info
            Text(
                text = "Source: ${session.measurementSource}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontSize = 10.sp
                )
            )
        }
    }
}
