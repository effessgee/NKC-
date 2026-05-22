package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "complaints")
data class Complaint(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val branchName: String,
    val city: String,
    val description: String,
    val urgency: String, // "Low" (Green), "Medium" (Orange), "High" (Red)
    val category: String, // "Janitorial", "MEP - Electrical", "MEP - Mechanical", "MEP - Plumbing"
    val status: String, // "Pending", "Assigned", "In Progress", "Done" (Wait Client), "Completed"
    val clientGeoLocation: String, // "Latitude, Longitude" or "Karachi Main Branch"
    val timeOfComplaint: Long = System.currentTimeMillis(),
    val assignedStaffId: Int? = null,
    val assignedStaffName: String? = null,
    val beforePicture: String? = null, // Base64 or drawing string
    val afterPicture: String? = null, // Base64 or drawing string
    val digitalSignature: String? = null, // Drawing points or name representation
    val remarks: String? = null,
    val clientApproved: Boolean = false,
    val feedbackRating: Int = 0
)

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // "Consumables", "Equipments", "Tools", "Machinery"
    val frequency: String, // "Daily", "Weekly", "Monthly"
    val status: String, // "Available", "In Use", "Maintenance Required", "Out of Stock"
    val quantityDetail: String, // e.g., "15 units", "20 liters"
    val lastCheckedDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "staff_members")
data class StaffMember(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val designation: String, // "Janitorial Lead", "MEP Technician", "Senior Electrician"
    val phone: String,
    val branch: String, // Allied / nearest bank branch
    val biometricSignedIn: Boolean = false,
    val attendanceTime: Long = 0L,
    val currentLat: Double = 24.8607,
    val currentLng: Double = 67.0011
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val complaintId: Int,
    val branchName: String,
    val amount: Double,
    val details: String, // Description of work and cost breakdown
    val generatedAt: Long = System.currentTimeMillis(),
    val sentToEmail: String
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val complaintId: Int,
    val senderName: String,
    val senderRole: String, // "Client" or "Staff"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)
