package com.example.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

enum class PortalRole {
    CLIENT,
    LOCATION_MANAGER,
    FIELD_TEAM,
    HQ_MANAGER
}

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String // "Complaint", "Assignment", "Update", "System"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = AppRepository(database)

    // Active screen navigation state
    val currentRole = MutableStateFlow(PortalRole.HQ_MANAGER) // Default HQ Manager for preview
    val selectedComplaintId = MutableStateFlow<Int?>(null)
    val activeChatComplaintId = MutableStateFlow<Int?>(null)

    // Flow objects
    val complaints: StateFlow<List<Complaint>> = repository.complaints
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventoryItems: StateFlow<List<InventoryItem>> = repository.inventoryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val staffMembers: StateFlow<List<StaffMember>> = repository.staffMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<Invoice>> = repository.invoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notification queue for simulation
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Active chat state
    val activeChatMessages = activeChatComplaintId
        .flatMapLatest { id ->
            if (id != null) repository.getChatMessages(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated Biometric Scan State for staff
    val activeScanningStaffId = MutableStateFlow<Int?>(null)

    // Simulation feedback
    val toastMessage = MutableStateFlow<String?>(null)

    // Export status
    val exportedFilePath = MutableStateFlow<String?>(null)

    // Dark/Light Theme selection flow
    val isDarkTheme = MutableStateFlow(false)

    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
    }

    init {
        seedInitialData()
        simulateBackgroundOperations()
    }

    private fun seedInitialData() {
        viewModelScope.launch {
            // Check and seed staff members
            repository.staffMembers.first().let { currentStaff ->
                if (currentStaff.isEmpty()) {
                    val initialStaff = listOf(
                        StaffMember(name = "Muhammad Ali", designation = "MEP Senior Technician", phone = "+92-300-1234567", branch = "HBL I.I. Chundrigar Rd Karachi", currentLat = 24.8607, currentLng = 67.0011),
                        StaffMember(name = "Zubair Khan", designation = "Janitorial & Cleaning Lead", phone = "+92-321-8765432", branch = "Alfalah Gulberg Blvd Lahore", currentLat = 31.5204, currentLng = 74.3587),
                        StaffMember(name = "Ayesha Siddiqui", designation = "HVAC & Electrical Engineer", phone = "+92-333-5551234", branch = "MCB Blue Area Islamabad", currentLat = 33.6844, currentLng = 73.0479),
                        StaffMember(name = "Haris Ahmed", designation = "MEP Plumbing Technician", phone = "+92-345-9876543", branch = "UBL Saddar Road Peshawar", currentLat = 34.0151, currentLng = 71.5249),
                        StaffMember(name = "Kamran Shah", designation = "Janitorial Specialist", phone = "+92-301-4448888", branch = "NBP The Mall Rawalpindi", currentLat = 33.5984, currentLng = 73.0441)
                    )
                    initialStaff.forEach { repository.insertStaffMember(it) }
                }
            }

            // Check and seed inventory
            repository.inventoryItems.first().let { items ->
                if (items.isEmpty()) {
                    val initialInventory = listOf(
                        // Consumables
                        InventoryItem(name = "Industrial Detergent & Sanitizer", category = "Consumables", frequency = "Daily", status = "Available", quantityDetail = "85 Liters"),
                        InventoryItem(name = "HEPA Filter Disinfectant", category = "Consumables", frequency = "Weekly", status = "Available", quantityDetail = "30 Cans"),
                        InventoryItem(name = "Microfiber Cleaning Mops", category = "Consumables", frequency = "Daily", status = "In Use", quantityDetail = "50 Units"),
                        // Equipments
                        InventoryItem(name = "Heavy Duty Floor Buffing Machine", category = "Equipments", frequency = "Weekly", status = "Maintenance Required", quantityDetail = "2 Units"),
                        InventoryItem(name = "Industrial Jet Vacuum Cleaner", category = "Equipments", frequency = "Daily", status = "Available", quantityDetail = "6 Units"),
                        // Tools
                        InventoryItem(name = "Complete MEP Plumbing Toolset", category = "Tools", frequency = "Monthly", status = "Available", quantityDetail = "4 Kits"),
                        InventoryItem(name = "Digital Clamp Meters & Wire Strippers", category = "Tools", frequency = "Weekly", status = "Available", quantityDetail = "8 Units"),
                        // Machinery
                        InventoryItem(name = "Central HVAC Ductor Blower Unit", category = "Machinery", frequency = "Monthly", status = "In Use", quantityDetail = "1 Unit")
                    )
                    initialInventory.forEach { repository.insertInventoryItem(it) }
                }
            }

            // Check and seed initial complaints
            repository.complaints.first().let { complaintList ->
                if (complaintList.isEmpty()) {
                    val seedComplaints = listOf(
                        Complaint(
                            branchName = "HBL - I.I. Chundrigar Road, Karachi",
                            city = "Karachi",
                            description = "Central Air Conditioning chiller unit has a refrigerant leak leading to poor cooling in main teller area.",
                            urgency = "High",
                            category = "MEP - Mechanical",
                            status = "Pending",
                            clientGeoLocation = "24.8601, 67.0019",
                            timeOfComplaint = System.currentTimeMillis() - 7200000 // 2 hrs ago
                        ),
                        Complaint(
                            branchName = "Alfalah Bank - Gulberg Boulevard, Lahore",
                            city = "Lahore",
                            description = "Scheduled deep tile sanitization and polishing overdue for safe banker lounge and branch floor.",
                            urgency = "Medium",
                            category = "Janitorial",
                            status = "Assigned",
                            clientGeoLocation = "31.5204, 74.3587",
                            timeOfComplaint = System.currentTimeMillis() - 14400000, // 4 hrs ago
                            assignedStaffId = 2,
                            assignedStaffName = "Zubair Khan"
                        ),
                        Complaint(
                            branchName = "MCB Bank - Blue Area, Islamabad",
                            city = "Islamabad",
                            description = "Short circuit in the electrical panel leading to UPS battery bypass offline failure.",
                            urgency = "High",
                            category = "MEP - Electrical",
                            status = "In Progress",
                            clientGeoLocation = "33.6844, 73.0479",
                            timeOfComplaint = System.currentTimeMillis() - 28800000, // 8 hrs ago
                            assignedStaffId = 3,
                            assignedStaffName = "Ayesha Siddiqui",
                            beforePicture = "MOCK_ELECTRICAL_MESS"
                        ),
                        Complaint(
                            branchName = "UBL - Saddar Road, Peshawar",
                            city = "Peshawar",
                            description = "Water sewage pipeline blocked in washroom area triggering basement water seepage.",
                            urgency = "High",
                            category = "MEP - Plumbing",
                            status = "Completed",
                            clientGeoLocation = "34.0151, 71.5249",
                            timeOfComplaint = System.currentTimeMillis() - 86400000, // 1 day ago
                            assignedStaffId = 4,
                            assignedStaffName = "Haris Ahmed",
                            beforePicture = "MOCK_DRAIN_BEFORE",
                            afterPicture = "MOCK_DRAIN_AFTER",
                            digitalSignature = "Signed: Manager S. Afridi",
                            remarks = "Replaced 3-inch PVC pipeline coupling completely.",
                            clientApproved = true,
                            feedbackRating = 5
                        )
                    )
                    seedComplaints.forEach { cmp ->
                        val cmpId = repository.insertComplaint(cmp).toInt()
                        // Insert mock chat messages for completed items
                        if (cmp.status == "Completed") {
                            repository.insertChatMessage(ChatMessage(complaintId = cmpId, senderName = "Haris Ahmed", senderRole = "Staff", messageText = "Nearest Plumber Haris deployed. Rushing to the branch site."))
                            repository.insertChatMessage(ChatMessage(complaintId = cmpId, senderName = "Haris Ahmed", senderRole = "Staff", messageText = "Arrived. The before image is logged. Starting excavation and clearing."))
                            repository.insertChatMessage(ChatMessage(complaintId = cmpId, senderName = "Haris Ahmed", senderRole = "Staff", messageText = "Issue resolved, leak plugged. Branch incharge has signed completion status."))
                            repository.insertChatMessage(ChatMessage(complaintId = cmpId, senderName = "S. Afridi (Branch Incharge)", senderRole = "Client", messageText = "Great responsive work. Approved! Thanks CleanTrack."))
                        }
                    }

                    // Seed an invoice as well
                    repository.insertInvoice(
                        Invoice(
                            complaintId = 4,
                            branchName = "UBL - Saddar Road, Peshawar",
                            amount = 8500.0,
                            details = "MEP Maintenance: PVC Pipeline replacement, leakage plugging, and floor wash dry-up. (Part #PV302 - Rs.4500, Labor - Rs.4000)",
                            sentToEmail = "peshawar_ubl@ubl.com.pk"
                        )
                    )
                }
            }

            // Add first welcome notification
            addNotification(
                title = "System Ready",
                message = "CleanTrack MEP Pakistan network monitoring system active. 4,000+ staff configured online.",
                type = "System"
            )
        }
    }

    private fun simulateBackgroundOperations() {
        // Just simulates some interval notification checks to make it live and interactive
        viewModelScope.launch {
            // Emulate temporary notices randomly or when states change
        }
    }

    // Role switcher
    fun setRole(role: PortalRole) {
        currentRole.value = role
        addNotification(
            title = "Portal Switch",
            message = "Switched dashboard presentation dynamically to ${role.name.replace("_", " ")} portal.",
            type = "System"
        )
    }

    // Add Notification helper
    fun addNotification(title: String, message: String, type: String) {
        val newNotif = AppNotification(title = title, message = message, type = type)
        val currentList = _notifications.value.toMutableList()
        currentList.add(0, newNotif) // insertion order newest first
        _notifications.value = currentList
    }

    fun dismissNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.id != id }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    // Client functions
    fun createComplaint(
        branch: String,
        city: String,
        category: String,
        urgency: String,
        description: String,
        lat: String = "24.8607",
        lng: String = "67.0011"
    ) {
        viewModelScope.launch {
            val complaint = Complaint(
                branchName = branch,
                city = city,
                category = category,
                urgency = urgency,
                description = description,
                status = "Pending",
                clientGeoLocation = "$lat, $lng",
                timeOfComplaint = System.currentTimeMillis()
            )
            val newId = repository.insertComplaint(complaint).toInt()

            addNotification(
                title = "New Complaint Logged",
                message = "[${urgency} Urgency] Complaint filed for $branch ($city). Request ID: #$newId",
                type = "Complaint"
            )
            toastMessage.value = "Complaint #$newId registered successfully!"
        }
    }

    // Manager / Staff functions: Assign staff to complaint
    fun assignComplaint(id: Int, staffId: Int, staffName: String) {
        viewModelScope.launch {
            val complaint = repository.getComplaintById(id)
            if (complaint != null) {
                val updated = complaint.copy(
                    status = "Assigned",
                    assignedStaffId = staffId,
                    assignedStaffName = staffName
                )
                repository.updateComplaint(updated)

                addNotification(
                    title = "Task Assigned",
                    message = "Task #$id was assigned to Field Tech $staffName.",
                    type = "Assignment"
                )
                toastMessage.value = "Assigned $staffName to Complaint #$id!"
            }
        }
    }

    // Field staff actions
    // 1. Biometric Scanner Simulation
    fun triggerBiometricScan(staffId: Int) {
        activeScanningStaffId.value = staffId
    }

    fun completeBiometricScan(staffId: Int) {
        viewModelScope.launch {
            val currentStaff = repository.staffMembers.first()
            val staff = currentStaff.find { it.id == staffId }
            if (staff != null) {
                val toggle = !staff.biometricSignedIn
                val updated = staff.copy(
                    biometricSignedIn = toggle,
                    attendanceTime = if (toggle) System.currentTimeMillis() else 0L
                )
                repository.updateStaffMember(updated)
                activeScanningStaffId.value = null

                val status = if (toggle) "Clocked IN via biometric verification" else "Clocked OUT safely"
                addNotification(
                    title = "Attendance Status Update",
                    message = "${staff.name} is now $status.",
                    type = "System"
                )
                toastMessage.value = "${staff.name}: $status"
            }
        }
    }

    // 2. Take Before Picture
    fun updateBeforePicture(complaintId: Int, encodedDrawing: String) {
        viewModelScope.launch {
            val complaint = repository.getComplaintById(complaintId)
            if (complaint != null) {
                val updated = complaint.copy(
                    status = "In Progress",
                    beforePicture = encodedDrawing
                )
                repository.updateComplaint(updated)

                addNotification(
                    title = "Job In Progress",
                    message = "Field staff loaded the before picture for Complaint #$complaintId. Project execution initiated.",
                    type = "Update"
                )
                toastMessage.value = "Before photo recorded. Working on job..."
            }
        }
    }

    // 3. Complete Task (Submit After Picture, remarks, digital signature)
    fun completeTask(
        complaintId: Int,
        afterPicture: String,
        remarks: String,
        signature: String
    ) {
        viewModelScope.launch {
            val complaint = repository.getComplaintById(complaintId)
            if (complaint != null) {
                val updated = complaint.copy(
                    status = "Done", // Moves to "Done", waiting Client Approval
                    afterPicture = afterPicture,
                    remarks = remarks,
                    digitalSignature = signature
                )
                repository.updateComplaint(updated)

                addNotification(
                    title = "Job Marked Done",
                    message = "Task #$complaintId completed by field staff. Pending Client signoff and invoice authorization.",
                    type = "Update"
                )
                toastMessage.value = "Job finished! Pending client approval."
            }
        }
    }

    // 4. Update task as pending with remarks
    fun updateTaskPending(complaintId: Int, remarks: String) {
        viewModelScope.launch {
            val complaint = repository.getComplaintById(complaintId)
            if (complaint != null) {
                val updated = complaint.copy(
                    status = "Assigned",
                    remarks = "Delayed: $remarks"
                )
                repository.updateComplaint(updated)

                addNotification(
                    title = "Job Postponed",
                    message = "Task #$complaintId updated with remarks: $remarks",
                    type = "Update"
                )
                toastMessage.value = "Task details noted & updated status."
            }
        }
    }

    // Client approves and generates Invoice
    fun approveComplaint(complaintId: Int, rating: Int) {
        viewModelScope.launch {
            val complaint = repository.getComplaintById(complaintId)
            if (complaint != null) {
                val updated = complaint.copy(
                    status = "Completed",
                    clientApproved = true,
                    feedbackRating = rating
                )
                repository.updateComplaint(updated)

                // Generate Invoice
                val minCost = if (complaint.category.contains("MEP")) 6000.0 else 3000.0
                val randomCostFactor = (Math.random() * 4000).toInt() + minCost
                val detailsDesc = when (complaint.category) {
                    "Janitorial" -> "Deep professional cleaning & disinfecting services (Janitorial Division). Staff cleaning agent consumables included. Cost: Rs. $randomCostFactor"
                    "MEP - Electrical" -> "Mechanical, Electrical, Plumbing: Diagnostic short circuit fix, wire coupling repair, terminal block test. Parts: Rs. 2000, Labor: Rs. ${randomCostFactor - 2000}"
                    "MEP - Plumbing" -> "Mechanical, Electrical, Plumbing: Sewage water bypass clearing, replacement washers, high pressure system flushing. Cost: Rs. $randomCostFactor"
                    else -> "MEP HVAC Maintenance, airflow balancing, filter clearance. Cost: Rs. $randomCostFactor"
                }

                val branchEmail = "${complaint.branchName.lowercase().replace(" ", "_").replace(",", "")}@bankdomain.pk"
                val invoiceObj = Invoice(
                    complaintId = complaint.id,
                    branchName = complaint.branchName,
                    amount = randomCostFactor,
                    details = detailsDesc,
                    sentToEmail = branchEmail
                )
                repository.insertInvoice(invoiceObj)

                addNotification(
                    title = "Job Completed & Approved",
                    message = "Client marked Job #$complaintId Completed with a $rating-star rating. Invoice generated automatically against client's portal accounts.",
                    type = "System"
                )
                toastMessage.value = "Approved successfully! Invoice sent to $branchEmail and saved."
            }
        }
    }

    // Live chat function
    fun sendChatMessage(complaintId: Int, message: String, role: String, senderName: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            val msg = ChatMessage(
                complaintId = complaintId,
                senderName = senderName,
                senderRole = role,
                messageText = message
            )
            repository.insertChatMessage(msg)
        }
    }

    // Consumables / Inventory actions
    fun addInventoryItem(name: String, category: String, frequency: String, quantity: String) {
        viewModelScope.launch {
            val item = InventoryItem(
                name = name,
                category = category,
                frequency = frequency,
                status = "Available",
                quantityDetail = quantity
            )
            repository.insertInventoryItem(item)
            toastMessage.value = "Added $name to system inventory ledger."
        }
    }

    fun updateInventoryStatus(item: InventoryItem, newStatus: String, newQty: String? = null) {
        viewModelScope.launch {
            val updated = item.copy(
                status = newStatus,
                quantityDetail = newQty ?: item.quantityDetail,
                lastCheckedDate = System.currentTimeMillis()
            )
            repository.updateInventoryItem(updated)
            toastMessage.value = "Updated ${item.name} to $newStatus."
        }
    }

    // Expose generated file strings or export CSV/PDF action
    fun exportPerformanceData(format: String) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val allComplaints = complaints.value
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

            val cacheDir = context.cacheDir
            val filename = "CleanTrack_PerformanceReport_${System.currentTimeMillis()}.$format"
            val file = File(cacheDir, filename)

            try {
                if (format == "csv") {
                    val sb = java.lang.StringBuilder()
                    sb.append("Complaint ID,Branch Name,City,Category,Urgency,Status,Date Filed,Staff Assigned,Completed,Rating\n")
                    allComplaints.forEach { cmp ->
                        sb.append("${cmp.id},\"${cmp.branchName}\",\"${cmp.city}\",\"${cmp.category}\",${cmp.urgency},${cmp.status},${formatter.format(Date(cmp.timeOfComplaint))},\"${cmp.assignedStaffName ?: "Unassigned"}\",${cmp.clientApproved},${cmp.feedbackRating}\n")
                    }
                    file.writeText(sb.toString())
                } else {
                    // Simulated rich PDF outline
                    val sb = java.lang.StringBuilder()
                    sb.append("========================================================\n")
                    sb.append("         CLEANTRACK MEP PAKISTAN PERFORMANCE REPORT     \n")
                    sb.append("========================================================\n")
                    sb.append("Report Type: Official Performance & Task Resolution Ledger\n")
                    sb.append("Generated At: ${formatter.format(Date())}\n")
                    sb.append("Staff Network Size: Over 4,000 active staff nationwide\n")
                    sb.append("--------------------------------------------------------\n\n")

                    val completedCount = allComplaints.filter { it.status == "Completed" }.size
                    val total = allComplaints.size
                    val completionRate = if (total > 0) (completedCount.toFloat() / total * 100).toInt() else 0

                    sb.append("KEY ANALYTICS SUMMARY:\n")
                    sb.append("- Total Logged Complaint Tasks: $total\n")
                    sb.append("- Completed Tasks: $completedCount\n")
                    sb.append("- Current Completion Rate: $completionRate%\n")
                    sb.append("- Outstanding Backlog / Pending: ${allComplaints.filter { it.status == "Pending" || it.status == "Assigned" || it.status == "In Progress" }.size}\n")
                    sb.append("--------------------------------------------------------\n\n")

                    sb.append("DETAILED BREAKDOWN BY BRANCH:\n")
                    allComplaints.forEach { cmp ->
                        sb.append("#${cmp.id} - [${cmp.urgency.uppercase()}] ${cmp.branchName} (${cmp.city})\n")
                        sb.append("   - Division: ${cmp.category} | Status: ${cmp.status}\n")
                        sb.append("   - Description: ${cmp.description}\n")
                        sb.append("   - Staff Assigned: ${cmp.assignedStaffName ?: "N/A"}\n")
                        sb.append("   - Completion Signoff: ${if (cmp.clientApproved) "Approved (Digital Signature Captured)" else "Pending approval"}\n")
                        sb.append("   - Feedback / Rating: ${if (cmp.feedbackRating > 0) "${cmp.feedbackRating}/5 stars" else "N/A"}\n\n")
                    }

                    sb.append("=================== END OF REPORT ======================\n")
                    file.writeText(sb.toString())
                }

                exportedFilePath.value = file.absolutePath
                toastMessage.value = "Report exported successfully as ${format.uppercase()} file: \n${file.name}"
                addNotification(
                    title = "Report Exported",
                    message = "Generated nationwide monthly business performance analytics ledger in ${format.uppercase()} format. Ready for review.",
                    type = "System"
                )
            } catch (e: Exception) {
                toastMessage.value = "Error exporting data: ${e.localizedMessage}"
            }
        }
    }

    fun clearToast() {
        toastMessage.value = null
    }

    fun clearExport() {
        exportedFilePath.value = null
    }
}
