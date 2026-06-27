package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorAuthView(
    viewModel: TutorViewModel,
    onLoginSuccess: (role: String, identifier: String) -> Unit
) {
    var currentAuthScreen by remember { mutableStateOf("WELCOME") } // "WELCOME", "STUDENT_LOGIN", "TUTOR_LOGIN", "ADMIN_LOGIN"
    
    // Core states
    var phoneInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    
    var showPassword by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var isOtpSent by remember { mutableStateOf(false) }
    
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Royal Blue Theme Gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFEFF6FF), // Soft Blue
            Color(0xFFFFFFFF), // Pure White
            Color(0xFFF8FAFC)  // Slate Light
        )
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .systemBarsPadding()
    ) {
        val isWide = maxWidth > 600.dp
        
        // Centered responsive container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isWide) 24.dp else 16.dp),
            contentAlignment = if (isWide) Alignment.Center else Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .then(
                        if (isWide) {
                            Modifier.shadow(24.dp, shape = RoundedCornerShape(28.dp))
                        } else {
                            Modifier
                        }
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isWide) Color.White else Color.Transparent
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isWide) 32.dp else 16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    
                    // Animated content transition
                    AnimatedContent(
                        targetState = currentAuthScreen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(200))
                        },
                        label = "AuthScreenTransition"
                    ) { screen ->
                        when (screen) {
                            "WELCOME" -> WelcomeScreen(
                                onNavigateTo = { screenKey ->
                                    errorMessage = null
                                    successMessage = null
                                    isOtpSent = false
                                    phoneInput = ""
                                    passwordInput = ""
                                    otpInput = ""
                                    emailInput = ""
                                    currentAuthScreen = screenKey
                                }
                            )
                            "STUDENT_LOGIN" -> StudentLoginFlow(
                                phone = phoneInput,
                                onPhoneChange = { phoneInput = it },
                                otp = otpInput,
                                onOtpChange = { otpInput = it },
                                isOtpSent = isOtpSent,
                                isSubmitting = isSubmitting,
                                errorMessage = errorMessage,
                                successMessage = successMessage,
                                onSendOtp = {
                                    if (phoneInput.length < 10) {
                                        errorMessage = "Please enter a valid 10-digit mobile number"
                                    } else {
                                        errorMessage = null
                                        coroutineScope.launch {
                                            isSubmitting = true
                                            delay(1000)
                                            isSubmitting = false
                                            isOtpSent = true
                                            successMessage = "OTP sent! Use code 123456 to log in."
                                        }
                                    }
                                },
                                onVerifyLogin = {
                                    if (otpInput.length != 6) {
                                        errorMessage = "Please enter the 6-digit OTP"
                                    } else if (otpInput != "123456") {
                                        errorMessage = "Incorrect OTP. Use 123456 for demo mode."
                                    } else {
                                        errorMessage = null
                                        coroutineScope.launch {
                                            isSubmitting = true
                                            delay(1200)
                                            isSubmitting = false
                                            onLoginSuccess("STUDENT", "Student/Parent (+91 $phoneInput)")
                                        }
                                    }
                                },
                                onGoogleLogin = {
                                    coroutineScope.launch {
                                        isSubmitting = true
                                        successMessage = "Connecting securely to Google Accounts..."
                                        delay(1500)
                                        isSubmitting = false
                                        onLoginSuccess("STUDENT", "Google User (Demo)")
                                    }
                                },
                                onBack = { currentAuthScreen = "WELCOME" }
                            )
                            "TUTOR_LOGIN" -> TutorLoginFlow(
                                phone = phoneInput,
                                onPhoneChange = { phoneInput = it },
                                password = passwordInput,
                                onPasswordChange = { passwordInput = it },
                                otp = otpInput,
                                onOtpChange = { otpInput = it },
                                rememberMe = rememberMe,
                                onRememberMeChange = { rememberMe = it },
                                showPassword = showPassword,
                                onShowPasswordToggle = { showPassword = !showPassword },
                                isSubmitting = isSubmitting,
                                errorMessage = errorMessage,
                                successMessage = successMessage,
                                onLogin = {
                                    if (phoneInput.length < 10) {
                                        errorMessage = "Please enter a valid 10-digit mobile number"
                                    } else if (passwordInput.length < 4) {
                                        errorMessage = "Password must be at least 4 characters"
                                    } else {
                                        errorMessage = null
                                        coroutineScope.launch {
                                            isSubmitting = true
                                            delay(1200)
                                            isSubmitting = false
                                            // By default, tutor session maps to Tutor 1 (or match)
                                            onLoginSuccess("TUTOR", "Tutor Professional (+91 $phoneInput)")
                                        }
                                    }
                                },
                                onBack = { currentAuthScreen = "WELCOME" }
                            )
                            "ADMIN_LOGIN" -> AdminLoginFlow(
                                email = emailInput,
                                onEmailChange = { emailInput = it },
                                password = passwordInput,
                                onPasswordChange = { passwordInput = it },
                                isSubmitting = isSubmitting,
                                errorMessage = errorMessage,
                                onLogin = {
                                    if (!emailInput.contains("@") || emailInput.length < 5) {
                                        errorMessage = "Please enter a valid administrator email"
                                    } else if (passwordInput.length < 4) {
                                        errorMessage = "Password is too short"
                                    } else {
                                        errorMessage = null
                                        coroutineScope.launch {
                                            isSubmitting = true
                                            delay(1200)
                                            isSubmitting = false
                                            onLoginSuccess("ADMIN", emailInput)
                                        }
                                    }
                                },
                                onBack = { currentAuthScreen = "WELCOME" }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// WELCOME SCREEN
// ------------------------------------------------------------------------
@Composable
fun WelcomeScreen(onNavigateTo: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Luxury Logo Emblem
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF2563EB), Color(0xFF3B82F6))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Logo Star",
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // App Branding
        Text(
            text = "TUTOR_NOW",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A),
            letterSpacing = 1.5.sp
        )
        
        Text(
            text = "Premium, On-Demand Private Education",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Professional Onboarding Graphic Representation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Premium verified",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Verified Elite Educators",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                    Text(
                        text = "Curated top-tier tutors delivered safely to your location in Noida.",
                        fontSize = 11.sp,
                        color = Color(0xFF1E40AF).copy(alpha = 0.8f)
                    )
                }
            }
        }

        Text(
            text = "CONTINUE YOUR EXPERIENCE AS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Persona Selection Buttons (Luxury Card Style)
        PersonaOptionCard(
            title = "Student / Parent Portal",
            subtitle = "Book hyper-personalized elite home tutors nearby",
            icon = Icons.Default.Person,
            colorScheme = Color(0xFF2563EB),
            onClick = { onNavigateTo("STUDENT_LOGIN") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        PersonaOptionCard(
            title = "Elite Tutor Workspace",
            subtitle = "Manage scheduling, accept bookings & track wallet",
            icon = Icons.Default.Edit,
            colorScheme = Color(0xFF0F172A),
            onClick = { onNavigateTo("TUTOR_LOGIN") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        PersonaOptionCard(
            title = "Admin HQ Dashboard",
            subtitle = "Authorize partners, verify credentials & audit payouts",
            icon = Icons.Default.Settings,
            colorScheme = Color(0xFFF97316),
            onClick = { onNavigateTo("ADMIN_LOGIN") }
        )
        
        Spacer(modifier = Modifier.height(30.dp))
        
        // Trust and security disclaimer
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Secured",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "End-to-End Encrypted Session Management",
                fontSize = 10.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
fun PersonaOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    colorScheme: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = colorScheme,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Go",
                tint = Color(0xFF94A3B8)
            )
        }
    }
}

// ------------------------------------------------------------------------
// STUDENT LOGIN SCREEN
// ------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLoginFlow(
    phone: String,
    onPhoneChange: (String) -> Unit,
    otp: String,
    onOtpChange: (String) -> Unit,
    isOtpSent: Boolean,
    isSubmitting: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onSendOtp: () -> Unit,
    onVerifyLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Back Button & Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Student / Parent Portal",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Welcome to TutorNow",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A)
        )
        Text(
            text = "Verify your phone number with OTP to browse top-rated home tutors.",
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Error Banner
        errorMessage?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(it, color = Color(0xFF991B1B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Success Banner
        successMessage?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(it, color = Color(0xFF065F46), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        if (!isOtpSent) {
            // Step 1: Input Phone
            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 10) onPhoneChange(it) },
                label = { Text("Phone Number") },
                placeholder = { Text("98765 XXXXX") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSendOtp,
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Get Verification Code", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                Text(
                    "OR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Premium Google Login Button
            OutlinedButton(
                onClick = onGoogleLogin,
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBox, // Represents Google Emblem for simplicity
                        contentDescription = "Google Logo",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Continue with Google", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

        } else {
            // Step 2: Input OTP
            Text(
                "Verification Code Sent",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                "An SMS was sent to +91 $phone with the login credentials.",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) onOtpChange(it) },
                label = { Text("6-Digit OTP Code") },
                placeholder = { Text("123456") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onVerifyLogin,
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Verify & Authenticate", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onSendOtp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Resend OTP Code", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Privacy Policy / Terms of Service footer
        Text(
            text = "By signing in, you agree to our Terms of Use and Privacy Policy.",
            fontSize = 10.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ------------------------------------------------------------------------
// TUTOR LOGIN SCREEN
// ------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorLoginFlow(
    phone: String,
    onPhoneChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    otp: String,
    onOtpChange: (String) -> Unit,
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    showPassword: Boolean,
    onShowPasswordToggle: () -> Unit,
    isSubmitting: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onLogin: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Elite Tutor Workspace",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Partner Login",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A)
        )
        Text(
            text = "Provide your registered mobile number and secure password to open your roster.",
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Error Banner
        errorMessage?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(it, color = Color(0xFF991B1B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.length <= 10) onPhoneChange(it) },
            label = { Text("Registered Phone Number") },
            placeholder = { Text("98765 XXXXX") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2563EB),
                unfocusedBorderColor = Color(0xFFE2E8F0)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Security Password") },
            placeholder = { Text("••••••••") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
            trailingIcon = {
                IconButton(onClick = onShowPasswordToggle) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.FavoriteBorder else Icons.Default.Favorite, // Simple lock alternative visibility representing eyeball state
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2563EB),
                unfocusedBorderColor = Color(0xFFE2E8F0)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Remember Me & Forgot Password
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = onRememberMeChange,
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB))
                )
                Text("Remember Me", fontSize = 12.sp, color = Color(0xFF475569))
            }

            TextButton(onClick = { /* Demo Action */ }) {
                Text("Forgot Password?", color = Color(0xFF2563EB), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogin,
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Enter Workspace", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // OTP verification alternative
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Having trouble logging in?", fontSize = 11.sp, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Request OTP Access",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB),
                modifier = Modifier.clickable { /* Demo */ }
            )
        }
    }
}

// ------------------------------------------------------------------------
// ADMIN LOGIN SCREEN
// ------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginFlow(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isSubmitting: Boolean,
    errorMessage: String?,
    onLogin: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Admin HQ Dashboard",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Secure Admin Auth",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A)
        )
        Text(
            text = "Only authorized corporate staff can access internal analytics databases.",
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Error Banner
        errorMessage?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(it, color = Color(0xFF991B1B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Multi-Factor Badge (M3 Style Indicator)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = "Safe", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🛡️ 2-Factor Authentication (2FA) Ready",
                    color = Color(0xFF065F46),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Corporate Email Address") },
            placeholder = { Text("admin@tutornow.in") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2563EB),
                unfocusedBorderColor = Color(0xFFE2E8F0)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Security Access Token") },
            placeholder = { Text("••••••••") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2563EB),
                unfocusedBorderColor = Color(0xFFE2E8F0)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogin,
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316))
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Authorize Access Session", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
