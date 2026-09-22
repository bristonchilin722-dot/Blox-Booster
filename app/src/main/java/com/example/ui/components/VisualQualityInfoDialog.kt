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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VisualQualityInfoDialog(
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
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Visual Quality",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Visual Quality Guide",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Roblox Android Graphics Architecture",
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

                Spacer(modifier = Modifier.height(14.dp))

                // Security & Anti-Cheat transparency banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .border(1.dp, NeonAmber.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Anti-Cheat",
                            tint = NeonAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Why Blox Booster Doesn't Fake Shaders",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = NeonAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Roblox on Android is protected by Hyperion anti-tamper security. Third-party apps cannot inject external ReShade or custom post-processing shaders into Roblox without modifying game binaries (which risks permanent bans). Blox Booster strictly uses legitimate Android OS and hardware driver controls.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "IN-GAME GRAPHICS QUALITY LEVELS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                InfoCard(
                    title = "Level 1 (Manual) — Recommended for Max FPS",
                    subtitle = "Disables dynamic shadows, reduces particle systems, minimizes draw distance. Combined with Blox Booster Potato Mode, this yields up to +40% FPS on budget hardware.",
                    accent = NeonGreen
                )

                Spacer(modifier = Modifier.height(6.dp))

                InfoCard(
                    title = "Level 2–5 (Manual) — Balanced Visuals",
                    subtitle = "Standard texture resolution, subtle lighting effects, medium draw distance. Optimal for Balanced Mode on the Poco C71.",
                    accent = NeonCyan
                )

                Spacer(modifier = Modifier.height(6.dp))

                InfoCard(
                    title = "Level 6–10 (Manual) — High Fidelity",
                    subtitle = "Dynamic shadow cascades, sun rays, full water reflections, atmospheric bloom. Requires high GPU bandwidth.",
                    accent = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "HOW BLOX BOOSTER ENHANCES VISUAL FIDELITY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        QualityBullet(
                            title = "Stable 60Hz Display Pacing",
                            desc = "Locks display refresh rate to match Roblox's internal 60 FPS tick rate, eliminating frame judder and screen tearing."
                        )
                        QualityBullet(
                            title = "Native 1:1 Sharpness (Performance Mode)",
                            desc = "Preserves 100% pixel sharpness without downscaling, while elevating CPU thread priority."
                        )
                        QualityBullet(
                            title = "Fill-Rate Reduction (Potato Mode)",
                            desc = "Safely scales the game overlay render target to reduce pixel shading workload on budget GPUs."
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
                    Text(text = "Got It", color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, subtitle: String, accent: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            )
        }
    }
}

@Composable
private fun QualityBullet(title: String, desc: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(NeonCyan)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            )
        }
    }
}
