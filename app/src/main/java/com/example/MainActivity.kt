package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import android.content.Intent
import android.net.Uri
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.data?.let { handleDeepLink(it) }
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
    val selectedTab by viewModel.selectedTab.collectAsState()
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

    // Hardware back button returns to Cockpit or dismisses Spotify screen
    if (showSpotifyScreen) {
        BackHandler {
            viewModel.setSpotifyScreenVisible(false)
        }
    } else if (selectedTab != 0) {
        BackHandler {
            viewModel.selectTab(0)
        }
    }

    Scaffold(
        containerColor = CockpitBackground,
        bottomBar = {
            CarPlayNavBar(
                selectedTab = selectedTab,
                onTabSelected = {
                    viewModel.setSpotifyScreenVisible(false)
                    viewModel.selectTab(it)
                },
                accentColor = accentColor
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (selectedTab) {
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
                    onOpenSpotify = { viewModel.setSpotifyScreenVisible(true) }
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
                    onThemeSelect = { viewModel.updateTheme(it) },
                    onUpdateBikeDetails = { nick, model, style -> viewModel.updateBikeDetails(nick, model, style) },
                    onProcessCustomImage = { uri, style -> viewModel.processCustomMotorcycleImage(uri, style) },
                    onExportPdf = { viewModel.exportMaintenancePdfReport() },
                    onOpenEmergencyDialog = { viewModel.setEmergencyDialogVisible(true) },
                    onOpenSpotify = { viewModel.setSpotifyScreenVisible(true) }
                )
            }

            // Spotify Screen Overlay
            if (showSpotifyScreen) {
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
                    onLogout = { viewModel.logoutSpotify() },
                    onBack = { viewModel.setSpotifyScreenVisible(false) }
                )
            }
        }
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
