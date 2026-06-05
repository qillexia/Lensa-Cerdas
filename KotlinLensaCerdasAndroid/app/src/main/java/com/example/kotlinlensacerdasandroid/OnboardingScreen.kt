package com.example.kotlinlensacerdasandroid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import coil.decode.GifDecoder
import android.os.Build

// Warna Tambahan
val ColorSecondaryContainer = Color(0xFF14D1FF)
val ColorOnPrimaryContainer = Color(0xFFEFEFFF)

@Composable
fun OnboardingScreen(
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // --- 1. Background Grid Pattern ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 40.dp.toPx()
            val gridColor = Color.White.copy(alpha = 0.03f)
            
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += step
            }
            
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += step
            }
        }

        // --- 2. Ambient Background Glows ---
        // Top Center/Left Glow
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 100.dp)
                .size(400.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrimaryContainer.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
                }
        )
        // Bottom Right Glow
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-50).dp, y = (-100).dp)
                .size(300.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ColorSecondaryContainer.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    )
                }
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // --- 3. Top Navigation Anchor ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .statusBarsPadding(), // Menghindari status bar
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(48.dp) // w-12 h-12
                        .clip(CircleShape)
                        .background(SurfaceContainerLow.copy(alpha = 0.4f))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = OnSurface
                    )
                }

                // Title
                Text(
                    text = "LensaCerdas",
                    color = OnSurface,
                    fontSize = 24.sp, // font-headline-md
                    fontWeight = FontWeight.Bold
                )

                // Spacer for balance
                Spacer(modifier = Modifier.size(48.dp))
            }

            // --- 4. Central Content Canvas ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Hero Visual
                    Box(
                        modifier = Modifier.size(192.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Glow belakang logo
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
                            modifier = Modifier.size(120.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Typography Cluster
                    Text(
                        text = buildAnnotatedString {
                            append("Clarity through\n")
                            withStyle(style = SpanStyle(color = PrimaryContainer)) {
                                append("Intelligence")
                            }
                        },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 38.sp,
                        letterSpacing = (-0.64).sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "LensaCerdas leverages Gemini AI to distill complex study materials into clear, concise summaries, helping you master any subject with ease.",
                        fontSize = 15.sp,
                        color = OnSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Progressive Disclosure Indicator (Step Dots)
                }
            }

            // --- 5. Bottom Action Shell ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 130.dp, vertical = 28.dp)
                    .navigationBarsPadding() // Menghindari navigation bar
            ) {
                Surface(
                    color = PrimaryContainer,
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable { onNextClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "NEXT",
                            color = ColorOnPrimaryContainer,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Dot(active: Boolean) {
    Box(
        modifier = Modifier
            .size(width = if (active) 24.dp else 8.dp, height = 8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (active) PrimaryContainer else OnSurfaceVariant.copy(alpha = 0.2f))
    )
}
