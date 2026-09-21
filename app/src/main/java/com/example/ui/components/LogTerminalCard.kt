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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BoostLog
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LogTerminalCard(
    logs: List<BoostLog>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("log_terminal_card")
    ) {
        Column {
            // Title & Clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OPTIMIZATION LOGS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                            color = TextPrimary
                        )
                    )
                }

                if (logs.isNotEmpty()) {
                    IconButton(
                        onClick = onClearLogs,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear logs",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Log Console Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 180.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCard)
                    .padding(8.dp)
            ) {
                if (logs.isEmpty()) {
                    Text(
                        text = "Terminal idle. Tap Boost to view live system optimization logs.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs, key = { it.id }) { log ->
                            val tagColor = when (log.category) {
                                "PROCESS" -> NeonCyan
                                "MEMORY" -> NeonGreen
                                "POTATO" -> NeonAmber
                                "SHADERS" -> NeonPurple
                                "CPU" -> NeonCyan
                                else -> TextMuted
                            }

                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = log.timeFormatted,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "[${log.category}]",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = tagColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = log.message,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
