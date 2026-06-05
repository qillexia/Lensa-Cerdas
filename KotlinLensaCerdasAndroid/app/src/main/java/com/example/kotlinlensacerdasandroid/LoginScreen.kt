package com.example.kotlinlensacerdasandroid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import coil.decode.GifDecoder
import android.os.Build
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import android.util.Log

val ColorLoginBackground = Color(0xFF0A0A0B)

@Composable
fun LoginScreen(
    onGoogleLoginSuccess: (String, String?, String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Definisi vector SVG Google Logo agar sesuai dengan Tailwind HTML
    val googleIcon = remember {
        ImageVector.Builder(
            name = "GoogleLogo",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(ColorOnPrimaryContainer)) {
                moveTo(22.56f, 12.25f)
                curveToRelative(0f, -0.78f, -0.07f, -1.53f, -0.2f, -2.25f)
                lineTo(12f, 10f)
                lineTo(12f, 14.26f)
                lineToRelative(5.92f, 0f)
                curveToRelative(-0.26f, 1.37f, -1.04f, 2.53f, -2.21f, 3.31f)
                lineToRelative(0f, 2.77f)
                lineToRelative(3.57f, 0f)
                curveToRelative(2.08f, -1.92f, 3.28f, -4.74f, 3.28f, -8.09f)
                close()
            }
            path(fill = SolidColor(ColorOnPrimaryContainer)) {
                moveTo(12f, 23f)
                curveToRelative(2.97f, 0f, 5.46f, -0.98f, 7.28f, -2.66f)
                lineToRelative(-3.57f, -2.77f)
                curveToRelative(-0.98f, 0.66f, -2.23f, 1.06f, -3.71f, 1.06f)
                curveToRelative(-2.86f, 0f, -5.29f, -1.93f, -6.16f, -4.53f)
                lineTo(2.18f, 14.1f)
                lineToRelative(0f, 2.84f)
                curveTo(3.99f, 20.53f, 7.7f, 23f, 12f, 23f)
                close()
            }
            path(fill = SolidColor(ColorOnPrimaryContainer)) {
                moveTo(5.84f, 14.09f)
                curveToRelative(-0.22f, -0.66f, -0.35f, -1.36f, -0.35f, -2.09f)
                reflectiveCurveToRelative(0.13f, -1.43f, 0.35f, -2.09f)
                lineTo(5.84f, 7.07f)
                lineTo(2.18f, 7.07f)
                curveTo(1.43f, 8.55f, 1f, 10.22f, 1f, 12f)
                reflectiveCurveToRelative(0.43f, 3.45f, 1.18f, 4.93f)
                lineToRelative(2.85f, -2.22f)
                lineToRelative(0.81f, -0.62f)
                close()
            }
            path(fill = SolidColor(ColorOnPrimaryContainer)) {
                moveTo(12f, 5.38f)
                curveToRelative(1.62f, 0f, 3.06f, 0.56f, 4.21f, 1.64f)
                lineToRelative(3.15f, -3.15f)
                curveTo(17.45f, 2.09f, 14.97f, 1f, 12f, 1f)
                curveTo(7.7f, 1f, 3.99f, 3.47f, 2.18f, 7.07f)
                lineToRelative(3.66f, 2.84f)
                curveToRelative(0.87f, -2.6f, 3.3f, -4.53f, 6.16f, -4.53f)
                close()
            }
        }.build()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorLoginBackground)
            .drawBehind {
                // Background ambient glows defined in Tailwind CSS
                // radial-gradient(circle at 50% 0%, rgba(46, 91, 255, 0.1) 0%, transparent 60%)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryContainer.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, 0f),
                        radius = size.width * 0.6f
                    )
                )
                // radial-gradient(circle at 0% 100%, rgba(46, 91, 255, 0.05) 0%, transparent 40%)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryContainer.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(0f, size.height),
                        radius = size.width * 0.4f
                    )
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Main Content Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Logo Image
                Box(
                    modifier = Modifier
                        .size(192.dp)
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Glow Behind Logo
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            PrimaryContainer.copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    )
                                )
                            }
                    )

                    val context = LocalContext.current
                    val imageLoader = remember {
                        ImageLoader.Builder(context)
                            .components {
                                if (Build.VERSION.SDK_INT >= 28) {
                                    add(ImageDecoderDecoder.Factory())
                                } else {
                                    add(GifDecoder.Factory())
                                }
                            }
                            .build()
                    }

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(R.drawable.awesome)
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = "LensaCerdas Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                }

                Text(
                    text = "Welcome!",
                    color = OnSurface,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.64).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Sign in to continue your learning journey",
                    color = OnSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    lineHeight = 28.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Action Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 28.dp)
                    .padding(bottom = 10.dp)
                    .navigationBarsPadding()
            ) {
                Surface(
                    color = PrimaryContainer,
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable {
                            coroutineScope.launch {
                                try {
                                    val credentialManager = CredentialManager.create(context)
                                    
                                    // Masukkan Web Client ID lengkap Anda di sini
                                    // (Yang Anda buat di Google Cloud Console dengan tipe "Web application")
                                    val webClientId = "1031739868900-8uclklt364p6i7mefe1gn3f893r0t4kn.apps.googleusercontent.com"
                                    
                                    val googleIdOption = GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                        .setServerClientId(webClientId)
                                        .setAutoSelectEnabled(true)
                                        .build()
                                    
                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleIdOption)
                                        .build()
                                    
                                    val result = credentialManager.getCredential(
                                        request = request,
                                        context = context
                                    )
                                    
                                    val credential = result.credential
                                    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                        val displayName = googleIdTokenCredential.displayName ?: "User AI"
                                        val profilePictureUri = googleIdTokenCredential.profilePictureUri?.toString()
                                        val googleId = googleIdTokenCredential.id // Digunakan juga sebagai email
                                        
                                        // Panggil callback berhasil
                                        onGoogleLoginSuccess(displayName, profilePictureUri, googleId)
                                    }
                                } catch (e: Exception) {
                                    Log.e("LoginScreen", "Google Sign In Error", e)
                                    android.widget.Toast.makeText(context, "Login gagal: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = googleIcon,
                            contentDescription = "Google Logo",
                            tint = ColorOnPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            color = ColorOnPrimaryContainer,
                            fontSize = 17.sp, // approximate headline-md
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
