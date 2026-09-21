package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ShizukuStatus
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ShizukuSetupDialog(
    status: ShizukuStatus,
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit,
    onRefreshStatus: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BackgroundDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .testTag("shizuku_setup_dialog")
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Shizuku Privileges",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current Status Banner
                val bannerColor = if (status.isServiceRunning && status.isPermissionGranted) NeonGreen
                else if (status.isServiceRunning) NeonAmber else NeonAmber

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .border(1.dp, bannerColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "CURRENT STATUS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = bannerColor
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = status.statusMessage,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Why Shizuku?",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Shizuku grants Blox Booster elevated Android ADB permissions to kill background tasks, manage phantom processes, lock display refresh rates, and set realtime CPU priority for Roblox without rooting.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Quick 1-Minute Setup:",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                StepRow(number = "1", title = "Install Shizuku", desc = "Get Shizuku from GitHub or Google Play Store")
                StepRow(number = "2", title = "Start via Wireless Debugging", desc = "Enable Developer Options > Wireless Debugging > Pair inside Shizuku app")
                StepRow(number = "3", title = "Grant Blox Booster Permission", desc = "Tap the button below to allow adb execution")

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (status.isServiceRunning && !status.isPermissionGranted) {
                        Button(
                            onClick = onRequestPermission,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("dialog_request_perm_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Grant Shizuku Permission",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SurfaceDark
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { openShizukuAppOrStore(context) },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Open Shizuku",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        Button(
                            onClick = onRefreshStatus,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Refresh",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepRow(number: String, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(SurfaceCard),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    fontSize = 11.sp
                )
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 11.sp
                )
            )
        }
    }
}

private fun openShizukuAppOrStore(context: Context) {
    val pkg = "moe.shizuku.privileged.api"
    val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
    if (launchIntent != null) {
        context.startActivity(launchIntent)
    } else {
        try {
            val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
            storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(storeIntent)
        } catch (_: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/"))
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
        }
    }
}
