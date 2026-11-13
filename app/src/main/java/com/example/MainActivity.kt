package com.example

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.CarPlayNavBar
import com.example.ui.screens.BikeCustomizationScreen
import com.example.ui.screens.BikeSelectorDialog
import com.example.ui.screens.CockpitScreen
import com.example.ui.screens.EmergencyContactsDialog
import com.example.ui.screens.FuelScreen
import com.example.ui.screens.MaintenanceScreen
import com.example.ui.screens.SpotifyScreen
import com.example.ui.screens.TripsScreen
import com.example.ui.screens.VoiceCommandDialog
import com.example.ui.theme.CockpitBackground
import com.example.ui.theme.MotoDashTheme
import com.example.ui.viewmodel.MotoDashboardViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MotoDashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep screen on during ride
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Handle possible Spotify OAuth redirect from initial launch
        intent?.data?.let { handleDeepLink(it) }

        setContent {
            val profile by viewModel.profile.collectAsState()

            MotoDashTheme(themeMode = profile.themeMode) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { handleDeepLink(it) }
    }

    private fun handleDeepLink(uri: Uri) {
        if (uri.scheme == "motodash" && uri.host == "callback") {
            viewModel.handleSpotifyRedirect(uri)
            viewModel.setSpotifyScreenVisible(true)
        }
    }
}

