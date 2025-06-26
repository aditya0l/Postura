package com.example.postura.model

import androidx.compose.ui.geometry.Offset

data class KeyPoint(
    val bodyPart: BodyPart,
    val coordinate: Offset,
    val score: Float
)
