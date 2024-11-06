package com.example.englishtrain

data class Word(
    val id: Int,
    val english: String,
    val russian: String,
    var isLearned: Boolean,
    val difficulty: Int
)
