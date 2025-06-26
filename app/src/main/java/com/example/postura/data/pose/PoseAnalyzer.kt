package com.example.postura.data.pose

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.postura.utils.toBitmap
import com.example.postura.viewmodel.PoseViewModel
import com.example.postura.model.KeyPoint
import com.example.postura.model.BodyPart
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import kotlin.system.measureTimeMillis
import kotlin.math.abs

class PoseAnalyzer(
    private val poseDetector: PoseDetector,
    private val viewModel: PoseViewModel
) : ImageAnalysis.Analyzer {

    private var frameCount = 0
    private var lastLogTime = System.currentTimeMillis()

    override fun analyze(image: ImageProxy) {
        val startTime = System.currentTimeMillis()
        
        try {
            // Convert ImageProxy to Bitmap (with correct rotation)
            val bitmap: Bitmap = image.toBitmap(image.imageInfo.rotationDegrees)

            // Load into TensorImage and process in one step
            val tensorImage = TensorImage(DataType.FLOAT32).apply {
                load(bitmap)
            }

            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(256, 256, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(0f, 255f))
                .build()

            val processedImage = imageProcessor.process(tensorImage)

            // Run inference
            val inferenceTime = measureTimeMillis {
                val keypoints = poseDetector.detectPose(processedImage)
                
                // Analyze posture and get feedback
                val postureFeedback = analyzePosture(keypoints)
                
                // Update ViewModel with both keypoints and feedback
                viewModel.updateKeypoints(keypoints)
                viewModel.updatePostureFeedback(postureFeedback)
                
                // Log detection results
                val confidentKeypoints = keypoints.count { keypoint -> keypoint.score > 0.05f }
                val avgConfidence = if (keypoints.isNotEmpty()) keypoints.map { keypoint -> keypoint.score }.average() else 0.0
                
                frameCount++
                val currentTime = System.currentTimeMillis()
                
                // Log every 30 frames (about once per second at 30fps)
                if (frameCount % 30 == 0) {
                    val fps = 30.0 / ((currentTime - lastLogTime) / 1000.0)
                    Log.d("PoseAnalyzer", "📊 Frame $frameCount - FPS: ${fps.toInt()}, " +
                        "Keypoints: $confidentKeypoints/${keypoints.size}, " +
                        "Avg Confidence: ${(avgConfidence * 100).toInt()}%")
                    
                    // Log posture feedback
                    Log.d("PoseAnalyzer", "💡 Posture Feedback: ${postureFeedback.message}")
                    
                    lastLogTime = currentTime
                }
                
                // Log high-confidence detections (lowered threshold for debugging)
                if (confidentKeypoints >= 10) {
                    Log.d("PoseAnalyzer", "✅ Good detection: $confidentKeypoints confident keypoints")
                } else if (confidentKeypoints >= 5) {
                    Log.d("PoseAnalyzer", "⚠️ Partial detection: $confidentKeypoints confident keypoints")
                } else if (keypoints.isNotEmpty()) {
                    val maxScore = keypoints.maxOf { keypoint -> keypoint.score }
                    Log.d("PoseAnalyzer", "❌ Low confidence: max score = ${(maxScore * 100).toInt()}%")
                } else {
                    Log.d("PoseAnalyzer", "❌ No keypoints detected")
                }
            }
            
            val totalTime = System.currentTimeMillis() - startTime
            if (frameCount % 30 == 0) {
                Log.d("PoseAnalyzer", "⏱️ Processing time: ${totalTime}ms (inference: ${inferenceTime}ms)")
            }

        } catch (e: OutOfMemoryError) {
            Log.e("PoseAnalyzer", "❌ Out of memory error", e)
            // Could trigger garbage collection or reduce processing frequency
        } catch (e: IllegalArgumentException) {
            Log.e("PoseAnalyzer", "❌ Invalid image format", e)
        } catch (e: Exception) {
            Log.e("PoseAnalyzer", "❌ Error analyzing image", e)
        } finally {
            image.close()
        }
    }

    private fun analyzePosture(keypoints: List<KeyPoint>): PostureFeedback {
        if (keypoints.size < 17) {
            return PostureFeedback(
                quality = PostureQuality.UNKNOWN,
                message = "Not enough keypoints detected",
                corrections = listOf("Move closer to camera", "Improve lighting")
            )
        }

        val confidentKeypoints = keypoints.filter { keypoint -> keypoint.score > 0.05f }
        if (confidentKeypoints.size < 5) {
            return PostureFeedback(
                quality = PostureQuality.POOR,
                message = "Poor detection quality",
                corrections = listOf("Stand closer to camera", "Face camera directly", "Improve lighting")
            )
        }

        // Analyze shoulder alignment
        val leftShoulder = keypoints.find { keypoint -> keypoint.bodyPart == BodyPart.LEFT_SHOULDER }
        val rightShoulder = keypoints.find { keypoint -> keypoint.bodyPart == BodyPart.RIGHT_SHOULDER }
        
        val shoulderAlignment = analyzeShoulderAlignment(leftShoulder, rightShoulder)
        
        // Analyze head position
        val nose = keypoints.find { keypoint -> keypoint.bodyPart == BodyPart.NOSE }
        val headPosition = analyzeHeadPosition(nose, leftShoulder, rightShoulder)
        
        // Analyze overall posture
        val overallQuality = when {
            shoulderAlignment == PostureQuality.GOOD && headPosition == PostureQuality.GOOD -> PostureQuality.GOOD
            shoulderAlignment == PostureQuality.POOR || headPosition == PostureQuality.POOR -> PostureQuality.POOR
            else -> PostureQuality.FAIR
        }

        val corrections = mutableListOf<String>()
        if (shoulderAlignment == PostureQuality.POOR) {
            corrections.add("Level your shoulders")
        }
        if (headPosition == PostureQuality.POOR) {
            corrections.add("Keep your head straight")
        }
        if (corrections.isEmpty()) {
            corrections.add("Great posture! Keep it up!")
        }

        return PostureFeedback(
            quality = overallQuality,
            message = when (overallQuality) {
                PostureQuality.GOOD -> "Excellent posture!"
                PostureQuality.FAIR -> "Good posture with room for improvement"
                PostureQuality.POOR -> "Posture needs attention"
                PostureQuality.UNKNOWN -> "Unable to analyze posture"
            },
            corrections = corrections
        )
    }

    private fun analyzeShoulderAlignment(leftShoulder: KeyPoint?, rightShoulder: KeyPoint?): PostureQuality {
        if (leftShoulder == null || rightShoulder == null || 
            leftShoulder.score < 0.05f || rightShoulder.score < 0.05f) {
            return PostureQuality.UNKNOWN
        }

        val shoulderHeightDiff = kotlin.math.abs(leftShoulder.coordinate.y - rightShoulder.coordinate.y)
        return when {
            shoulderHeightDiff < 0.05f -> PostureQuality.GOOD
            shoulderHeightDiff < 0.1f -> PostureQuality.FAIR
            else -> PostureQuality.POOR
        }
    }

    private fun analyzeHeadPosition(nose: KeyPoint?, leftShoulder: KeyPoint?, rightShoulder: KeyPoint?): PostureQuality {
        if (nose == null || leftShoulder == null || rightShoulder == null ||
            nose.score < 0.05f || leftShoulder.score < 0.05f || rightShoulder.score < 0.05f) {
            return PostureQuality.UNKNOWN
        }

        val shoulderCenterX = (leftShoulder.coordinate.x + rightShoulder.coordinate.x) / 2.0f
        val headOffset = kotlin.math.abs(nose.coordinate.x - shoulderCenterX)
        
        return when {
            headOffset < 0.05f -> PostureQuality.GOOD
            headOffset < 0.1f -> PostureQuality.FAIR
            else -> PostureQuality.POOR
        }
    }
}

enum class PostureQuality {
    GOOD, FAIR, POOR, UNKNOWN
}

data class PostureFeedback(
    val quality: PostureQuality,
    val message: String,
    val corrections: List<String>
)
