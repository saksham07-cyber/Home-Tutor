package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Booking
import com.example.data.Tutor
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorAppView(viewModel: TutorViewModel) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
    val activeTutorId by viewModel.activeTutorId.collectAsStateWithLifecycle()
    val tutorsList by viewModel.filteredTutors.collectAsStateWithLifecycle()
    val rawTutors by viewModel.tutors.collectAsStateWithLifecycle()
    val bookingsList by viewModel.bookings.collectAsStateWithLifecycle()
    val notificationMessage by viewModel.notificationMessage.collectAsStateWithLifecycle()

    var showProfileMenu by remember { mutableStateOf(false) }

    if (!isLoggedIn) {
        TutorAuthView(
            viewModel = viewModel,
            onLoginSuccess = { role, identifier ->
                viewModel.loginUser(role, identifier)
            }
        )
        return
    }

    var showInstantBookingDialog by remember { mutableStateOf(false) }
    var showScheduleBookingDialog by remember { mutableStateOf(false) }
    var showTutorsListDialog by remember { mutableStateOf(false) }
    var showAddTutorDialog by remember { mutableStateOf(false) }
    var showAssignTutorDialogForBooking by remember { mutableStateOf<Booking?>(null) }
    var showReviewDialogForBooking by remember { mutableStateOf<Booking?>(null) }

    // Navigation and sub-state persistence
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF7F9FC))
                    .statusBarsPadding()
            ) {
                // Application Top Accent
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TUTOR_NOW CORE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF),
                            letterSpacing = 1.sp
                        )
                    }

                    // Simulated Profile Avatar with Notification Badge & Dropdown Session Menu
                    Box(modifier = Modifier.wrapContentSize()) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFDBEAFE))
                                .clickable { showProfileMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981)) // Active session green online status
                                .border(1.5.dp, Color(0xFFF7F9FC), CircleShape)
                                .align(Alignment.TopEnd)
                        )

                        DropdownMenu(
                            expanded = showProfileMenu,
                            onDismissRequest = { showProfileMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(
                                            text = "ACTIVE SESSION",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B),
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = loggedInUser ?: "Demo Account",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Role: ${currentRole.lowercase().replaceFirstChar { it.uppercase() }}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF2563EB),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                onClick = {},
                                enabled = false
                            )
                            Divider(color = Color(0xFFE2E8F0))
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ExitToApp,
                                            contentDescription = "Log Out",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Log Out of Session",
                                            color = Color(0xFFEF4444),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                },
                                onClick = {
                                    showProfileMenu = false
                                    viewModel.logoutUser()
                                }
                            )
                        }
                    }
                }

                // Interactive Workspace Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val roles = listOf("STUDENT" to "Parent", "TUTOR" to "Tutor", "ADMIN" to "Admin HQ")
                    roles.forEach { (roleKey, label) ->
                        val isSelected = currentRole == roleKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color.White else Color.Transparent)
                                .clickable { viewModel.setRole(roleKey) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF1E40AF) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // M3 Premium Navigation Simulation bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val items = listOf(
                        Triple("Home", Icons.Default.Home, "STUDENT"),
                        Triple("My Classes", Icons.Default.DateRange, "TUTOR"),
                        Triple("Admin HQ", Icons.Default.Settings, "ADMIN")
                    )
                    items.forEach { (title, icon, role) ->
                        val isSelected = currentRole == role
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.setRole(role) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) Color(0xFFDBEAFE) else Color.Transparent)
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (isSelected) Color(0xFF1E40AF) else Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF1E40AF) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF7F9FC)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (currentRole) {
                    "STUDENT" -> StudentDashboard(
                        viewModel = viewModel,
                        tutorsList = tutorsList,
                        bookingsList = bookingsList,
                        onInstantBookClick = { showInstantBookingDialog = true },
                        onScheduleBookClick = { showScheduleBookingDialog = true },
                        onTopRatedClick = { showTutorsListDialog = true },
                        onReviewClick = { booking -> showReviewDialogForBooking = booking }
                    )
                    "TUTOR" -> TutorDashboard(
                        viewModel = viewModel,
                        activeTutorId = activeTutorId,
                        rawTutors = rawTutors,
                        bookingsList = bookingsList
                    )
                    "ADMIN" -> AdminDashboard(
                        viewModel = viewModel,
                        rawTutors = rawTutors,
                        bookingsList = bookingsList,
                        onAddTutorClick = { showAddTutorDialog = true },
                        onAssignTutorClick = { booking -> showAssignTutorDialogForBooking = booking }
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Notification Overlay (Simulation)
            notificationMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Alert",
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = msg,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearNotification() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog implementations for the premium user flows
    if (showInstantBookingDialog) {
        InstantBookingDialog(
            viewModel = viewModel,
            onDismiss = { showInstantBookingDialog = false },
            onConfirm = {
                viewModel.bookTutor {
                    showInstantBookingDialog = false
                }
            }
        )
    }

    if (showScheduleBookingDialog) {
        ScheduleBookingDialog(
            viewModel = viewModel,
            onDismiss = { showScheduleBookingDialog = false },
            onConfirm = {
                viewModel.bookTutor {
                    showScheduleBookingDialog = false
                }
            }
        )
    }

    if (showTutorsListDialog) {
        TutorsListDialog(
            tutors = rawTutors,
            onDismiss = { showTutorsListDialog = false }
        )
    }

    if (showAddTutorDialog) {
        AddTutorDialog(
            viewModel = viewModel,
            onDismiss = { showAddTutorDialog = false }
        )
    }

    showAssignTutorDialogForBooking?.let { booking ->
        AssignTutorDialog(
            booking = booking,
            tutors = rawTutors,
            onDismiss = { showAssignTutorDialogForBooking = null },
            onAssign = { tutorId ->
                viewModel.assignTutor(booking.id, tutorId)
                showAssignTutorDialogForBooking = null
            }
        )
    }

    showReviewDialogForBooking?.let { booking ->
        SubmitReviewDialog(
            booking = booking,
            onDismiss = { showReviewDialogForBooking = null },
            onSubmit = { rating, text ->
                viewModel.completeBooking(booking.id, rating, text)
                showReviewDialogForBooking = null
            }
        )
    }
}

// ------------------------------------------------------------------------
// STUDENT DASHBOARD (Parent / Student Workspace)
// ------------------------------------------------------------------------
@Composable
fun StudentDashboard(
    viewModel: TutorViewModel,
    tutorsList: List<Tutor>,
    bookingsList: List<Booking>,
    onInstantBookClick: () -> Unit,
    onScheduleBookClick: () -> Unit,
    onTopRatedClick: () -> Unit,
    onReviewClick: (Booking) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedSector by viewModel.selectedSectorFilter.collectAsStateWithLifecycle()

    // 1. Current Active Location Banner
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CURRENT LAUNCH CITY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (selectedSector.isEmpty()) "Sector 75, Noida" else "$selectedSector, Noida",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
            }

            // Quick Noida sector selector chip
            var showSectorsDropdown by remember { mutableStateOf(false) }
            Box {
                Button(
                    onClick = { showSectorsDropdown = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Change Sector", color = Color(0xFF2563EB), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                }
                DropdownMenu(
                    expanded = showSectorsDropdown,
                    onDismissRequest = { showSectorsDropdown = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("All Sectors") },
                        onClick = {
                            viewModel.setSectorFilter("")
                            showSectorsDropdown = false
                        }
                    )
                    viewModel.sectorsList.forEach { sector ->
                        DropdownMenuItem(
                            text = { Text(sector) },
                            onClick = {
                                viewModel.setSectorFilter(sector)
                                showSectorsDropdown = false
                            }
                        )
                    }
                }
            }
        }
    }

    // 2. M3 Subject/Tutor Search Box
    OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.setSearchQuery(it) },
        placeholder = { Text("Search subjects, e.g. Physics, Mathematics...", color = Color(0xFF94A3B8), fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF3B82F6)) },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("search_subjects_input")
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(30.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF0F4F8),
            unfocusedContainerColor = Color(0xFFF8FAFC),
            focusedBorderColor = Color(0xFFBFDBFE),
            unfocusedBorderColor = Color(0xFFE2E8F0)
        ),
        singleLine = true
    )

    // 3. MAIN BENTO GRID AREA
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bento Part 1: Instant Booking Hero Card (Wide, Blue Gradient)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                    )
                )
                .clickable { onInstantBookClick() }
                .padding(24.dp)
        ) {
            // Decorative shapes for Bento glassmorphism
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 40.dp, y = 40.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
            )

            Column(modifier = Modifier.align(Alignment.TopStart)) {
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "QUICK CONNECT",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Instant Booking",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A verified tutor reaches your home in 45 mins",
                    color = Color(0xFFBFDBFE),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // CTA Floating button inside bento grid item
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Book",
                    tint = Color(0xFF1D4ED8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Bento Part 2: Two-Column Row (Schedule vs Top Rated)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Schedule Card (Orange Theme Accent)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp)
                    .clickable { onScheduleBookClick() },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF7ED)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Schedule",
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Schedule",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Plan for future dates",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Top Rated / Masters Card (Indigo Theme Accent)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp)
                    .clickable { onTopRatedClick() },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Overlapping mini avatar profiles
                    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                        listOf(Color(0xFF93C5FD), Color(0xFFFCA5A5), Color(0xFF86EFAC)).forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(1.5.dp, Color(0xFF1E1B4B), CircleShape)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Top Tutors",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Verified Masters",
                            fontSize = 11.sp,
                            color = Color(0xFF818CF8),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Bento Part 3: Recent Bookings Status Console
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Booking Console",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(12.dp))

                val userBookings = bookingsList.filter { it.parentName == viewModel.formParentName.value }
                if (userBookings.isEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "None", tint = Color(0xFF94A3B8))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("No current sessions", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                            Text("Book your first home tutor class above!", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        userBookings.take(3).forEach { booking ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(16.dp))
                                    .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when (booking.status) {
                                                "PENDING" -> Color(0xFFFEF3C7)
                                                "ASSIGNED" -> Color(0xFFDBEAFE)
                                                "ACTIVE" -> Color(0xFFDCEFDF)
                                                else -> Color(0xFFF1F5F9)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = booking.subject.take(2).uppercase(),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = when (booking.status) {
                                            "PENDING" -> Color(0xFFD97706)
                                            "ASSIGNED" -> Color(0xFF2563EB)
                                            "ACTIVE" -> Color(0xFF15803D)
                                            else -> Color(0xFF475569)
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${booking.subject} • ${booking.className}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = if (booking.status == "PENDING") {
                                            "Finding premium tutor nearby..."
                                        } else {
                                            "Tutor: ${booking.tutorName ?: "Assigned"}"
                                        },
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                when (booking.status) {
                                                    "PENDING" -> Color(0xFFF59E0B)
                                                    "ASSIGNED" -> Color(0xFF3B82F6)
                                                    "ACTIVE" -> Color(0xFF10B981)
                                                    else -> Color(0xFF94A3B8)
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = booking.status,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }

                                    if (booking.status == "COMPLETED" && booking.rating == null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Review Now",
                                            fontSize = 10.sp,
                                            color = Color(0xFF2563EB),
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = TextDecoration.Underline,
                                            modifier = Modifier
                                                .clickable { onReviewClick(booking) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// TUTOR DASHBOARD (Tutor Workspace)
// ------------------------------------------------------------------------
@Composable
fun TutorDashboard(
    viewModel: TutorViewModel,
    activeTutorId: Long,
    rawTutors: List<Tutor>,
    bookingsList: List<Booking>
) {
    val activeTutor = rawTutors.find { it.id == activeTutorId } ?: rawTutors.firstOrNull()

    if (activeTutor == null) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Interactive switch to choose which Tutor profile you are demoing
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Switch Demo Tutor Profile:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rawTutors.forEach { t ->
                    val isActiveProfile = t.id == activeTutorId
                    FilterChip(
                        selected = isActiveProfile,
                        onClick = { viewModel.setActiveTutor(t.id) },
                        label = { Text(t.name, fontSize = 11.sp) }
                    )
                }
            }
        }
    }

    // Profile Summary & Availability Status
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFEFF6FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AccountBox, contentDescription = "Tutor Icon", tint = Color(0xFF2563EB), modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(activeTutor.name, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
            Text(activeTutor.subjects, fontSize = 12.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = "Star", tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("${activeTutor.rating} (${activeTutor.ratingCount} reviews)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
            }
        }

        // Online Status Switch
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Switch(
                checked = activeTutor.isAvailable,
                onCheckedChange = { viewModel.toggleTutorAvailability(activeTutor.id) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF10B981)
                )
            )
            Text(
                text = if (activeTutor.isAvailable) "ONLINE" else "OFFLINE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (activeTutor.isAvailable) Color(0xFF10B981) else Color(0xFF94A3B8)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Bento Part 1: Wallet & Earnings Card (Dark Slate Metallic Gradient)
    val completedBookings = bookingsList.filter { it.tutorId == activeTutor.id && it.status == "COMPLETED" }
    val totalEarnings = completedBookings.sumOf { it.costTotal }
    val platformCut = totalEarnings * 0.20
    val netEarnings = totalEarnings - platformCut

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Glass design background
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-20).dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TUTOR WALLET", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("20% Fee Applied", color = Color(0xFF60A5FA), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "₹${String.format(Locale.US, "%,.2f", netEarnings)}",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text("Net Withdrawable Balance", color = Color(0xFF64748B), fontSize = 11.sp)
                    }

                    Button(
                        onClick = { viewModel.showNotification("Payout processed to your linked Bank Account instantly!") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        enabled = netEarnings > 0
                    ) {
                        Text("Payout", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Tutor Tasks / Active Assignment List
    Text(
        text = "My Assignments Queue",
        fontSize = 15.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF0F172A),
        modifier = Modifier.padding(bottom = 12.dp)
    )

    val myBookings = bookingsList.filter { it.tutorId == activeTutor.id }
    if (myBookings.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Info, contentDescription = "Empty", tint = Color(0xFF94A3B8), modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No sessions assigned yet", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                Text("Keep online switch enabled to get near bookings.", fontSize = 11.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            myBookings.forEach { booking ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(booking.subject, color = Color(0xFF2563EB), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (booking.status) {
                                            "ASSIGNED" -> Color(0xFFFEF3C7)
                                            "ACTIVE" -> Color(0xFFD1FAE5)
                                            "COMPLETED" -> Color(0xFFF1F5F9)
                                            else -> Color.Gray
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = booking.status,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (booking.status) {
                                        "ASSIGNED" -> Color(0xFFD97706)
                                        "ACTIVE" -> Color(0xFF065F46)
                                        else -> Color(0xFF475569)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Student: ${booking.studentName} (${booking.className} - ${booking.board})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Pin", tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(booking.detailAddress, fontSize = 12.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(Icons.Default.DateRange, contentDescription = "Time", tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Schedule: ${booking.bookingDate} @ ${booking.bookingTime} (${booking.durationHours} hrs)", fontSize = 12.sp, color = Color(0xFF64748B))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions for states
                        when (booking.status) {
                            "ASSIGNED" -> {
                                Button(
                                    onClick = { viewModel.acceptBooking(booking.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Accept & Confirm Session", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            "ACTIVE" -> {
                                var showFeedbackField by remember { mutableStateOf(false) }
                                var ratingVal by remember { mutableStateOf(5) }
                                var commentText by remember { mutableStateOf("") }

                                if (!showFeedbackField) {
                                    Button(
                                        onClick = { showFeedbackField = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Complete Home Class Session", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text("Final Class Rating & Review from Tutor:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                            (1..5).forEach { star ->
                                                IconButton(onClick = { ratingVal = star }, modifier = Modifier.size(28.dp)) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = "$star Stars",
                                                        tint = if (star <= ratingVal) Color(0xFFF59E0B) else Color(0xFFCBD5E1)
                                                    )
                                                }
                                            }
                                        }
                                        OutlinedTextField(
                                            value = commentText,
                                            onValueChange = { commentText = it },
                                            placeholder = { Text("How was the student's progress? e.g. Rahul understood thermodynamics formulas perfectly!") },
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = TextStyle(fontSize = 12.sp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                            TextButton(onClick = { showFeedbackField = false }) {
                                                Text("Cancel")
                                            }
                                            Button(
                                                onClick = { viewModel.completeBooking(booking.id, ratingVal, commentText) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                            ) {
                                                Text("Submit & Finish Class")
                                            }
                                        }
                                    }
                                }
                            }
                            "COMPLETED" -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "Session Finished • Earnings Credited",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF475569),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// TextStyle backup helper
private fun TextStyle(fontSize: androidx.compose.ui.unit.TextUnit) = androidx.compose.ui.text.TextStyle(fontSize = fontSize)

// ------------------------------------------------------------------------
// ADMIN DASHBOARD (Admin HQ Workspace)
// ------------------------------------------------------------------------
@Composable
fun AdminDashboard(
    viewModel: TutorViewModel,
    rawTutors: List<Tutor>,
    bookingsList: List<Booking>,
    onAddTutorClick: () -> Unit,
    onAssignTutorClick: (Booking) -> Unit
) {
    // Analytics Metrics Row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val totalRevenue = bookingsList.filter { it.status == "COMPLETED" }.sumOf { it.costTotal }
        val adminCommission = totalRevenue * 0.20

        listOf(
            Triple("Revenue", "₹${String.format(Locale.US, "%,.0f", adminCommission)}", Color(0xFF3B82F6)),
            Triple("Bookings", "${bookingsList.size}", Color(0xFF10B981)),
            Triple("Active Tutors", "${rawTutors.filter { it.isActive }.size}", Color(0xFF8B5CF6))
        ).forEach { (label, value, tint) ->
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(label, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = tint)
                }
            }
        }
    }

    // Interactive Button: Onboard New Tutor Directly
    Button(
        onClick = onAddTutorClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .testTag("add_tutor_button"),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Manual Onboard Verified Tutor", color = Color.White, fontWeight = FontWeight.Bold)
    }

    // List of Pending Bookings
    Text(
        text = "Pending Bookings Dispatch Desk",
        fontSize = 15.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF0F172A),
        modifier = Modifier.padding(bottom = 12.dp)
    )

    val pendingBookings = bookingsList.filter { it.status == "PENDING" }
    if (pendingBookings.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                Text("All systems clear! No pending booking dispatch requests.", color = Color(0xFF64748B), fontSize = 13.sp)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 20.dp)) {
            pendingBookings.forEach { booking ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFFFEF08A))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("${booking.subject} • ${booking.className}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Student: ${booking.studentName} (Parent: ${booking.parentName})", fontSize = 12.sp, color = Color(0xFF475569))
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFEF9C3), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("PENDING DISPATCH", color = Color(0xFF854D0E), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Icon(Icons.Default.LocationOn, contentDescription = "Pin", tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${booking.sectorAddress} • ${booking.detailAddress}", fontSize = 11.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onAssignTutorClick(booking) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Assign Match Tutor", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Tutors Registry Panel
    Text(
        text = "Verified Tutors Registry",
        fontSize = 15.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF0F172A),
        modifier = Modifier.padding(bottom = 12.dp)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rawTutors.forEach { tutor ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tutor.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(tutor.subjects, fontSize = 11.sp, color = Color(0xFF64748B))
                        Text("Rate: ₹${tutor.costPerHour}/hr • Phone: ${tutor.phone}", fontSize = 11.sp, color = Color(0xFF475569))
                        Text("Preferred: ${tutor.preferredSectors}", fontSize = 10.sp, color = Color(0xFF94A3B8), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Switch(
                            checked = tutor.isActive,
                            onCheckedChange = { viewModel.toggleTutorActiveStatus(tutor.id) },
                            modifier = Modifier.scale(0.8f)
                        )
                        Text(
                            text = if (tutor.isActive) "ACTIVE" else "DISABLED",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (tutor.isActive) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { viewModel.deleteTutor(tutor.id) }
                        )
                    }
                }
            }
        }
    }
}

// Scale modifier helper for Switch scaling in Admin Dashboard
fun Modifier.scale(scale: Float): Modifier = this

// ------------------------------------------------------------------------
// MODAL DIALOGS
// ------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstantBookingDialog(
    viewModel: TutorViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val subject by viewModel.formSubject.collectAsStateWithLifecycle()
    val className by viewModel.formClass.collectAsStateWithLifecycle()
    val board by viewModel.formBoard.collectAsStateWithLifecycle()
    val duration by viewModel.formDuration.collectAsStateWithLifecycle()
    val sector by viewModel.formSector.collectAsStateWithLifecycle()
    val detailAddress by viewModel.formDetailAddress.collectAsStateWithLifecycle()

    var showSubjectDropdown by remember { mutableStateOf(false) }
    var showClassDropdown by remember { mutableStateOf(false) }
    var showBoardDropdown by remember { mutableStateOf(false) }
    var showSectorDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Instant 45m Dispatch",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E3A8A)
                )

                Text(
                    text = "Fill the student criteria and our manual dispatcher will select the ideal matches in Sector 75 instantly.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Divider(color = Color(0xFFF1F5F9))

                // Subject Selector
                Column {
                    Text("Select Subject", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showSubjectDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(subject)
                        }
                        DropdownMenu(
                            expanded = showSubjectDropdown,
                            onDismissRequest = { showSubjectDropdown = false }
                        ) {
                            viewModel.subjectsList.forEach { s ->
                                DropdownMenuItem(text = { Text(s) }, onClick = {
                                    viewModel.formSubject.value = s
                                    showSubjectDropdown = false
                                })
                            }
                        }
                    }
                }

                // Class and Board (Row)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Class", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showClassDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(className)
                            }
                            DropdownMenu(
                                expanded = showClassDropdown,
                                onDismissRequest = { showClassDropdown = false }
                            ) {
                                viewModel.classesList.forEach { c ->
                                    DropdownMenuItem(text = { Text(c) }, onClick = {
                                        viewModel.formClass.value = c
                                        showClassDropdown = false
                                    })
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Board", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showBoardDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(board)
                            }
                            DropdownMenu(
                                expanded = showBoardDropdown,
                                onDismissRequest = { showBoardDropdown = false }
                            ) {
                                viewModel.boardsList.forEach { b ->
                                    DropdownMenuItem(text = { Text(b) }, onClick = {
                                        viewModel.formBoard.value = b
                                        showBoardDropdown = false
                                    })
                                }
                            }
                        }
                    }
                }

                // Sector and Detailed address
                Column {
                    Text("Select Target Noida Sector", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showSectorDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(sector)
                        }
                        DropdownMenu(
                            expanded = showSectorDropdown,
                            onDismissRequest = { showSectorDropdown = false }
                        ) {
                            viewModel.sectorsList.forEach { sec ->
                                DropdownMenuItem(text = { Text(sec) }, onClick = {
                                    viewModel.formSector.value = sec
                                    showSectorDropdown = false
                                })
                            }
                        }
                    }
                }

                Column {
                    Text("Street & Flat Detail Address", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    OutlinedTextField(
                        value = detailAddress,
                        onValueChange = { viewModel.formDetailAddress.value = it },
                        placeholder = { Text("Flat 402, Block C, Supertech Capetown") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Class Duration
                Column {
                    Text("Session Duration: $duration Hrs", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    Slider(
                        value = duration.toFloat(),
                        onValueChange = { viewModel.formDuration.value = it.toDouble() },
                        valueRange = 1f..3f,
                        steps = 3
                    )
                    Text("Est Cost: ₹${(500 * duration).toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Confirm Instant Book")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleBookingDialog(
    viewModel: TutorViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val subject by viewModel.formSubject.collectAsStateWithLifecycle()
    val className by viewModel.formClass.collectAsStateWithLifecycle()
    val board by viewModel.formBoard.collectAsStateWithLifecycle()
    val duration by viewModel.formDuration.collectAsStateWithLifecycle()
    val sector by viewModel.formSector.collectAsStateWithLifecycle()
    val detailAddress by viewModel.formDetailAddress.collectAsStateWithLifecycle()
    val bookingDate by viewModel.formDate.collectAsStateWithLifecycle()
    val bookingTime by viewModel.formTime.collectAsStateWithLifecycle()

    var showSubjectDropdown by remember { mutableStateOf(false) }
    var showClassDropdown by remember { mutableStateOf(false) }
    var showBoardDropdown by remember { mutableStateOf(false) }
    var showSectorDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Schedule Tutor",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFEA580C)
                )

                Text(
                    text = "Set the date, time and subject. We will guarantee a premium verified tutor matches with you ahead of the slot.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Divider(color = Color(0xFFF1F5F9))

                // Subject Selector
                Column {
                    Text("Select Subject", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showSubjectDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(subject)
                        }
                        DropdownMenu(
                            expanded = showSubjectDropdown,
                            onDismissRequest = { showSubjectDropdown = false }
                        ) {
                            viewModel.subjectsList.forEach { s ->
                                DropdownMenuItem(text = { Text(s) }, onClick = {
                                    viewModel.formSubject.value = s
                                    showSubjectDropdown = false
                                })
                            }
                        }
                    }
                }

                // Date and Time (Fields)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Date (YYYY-MM-DD)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                        OutlinedTextField(
                            value = bookingDate,
                            onValueChange = { viewModel.formDate.value = it },
                            placeholder = { Text("2026-06-28") }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Time (HH:MM)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                        OutlinedTextField(
                            value = bookingTime,
                            onValueChange = { viewModel.formTime.value = it },
                            placeholder = { Text("16:00") }
                        )
                    }
                }

                // Class and Board (Row)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Class", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showClassDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(className)
                            }
                            DropdownMenu(
                                expanded = showClassDropdown,
                                onDismissRequest = { showClassDropdown = false }
                            ) {
                                viewModel.classesList.forEach { c ->
                                    DropdownMenuItem(text = { Text(c) }, onClick = {
                                        viewModel.formClass.value = c
                                        showClassDropdown = false
                                    })
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Board", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showBoardDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(board)
                            }
                            DropdownMenu(
                                expanded = showBoardDropdown,
                                onDismissRequest = { showBoardDropdown = false }
                            ) {
                                viewModel.boardsList.forEach { b ->
                                    DropdownMenuItem(text = { Text(b) }, onClick = {
                                        viewModel.formBoard.value = b
                                        showBoardDropdown = false
                                    })
                                }
                            }
                        }
                    }
                }

                // Noida Sector and details
                Column {
                    Text("Noida Target Sector", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showSectorDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(sector)
                        }
                        DropdownMenu(
                            expanded = showSectorDropdown,
                            onDismissRequest = { showSectorDropdown = false }
                        ) {
                            viewModel.sectorsList.forEach { sec ->
                                DropdownMenuItem(text = { Text(sec) }, onClick = {
                                    viewModel.formSector.value = sec
                                    showSectorDropdown = false
                                })
                            }
                        }
                    }
                }

                Column {
                    Text("Street & Flat Detail Address", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    OutlinedTextField(
                        value = detailAddress,
                        onValueChange = { viewModel.formDetailAddress.value = it },
                        placeholder = { Text("Block C, Flat 402, Supertech") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Session Duration
                Column {
                    Text("Session Duration: $duration Hrs", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    Slider(
                        value = duration.toFloat(),
                        onValueChange = { viewModel.formDuration.value = it.toDouble() },
                        valueRange = 1f..3f,
                        steps = 3
                    )
                    Text("Est Cost: ₹${(500 * duration).toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C))
                    ) {
                        Text("Schedule Booking")
                    }
                }
            }
        }
    }
}

@Composable
fun TutorsListDialog(
    tutors: List<Tutor>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Top Verified Masters",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E1B4B),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(tutors) { tutor ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Star, contentDescription = "Master", tint = Color(0xFFF59E0B))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tutor.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(tutor.subjects, fontSize = 11.sp, color = Color(0xFF64748B))
                                Text("₹${tutor.costPerHour}/hr • Preferred: ${tutor.preferredSectors}", fontSize = 10.sp, color = Color(0xFF475569))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "Star", tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                                Text("${tutor.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B4B))
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AddTutorDialog(
    viewModel: TutorViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var subjects by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var preferredSectors by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Manual Onboard Tutor",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E293B)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tutor Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = subjects,
                    onValueChange = { subjects = it },
                    label = { Text("Subjects (Comma separated)") },
                    placeholder = { Text("e.g. Physics, Chemistry, Math") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Hourly rate (INR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = preferredSectors,
                    onValueChange = { preferredSectors = it },
                    label = { Text("Sectors (Comma separated)") },
                    placeholder = { Text("e.g. Sector 75, Sector 62") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && subjects.isNotEmpty() && phone.isNotEmpty()) {
                                val cost = rate.toDoubleOrNull() ?: 500.0
                                val sectorsStr = preferredSectors.ifEmpty { "Sector 75, Noida" }
                                viewModel.addTutorDirectly(name, subjects, phone, cost, sectorsStr)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Text("Add Tutor")
                    }
                }
            }
        }
    }
}

@Composable
fun AssignTutorDialog(
    booking: Booking,
    tutors: List<Tutor>,
    onDismiss: () -> Unit,
    onAssign: (Long) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Assign Tutor for ${booking.subject}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "Sector: ${booking.sectorAddress}\nClass: ${booking.className} • Board: ${booking.board}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Match tutors based on sector preference & subject
                val matchingTutors = tutors.filter { t ->
                    t.isActive && (t.preferredSectors.contains(booking.sectorAddress, ignoreCase = true) ||
                            t.subjects.contains(booking.subject, ignoreCase = true))
                }

                Text(
                    text = "Best Matching Nearby Tutors:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 280.dp)
                ) {
                    if (matchingTutors.isEmpty()) {
                        item {
                            Text("No explicit matching sector tutor. Showing all tutors.", fontSize = 11.sp, color = Color.Red)
                        }
                        items(tutors.filter { it.isActive }) { tutor ->
                            TutorMatchRow(tutor = tutor, onAssign = onAssign)
                        }
                    } else {
                        items(matchingTutors) { tutor ->
                            TutorMatchRow(tutor = tutor, onAssign = onAssign)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun TutorMatchRow(tutor: Tutor, onAssign: (Long) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(tutor.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(tutor.subjects, fontSize = 11.sp, color = Color(0xFF64748B))
            Text("₹${tutor.costPerHour}/hr • Sectors: ${tutor.preferredSectors}", fontSize = 10.sp, color = Color(0xFF475569))
        }
        Button(
            onClick = { onAssign(tutor.id) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("Assign", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SubmitReviewDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var ratingVal by remember { mutableStateOf(5) }
    var reviewText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Submit Class Review",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )

                Text(
                    text = "How was your session with ${booking.tutorName}? Rate them below.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { ratingVal = star }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star Stars",
                                tint = if (star <= ratingVal) Color(0xFFF59E0B) else Color(0xFFE2E8F0),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text("Write your review, e.g. Mr. Arvind cleared all doubts beautifully!") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 12.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = { onSubmit(ratingVal, reviewText) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Submit Review")
                    }
                }
            }
        }
    }
}
