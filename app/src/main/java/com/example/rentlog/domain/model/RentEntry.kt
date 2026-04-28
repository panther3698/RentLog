package com.devchiradhi.rentlog.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rent_entries",
    foreignKeys = [
        ForeignKey(
            entity = Landlord::class,
            parentColumns = ["id"],
            childColumns = ["landlordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["landlordId"])]
)
data class RentEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val month: Int, // 1-12
    val year: Int,
    val amount: Double,
    val paymentDate: Long,
    val transactionId: String,
    val landlordId: Int,
    val attachmentUri: String = ""
)
