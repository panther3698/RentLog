package com.devchiradhi.rentlog.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.devchiradhi.rentlog.domain.model.Landlord
import com.devchiradhi.rentlog.domain.model.RentEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun rentEntries_areReturnedForMatchingFiscalYearAndLandlord() = runBlocking {
        val landlordId = database.landlordDao().insertOrUpdateLandlord(
            Landlord(
                name = "Asha Patel",
                tenantName = "Ravi Kumar",
                tenantAddress = "Flat 12, Bengaluru",
                landlordAddress = "MG Road, Bengaluru",
                panNumber = "ABCDE1234F",
                defaultRentAmount = 22000.0
            )
        ).toInt()

        database.rentEntryDao().insertOrUpdateRentEntry(
            RentEntry(
                month = 4,
                year = 2025,
                amount = 22000.0,
                paymentDate = 1_744_368_000_000,
                transactionId = "UPI-APR",
                landlordId = landlordId
            )
        )
        database.rentEntryDao().insertOrUpdateRentEntry(
            RentEntry(
                month = 5,
                year = 2025,
                amount = 22000.0,
                paymentDate = 1_746_960_000_000,
                transactionId = "UPI-MAY",
                landlordId = landlordId
            )
        )

        val entries = database.rentEntryDao()
            .getEntriesForYearAndLandlord(2025, landlordId)
            .first()

        assertEquals(listOf(4, 5), entries.map { it.month })
        assertEquals(44000.0, entries.sumOf { it.amount }, 0.0)
    }
}
