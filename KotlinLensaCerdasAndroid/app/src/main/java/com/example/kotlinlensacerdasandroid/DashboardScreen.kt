package com.example.kotlinlensacerdasandroid

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kotlinlensacerdasandroid.network.ApiClient
import com.example.kotlinlensacerdasandroid.network.HistoryItem
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Custom Colors
val ColorBg = Color(0xFF131314)
val ColorOnSurfaceMain = Color(0xFFE5E2E3)
val ColorOnSurfaceVar = Color(0xFFC4C5D9)
val ColorPrimaryContainerBox = Color(0xFF2E5BFF)
val ColorOnPrimaryContainerText = Color(0xFFEFEFFF)
val ColorSecondaryContainerBox = Color(0xFF14D1FF)
val ColorSecondaryText = Color(0xFFA6E6FF)
val ColorTertiaryContainerBox = Color(0xFFC24100)
val ColorTertiaryText = Color(0xFFFFB59B)
val ColorSurfaceContainerLowBox = Color(0xFF1C1B1C)
val ColorSurfaceContainerHighestBox = Color(0xFF353436)
val ColorPrimaryText = Color(0xFFB8C3FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userName: String = "Budi",
    userPhotoUrl: String? = null,
    userId: Int = -1,
    onNavigateToSummarize: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onPdfTextReady: (String, String?) -> Unit = { _, _ -> },
    onLinkTextReady: (String) -> Unit = {}
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showLinkDialog by remember { mutableStateOf(false) }
    var recentItems by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }

    // Load recent 2 items
    LaunchedEffect(userId) {
        if (userId > 0) {
            try {
                val response = ApiClient.instance.getHistory(userId)
                if (response.success) {
                    recentItems = (response.data ?: emptyList()).take(2)
                }
            } catch (_: Exception) { }
        }
    }

    // PDF file picker
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val readerPdf = PdfReader(inputStream)
                    val sb = StringBuilder()
                    for (i in 1..readerPdf.numberOfPages) {
                        val text = PdfTextExtractor.getTextFromPage(readerPdf, i)
                        if (text.isNotBlank()) sb.append(text).append("\n")
                    }
                    readerPdf.close()
                    val extractedText = sb.toString()
                    if (extractedText.isBlank()) {
                        Toast.makeText(context, "PDF tidak memiliki teks yang bisa dibaca", Toast.LENGTH_LONG).show()
                    } else {
                        val fileName = getFileNameFromUri(context, uri)
                        onPdfTextReady(extractedText, fileName)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal membaca PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                    if (userId > 0) {
                        try {
                            val response = ApiClient.instance.getHistory(userId)
                            if (response.success) recentItems = (response.data ?: emptyList()).take(2)
                        } catch (_: Exception) { }
                    }
                    delay(500)
                    isRefreshing = false
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
                    .padding(top = 110.dp, bottom = 120.dp)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                HeroSection(userName, onStartClick = onNavigateToSummarize)
                FeatureGridSection(
                    onScanClick = onNavigateToSummarize,
                    onUploadPdfClick = { pdfLauncher.launch("application/pdf") },
                    onLinkClick = { showLinkDialog = true }
                )
                RecentActivitySection(
                    items = recentItems,
                    onViewAllClick = onNavigateToHistory
                )
            }
        }

        // Top App Bar
        TopNavBar(userName, userPhotoUrl)
    }

    // Link Dialog
    if (showLinkDialog) {
        DashboardLinkDialog(
            onDismiss = { showLinkDialog = false },
            onConfirm = { link ->
                showLinkDialog = false
                onLinkTextReady(link)
            }
        )
    }
}

@Composable
fun TopNavBar(userName: String, userPhotoUrl: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorBg.copy(alpha = 1f))
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = "Logo",
                tint = ColorPrimaryContainerBox,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "LensaCerdas",
                color = ColorOnSurfaceMain,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.5).sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = userName,
                    color = ColorOnSurfaceMain,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Google Account",
                    color = ColorOnSurfaceVar,
                    fontSize = 10.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, ColorPrimaryContainerBox.copy(alpha = 0.2f), CircleShape)
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
        }
    }
}

@Composable
fun HeroSection(userName: String, onStartClick: () -> Unit = {}) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val shortName = userName.split(" ").firstOrNull() ?: "Budi"
            Text(
                text = "Hallo $shortName!",
                color = ColorOnSurfaceMain,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Siap untuk mensintesis pengetahuan hari ini?",
                color = ColorOnSurfaceVar,
                fontSize = 14.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1C1B1C).copy(alpha = 0.8f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 40.dp, y = 40.dp)
                    .size(160.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(ColorPrimaryContainerBox.copy(alpha = 0.2f), Color.Transparent)
                            )
                        )
                    }
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ColorPrimaryContainerBox.copy(alpha = 0.2f))
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Create",
                        tint = ColorPrimaryContainerBox
                    )
                }

                Column {
                    Text(
                        text = "Buat Ringkasan Baru",
                        color = ColorOnSurfaceMain,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Biarkan AI kami memproses dokumen atau gambar Anda menjadi poin-poin cerdas.",
                        color = ColorOnSurfaceVar,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Surface(
                    color = ColorPrimaryContainerBox,
                    shape = CircleShape,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable { onStartClick() }.padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = ColorOnPrimaryContainerText,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Mulai Sekarang",
                            color = ColorOnPrimaryContainerText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureGridSection(
    onScanClick: () -> Unit = {},
    onUploadPdfClick: () -> Unit = {},
    onLinkClick: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "INPUT CERDAS",
            color = ColorOnSurfaceVar,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.6.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.DocumentScanner,
                iconTint = ColorSecondaryContainerBox,
                iconBg = ColorSecondaryContainerBox.copy(alpha = 0.1f),
                title = "Scan Dokumen",
                onClick = onScanClick
            )
            FeatureCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.CloudUpload,
                iconTint = ColorTertiaryText,
                iconBg = ColorTertiaryContainerBox.copy(alpha = 0.1f),
                title = "Unggah PDF",
                onClick = onUploadPdfClick
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1B1C).copy(alpha = 0.8f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .clickable { onLinkClick() }
                .padding(20.dp)
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
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ColorPrimaryContainerBox.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Link,
                            contentDescription = "Link",
                            tint = ColorPrimaryContainerBox
                        )
                    }
                    Text(
                        text = "Ringkas Tautan Web",
                        color = ColorOnSurfaceMain,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Arrow Right",
                    tint = ColorOnSurfaceVar
                )
            }
        }
    }
}

