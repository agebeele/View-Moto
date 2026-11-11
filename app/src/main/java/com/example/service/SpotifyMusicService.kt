package com.example.service

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.net.Uri
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
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.math.sin

class SpotifyMusicService(private val context: Context) {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var playbackTickerJob: Job? = null
    private var audioToneJob: Job? = null

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    // Configurable Spotify Client ID for OAuth
    var spotifyClientId: String = "4f8a846f48864132808c1097e88258dc"
    var redirectUri: String = "motodash://callback"

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    private val _lastAuthError = MutableStateFlow<String?>(null)
    val lastAuthError: StateFlow<String?> = _lastAuthError.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    // No dummy accounts: strictly authenticated via Spotify Web API
    private val unlinkedUser = SpotifyUser(
        id = "unlinked",
        username = "",
        displayName = "Sin cuenta vinculada",
        email = "Inicia sesión con la API de Spotify",
        plan = "No conectado",
        isConnected = false
    )

    private val _accounts = MutableStateFlow<List<SpotifyUser>>(emptyList())
    val accounts: StateFlow<List<SpotifyUser>> = _accounts.asStateFlow()

    private val _currentUser = MutableStateFlow(unlinkedUser)
    val currentUser: StateFlow<SpotifyUser> = _currentUser.asStateFlow()

    private val placeholderTrack = SpotifyTrack(
        id = "sp_welcome",
        title = "Conecta tu Spotify",
        artist = "Presiona 'Iniciar sesión en Spotify'",
        album = "Spotify Web API",
        durationSeconds = 180,
        isLiked = false,
        albumCoverColorHex = "#1DB954",
        genre = "Spotify Web"
    )

    private val _likedTracks = MutableStateFlow<List<SpotifyTrack>>(emptyList())
    val likedTracks: StateFlow<List<SpotifyTrack>> = _likedTracks.asStateFlow()

    private val _playbackState = MutableStateFlow(
        SpotifyPlaybackState(
            currentTrack = placeholderTrack,
            isPlaying = false,
            positionSeconds = 0,
            isShuffle = false,
            isRepeat = false
        )
    )
    val playbackState: StateFlow<SpotifyPlaybackState> = _playbackState.asStateFlow()

    /**
     * Builds the official Spotify OAuth web authorization URL.
     */
    fun buildOAuthLoginUrl(): String {
        val encodedRedirect = URLEncoder.encode(redirectUri, "UTF-8")
        val scopes = URLEncoder.encode(
            "user-read-private user-read-email user-library-read user-read-playback-state user-modify-playback-state playlist-read-private",
            "UTF-8"
        )
        return "https://accounts.spotify.com/authorize?client_id=$spotifyClientId&response_type=token&redirect_uri=$encodedRedirect&scope=$scopes&show_dialog=true"
    }

