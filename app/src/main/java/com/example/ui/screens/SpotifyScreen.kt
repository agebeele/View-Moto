package com.example.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpotifyPlaybackState
import com.example.model.SpotifyTrack
import com.example.model.SpotifyUser
import com.example.ui.theme.CockpitBackground
import com.example.ui.theme.CockpitCardBg
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.HighContrastWhite
import com.example.ui.theme.TextSecondary

private val SpotifyGreen = Color(0xFF1DB954)
private val SpotifyDarkSurface = Color(0xFF121212)
private val SpotifyCardSurface = Color(0xFF181818)

@Composable
fun SpotifyScreen(
    playbackState: SpotifyPlaybackState,
    likedTracks: List<SpotifyTrack>,
    currentUser: SpotifyUser,
    accounts: List<SpotifyUser>,
    onPlayTrack: (SpotifyTrack) -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleLike: (String) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onSwitchAccount: (String) -> Unit,
    onLogin: (String, String) -> Unit,
    onLoginWithWeb: () -> Unit = {},
    onLogout: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAccountDialog by remember { mutableStateOf(false) }
    var loginUsernameInput by remember { mutableStateOf("") }
    var loginPlanInput by remember { mutableStateOf("Spotify Premium") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CockpitBackground)
            .statusBarsPadding()
            .testTag("spotify_screen")
    ) {
        // Top Bar: Back, Spotify Brand and Account Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar al Cockpit",
                        tint = HighContrastWhite
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = null,
                    tint = SpotifyGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Spotify",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = HighContrastWhite,
                    letterSpacing = 0.5.sp
                )
            }

            // User Profile & Switch Account Button
            Surface(
                color = SpotifyDarkSurface,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitCardBorder),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showAccountDialog = true }
                    .testTag("spotify_account_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(SpotifyGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser.displayName.take(1).uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                    Column {
                        Text(
                            text = currentUser.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighContrastWhite,
                            maxLines = 1
                        )
                        Text(
                            text = currentUser.plan,
                            fontSize = 9.sp,
                            color = SpotifyGreen
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Cambiar cuenta",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Hero: "Tus me gusta" Playlist Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SpotifyCardSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CockpitCardBorder, RoundedCornerShape(22.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0F5132),
                                        SpotifyCardSurface
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Big Liked Songs Art
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF450AF5),
                                                    Color(0xFF8E8EE5),
                                                    Color(0xFF1DB954)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "PLAYLIST DE CARPLAY",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SpotifyGreen,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Tus me gusta",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = HighContrastWhite
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${likedTracks.size} canciones descargadas · Modo MotoDash",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Play & Shuffle Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (likedTracks.isNotEmpty()) {
                                            if (playbackState.isPlaying) {
                                                onTogglePlayPause()
                                            } else {
                                                onPlayTrack(likedTracks.first())
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("spotify_play_all_button")
                                ) {
                                    Icon(
                                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (playbackState.isPlaying) "Pausar" else "Reproducir Todo",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        fontSize = 14.sp
                                    )
                                }

                                IconButton(
                                    onClick = onToggleShuffle,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(if (playbackState.isShuffle) SpotifyGreen.copy(alpha = 0.2f) else SpotifyDarkSurface)
                                        .border(1.dp, if (playbackState.isShuffle) SpotifyGreen else CockpitCardBorder, CircleShape)
                                        .testTag("spotify_shuffle_toggle")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = "Aleatorio",
                                        tint = if (playbackState.isShuffle) SpotifyGreen else HighContrastWhite
                                    )
                                }

                                IconButton(
                                    onClick = onToggleRepeat,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(if (playbackState.isRepeat) SpotifyGreen.copy(alpha = 0.2f) else SpotifyDarkSurface)
                                        .border(1.dp, if (playbackState.isRepeat) SpotifyGreen else CockpitCardBorder, CircleShape)
                                        .testTag("spotify_repeat_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (playbackState.isRepeat) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                        contentDescription = "Repetir",
                                        tint = if (playbackState.isRepeat) SpotifyGreen else HighContrastWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Canciones en tu biblioteca",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighContrastWhite
                )
            }

            // Track list
            itemsIndexed(likedTracks) { index, track ->
                val isCurrentTrack = playbackState.currentTrack.id == track.id
                val itemBg = if (isCurrentTrack) Color(0xFF1E2822) else SpotifyCardSurface

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = itemBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isCurrentTrack) SpotifyGreen.copy(alpha = 0.7f) else CockpitCardBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onPlayTrack(track) }
                        .testTag("spotify_track_${track.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Cover Art Thumbnail with genre color
                            val coverColor = try {
                                Color(android.graphics.Color.parseColor(track.albumCoverColorHex))
                            } catch (_: Exception) {
                                SpotifyGreen
                            }

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(coverColor),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCurrentTrack && playbackState.isPlaying) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = "Sonando",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = track.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrentTrack) SpotifyGreen else HighContrastWhite,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${track.artist} · ${track.album}",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = track.durationFormatted,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(onClick = { onToggleLike(track.id) }) {
                                Icon(
                                    imageVector = if (track.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Me gusta",
                                    tint = if (track.isLiked) SpotifyGreen else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Persistent Motorcycle Player Bar
        Card(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = SpotifyDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CockpitCardBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Progress Bar
                LinearProgressIndicator(
                    progress = { playbackState.progressFraction },
                    color = SpotifyGreen,
                    trackColor = CockpitCardBorder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = playbackState.positionFormatted,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = playbackState.currentTrack.durationFormatted,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Track Info & Big Tactile Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playbackState.currentTrack.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = HighContrastWhite,
                            maxLines = 1
                        )
                        Text(
                            text = "${playbackState.currentTrack.artist} · Spotify",
                            fontSize = 12.sp,
                            color = SpotifyGreen,
                            maxLines = 1
                        )
                    }

                    // Playback Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = onPreviousTrack) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Anterior",
                                tint = HighContrastWhite,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        // Big Play / Pause Button
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(SpotifyGreen)
                                .clickable { onTogglePlayPause() }
                                .testTag("spotify_main_play_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playbackState.isPlaying) "Pausar" else "Reproducir",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        IconButton(onClick = onNextTrack) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Siguiente",
                                tint = HighContrastWhite,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal: Iniciar sesión / Cambiar de cuenta
    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = SpotifyGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cuentas de Spotify",
                        fontWeight = FontWeight.Bold,
                        color = HighContrastWhite,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Cambiar de cuenta activa para reproducir tus playlists y me gusta:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    // List of registered accounts
                    accounts.forEach { user ->
                        val isCurrent = user.id == currentUser.id
                        Surface(
                            color = if (isCurrent) Color(0xFF1E2822) else SpotifyDarkSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCurrent) SpotifyGreen else CockpitCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSwitchAccount(user.id)
                                    showAccountDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (isCurrent) SpotifyGreen else Color.DarkGray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = user.displayName.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = user.displayName,
                                            fontWeight = FontWeight.Bold,
                                            color = HighContrastWhite,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${user.email} · ${user.plan}",
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Activa",
                                        tint = SpotifyGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Usar",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SpotifyGreen
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Primary: Official Web Authorization flow via accounts.spotify.com
                    Button(
                        onClick = {
                            onLoginWithWeb()
                            showAccountDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("spotify_web_login_button")
                    ) {
                        Icon(imageVector = Icons.Default.Login, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Iniciar Sesión Oficial (Web Spotify)",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = "Abre accounts.spotify.com para autorizar y sincronizar tu perfil real y canciones de 'Tus Me Gusta' mediante la API de Spotify.",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "O ingresa con usuario rápido:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HighContrastWhite
                    )

                    OutlinedTextField(
                        value = loginUsernameInput,
                        onValueChange = { loginUsernameInput = it },
                        placeholder = { Text("Usuario o correo Spotify", color = Color.Gray) },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SpotifyGreen,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (loginUsernameInput.isNotBlank()) {
                                onLogin(loginUsernameInput.trim(), loginPlanInput)
                                loginUsernameInput = ""
                                showAccountDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CockpitCardBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Acceso Rápido Local", color = HighContrastWhite, fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = {
                            onLogout()
                            showAccountDialog = false
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cerrar Sesión Activa", color = Color(0xFFFF5252), fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAccountDialog = false }) {
                    Text("Cerrar", color = Color.Gray)
                }
            },
            containerColor = CockpitCardBg
        )
    }
}
