package com.example.postura.uio

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.postura.data.pose.PoseAnalyzer
import com.example.postura.data.pose.PoseDetector
import com.example.postura.model.KeyPoint
import com.example.postura.pose.PoseOverlay
import com.example.postura.viewmodel.PoseViewModel
import java.util.concurrent.Executors

@Composable
fun CameraPreview(viewModel: PoseViewModel) {
    val context = LocalContext.current
    val keypoints by viewModel.keypoints.collectAsState()
    val postureFeedback by viewModel.postureFeedback.collectAsState()
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var cameraStarted by remember { mutableStateOf(false) }
    var debugMode by remember { mutableStateOf(true) } // Enable debug mode by default

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewView = this
                }
            }
        )

        // 🔴 Overlay on top of camera preview
        PoseOverlay(
            modifier = Modifier.matchParentSize(),
            keypoints = keypoints,
            drawLines = true,
            postureFeedback = postureFeedback
        )

        // Debug info overlay
        if (debugMode) {
            DebugInfoOverlay(
                keypoints = keypoints,
                onToggleDebug = { debugMode = !debugMode }
            )
        }

        // Tips overlay for better detection
        DetectionTipsOverlay()

        Log.d("PoseOverlay", "🔍 Drawing ${keypoints.size} keypoints")
    }

    // Start camera when previewView becomes available
    LaunchedEffect(previewView) {
        if (previewView != null && !cameraStarted) {
            Log.d("CameraPreview", "🎯 PreviewView available, starting camera...")
            cameraStarted = true
        }
    }

    // Proper lifecycle management for camera executor and camera setup
    DisposableEffect(cameraStarted) {
        val cameraExecutor = Executors.newSingleThreadExecutor()
        
        // Start camera when ready
        if (cameraStarted && previewView != null) {
            startCamera(previewView!!, context, viewModel, cameraExecutor)
        }
        
        onDispose {
            Log.d("CameraPreview", "🧹 Cleaning up camera resources")
            cameraExecutor.shutdown()
        }
    }
}

@Composable
fun DebugInfoOverlay(
    keypoints: List<KeyPoint>,
    onToggleDebug: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Debug info in bottom-right corner
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = "🔧 Debug Mode",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Total Keypoints: ${keypoints.size}",
                color = Color.White,
                fontSize = 12.sp
            )
            Text(
                text = "Confident (>0.5): ${keypoints.count { keypoint -> keypoint.score > 0.5f }}",
                color = Color.White,
                fontSize = 12.sp
            )
            Text(
                text = "High (>0.7): ${keypoints.count { keypoint -> keypoint.score > 0.7f }}",
                color = Color.White,
                fontSize = 12.sp
            )
            if (keypoints.isNotEmpty()) {
                val avgConfidence = keypoints.map { keypoint -> keypoint.score }.average()
                Text(
                    text = "Avg: ${(avgConfidence * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            androidx.compose.material3.Button(
                onClick = onToggleDebug,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Hide Debug")
            }
        }
    }
}

@Composable
fun DetectionTipsOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = "💡 Detection Tips",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "• Stand 2-3 feet away",
                color = Color.White,
                fontSize = 11.sp
            )
            Text(
                text = "• Face the camera directly",
                color = Color.White,
                fontSize = 11.sp
            )
            Text(
                text = "• Ensure good lighting",
                color = Color.White,
                fontSize = 11.sp
            )
            Text(
                text = "• Clear background",
                color = Color.White,
                fontSize = 11.sp
            )
        }
    }
}

private fun startCamera(
    previewView: PreviewView,
    context: Context,
    viewModel: PoseViewModel,
    executor: java.util.concurrent.Executor
) {
    Log.d("CameraPreview", "🚀 Starting camera initialization...")
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
        try {
            val cameraProvider = cameraProviderFuture.get()
            Log.d("CameraPreview", "✅ Camera provider obtained")

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            Log.d("CameraPreview", "✅ Preview configured")

            val poseDetector = PoseDetector(context)
            Log.d("CameraPreview", "✅ Pose detector initialized")

            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(
                        executor,
                        PoseAnalyzer(poseDetector, viewModel)
                    )
                }
            Log.d("CameraPreview", "✅ Image analyzer configured")

            // Try front camera first, fallback to back camera
            val cameraSelector = try {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } catch (e: Exception) {
                Log.w("CameraPreview", "Front camera not available, using back camera", e)
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            Log.d("CameraPreview", "✅ Camera selector configured")

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    context as androidx.lifecycle.LifecycleOwner,
                    cameraSelector,
                    preview,
                    analyzer
                )
                Log.d("CameraPreview", "✅ Camera started successfully")
            } catch (e: Exception) {
                Log.e("CameraPreview", "❌ Error binding camera to lifecycle", e)
                e.printStackTrace()
            }

        } catch (e: Exception) {
            Log.e("CameraPreview", "❌ Error in camera setup", e)
            e.printStackTrace()
        }

    }, ContextCompat.getMainExecutor(context))
}
