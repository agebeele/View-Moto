package com.example.model

data class EmergencyContact(
    val id: String,
    val name: String,
    val phone: String,
    val relationship: String,
    val isPrimary: Boolean = false,
    val voiceCommandAlias: String = ""
)

data class MediaPlaybackState(
    val title: String = "Rider In The Storm",
    val artist: String = "Synthwave Riders",
    val album: String = "Night Asphalt Vol. 1",
    val isPlaying: Boolean = false,
    val progressFraction: Float = 0.35f,
    val currentPositionSeconds: Int = 84,
    val durationSeconds: Int = 240,
    val activeApp: String = "Spotify"
)
