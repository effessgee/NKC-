package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.UrgencyHigh
import com.example.ui.theme.UrgencyLow
import com.example.ui.theme.UrgencyMedium
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val model: MainViewModel = viewModel()
            val isDarkTheme by model.isDarkTheme.collectAsState()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                CleanTrackApp(model)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanTrackApp(model: MainViewModel = viewModel()) {
    val context = LocalContext.current

    // State bindings
    val currentRole by model.currentRole.collectAsState()
    val complaints by model.complaints.collectAsState()
    val inventoryItems by model.inventoryItems.collectAsState()
    val staffMembers by model.staffMembers.collectAsState()
    val invoices by model.invoices.collectAsState()
    val notifications by model.notifications.collectAsState()

    val selectedCmpId by model.selectedComplaintId.collectAsState()
    val activeChatId by model.activeChatComplaintId.collectAsState()
    val scanningStaffId by model.activeScanningStaffId.collectAsState()
    val toastMessage by model.toastMessage.collectAsState()
    val exportedFile by model.exportedFilePath.collectAsState()

    var showNotifDrawer by remember { mutableStateOf(false) }

    // Handle incoming toasted alerts
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            model.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CleanHands, contentDescription = "Logo", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("CleanTrack MEP", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("4,120 Staff Active • Pakistan", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                },
                actions = {
                    val isDarkTheme by model.isDarkTheme.collectAsState()
                    IconButton(
                        onClick = { model.toggleTheme() },
                        modifier = Modifier
                            .testTag("theme_toggle")
                            .padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark/Light Theme",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    // Notifications bell button
                    BadgedBox(
                        badge = {
                            if (notifications.isNotEmpty()) {
                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                    Text(notifications.size.toString())
                                }
                            }
                        },
                        modifier = Modifier
                            .clickable { showNotifDrawer = true }
                            .padding(end = 8.dp)
                            .testTag("notification_bell")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    // Sleek Interface Persona Avatar MA
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .background(Color(0xFFEADDFF), CircleShape)
                            .border(1.dp, Color(0xFFD0BCFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("MA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Role switcher tab bar at the absolute bottom (with safe insets padding inside Scaffold)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Text(
                        text = "EXPLORE SEAMLESS MULTI-ROLE FLOW (FOR APPMOBILE REVIEW):",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        PortalSwitcherTab(
                            role = PortalRole.HQ_MANAGER,
                            label = "1. HQ Dashboard",
                            icon = Icons.Default.Analytics,
                            active = currentRole == PortalRole.HQ_MANAGER,
                            onClick = { model.setRole(PortalRole.HQ_MANAGER) }
                        )
                        PortalSwitcherTab(
                            role = PortalRole.LOCATION_MANAGER,
                            label = "2. City Mgr",
                            icon = Icons.Default.SupervisorAccount,
                            active = currentRole == PortalRole.LOCATION_MANAGER,
                            onClick = { model.setRole(PortalRole.LOCATION_MANAGER) }
                        )
                        PortalSwitcherTab(
                            role = PortalRole.FIELD_TEAM,
                            label = "3. Field crew",
                            icon = Icons.Default.Engineering,
                            active = currentRole == PortalRole.FIELD_TEAM,
                            onClick = { model.setRole(PortalRole.FIELD_TEAM) }
                        )
                        PortalSwitcherTab(
                            role = PortalRole.CLIENT,
                            label = "4. Client Portal",
                            icon = Icons.Default.Storefront,
                            active = currentRole == PortalRole.CLIENT,
                            onClick = { model.setRole(PortalRole.CLIENT) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main views rendering based on role selection
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentRole) {
                    PortalRole.HQ_MANAGER -> HQManagerDashboard(model, complaints, inventoryItems, staffMembers, invoices)
                    PortalRole.LOCATION_MANAGER -> LocationManagerDashboard(model, complaints, staffMembers)
                    PortalRole.FIELD_TEAM -> FieldCrewDashboard(model, complaints, staffMembers)
                    PortalRole.CLIENT -> ClientDashboard(model, complaints)
                }
            }

            // Notification Slide Out Sheet Overlay
            AnimatedVisibility(
                visible = showNotifDrawer,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it })
            ) {
                NotificationOverlay(
                    notifications = notifications,
                    onDismissNotif = { model.dismissNotification(it) },
                    onClearAll = { model.clearAllNotifications() },
                    onClose = { showNotifDrawer = false }
                )
            }

            // Biometric Fingerprint scan simulation dialog
            scanningStaffId?.let { staffId ->
                val staff = staffMembers.find { it.id == staffId }
                staff?.let {
                    BiometricSimulationDialog(
                        staffName = it.name,
                        isSignedIn = it.biometricSignedIn,
                        onConfirm = { model.completeBiometricScan(staffId) },
                        onDismiss = { model.activeScanningStaffId.value = null }
                    )
                }
            }

            // Simple details screen overlay for items
            selectedCmpId?.let { cmpId ->
                ComplaintDetailsDialog(
                    complaintId = cmpId,
                    model = model,
                    role = currentRole,
                    staffMembers = staffMembers,
                    onClose = { model.selectedComplaintId.value = null }
                )
            }

            // Report Export Summary Dialog
            exportedFile?.let { filePath ->
                ReportExportSuccessDialog(
                    filePath = filePath,
                    onClose = { model.clearExport() }
                )
            }
        }
    }
}

@Composable
fun PortalSwitcherTab(
    role: PortalRole,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(if (active) 1.0f else 0.6f)
        ) {
            if (active) {
                // Active capsule background pill
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(28.dp)
                        .background(Color(0xFFEADDFF), RoundedCornerShape(14.dp)), // w-16 h-8 style capsule from template
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color(0xFF21005D),
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                // Inactive regular icon
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                color = if (active) Color(0xFF1C1B1F) else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==========================================
// 1. HQ MANAGER DASHBOARD PORTAL
// ==========================================
@Composable
fun HQManagerDashboard(
    model: MainViewModel,
    complaints: List<Complaint>,
    inventory: List<InventoryItem>,
    staff: List<StaffMember>,
    invoices: List<Invoice>
) {
    var activeTab by remember { mutableStateOf("analytics") } // "analytics", "inventory", "invoices"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        // High visibility stats overlay banner
        item {
            HQLedgerHeader(staff, complaints)
        }

        // Action Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                HQMenuTabButton("National Analytics", Icons.Default.Analytics, activeTab == "analytics", onClick = { activeTab = "analytics" })
                HQMenuTabButton("MEP/Janitorial Stock", Icons.Default.Inventory, activeTab == "inventory", onClick = { activeTab = "inventory" })
                HQMenuTabButton("Accounts & Bills", Icons.Default.ReceiptLong, activeTab == "invoices", onClick = { activeTab = "invoices" })
            }
        }

        when (activeTab) {
            "analytics" -> {
                item {
                    CanvasDashboardCharts(complaints = complaints)
                }
                item {
                    PakistanMapSchematicBoard(complaints = complaints, onCitySelected = { cityName ->
                        model.addNotification("Filter Map", "Selected service hub filter: $cityName", "System")
                    })
                }
                item {
                    // Export PDF / CSV Analytics controls
                    ExportPerformanceLogsCard(onExport = { format -> model.exportPerformanceData(format) })
                }
                item {
                    HQStaffOverviewList(staff)
                }
            }
            "inventory" -> {
                item {
                    HQInventoryManagementSection(model, inventory)
                }
            }
            "invoices" -> {
                item {
                    HQAccountsListing(invoices)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

@Composable
fun HQLedgerHeader(staff: List<StaffMember>, complaints: List<Complaint>) {
    val totalStaff = 4325 // High fidelity display matches Pakistan stats
    val activeDuty = staff.count { it.biometricSignedIn }
    val resolved = complaints.count { it.status == "Completed" || it.status == "Done" }
    val backlog = complaints.count { it.status != "Completed" && it.status != "Done" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bento Card 1: Active Staff (White card, sleek border, accent values)
        Card(
            modifier = Modifier
                .weight(1f)
                .height(115.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), // Dynamic border style
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE STAFF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        letterSpacing = 0.5.sp
                    )
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Staff Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$activeDuty",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+12%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Bento Card 2: Pending Backlog (Lavender Card matching the prototype style)
        Card(
            modifier = Modifier
                .weight(1f)
                .height(115.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BACKLOG TASKS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )
                    Icon(
                        imageVector = Icons.Default.PendingActions,
                        contentDescription = "Backlog Icon",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "$backlog",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun HQMenuTabButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ExportPerformanceLogsCard(onExport: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)) // Sleek dark slate-900 style background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = "Report",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Monthly Report Ready",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Janitorial & MEP analytics tracker",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.62f)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onExport("pdf") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)), // Indigo accent button from template website mockup
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "EXPORT PDF",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun HQStaffOverviewList(staff: List<StaffMember>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Regional Team Leads Map Status",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text("Total: 4,000+ Deployed", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))

            staff.forEach { member ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (member.biometricSignedIn) Color(0xFF2ECC71).copy(0.15f) else Color.Gray.copy(0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (member.biometricSignedIn) Icons.Default.CheckCircle else Icons.Default.AccountCircle,
                                contentDescription = "Sig",
                                tint = if (member.biometricSignedIn) Color(0xFF2ECC71) else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(member.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${member.designation} • ${member.branch.take(18)}..", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                if (member.biometricSignedIn) Color(0xFF2ECC71).copy(0.2f) else Color.Gray.copy(0.15f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (member.biometricSignedIn) "ACTIVE" else "OFFLINE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (member.biometricSignedIn) Color(0xFF1E8449) else Color.DarkGray
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
            }
        }
    }
}

@Composable
fun HQInventoryManagementSection(model: MainViewModel, inventory: List<InventoryItem>) {
    var activeFreqFilter by remember { mutableStateOf("All") } // "All", "Daily", "Weekly", "Monthly"
    var showAddItemPopup by remember { mutableStateOf(false) }

    // Popup input variables
    var newItemName by remember { mutableStateOf("") }
    var newItemCategory by remember { mutableStateOf("Consumables") } // Consumables, Equipments, Tools, Machinery
    var newItemFreq by remember { mutableStateOf("Daily") }
    var newItemQty by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Operation Ledgers: Consumables & Machinery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showAddItemPopup = true }) {
                Icon(Icons.Default.AddCircle, contentDescription = "Add Item", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Frequency Filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("All", "Daily", "Weekly", "Monthly").forEach { freq ->
                FilterChip(
                    selected = activeFreqFilter == freq,
                    onClick = { activeFreqFilter = freq },
                    label = { Text(freq, fontSize = 11.sp) }
                )
            }
        }

        if (showAddItemPopup) {
            AlertDialog(
                onDismissRequest = { showAddItemPopup = false },
                title = { Text("Log New Resource Utility", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newItemName,
                            onValueChange = { newItemName = it },
                            label = { Text("Item / Machine Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newItemQty,
                            onValueChange = { newItemQty = it },
                            label = { Text("Quantity Detail (e.g. 15 Liters, 4 Units)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Classification Hierarchy:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Consumables", "Equipments", "Tools", "Machinery").forEach { cat ->
                                Button(
                                    onClick = { newItemCategory = cat },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (newItemCategory == cat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 2.dp)
                                ) {
                                    Text(cat.take(9), fontSize = 9.sp, color = if (newItemCategory == cat) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Text("Audit Frequency:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Daily", "Weekly", "Monthly").forEach { frq ->
                                Button(
                                    onClick = { newItemFreq = frq },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (newItemFreq == frq) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(frq, fontSize = 10.sp, color = if (newItemFreq == frq) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newItemName.isNotBlank() && newItemQty.isNotBlank()) {
                                model.addInventoryItem(newItemName, newItemCategory, newItemFreq, newItemQty)
                                showAddItemPopup = false
                                newItemName = ""
                                newItemQty = ""
                            }
                        }
                    ) {
                        Text("Add Resource")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddItemPopup = false }) { Text("Cancel") }
                }
            )
        }

        // Filter and display items
        val filteredItems = if (activeFreqFilter == "All") inventory else inventory.filter { it.frequency == activeFreqFilter }

        if (filteredItems.isEmpty()) {
            Text("No registered resource audits under schedule frequency.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
        } else {
            filteredItems.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                when (item.category) {
                                                    "Consumables" -> Color(0xFF3498DB)
                                                    "Equipments" -> Color(0xFF9B59B6)
                                                    "Tools" -> Color(0xFFF1C40F)
                                                    else -> Color(0xFFE67E22)
                                                },
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("Class: ${item.category} • Schedule: ${item.frequency}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            // Interactive Status Switcher
                            var expandedStatusMenu by remember { mutableStateOf(false) }
                            Box {
                                AssistChip(
                                    onClick = { expandedStatusMenu = true },
                                    label = { Text(item.status, fontSize = 10.sp) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        labelColor = when(item.status) {
                                            "Available" -> Color(0xFF2E7D32)
                                            "In Use" -> Color(0xFF1565C0)
                                            "Maintenance Required" -> Color(0xFFC62828)
                                            else -> Color(0xFFEF6C00)
                                        }
                                    )
                                )
                                DropdownMenu(
                                    expanded = expandedStatusMenu,
                                    onDismissRequest = { expandedStatusMenu = false }
                                ) {
                                    listOf("Available", "In Use", "Maintenance Required", "Out of Stock").forEach { status ->
                                        DropdownMenuItem(
                                            text = { Text(status, fontSize = 12.sp) },
                                            onClick = {
                                                model.updateInventoryStatus(item, status)
                                                expandedStatusMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Current Stock: ${item.quantityDetail}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(item.lastCheckedDate))
                            Text("Last checked: $formattedDate", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HQAccountsListing(invoices: List<Invoice>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Billing Ledgers & Auto-Invoiced Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Real-time generated financial logs against client bank branches.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))

        if (invoices.isEmpty()) {
            Text("No active invoices registered yet. Create and approve branch jobs to generate billing sheets.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
        } else {
            invoices.forEach { invoice ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("INVOICE #2026-${100 + invoice.id}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = "Rs. ${invoice.amount}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Branch Account: ${invoice.branchName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Billed Service: ${invoice.details}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = "Email", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Transmitted to: ${invoice.sentToEmail}", fontSize = 9.sp, color = Color.Gray)
                            }
                            val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(invoice.generatedAt))
                            Text(dateStr, fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 2. LOCATION MANAGER PORTAL (CITY LEVEL)
// ==========================================
@Composable
fun LocationManagerDashboard(
    model: MainViewModel,
    complaints: List<Complaint>,
    staff: List<StaffMember>
) {
    var activeCityFilter by remember { mutableStateOf("All Cities") }
    var selectedComplaintForAssignment by remember { mutableStateOf<Complaint?>(null) }

    val activeComplaints = complaints.filter {
        it.status != "Completed" && (activeCityFilter == "All Cities" || it.city.lowercase() == activeCityFilter.lowercase())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        item {
            Text(
                text = "City Operational Queue (Dispatch Board)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }

        // City Selector Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All Cities", "Karachi", "Lahore", "Islamabad", "Peshawar", "Rawalpindi").forEach { city ->
                    FilterChip(
                        selected = activeCityFilter == city,
                        onClick = { activeCityFilter = city },
                        label = { Text(city, fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (city == "All Cities") Icons.Default.Public else Icons.Default.LocationOn,
                                contentDescription = city,
                                modifier = Modifier.size(14.dp),
                                tint = if (activeCityFilter == city) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }

        if (activeComplaints.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudDone, contentDescription = "Clean", tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Text(
                            text = "City service grid secure! No active complaints logged.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(activeComplaints) { cmp ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("complaint_card_${cmp.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        when (cmp.urgency) {
                                            "High" -> UrgencyHigh.copy(0.12f)
                                            "Medium" -> UrgencyMedium.copy(0.12f)
                                            else -> UrgencyLow.copy(0.12f)
                                        },
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${cmp.urgency.uppercase()} URGENCY",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (cmp.urgency) {
                                        "High" -> UrgencyHigh
                                        "Medium" -> UrgencyMedium
                                        else -> UrgencyLow
                                    }
                                )
                            }

                            Text(
                                text = cmp.status.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(cmp.branchName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(cmp.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Division: ${cmp.category}", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                            if (cmp.status == "Pending") {
                                Button(
                                    onClick = { selectedComplaintForAssignment = cmp },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(32.dp).testTag("assign_button_${cmp.id}")
                                ) {
                                    Icon(Icons.Default.DirectionsRun, contentDescription = "Run", modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Assign Nearest Crew", fontSize = 10.sp)
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = "Assigned", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Deployed: ${cmp.assignedStaffName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }

    // Assign Staff Selection Modal Dialog
    selectedComplaintForAssignment?.let { cmp ->
        AlertDialog(
            onDismissRequest = { selectedComplaintForAssignment = null },
            title = { Text("Deploy Field Specialist", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select from nearest active nationwide staff available:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                    staff.forEach { staffMember ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    model.assignComplaint(cmp.id, staffMember.id, staffMember.name)
                                    selectedComplaintForAssignment = null
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(staffMember.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("${staffMember.designation} • ${staffMember.branch}", fontSize = 11.sp, color = Color.Gray)
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (staffMember.biometricSignedIn) Color(0xFF2ECC71).copy(0.15f) else Color.Gray.copy(0.12f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (staffMember.biometricSignedIn) "Active Scan" else "Offline",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (staffMember.biometricSignedIn) Color(0xFF27AE60) else Color.DarkGray
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedComplaintForAssignment = null }) { Text("Cancel") }
            }
        )
    }
}


// ==========================================
// 3. FIELD CREW PORTAL (MOBILE FIELD CREW)
// ==========================================
@Composable
fun FieldCrewDashboard(
    model: MainViewModel,
    complaints: List<Complaint>,
    staff: List<StaffMember>
) {
    // We'll let the user simulate running as ONE specific Field Crew (e.g. staff ID 1, Muhammad Ali)
    // To allow evaluating different perspectives, they can switch active staff!
    var selectedActiveStaffId by remember { mutableStateOf(1) }
    val activeStaff = staff.find { it.id == selectedActiveStaffId } ?: staff.firstOrNull()

    var showBiometricScanner by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        // Staff Profile and Biometric Swiper
        activeStaff?.let { member ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Field Engineer Workstation", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Selector to switch staff profile
                                    Text(member.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Switch Profile",
                                        modifier = Modifier
                                            .clickable {
                                                val nextId = if (selectedActiveStaffId < staff.size) selectedActiveStaffId + 1 else 1
                                                selectedActiveStaffId = nextId
                                            }
                                            .padding(horizontal = 4.dp)
                                    )
                                }
                                Text("Title: ${member.designation} • ${member.phone}", fontSize = 11.sp, color = Color.Gray)
                            }

                            // Biometric Button
                            Button(
                                onClick = { model.triggerBiometricScan(member.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (member.biometricSignedIn) Color(0xFF2ECC71) else Color(0xFFE74C3C)
                                )
                            ) {
                                Icon(Icons.Default.Fingerprint, contentDescription = "Bio", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (member.biometricSignedIn) "IN" else "SECURE IN", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // Task list assigned to this field engineer
            val assignedTasks = complaints.filter { it.assignedStaffId == member.id && it.status != "Completed" }

            item {
                Text(
                    text = "My Shift Assignments (${assignedTasks.size} Active)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            if (!member.biometricSignedIn) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .background(MaterialTheme.colorScheme.error.copy(0.04f), RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.error.copy(0.2f), RoundedCornerShape(10.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "BIOMETRIC AUTHENTICATION REQUIRED",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "To comply with attendance accuracy and bank branch security protocols, you must scan your fingerprint using biometric authentication above to access job details.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else if (assignedTasks.isEmpty()) {
                item {
                    Text(
                        "No tasks registered in your sector workload currently.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(assignedTasks) { task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { model.selectedComplaintId.value = task.id },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (task.status == "In Progress") Color(0xFFFFA726).copy(0.12f) else Color(0xFF2ECC71).copy(0.12f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = task.status.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (task.status == "In Progress") Color(0xFFE65100) else Color(0xFF1B5E20)
                                    )
                                }
                                Text("ID: #T-${task.id}", fontSize = 10.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(task.branchName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(task.description, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = "Gps", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Geofence: Valid Status", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "Perform Work →",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}


// ==========================================
// 4. CLIENT PORTAL (BANK INCHARGE OR BRANCH MGR)
// ==========================================
@Composable
fun ClientDashboard(
    model: MainViewModel,
    complaints: List<Complaint>
) {
    var showLodgeReportBlock by remember { mutableStateOf(false) }

    // Logic fields for lodged report
    val pakBranches = listOf(
        "HBL - I.I. Chundrigar Road, Karachi",
        "Alfalah Bank - Gulberg Boulevard, Lahore",
        "MCB Bank - Blue Area, Islamabad",
        "UBL - Saddar Road, Peshawar",
        "NBP - The Mall, Rawalpindi",
        "Meezan Bank - Satiyana Road, Faisalabad"
    )
    var activeBranchLink by remember { mutableStateOf(pakBranches[0]) }
    var inputDesc by remember { mutableStateOf("") }
    var activeDivision by remember { mutableStateOf("Janitorial") } // Janitorial, MEP - Electrical, MEP - Mechanical, MEP - Plumbing
    var activeUrg by remember { mutableStateOf("Medium") } // Low, Medium, High

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Branch Operations: Secure Portal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { showLodgeReportBlock = !showLodgeReportBlock },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(if (showLodgeReportBlock) Icons.Default.Cancel else Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showLodgeReportBlock) "Hide Form" else "Lodge Complaint")
                }
            }
        }

        if (showLodgeReportBlock) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Lodge Branch Service Request", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Branch Select
                        Text("1. Confirm Your Pakistani Bank Branch Location:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        var expandedBranchDropDown by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = { expandedBranchDropDown = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(activeBranchLink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Down")
                            }
                            DropdownMenu(
                                expanded = expandedBranchDropDown,
                                onDismissRequest = { expandedBranchDropDown = false }
                            ) {
                                pakBranches.forEach { b ->
                                    DropdownMenuItem(
                                        text = { Text(b) },
                                        onClick = {
                                            activeBranchLink = b
                                            expandedBranchDropDown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category select
                        Text("2. Facility Division classification:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Janitorial", "MEP - Electrical", "MEP - Mechanical", "MEP - Plumbing").forEach { div ->
                                Button(
                                    onClick = { activeDivision = div },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (activeDivision == div) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = div.replace("MEP - ", ""),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (activeDivision == div) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Urgency level
                        Text("3. Problem Severity Priority Level:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Low", "Medium", "High").forEach { urg ->
                                Button(
                                    onClick = { activeUrg = urg },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (activeUrg == urg) {
                                            when (urg) {
                                                "High" -> UrgencyHigh
                                                "Medium" -> UrgencyMedium
                                                else -> UrgencyLow
                                            }
                                        } else MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = urg,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (activeUrg == urg) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Detailed Fault description
                        OutlinedTextField(
                            value = inputDesc,
                            onValueChange = { inputDesc = it },
                            label = { Text("Describe the maintenance fault exactly (e.g. electrical bypass, AC chiller noise, washroom drainage clogging)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (inputDesc.isNotBlank()) {
                                    val city = if (activeBranchLink.contains("Karachi")) "Karachi"
                                    else if (activeBranchLink.contains("Lahores")) "Lahore"
                                    else if (activeBranchLink.contains("Islamabad")) "Islamabad"
                                    else if (activeBranchLink.contains("Peshawar")) "Peshawar"
                                    else if (activeBranchLink.contains("Rawalpindi")) "Rawalpindi"
                                    else "Faisalabad"

                                    model.createComplaint(
                                        branch = activeBranchLink,
                                        city = city,
                                        category = activeDivision,
                                        urgency = activeUrg,
                                        description = inputDesc
                                    )
                                    inputDesc = ""
                                    showLodgeReportBlock = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Deploy Operational Request & Ping Dispatcher")
                        }
                    }
                }
            }
        }

        // Active complaints list logged by client
        item {
            Text(
                text = "My Active Service Requests Grid",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (complaints.isEmpty()) {
            item {
                Text("No service tickets logged from your branch credentials currently.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(16.dp))
            }
        } else {
            items(complaints) { ticket ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { model.selectedComplaintId.value = ticket.id },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("TICKET #${ticket.id}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .background(
                                        when (ticket.status) {
                                            "Completed" -> Color(0xFF2E7D32).copy(0.12f)
                                            "Done" -> Color(0xFF1565C0).copy(0.12f)
                                            else -> Color(0xFFC62828).copy(0.12f)
                                        },
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = ticket.status.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = when (ticket.status) {
                                        "Completed" -> Color(0xFF2E7D32)
                                        "Done" -> Color(0xFF1565C0)
                                        else -> Color(0xFFC62828)
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(ticket.branchName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(ticket.description, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Division: ${ticket.category} • Urgency: ${ticket.urgency}", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                "Manage Ticket →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}


// ==========================================
// NOTIFICATION OVERLAY PANEL DRAWER
// ==========================================
@Composable
fun NotificationOverlay(
    notifications: List<AppNotification>,
    onDismissNotif: (String) -> Unit,
    onClearAll: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onClose)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.82f)
                .background(MaterialTheme.colorScheme.background)
                .align(Alignment.CenterEnd)
                .clickable(enabled = false) { }
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = "Alert", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Real-Time Dispatch logs", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Temporary push logs screen", fontSize = 10.sp, color = Color.Gray)
                    TextButton(onClick = onClearAll) {
                        Text("Clear All Logs", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                if (notifications.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("No active real-time push logs logged context.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(notifications) { notif ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = notif.type.uppercase(),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        IconButton(
                                            onClick = { onDismissNotif(notif.id) },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Del", tint = Color.LightGray, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                    Text(notif.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(notif.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// COMPLAINT DETAILS / MANAGEMENT FLOW SCREEN
// ==========================================
@Composable
fun ComplaintDetailsDialog(
    complaintId: Int,
    model: MainViewModel,
    role: PortalRole,
    staffMembers: List<StaffMember>,
    onClose: () -> Unit
) {
    val complaints by model.complaints.collectAsState()
    val ticket = complaints.find { it.id == complaintId } ?: return

    val chatMessages by model.activeChatMessages.collectAsState()
    var inputChatMessage by remember { mutableStateOf("") }

    // Task simulation parameters
    var inputRemarks by remember { mutableStateOf("") }
    var activeBeforePictureSelection by remember { mutableStateOf<String?>(ticket.beforePicture) }
    var activeAfterPictureSelection by remember { mutableStateOf<String?>(ticket.afterPicture) }
    var inputSignatureSelection by remember { mutableStateOf<String>(ticket.digitalSignature ?: "") }

    // Client approval rating
    var clientRating by remember { mutableStateOf(5) }

    LaunchedEffect(complaintId) {
        model.activeChatComplaintId.value = complaintId
    }

    AlertDialog(
        onDismissRequest = onClose,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Operational Job Card #T-${ticket.id}", fontSize = 16.sp, fontWeight = FontWeight.Black)
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "Close") }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Summary Block
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.3f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(ticket.branchName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(ticket.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Category: ${ticket.category} • Urgency Priority: ${ticket.urgency}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("State Status: ${ticket.status.uppercase()}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Before vs After Gallery Slider
                if (ticket.beforePicture != null || activeBeforePictureSelection != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text("Before Repair Photo", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp)
                                        .background(Color.Red.copy(0.08f), RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = "Issue", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = (ticket.beforePicture ?: activeBeforePictureSelection ?: "").take(20),
                                        fontSize = 8.sp,
                                        modifier = Modifier.align(Alignment.BottomCenter).padding(4.dp)
                                    )
                                }
                            }
                        }

                        if (ticket.afterPicture != null || activeAfterPictureSelection != null) {
                            Card(modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text("After Repair Photo", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(72.dp)
                                            .background(Color.Green.copy(0.08f), RoundedCornerShape(4.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Clean", tint = Color.Green, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = (ticket.afterPicture ?: activeAfterPictureSelection ?: "").take(20),
                                            fontSize = 8.sp,
                                            modifier = Modifier.align(Alignment.BottomCenter).padding(4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ROLE TAILORED ACTIONS
                when (role) {
                    PortalRole.FIELD_TEAM -> {
                        // Field engineer workflow
                        if (ticket.status == "Assigned" || ticket.status == "In Progress") {
                            Text("FIELD WORKER WORKFLOW CONTROL:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                            // 1. Before picture
                            InteractivePhotoSelector(
                                title = "1. Record Site Before Repair Photo",
                                currentSelection = activeBeforePictureSelection ?: ticket.beforePicture,
                                category = ticket.category,
                                isResult = false,
                                onPhotoSelected = {
                                    activeBeforePictureSelection = if (it.isBlank()) null else it
                                    if (it.isNotBlank()) model.updateBeforePicture(ticket.id, it)
                                }
                            )

                            if (ticket.status == "In Progress") {
                                // 2. Checklist
                                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("2. Facility Field Checklist", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = "V", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("MEP safety gear worn / sanitize materials loaded", fontSize = 11.sp)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = "V", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Branch Manager logged arrival & site clearance given", fontSize = 11.sp)
                                        }
                                    }
                                }

                                // 3. After photo
                                InteractivePhotoSelector(
                                    title = "3. Record Job Accomplishment After Photo",
                                    currentSelection = activeAfterPictureSelection ?: ticket.afterPicture,
                                    category = ticket.category,
                                    isResult = true,
                                    onPhotoSelected = { activeAfterPictureSelection = if (it.isBlank()) null else it }
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // 4. Remarks
                                OutlinedTextField(
                                    value = inputRemarks,
                                    onValueChange = { inputRemarks = it },
                                    label = { Text("Engineering Field Remarks / Spares Used") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 2
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // 5. Signature pad
                                SignaturePad(
                                    modifier = Modifier.fillMaxWidth(),
                                    onSignatureCaptured = { inputSignatureSelection = it }
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Submit action
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (activeAfterPictureSelection != null && inputSignatureSelection.isNotBlank()) {
                                                model.completeTask(
                                                    ticket.id,
                                                    activeAfterPictureSelection!!,
                                                    inputRemarks.ifBlank { "Job resolved cleanly." },
                                                    inputSignatureSelection
                                                )
                                                // Automatic instant message notifying Client
                                                model.sendChatMessage(ticket.id, "COMPLETION UPDATE: Issue resolved. Before/after photos loaded and signed. Please verify and authorize checkout invoice.", "Staff", ticket.assignedStaffName ?: "Field Tech")
                                                onClose()
                                            } else {
                                                model.toastMessage.value = "Required: Ensure After Photo Captured and Signature Verified."
                                            }
                                        },
                                        modifier = Modifier.weight(1.3f)
                                    ) {
                                        Text("Authorize Job Done", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            if (inputRemarks.isNotBlank()) {
                                                model.updateTaskPending(ticket.id, inputRemarks)
                                                onClose()
                                            } else {
                                                model.toastMessage.value = "Enter remarks to document the delay reasons."
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Log Delay / Pending", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                    PortalRole.CLIENT -> {
                        // Client approval stage
                        if (ticket.status == "Done" && !ticket.clientApproved) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("FIELD WORK ASSIGNMENT DECLARED DONE!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                                    Text("Verify the Before vs After photo evidence log and clear the invoice bill:", fontSize = 11.sp, color = Color.Gray)
                                    Text("Crew Remarks: ${ticket.remarks ?: "N/A"}", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Rating
                                    Text("Rate Crew Performance Experience (1-5 Stars):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        (1..5).forEach { star ->
                                            IconButton(onClick = { clientRating = star }) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Star",
                                                    tint = if (star <= clientRating) Color(0xFFF1C40F) else Color.LightGray
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            model.approveComplaint(ticket.id, clientRating)
                                            model.sendChatMessage(ticket.id, "CLIENT APPROVED: Branch Manager signed off resolution with $clientRating/5 feedback stars score.", "Client", "S. Afridi (Branch Mgr)")
                                            onClose()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Authorize and Checkout Job Completed")
                                    }
                                }
                            }
                        }
                    }
                    else -> {}
                }

                Spacer(modifier = Modifier.height(12.dp))

                // LIVE COORDINATION CHAT DRAWER
                Text("Branch Live Coordination Message Stream", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Messages Scroll box
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (chatMessages.isEmpty()) {
                                item {
                                    Text("Secure chat channel active. Keep communication details mapped here.", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(8.dp))
                                }
                            } else {
                                items(chatMessages) { msg ->
                                    val isClient = msg.senderRole == "Client"
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = if (isClient) Alignment.End else Alignment.Start
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isClient) MaterialTheme.colorScheme.primary.copy(0.12f) else MaterialTheme.colorScheme.secondary.copy(0.15f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Column {
                                                Text(msg.senderName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                Text(msg.messageText, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Send input field row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputChatMessage,
                                onValueChange = { inputChatMessage = it },
                                placeholder = { Text("Message...", fontSize = 11.sp) },
                                modifier = Modifier
                                    .weight(1f),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                            )
                            IconButton(
                                onClick = {
                                    if (inputChatMessage.isNotBlank()) {
                                        val roleStr = if (role == PortalRole.CLIENT) "Client" else "Staff"
                                        val nameStr = if (role == PortalRole.CLIENT) "Branch Manager" else (ticket.assignedStaffName ?: "Field Crew")
                                        model.sendChatMessage(ticket.id, inputChatMessage, roleStr, nameStr)
                                        inputChatMessage = ""
                                    }
                                },
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}


// ==========================================
// ADHOC HELPERS / DIALOGS
// ==========================================
@Composable
fun BiometricSimulationDialog(
    staffName: String,
    isSignedIn: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Biometric Authentication Required", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Press below to simulate scanning fingerprint scanner of bank branch attendance device to ${if(isSignedIn) "Clock out" else "Clock in"} $staffName.",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(0.1f), CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { onConfirm() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Scan",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("SCAN MODULE ONLINE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ReportExportSuccessDialog(
    filePath: String,
    onClose: () -> Unit
) {
    val file = File(filePath)
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.UploadFile, contentDescription = "Doc", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Report Exported Securely", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text("Nationwide Monthly Business performance analytics ledger generated successfully:", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text("File: ${file.name}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Storage: Application Local Sandbox", fontSize = 10.sp, color = Color.Gray)
                        Text("Size: ${file.length()} Bytes", fontSize = 10.sp, color = Color.Gray)
                        Text("Transmitting back to Central Office database nodes...", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("This file compiles high confidence telemetry including signature checkpoints, parts logs, and MEP billing sheets.", fontSize = 11.sp, color = Color.Gray)
            }
        },
        confirmButton = {
            Button(onClick = onClose) { Text("Done") }
        }
    )
}