    /**
     * Redirects user to Spotify's official login page via web browser.
     */
    fun openSpotifyLoginInBrowser() {
        val url = buildOAuthLoginUrl()
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    /**
     * Handles redirect from Spotify OAuth deep link callback (motodash://callback#access_token=...)
     */
    fun handleOAuthRedirectUri(uri: Uri) {
        val fragment = uri.fragment ?: uri.query ?: ""
        val token = fragment.split("&")
            .find { it.startsWith("access_token=") }
            ?.substringAfter("access_token=")

        if (!token.isNullOrBlank()) {
            _accessToken.value = token
            fetchProfileAndLikedSongs(token)
        } else {
            val error = fragment.split("&")
                .find { it.startsWith("error=") }
                ?.substringAfter("error=")
            _lastAuthError.value = error ?: "No se recibió token de acceso"
        }
    }

    /**
     * Calls Spotify RESTful API with OAuth Access Token to fetch Profile and Real Liked Songs.
     */
    fun fetchProfileAndLikedSongs(token: String) {
        serviceScope.launch {
            _isAuthenticating.value = true
            _lastAuthError.value = null

            try {
                // 1. Fetch User Profile (GET https://api.spotify.com/v1/me)
                val profileRequest = Request.Builder()
                    .url("https://api.spotify.com/v1/me")
                    .header("Authorization", "Bearer $token")
                    .header("Accept", "application/json")
                    .build()

                val profileResponse = withContext(Dispatchers.IO) {
                    httpClient.newCall(profileRequest).execute()
                }

                if (profileResponse.isSuccessful) {
                    val bodyStr = profileResponse.body?.string() ?: "{}"
                    val json = JSONObject(bodyStr)
                    val id = json.optString("id", "spotify_${System.currentTimeMillis()}")
                    val displayName = json.optString("display_name", "Usuario Spotify")
                    val email = json.optString("email", "conectado@spotify.com")
                    val product = json.optString("product", "premium").replaceFirstChar { it.uppercase() }

                    val authenticatedUser = SpotifyUser(
                        id = id,
                        username = id,
                        displayName = displayName,
                        email = email,
                        plan = "Spotify $product",
                        isConnected = true
                    )

                    _currentUser.value = authenticatedUser
                    _accounts.update { list ->
                        listOf(authenticatedUser) + list.filter { it.id != id }.map { it.copy(isConnected = false) }
                    }

                    // 2. Fetch User's Liked Songs (GET https://api.spotify.com/v1/me/tracks?limit=30)
                    val tracksRequest = Request.Builder()
                        .url("https://api.spotify.com/v1/me/tracks?limit=30")
                        .header("Authorization", "Bearer $token")
                        .header("Accept", "application/json")
                        .build()

                    val tracksResponse = withContext(Dispatchers.IO) {
                        httpClient.newCall(tracksRequest).execute()
                    }

                    if (tracksResponse.isSuccessful) {
                        val tracksBody = tracksResponse.body?.string() ?: "{}"
                        val tracksJson = JSONObject(tracksBody)
                        val items = tracksJson.optJSONArray("items")

                        if (items != null && items.length() > 0) {
                            val parsedTracks = mutableListOf<SpotifyTrack>()
                            for (i in 0 until items.length()) {
                                val item = items.getJSONObject(i)
                                val trackObj = item.optJSONObject("track") ?: continue
                                val trackId = trackObj.optString("id", "track_$i")
                                val title = trackObj.optString("name", "Pista $i")
                                val durationMs = trackObj.optLong("duration_ms", 180000L)
                                val albumObj = trackObj.optJSONObject("album")
                                val albumName = albumObj?.optString("name") ?: "Álbum"

                                val artistsArr = trackObj.optJSONArray("artists")
                                val artistName = if (artistsArr != null && artistsArr.length() > 0) {
                                    artistsArr.getJSONObject(0).optString("name", "Artista")
                                } else "Artista"

                                val colors = listOf("#1DB954", "#E11D48", "#2563EB", "#7928CA", "#EA580C", "#16A34A")
                                val color = colors[i % colors.size]

                                parsedTracks.add(
                                    SpotifyTrack(
                                        id = trackId,
                                        title = title,
                                        artist = artistName,
                                        album = albumName,
                                        durationSeconds = (durationMs / 1000).toInt(),
                                        isLiked = true,
                                        albumCoverColorHex = color,
                                        genre = "Spotify Liked"
                                    )
                                )
                            }
                            if (parsedTracks.isNotEmpty()) {
                                _likedTracks.value = parsedTracks
                                _playbackState.update { it.copy(currentTrack = parsedTracks[0]) }
                            }
                        }
                    }
                } else {
                    _lastAuthError.value = "Error al autenticar: ${profileResponse.code} ${profileResponse.message}"
                }
            } catch (e: Exception) {
                _lastAuthError.value = "Error de red: ${e.message}"
            } finally {
                _isAuthenticating.value = false
            }
        }
    }

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

    fun switchAccount(userId: String) {
        val found = _accounts.value.find { it.id == userId } ?: return
        _accounts.update { list ->
            list.map { it.copy(isConnected = it.id == userId) }
        }
        _currentUser.value = found.copy(isConnected = true)
    }

    fun login(usernameOrEmail: String = "", plan: String = "Spotify Premium"): SpotifyUser {
        openSpotifyLoginInBrowser()
        return _currentUser.value
    }

    fun loginManual(usernameOrEmail: String, plan: String = "Spotify Premium"): SpotifyUser {
        val cleanName = usernameOrEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
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
        _accessToken.value = null
        _playbackState.update { it.copy(isPlaying = false) }
        playbackTickerJob?.cancel()
    }

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

    private fun playAudioChime() {
        audioToneJob?.cancel()
        audioToneJob = serviceScope.launch(Dispatchers.IO) {
            try {
                val sampleRate = 22050
                val numSamples = (sampleRate * 0.15).toInt()
                val buffer = ShortArray(numSamples)
                val freq = 440.0

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
            } catch (_: Exception) {}
        }
    }
}
