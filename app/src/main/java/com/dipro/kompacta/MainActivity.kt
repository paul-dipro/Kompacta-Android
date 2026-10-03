package com.dipro.kompacta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dipro.kompacta.ui.theme.KompactaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KompactaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ImageInfoScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ImageInfoScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var resultText by remember { mutableStateOf("No image selected yet.") }

    // Registers the photo picker. The lambda runs when the user picks an image or cancels.
    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        resultText = if (uri == null) {
            "No image selected."
        } else {
            try {
                describe(readImageInfo(context, uri))
            } catch (e: Exception) {
                "Could not read the image: ${e.message}"
            }
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = {
            pickImage.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }) {
            Text("Choose image")
        }
        Text(text = resultText)
    }
}

private fun describe(info: ImageInfo): String = buildString {
    appendLine("Dimensions: ${info.width} x ${info.height} px")
    appendLine("Type: ${info.mimeType ?: "unknown"}")
    appendLine("File size: ${info.fileSizeBytes?.let { formatBytes(it) } ?: "unknown"}")
    append("Decoded in RAM (ARGB_8888): ${formatBytes(info.estimatedBitmapBytes)}")
}