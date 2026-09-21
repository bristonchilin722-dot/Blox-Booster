package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShizukuStatus
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HeaderBar(
    shizukuStatus: ShizukuStatus,
    onShizukuClick: () -> Unit,
    onOpenSideMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo and Title
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(NeonCyan, NeonPurple)
                        )
                    )
                    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚡",
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BLOX",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "BOOSTER",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = 1.sp
                        )
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Made by Briston",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = " • Shizuku Optimizer",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Actions: Shizuku Pill + Side Menu Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Shizuku Status Chip
            val (statusColor, statusIcon, statusLabel) = when {
                shizukuStatus.isServiceRunning && shizukuStatus.isPermissionGranted ->
                    Triple(NeonGreen, Icons.Default.CheckCircle, "Shizuku ADB")
                shizukuStatus.isServiceRunning ->
                    Triple(NeonAmber, Icons.Default.Warning, "Grant Perm")
                else ->
                    Triple(NeonAmber, Icons.Default.Warning, "Shizuku Setup")
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable { onShizukuClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("shizuku_status_chip"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )
            }

            // Open Side Menu Button
            IconButton(
                onClick = onOpenSideMenu,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCard)
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .testTag("header_side_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Open Tuning Menu",
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
