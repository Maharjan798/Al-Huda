package com.example.ui.viewmodels

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.AyahUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AudioPlayerState {
    object Idle : AudioPlayerState()
    data class Playing(val currentAyah: AyahUiModel) : AudioPlayerState()
    data class Paused(val currentAyah: AyahUiModel) : AudioPlayerState()
    data class Buffering(val currentAyah: AyahUiModel) : AudioPlayerState()
    data class Error(val message: String) : AudioPlayerState()
}

class AudioPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private var mediaPlayer: MediaPlayer? = null

    private val _playerState = MutableStateFlow<AudioPlayerState>(AudioPlayerState.Idle)
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()

    private val _isRepeatEnabled = MutableStateFlow(false)
    val isRepeatEnabled: StateFlow<Boolean> = _isRepeatEnabled.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _currentPlayingIndex = MutableStateFlow(0)
    val currentPlayingIndex: StateFlow<Int> = _currentPlayingIndex.asStateFlow()

    private var playlist = emptyList<AyahUiModel>()
    private var currentIndex = 0
    private var progressJob: Job? = null

    fun playAyahList(ayahs: List<AyahUiModel>, startIndex: Int = 0) {
        if (ayahs.isEmpty()) return
        playlist = ayahs
        currentIndex = startIndex.coerceIn(0, ayahs.lastIndex)
        _currentPlayingIndex.value = currentIndex
        playCurrentIndex()
    }

    private fun playCurrentIndex() {
        val currentAyah = playlist.getOrNull(currentIndex) ?: return
        val url = currentAyah.audioUrl
        _currentPlayingIndex.value = currentIndex
        if (url.isNull_orEmpty()) {
            _playerState.value = AudioPlayerState.Error("Audio unavailable for verse ${currentAyah.numberInSurah}")
            return
        }

        releasePlayer()
        _playerState.value = AudioPlayerState.Buffering(currentAyah)

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { mp ->
                    applyPlaybackSpeed(mp)
                    mp.start()
                    _duration.value = mp.duration
                    _playerState.value = AudioPlayerState.Playing(currentAyah)
                    startProgressTracker()
                }
                setOnCompletionListener {
                    if (_isRepeatEnabled.value) {
                        playCurrentIndex()
                    } else if (currentIndex < playlist.lastIndex) {
                        currentIndex++
                        _currentPlayingIndex.value = currentIndex
                        playCurrentIndex()
                    } else {
                        _playerState.value = AudioPlayerState.Idle
                    }
                }
                setOnErrorListener { _, _, _ ->
                    _playerState.value = AudioPlayerState.Error("Playback error. Checking network connection.")
                    true
                }
            }
        } catch (e: Exception) {
            _playerState.value = AudioPlayerState.Error("Error preparing audio stream")
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                applyPlaybackSpeed(mp)
            }
        }
    }

    private fun applyPlaybackSpeed(mp: MediaPlayer) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                val speed = _playbackSpeed.value
                val params = mp.playbackParams
                params.speed = speed
                mp.playbackParams = params
            } catch (e: Exception) {
                // Ignore if device playbackParams fails
            }
        }
    }

    fun closePlayer() {
        releasePlayer()
        _playerState.value = AudioPlayerState.Idle
        _currentPosition.value = 0
        _duration.value = 0
    }

    fun togglePlayPause() {
        val currentState = _playerState.value
        when (currentState) {
            is AudioPlayerState.Playing -> {
                mediaPlayer?.pause()
                _playerState.value = AudioPlayerState.Paused(currentState.currentAyah)
            }
            is AudioPlayerState.Paused -> {
                mediaPlayer?.start()
                _playerState.value = AudioPlayerState.Playing(currentState.currentAyah)
                startProgressTracker()
            }
            else -> {
                if (playlist.isNotEmpty()) playCurrentIndex()
            }
        }
    }

    fun nextAyah() {
        if (currentIndex < playlist.lastIndex) {
            currentIndex++
            playCurrentIndex()
        }
    }

    fun previousAyah() {
        if (currentIndex > 0) {
            currentIndex--
            playCurrentIndex()
        }
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    fun toggleRepeat() {
        _isRepeatEnabled.value = !_isRepeatEnabled.value
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (mediaPlayer?.isPlaying == true) {
                _currentPosition.value = mediaPlayer?.currentPosition ?: 0
                delay(500)
            }
        }
    }

    private fun releasePlayer() {
        progressJob?.cancel()
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}

private fun String?.isNull_orEmpty(): Boolean = this == null || this.trim().isEmpty()
