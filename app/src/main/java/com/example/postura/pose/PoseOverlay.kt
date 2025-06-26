package com.example.postura.pose

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.postura.model.BodyPart
import com.example.postura.model.KeyPoint
import com.example.postura.data.pose.PostureFeedback
import com.example.postura.data.pose.PostureQuality

@Composable
fun PoseOverlay(
    modifier: Modifier,
    keypoints: List<KeyPoint>,
    drawLines: Boolean,
    postureFeedback: PostureFeedback? = null
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            Log.d("CanvasSize", "Canvas size: width=$canvasWidth, height=$canvasHeight")
            Log.d("PoseOverlay", "🔍 Drawing ${keypoints.size} keypoints")

            // Draw keypoints with confidence threshold
            keypoints.forEach { keypoint ->
                // Show all keypoints but color by confidence
                val scaledX = keypoint.coordinate.x * canvasWidth
                val scaledY = keypoint.coordinate.y * canvasHeight

                // Color based on confidence (adjusted for low-confidence scenario)
                val color = when {
                    keypoint.score > 0.05f -> Color.Green  // >5%
                    keypoint.score > 0.02f -> Color.Yellow // >2%
                    else -> Color.Red
                }

                drawCircle(
                    color = color,
                    radius = 8f,
                    center = Offset(scaledX, scaledY)
                )
            }

            // Draw joint lines (only for confident keypoints)
            if (drawLines) {
                drawLineBetween(keypoints, BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER, canvasWidth, canvasHeight)
                drawLineBetween(keypoints, BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP, canvasWidth, canvasHeight)

                drawLineBetween(keypoints, BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW, canvasWidth, canvasHeight)
                drawLineBetween(keypoints, BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST, canvasWidth, canvasHeight)
                drawLineBetween(keypoints, BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW, canvasWidth, canvasHeight)
                drawLineBetween(keypoints, BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST, canvasWidth, canvasHeight)

                drawLineBetween(keypoints, BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE, canvasWidth, canvasHeight)
                drawLineBetween(keypoints, BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE, canvasWidth, canvasHeight)
                drawLineBetween(keypoints, BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE, canvasWidth, canvasHeight)
                drawLineBetween(keypoints, BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE, canvasWidth, canvasHeight)
            }
        }

        // Status overlay in top-left corner
        DetectionStatusOverlay(keypoints = keypoints)
        
        // Posture feedback overlay in bottom-left corner
        postureFeedback?.let { feedback ->
            PostureFeedbackOverlay(feedback = feedback)
        }
    }
}

@Composable
fun DetectionStatusOverlay(keypoints: List<KeyPoint>) {
    val confidentKeypoints = keypoints.count { keypoint -> keypoint.score > 0.05f } // >5%
    val totalKeypoints = keypoints.size
    val detectionStatus = when {
        confidentKeypoints >= 10 -> "✅ Good Detection"
        confidentKeypoints >= 5 -> "⚠️ Partial Detection"
        confidentKeypoints > 0 -> "❌ Poor Detection"
        else -> "❌ No Detection"
    }
    
    val statusColor = when {
        confidentKeypoints >= 10 -> Color.Green
        confidentKeypoints >= 5 -> Color.Yellow
        else -> Color.Red
    }

    Box(
        modifier = Modifier
            .padding(16.dp)
            .background(
                color = Color.Black.copy(alpha = 0.7f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = detectionStatus,
                color = statusColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Keypoints: $confidentKeypoints/$totalKeypoints",
                color = Color.White,
                fontSize = 14.sp
            )
            if (keypoints.isNotEmpty()) {
                val avgConfidence = keypoints.map { keypoint -> keypoint.score }.average()
                val maxConfidence = keypoints.maxOf { keypoint -> keypoint.score }
                Text(
                    text = "Avg: ${(avgConfidence * 100).toInt()}% | Max: ${(maxConfidence * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun PostureFeedbackOverlay(feedback: PostureFeedback) {
    val backgroundColor = when (feedback.quality) {
        PostureQuality.GOOD -> Color.Green.copy(alpha = 0.8f)
        PostureQuality.FAIR -> Color.Yellow.copy(alpha = 0.8f)
        PostureQuality.POOR -> Color.Red.copy(alpha = 0.8f)
        PostureQuality.UNKNOWN -> Color.Gray.copy(alpha = 0.8f)
    }
    
    val textColor = Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(
                    color = backgroundColor,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = "💡 Posture Analysis",
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = feedback.message,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Suggestions:",
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            feedback.corrections.forEach { correction ->
                Text(
                    text = "• $correction",
                    color = textColor,
                    fontSize = 10.sp
                )
            }
        }
    }
}

private fun DrawScope.drawLineBetween(
    keypoints: List<KeyPoint>,
    partA: BodyPart,
    partB: BodyPart,
    canvasWidth: Float,
    canvasHeight: Float
) {
    val kpA = keypoints.find { it.bodyPart == partA }
    val kpB = keypoints.find { it.bodyPart == partB }

    // Only draw lines if both keypoints have sufficient confidence
    if (kpA != null && kpB != null && kpA.score > 0.3f && kpB.score > 0.3f) {
        drawLine(
            color = Color.Green,
            start = Offset(kpA.coordinate.x * canvasWidth, kpA.coordinate.y * canvasHeight),
            end = Offset(kpB.coordinate.x * canvasWidth, kpB.coordinate.y * canvasHeight),
            strokeWidth = 4f
        )
    }
}
