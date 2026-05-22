package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplaintDao {
    @Query("SELECT * FROM complaints ORDER BY timeOfComplaint DESC")
    fun getAllComplaints(): Flow<List<Complaint>>

    @Query("SELECT * FROM complaints WHERE id = :id")
    suspend fun getComplaintById(id: Int): Complaint?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: Complaint): Long

    @Update
    suspend fun updateComplaint(complaint: Complaint)

    @Delete
    suspend fun deleteComplaint(complaint: Complaint)
}

@Dao
interface InventoryItemDao {
    @Query("SELECT * FROM inventory_items ORDER BY category, name")
    fun getAllInventoryItems(): Flow<List<InventoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem)

    @Update
    suspend fun updateInventoryItem(item: InventoryItem)

    @Delete
    suspend fun deleteInventoryItem(item: InventoryItem)
}

@Dao
interface StaffMemberDao {
    @Query("SELECT * FROM staff_members ORDER BY name")
    fun getAllStaffMembers(): Flow<List<StaffMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaffMember(staff: StaffMember)

    @Update
    suspend fun updateStaffMember(staff: StaffMember)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY generatedAt DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE complaintId = :complaintId ORDER BY timestamp ASC")
    fun getChatMessagesForComplaint(complaintId: Int): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)
}
