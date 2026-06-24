package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.speech.SpeechRecognizer
import android.speech.RecognizerIntent
import android.speech.RecognitionListener
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import android.content.Context

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkModeState by remember { mutableStateOf<Boolean?>(null) }
            val systemDark = isSystemInDarkTheme()
            val isDark = isDarkModeState ?: systemDark

            MyApplicationTheme(darkTheme = isDark) {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    LandingPageScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        isDarkMode = isDark,
                        onThemeToggle = {
                            isDarkModeState = !isDark
                        }
                    )
                }
            }
        }
    }
}

// Data holder for active service requests displayed in the ticket dashboard
data class ServiceRequest(
    val id: String,
    val title: String,
    val priority: String, // "P1 - Critical", "P2 - High", "P3 - Moderate", "P4 - Low"
    val category: String,
    val status: String,   // "In Progress" or "Resolved"
    val site: String,
    val clientNotes: String,
    val updatedTime: String,
    val technicianNotes: String,
    val timestamp: String,
    val customerContact: String
)

// Data holder for mock ServiceNow texts depending on language selection
data class ServiceNowStrings(
    val appTitle: String,
    val ticketDetails: String,
    val statusLabel: String,
    val clientNotesLabel: String,
    val resolveButton: String,
    val statusInProgress: String,
    val statusResolved: String,
    val statusResolving: String,
    val clientNotesText: String,
    val dialogTitle: String,
    val selectCodeLabel: String,
    val resolutionNotesLabel: String,
    val cancelLabel: String,
    val submitLabel: String,
    val successText: String,
    val priorityLabel: String,
    val priorityValue: String,
    val categoryLabel: String,
    val categoryValue: String,
    val siteLabel: String,
    val techIdLabel: String
)

private val localizedStrings = mapOf(
    "EN" to ServiceNowStrings(
        appTitle = "D Graham support",
        ticketDetails = "ID: INC-2026-9904",
        statusLabel = "Status",
        clientNotesLabel = "ServiceNow Client Notes",
        resolveButton = "Resolve Ticket",
        statusInProgress = "In Progress",
        statusResolved = "Resolved",
        statusResolving = "Syncing...",
        clientNotesText = "Server rack 4B router needs manual fiber optic line termination at site #12.",
        dialogTitle = "Resolve ServiceNow Ticket",
        selectCodeLabel = "Resolution Code",
        resolutionNotesLabel = "Internal Resolution Notes",
        cancelLabel = "Cancel",
        submitLabel = "Submit Resolution",
        successText = "Ticket status synchronized securely.",
        priorityLabel = "Priority",
        priorityValue = "P1 - Critical",
        categoryLabel = "Issue Type",
        categoryValue = "Hardware Provisioning",
        siteLabel = "Site Node",
        techIdLabel = "Technician ID"
    ),
    "ES" to ServiceNowStrings(
        appTitle = "servicio D Graham",
        ticketDetails = "ID: INC-2026-9904",
        statusLabel = "Estado",
        clientNotesLabel = "Notas de Cliente ServiceNow",
        resolveButton = "Resolver Incidente",
        statusInProgress = "En Progreso",
        statusResolved = "Resuelto",
        statusResolving = "Enviando...",
        clientNotesText = "El enrutador del rack de servidores 4B necesita terminación manual de línea de fibra óptica en el sitio #12.",
        dialogTitle = "Resolver Ticket ServiceNow",
        selectCodeLabel = "Código de Resolución",
        resolutionNotesLabel = "Notas de Resolución Internas",
        cancelLabel = "Cancelar",
        submitLabel = "Enviar Resolución",
        successText = "Estado del ticket sincronizado de forma segura.",
        priorityLabel = "Prioridad",
        priorityValue = "P1 - Crítico",
        categoryLabel = "Tipo de Incidencia",
        categoryValue = "Suministro de Hardware",
        siteLabel = "Nodo del Sitio",
        techIdLabel = "ID del Técnico"
    ),
    "FR" to ServiceNowStrings(
        appTitle = "centre D Graham",
        ticketDetails = "ID: INC-2026-9904",
        statusLabel = "Statut",
        clientNotesLabel = "Notes Client ServiceNow",
        resolveButton = "Résoudre l'Incident",
        statusInProgress = "En Cours",
        statusResolved = "Résolu",
        statusResolving = "Transmission...",
        clientNotesText = "Le routeur de la baie de serveurs 4B nécessite une terminaison manuelle de la ligne de fibre optique sur le site #12.",
        dialogTitle = "Résoudre un Ticket ServiceNow",
        selectCodeLabel = "Code de Résolution",
        resolutionNotesLabel = "Notes de Résolution Internes",
        cancelLabel = "Annuler",
        submitLabel = "Soumettre Résolution",
        successText = "Statut du ticket synchronisé en toute sécurité.",
        priorityLabel = "Priorité",
        priorityValue = "P1 - Critique",
        categoryLabel = "Type d'Incident",
        categoryValue = "Raccordement Matériel",
        siteLabel = "Nœud du Site",
        techIdLabel = "ID du Technicien"
    )
)

