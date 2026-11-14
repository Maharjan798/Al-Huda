package com.example.ui.screens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldSecondary
import com.example.ui.viewmodels.AudioPlayerState
import com.example.ui.viewmodels.AudioPlayerViewModel

@Composable
fun MiniAudioPlayer(
    audioViewModel: AudioPlayerViewModel,
    onPlayerClick: () -> Unit = {}
) {
    val playerState by audioViewModel.playerState.collectAsState()
    val currentPosition by audioViewModel.currentPosition.collectAsState()
    val duration by audioViewModel.duration.collectAsState()

    val currentAyah = when (playerState) {
        is AudioPlayerState.Playing -> (playerState as AudioPlayerState.Playing).currentAyah
        is AudioPlayerState.Paused -> (playerState as AudioPlayerState.Paused).currentAyah
        is AudioPlayerState.Buffering -> (playerState as AudioPlayerState.Buffering).currentAyah
        else -> null
    }

    AnimatedVisibility(
        visible = currentAyah != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        if (currentAyah != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable { onPlayerClick() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = com.example.ui.theme.EmeraldPrimary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column {
                    val progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = GoldSecondary,
                        trackColor = GoldSecondary.copy(alpha = 0.2f)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${currentAyah.surahName} • Ayah ${currentAyah.numberInSurah}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentAyah.arabicText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = GoldSecondary,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(onClick = { audioViewModel.previousAyah() }) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous Ayah",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        IconButton(onClick = { audioViewModel.togglePlayPause() }) {
                            Icon(
                                imageVector = if (playerState is AudioPlayerState.Playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play or Pause",
                                tint = GoldSecondary
                            )
                        }

                        IconButton(onClick = { audioViewModel.nextAyah() }) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Ayah",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        IconButton(onClick = { audioViewModel.closePlayer() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Player",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
