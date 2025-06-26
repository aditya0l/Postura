package com.example.postura.viewmodel

import androidx.lifecycle.ViewModel
import com.example.postura.model.KeyPoint
import com.example.postura.data.pose.PostureFeedback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class PoseDetectionState {
    object Loading : PoseDetectionState()
    data class Success(val keypoints: List<KeyPoint>) : PoseDetectionState()
    data class Error(val message: String) : PoseDetectionState()
}

class PoseViewModel : ViewModel() {
    private val _state = MutableStateFlow<PoseDetectionState>(PoseDetectionState.Loading)
    val state: StateFlow<PoseDetectionState> = _state

    // Backward compatibility
    val keypoints: StateFlow<List<KeyPoint>> = MutableStateFlow(emptyList())
    
    // Posture feedback
    private val _postureFeedback = MutableStateFlow<PostureFeedback?>(null)
    val postureFeedback: StateFlow<PostureFeedback?> = _postureFeedback

    fun updateKeypoints(newPoints: List<KeyPoint>) {
        _state.value = PoseDetectionState.Success(newPoints)
        (keypoints as MutableStateFlow).value = newPoints
    }

    fun updatePostureFeedback(feedback: PostureFeedback) {
        _postureFeedback.value = feedback
    }

    fun setError(message: String) {
        _state.value = PoseDetectionState.Error(message)
    }

    fun setLoading() {
        _state.value = PoseDetectionState.Loading
    }
}