@Composable
fun LandingPageScreen(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = LocalThemeDark.current,
    onThemeToggle: () -> Unit = {}
) {
    val theme = getThemeColors(isDarkMode)
    val CleanWhite = theme.CleanWhite
    val SlateNavy = theme.SlateNavy
    val SlateSubtle = theme.SlateSubtle
    val BorderSlate = theme.BorderSlate
    val LightBackground = theme.LightBackground
    val UnisysBlue = theme.UnisysBlue
    val AccentTechBlue = theme.AccentTechBlue

    val context = LocalContext.current
    var recentSubmittedTicketsState by remember {
        mutableStateOf(loadRecentTicketsFromStorage(context))
    }

    val coroutineScope = rememberCoroutineScope()
    var systemLanguage by remember { mutableStateOf("EN") }
    var activeTicketsState by remember {
        val initialRecent = loadRecentTicketsFromStorage(context)
        val defaultList = listOf(
            ServiceRequest(
                    id = "INC-2026-9904",
                    title = "Server rack 4B router manual termination",
                    priority = "P1 - Critical",
                    category = "Hardware Provisioning",
                    status = "In Progress",
                    site = "D Graham Node #4592",
                    clientNotes = "Server rack 4B router needs manual fiber optic line termination at site #12.",
                    updatedTime = "10m ago",
                    technicianNotes = "Manual fiber optic alignment required. Splice tray #3 is saturated. Signal calibration is on-hold until splicing is verified.",
                    timestamp = "2026-06-22 11:10:45 UTC",
                    customerContact = "Sarah Jenkins (sjenkins@dgraham-networks.com) - +1 (555) 014-9904"
                ),
                ServiceRequest(
                    id = "INC-2026-9912",
                    title = "Backup generator auto-start failure",
                    priority = "P1 - Critical",
                    category = "Power Systems",
                    status = "In Progress",
                    site = "D Graham Node #1024",
                    clientNotes = "Generator failed dual automation grid checks during test cycle.",
                    updatedTime = "45m ago",
                    technicianNotes = "Tested relays and confirmed solenoid coil malfunction. Replacement coil is dispatched. Site cooling system must hold secondary loads.",
                    timestamp = "2026-06-22 10:35:12 UTC",
                    customerContact = "Marcus Brody (mbrody@dgraham-power.com) - +1 (555) 017-9912"
                ),
                ServiceRequest(
                    id = "INC-2026-9920",
                    title = "Edge site database replication lag",
                    priority = "P2 - High",
                    category = "Database Operations",
                    status = "In Progress",
                    site = "D Graham Node #9011",
                    clientNotes = "Replication backlog exceeded 500k records. High risk of split-brain.",
                    updatedTime = "1h ago",
                    technicianNotes = "Primary node storage buffer filled up due to rapid ingestion rate. Clearing old cache tables and throttling temporary staging write locks.",
                    timestamp = "2026-06-22 10:20:00 UTC",
                    customerContact = "Lina Chen (lchen@dgraham-data.com) - +1 (555) 019-9920"
                ),
                ServiceRequest(
                    id = "INC-2026-9935",
                    title = "Fiber-optic backbone signal drop",
                    priority = "P3 - Moderate",
                    category = "Telecommunications",
                    status = "Resolved",
                    site = "D Graham Node #3312",
                    clientNotes = "Sub-sea optical cable carrier reported 3dB path attenuation.",
                    updatedTime = "4h ago",
                    technicianNotes = "Backbone diversion route verified. Attenuation normalized down to 0.4dB. Path verified via OTDR diagnostics sweep.",
                    timestamp = "2026-06-22 07:15:33 UTC",
                    customerContact = "Darrin Vance (dvance@dgraham-telecom.com) - +1 (555) 012-9935"
                ),
                ServiceRequest(
                    id = "INC-2026-9941",
                    title = "Air handler unit #2 compressor stall",
                    priority = "P4 - Low",
                    category = "HVAC & Facilities",
                    status = "In Progress",
                    site = "D Graham Node #0056",
                    clientNotes = "High compressor winding temperature detected during continuous peak load.",
                    updatedTime = "5h ago",
                    technicianNotes = "HVAC unit #2 auxiliary fan power cycle completed. Temperature trending down (now 62C down from 85C). Thermal limits are stable.",
                    timestamp = "2026-06-22 06:10:22 UTC",
                    customerContact = "Patricia Miller (pmiller@dgraham-hq.com) - +1 (555) 011-9941"
                )
            )
            mutableStateOf(initialRecent + defaultList)
        }
    var selectedTicketId by remember { mutableStateOf("INC-2026-9904") }
    var simulatorViewMode by remember { mutableStateOf("dashboard") } // default to "dashboard" to show off the ticker dashboard view immediately

    val currentSelectedTicket = activeTicketsState.find { it.id == selectedTicketId } ?: activeTicketsState[0]
    val ticketStatusState = currentSelectedTicket.status
    var offlineModeEnabled by remember { mutableStateOf(false) }
    var localBufferedQueues by remember { mutableStateOf(0) }
    
    // Live synchronization terminal events
    var syncHistoryList by remember {
        mutableStateOf(
            listOf(
                "11:08:15 [CONN] Secured VPN FIPS 140-2 established and tunneling.",
                "11:08:16 [SYNC] Listening active. Mirrored to ServiceNow node #4592.",
                "11:08:18 [READY] Enterprise credentials verified globally."
            )
        )
    }

    // Access Invitation States
    var showRequestAccessDialog by remember { mutableStateOf(false) }
    var companyMailField by remember { mutableStateOf("") }
    var isSubmittedSuccess by remember { mutableStateOf(false) }

    // Diagnostic Scanner States
    var diagnosticIntegrityChecked by remember { mutableStateOf(false) }
    var isRunningDiagnosticScanner by remember { mutableStateOf(false) }
    var diagnosticSecurityStatusText by remember { mutableStateOf("Ready to initiate FIPS handshake scan.") }

    // App Download simulated states
    var isDownloadingApp by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }

    fun addSyncLog(message: String) {
        val timeStamp = "11:09:${(10..59).random()}"
        syncHistoryList = syncHistoryList + "$timeStamp $message"
    }

    val onDownloadAppClick: () -> Unit = {
        if (!isDownloadingApp) {
            coroutineScope.launch {
                isDownloadingApp = true
                downloadProgress = 0f
                addSyncLog("📥 [DOWNLOAD] Initiated secure download of ServiceNow Integration Client APK...")
                while (downloadProgress < 1f) {
                    delay(150)
                    downloadProgress += 0.1f
                    if (downloadProgress > 1f) downloadProgress = 1f
                }
                addSyncLog("✓ [DOWNLOAD] Download complete. dgraham_field_hub_v2.1.4.apk saved to local storage.")
                Toast.makeText(context, "Download complete: dgraham_field_hub_v2.1.4.apk successfully saved.", Toast.LENGTH_LONG).show()
                isDownloadingApp = false
            }
        }
    }

    val onAddTicket: (String, String, String, String, String, String, String) -> Unit = { title, category, priority, techId, site, notes, contact ->
        val newId = "INC-2026-${(1000..9999).random()}"
        val newTicket = ServiceRequest(
            id = newId,
            title = title,
            priority = priority,
            category = category,
            status = "In Progress",
            site = site,
            clientNotes = notes,
            updatedTime = "Just now",
            technicianNotes = "Awaiting deployment of technician $techId.",
            timestamp = "2026-06-23 13:58:00 UTC",
            customerContact = contact
        )
        activeTicketsState = listOf(newTicket) + activeTicketsState
        selectedTicketId = newId
        val updatedRecent = (listOf(newTicket) + recentSubmittedTicketsState).take(5)
        recentSubmittedTicketsState = updatedRecent
        saveRecentTicketsToStorage(context, updatedRecent)
        if (offlineModeEnabled) {
            localBufferedQueues += 1
            addSyncLog("⚠️ [QUEUE] Queued new incident $newId locally. Tech ID: $techId, Issue Type: $category.")
        } else {
            addSyncLog("✓ [CREATE] Dispatched new incident $newId directly to ServiceNow. Tech: $techId, Category: $category.")
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .background(LightBackground)
            .fillMaxSize()
    ) {
        val widthDp = maxWidth
        val isWideLayout = widthDp >= 760.dp

        // Stylish Tech-related background image with gradient overlay in the upper field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isWideLayout) 750.dp else 1120.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_tech_background_1782321423534),
                contentDescription = "Cybersecurity professional desk banner background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                if (isDarkMode) Color(0x220F172A) else Color(0x22FFFFFF),
                                if (isDarkMode) Color(0xAA0F172A) else Color(0xAAFFFFFF),
                                if (isDarkMode) LightBackground else LightBackground
                            )
                        )
                    )
            )
        }

        // Ambient background circuit aesthetics
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .drawBehind {
                    // Modern grid mesh of dots for high fidelity enterprise feel
                    val rows = 40
                    val cols = 30
                    val gapX = size.width / cols
                    val gapY = size.height / rows
                    for (r in 0..rows) {
                        for (c in 0..cols) {
                            drawCircle(
                                color = if (isDarkMode) Color(0x0A94A3B8) else Color(0x060A3E72),
                                radius = 2.dp.toPx(),
                                center = Offset(c * gapX, r * gapY)
                            )
                        }
                    }
                }
        ) {
            // Enterprise Global Corporate Top Brand Bar
            CorporateHeroNavigationBar(
                systemLanguage = systemLanguage,
                onLanguageChange = { systemLanguage = it },
                onRequestAccessClicked = { showRequestAccessDialog = true },
                isDarkMode = isDarkMode,
                onThemeToggle = onThemeToggle
            )

            // Split screen or compact layout container
            if (isWideLayout) {
                // Wide Desktop View
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column (Text & Benefits Pitch)
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .padding(end = 32.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        HeroPromoBenefitBlock(
                            onRequestClicked = { showRequestAccessDialog = true },
                            onDownloadClicked = onDownloadAppClick,
                            isDownloading = isDownloadingApp,
                            downloadProgress = downloadProgress
                        )
                    }

                    // Right Column (Interactive Smartphone Simulator)
                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SmartphoneSimulatorContainer(
                            systemLanguage = systemLanguage,
                            onLanguageChange = { systemLanguage = it },
                            ticketStatusState = ticketStatusState,
                            onStatusChange = { newStatus ->
                                activeTicketsState = activeTicketsState.map {
                                    if (it.id == selectedTicketId) it.copy(status = newStatus) else it
                                }
                                if (newStatus == "Resolved") {
                                    if (offlineModeEnabled) {
                                        localBufferedQueues += 1
                                        addSyncLog("⚠️ [QUEUE] Saved local resolution of $selectedTicketId. Queue count: $localBufferedQueues")
                                    } else {
                                        addSyncLog("✓ [RESOLVE] Dispatched $selectedTicketId resolved. Sync successful.")
                                    }
                                } else {
                                    addSyncLog("↩ [REOPEN] Reopened ServiceNow $selectedTicketId.")
                                }
                            },
                            offlineModeEnabled = offlineModeEnabled,
                            onAddLogMessage = { addSyncLog(it) },
                            activeTickets = activeTicketsState,
                            selectedTicketId = selectedTicketId,
                            onSelectedTicketChange = { selectedTicketId = it },
                            simulatorViewMode = simulatorViewMode,
                            onSimulatorViewModeChange = { simulatorViewMode = it },
                            onAddTicket = onAddTicket
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        RecentTicketsSection(
                            recentTickets = recentSubmittedTicketsState,
                            onTicketClick = { clickedTicket ->
                                if (!activeTicketsState.any { it.id == clickedTicket.id }) {
                                    activeTicketsState = listOf(clickedTicket) + activeTicketsState
                                }
                                selectedTicketId = clickedTicket.id
                                simulatorViewMode = "details"
                            },
                            isDarkMode = isDarkMode
                        )
                    }
                }
            } else {
                // Compact Layout - Collapsed Mobile-First "App-First" view
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header promo still at top
                    HeroHeaderCompactText(
                        onDownloadClicked = onDownloadAppClick,
                        isDownloading = isDownloadingApp,
                        downloadProgress = downloadProgress
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Prominent central phone mockup
                    SmartphoneSimulatorContainer(
                        systemLanguage = systemLanguage,
                        onLanguageChange = { systemLanguage = it },
                        ticketStatusState = ticketStatusState,
                        onStatusChange = { newStatus ->
                            activeTicketsState = activeTicketsState.map {
                                if (it.id == selectedTicketId) it.copy(status = newStatus) else it
                            }
                            if (newStatus == "Resolved") {
                                if (offlineModeEnabled) {
                                    localBufferedQueues += 1
                                    addSyncLog("⚠️ [QUEUE] Saved local resolution of $selectedTicketId. Queue count: $localBufferedQueues")
                                } else {
                                    addSyncLog("✓ [RESOLVE] Dispatched $selectedTicketId resolved. Sync successful.")
                                }
                            } else {
                                    addSyncLog("↩ [REOPEN] Reopened ServiceNow $selectedTicketId.")
                            }
                        },
                        offlineModeEnabled = offlineModeEnabled,
                        onAddLogMessage = { addSyncLog(it) },
                        activeTickets = activeTicketsState,
                        selectedTicketId = selectedTicketId,
                        onSelectedTicketChange = { selectedTicketId = it },
                        simulatorViewMode = simulatorViewMode,
                        onSimulatorViewModeChange = { simulatorViewMode = it },
                        onAddTicket = onAddTicket
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    RecentTicketsSection(
                        recentTickets = recentSubmittedTicketsState,
                        onTicketClick = { clickedTicket ->
                            if (!activeTicketsState.any { it.id == clickedTicket.id }) {
                                activeTicketsState = listOf(clickedTicket) + activeTicketsState
                            }
                            selectedTicketId = clickedTicket.id
                            simulatorViewMode = "details"
                        },
                        isDarkMode = isDarkMode
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Access CTA below mockup
                    RequestAccessFloatingTriggerCard(
                        onRequestClicked = { showRequestAccessDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Below the Fold: Features Section & Interactive Panels
            FeaturesSectionBelowFold(
                offlineModeEnabled = offlineModeEnabled,
                onOfflineModeToggle = { toggledOffline ->
                    offlineModeEnabled = toggledOffline
                    if (toggledOffline) {
                        addSyncLog("📡 [OFFLINE] Network disconnected. Running isolated secure workspace cache.")
                    } else {
                        addSyncLog("📡 [ONLINE] Network re-established. Syncing background cache...")
                        if (localBufferedQueues > 0) {
                            addSyncLog("✓ [SYNC] Pushed $localBufferedQueues queued ticket updates successfully to ServiceNow.")
                            localBufferedQueues = 0
                        }
                    }
                },
                localBufferedQueues = localBufferedQueues,
                syncHistoryList = syncHistoryList,
                diagnosticIntegrityChecked = diagnosticIntegrityChecked,
                isRunningDiagnosticScanner = isRunningDiagnosticScanner,
                diagnosticSecurityStatusText = diagnosticSecurityStatusText,
                onInitiateScanner = {
                    coroutineScope.launch {
                        isRunningDiagnosticScanner = true
                        diagnosticSecurityStatusText = "Initializing secure integrity assessment..."
                        delay(800)
                        diagnosticSecurityStatusText = "Verifying FIPS 140-2 Tunnel Certificates..."
                        delay(1000)
                        diagnosticSecurityStatusText = "Pinging ServiceNow secure endpoints... 24ms."
                        delay(800)
                        diagnosticSecurityStatusText = "AES-GCM-256 local ledger key check: VALID."
                        delay(600)
                        diagnosticIntegrityChecked = true
                        isRunningDiagnosticScanner = false
                        diagnosticSecurityStatusText = "System verified. SHA checksum: D23A4B9F..."
                        addSyncLog("🛡️ [SECURITY] Client hardware compliance signature scan passed.")
                    }
                },
                onClearTerminal = {
                    syncHistoryList = listOf("console log cleared. VPN tunneling actively listening.")
                }
            )

            // Trust Footer
            DGrahamEnterpriseFooter()
        }

        // High-Conversion Invitation Dialog
        if (showRequestAccessDialog) {
            SecureRequestAccessDialog(
                companyMail = companyMailField,
                onMailChange = { companyMailField = it },
                isSubmittingSuccess = isSubmittedSuccess,
                onDismiss = {
                    showRequestAccessDialog = false
                    isSubmittedSuccess = false
                },
                onSubmit = {
                    coroutineScope.launch {
                        addSyncLog("🛡️ [AUTH] Access request initiated for: $companyMailField")
                        isSubmittedSuccess = true
                    }
                }
            )
        }
    }
}

// Top navigation banner with enterprise branding and clean CTA
@Composable
fun CorporateHeroNavigationBar(
    systemLanguage: String,
    onLanguageChange: (String) -> Unit,
    onRequestAccessClicked: () -> Unit,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = getThemeColors(isDarkMode)
    val CleanWhite = theme.CleanWhite
    val SlateNavy = theme.SlateNavy
    val SlateSubtle = theme.SlateSubtle
    val BorderSlate = theme.BorderSlate
    val LightBackground = theme.LightBackground
    val UnisysBlue = theme.UnisysBlue
    val AccentTechBlue = theme.AccentTechBlue

    val navSubTitleText = when(systemLanguage) {
        "ES" -> "Portal de Operaciones de Campo"
        "FR" -> "Portail des Opérations de Terrain"
        else -> "Field Operations Portal"
    }

    val fipsLabelText = when(systemLanguage) {
        "ES" -> "SEGURO FIPS 140-2"
        "FR" -> "SÉCURISÉ FIPS 140-2"
        else -> "FIPS 140-2 SECURE"
    }

    val navButtonText = when(systemLanguage) {
        "ES" -> "Solicitar Acceso"
        "FR" -> "Demander l'Accès"
        else -> "Request Access"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CleanWhite),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Large styled corporate "D" logo
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(UnisysBlue, AccentTechBlue)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "D",
                        color = CleanWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif
                    )
                }
                
                Column {
                    Text(
                        text = "D GRAHAM",
                        color = SlateNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = navSubTitleText,
                        color = SlateSubtle,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Highly Accessible Dynamic Segmented Language Switcher Component
                Row(
                    modifier = Modifier
                        .background(if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val languages = listOf(
                        "EN" to "🇺🇸 EN",
                        "ES" to "🇪🇸 ES",
                        "FR" to "🇫🇷 FR"
                    )
                    languages.forEach { (langCode, langLabel) ->
                        val isSelected = systemLanguage == langCode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) UnisysBlue else Color.Transparent)
                                .clickable { onLanguageChange(langCode) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("lang_switch_$langCode")
                        ) {
                            Text(
                                text = langLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) CleanWhite else SlateSubtle
                            )
                        }
                    }
                }

                // Highly Accessible Theme Switcher Segmented Control
                Row(
                    modifier = Modifier
                        .background(if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Light mode option
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isDarkMode) UnisysBlue else Color.Transparent)
                            .clickable { if (isDarkMode) onThemeToggle() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("theme_switch_light")
                    ) {
                        Text(
                            text = "☀️ LIGHT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isDarkMode) CleanWhite else SlateSubtle
                        )
                    }
                    // Dark mode option
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDarkMode) UnisysBlue else Color.Transparent)
                            .clickable { if (!isDarkMode) onThemeToggle() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("theme_switch_dark")
                    ) {
                        Text(
                            text = "🌙 DARK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) CleanWhite else SlateSubtle
                        )
                    }
                }

                // Trusted security seal badge - Desktop visible
                Surface(
                    color = if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "FIPS Authentication",
                            tint = UnisysBlue,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = fipsLabelText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = UnisysBlue
                        )
                    }
                }

                Button(
                    onClick = onRequestAccessClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTechBlue),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("nav_request_access_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock icon",
                        tint = CleanWhite,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = navButtonText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Hero Promotional Text Block (Left Side in Desktop)
@Composable
fun HeroPromoBenefitBlock(
    onRequestClicked: () -> Unit,
    onDownloadClicked: () -> Unit,
    isDownloading: Boolean,
    downloadProgress: Float,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeDark.current
    val theme = getThemeColors(isDark)
    val CleanWhite = theme.CleanWhite
    val SlateNavy = theme.SlateNavy
    val SlateSubtle = theme.SlateSubtle
    val BorderSlate = theme.BorderSlate
    val LightBackground = theme.LightBackground
    val UnisysBlue = theme.UnisysBlue
    val AccentTechBlue = theme.AccentTechBlue

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            color = Color(0xFFEFF6FF),
            shape = RoundedCornerShape(32.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Verified status",
                    tint = AccentTechBlue,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MILITARY-GRADE SECURITY PROTOCOLS",
                    color = AccentTechBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        Text(
            text = "Field Tech Hub",
            style = TextStyle(
                fontSize = 42.sp,
                lineHeight = 50.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SlateNavy,
                letterSpacing = (-1).sp
            )
        )

        Text(
            text = "Interactive D Graham Mobile App Simulator",
            color = AccentTechBlue,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Text(
            text = "Specially engineered for D Graham field operations. Interact with the high-fidelity mobile app simulator on the right to test status changes, view customer contacts, and run offline cache sync tests. Access the secure request form below to submit security credential proposals.",
            fontSize = 15.sp,
            lineHeight = 23.sp,
            color = SlateSubtle,
            fontWeight = FontWeight.Normal
        )

        Divider(color = BorderSlate, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

        // High conversion structural bullets
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BenefitBulletPoint(
                title = "Interactive Mobile App Simulator",
                desc = "Test live state transitions, review internal technician notes, and inspect precise event timestamps."
            )
            BenefitBulletPoint(
                title = "Secure Access Request Form",
                desc = "Submit supervisor badges and regional credentials via a military-grade secure portal."
            )
            BenefitBulletPoint(
                title = "FIPS Offline Vault Sync",
                desc = "Log critical incident updates even during network drop simulations. Sync local cache queues automatically."
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onRequestClicked,
                colors = ButtonDefaults.buttonColors(containerColor = UnisysBlue),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("hero_request_access_btn")
            ) {
                Text(
                    text = "Open Secure Request Form",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Forward arrow",
                    tint = CleanWhite,
                    modifier = Modifier.size(16.dp)
                )
            }

            OutlinedButton(
                onClick = onDownloadClicked,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = UnisysBlue),
                border = BorderStroke(1.5.dp, UnisysBlue),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                shape = RoundedCornerShape(10.dp),
                enabled = !isDownloading,
                modifier = Modifier.testTag("hero_download_app_btn")
            ) {
                if (isDownloading) {
                    CircularProgressIndicator(
                        progress = downloadProgress,
                        modifier = Modifier.size(16.dp),
                        color = UnisysBlue,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Downloading (${(downloadProgress * 100).toInt()}%)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Download app icon",
                        tint = UnisysBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Download App",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Compact version of top promotional copy for smaller screens
@Composable
fun HeroHeaderCompactText(
    onDownloadClicked: () -> Unit,
    isDownloading: Boolean,
    downloadProgress: Float,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeDark.current
    val theme = getThemeColors(isDark)
    val CleanWhite = theme.CleanWhite
    val SlateNavy = theme.SlateNavy
    val SlateSubtle = theme.SlateSubtle
    val BorderSlate = theme.BorderSlate
    val LightBackground = theme.LightBackground
    val UnisysBlue = theme.UnisysBlue
    val AccentTechBlue = theme.AccentTechBlue

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            color = Color(0xFFEFF6FF),
            shape = RoundedCornerShape(32.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Verified status",
                    tint = AccentTechBlue,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SECURE PROTOCOLS ACTIVE",
                    color = AccentTechBlue,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Text(
            text = "Field Tech Hub",
            fontSize = 32.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SlateNavy,
            textAlign = TextAlign.Center
        )

        Text(
            text = "D Graham Mobile App Simulator",
            color = AccentTechBlue,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Interact with the high-fidelity D Graham simulator below to test live status resolutions, view technician details, and run offline cache sync trials. Use the secure form to submit authorization requests.",
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = SlateSubtle,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onDownloadClicked,
            colors = ButtonDefaults.buttonColors(containerColor = UnisysBlue),
            shape = RoundedCornerShape(8.dp),
            enabled = !isDownloading,
            modifier = Modifier.testTag("compact_download_app_btn")
        ) {
            if (isDownloading) {
                CircularProgressIndicator(
                    progress = downloadProgress,
                    modifier = Modifier.size(16.dp),
                    color = CleanWhite,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Downloading (${(downloadProgress * 100).toInt()}%)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Download app icon",
                    tint = CleanWhite,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Download Android App",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Floating card to trigger access below simulator on compact view
@Composable
fun RequestAccessFloatingTriggerCard(
    onRequestClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeDark.current
    val theme = getThemeColors(isDark)
    val CleanWhite = theme.CleanWhite
    val SlateNavy = theme.SlateNavy
    val SlateSubtle = theme.SlateSubtle
    val BorderSlate = theme.BorderSlate
    val LightBackground = theme.LightBackground
    val UnisysBlue = theme.UnisysBlue
    val AccentTechBlue = theme.AccentTechBlue

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = UnisysBlue),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Register with Field Tech Hub",
                color = CleanWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Submit our secure request form to register your technician hardware key and run the live simulator in secure mode.",
                color = Color(0xFF90CDF4),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRequestClicked,
                colors = ButtonDefaults.buttonColors(containerColor = CleanWhite, contentColor = UnisysBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("compact_cta_button")
            ) {
                Text(
                    text = "Open Secure Request Form",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// Custom checkmark benefit graphics row
@Composable
fun BenefitBulletPoint(title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
                .background(Color(0xFFDCFCE7), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Checked icon",
                tint = ServiceGreen,
                modifier = Modifier.size(11.dp)
            )
        }
        Column {
            Text(
                text = title,
                color = SlateNavy,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = desc,
                color = SlateSubtle,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

// High-Fidelity Phone Chassis enclosing the interactive client simulator
@Composable
fun SmartphoneSimulatorContainer(
    systemLanguage: String,
    onLanguageChange: (String) -> Unit,
    ticketStatusState: String,
    onStatusChange: (String) -> Unit,
    offlineModeEnabled: Boolean,
    onAddLogMessage: (String) -> Unit,
    activeTickets: List<ServiceRequest>,
    selectedTicketId: String,
    onSelectedTicketChange: (String) -> Unit,
    simulatorViewMode: String,
    onSimulatorViewModeChange: (String) -> Unit,
    onAddTicket: (String, String, String, String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeDark.current
    val theme = getThemeColors(isDark)
    val CleanWhite = theme.CleanWhite
    val SlateNavy = theme.SlateNavy
    val SlateSubtle = theme.SlateSubtle
    val BorderSlate = theme.BorderSlate
    val LightBackground = theme.LightBackground
    val UnisysBlue = theme.UnisysBlue
    val AccentTechBlue = theme.AccentTechBlue

    // Smartphone Outer Bezel
    Box(
        modifier = modifier
            .width(310.dp)
            .shadow(16.dp, RoundedCornerShape(38.dp))
            .background(Color(0xFF1E293B), RoundedCornerShape(38.dp)) // Carbon charcoal phone body
            .border(4.dp, Color(0xFF475569), RoundedCornerShape(38.dp)) // Chrome matte edge
            .padding(8.dp) // Phone case separation
    ) {
        // Inner Glass Screen Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
            border = BorderStroke(1.dp, Color(0xFF0F172A))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Smartphone Top Notch Housing Area & Mock OS status bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateNavy)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    // Left: Carrier network details
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (offlineModeEnabled) Color.Red else ServiceGreen, CircleShape)
                        )
                        Text(
                            text = if (offlineModeEnabled) "Isolated Cache" else "D Graham Secure",
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Center: Physical Top Notch Ear Speaker & Screen Camera
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color(0xFF020617), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Speaker pill
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(3.dp)
                                .background(Color(0xFF334155), CircleShape)
                        )
                        // Camera lens dot
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(Color(0xFF1E3A8A), CircleShape)
                        )
                    }

                    // Right: Custom Cellular Signal drawing & Battery text
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "VPN Active Seal",
                            tint = Color(0xFF90CDF4),
                            modifier = Modifier.size(10.dp)
                        )
                        
                        // Custom vector signal bars (using lightweight Row of blocks to guarantee compiling)
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(1.dp),
                            modifier = Modifier.padding(bottom = 1.dp)
                        ) {
                            Box(modifier = Modifier.width(2.dp).height(3.dp).background(if (offlineModeEnabled) Color(0xFF475569) else Color.White))
                            Box(modifier = Modifier.width(2.dp).height(5.dp).background(if (offlineModeEnabled) Color(0xFF475569) else Color.White))
                            Box(modifier = Modifier.width(2.dp).height(7.dp).background(if (offlineModeEnabled) Color(0xFF475569) else Color.White))
                            Box(modifier = Modifier.width(2.dp).height(9.dp).background(if (offlineModeEnabled) Color(0xFF475569) else Color.White))
                        }

                        Text(
                            text = "94%",
                            color = Color(0xFFE2E8F0),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Interactive Mobile App Simulator Inside Contents
                AppSimulatorInnerScreen(
                    languageSelected = systemLanguage,
                    onLanguageChange = onLanguageChange,
                    ticketStatus = ticketStatusState,
                    onStatusChange = onStatusChange,
                    offlineMode = offlineModeEnabled,
                    onAddLogMessage = onAddLogMessage,
                    activeTickets = activeTickets,
                    selectedTicketId = selectedTicketId,
                    onSelectedTicketChange = onSelectedTicketChange,
                    simulatorViewMode = simulatorViewMode,
                    onSimulatorViewModeChange = onSimulatorViewModeChange,
                    onAddTicket = onAddTicket
                )
            }
        }
        
        // Mock bottom home slide pill
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .width(90.dp)
                .height(4.dp)
                .background(Color(0xFF94A3B8), CircleShape)
        )
    }
}

// Inner Content Screen of the ServiceNow mock mobile application
@Composable
fun AppSimulatorInnerScreen(
    languageSelected: String,
    onLanguageChange: (String) -> Unit,
    ticketStatus: String,
    onStatusChange: (String) -> Unit,
    offlineMode: Boolean,
    onAddLogMessage: (String) -> Unit,
    activeTickets: List<ServiceRequest>,
    selectedTicketId: String,
    onSelectedTicketChange: (String) -> Unit,
    simulatorViewMode: String,
    onSimulatorViewModeChange: (String) -> Unit,
    onAddTicket: (String, String, String, String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeDark.current
    val theme = getThemeColors(isDark)
    val CleanWhite = theme.CleanWhite
    val SlateNavy = theme.SlateNavy
    val SlateSubtle = theme.SlateSubtle
    val BorderSlate = theme.BorderSlate
    val LightBackground = theme.LightBackground
    val UnisysBlue = theme.UnisysBlue
    val AccentTechBlue = theme.AccentTechBlue

    val currentTexts = localizedStrings[languageSelected] ?: localizedStrings["EN"]!!
    var isDropDownExpanded by remember { mutableStateOf(false) }
    var showInnerResolutionModal by remember { mutableStateOf(false) }
    var resolutionNotesDraft by remember { mutableStateOf("") }
    var selectedResolutionCode by remember { mutableStateOf("Permanent Wire Repair Completed") }
    var selectedGranularTicketId by remember { mutableStateOf<String?>(null) }

    // States for creating/filing a new ticket
    var showCreateTicketDialog by remember { mutableStateOf(false) }
    var createTechId by remember { mutableStateOf("") }
    var createIssueType by remember { mutableStateOf("") }
    var createTitle by remember { mutableStateOf("") }
    var createSiteNode by remember { mutableStateOf("D Graham Node #4592") }
    var createContact by remember { mutableStateOf("Dee Nice - +1 (555) 012-1430") }
    var createNotes by remember { mutableStateOf("") }

    // Validation error states
    var createTechIdError by remember { mutableStateOf<String?>(null) }
    var createIssueTypeError by remember { mutableStateOf<String?>(null) }

    // Speech Recognition States
    var isListeningState by remember { mutableStateOf(false) }
    var speechPartialTextState by remember { mutableStateOf("") }
    var speechRmsState by remember { mutableStateOf(0f) }
    var showDictationOverlayState by remember { mutableStateOf(false) }
    var speechErrorState by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                showDictationOverlayState = true
                isListeningState = true
                speechPartialTextState = ""
                speechErrorState = null
            } else {
                Toast.makeText(context, "Microphone permission is required for voice dictation", Toast.LENGTH_LONG).show()
            }
        }
    )

    DisposableEffect(isListeningState) {
        var speechRecognizer: SpeechRecognizer? = null
        if (isListeningState) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                }
                
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            speechErrorState = null
                        }
                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(rmsdB: Float) {
                            speechRmsState = rmsdB
                        }
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {}
                        override fun onError(error: Int) {
                            val msg = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                                SpeechRecognizer.ERROR_CLIENT -> "Client-side error"
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Service busy"
                                SpeechRecognizer.ERROR_SERVER -> "Server error"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                                else -> "Recognition failed ($error)"
                            }
                            speechErrorState = msg
                        }
                        override fun onResults(results: Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                createIssueType = matches[0]
                                createIssueTypeError = null
                                isListeningState = false
                                showDictationOverlayState = false
                            }
                        }
                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                speechPartialTextState = matches[0]
                            }
                        }
                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                    startListening(intent)
                }
            } catch (e: Exception) {
                speechErrorState = "Speech recognition service not available"
            }
        }
        
        onDispose {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    // Success Toast States
    var showToast by remember { mutableStateOf(false) }
    var toastText by remember { mutableStateOf("") }

    LaunchedEffect(showToast) {
        if (showToast) {
            kotlinx.coroutines.delay(3500)
            showToast = false
        }
    }

    val selectedTicket = activeTickets.find { it.id == selectedTicketId } ?: activeTickets[0]
    
    // Dynamic localization mappings for any chosen ticket
    val ticketPriority = when {
        selectedTicket.priority.contains("Critical") -> if (languageSelected == "ES") "P1 - Crítico" else if (languageSelected == "FR") "P1 - Critique" else "P1 - Critical"
        selectedTicket.priority.contains("High") -> if (languageSelected == "ES") "P2 - Alto" else if (languageSelected == "FR") "P2 - Élevé" else "P2 - High"
        selectedTicket.priority.contains("Moderate") -> if (languageSelected == "ES") "P3 - Moderado" else if (languageSelected == "FR") "P3 - Modéré" else "P3 - Moderate"
        else -> if (languageSelected == "ES") "P4 - Bajo" else if (languageSelected == "FR") "P4 - Faible" else "P4 - Low"
    }
    
    val ticketCategory = when (selectedTicket.id) {
        "INC-2026-9904" -> currentTexts.categoryValue
        "INC-2026-9912" -> if (languageSelected == "ES") "Sistemas de Energía" else if (languageSelected == "FR") "Systèmes d'Énergie" else "Power Systems"
        "INC-2026-9920" -> if (languageSelected == "ES") "Operaciones de Base de Datos" else if (languageSelected == "FR") "Opérations de Base de Données" else "Database Operations"
        "INC-2026-9935" -> if (languageSelected == "ES") "Telecomunicaciones" else if (languageSelected == "FR") "Télécommunications" else "Telecommunications"
        else -> if (languageSelected == "ES") "HVAC y Instalaciones" else if (languageSelected == "FR") "CVC & Installations" else "HVAC & Facilities"
    }

    val ticketNotes = when (selectedTicket.id) {
        "INC-2026-9904" -> currentTexts.clientNotesText
        "INC-2026-9912" -> if (languageSelected == "ES") "El generador falló las pruebas de red duales durante el ciclo de prueba." else if (languageSelected == "FR") "Le générateur a échoué aux tests de réseau doubles pendant le cycle d'essai." else "Generator failed dual automation grid checks during test cycle."
        "INC-2026-9920" -> if (languageSelected == "ES") "El retraso de replicación superó los 500k registros. Alto riesgo de cerebro dividido." else if (languageSelected == "FR") "Le retard de réplication a dépassé 500k enregistrements. Risque élevé de split-brain." else "Replication backlog exceeded 500k records. High risk of split-brain."
        "INC-2026-9935" -> if (languageSelected == "ES") "El operador de cable óptico submarino informó una atenuación de ruta de 3dB." else if (languageSelected == "FR") "L'opérateur de câble optique sous-marin a signalé une atténuation de chemin de 3dB." else "Sub-sea optical cable carrier reported 3dB path attenuation."
        else -> if (languageSelected == "ES") "Se detectó temperatura alta en el devanado del compresor durante carga máxima continua." else if (languageSelected == "FR") "Température élevée de l'enroulement du compresseur détectée pendant une charge de pointe continue." else "High compressor winding temperature detected during continuous peak load."
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // App Custom Private Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(UnisysBlue)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Title with App Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(CleanWhite, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "U",
                            color = UnisysBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = currentTexts.appTitle.uppercase(),
                        color = CleanWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                // LANGUAGE DROPDOWN TOGGLE (English, Spanish, French)
                Box {
                    Button(
                        onClick = { isDropDownExpanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x33FFFFFF),
                            contentColor = CleanWhite
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .height(26.dp)
                            .testTag("simulator_language_dropdown_trigger")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Security language selector",
                                tint = CleanWhite,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = languageSelected,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown icon",
                                tint = CleanWhite,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    // Standard Material Dropdown Menu
                    DropdownMenu(
                        expanded = isDropDownExpanded,
                        onDismissRequest = { isDropDownExpanded = false },
                        modifier = Modifier
                            .background(CleanWhite)
                            .border(1.dp, BorderSlate, RoundedCornerShape(4.dp))
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text("🇺🇸 English", fontSize = 12.sp, color = SlateNavy)
                            },
                            onClick = {
                                onLanguageChange("EN")
                                isDropDownExpanded = false
                                onAddLogMessage("🌍 [LANG] Changed mobile client language profile to English.")
                            },
                            modifier = Modifier.testTag("lang_en_opt")
                        )
                        DropdownMenuItem(
                            text = {
                                Text("🇪🇸 Español", fontSize = 12.sp, color = SlateNavy)
                            },
                            onClick = {
                                onLanguageChange("ES")
                                isDropDownExpanded = false
                                onAddLogMessage("🌍 [LANG] Changed mobile client language profile to Spanish.")
                            },
                            modifier = Modifier.testTag("lang_es_opt")
                        )
                        DropdownMenuItem(
                            text = {
                                Text("🇫🇷 Français", fontSize = 12.sp, color = SlateNavy)
                            },
                            onClick = {
                                onLanguageChange("FR")
                                isDropDownExpanded = false
                                onAddLogMessage("🌍 [LANG] Changed mobile client language profile to French.")
                            },
                            modifier = Modifier.testTag("lang_fr_opt")
                        )
                    }
                }
            }
        }

        // Sub-bar indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEFF6FF))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ServiceNow Incident Relay",
                color = UnisysBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Surface(
                color = if (offlineMode) BadgeBgRed else BadgeBgGreen,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (offlineMode) "LOCAL BUFFER CACHE" else "SECURE ON-API",
                    color = if (offlineMode) TicketP1Red else ServiceGreen,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        // Main app interactive frame scroll zone
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (simulatorViewMode == "dashboard") {
                // TICKET DASHBOARD VIEW
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header inside dashboard
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (languageSelected == "ES") "Solicitudes de Servicio Activas" else if (languageSelected == "FR") "Demandes de Service Actives" else "Active Service Requests",
                            color = SlateNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${activeTickets.size} ${if (languageSelected == "ES") "Activos" else if (languageSelected == "FR") "Actifs" else "Active"}",
                                color = AccentTechBlue,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // File Incident action trigger
                    Button(
                        onClick = {
                            showCreateTicketDialog = true
                            createTechId = ""
                            createIssueType = ""
                            createTitle = ""
                            createNotes = ""
                            createTechIdError = null
                            createIssueTypeError = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UnisysBlue),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .testTag("simulator_file_incident_btn"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add incident icon",
                                tint = CleanWhite,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (languageSelected == "ES") "Registrar Incidencia" else if (languageSelected == "FR") "Signaler un Incident" else "File Incident",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Card items in activeTickets
                    activeTickets.forEach { ticket ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectedTicketChange(ticket.id)
                                    selectedGranularTicketId = ticket.id
                                    onAddLogMessage("📱 [MOBILE] Opened granular technical and customer contact info modal for ${ticket.id}")
                                }
                                .testTag("ticket_card_${ticket.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedTicketId == ticket.id) Color(0xFFEFF6FF) else CleanWhite
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (selectedTicketId == ticket.id) AccentTechBlue else BorderSlate
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Row 1: ID & Time
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    if (ticket.status == "In Progress") ServiceOrange else ServiceGreen,
                                                    CircleShape
                                                )
                                        )
                                        Text(
                                            text = ticket.id,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = SlateNavy
                                        )
                                    }
                                    Text(
                                        text = ticket.updatedTime,
                                        fontSize = 8.sp,
                                        color = SlateSubtle,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Row 2: Title
                                Text(
                                    text = ticket.title,
                                    fontSize = 10.sp,
                                    color = SlateNavy,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // Row 3: Badges
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Priority badge
                                    val (badgeBg, badgeText, badgeLabel) = when {
                                        ticket.priority.contains("Critical") -> Triple(BadgeBgRed, TicketP1Red, "P1 - Critical")
                                        ticket.priority.contains("High") -> Triple(BadgeBgOrange, ServiceOrange, "P2 - High")
                                        ticket.priority.contains("Moderate") -> Triple(Color(0xFFEFF6FF), AccentTechBlue, "P3 - Moderate")
                                        else -> Triple(Color(0xFFF1F5F9), Color(0xFF64748B), "P4 - Low")
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .background(badgeBg, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (languageSelected == "ES") {
                                                badgeLabel.replace("Critical", "Crítico").replace("High", "Alto").replace("Moderate", "Moderado").replace("Low", "Bajo")
                                            } else if (languageSelected == "FR") {
                                                badgeLabel.replace("Critical", "Critique").replace("High", "Élevé").replace("Moderate", "Modéré").replace("Low", "Faible")
                                            } else {
                                                badgeLabel
                                            },
                                            color = badgeText,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Status badge
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (ticket.status == "In Progress") BadgeBgOrange else BadgeBgGreen,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (ticket.status == "In Progress") {
                                                currentTexts.statusInProgress.uppercase()
                                            } else {
                                                currentTexts.statusResolved.uppercase()
                                            },
                                            color = if (ticket.status == "In Progress") ServiceOrange else ServiceGreen,
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // TICKET DETAILS (ACTIVE SCREEN)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Ticket Meta Information Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CleanWhite),
                        border = BorderStroke(1.dp, BorderSlate)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ID: $selectedTicketId",
                                    color = SlateNavy,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                
                                // High-critical urgency badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (selectedTicket.priority.contains("Critical")) BadgeBgRed else if (selectedTicket.priority.contains("High")) BadgeBgOrange else Color(0xFFEFF6FF),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "P1 Warning",
                                            tint = if (selectedTicket.priority.contains("Critical")) TicketP1Red else if (selectedTicket.priority.contains("High")) ServiceOrange else AccentTechBlue,
                                            modifier = Modifier.size(9.dp)
                                        )
                                        Text(
                                            text = ticketPriority.uppercase(),
                                            color = if (selectedTicket.priority.contains("Critical")) TicketP1Red else if (selectedTicket.priority.contains("High")) ServiceOrange else AccentTechBlue,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }

                            Divider(color = BorderSlate)

                            // Meta details Grid (Issue Type, Technician ID, Site Node, Status)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Row 1: Issue Type & Technician ID
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1.1f)) {
                                        Text(text = currentTexts.categoryLabel.uppercase(), color = SlateSubtle, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = ticketCategory, color = SlateNavy, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Column(modifier = Modifier.weight(0.9f)) {
                                        Text(text = currentTexts.techIdLabel.uppercase(), color = SlateSubtle, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = "TECH-DG-${selectedTicketId.takeLast(4)}", color = SlateNavy, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }

                                Divider(color = Color(0xFFF1F5F9))

                                // Row 2: Site Node & Status Pill
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.1f)) {
                                        Text(text = currentTexts.siteLabel.uppercase(), color = SlateSubtle, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = selectedTicket.site, color = SlateNavy, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Column(modifier = Modifier.weight(0.9f)) {
                                        Text(text = currentTexts.statusLabel.uppercase(), color = SlateSubtle, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        // Status colored pill
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (ticketStatus == "In Progress") BadgeBgOrange else BadgeBgGreen,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (ticketStatus == "In Progress") currentTexts.statusInProgress.uppercase() else currentTexts.statusResolved.uppercase(),
                                                color = if (ticketStatus == "In Progress") ServiceOrange else ServiceGreen,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Client Notes Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CleanWhite),
                        border = BorderStroke(1.dp, BorderSlate)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Client Notes logo",
                                    tint = SlateSubtle,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = currentTexts.clientNotesLabel,
                                    color = SlateSubtle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }

                            // Fully localized client incident issue note
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = ticketNotes,
                                    color = SlateNavy,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Granular Details Telemetry Trigger
                    OutlinedButton(
                        onClick = {
                            selectedGranularTicketId = selectedTicketId
                            onAddLogMessage("📱 [MOBILE] Opened technical diagnostics & contact panel for $selectedTicketId")
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = UnisysBlue),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, UnisysBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("app_view_granular_details_trigger")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Technical Info Diagnostics",
                                tint = UnisysBlue,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (languageSelected == "ES") "Ver Diagnóstico y Contacto" else if (languageSelected == "FR") "Voir Diagnostic Tech & Contact" else "View Technical Logs & Contact",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Resolve / Reopen action CTA section
                    Spacer(modifier = Modifier.weight(1f))

                    if (ticketStatus == "In Progress") {
                        Button(
                            onClick = { showInnerResolutionModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ServiceOrange),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("app_resolve_incident_trigger")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Resolve ticket icon",
                                tint = CleanWhite,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentTexts.resolveButton,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                onStatusChange("In Progress")
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateNavy),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, SlateNavy),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("app_reopen_incident_trigger")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset ticket state",
                                tint = SlateNavy,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (languageSelected == "ES") "Reabrir Ticket" else if (languageSelected == "FR") "Rouvrir l'Incident" else "Reopen Ticket (Reset)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Real simulated create ticket/incident dialogue overlay directly within the phone simulation
            if (showCreateTicketDialog) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x990F172A))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp),
                        colors = CardDefaults.cardColors(containerColor = CleanWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (languageSelected == "ES") "Ficha de Registro de Incidencia" else if (languageSelected == "FR") "Créer un Dossier d'Incident" else "File Incident Proposal",
                                color = SlateNavy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )

                            // Technician ID field (Mandatory)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "${if (languageSelected == "ES") "ID del Técnico" else if (languageSelected == "FR") "ID du Technicien" else "Technician ID"} *",
                                    color = SlateNavy,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedTextField(
                                    value = createTechId,
                                    onValueChange = {
                                        createTechId = it
                                        createTechIdError = null
                                    },
                                    placeholder = { Text("e.g. TECH-DG-1234", fontSize = 9.sp) },
                                    textStyle = TextStyle(fontSize = 10.sp),
                                    singleLine = true,
                                    isError = createTechIdError != null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("form_tech_id_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = UnisysBlue,
                                        unfocusedBorderColor = BorderSlate,
                                        errorBorderColor = TicketP1Red
                                    )
                                )
                                if (createTechIdError != null) {
                                    Text(
                                        text = createTechIdError!!,
                                        color = TicketP1Red,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.testTag("error_tech_id_empty")
                                    )
                                }
                            }

                            // Issue Type field (Mandatory)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "${if (languageSelected == "ES") "Tipo de Incidencia" else if (languageSelected == "FR") "Type d'Incident" else "Issue Type / Category"} *",
                                    color = SlateNavy,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedTextField(
                                    value = createIssueType,
                                    onValueChange = {
                                        createIssueType = it
                                        createIssueTypeError = null
                                    },
                                    placeholder = { Text("e.g. Hardware Provisioning", fontSize = 9.sp) },
                                    textStyle = TextStyle(fontSize = 10.sp),
                                    singleLine = true,
                                    isError = createIssueTypeError != null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("form_issue_type_input"),
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                                                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                                    isListeningState = true
                                                    speechPartialTextState = ""
                                                    speechErrorState = null
                                                    showDictationOverlayState = true
                                                } else {
                                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                }
                                            },
                                            modifier = Modifier.size(32.dp).testTag("voice_dictation_mic_button")
                                        ) {
                                            MicIcon(
                                                tint = if (isListeningState) TicketP1Red else UnisysBlue,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = UnisysBlue,
                                        unfocusedBorderColor = BorderSlate,
                                        errorBorderColor = TicketP1Red
                                    )
                                )
                                if (createIssueTypeError != null) {
                                    Text(
                                        text = createIssueTypeError!!,
                                        color = TicketP1Red,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.testTag("error_issue_type_empty")
                                    )
                                }
                            }



                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { showCreateTicketDialog = false }
                                ) {
                                    Text(text = currentTexts.cancelLabel, fontSize = 9.sp, color = SlateSubtle)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Button(
                                    onClick = {
                                        println("DEBUG_TEST: onClick triggered!")
                                        var hasError = false
                                        if (createTechId.trim().isEmpty()) {
                                            createTechIdError = "Technician ID is required"
                                            hasError = true
                                        }
                                        if (createIssueType.trim().isEmpty()) {
                                            createIssueTypeError = "Issue Type is required"
                                            hasError = true
                                        }
                                        if (!hasError) {
                                            val techIdPill = createTechId.trim()
                                            onAddTicket(
                                                createTitle.ifBlank { "Unscheduled Maintenance Node Check" },
                                                createIssueType,
                                                "P2 - High",
                                                createTechId,
                                                createSiteNode,
                                                createNotes.ifBlank { "Dispatched by client diagnostic proposal." },
                                                createContact
                                            )
                                            toastText = when (languageSelected) {
                                                "ES" -> "✓ ¡Incidencia registrada con éxito para el técnico $techIdPill!"
                                                "FR" -> "✓ Incident créé avec succès pour le technicien $techIdPill !"
                                                else -> "✓ Ticket filed successfully for Tech ID $techIdPill!"
                                            }
                                            showToast = true
                                            showCreateTicketDialog = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = UnisysBlue),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("submit_create_ticket_btn")
                                ) {
                                    Text(
                                        text = if (languageSelected == "ES") "Registrar" else if (languageSelected == "FR") "Soumettre" else "Submit Proposal",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Voice Dictation overlay directly within the phone simulation
            if (showDictationOverlayState) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xE00F172A)) // dark translucent overlay
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp)
                            .testTag("dictation_overlay_card"),
                        colors = CardDefaults.cardColors(containerColor = CleanWhite),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    MicIcon(tint = if (isListeningState) TicketP1Red else SlateSubtle, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = if (isListeningState) "Dictating category..." else "Voice Dictation",
                                        color = SlateNavy,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                // Close button
                                IconButton(
                                    onClick = {
                                        isListeningState = false
                                        showDictationOverlayState = false
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Close",
                                        tint = SlateSubtle,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Pulsing wave simulation
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 1.0f,
                                targetValue = 1.4f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulseScale"
                            )
                            
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(80.dp)
                            ) {
                                // Pulsing waves
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .scale(if (isListeningState) pulseScale + (speechRmsState / 15f).coerceIn(0f, 0.5f) else 1.0f)
                                        .background(
                                            color = if (isListeningState) TicketP1Red.copy(alpha = 0.2f) else SlateSubtle.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                )
                                
                                // Microphone circle
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(
                                            color = if (isListeningState) TicketP1Red else SlateSubtle,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MicIcon(tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                            }

                            // Subtitle/Real-time Transcript text
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "Transcript Output:",
                                    fontSize = 8.sp,
                                    color = SlateSubtle,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 40.dp)
                                        .border(1.dp, BorderSlate, RoundedCornerShape(8.dp)),
                                    color = LightBackground,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(8.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = if (speechPartialTextState.isNotEmpty()) speechPartialTextState else if (isListeningState) "Speak category clearly..." else "Ready to dictate.",
                                            fontSize = 9.sp,
                                            color = if (speechPartialTextState.isNotEmpty()) SlateNavy else SlateSubtle,
                                            fontStyle = if (speechPartialTextState.isEmpty()) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                                        )
                                    }
                                }
                            }

                            // Fallback Simulator Panel
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Tap a simulated tech phrase to auto-dictate:",
                                    fontSize = 8.sp,
                                    color = SlateSubtle,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val fallbackTemplates = listOf(
                                        "Hardware Provisioning Failure",
                                        "Fiber Optic Repair Needed",
                                        "Database Connection Outage",
                                        "Network Router Calibration",
                                        "Field Sensor Maintenance"
                                    )
                                    fallbackTemplates.forEach { template ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(UnisysBlue.copy(alpha = 0.1f))
                                                .border(1.dp, UnisysBlue.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                                .clickable {
                                                    coroutineScope.launch {
                                                        isListeningState = false
                                                        speechPartialTextState = ""
                                                        speechErrorState = null
                                                        
                                                        var currentString = ""
                                                        for (char in template) {
                                                            currentString += char
                                                            speechPartialTextState = currentString
                                                            delay(25)
                                                        }
                                                        
                                                        delay(200)
                                                        createIssueType = template
                                                        createIssueTypeError = null
                                                        showDictationOverlayState = false
                                                    }
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = template,
                                                fontSize = 8.sp,
                                                color = UnisysBlue,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }

                            // Error indicator
                            speechErrorState?.let { err ->
                                Text(
                                    text = "⚠️ $err",
                                    color = TicketP1Red,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Control actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        isListeningState = false
                                        showDictationOverlayState = false
                                    },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateSubtle),
                                    border = BorderStroke(1.dp, BorderSlate),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Cancel", fontSize = 9.sp)
                                }
                                
                                if (isListeningState) {
                                    Button(
                                        onClick = {
                                            isListeningState = false
                                        },
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = TicketP1Red),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Stop", fontSize = 9.sp, color = Color.White)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            isListeningState = true
                                            speechPartialTextState = ""
                                            speechErrorState = null
                                        },
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = UnisysBlue),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Listen", fontSize = 9.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Real simulated popover resolution dialogue overlay directly within the phone simulation
            if (showInnerResolutionModal) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x990F172A))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp),
                        colors = CardDefaults.cardColors(containerColor = CleanWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = currentTexts.dialogTitle,
                                color = SlateNavy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )

                            // Dropdown indicator
                            Text(text = currentTexts.selectCodeLabel, color = SlateSubtle, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                    .border(1.dp, BorderSlate, RoundedCornerShape(4.dp))
                                    .padding(6.dp)
                            ) {
                                Text(
                                    text = selectedResolutionCode,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SlateNavy
                                )
                            }

                            // Text entry notes
                            Text(text = currentTexts.resolutionNotesLabel, color = SlateSubtle, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = resolutionNotesDraft,
                                onValueChange = { resolutionNotesDraft = it },
                                placeholder = { Text("E.g. Completed manual line repair.", fontSize = 9.sp) },
                                textStyle = TextStyle(fontSize = 10.sp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = UnisysBlue,
                                    unfocusedBorderColor = BorderSlate
                                )
                            )

                            // Synchronizing details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { showInnerResolutionModal = false }
                                ) {
                                    Text(text = currentTexts.cancelLabel, fontSize = 9.sp, color = SlateSubtle)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Button(
                                    onClick = {
                                        onStatusChange("Resolved")
                                        showInnerResolutionModal = false
                                        resolutionNotesDraft = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ServiceGreen),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("submit_inner_resolution_btn")
                                ) {
                                    Text(text = currentTexts.submitLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Granular Details Modal Overlay
            selectedGranularTicketId?.let { ticketId ->
                val ticket = activeTickets.find { it.id == ticketId }
                if (ticket != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xCC0F172A))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp),
                            colors = CardDefaults.cardColors(containerColor = CleanWhite),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Header: ID and Title
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    if (ticket.status == "In Progress") ServiceOrange else ServiceGreen,
                                                    CircleShape
                                                )
                                        )
                                        Text(
                                            text = ticket.id,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = UnisysBlue
                                        )
                                    }
                                    
                                    // Text dismiss button
                                    Text(
                                        text = if (languageSelected == "ES") "Cerrar" else if (languageSelected == "FR") "Fermer" else "Close",
                                        color = SlateSubtle,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable { selectedGranularTicketId = null }
                                            .padding(4.dp)
                                    )
                                }

                                Text(
                                    text = ticket.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateNavy
                                )

                                Divider(color = BorderSlate)

                                // Section 1: Detailed Timestamps
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = if (languageSelected == "ES") "⏱️ MARCA DE TIEMPO REGISTRADA" else if (languageSelected == "FR") "⏱️ HORODATAGE ENREGISTRÉ" else "⏱️ REGISTERED TIMESTAMP",
                                        color = SlateSubtle,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = ticket.timestamp,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SlateNavy,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }

                                // Section 2: Technician Notes
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = if (languageSelected == "ES") "🔧 NOTAS DEL TÉCNICO" else if (languageSelected == "FR") "🔧 NOTES DU TECHNICIEN" else "🔧 TECHNICIAN NOTES",
                                        color = SlateSubtle,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFEFF6FF), RoundedCornerShape(6.dp))
                                            .border(1.dp, Color(0xFFDBEAFE), RoundedCornerShape(6.dp))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = ticket.technicianNotes,
                                            color = SlateNavy,
                                            fontSize = 10.sp,
                                            lineHeight = 14.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }

                                // Section 3: Customer / Contact Info
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = if (languageSelected == "ES") "👤 INFO DE CONTACTO" else if (languageSelected == "FR") "👤 INFOS DE CONTACT" else "👤 CUSTOMER CONTACT INFO",
                                        color = SlateSubtle,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                            .border(1.dp, BorderSlate, RoundedCornerShape(6.dp))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = ticket.customerContact,
                                            color = SlateNavy,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }

                                Divider(color = BorderSlate)

                                // Actions row inside modal
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Close Button
                                    OutlinedButton(
                                        onClick = { selectedGranularTicketId = null },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, SlateSubtle),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateNavy),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (languageSelected == "ES") "Descartar" else if (languageSelected == "FR") "Fermer" else "Dismiss",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Action to full detailing page (manage ticket)
                                    Button(
                                        onClick = {
                                            onSelectedTicketChange(ticket.id)
                                            onSimulatorViewModeChange("details")
                                            selectedGranularTicketId = null
                                            onAddLogMessage("📱 [MOBILE] Closed and opened status manager details screen for ${ticket.id}")
                                        },
                                        modifier = Modifier.weight(1.2f),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = UnisysBlue),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (languageSelected == "ES") "Ver Acciones" else if (languageSelected == "FR") "Voir Actions" else "Manage Status",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CleanWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // High-Fidelity Custom Bottom Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F5F9))
                .border(BorderStroke(1.dp, BorderSlate))
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option 1: Dashboard
            val isDashboardSelected = simulatorViewMode == "dashboard"
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onSimulatorViewModeChange("dashboard")
                        onAddLogMessage("📱 [MOBILE] Switched screen view to Ticket Dashboard.")
                    }
                    .testTag("nav_dashboard")
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Dashboard Nav icon",
                    tint = if (isDashboardSelected) UnisysBlue else SlateSubtle,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (languageSelected == "ES") "Tablero" else if (languageSelected == "FR") "Tableau" else "Dashboard",
                    color = if (isDashboardSelected) UnisysBlue else SlateSubtle,
                    fontSize = 9.sp,
                    fontWeight = if (isDashboardSelected) FontWeight.Bold else FontWeight.Normal
                )
            }

            // Separator line
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(BorderSlate)
            )

            // Option 2: Details
            val isDetailsSelected = simulatorViewMode == "details"
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onSimulatorViewModeChange("details")
                        onAddLogMessage("📱 [MOBILE] Switched screen view to Detail view of $selectedTicketId.")
                    }
                    .testTag("nav_details")
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Details Nav icon",
                    tint = if (isDetailsSelected) UnisysBlue else SlateSubtle,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (languageSelected == "ES") "Detalle" else if (languageSelected == "FR") "Détails" else "Ticket Details",
                    color = if (isDetailsSelected) UnisysBlue else SlateSubtle,
                    fontSize = 9.sp,
                    fontWeight = if (isDetailsSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
        }
        
        // Beautiful success notification toast overlay component
        AnimatedVisibility(
            visible = showToast,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                color = Color(0xFF0F172A), // Slate 900
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("success_toast_container")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success tick icon",
                        tint = ServiceGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = toastText,
                        color = CleanWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// Below the Fold: Features Section & Interactive Panels
@Composable
fun FeaturesSectionBelowFold(
    offlineModeEnabled: Boolean,
    onOfflineModeToggle: (Boolean) -> Unit,
    localBufferedQueues: Int,
    syncHistoryList: List<String>,
    diagnosticIntegrityChecked: Boolean,
    isRunningDiagnosticScanner: Boolean,
    diagnosticSecurityStatusText: String,
    onInitiateScanner: () -> Unit,
    onClearTerminal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeDark.current
    val theme = getThemeColors(isDark)
    val CleanWhite = theme.CleanWhite
    val SlateNavy = theme.SlateNavy
    val SlateSubtle = theme.SlateSubtle
    val BorderSlate = theme.BorderSlate
    val LightBackground = theme.LightBackground
    val UnisysBlue = theme.UnisysBlue
    val AccentTechBlue = theme.AccentTechBlue

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFEEF2F6))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Section Title
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "ENTERPRISE CAPABILITIES",
                color = AccentTechBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Resilient, Compliant Architecture",
                color = SlateNavy,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Designed specifically to meet the high data isolation required by defense, health-agency, and financial field dispatches.",
                color = SlateSubtle,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 500.dp)
            )
        }

        // Feature interactive cards row (3 columns using BoxWithConstraints/multi-row inside a fluid container)
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CARD 1: Privacy-First Architecture with simulated integrity checker
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CleanWhite),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFEFF6FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Shield logo",
                            tint = AccentTechBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Privacy-First Security Envelope",
                            color = SlateNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Includes client-side database encryption (AES-256), memory scrubbing upon background execution, and automated VPN handshakes to D Graham private proxies.",
                            color = SlateSubtle,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Interactive Scan Button
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, BorderSlate)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "FIPS Security Integrity Check",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateNavy
                                    )
                                    Text(
                                        text = diagnosticSecurityStatusText,
                                        fontSize = 9.sp,
                                        color = if (diagnosticIntegrityChecked) ServiceGreen else SlateSubtle
                                    )
                                }

                                Button(
                                    onClick = onInitiateScanner,
                                    enabled = !isRunningDiagnosticScanner,
                                    colors = ButtonDefaults.buttonColors(containerColor = UnisysBlue),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("diagnostic_scan_action_btn")
                                ) {
                                    if (isRunningDiagnosticScanner) {
                                        CircularProgressIndicator(
                                            color = CleanWhite,
                                            strokeWidth = 1.dp,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = if (diagnosticIntegrityChecked) Icons.Default.Done else Icons.Default.PlayArrow,
                                            contentDescription = "Diagnostic play icon",
                                            tint = CleanWhite,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = if (diagnosticIntegrityChecked) "Passed" else "Scan", fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // CARD 2: Real-time ServiceNow Sync with Live Sync terminal history list
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CleanWhite),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFECFDF5), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "ServiceNow Sync Logo",
                            tint = ServiceGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Real-Time ServiceNow Sync",
                                color = SlateNavy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            
                            // Live heartbeat dot
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    color = if (offlineModeEnabled) Color.Red else ServiceGreen,
                                    shape = CircleShape,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                                Text(
                                    text = if (offlineModeEnabled) "Paused" else "Live Syncing",
                                    fontSize = 8.sp,
                                    color = if (offlineModeEnabled) Color.Red else ServiceGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "Fully backward-compatible with older ServiceNow instances, syncing parameters instantly across global nodes.",
                            color = SlateSubtle,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Sync incident console feed
                        Surface(
                            color = SlateNavy,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "CONSOLE SYNC LEDGER",
                                        color = Color(0xFFA5B4FC),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Clear console",
                                        color = Color(0xFF6366F1),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier
                                            .clickable { onClearTerminal() }
                                            .testTag("clear_logs_cta")
                                    )
                                }

                                Divider(color = Color(0x22FFFFFF))

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    // Render only the latest 3 elements to avoid long page stretches
                                    syncHistoryList.takeLast(3).forEach { logLine ->
                                        Text(
                                            text = logLine,
                                            color = Color(0xFFE2E8F0),
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // CARD 3: Offline-Capable Workflow with Network simulator switch
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CleanWhite),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFFF7ED), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Offline icon",
                            tint = ServiceOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Isolated Offline Workflow",
                                color = SlateNavy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            
                            // Isolated Cache counter
                            if (localBufferedQueues > 0) {
                                Surface(
                                    color = BadgeBgOrange,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "$localBufferedQueues Queued",
                                        color = ServiceOrange,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Lose cell range entirely inside shielded basements or server buildings. Edits accumulate locally and auto-push the minute your link detects active carrier signal.",
                            color = SlateSubtle,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Toggle offline mode simulator
                        Surface(
                            color = Color(0xFFFFF7ED),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFEDD5))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (offlineModeEnabled) Icons.Default.Warning else Icons.Default.Check,
                                        contentDescription = "Cloud status icon",
                                        tint = if (offlineModeEnabled) TicketP1Red else AccentTechBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (offlineModeEnabled) "Simulated State: OFFLINE" else "Simulated State: ONLINE / NET",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (offlineModeEnabled) TicketP1Red else UnisysBlue
                                    )
                                }

                                Switch(
                                    checked = offlineModeEnabled,
                                    onCheckedChange = onOfflineModeToggle,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = TicketP1Red,
                                        checkedTrackColor = BadgeBgRed,
                                        uncheckedThumbColor = AccentTechBlue,
                                        uncheckedTrackColor = Color(0xFFDBEAFE)
                                    ),
                                    modifier = Modifier
                                        .scale(0.8f)
                                        .testTag("offline_simulator_switch")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Secure Request Access Dialog modal popup
