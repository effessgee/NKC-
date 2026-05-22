package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase) {
    val complaints: Flow<List<Complaint>> = db.complaintDao().getAllComplaints()
    val inventoryItems: Flow<List<InventoryItem>> = db.inventoryItemDao().getAllInventoryItems()
    val staffMembers: Flow<List<StaffMember>> = db.staffMemberDao().getAllStaffMembers()
    val invoices: Flow<List<Invoice>> = db.invoiceDao().getAllInvoices()

    suspend fun getComplaintById(id: Int): Complaint? = db.complaintDao().getComplaintById(id)

    suspend fun insertComplaint(complaint: Complaint): Long = db.complaintDao().insertComplaint(complaint)

    suspend fun updateComplaint(complaint: Complaint) = db.complaintDao().updateComplaint(complaint)

    suspend fun deleteComplaint(complaint: Complaint) = db.complaintDao().deleteComplaint(complaint)

    suspend fun insertInventoryItem(item: InventoryItem) = db.inventoryItemDao().insertInventoryItem(item)

    suspend fun updateInventoryItem(item: InventoryItem) = db.inventoryItemDao().updateInventoryItem(item)

    suspend fun deleteInventoryItem(item: InventoryItem) = db.inventoryItemDao().deleteInventoryItem(item)

    suspend fun insertStaffMember(staff: StaffMember) = db.staffMemberDao().insertStaffMember(staff)

    suspend fun updateStaffMember(staff: StaffMember) = db.staffMemberDao().updateStaffMember(staff)

    suspend fun insertInvoice(invoice: Invoice) = db.invoiceDao().insertInvoice(invoice)

    fun getChatMessages(complaintId: Int): Flow<List<ChatMessage>> = db.chatMessageDao().getChatMessagesForComplaint(complaintId)

    suspend fun insertChatMessage(message: ChatMessage) = db.chatMessageDao().insertChatMessage(message)
}
