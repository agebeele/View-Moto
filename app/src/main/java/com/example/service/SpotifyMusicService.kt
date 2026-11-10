package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.SpotifyPlaybackState
import com.example.model.SpotifyTrack
import com.example.model.SpotifyUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class SpotifyMusicService(private val context: Context) {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var playbackTickerJob: Job? = null
    private var audioToneJob: Job? = null

    // Saved accounts to switch easily
    private val defaultAccounts = listOf(
        SpotifyUser(
            id = "user_01",
            username = "rider_300z",
            displayName = "Rider 300Z",
            email = "alex.rider@motodash.mx",
            plan = "Spotify Premium",
            isConnected = true
        ),
        SpotifyUser(
            id = "user_02",
            username = "motoclub_cdmx",
            displayName = "MotoClub Asfalto",
            email = "club@asfalto.mx",
            plan = "Spotify Duo",
            isConnected = false
        ),
        SpotifyUser(
            id = "user_03",
            username = "track_day_pilot",
            displayName = "Piloto Nocturno",
            email = "nocturno@speed.io",
            plan = "Spotify Premium",
            isConnected = false
        )
    )

    private val _accounts = MutableStateFlow(defaultAccounts)
    val accounts: StateFlow<List<SpotifyUser>> = _accounts.asStateFlow()

    private val _currentUser = MutableStateFlow(defaultAccounts[0])
    val currentUser: StateFlow<SpotifyUser> = _currentUser.asStateFlow()

    // "Tus me gusta" (Liked Songs)
    private val defaultLikedTracks = listOf(
        SpotifyTrack("sp_1", "Midnight City", "M83", "Hurry Up, We're Dreaming", 243, isLiked = true, albumCoverColorHex = "#7928CA", genre = "Synthwave"),
        SpotifyTrack("sp_2", "Blinding Lights", "The Weeknd", "After Hours", 200, isLiked = true, albumCoverColorHex = "#E11D48", genre = "Electro Pop"),
        SpotifyTrack("sp_3", "Born to be Wild", "Steppenwolf", "Steppenwolf", 210, isLiked = true, albumCoverColorHex = "#D97706", genre = "Classic Rock"),
        SpotifyTrack("sp_4", "Highway to Hell", "AC/DC", "Highway to Hell", 208, isLiked = true, albumCoverColorHex = "#DC2626", genre = "Hard Rock"),
        SpotifyTrack("sp_5", "Nightcall", "Kavinsky", "OutRun", 259, isLiked = true, albumCoverColorHex = "#2563EB", genre = "Synthwave"),
        SpotifyTrack("sp_6", "Kickstart My Heart", "Mötley Crüe", "Dr. Feelgood", 282, isLiked = true, albumCoverColorHex = "#EA580C", genre = "Metal"),
        SpotifyTrack("sp_7", "The Trooper", "Iron Maiden", "Piece of Mind", 251, isLiked = true, albumCoverColorHex = "#16A34A", genre = "Heavy Metal"),
        SpotifyTrack("sp_8", "Acelerando el Motor", "MotoDash Riders", "Pista 300Z", 195, isLiked = true, albumCoverColorHex = "#FF007F", genre = "Motorcycle Beats"),
        SpotifyTrack("sp_9", "Radar Love", "Golden Earring", "Moontan", 384, isLiked = true, albumCoverColorHex = "#0D9488", genre = "Road Rock"),
        SpotifyTrack("sp_10", "Ride the Lightning", "Metallica", "Ride the Lightning", 396, isLiked = true, albumCoverColorHex = "#3B82F6", genre = "Thrash Metal")
    )

    private val _likedTracks = MutableStateFlow(defaultLikedTracks)
    val likedTracks: StateFlow<List<SpotifyTrack>> = _likedTracks.asStateFlow()

    private val _playbackState = MutableStateFlow(
        SpotifyPlaybackState(
            currentTrack = defaultLikedTracks[0],
            isPlaying = false,
            positionSeconds = 45,
            isShuffle = false,
            isRepeat = false
        )
    )
    val playbackState: StateFlow<SpotifyPlaybackState> = _playbackState.asStateFlow()

    fun playTrack(track: SpotifyTrack) {
        _playbackState.update {
            it.copy(
                currentTrack = track,
                isPlaying = true,
                positionSeconds = 0
            )
        }
        startPlaybackTicker()
        playAudioChime()
    }

    fun togglePlayPause() {
        val nextPlaying = !_playbackState.value.isPlaying
        _playbackState.update { it.copy(isPlaying = nextPlaying) }
        if (nextPlaying) {
            startPlaybackTicker()
            playAudioChime()
        } else {
            playbackTickerJob?.cancel()
            playbackTickerJob = null
        }
    }

    fun nextTrack() {
        val tracks = _likedTracks.value
        if (tracks.isEmpty()) return
        val currentIndex = tracks.indexOfFirst { it.id == _playbackState.value.currentTrack.id }
        val nextIndex = if (_playbackState.value.isShuffle) {
            (0 until tracks.size).filter { it != currentIndex }.randomOrNull() ?: 0
        } else {
            (currentIndex + 1) % tracks.size
        }
        playTrack(tracks[nextIndex])
    }

    fun previousTrack() {
        val tracks = _likedTracks.value
        if (tracks.isEmpty()) return
        val currentIndex = tracks.indexOfFirst { it.id == _playbackState.value.currentTrack.id }
        val prevIndex = if (currentIndex <= 0) tracks.size - 1 else currentIndex - 1
        playTrack(tracks[prevIndex])
    }

    fun seekToFraction(fraction: Float) {
        val track = _playbackState.value.currentTrack
        val targetSeconds = (track.durationSeconds * fraction).toInt().coerceIn(0, track.durationSeconds)
        _playbackState.update { it.copy(positionSeconds = targetSeconds) }
    }

    fun toggleLike(trackId: String) {
        _likedTracks.update { list ->
            list.map { track ->
                if (track.id == trackId) {
                    track.copy(isLiked = !track.isLiked)
                } else track
            }
        }
        // Update current track liked state if matching
        if (_playbackState.value.currentTrack.id == trackId) {
            _playbackState.update {
                it.copy(currentTrack = it.currentTrack.copy(isLiked = !it.currentTrack.isLiked))
            }
        }
    }

    fun toggleShuffle() {
        _playbackState.update { it.copy(isShuffle = !it.isShuffle) }
    }

    fun toggleRepeat() {
        _playbackState.update { it.copy(isRepeat = !it.isRepeat) }
    }

    // -------------------------------------------------------------
    // Account Management (CRUD / Login / Switch)
    // -------------------------------------------------------------
    fun switchAccount(userId: String) {
        val found = _accounts.value.find { it.id == userId } ?: return
        _accounts.update { list ->
            list.map { it.copy(isConnected = it.id == userId) }
        }
        _currentUser.value = found.copy(isConnected = true)
    }

    fun login(usernameOrEmail: String, plan: String = "Spotify Premium"): SpotifyUser {
        val existing = _accounts.value.find {
            it.username.equals(usernameOrEmail, ignoreCase = true) ||
                    it.email.equals(usernameOrEmail, ignoreCase = true)
        }
        if (existing != null) {
            switchAccount(existing.id)
            return existing
        }

        val cleanName = usernameOrEmail.substringBefore("@").replace(".", " ").capitalize()
        val newUser = SpotifyUser(
            id = "user_${System.currentTimeMillis()}",
            username = usernameOrEmail.substringBefore("@").lowercase(),
            displayName = cleanName.ifBlank { "Motociclista" },
            email = if (usernameOrEmail.contains("@")) usernameOrEmail else "$usernameOrEmail@spotify.com",
            plan = plan,
            isConnected = true
        )
        _accounts.update { list ->
            list.map { it.copy(isConnected = false) } + newUser
        }
        _currentUser.value = newUser
        return newUser
    }

    fun logout() {
        _currentUser.update { it.copy(isConnected = false) }
        _accounts.update { list ->
            list.map { it.copy(isConnected = false) }
        }
        _playbackState.update { it.copy(isPlaying = false) }
        playbackTickerJob?.cancel()
    }

    // -------------------------------------------------------------
    // Playback Timing Ticker
    // -------------------------------------------------------------
    private fun startPlaybackTicker() {
        playbackTickerJob?.cancel()
        playbackTickerJob = serviceScope.launch {
            while (isActive && _playbackState.value.isPlaying) {
                delay(1000L)
                val currentSec = _playbackState.value.positionSeconds + 1
                val trackLen = _playbackState.value.currentTrack.durationSeconds
                if (currentSec >= trackLen) {
                    if (_playbackState.value.isRepeat) {
                        _playbackState.update { it.copy(positionSeconds = 0) }
                    } else {
                        nextTrack()
                    }
                } else {
                    _playbackState.update { it.copy(positionSeconds = currentSec) }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // Audible synthesizer chime so music play produces actual sound
    // -------------------------------------------------------------
    private fun playAudioChime() {
        audioToneJob?.cancel()
        audioToneJob = serviceScope.launch(Dispatchers.IO) {
            try {
                val sampleRate = 22050
                val numSamples = (sampleRate * 0.15).toInt()
                val buffer = ShortArray(numSamples)
                val freq = 440.0 // A4 note

                for (i in 0 until numSamples) {
                    val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                    val sample = (sin(angle) * 0.3 * Short.MAX_VALUE).toInt()
                    buffer[i] = sample.toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                delay(200L)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Audio output handled gracefully if muted or in background
            }
        }
    }
}