@Composable
fun SecureRequestAccessDialog(
    companyMail: String,
    onMailChange: (String) -> Unit,
    isSubmittingSuccess: Boolean,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeDark.current
    val theme = getThemeColors(isDark)
    val CleanWhite = theme.CleanWhite
    val SlateNavy = theme.SlateNavy
    val SlateSubtle = theme.SlateSubtle
    val BorderSlate = theme.BorderSlate
    val LightBackground = theme.LightBackground
    val UnisysBlue = theme.UnisysBlue
    val AccentTechBlue = theme.AccentTechBlue

    var errorText by remember { mutableStateOf<String?>(null) }
    var supervisorIdField by remember { mutableStateOf("") }
    var regionalField by remember { mutableStateOf("USA - East Terminals") }
    var securityAcknowledgeChecked by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Accredited Access Icon",
                    tint = UnisysBlue,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Request Gateway Access",
                    color = SlateNavy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            if (isSubmittingSuccess) {
                // Success screen inside the security modal
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color(0xFFDCFCE7), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Submit success",
                            tint = ServiceGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "Access Proposal Submitted!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SlateNavy,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "We dispatched secure verification token proposals targeting $companyMail. Please open your security card viewer within 24 hours to accredit.",
                        fontSize = 12.sp,
                        color = SlateSubtle,
                        textAlign = TextAlign.Center
                    )
                    
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "SECURE PROTOCOL ID: RES-7784-A",
                            color = SlateNavy,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = UnisysBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("close_success_dialog_btn")
                    ) {
                        Text("Acknowledge & Close")
                    }
                }
            } else {
                // Main credentials entry sheet form validation
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Provide accredited credentials to register matching hardware keys and deploy incident simulator services.",
                        color = SlateSubtle,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    // Corporate Email Address field
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "D Graham Corporate Email",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateNavy
                        )
                        OutlinedTextField(
                            value = companyMail,
                            onValueChange = {
                                onMailChange(it)
                                errorText = null
                            },
                            placeholder = { Text("e.g. tech.name@dgraham.com", fontSize = 11.sp) },
                            singleLine = true,
                            isError = errorText != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = UnisysBlue,
                                unfocusedBorderColor = BorderSlate
                            ),
                            textStyle = TextStyle(fontSize = 12.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_email_input")
                        )
                    }

                    // Field supervisor / badge ID
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Deployer Badge ID",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateNavy
                        )
                        OutlinedTextField(
                            value = supervisorIdField,
                            onValueChange = { supervisorIdField = it },
                            placeholder = { Text("e.g. US-6648-9", fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = UnisysBlue,
                                unfocusedBorderColor = BorderSlate
                            ),
                            textStyle = TextStyle(fontSize = 12.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_badge_input")
                        )
                    }

                    // Regional field sector dropdown/selection placeholder
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Assigned Operational Hub",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateNavy
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                .border(1.dp, BorderSlate, RoundedCornerShape(4.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = regionalField, fontSize = 11.sp, color = SlateNavy, fontWeight = FontWeight.SemiBold)
                            Icon(imageVector = Icons.Default.Home, contentDescription = "Location hub locator", tint = SlateSubtle, modifier = Modifier.size(14.dp))
                        }
                    }

                    // Dynamic security check checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { securityAcknowledgeChecked = !securityAcknowledgeChecked }
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = securityAcknowledgeChecked,
                            onCheckedChange = { securityAcknowledgeChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = UnisysBlue)
                        )
                        Text(
                            text = "I swear compliance to corporate FIPS 140-2 confidentiality clauses.",
                            fontSize = 9.sp,
                            color = UnisysBlue,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (errorText != null) {
                        Text(text = errorText!!, color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Submit & Cancel button footer
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BorderSlate)
                        ) {
                            Text("Reject", fontSize = 12.sp, color = SlateSubtle)
                        }

                        Button(
                            onClick = {
                                if (companyMail.isEmpty() || !companyMail.contains("@")) {
                                    errorText = "Please enter an accredited email address."
                                } else if (supervisorIdField.isEmpty()) {
                                    errorText = "Please specify your Deployer Badge ID."
                                } else if (!securityAcknowledgeChecked) {
                                    errorText = "You must acknowledge compliance clauses first."
                                } else {
                                    onSubmit()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = UnisysBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_submit_access_btn")
                        ) {
                            Text("Authorize", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = true)
    )
}

// Enterprise trusted footer layout with legal disclosures
@Composable
fun DGrahamEnterpriseFooter(modifier: Modifier = Modifier) {
    val isDark = LocalThemeDark.current
    val theme = getThemeColors(isDark)
    val CleanWhite = theme.CleanWhite
    val SlateNavy = theme.SlateNavy
    val SlateSubtle = theme.SlateSubtle
    val BorderSlate = theme.BorderSlate
    val LightBackground = theme.LightBackground
    val UnisysBlue = theme.UnisysBlue
    val AccentTechBlue = theme.AccentTechBlue

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isDark) Color(0xFF090D16) else Color(0xFF0F172A))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFF334155), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "D",
                    color = CleanWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Text(
                text = "D GRAHAM SECURE OPERATIONS",
                color = CleanWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
        }

        Text(
            text = "ServiceNow integration client is owned and maintained by D Graham global hardware team dispatches. All server requests are cryptographically parsed and subject to strict federal logging constraints.",
            color = SlateSubtle,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            modifier = Modifier.widthIn(max = 550.dp)
        )

        Divider(color = Color(0xFF1E293B), thickness = 1.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "© 2026 D Graham Corp. All rights secured.",
                color = SlateSubtle,
                fontSize = 10.sp
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Security Terms", color = Color(0xFF6366F1), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = "System Status", color = ServiceGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MicIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        
        // Draw mic body
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.35f, h * 0.2f),
            size = androidx.compose.ui.geometry.Size(w * 0.3f, h * 0.45f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.15f, w * 0.15f)
        )
        
        // Draw stand/cradle
        drawArc(
            color = tint,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.25f, h * 0.35f),
            size = androidx.compose.ui.geometry.Size(w * 0.5f, h * 0.35f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
        
        // Draw vertical shaft
        drawLine(
            color = tint,
            start = Offset(w * 0.5f, h * 0.7f),
            end = Offset(w * 0.5f, h * 0.85f),
            strokeWidth = 2.dp.toPx()
        )
        
        // Draw base
        drawLine(
            color = tint,
            start = Offset(w * 0.35f, h * 0.85f),
            end = Offset(w * 0.65f, h * 0.85f),
            strokeWidth = 2.dp.toPx()
        )
    }
}

