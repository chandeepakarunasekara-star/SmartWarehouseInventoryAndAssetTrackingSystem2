package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data class representing an operational warehouse live camera feed.
 */
data class VideoFeedInfo(
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val tag: String,
    val cameraCode: String,
    val detectedObject: String
)

/**
 * High-performance Warehouse Operations Video Feed Player.
 * Simulates real-time CCTV facility camera feeds with animated optical laser scanning,
 * active scanlines, telemetry HUD, live timecode, and playback controls.
 * Avoids MediaPlayerNative network errors (1, -2147483648) by utilizing native Compose animations.
 */
@Composable
fun WarehouseVideoClipPlayer(
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true
) {
    val videoFeeds = remember {
        listOf(
            VideoFeedInfo(
                title = "Automated Dispatch & Conveyor",
                subtitle = "High-speed optical sortation line #4",
                imageUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=800&q=80",
                tag = "Conveyor Bay",
                cameraCode = "CAM-01 [NORTH-DOCK]",
                detectedObject = "BOX #4920 [INSPECTED]"
            ),
            VideoFeedInfo(
                title = "Robotic AGV Pallet Staging",
                subtitle = "Autonomous forklift transit zone B",
                imageUrl = "https://images.unsplash.com/photo-1587293852726-70cdb56c2866?auto=format&fit=crop&w=800&q=80",
                tag = "AGV Staging",
                cameraCode = "CAM-02 [BAY-TRANSIT]",
                detectedObject = "FORKLIFT-FL01 [ENGAGED]"
            ),
            VideoFeedInfo(
                title = "High-Bay Racks & Stacker",
                subtitle = "Automated storage & retrieval system",
                imageUrl = "https://images.unsplash.com/photo-1553413077-190dd305871c?auto=format&fit=crop&w=800&q=80",
                tag = "High-Bay ASRS",
                cameraCode = "CAM-03 [RACK-STACKER]",
                detectedObject = "PALLET-RACK-04 [OCCUPIED]"
            )
        )
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(autoPlay) }
    var isMuted by remember { mutableStateOf(true) }
    var currentTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val currentFeed = videoFeeds[selectedIndex]

    // Updating live timecode
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(100)
            currentTimestamp = System.currentTimeMillis()
        }
    }

    // Infinite animation for laser scanning reticle and scanlines
    val infiniteTransition = rememberInfiniteTransition(label = "cctv_effects")

    // Laser bar moving up and down
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    // Pulsing recording indicator
    val recAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rec_blink"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("warehouse_video_clip_player")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color(0xFF065F46),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Operations Video Feeds",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Live telemetry & automated facility camera clips",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Live Pill Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFDCFCE7))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF16A34A).copy(alpha = if (isPlaying) recAlpha else 0.4f))
                        )
                        Text(
                            text = if (isPlaying) "LIVE FEED" else "PAUSED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }

            // Video Player Display View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF032B1E)),
                contentAlignment = Alignment.Center
            ) {
                // Background visual photo of the active facility feed
                AsyncImage(
                    model = currentFeed.imageUrl,
                    contentDescription = currentFeed.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark CRT camera tint overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color(0xFF032B1E).copy(alpha = 0.35f),
                                    Color.Black.copy(alpha = 0.65f)
                                )
                            )
                        )
                )

                // Animated Scanlines and Laser Reticle on Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Subtle horizontal CRT scanlines
                    val scanlineGap = 8f
                    var yScan = 0f
                    while (yScan < h) {
                        drawLine(
                            color = Color.Black.copy(alpha = 0.12f),
                            start = Offset(0f, yScan),
                            end = Offset(w, yScan),
                            strokeWidth = 1f
                        )
                        yScan += scanlineGap
                    }

                    if (isPlaying) {
                        // Moving Optical Laser Line (Simulating Barcode / LiDAR Scanner)
                        val laserY = h * laserPosition
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF4ADE80),
                                    Color(0xFF22C55E),
                                    Color(0xFF4ADE80),
                                    Color.Transparent
                                )
                            ),
                            start = Offset(w * 0.1f, laserY),
                            end = Offset(w * 0.9f, laserY),
                            strokeWidth = 3f
                        )

                        // Object Detection Bounding Box Overlay
                        val boxLeft = w * 0.32f
                        val boxTop = h * 0.28f
                        val boxW = w * 0.36f
                        val boxH = h * 0.44f

                        drawRect(
                            color = Color(0xFF4ADE80).copy(alpha = 0.75f),
                            topLeft = Offset(boxLeft, boxTop),
                            size = Size(boxW, boxH),
                            style = Stroke(
                                width = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                            )
                        )
                    }
                }

                // Top Left HUD: Camera Code & Live Recording Indicator
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = if (isPlaying) recAlpha else 0.4f))
                    )
                    Text(
                        text = "REC [●] ${currentFeed.cameraCode}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Top Right HUD: Live Timecode
                val sdf = remember { SimpleDateFormat("HH:mm:ss.S", Locale.getDefault()) }
                Text(
                    text = sdf.format(Date(currentTimestamp)),
                    color = Color(0xFF4ADE80),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                )

                // Bottom Left HUD: Optical AI Detection Label
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "TARGET: ${currentFeed.detectedObject}",
                        color = Color(0xFF4ADE80),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "FPS: 29.97 • 1080p • 4.8 Mbps",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Bottom Right Controls (Play/Pause & Sound)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .testTag("video_feed_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .testTag("video_feed_mute")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Clip Selector Pills (Dock Sorting, AGV Staging, High-Bay ASRS)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                videoFeeds.forEachIndexed { index, feed ->
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Color(0xFF084B34) else Color(0xFFF1F5F9))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF084B34) else Color(0xFFE2E8F0),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                selectedIndex = index
                                isPlaying = true
                            }
                            .padding(vertical = 8.dp, horizontal = 6.dp)
                            .testTag("feed_pill_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = feed.tag,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF334155),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
