package com.utp.finalproject.data

data class PetTask(
    val id: Long = 0,
    val petName: String,
    val taskType: String,
    val dueDate: String,
    val priority: String,
    val notes: String,
    val isCompleted: Boolean = false
)
