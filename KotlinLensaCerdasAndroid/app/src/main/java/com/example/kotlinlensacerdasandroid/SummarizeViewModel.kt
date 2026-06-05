package com.example.kotlinlensacerdasandroid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SummarizeViewModel : ViewModel() {
    var textInput by mutableStateOf("")
    var titleInput by mutableStateOf("")
    var summaryResult by mutableStateOf("")
    var cameraUriString by mutableStateOf<String?>(null)
    var uploadedFileType by mutableStateOf<String?>(null)
    var uploadedFileRealName by mutableStateOf<String?>(null)
}
