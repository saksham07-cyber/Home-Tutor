package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tutors")
data class Tutor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val subjects: String, // Comma separated list of subjects e.g. "Physics, Math"
    val rating: Double,
    val ratingCount: Int,
    val photoUrl: String,
    val isVerified: Boolean = true,
    val costPerHour: Double,
    val preferredSectors: String, // Comma-separated sectors
    val phone: String,
    val isActive: Boolean = true,
    val isAvailable: Boolean = true // Tutor online/offline state
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parentName: String,
    val studentName: String,
    val subject: String,
    val className: String,
    val board: String,
    val durationHours: Double,
    val sectorAddress: String, // Chosen Noida Sector
    val detailAddress: String, // Street address details
    val bookingDate: String, // e.g. "2026-06-28"
    val bookingTime: String, // e.g. "16:00"
    val status: String, // "PENDING", "ASSIGNED", "COMPLETED"
    val tutorId: Long? = null,
    val tutorName: String? = null,
    val tutorPhone: String? = null,
    val costTotal: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val rating: Int? = null,
    val reviewText: String? = null
)