@Composable
fun MainAppContent(viewModel: MotoDashboardViewModel) {
    val context = LocalContext.current
    val selectedTab by viewModel.selectedTab.collectAsState()
    val canNavigateBackTab by viewModel.canNavigateBackTab.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val garage by viewModel.garage.collectAsState()
    val showBikeSelectorDialog by viewModel.showBikeSelectorDialog.collectAsState()
    val tpmsData by viewModel.tpms.collectAsState()
    val activeTrip by viewModel.activeTrip.collectAsState()
    val tripHistory by viewModel.tripHistory.collectAsState()
    val componentIntervals by viewModel.componentIntervals.collectAsState()
    val filteredServices by viewModel.filteredServices.collectAsState()
    val searchQuery by viewModel.serviceSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedServiceCategory.collectAsState()
    val fuelStats by viewModel.fuelStats.collectAsState()
    val fuelHistory by viewModel.fuelHistory.collectAsState()
    val emergencyContacts by viewModel.emergencyContacts.collectAsState()
    val mediaState by viewModel.mediaState.collectAsState()
    val customBikeBitmap by viewModel.customBikePreviewBitmap.collectAsState()
    val showVoiceDialog by viewModel.showVoiceDialog.collectAsState()
    val showEmergencyDialog by viewModel.showEmergencyDialog.collectAsState()
    val pdfSuccessToast by viewModel.pdfExportSuccessToast.collectAsState()
    val voiceFeedback by viewModel.voiceAssistant.lastVoiceFeedback.collectAsState()

    // Spotify State
    val showSpotifyScreen by viewModel.showSpotifyScreen.collectAsState()
    val spotifyPlaybackState by viewModel.spotifyPlaybackState.collectAsState()
    val spotifyLikedTracks by viewModel.spotifyLikedTracks.collectAsState()
    val spotifyCurrentUser by viewModel.spotifyCurrentUser.collectAsState()
    val spotifyAccounts by viewModel.spotifyAccounts.collectAsState()

    val accentColor = MaterialTheme.colorScheme.primary

    // Request permissions for Location, Audio and Notifications
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionsLauncher.launch(permissions.toTypedArray())
    }

    // 1. In-App Tab History Back Stack: steps back to previous visited tab inside the app
    val hasActiveOverlay = showSpotifyScreen || showBikeSelectorDialog || showVoiceDialog || showEmergencyDialog
    BackHandler(enabled = canNavigateBackTab && !hasActiveOverlay) {
        viewModel.navigateBackTab()
    }

    // 2. Active Ride Safety Guard: while riding on Cockpit root, require double back gesture to prevent accidental app exit
    var lastRideBackPressMs by remember { mutableLongStateOf(0L) }
    val rideExitGuardEnabled = activeTrip.isRiding && !canNavigateBackTab && !hasActiveOverlay
    BackHandler(enabled = rideExitGuardEnabled) {
        val now = System.currentTimeMillis()
        if (now - lastRideBackPressMs < 2000L) {
            // Allow exit on second consecutive back gesture within 2 seconds
            (context as? ComponentActivity)?.moveTaskToBack(true)
        } else {
            lastRideBackPressMs = now
            Toast.makeText(context, "Viaje en curso: desliza atrás otra vez para salir", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = CockpitBackground,
        bottomBar = {
            if (!showSpotifyScreen) {
                CarPlayNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = {
                        viewModel.setSpotifyScreenVisible(false)
                        viewModel.selectTab(it)
                    },
                    accentColor = accentColor
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(durationMillis = 180),
                label = "main_tab_crossfade"
            ) { tab ->
                when (tab) {
                    0 -> CockpitScreen(
                        profile = profile,
                        tpmsData = tpmsData,
                        activeTrip = activeTrip,
                        mediaState = mediaState,
                        customBikeBitmap = customBikeBitmap,
                        onToggleRide = { viewModel.toggleRide() },
                        onVoiceClick = { viewModel.setVoiceDialogVisible(true) },
                        onEmergencyClick = { viewModel.setEmergencyDialogVisible(true) },
                        onToggleMedia = { viewModel.toggleMedia() },
                        onNextTrack = { viewModel.nextMediaTrack() },
                        onSimulateTpmsWarning = { viewModel.triggerSimulatedTpmsWarning() },
                        onResetTpms = { viewModel.resetTpmsToNormal() },
                        onOpenSpotify = { viewModel.setSpotifyScreenVisible(true) },
                        onOpenBikeSelector = { viewModel.setBikeSelectorVisible(true) }
                    )
                    1 -> TripsScreen(
                        tripHistory = tripHistory
                    )
                    2 -> MaintenanceScreen(
                        profile = profile,
                        componentIntervals = componentIntervals,
                        services = filteredServices,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        pdfSuccessToast = pdfSuccessToast,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onCategorySelect = { viewModel.setServiceCategory(it) },
                        onUpdateOdometer = { viewModel.updateOdometer(it) },
                        onAddService = { title, cat, cost, notes -> viewModel.addNewService(title, cat, cost, notes) },
                        onExportPdf = { viewModel.exportMaintenancePdfReport() },
                        onSendUrgentPushAlert = { viewModel.sendUrgentMaintenancePushNotification() },
                        onClearPdfToast = { viewModel.clearPdfToast() }
                    )
                    3 -> FuelScreen(
                        fuelStats = fuelStats,
                        fuelHistory = fuelHistory,
                        onAddFuelEntry = { liters, cost -> viewModel.addNewFuelEntry(liters, cost) },
                        onUpdateEstimatedEfficiency = { viewModel.updateEstimatedEfficiency(it) },
                        onUpdateFuelEntry = { viewModel.updateFuelEntry(it) },
                        onDeleteFuelEntry = { viewModel.deleteFuelEntry(it) }
                    )
                    4 -> BikeCustomizationScreen(
                        profile = profile,
                        emergencyContacts = emergencyContacts,
                        customBikeBitmap = customBikeBitmap,
                        garageList = garage,
                        imageProcessor = viewModel.imageProcessor,
                        onThemeSelect = { viewModel.updateTheme(it) },
                        onUpdateBikeDetails = { nick, model, style -> viewModel.updateBikeDetails(nick, model, style) },
                        onProcessCustomImage = { uri, style -> viewModel.processCustomMotorcycleImage(uri, style) },
                        onSaveCutoutBitmap = { viewModel.saveProcessedCutoutBitmap(it) },
                        onOpenBikeSelector = { viewModel.setBikeSelectorVisible(true) },
                        onExportPdf = { viewModel.exportMaintenancePdfReport() },
                        onOpenEmergencyDialog = { viewModel.setEmergencyDialogVisible(true) },
                        onOpenSpotify = { viewModel.setSpotifyScreenVisible(true) }
                    )
                }
            }

            // Spotify Screen Overlay with Slide Back Transition
            AnimatedVisibility(
                visible = showSpotifyScreen,
                enter = slideInHorizontally { it / 2 } + fadeIn(),
                exit = slideOutHorizontally { it / 2 } + fadeOut()
            ) {
                SpotifyScreen(
                    playbackState = spotifyPlaybackState,
                    likedTracks = spotifyLikedTracks,
                    currentUser = spotifyCurrentUser,
                    accounts = spotifyAccounts,
                    onPlayTrack = { viewModel.playSpotifyTrack(it) },
                    onTogglePlayPause = { viewModel.toggleSpotifyPlayPause() },
                    onNextTrack = { viewModel.nextSpotifyTrack() },
                    onPreviousTrack = { viewModel.previousSpotifyTrack() },
                    onSeek = { viewModel.seekSpotifyFraction(it) },
                    onToggleLike = { viewModel.toggleSpotifyLike(it) },
                    onToggleShuffle = { viewModel.toggleSpotifyShuffle() },
                    onToggleRepeat = { viewModel.toggleSpotifyRepeat() },
                    onSwitchAccount = { viewModel.switchSpotifyAccount(it) },
                    onLogin = { user, plan -> viewModel.loginSpotify(user, plan) },
                    onLoginWithWeb = { viewModel.openSpotifyLoginInBrowser() },
                    onLogout = { viewModel.logoutSpotify() },
                    onBack = { viewModel.setSpotifyScreenVisible(false) }
                )
            }
        }
    }

    // Bike Selector / Garage Dialog
    if (showBikeSelectorDialog) {
        BikeSelectorDialog(
            currentProfile = profile,
            garageList = garage,
            onSelectBike = { viewModel.switchActiveMotorcycle(it) },
            onAddNewBike = { nick, model, plate, odo, theme ->
                viewModel.addMotorcycleToGarage(nick, model, plate, odo, theme)
            },
            onDeleteBike = { viewModel.deleteMotorcycleFromGarage(it) },
            onDismiss = { viewModel.setBikeSelectorVisible(false) }
        )
    }

    // Voice Command Modal
    if (showVoiceDialog) {
        VoiceCommandDialog(
            lastFeedback = voiceFeedback,
            accentColor = accentColor,
            onExecuteCommand = { viewModel.executeVoiceCommand(it) },
            onDismiss = { viewModel.setVoiceDialogVisible(false) }
        )
    }

    // Emergency SOS Modal (with Full CRUD)
    if (showEmergencyDialog) {
        EmergencyContactsDialog(
            contacts = emergencyContacts,
            onDismiss = { viewModel.setEmergencyDialogVisible(false) },
            onAddContact = { name, phone, rel, primary ->
                viewModel.addEmergencyContact(name, phone, rel, primary)
            },
            onUpdateContact = { contact ->
                viewModel.updateEmergencyContact(contact)
            },
            onDeleteContact = { contactId ->
                viewModel.deleteEmergencyContact(contactId)
            }
        )
    }
}
