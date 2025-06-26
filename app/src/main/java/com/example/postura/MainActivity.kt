package com.example.postura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.postura.ui.theme.PosturaTheme
import com.example.postura.uio.CameraScreen // ✅ This uses permission-aware composable
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PosturaTheme {
                CameraScreen() // ✅ Handles permissions and launches CameraPreview
            }
        }
    }
}
