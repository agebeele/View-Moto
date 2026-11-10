package com.example.model

data class SpotifyUser(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String? = null,
    val plan: String = "Spotify Premium",
    val isConnected: Boolean = true
)

data class SpotifyTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val isLiked: Boolean = true,
    val albumCoverColorHex: String = "#1DB954",
    val genre: String = "Rock / Synth"
) {
    val durationFormatted: String
        get() = String.format("%02d:%02d", durationSeconds / 60, durationSeconds % 60)
}

data class SpotifyPlaybackState(
    val currentTrack: SpotifyTrack,
    val isPlaying: Boolean = false,
    val positionSeconds: Int = 0,
    val isShuffle: Boolean = false,
    val isRepeat: Boolean = false,
    val volumePercent: Int = 85
) {
    val progressFraction: Float
        get() = if (currentTrack.durationSeconds > 0) {
            (positionSeconds.toFloat() / currentTrack.durationSeconds.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val positionFormatted: String
        get() = String.format("%02d:%02d", positionSeconds / 60, positionSeconds % 60)
}
