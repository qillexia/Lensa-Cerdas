package com.example.kotlinlensacerdasandroid

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String = "Budi",
    userPhotoUrl: String? = null,
    onLogoutClick: () -> Unit = {}
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var showPersonalDataDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBg)
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    delay(1000)
                    isRefreshing = false
                    Toast.makeText(context, "Data profil terbaru berhasil disinkronkan!", Toast.LENGTH_SHORT).show()
                }
            },
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .statusBarsPadding() // Menambah jarak aman dari status bar sistem
                    .padding(top = 24.dp, bottom = 120.dp)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {

                Text(
                    text = "Akun Anda",
                    color = ColorOnSurfaceMain,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                // Profile Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColorSurfaceContainerLowBox.copy(alpha = 0.8f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(3.dp, ColorPrimaryContainerBox.copy(alpha = 0.3f), CircleShape)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(userPhotoUrl ?: "https://lh3.googleusercontent.com/aida-public/AB6AXuAqo44uvkWLicU0ODlM0CjIG6eMC66sEE4EnQUHpaM_0HsJrQV-h7pnWfNBvbixW-rpQZQhjZZpC0pA5eXjORxhbUBFLwol20B7ELTUMOSkWqgLwxUO7Z403qLAXhcKZscPk1yBUntkHylcP1quKoLTMmWhXKb3Ybm7jWK-9KTvgYG7bpxj2QyKSjTrCdwNqoHSPcpOIO7TlHHj6sQY0N6hjOZTT31ttY69WHRr-1IXYRGOL9sacI4wl5H1xCWAyKOPaEU6NhAxUnjX")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = userName,
                                color = ColorOnSurfaceMain,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = "Email",
                                    tint = ColorOnSurfaceVar,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Akun Google Tertaut",
                                    color = ColorOnSurfaceVar,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "PENGATURAN UMUM",
                        color = ColorOnSurfaceVar,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.6.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    SettingItem(
                        icon = Icons.Outlined.Person, 
                        title = "Data Personal",
                        onClick = { showPersonalDataDialog = true }
                    )
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "DUKUNGAN",
                        color = ColorOnSurfaceVar,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.6.sp,
                        modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                    )

                    SettingItem(
                        icon = Icons.Outlined.HelpOutline, 
                        title = "Pusat Bantuan",
                        onClick = { showHelpDialog = true }
                    )
                    SettingItem(
                        icon = Icons.Outlined.Info, 
                        title = "Tentang LensaCerdas",
                        onClick = { showAboutDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = onLogoutClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF93000A).copy(alpha = 0.2f),
                        contentColor = Color(0xFFFFB4AB)
                    ),
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(13.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Logout, contentDescription = "Logout")
                        Text(text = "Keluar dari Akun", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showPersonalDataDialog) PersonalDataDialog(userName, onDismiss = { showPersonalDataDialog = false })
    if (showHelpDialog) HelpCenterDialog(onDismiss = { showHelpDialog = false })
    if (showAboutDialog) AboutDialog(onDismiss = { showAboutDialog = false })
}

@Composable
fun SettingItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorSurfaceContainerLowBox.copy(alpha = 0.8f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = ColorOnSurfaceVar)
                Text(text = title, color = ColorOnSurfaceMain, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Go",
                tint = ColorOnSurfaceVar
            )
        }
    }
}

@Composable
fun PersonalDataDialog(userName: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorSurfaceContainerLowBox,
        title = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Data Personal", color = ColorOnSurfaceMain, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = userName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ColorOnSurfaceMain,
                        unfocusedTextColor = ColorOnSurfaceMain,
                        focusedLabelColor = Color.White.copy(alpha = 0.7f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        focusedBorderColor = ColorPrimaryContainerBox,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    )
                )
                OutlinedTextField(
                    value = "Terkoneksi dengan Google",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status Akun") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ColorPrimaryContainerBox,
                        unfocusedTextColor = ColorPrimaryContainerBox,
                        focusedLabelColor = Color.White.copy(alpha = 0.7f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        focusedBorderColor = ColorPrimaryContainerBox,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    )
                )
            }
        },
        confirmButton = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = onDismiss) {
                    Text("Tutup", color = ColorPrimaryContainerBox)
                }
            }
        }
    )
}

@Composable
fun HelpCenterDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorSurfaceContainerLowBox,
        title = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Pusat Bantuan", color = ColorOnSurfaceMain, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Mengalami kendala saat menggunakan LensaCerdas? Tim dukungan kami siap membantu Anda 24/7.",
                    color = ColorOnSurfaceVar,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorBg)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.Email, null, tint = ColorPrimaryContainerBox)
                        Spacer(Modifier.width(12.dp))
                        Text("haqilabdillah@gmail.com", color = ColorOnSurfaceMain, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        confirmButton = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                            data = android.net.Uri.parse("mailto:haqilabdillah@gmail.com")
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Bantuan LensaCerdas")
                        }
                        context.startActivity(intent)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryContainerBox, contentColor = ColorOnPrimaryContainerText)
                ) {
                    Text("Kirim Email")
                }
            }
        }
    )
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorSurfaceContainerLowBox,
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp), // <-- Tambahkan margin/padding bawah di sini
                contentAlignment = Alignment.Center
            ) {
                Text("Tentang LensaCerdas", color = ColorOnSurfaceMain, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ColorSurfaceContainerHighestBox)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = ColorPrimaryContainerBox,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LensaCerdas", color = ColorOnSurfaceMain, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Versi 1.0.0 (Zen Edition)", color = ColorPrimaryContainerBox, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                
                Text(
                    text = "LensaCerdas adalah asisten AI revolusioner yang dirancang khusus untuk membantu mahasiswa dan profesional. Dengan integrasi OCR canggih dan Natural Language Processing terdepan, aplikasi ini mampu mengubah teks panjang dari buku maupun dokumen digital menjadi ringkasan yang mudah dipahami dalam hitungan detik.",
                    color = ColorOnSurfaceVar,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ColorSurfaceContainerHighestBox.copy(alpha = 0.5f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pengembang", color = ColorOnSurfaceVar, fontSize = 12.sp)
                        Text("Haqil Abdillah", color = ColorOnSurfaceMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Basis AI", color = ColorOnSurfaceVar, fontSize = 12.sp)
                        Text("Gemini 1.5 Pro", color = ColorOnSurfaceMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Framework", color = ColorOnSurfaceVar, fontSize = 12.sp)
                        Text("Jetpack Compose", color = ColorOnSurfaceMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                
                Text("Dibuat dengan ❤️ di Indonesia", color = ColorOnSurfaceVar, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
        },
        confirmButton = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = onDismiss) {
                    Text("Tutup", color = ColorPrimaryContainerBox)
                }
            }
        }
    )
}
