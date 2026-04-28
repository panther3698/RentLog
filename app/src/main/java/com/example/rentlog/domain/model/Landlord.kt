package com.devchiradhi.rentlog.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "landlords")
data class Landlord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val tenantName: String = "",
    val tenantAddress: String = "",
    val landlordAddress: String = "",
    val panNumber: String,
    val defaultRentAmount: Double = 0.0
)
