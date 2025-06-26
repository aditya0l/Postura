package com.example.postura.data.pose

import android.content.Context
import android.util.Log
import androidx.compose.ui.geometry.Offset
import com.example.postura.model.BodyPart
import com.example.postura.model.KeyPoint
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class PoseDetector(context: Context) {

    private val interpreter: Interpreter

    init {
        try {
            Log.d("PoseDetector", "🚀 Initializing PoseDetector...")
            val assetFile = context.assets.openFd("movenet_thunder.tflite")
            Log.d("PoseDetector", "✅ Model file found: ${assetFile.declaredLength} bytes")
            
            val fileStream = FileInputStream(assetFile.fileDescriptor)
            val fileChannel = fileStream.channel
            val modelBuffer: MappedByteBuffer = fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                assetFile.startOffset,
                assetFile.declaredLength
            )
            
            val options = Interpreter.Options()
            interpreter = Interpreter(modelBuffer, options)
            Log.d("PoseDetector", "✅ TensorFlow Lite interpreter initialized successfully")
            
        } catch (e: Exception) {
            Log.e("PoseDetector", "❌ Error initializing PoseDetector", e)
            throw e
        }
    }

    fun detectPose(tensorImage: TensorImage): List<KeyPoint> {
        try {
            val output = Array(1) { Array(1) { Array(17) { FloatArray(3) } } } // Shape: [1, 1, 17, 3]

            interpreter.run(tensorImage.buffer, output)

            // Debug: Log raw model output
            Log.d("PoseDetector", "🔍 Raw model output:")
            output[0][0].forEachIndexed { index, kp ->
                Log.d("PoseDetector", "  ${BodyPart.from(index)}: x=${kp[1]}, y=${kp[0]}, score=${kp[2]}")
            }

            // Return normalized coordinates (0-1) - let UI handle scaling
            return output[0][0].mapIndexed { i, kp ->
                KeyPoint(
                    bodyPart = BodyPart.from(i),
                    coordinate = Offset(kp[1], kp[0]), // Normalized coordinates (0-1)
                    score = kp[2]
                )
            }
        } catch (e: Exception) {
            Log.e("PoseDetector", "❌ Error during pose detection", e)
            return emptyList()
        }
    }
}
