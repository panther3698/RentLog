package com.example.rentlog.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rent_logs")
data class RentLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tenantName: String,
    val amount: Double,
    val date: Long,
    val isPaid: Boolean = false
)
