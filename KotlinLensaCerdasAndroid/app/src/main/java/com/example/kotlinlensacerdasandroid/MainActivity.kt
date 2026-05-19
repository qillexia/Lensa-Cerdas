package com.example.kotlinlensacerdasandroid

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.kotlinlensacerdasandroid.network.ApiClient
import com.example.kotlinlensacerdasandroid.network.LoginRequest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val sharedPref = getSharedPreferences("LensaCerdasPrefs", Context.MODE_PRIVATE)
        val savedUser = sharedPref.getString("LOGGED_IN_USER", null)
        val savedPhoto = sharedPref.getString("LOGGED_IN_PHOTO", null)
        val savedId = sharedPref.getInt("LOGGED_IN_ID", -1)
        
        val initialScreen = if (savedUser != null) "dashboard" else "splash"

        setContent {
            var currentScreen by rememberSaveable { mutableStateOf(initialScreen) }
            var loggedInUser by rememberSaveable { mutableStateOf(savedUser ?: "Siswa AI") }
            var userPhotoUrl by rememberSaveable { mutableStateOf(savedPhoto) }
            var loggedInUserId by rememberSaveable { mutableStateOf(savedId) }
            val coroutineScope = rememberCoroutineScope()
            
            val screenOrder = listOf("splash", "onboarding", "login", "dashboard", "history", "summarize", "profile")

            Box(modifier = Modifier.fillMaxSize()) {
                // Tangani tombol back hp (system back)
                BackHandler(enabled = currentScreen in listOf("history", "summarize", "profile")) {
                    currentScreen = "dashboard"
                }

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        val isBottomNavTransition = (initialState in listOf("dashboard", "history", "summarize", "profile")) && 
                                                    (targetState in listOf("dashboard", "history", "summarize", "profile"))

                        if (isBottomNavTransition) {
                            fadeIn(animationSpec = tween(200)).togetherWith(fadeOut(animationSpec = tween(200)))
                        } else {
                            val initialIndex = screenOrder.indexOf(initialState)
                            val targetIndex = screenOrder.indexOf(targetState)
                            if (targetIndex > initialIndex) {
                                (slideInHorizontally(animationSpec = tween(400), initialOffsetX = { fullWidth -> fullWidth }) + fadeIn(animationSpec = tween(400))).togetherWith(
                                    slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { fullWidth -> -fullWidth }) + fadeOut(animationSpec = tween(400))
                                )
                            } else {
                                (slideInHorizontally(animationSpec = tween(400), initialOffsetX = { fullWidth -> -fullWidth }) + fadeIn(animationSpec = tween(400))).togetherWith(
                                    slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { fullWidth -> fullWidth }) + fadeOut(animationSpec = tween(400))
                                )
                            }
                        }
                    },
                    label = "ScreenTransition"
                ) { targetScreen ->
                    when (targetScreen) {
                        "splash" -> LensaCerdasSplashScreen(onNextClick = { currentScreen = "onboarding" })
                        "onboarding" -> OnboardingScreen(
                            onBackClick = { currentScreen = "splash" },
                            onNextClick = { currentScreen = "login" }
                        )
                        "login" -> LoginScreen(
                            onGoogleLoginSuccess = { userName, photoUrl, googleId ->
                                coroutineScope.launch {
                                    try {
                                        val req = LoginRequest(googleId, userName, googleId, photoUrl)
                                        val response = ApiClient.instance.loginWithGoogle(req)
                                        if (response.success && response.data != null) {
                                            sharedPref.edit().apply {
                                                putString("LOGGED_IN_USER", userName)
                                                putString("LOGGED_IN_PHOTO", photoUrl)
                                                putInt("LOGGED_IN_ID", response.data.id)
                                                apply()
                                            }
                                            loggedInUser = userName
                                            userPhotoUrl = photoUrl
                                            loggedInUserId = response.data.id
                                            currentScreen = "dashboard"
                                        }
                                    } catch (e: Exception) {
                                        Log.e("MainActivity", "Login error", e)
                                        sharedPref.edit().apply {
                                            putString("LOGGED_IN_USER", userName)
                                            putString("LOGGED_IN_PHOTO", photoUrl)
                                            apply()
                                        }
                                        loggedInUser = userName
                                        userPhotoUrl = photoUrl
                                        currentScreen = "dashboard"
                                    }
                                }
                            }
                        )
                        "dashboard" -> {
                            val viewModel: SummarizeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                            DashboardScreen(
                                userName = loggedInUser,
                                userPhotoUrl = userPhotoUrl,
                                userId = loggedInUserId,
                                onNavigateToSummarize = { currentScreen = "summarize" },
                                onNavigateToHistory = { currentScreen = "history" },
                                onPdfTextReady = { text, fileName ->
                                    viewModel.textInput = text
                                    viewModel.uploadedFileType = "Dokumen PDF"
                                    viewModel.uploadedFileRealName = fileName
                                    currentScreen = "summarize"
                                },
                                onLinkTextReady = { link ->
                                    viewModel.textInput = "Konten dari $link"
                                    viewModel.uploadedFileType = null
                                    viewModel.uploadedFileRealName = null
                                    currentScreen = "summarize"
                                }
                            )
                        }
                        "history" -> HistoryScreen(
                            userId = loggedInUserId,
                            userName = loggedInUser,
                            userPhotoUrl = userPhotoUrl,
                            onHomeClick = { currentScreen = "dashboard" }
                        )
                        "summarize" -> SummarizeScreen(
                            userId = loggedInUserId,
                            userName = loggedInUser,
                            userPhotoUrl = userPhotoUrl,
                            onSummarizeSuccess = { currentScreen = "history" }
                        )
                        "profile" -> ProfileScreen(
                            userName = loggedInUser,
                            userPhotoUrl = userPhotoUrl,
                            onLogoutClick = {
                                sharedPref.edit().clear().apply()
                                currentScreen = "login"
                            }
                        )
                    }
                }

                if (currentScreen in listOf("dashboard", "history", "summarize", "profile")) {
                    BottomNavBar(
                        currentTab = if (currentScreen == "dashboard") "home" else currentScreen,
                        onHomeClick = { currentScreen = "dashboard" },
                        onHistoryClick = { currentScreen = "history" },
                        onSummarizeClick = { currentScreen = "summarize" },
                        onProfileClick = { currentScreen = "profile" }
                    )
                }
            }
        }
    }
}
