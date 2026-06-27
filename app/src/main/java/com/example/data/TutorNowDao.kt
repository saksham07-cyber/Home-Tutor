package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TutorDao {
    @Query("SELECT * FROM tutors ORDER BY rating DESC")
    fun getAllTutors(): Flow<List<Tutor>>

    @Query("SELECT * FROM tutors WHERE id = :id")
    suspend fun getTutorById(id: Long): Tutor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTutor(tutor: Tutor)

    @Update
    suspend fun updateTutor(tutor: Tutor)

    @Delete
    suspend fun deleteTutor(tutor: Tutor)

    @Query("SELECT COUNT(*) FROM tutors")
    suspend fun getTutorCount(): Int
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun getBookingById(id: Long): Booking?

    @Query("SELECT * FROM bookings WHERE tutorId = :tutorId ORDER BY timestamp DESC")
    fun getBookingsByTutor(tutorId: Long): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Update
    suspend fun updateBooking(booking: Booking)

    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteBookingById(id: Long)
}
