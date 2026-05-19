package com.example.kotlinlensacerdasandroid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

// --- Ekstraksi Warna dari Tailwind Config ---
val BackgroundColor = Color(0xFF131314)
val PrimaryContainer = Color(0xFF2E5BFF)
val OnSurface = Color(0xFFE5E2E3)
val OnSurfaceVariant = Color(0xFFC4C5D9)
val SurfaceContainerLow = Color(0xFF1C1B1C)

@Composable
fun LensaCerdasSplashScreen(
    onNextClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // 1. Efek Glow Latar Belakang (Menggunakan Radial Gradient agar tidak terpotong kotak)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrimaryContainer.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension * 0.45f
                        )
                    )
                }
        )

        // 2. Konten Utama
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(2f)
            ) {
                // Wadah Logo
                Box(
                    modifier = Modifier.size(192.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Glow Logo (Radial Gradient)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            PrimaryContainer.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    )
                                )
                            }
                    )

                    // Gambar Logo
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "LensaCerdas Logo",
                        tint = ColorPrimaryContainerBox,
                        modifier = Modifier.size(100.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Nama Aplikasi
                Text(
                    text = "LensaCerdas",
                    color = OnSurface,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.64).sp,
                    lineHeight = 38.sp,
                    textAlign = TextAlign.Center
                )

                // Subtitle
                Text(
                    text = "Clarity through intelligence.",
                    color = OnSurfaceVariant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 25.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            // Bagian Bawah: Tombol Next
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(bottom = 48.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerLow.copy(alpha = 0.4f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                        .clickable { onNextClick() }
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "NEXT",
                        color = OnSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.6.sp
                    )
                }
            }
        }
    }
}