fun saveRecentTicketsToStorage(context: Context, tickets: List<ServiceRequest>) {
    try {
        val sharedPref = context.getSharedPreferences("localStorage_recent_tickets", Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        tickets.forEach { ticket ->
            val jsonObject = JSONObject().apply {
                put("id", ticket.id)
                put("title", ticket.title)
                put("priority", ticket.priority)
                put("category", ticket.category)
                put("status", ticket.status)
                put("site", ticket.site)
                put("clientNotes", ticket.clientNotes)
                put("updatedTime", ticket.updatedTime)
                put("technicianNotes", ticket.technicianNotes)
                put("timestamp", ticket.timestamp)
                put("customerContact", ticket.customerContact)
            }
            jsonArray.put(jsonObject)
        }
        sharedPref.edit().putString("recent_tickets_key", jsonArray.toString()).apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun loadRecentTicketsFromStorage(context: Context): List<ServiceRequest> {
    val result = mutableListOf<ServiceRequest>()
    try {
        val sharedPref = context.getSharedPreferences("localStorage_recent_tickets", Context.MODE_PRIVATE)
        val jsonString = sharedPref.getString("recent_tickets_key", null)
        if (!jsonString.isNullOrEmpty()) {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                result.add(
                    ServiceRequest(
                        id = jsonObject.optString("id", ""),
                        title = jsonObject.optString("title", ""),
                        priority = jsonObject.optString("priority", ""),
                        category = jsonObject.optString("category", ""),
                        status = jsonObject.optString("status", ""),
                        site = jsonObject.optString("site", ""),
                        clientNotes = jsonObject.optString("clientNotes", ""),
                        updatedTime = jsonObject.optString("updatedTime", ""),
                        technicianNotes = jsonObject.optString("technicianNotes", ""),
                        timestamp = jsonObject.optString("timestamp", ""),
                        customerContact = jsonObject.optString("customerContact", "")
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}

@Composable
fun RecentTicketsSection(
    recentTickets: List<ServiceRequest>,
    onTicketClick: (ServiceRequest) -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val theme = getThemeColors(isDarkMode)
    val CleanWhite = theme.CleanWhite
    val SlateNavy = theme.SlateNavy
    val SlateSubtle = theme.SlateSubtle
    val BorderSlate = theme.BorderSlate
    val LightBackground = theme.LightBackground
    val UnisysBlue = theme.UnisysBlue
    val AccentTechBlue = theme.AccentTechBlue

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("recent_tickets_list_card"),
        colors = CardDefaults.cardColors(containerColor = CleanWhite),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(UnisysBlue.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Recent Tickets List",
                        tint = UnisysBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Recent Tickets",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateNavy
                    )
                    Text(
                        text = "LocalStorage persistent cache (last 5 submitted)",
                        fontSize = 11.sp,
                        color = SlateSubtle
                    )
                }
            }

            if (recentTickets.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "No recent tickets",
                        tint = SlateSubtle.copy(alpha = 0.6f),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "No tickets submitted in this session.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateNavy.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Create a ticket using the 'Create Ticket' form in the simulator above to see it stored here locally.",
                        fontSize = 11.sp,
                        color = SlateSubtle,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                // List of tickets
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    recentTickets.forEach { ticket ->
                        RecentTicketItem(
                            ticket = ticket,
                            onClick = { onTicketClick(ticket) },
                            theme = theme
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentTicketItem(
    ticket: ServiceRequest,
    onClick: () -> Unit,
    theme: ThemeColors
) {
    val animAlpha = remember { Animatable(0f) }
    val animOffsetY = remember { Animatable(12f) }

    LaunchedEffect(ticket.id) {
        launch {
            animAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            animOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing)
            )
        }
    }

    val CleanWhite = theme.CleanWhite
    val SlateNavy = theme.SlateNavy
    val SlateSubtle = theme.SlateSubtle
    val BorderSlate = theme.BorderSlate
    val LightBackground = theme.LightBackground
    val UnisysBlue = theme.UnisysBlue
    val AccentTechBlue = theme.AccentTechBlue

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = animAlpha.value
                translationY = animOffsetY.value
            }
            .clickable { onClick() }
            .testTag("recent_ticket_item_${ticket.id}"),
        color = LightBackground,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ticket ID & Priority Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = ticket.id,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = UnisysBlue,
                        style = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    )
                    
                    // Priority Badge
                    val priorityColor = when {
                        ticket.priority.contains("P1") -> TicketP1Red
                        ticket.priority.contains("P2") -> Color(0xFFF97316) // orange
                        else -> AccentTechBlue
                    }
                    Box(
                        modifier = Modifier
                            .background(priorityColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .border(1.dp, priorityColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = ticket.priority.substringBefore(" -"),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor
                        )
                    }
                }

                // Date/Time
                Text(
                    text = ticket.updatedTime,
                    fontSize = 11.sp,
                    color = SlateSubtle
                )
            }

            // Title
            Text(
                text = ticket.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlateNavy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Category & Site Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Category",
                        tint = SlateSubtle,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = ticket.category,
                        fontSize = 11.sp,
                        color = SlateSubtle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = if (ticket.status == "Resolved") ServiceGreen else Color(0xFFF59E0B),
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = ticket.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (ticket.status == "Resolved") ServiceGreen else Color(0xFFF59E0B)
                    )
                }
            }
        }
    }
}
