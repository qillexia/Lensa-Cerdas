package com.example.kotlinlensacerdasandroid

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.kotlinlensacerdasandroid.network.ApiClient
import com.example.kotlinlensacerdasandroid.network.SummarizeRequest
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummarizeScreen(
    userId: Int,
    userName: String,
    userPhotoUrl: String?,
    onSummarizeSuccess: () -> Unit = {},
    viewModel: SummarizeViewModel = viewModel()
) = with(viewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    var isLoading by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var showCameraChoiceDialog by remember { mutableStateOf(false) }
    var selectedStyle by rememberSaveable { mutableStateOf("standar") }
    var selectedLength by rememberSaveable { mutableStateOf("standar") }

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

    // Camera Launcher for OCR (Menggunakan High-Res)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraUriString != null) {
            val uri = Uri.parse(cameraUriString)
            isLoading = true
            try {
                val image = InputImage.fromFilePath(context, uri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        isLoading = false
                        if (visionText.text.isNotBlank()) {
                            textInput = visionText.text
                            uploadedFileType = "Foto dari Kamera"
                            uploadedFileRealName = null
                            Toast.makeText(context, "Teks berhasil diekstrak!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Tidak ada teks yang terdeteksi di foto", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        Toast.makeText(context, "OCR Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                isLoading = false
                Toast.makeText(context, "Gagal memproses foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Image from Gallery (for OCR)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isLoading = true
            try {
                val image = InputImage.fromFilePath(context, uri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        isLoading = false
                        if (visionText.text.isNotBlank()) {
                            textInput = visionText.text
                            uploadedFileType = "Gambar dari Galeri"
                            uploadedFileRealName = getFileNameFromUri(context, uri)
                            Toast.makeText(context, "Teks gambar berhasil diekstrak!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Tidak ada teks di gambar ini", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        isLoading = false
                        Toast.makeText(context, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    // Document Picker (PDF only)
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val mimeType = context.contentResolver.getType(uri)
                val inputStream = context.contentResolver.openInputStream(uri)

                if (inputStream != null) {
                    // Ekstrak teks dari PDF
                    val readerPdf = PdfReader(inputStream)
                    val numPages = readerPdf.numberOfPages
                    val sb = java.lang.StringBuilder()
                    for (i in 1..numPages) {
                        val text = PdfTextExtractor.getTextFromPage(readerPdf, i)
                        if (text.isNotBlank()) {
                            sb.append(text).append("\n")
                        }
                    }
                    readerPdf.close()
                    textInput = sb.toString()
                    if (textInput.isBlank()) {
                        Toast.makeText(context, "PDF tidak memiliki teks yang bisa dibaca", Toast.LENGTH_LONG).show()
                    } else {
                        uploadedFileType = "Dokumen PDF"
                        uploadedFileRealName = getFileNameFromUri(context, uri)
                        Toast.makeText(context, "PDF berhasil diekstrak!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal membaca file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Speech-to-Text Launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                val spokenText = results[0]
                textInput = if (textInput.isBlank()) spokenText else "$textInput\n$spokenText"
                Toast.makeText(context, "Suara berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
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
                    delay(1000) // Simulate refresh
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
                    .padding(start = 24.dp, top = 110.dp, end = 24.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Buat Ringkasan", color = ColorOnSurfaceMain, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("Gunakan AI untuk meringkas materi Anda.", color = ColorOnSurfaceVar, fontSize = 14.sp)
                }

                // Input Title
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Judul (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ColorOnSurfaceMain,
                        unfocusedTextColor = ColorOnSurfaceMain,
                        cursorColor = ColorPrimaryContainerBox,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        focusedContainerColor = ColorSurfaceContainerLowBox,
                        unfocusedContainerColor = ColorSurfaceContainerLowBox,
                        focusedBorderColor = ColorPrimaryContainerBox,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Input Content
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Tempel teks materi di sini...") },
                        modifier = Modifier.fillMaxWidth().height(200.dp),
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
                        shape = RoundedCornerShape(16.dp)
                    )

                    if (uploadedFileType != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(23.dp))
                                .background(ColorSurfaceContainerHighestBox)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape).background(ColorPrimaryContainerBox),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(androidx.compose.material.icons.Icons.Outlined.Description, contentDescription = null, tint = ColorOnPrimaryContainerText, modifier = Modifier.size(20.dp))
                                }
                                Column {
                                    Text(uploadedFileType!!, color = ColorOnSurfaceMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    if (uploadedFileRealName != null) {
                                        Text(uploadedFileRealName!!, color = ColorOnSurfaceVar, fontSize = 12.sp, maxLines = 1)
                                    }
                                }
                            }
                            IconButton(
                                onClick = { 
                                    uploadedFileType = null
                                    uploadedFileRealName = null
                                    textInput = ""
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(androidx.compose.material.icons.Icons.Filled.Close, contentDescription = "Hapus File", tint = ColorOnSurfaceVar)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                        QuickActionButton(Modifier.weight(1f), Icons.Outlined.CloudUpload, "PDF") { fileLauncher.launch("application/pdf") }
                        QuickActionButton(Modifier.weight(1f), Icons.Outlined.CameraAlt, "Kamera") { showCameraChoiceDialog = true }
                        QuickActionButton(Modifier.weight(1f), Icons.Outlined.Mic, "Suara") {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Bicara sekarang...")
                            }
                            try {
                                speechRecognizerLauncher.launch(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Fitur suara tidak didukung", Toast.LENGTH_SHORT).show()
                            }
                        }
                        QuickActionButton(Modifier.weight(1f), Icons.Outlined.Link, "Link") { showLinkDialog = true }
                    }

                    // Pilihan Gaya dan Panjang (Custom Summary Style)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Gaya Bahasa", color = ColorOnSurfaceMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                            FilterChip(selected = selectedStyle == "standar", onClick = { selectedStyle = "standar" }, label = { Text("Standar") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ColorPrimaryContainerBox, selectedLabelColor = Color.White))
                            FilterChip(selected = selectedStyle == "poin", onClick = { selectedStyle = "poin" }, label = { Text("Poin-poin") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ColorPrimaryContainerBox, selectedLabelColor = Color.White))
                            FilterChip(selected = selectedStyle == "santai", onClick = { selectedStyle = "santai" }, label = { Text("Santai / Gaul") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ColorPrimaryContainerBox, selectedLabelColor = Color.White))
                        }
                        
                        Text("Panjang Ringkasan", color = ColorOnSurfaceMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                            FilterChip(selected = selectedLength == "standar", onClick = { selectedLength = "standar" }, label = { Text("Standar") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ColorPrimaryContainerBox, selectedLabelColor = Color.White))
                            FilterChip(selected = selectedLength == "singkat", onClick = { selectedLength = "singkat" }, label = { Text("Sangat Singkat") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ColorPrimaryContainerBox, selectedLabelColor = Color.White))
                            FilterChip(selected = selectedLength == "detail", onClick = { selectedLength = "detail" }, label = { Text("Detail & Komprehensif") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ColorPrimaryContainerBox, selectedLabelColor = Color.White))
                        }
                    }
                }

                // Submit Button
                Button(
                    onClick = {
                        if (textInput.isBlank()) return@Button
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                val response = ApiClient.instance.createSummary(
                                    SummarizeRequest(userId, titleInput.ifBlank { "Ringkasan" }, textInput, selectedStyle, selectedLength)
                                )
                                if (response.success) {
                                    summaryResult = response.data?.summary ?: ""
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryContainerBox)
                ) {
                    Icon(Icons.Filled.AutoAwesome, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ringkas Sekarang", fontWeight = FontWeight.Bold)
                }

                // AI Thinking Animation
                AnimatedVisibility(
                    visible = isLoading,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ColorSurfaceContainerLowBox),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = ColorPrimaryContainerBox,
                                strokeWidth = 3.dp
                            )
                            Text(
                                "LensaCerdas sedang merangkum...",
                                color = ColorOnSurfaceMain,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // AI Result Card
                AnimatedVisibility(
                    visible = !isLoading && summaryResult.isNotBlank(),
                    enter = fadeIn() + expandVertically(animationSpec = tween(500)),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ColorSurfaceContainerLowBox),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                Arrangement.SpaceBetween,
                                Alignment.CenterVertically
                            ) {
                                Text("✨ Hasil Ringkasan", fontWeight = FontWeight.Bold, color = ColorOnSurfaceMain, fontSize = 18.sp)
                                Row {
                                    IconButton(onClick = {
                                        if (isSpeaking) {
                                            tts?.stop()
                                            isSpeaking = false
                                        } else {
                                            tts?.speak(summaryResult, TextToSpeech.QUEUE_FLUSH, null, null)
                                            isSpeaking = true
                                        }
                                    }) {
                                        Icon(if (isSpeaking) Icons.Outlined.Stop else Icons.Outlined.VolumeUp, contentDescription = "Dengar", tint = ColorOnSurfaceVar, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Summary", summaryResult)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = ColorOnSurfaceVar, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            Text(
                                text = summaryResult,
                                color = ColorOnSurfaceMain,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
            }
        }

        // Top App Bar
        TopNavBar(userName, userPhotoUrl)
    }

    // Dialogs
    if (showLinkDialog) LinkInputDialog(onDismiss = { showLinkDialog = false }, onConfirm = { textInput = it })
    if (showCameraChoiceDialog) {
        CameraChoiceDialog(
            onDismiss = { showCameraChoiceDialog = false },
            onCameraClick = {
                showCameraChoiceDialog = false
                val file = File(context.cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                cameraUriString = uri.toString()
                cameraLauncher.launch(uri)
            },
            onGalleryClick = {
                showCameraChoiceDialog = false
                galleryLauncher.launch("image/*")
            }
        )
    }
}

@Composable
fun QuickActionButton(modifier: Modifier, icon: ImageVector, text: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ColorSurfaceContainerLowBox)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 16.dp)
    ) {
        Icon(icon, null, tint = ColorPrimaryContainerBox)
        Text(text, color = ColorOnSurfaceMain, fontSize = 12.sp)
    }
}

@Composable
fun LinkInputDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
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
                        onClick = { if (link.isNotBlank()) { onConfirm("Konten dari $link"); onDismiss() } },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryContainerBox)
                    ) { Text("Proses", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun CameraChoiceDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceContainerLowBox)
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Pilih Sumber Gambar", fontWeight = FontWeight.Bold, color = ColorOnSurfaceMain, fontSize = 18.sp)
                Text("Ambil foto atau pilih gambar untuk OCR", color = ColorOnSurfaceVar, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorBg)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .clickable { onCameraClick() }
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.CameraAlt, null, tint = ColorSecondaryContainerBox, modifier = Modifier.size(32.dp))
                        Text("Kamera", color = ColorOnSurfaceMain, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorBg)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .clickable { onGalleryClick() }
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.Image, null, tint = ColorPrimaryContainerBox, modifier = Modifier.size(32.dp))
                        Text("Galeri", color = ColorOnSurfaceMain, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = cursor.getString(index)
                }
            }
        } catch (e: Exception) {
            // Abaikan jika error
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}