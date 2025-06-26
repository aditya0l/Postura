package com.example.postura.uio

import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.postura.viewmodel.PoseViewModel
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen() {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Request permission on first launch
    LaunchedEffect(Unit) {
        Log.d("CameraScreen", "🔐 Requesting camera permission...")
        cameraPermissionState.launchPermissionRequest()
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        Log.d("CameraScreen", "📱 Permission status: ${cameraPermissionState.status}")
        
        if (cameraPermissionState.status.isGranted) {
            Log.d("CameraScreen", "✅ Camera permission granted, showing camera preview")
            val viewModel: PoseViewModel = hiltViewModel()
            CameraPreview(viewModel = viewModel)
        } else {
            Log.d("CameraScreen", "❌ Camera permission not granted, showing rationale")
            PermissionRationale(onRequestPermission = {
                cameraPermissionState.launchPermissionRequest()
            })
        }
    }
}

@Composable
fun PermissionRationale(onRequestPermission: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Camera Permission Required") },
            text = {
                Text("We need access to your camera to analyze your posture during workouts.")
            },
            confirmButton = {
                TextButton(onClick = onRequestPermission) {
                    Text("Grant Permission")
                }
            }
        )
    }
}
