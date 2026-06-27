package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TutorRepository(
    private val tutorDao: TutorDao,
    private val bookingDao: BookingDao
) {
    val allTutors: Flow<List<Tutor>> = tutorDao.getAllTutors()
    val allBookings: Flow<List<Booking>> = bookingDao.getAllBookings()

    suspend fun getTutorById(id: Long): Tutor? = tutorDao.getTutorById(id)

    suspend fun insertTutor(tutor: Tutor) {
        tutorDao.insertTutor(tutor)
    }

    suspend fun updateTutor(tutor: Tutor) {
        tutorDao.updateTutor(tutor)
    }

    suspend fun deleteTutor(tutor: Tutor) {
        tutorDao.deleteTutor(tutor)
    }

    suspend fun insertBooking(booking: Booking): Long {
        return bookingDao.insertBooking(booking)
    }

    suspend fun updateBooking(booking: Booking) {
        bookingDao.updateBooking(booking)
    }

    suspend fun deleteBookingById(id: Long) {
        bookingDao.deleteBookingById(id)
    }

    fun getBookingsByTutor(tutorId: Long): Flow<List<Booking>> {
        return bookingDao.getBookingsByTutor(tutorId)
    }

    suspend fun seedTutorsIfEmpty() {
        val count = tutorDao.getTutorCount()
        if (count == 0) {
            val defaultTutors = listOf(
                Tutor(
                    name = "Mr. Arvind Sharma",
                    subjects = "Physics, Mathematics, Chemistry",
                    rating = 4.9,
                    ratingCount = 84,
                    photoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e",
                    costPerHour = 650.0,
                    preferredSectors = "Sector 75, Sector 76, Sector 77, Sector 78",
                    phone = "+91 98765 43210",
                    isActive = true,
                    isAvailable = true
                ),
                Tutor(
                    name = "Mrs. Neha Gupta",
                    subjects = "Biology, Science, Chemistry",
                    rating = 4.8,
                    ratingCount = 62,
                    photoUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330",
                    costPerHour = 550.0,
                    preferredSectors = "Sector 62, Sector 18, Sector 104",
                    phone = "+91 87654 32109",
                    isActive = true,
                    isAvailable = true
                ),
                Tutor(
                    name = "Dr. Vikram Singh",
                    subjects = "Computer Science, Python, Mathematics",
                    rating = 5.0,
                    ratingCount = 41,
                    photoUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e",
                    costPerHour = 800.0,
                    preferredSectors = "Greater Noida West, Knowledge Park, Pari Chowk",
                    phone = "+91 76543 21098",
                    isActive = true,
                    isAvailable = true
                ),
                Tutor(
                    name = "Ms. Priya Sen",
                    subjects = "English Literature, Social Studies, History",
                    rating = 4.7,
                    ratingCount = 53,
                    photoUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80",
                    costPerHour = 500.0,
                    preferredSectors = "Sector 18, Sector 104, Sector 107",
                    phone = "+91 65432 10987",
                    isActive = true,
                    isAvailable = true
                ),
                Tutor(
                    name = "Mr. Rohan Mehta",
                    subjects = "Mathematics, Physics, English",
                    rating = 4.6,
                    ratingCount = 29,
                    photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d",
                    costPerHour = 600.0,
                    preferredSectors = "Sector 137, Sector 143, Sector 150",
                    phone = "+91 54321 09876",
                    isActive = true,
                    isAvailable = true
                )
            )
            for (tutor in defaultTutors) {
                tutorDao.insertTutor(tutor)
            }
        }
    }
}
