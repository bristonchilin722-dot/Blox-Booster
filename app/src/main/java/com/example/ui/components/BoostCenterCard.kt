package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BoostMode
import com.example.data.model.BoostSettings
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
fun BoostCenterCard(
    settings: BoostSettings,
    isBoosting: Boolean,
    boostProgress: Float,
    boostStepText: String,
    onBoostClick: () -> Unit,
    onLaunchRoblox: () -> Unit,
    onOpenSideMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceCard)
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        NeonCyan.copy(alpha = 0.4f),
                        NeonPurple.copy(alpha = 0.1f)
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Active Mode Pill
            val modeColor = when (settings.activeMode) {
                BoostMode.POTATO -> NeonAmber
                BoostMode.SHADERS -> NeonPurple
                BoostMode.BALANCED -> NeonCyan
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCardLight)
                    .clickable { onOpenSideMenu() }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(modeColor)
                )
                Text(
                    text = "${settings.activeMode.title.uppercase()} • ${settings.boostIntensity.toInt()}% BOOST",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = modeColor,
                        letterSpacing = 0.8.sp
                    )
                )
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Giant Boost Button with Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(170.dp)
            ) {
                // Outer glowing pulse ring
                Box(
                    modifier = Modifier
                        .size(165.dp)
                        .scale(if (isBoosting) 1.1f else pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    modeColor.copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Outer border circle
                Box(
                    modifier = Modifier
                        .size(144.dp)
                        .clip(CircleShape)
                        .border(
                            2.dp,
                            Brush.sweepGradient(
                                listOf(NeonCyan, NeonPurple, NeonGreen, NeonCyan)
                            ),
                            CircleShape
                        )
                )

                // Main Core Button
                Box(
                    modifier = Modifier
                        .size(126.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    SurfaceDark,
                                    SurfaceCard
                                )
                            )
                        )
                        .clickable(enabled = !isBoosting) { onBoostClick() }
                        .testTag("boost_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isBoosting) {
                        CircularProgressIndicator(
                            progress = { boostProgress },
                            modifier = Modifier.size(118.dp),
                            color = NeonCyan,
                            strokeWidth = 4.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(boostProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan
                                )
                            )
                            Text(
                                text = "TUNING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(38.dp)
                            )
                            Text(
                                text = "BOOST",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "ROBLOX",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeonPurple,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress or Subtitle Text
            if (isBoosting) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = boostStepText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { boostProgress },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = NeonCyan,
                        trackColor = SurfaceDark
                    )
                }
            } else {
                Text(
                    text = "Tap to optimize background tasks & unlock peak performance",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Launch Roblox Button
                Button(
                    onClick = onLaunchRoblox,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("launch_roblox_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = SurfaceDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Play Roblox",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = SurfaceDark,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Open Side Tuner Menu
                OutlinedButton(
                    onClick = onOpenSideMenu,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("open_side_menu_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NeonCyan
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(NeonCyan, NeonPurple))
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Side Tuner",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
