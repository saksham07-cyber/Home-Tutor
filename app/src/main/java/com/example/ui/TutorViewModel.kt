package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TutorViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = TutorRepository(database.tutorDao(), database.bookingDao())

    // UI Role: "STUDENT", "TUTOR", "ADMIN"
    private val _currentRole = MutableStateFlow("STUDENT")
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    // Session and Login status
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _loggedInUser = MutableStateFlow<String?>(null)
    val loggedInUser: StateFlow<String?> = _loggedInUser.asStateFlow()

    fun loginUser(role: String, userIdentifier: String) {
        _currentRole.value = role
        _loggedInUser.value = userIdentifier
        _isLoggedIn.value = true
        showNotification("Welcome back! Logged in as ${role.lowercase().replaceFirstChar { it.uppercase() }}.")
    }

    fun logoutUser() {
        _isLoggedIn.value = false
        _loggedInUser.value = null
        showNotification("Logged out successfully.")
    }

    // Active Tutor ID (if the role is "TUTOR", default to 1 for Mr. Arvind Sharma, can switch)
    private val _activeTutorId = MutableStateFlow<Long>(1)
    val activeTutorId: StateFlow<Long> = _activeTutorId.asStateFlow()

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSectorFilter = MutableStateFlow("")
    val selectedSectorFilter: StateFlow<String> = _selectedSectorFilter.asStateFlow()

    // Database Flows
    val tutors: StateFlow<List<Tutor>> = repository.allTutors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookings: StateFlow<List<Booking>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Tutors based on search and sector
    val filteredTutors: StateFlow<List<Tutor>> = combine(tutors, searchQuery, selectedSectorFilter) { list, query, sec ->
        list.filter { tutor ->
            val matchQuery = query.isEmpty() || tutor.name.contains(query, ignoreCase = true) || tutor.subjects.contains(query, ignoreCase = true)
            val matchSector = sec.isEmpty() || tutor.preferredSectors.contains(sec, ignoreCase = true)
            matchQuery && matchSector && tutor.isActive
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Available sectors in Noida for initial launch
    val sectorsList = listOf(
        "Sector 18", "Sector 62", "Sector 75", "Sector 76",
        "Sector 77", "Sector 78", "Sector 104", "Sector 107",
        "Sector 137", "Sector 143", "Sector 150",
        "Greater Noida West", "Knowledge Park", "Pari Chowk"
    )

    val subjectsList = listOf(
        "Mathematics", "Physics", "Chemistry", "Biology",
        "Science", "Computer Science", "English Literature", "Social Studies"
    )

    val classesList = (1..12).map { "Class $it" }
    val boardsList = listOf("CBSE", "ICSE", "IB", "State Board")

    // Booking Form State
    var formSubject = MutableStateFlow("Mathematics")
    var formClass = MutableStateFlow("Class 10")
    var formBoard = MutableStateFlow("CBSE")
    var formDuration = MutableStateFlow(1.5)
    var formSector = MutableStateFlow("Sector 75")
    var formDetailAddress = MutableStateFlow("")
    var formDate = MutableStateFlow("")
    var formTime = MutableStateFlow("")
    var formParentName = MutableStateFlow("Saksham Gupta")
    var formStudentName = MutableStateFlow("Rahul Gupta")

    // Notification message (toast or snackbar simulation)
    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedTutorsIfEmpty()
            // Set default date and time
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val today = Calendar.getInstance()
            formDate.value = dateFormat.format(today.time)
            today.add(Calendar.HOUR_OF_DAY, 2)
            formTime.value = timeFormat.format(today.time)
        }
    }

    fun showNotification(message: String) {
        _notificationMessage.value = message
        viewModelScope.launch {
            kotlinx.coroutines.delay(4000)
            if (_notificationMessage.value == message) {
                _notificationMessage.value = null
            }
        }
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }

    fun setRole(role: String) {
        _currentRole.value = role
        showNotification("Switched to $role Workspace")
    }

    fun setActiveTutor(id: Long) {
        _activeTutorId.value = id
        viewModelScope.launch {
            val t = repository.getTutorById(id)
            if (t != null) {
                showNotification("Active Tutor set to ${t.name}")
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSectorFilter(sector: String) {
        _selectedSectorFilter.value = sector
    }

    fun bookTutor(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val baseRate = 500.0 // Default baseline
            val totalCost = baseRate * formDuration.value

            val newBooking = Booking(
                parentName = formParentName.value,
                studentName = formStudentName.value,
                subject = formSubject.value,
                className = formClass.value,
                board = formBoard.value,
                durationHours = formDuration.value,
                sectorAddress = formSector.value,
                detailAddress = formDetailAddress.value.ifEmpty { "Block C, Flat 402, ${formSector.value}, Noida" },
                bookingDate = formDate.value,
                bookingTime = formTime.value,
                status = "PENDING",
                costTotal = totalCost
            )

            repository.insertBooking(newBooking)
            showNotification("Booking Requested! Admin is assigning the best tutor nearby.")
            onSuccess()
        }
    }

    fun assignTutor(bookingId: Long, tutorId: Long) {
        viewModelScope.launch {
            val booking = database.bookingDao().getBookingById(bookingId)
            val tutor = repository.getTutorById(tutorId)

            if (booking != null && tutor != null) {
                val updatedBooking = booking.copy(
                    status = "ASSIGNED",
                    tutorId = tutor.id,
                    tutorName = tutor.name,
                    tutorPhone = tutor.phone,
                    costTotal = tutor.costPerHour * booking.durationHours
                )
                repository.updateBooking(updatedBooking)
                showNotification("Tutor '${tutor.name}' assigned successfully to Booking #${bookingId}!")
            }
        }
    }

    fun acceptBooking(bookingId: Long) {
        viewModelScope.launch {
            val booking = database.bookingDao().getBookingById(bookingId)
            if (booking != null) {
                val updatedBooking = booking.copy(status = "ACTIVE")
                repository.updateBooking(updatedBooking)
                showNotification("Class request accepted! Head to the address.")
            }
        }
    }

    fun completeBooking(bookingId: Long, rating: Int, review: String) {
        viewModelScope.launch {
            val booking = database.bookingDao().getBookingById(bookingId)
            if (booking != null) {
                val updatedBooking = booking.copy(
                    status = "COMPLETED",
                    rating = rating,
                    reviewText = review
                )
                repository.updateBooking(updatedBooking)

                // Update Tutor ratings metrics
                booking.tutorId?.let { tId ->
                    repository.getTutorById(tId)?.let { tutor ->
                        val currentCount = tutor.ratingCount
                        val currentRatingSum = tutor.rating * currentCount
                        val newCount = currentCount + 1
                        val newRating = ((currentRatingSum + rating) / newCount)
                        val formattedRating = String.format(Locale.US, "%.1f", newRating).toDouble()

                        repository.updateTutor(
                            tutor.copy(
                                rating = formattedRating,
                                ratingCount = newCount
                            )
                        )
                    }
                }

                showNotification("Class completed! Review submitted. Rating: $rating/5")
            }
        }
    }

    fun toggleTutorAvailability(tutorId: Long) {
        viewModelScope.launch {
            repository.getTutorById(tutorId)?.let { tutor ->
                val updated = tutor.copy(isAvailable = !tutor.isAvailable)
                repository.updateTutor(updated)
                showNotification("${tutor.name} is now ${if (updated.isAvailable) "ONLINE" else "OFFLINE"}")
            }
        }
    }

    fun addTutorDirectly(name: String, subjects: String, phone: String, rate: Double, sectors: String) {
        viewModelScope.launch {
            val newTutor = Tutor(
                name = name,
                subjects = subjects,
                rating = 5.0,
                ratingCount = 1,
                photoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2",
                costPerHour = rate,
                preferredSectors = sectors,
                phone = phone,
                isActive = true,
                isAvailable = true
            )
            repository.insertTutor(newTutor)
            showNotification("New verified Tutor '$name' added successfully!")
        }
    }

    fun toggleTutorActiveStatus(tId: Long) {
        viewModelScope.launch {
            repository.getTutorById(tId)?.let { tutor ->
                val updated = tutor.copy(isActive = !tutor.isActive)
                repository.updateTutor(updated)
                showNotification("Tutor status updated for ${tutor.name}")
            }
        }
    }

    fun deleteTutor(tId: Long) {
        viewModelScope.launch {
            repository.getTutorById(tId)?.let { tutor ->
                repository.deleteTutor(tutor)
                showNotification("Tutor '${tutor.name}' deleted from platform.")
            }
        }
    }
}
