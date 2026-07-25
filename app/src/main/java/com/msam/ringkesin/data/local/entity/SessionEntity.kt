package com.msam.ringkesin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transcript: String = "",
    val summary: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val language: String = "id-ID",
    val aiProvider: String = "",
    val isSummarized: Boolean = false,
)
