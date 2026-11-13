package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MotoRepository
import com.example.model.BikeGraphicStyle
import com.example.model.ComponentInterval
import com.example.model.EmergencyContact
import com.example.model.FuelEntry
import com.example.model.FuelStats
import com.example.model.MediaPlaybackState
import com.example.model.MotoProfile
import com.example.model.ServiceCategory
import com.example.model.ServiceRecord
import com.example.model.TireStatus
import com.example.model.TpmsData
import com.example.model.TripRecord
import com.example.model.VisualThemeMode
import com.example.service.ImageVectorizationProcessor
import com.example.service.NotificationHelper
import com.example.service.PdfReportExporter
import com.example.service.VoiceAssistantManager
import com.example.service.VoiceCommandResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

import com.example.model.SpotifyPlaybackState
import com.example.model.SpotifyTrack
import com.example.model.SpotifyUser
import com.example.service.SpotifyMusicService
import kotlinx.coroutines.flow.map

class MotoDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MotoRepository()
    val voiceAssistant = VoiceAssistantManager(application)
    val notificationHelper = NotificationHelper(application)
    val pdfExporter = PdfReportExporter(application)
    val imageProcessor = ImageVectorizationProcessor(application)
    val spotifyService = SpotifyMusicService(application)

    // Data streams from repository
    val profile: StateFlow<MotoProfile> = repository.profile
    val garage: StateFlow<List<MotoProfile>> = repository.garage
    val tpms: StateFlow<TpmsData> = repository.tpms
    val activeTrip = repository.activeTrip
    val tripHistory: StateFlow<List<TripRecord>> = repository.tripHistory
    val componentIntervals: StateFlow<List<ComponentInterval>> = repository.componentIntervals
    val fuelStats: StateFlow<FuelStats> = repository.fuelStats
    val fuelHistory: StateFlow<List<FuelEntry>> = repository.fuelHistory
    val emergencyContacts: StateFlow<List<EmergencyContact>> = repository.emergencyContacts
    val mediaState: StateFlow<MediaPlaybackState> = repository.mediaState

    // Spotify Music data streams
    val spotifyPlaybackState: StateFlow<SpotifyPlaybackState> = spotifyService.playbackState
    val spotifyLikedTracks: StateFlow<List<SpotifyTrack>> = spotifyService.likedTracks
    val spotifyCurrentUser: StateFlow<SpotifyUser> = spotifyService.currentUser
    val spotifyAccounts: StateFlow<List<SpotifyUser>> = spotifyService.accounts
    val spotifyAccessToken: StateFlow<String?> = spotifyService.accessToken
    val spotifyIsAuthenticating: StateFlow<Boolean> = spotifyService.isAuthenticating
    val spotifyAuthError: StateFlow<String?> = spotifyService.lastAuthError

    private val _showSpotifyScreen = MutableStateFlow(false)
    val showSpotifyScreen: StateFlow<Boolean> = _showSpotifyScreen.asStateFlow()

    private val _showBikeSelectorDialog = MutableStateFlow(false)
    val showBikeSelectorDialog: StateFlow<Boolean> = _showBikeSelectorDialog.asStateFlow()

    // Navigation and Tab Back Stack
    private val _tabBackStack = MutableStateFlow(listOf(0))
    val selectedTab: StateFlow<Int> = _tabBackStack
        .map { it.lastOrNull() ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val canNavigateBackTab: StateFlow<Boolean> = _tabBackStack
        .map { it.size > 1 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Service Filters
    private val _serviceSearchQuery = MutableStateFlow("")
    val serviceSearchQuery: StateFlow<String> = _serviceSearchQuery.asStateFlow()

    private val _selectedServiceCategory = MutableStateFlow(ServiceCategory.TODOS)
    val selectedServiceCategory: StateFlow<ServiceCategory> = _selectedServiceCategory.asStateFlow()

    val filteredServices: StateFlow<List<ServiceRecord>> = combine(
        repository.serviceHistory,
        _serviceSearchQuery,
        _selectedServiceCategory
    ) { services, query, category ->
        services.filter { record ->
            val matchesCategory = category == ServiceCategory.TODOS || record.category == category
            val matchesQuery = query.isBlank() ||
                    record.title.contains(query, ignoreCase = true) ||
                    record.notes.contains(query, ignoreCase = true) ||
                    record.workshopName.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Voice & Emergency Dialog states
    private val _showVoiceDialog = MutableStateFlow(false)
    val showVoiceDialog: StateFlow<Boolean> = _showVoiceDialog.asStateFlow()

    private val _showEmergencyDialog = MutableStateFlow(false)
    val showEmergencyDialog: StateFlow<Boolean> = _showEmergencyDialog.asStateFlow()

    private val _showCustomizer = MutableStateFlow(false)
    val showCustomizer: StateFlow<Boolean> = _showCustomizer.asStateFlow()

    private val _lastExportedPdfFile = MutableStateFlow<File?>(null)
    val lastExportedPdfFile: StateFlow<File?> = _lastExportedPdfFile.asStateFlow()

    private val _pdfExportSuccessToast = MutableStateFlow<String?>(null)
    val pdfExportSuccessToast: StateFlow<String?> = _pdfExportSuccessToast.asStateFlow()

    // Processed custom motorbike bitmap preview
    private val _customBikePreviewBitmap = MutableStateFlow<Bitmap?>(null)
    val customBikePreviewBitmap: StateFlow<Bitmap?> = _customBikePreviewBitmap.asStateFlow()

    private var rideSimulationJob: Job? = null

    init {
        // Sync Spotify playback with repository mediaState without unsolicited voice spam
        viewModelScope.launch {
            spotifyService.playbackState.collect { state ->
                repository.updateMediaState(
                    title = state.currentTrack.title,
                    artist = state.currentTrack.artist,
                    album = state.currentTrack.album,
                    isPlaying = state.isPlaying,
                    progressFraction = state.progressFraction,
                    currentPosition = state.positionSeconds,
                    duration = state.currentTrack.durationSeconds
                )
            }
        }
    }

    fun selectTab(index: Int) {
        val stack = _tabBackStack.value
        if (stack.lastOrNull() == index) return
        _tabBackStack.value = (stack + index).takeLast(16)
    }

    fun navigateBackTab(): Boolean {
        val stack = _tabBackStack.value
        return if (stack.size > 1) {
            _tabBackStack.value = stack.dropLast(1)
            true
        } else {
            false
        }
    }

    fun setSearchQuery(query: String) {
        _serviceSearchQuery.value = query
    }

    fun setServiceCategory(category: ServiceCategory) {
        _selectedServiceCategory.value = category
    }

    fun setVoiceDialogVisible(visible: Boolean) {
        _showVoiceDialog.value = visible
        if (visible) {
            voiceAssistant.setListening(true)
        } else {
            voiceAssistant.setListening(false)
        }
    }

    fun setEmergencyDialogVisible(visible: Boolean) {
        _showEmergencyDialog.value = visible
    }

    fun setCustomizerVisible(visible: Boolean) {
        _showCustomizer.value = visible
    }

    fun toggleRide() {
        val currentlyRiding = activeTrip.value.isRiding
        val nextRiding = !currentlyRiding
        repository.toggleRide(nextRiding)

        if (nextRiding) {
            voiceAssistant.speak("Modo de conducción iniciado. ¡Buen viaje! Monitoreo de velocidad y presión activado.")
            startRideSimulation()
        } else {
            voiceAssistant.speak("Viaje finalizado. Resumen de telemetría guardado en el historial.")
            rideSimulationJob?.cancel()
            rideSimulationJob = null
        }
    }

    private fun startRideSimulation() {
        rideSimulationJob?.cancel()
        rideSimulationJob = viewModelScope.launch {
            var currentSpeed = 0
            while (activeTrip.value.isRiding) {
                delay(1000)
                // Accelerate smoothly or cruise between 35 and 95 km/h
                val targetSpeed = if (currentSpeed < 45) {
                    currentSpeed + Random.nextInt(5, 12)
                } else {
                    (currentSpeed + Random.nextInt(-4, 6)).coerceIn(30, 105)
                }
                currentSpeed = targetSpeed
                val deltaDistance = (currentSpeed / 3600.0) // km per second

                repository.updateActiveTelemetry(currentSpeed, deltaDistance)

                // Voice speed alert if exceeding 100 km/h
                if (currentSpeed >= 100 && Random.nextFloat() < 0.2f) {
                    voiceAssistant.speak("Alerta: Velocidad actual a $currentSpeed kilómetros por hora.")
                }
            }
        }
    }

    fun executeVoiceCommand(command: String) {
        val result = voiceAssistant.processVoiceCommand(command)
        when (result) {
            is VoiceCommandResult.TriggerEmergency -> {
                _showEmergencyDialog.value = true
            }
            is VoiceCommandResult.CallContact -> {
                _showEmergencyDialog.value = true
            }
            is VoiceCommandResult.CheckTpms -> {
                // Done via speech
            }
            is VoiceCommandResult.CheckService -> {
                selectTab(2) // Go to Taller
            }
            is VoiceCommandResult.CheckSpeed -> {
                val speed = activeTrip.value.currentSpeedKmh
                voiceAssistant.speak("Tu velocidad actual es de $speed kilómetros por hora.")
            }
            is VoiceCommandResult.ToggleMusic -> {
                repository.toggleMediaPlayPause()
            }
            is VoiceCommandResult.NextTrack -> {
                repository.nextMediaTrack()
            }
            is VoiceCommandResult.Unknown -> {
                // Spoken via manager
            }
        }
        _showVoiceDialog.value = false
    }

    fun toggleMedia() {
        spotifyService.togglePlayPause()
    }

    fun nextMediaTrack() {
        spotifyService.nextTrack()
    }

    // Spotify Screen & Music Controls
    fun setSpotifyScreenVisible(visible: Boolean) {
        _showSpotifyScreen.value = visible
    }

    fun playSpotifyTrack(track: SpotifyTrack) {
        spotifyService.playTrack(track)
        voiceAssistant.speak("Reproduciendo ${track.title} de ${track.artist}")
    }

    fun toggleSpotifyPlayPause() {
        spotifyService.togglePlayPause()
    }

    fun nextSpotifyTrack() {
        spotifyService.nextTrack()
    }

    fun previousSpotifyTrack() {
        spotifyService.previousTrack()
    }

    fun seekSpotifyFraction(fraction: Float) {
        spotifyService.seekToFraction(fraction)
    }

    fun toggleSpotifyLike(trackId: String) {
        spotifyService.toggleLike(trackId)
    }

    fun toggleSpotifyShuffle() {
        spotifyService.toggleShuffle()
    }

    fun toggleSpotifyRepeat() {
        spotifyService.toggleRepeat()
    }

    fun switchSpotifyAccount(userId: String) {
        spotifyService.switchAccount(userId)
    }

    fun loginSpotify(usernameOrEmail: String, plan: String = "Spotify Premium") {
        spotifyService.login(usernameOrEmail, plan)
    }

    fun logoutSpotify() {
        spotifyService.logout()
    }

    fun openSpotifyLoginInBrowser() {
        spotifyService.openSpotifyLoginInBrowser()
    }

    fun handleSpotifyRedirect(uri: Uri) {
        spotifyService.handleOAuthRedirectUri(uri)
    }

    // Garage & Motorcycle Selector
    fun setBikeSelectorVisible(visible: Boolean) {
        _showBikeSelectorDialog.value = visible
    }

    fun switchActiveMotorcycle(motoId: String) {
        repository.switchActiveMotorcycle(motoId)
        _showBikeSelectorDialog.value = false
    }

    fun addMotorcycleToGarage(
        nickname: String,
        brandModel: String,
        plateNumber: String,
        odometerKm: Double,
        themeMode: VisualThemeMode = VisualThemeMode.NEON_MAGENTA
    ) {
        val newMoto = MotoProfile(
            id = "moto_${System.currentTimeMillis()}",
            nickname = nickname.ifBlank { "MI MOTO" },
            brandModel = brandModel.ifBlank { "MOTO" },
            plateNumber = plateNumber.ifBlank { "MEX001" },
            currentOdometerKm = odometerKm,
            targetTirePressureFrontPsi = 29.0,
            targetTirePressureRearPsi = 33.0,
            themeMode = themeMode,
            graphicStyle = BikeGraphicStyle.TRANSPARENT_STUDIO_PNG
        )
        repository.addMotorcycle(newMoto, makeActive = true)
        _showBikeSelectorDialog.value = false
    }

    fun deleteMotorcycleFromGarage(motoId: String) {
        repository.deleteMotorcycle(motoId)
    }

    // Fuel CRUD & Estimated Efficiency (Silenced TTS during typing/editing)
    fun updateEstimatedEfficiency(newKmPerL: Double) {
        repository.updateFuelStatsEstimatedEfficiency(newKmPerL)
    }

    fun updateFuelEntry(entry: FuelEntry) {
        repository.updateFuelEntry(entry)
    }

    fun deleteFuelEntry(entryId: String) {
        repository.deleteFuelEntry(entryId)
    }

    // Emergency Contact CRUD (Silenced TTS during typing/editing)
    fun addEmergencyContact(
        name: String,
        phone: String,
        relationship: String,
        isPrimary: Boolean,
        voiceAlias: String = ""
    ) {
        val newContact = EmergencyContact(
            id = "em_${System.currentTimeMillis()}",
            name = name,
            phone = phone,
            relationship = relationship,
            isPrimary = isPrimary,
            voiceCommandAlias = voiceAlias.ifBlank { name.split(" ").firstOrNull() ?: "" }
        )
        repository.addEmergencyContact(newContact)
    }

    fun updateEmergencyContact(contact: EmergencyContact) {
        repository.updateEmergencyContact(contact)
    }

    fun deleteEmergencyContact(contactId: String) {
        repository.deleteEmergencyContact(contactId)
    }

    fun triggerSimulatedTpmsWarning() {
        // Drops rear tire to 23 PSI to demonstrate voice alert and push notification
        repository.updateTpmsPressure(28.9, 23.2)
        voiceAssistant.speak("¡Advertencia crítica! Presión de neumático trasero ha caído a 23 PSI. Reduzca la velocidad.")
        notificationHelper.sendTpmsWarning("Neumático Trasero", 23.2)
    }

    fun resetTpmsToNormal() {
        repository.updateTpmsPressure(29.4, 32.8)
    }

    fun updateOdometer(newKm: Double) {
        repository.updateOdometer(newKm)
    }

    fun addNewService(title: String, category: ServiceCategory, costMxn: Double, notes: String) {
        val newRecord = ServiceRecord(
            id = "srv_${System.currentTimeMillis()}",
            dateDisplay = "Hoy, " + java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date()),
            odometerKm = profile.value.currentOdometerKm,
            category = category,
            title = title,
            costMxn = costMxn,
            notes = notes
        )
        repository.addServiceRecord(newRecord)
    }

    fun addNewFuelEntry(liters: Double, costMxn: Double) {
        val newEntry = FuelEntry(
            id = "fuel_${System.currentTimeMillis()}",
            dateDisplay = "Hoy, " + java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
            liters = liters,
            costMxn = costMxn,
            odometerKm = profile.value.currentOdometerKm
        )
        repository.addFuelEntry(newEntry)
    }

    fun exportMaintenancePdfReport() {
        val file = pdfExporter.generateMaintenancePdf(
            profile = profile.value,
            intervals = componentIntervals.value,
            records = repository.serviceHistory.value
        )
        if (file != null) {
            _lastExportedPdfFile.value = file
            _pdfExportSuccessToast.value = "Reporte PDF generado: ${file.name}"
            pdfExporter.sharePdfReport(file)
        } else {
            _pdfExportSuccessToast.value = "Error al generar archivo PDF."
        }
    }

    fun clearPdfToast() {
        _pdfExportSuccessToast.value = null
    }

    fun sendUrgentMaintenancePushNotification() {
        val urgentInterval = componentIntervals.value.firstOrNull { it.isUrgent }
            ?: componentIntervals.value.first()

        notificationHelper.sendMaintenanceAlert(
            title = "Recordatorio Urgente: ${urgentInterval.name}",
            message = "Faltan solo ${urgentInterval.remainingKm.toInt()} km para el mantenimiento requerido. Agende cita en su taller de confianza."
        )
    }

    fun updateTheme(themeMode: VisualThemeMode) {
        repository.updateProfile { it.copy(themeMode = themeMode) }
    }

    fun updateBikeDetails(nickname: String, model: String, graphicStyle: BikeGraphicStyle) {
        repository.updateProfile {
            it.copy(
                nickname = nickname,
                brandModel = model,
                graphicStyle = graphicStyle
            )
        }
    }

    fun saveProcessedCutoutBitmap(processed: Bitmap, style: BikeGraphicStyle = BikeGraphicStyle.TRANSPARENT_STUDIO_PNG) {
        viewModelScope.launch {
            _customBikePreviewBitmap.value = processed
            val savedPath = imageProcessor.saveProcessedBitmap(processed)
            if (savedPath != null) {
                repository.updateProfile {
                    it.copy(
                        customImageUri = savedPath,
                        graphicStyle = style
                    )
                }
            }
        }
    }

    fun processCustomMotorcycleImage(imageUri: Uri, style: BikeGraphicStyle) {
        viewModelScope.launch {
            val bitmap = imageProcessor.loadBitmapFromUri(imageUri)
            if (bitmap != null) {
                val accentColor = android.graphics.Color.parseColor(profile.value.themeMode.hexAccent)
                val processed = imageProcessor.processMotorcycleImage(bitmap, style, accentColor)
                saveProcessedCutoutBitmap(processed, style)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceAssistant.shutdown()
        rideSimulationJob?.cancel()
    }
}