@Composable
fun FeatureCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1B1C).copy(alpha = 0.8f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint
                )
            }
            Text(
                text = title,
                color = ColorOnSurfaceMain,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun RecentActivitySection(
    items: List<HistoryItem> = emptyList(),
    onViewAllClick: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AKTIVITAS TERKINI",
                color = ColorOnSurfaceVar,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.6.sp
            )
            Text(
                text = "Lihat Semua",
                color = ColorPrimaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onViewAllClick() }
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1C1B1C).copy(alpha = 0.8f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada aktivitas", color = ColorOnSurfaceVar, fontSize = 14.sp)
                }
            } else {
                items.forEach { item ->
                    ActivityItem(
                        icon = Icons.Outlined.Description,
                        iconTint = ColorPrimaryText,
                        title = item.title,
                        subtitle = formatDateTime(item.created_at)
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1B1C).copy(alpha = 0.8f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .clickable { }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ColorSurfaceContainerHighestBox),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Icon",
                        tint = iconTint
                    )
                }
                Column {
                    Text(
                        text = title,
                        color = ColorOnSurfaceMain,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        color = ColorOnSurfaceVar,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = ColorOnSurfaceVar
            )
        }
    }
}

@Composable
fun BottomNavBar(
    currentTab: String = "home",
    onHomeClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onSummarizeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth()
                .height(64.dp)
                .clip(CircleShape)
                .background(ColorSurfaceContainerLowBox)
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                val isHome = currentTab == "home"
                val homeBg by animateColorAsState(if (isHome) ColorPrimaryContainerBox else Color.Transparent, label = "homeBg")
                val homeIcon by animateColorAsState(if (isHome) ColorOnPrimaryContainerText else ColorOnSurfaceVar.copy(alpha = 0.6f), label = "homeIcon")
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(homeBg)
                        .clickable { onHomeClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "Home",
                        tint = homeIcon
                    )
                }
                
                // History
                val isHistory = currentTab == "history"
                val histBg by animateColorAsState(if (isHistory) ColorPrimaryContainerBox else Color.Transparent, label = "histBg")
                val histIcon by animateColorAsState(if (isHistory) ColorOnPrimaryContainerText else ColorOnSurfaceVar.copy(alpha = 0.6f), label = "histIcon")
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(histBg)
                        .clickable { onHistoryClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = "History",
                        tint = histIcon
                    )
                }
                
                // Summarize
                val isSummarize = currentTab == "summarize"
                val sumBg by animateColorAsState(if (isSummarize) ColorPrimaryContainerBox else Color.Transparent, label = "sumBg")
                val sumIcon by animateColorAsState(if (isSummarize) ColorOnPrimaryContainerText else ColorOnSurfaceVar.copy(alpha = 0.6f), label = "sumIcon")
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(sumBg)
                        .clickable { onSummarizeClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Summarize,
                        contentDescription = "Summarize",
                        tint = sumIcon
                    )
                }
                
                // Person (Profile)
                val isProfile = currentTab == "profile"
                val profBg by animateColorAsState(if (isProfile) ColorPrimaryContainerBox else Color.Transparent, label = "profBg")
                val profIcon by animateColorAsState(if (isProfile) ColorOnPrimaryContainerText else ColorOnSurfaceVar.copy(alpha = 0.6f), label = "profIcon")
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(profBg)
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Profile",
                        tint = profIcon
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardLinkDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var link by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceContainerLowBox)
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Ringkas Tautan Web", fontWeight = FontWeight.Bold, color = ColorOnSurfaceMain, fontSize = 18.sp)
                Text("Tempel URL halaman web yang ingin diringkas", color = ColorOnSurfaceVar, fontSize = 13.sp)
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    placeholder = { Text("https://contoh.com/artikel") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ColorOnSurfaceMain,
                        unfocusedTextColor = ColorOnSurfaceMain,
                        cursorColor = ColorPrimaryContainerBox,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.3f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.3f),
                        focusedContainerColor = ColorBg,
                        unfocusedContainerColor = ColorBg,
                        focusedBorderColor = ColorPrimaryContainerBox,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Batal", color = ColorOnSurfaceVar) }
                    Button(
                        onClick = { if (link.isNotBlank()) onConfirm(link) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryContainerBox)
                    ) { Text("Proses", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
