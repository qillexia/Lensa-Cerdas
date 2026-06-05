package com.example.kotlinlensacerdasandroid

import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.google.gson.Gson
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.kotlinlensacerdasandroid.network.ApiClient
import com.example.kotlinlensacerdasandroid.network.HistoryItem
import com.example.kotlinlensacerdasandroid.network.UpdateRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Stop
import android.speech.tts.TextToSpeech

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    userId: Int,
    userName: String,
    userPhotoUrl: String?,
    onHomeClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var historyItems by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedHistoryItem by remember { mutableStateOf<HistoryItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val pullToRefreshState = rememberPullToRefreshState()

    val sharedPrefs = remember { context.getSharedPreferences("HistoryCache", Context.MODE_PRIVATE) }
    val gson = remember { Gson() }

    val loadHistory = suspend {
        try {
            val response = ApiClient.instance.getHistory(userId)
            if (response.success) {
                historyItems = response.data ?: emptyList()
                sharedPrefs.edit().putString("history_$userId", gson.toJson(historyItems)).apply()
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            // Abaikan — terjadi saat user pindah screen
        } catch (e: Exception) {
            if (historyItems.isEmpty()) {
                Toast.makeText(context, "Gagal memuat: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.setLanguage(Locale("id", "ID"))
            }
        }
        tts = textToSpeech

        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    LaunchedEffect(userId) {
        val cachedJson = sharedPrefs.getString("history_$userId", null)
        if (cachedJson != null) {
            try {
                // Menghindari penggunaan TypeToken yang sering crash karena R8/ProGuard Release
                val cachedArray: Array<HistoryItem> = gson.fromJson(cachedJson, Array<HistoryItem>::class.java)
                historyItems = cachedArray.toList()
                if (historyItems.isNotEmpty()) {
                    isLoading = false
                }
            } catch (e: Exception) {
                // Ignore cache error
            }
        } else {
            isLoading = true
        }

        loadHistory()
        isLoading = false
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
                    loadHistory()
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
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Title Header
                Text(
                    text = "Riwayat Anda",
                    color = ColorOnSurfaceMain,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari ringkasan...", color = ColorOnSurfaceVar) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search", tint = ColorOnSurfaceVar) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ColorOnSurfaceMain,
                        unfocusedTextColor = ColorOnSurfaceMain,
                        cursorColor = ColorPrimaryContainerBox,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                        focusedContainerColor = ColorSurfaceContainerLowBox,
                        unfocusedContainerColor = ColorSurfaceContainerLowBox,
                        focusedBorderColor = ColorPrimaryContainerBox,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(100.dp),
                    singleLine = true
                )

                val filteredItems by remember(historyItems, searchQuery) {
                    derivedStateOf {
                        if (searchQuery.isBlank()) historyItems
                        else historyItems.filter {
                            it.title.contains(searchQuery, ignoreCase = true) || it.summary_text.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }

                if (isLoading && !isRefreshing) {
                    CircularProgressIndicator(color = ColorPrimaryContainerBox, modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (filteredItems.isEmpty()) {
                    Text(
                        text = if (searchQuery.isBlank()) "Belum ada riwayat ringkasan." else "Pencarian tidak ditemukan.",
                        color = ColorOnSurfaceVar,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        filteredItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(ColorSurfaceContainerLowBox)
                                    .clickable { selectedHistoryItem = item }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(ColorSurfaceContainerHighestBox),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.Description, contentDescription = null, tint = ColorPrimaryText)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        color = ColorOnSurfaceMain,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = formatDateTime(item.created_at),
                                        color = ColorOnSurfaceVar,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top App Bar
        TopNavBar(userName, userPhotoUrl)
    }

    selectedHistoryItem?.let { item ->
        var isEditing by remember { mutableStateOf(false) }
        var editableSummary by remember { mutableStateOf(item.summary_text) }
        var editableTitle by remember { mutableStateOf(item.title) }
        var isSaving by remember { mutableStateOf(false) }
        var isDeleting by remember { mutableStateOf(false) }

        Dialog(
            onDismissRequest = { if (!isSaving) selectedHistoryItem = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp), 
                colors = CardDefaults.cardColors(containerColor = ColorSurfaceContainerLowBox),
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                Column(Modifier.padding(30.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text(if (isEditing) "Edit Ringkasan" else "Hasil AI", fontWeight = FontWeight.Bold, color = ColorOnSurfaceMain)
                        Row {
                            if (!isEditing) {
                                IconButton(onClick = {
                                    if (isSpeaking) {
                                        tts?.stop()
                                        isSpeaking = false
                                    } else {
                                        tts?.speak(editableSummary, TextToSpeech.QUEUE_FLUSH, null, null)
                                        isSpeaking = true
                                    }
                                }) {
                                    Icon(if (isSpeaking) Icons.Outlined.Stop else Icons.Outlined.VolumeUp, null, tint = ColorOnSurfaceVar, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { isEditing = true }) {
                                    Icon(Icons.Outlined.Edit, null, tint = ColorOnSurfaceVar, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Summary", editableSummary)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Disalin!", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Outlined.ContentCopy, null, tint = ColorOnSurfaceVar, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = {
                                    isDeleting = true
                                    coroutineScope.launch {
                                        try {
                                            val response = ApiClient.instance.deleteSummary(item.id)
                                            if (response.success) {
                                                Toast.makeText(context, "Berhasil dihapus!", Toast.LENGTH_SHORT).show()
                                                selectedHistoryItem = null
                                                loadHistory()
                                            } else {
                                                Toast.makeText(context, "Gagal menghapus", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            isDeleting = false
                                        }
                                    }
                                }) {
                                    if (isDeleting) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFFFF5555), strokeWidth = 2.dp)
                                    else Icon(Icons.Outlined.Delete, null, tint = Color(0xFFFF5555), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    
                    if (isEditing) {
                        OutlinedTextField(
                            value = editableTitle,
                            onValueChange = { editableTitle = it },
                            label = { Text("Judul") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ColorOnSurfaceMain,
                                unfocusedTextColor = ColorOnSurfaceMain,
                                cursorColor = ColorPrimaryContainerBox,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                                focusedContainerColor = ColorSurfaceContainerHighestBox,
                                unfocusedContainerColor = ColorSurfaceContainerHighestBox,
                                focusedBorderColor = ColorPrimaryContainerBox,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                        OutlinedTextField(
                            value = editableSummary,
                            onValueChange = { editableSummary = it },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 400.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ColorOnSurfaceMain,
                                unfocusedTextColor = ColorOnSurfaceMain,
                                cursorColor = ColorPrimaryContainerBox,
                                focusedContainerColor = ColorSurfaceContainerHighestBox,
                                unfocusedContainerColor = ColorSurfaceContainerHighestBox,
                                focusedBorderColor = ColorPrimaryContainerBox,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    } else {
                        Text(editableSummary, color = ColorOnSurfaceMain)
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (isEditing) {
                            Button(
                                onClick = { 
                                    isEditing = false
                                    editableSummary = item.summary_text
                                    editableTitle = item.title 
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorSurfaceContainerHighestBox)
                            ) { Text("Batal", color = ColorOnSurfaceMain) }
                            
                            Button(
                                onClick = {
                                    isSaving = true
                                    coroutineScope.launch {
                                        try {
                                            val response = ApiClient.instance.updateSummary(item.id, UpdateRequest(editableTitle, editableSummary))
                                            if (response.success) {
                                                Toast.makeText(context, "Berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                                isEditing = false
                                                selectedHistoryItem = null
                                                loadHistory()
                                            } else {
                                                Toast.makeText(context, "Gagal menyimpan", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: kotlinx.coroutines.CancellationException) {
                                            // Abaikan
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            isSaving = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryContainerBox)
                            ) { 
                                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                else Text("Simpan", fontWeight = FontWeight.Bold) 
                            }
                        } else {
                            Button(
                                onClick = { 
                                    selectedHistoryItem = null 
                                    if (isSpeaking) {
                                        tts?.stop()
                                        isSpeaking = false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorSurfaceContainerHighestBox)
                            ) { 
                                Text("Tutup", color = ColorOnSurfaceMain, fontWeight = FontWeight.Bold) 
                            }
                            
                            Button(
                                onClick = { 
                                    exportToPdfAndShare(context, item.title, item.summary_text, formatDateTime(item.created_at))
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryContainerBox)
                            ) { 
                                Icon(Icons.Outlined.PictureAsPdf, contentDescription = "Export PDF", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Export PDF", fontWeight = FontWeight.Bold) 
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatDateTime(dateString: String): String {
    // Output format: "Sabtu, 17 Mei 2026 22:16"
    val outputFormat = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale("id", "ID"))
    outputFormat.timeZone = TimeZone.getDefault()

    // Bersihkan microseconds dari Supabase (6 digit → 3 digit)
    val cleaned = dateString
        .replace(Regex("\\.(\\d{3})\\d*"), ".$1")  // potong ke 3 digit milidetik
        .replace("+00:00", "+0000")                  // fix timezone format
        .replace("Z", "+0000")

    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",   // Supabase: 2025-05-17T15:16:59.501+0000
        "yyyy-MM-dd'T'HH:mm:ssZ",        // ISO tanpa milidetik
        "yyyy-MM-dd HH:mm:ss"            // MySQL standar
    )

    for (fmt in formats) {
        try {
            val inputFormat = SimpleDateFormat(fmt, Locale.getDefault())
            if (fmt.contains("Z")) inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(cleaned)
            if (date != null) return outputFormat.format(date)
        } catch (_: Exception) { }
    }

    return dateString
}

fun exportToPdfAndShare(context: Context, title: String, summary: String, dateStr: String) {
    try {
        val pdfsDir = File(context.cacheDir, "pdfs")
        if (!pdfsDir.exists()) pdfsDir.mkdirs()

        val pdfFile = File(pdfsDir, "Ringkasan_LensaCerdas_${System.currentTimeMillis()}.pdf")
        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(pdfFile))
        document.open()

        // Fonts
        val titleFont = com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 20f, com.itextpdf.text.Font.BOLD)
        val dateFont = com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11f, com.itextpdf.text.Font.ITALIC, com.itextpdf.text.BaseColor.GRAY)
        val bodyFont = com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12f, com.itextpdf.text.Font.NORMAL)
        val headerFont = com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10f, com.itextpdf.text.Font.NORMAL, com.itextpdf.text.BaseColor.GRAY)

        // Header
        val header = Paragraph("LensaCerdas \u2022 AI Summary Report", headerFont)
        header.alignment = com.itextpdf.text.Element.ALIGN_CENTER
        document.add(header)
        document.add(Paragraph("\n"))

        // Title
        document.add(Paragraph(title, titleFont))
        document.add(Paragraph(dateStr, dateFont))
        document.add(Paragraph("\n"))

        // Divider line
        val line = com.itextpdf.text.pdf.draw.LineSeparator()
        line.lineColor = com.itextpdf.text.BaseColor.LIGHT_GRAY
        document.add(com.itextpdf.text.Chunk(line))
        document.add(Paragraph("\n"))

        // Body
        val bodyParagraph = Paragraph(summary, bodyFont)
        bodyParagraph.leading = 18f
        document.add(bodyParagraph)

        document.close()

        // Share via Intent
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Ringkasan LensaCerdas: $title")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Bagikan PDF melalui"))

    } catch (e: Exception) {
        Toast.makeText(context, "Gagal export PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
