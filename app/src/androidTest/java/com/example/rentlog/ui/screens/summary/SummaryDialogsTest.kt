package com.devchiradhi.rentlog.ui.screens.summary

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SummaryDialogsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun monthPicker_returnsSelectedMonth() {
        var selectedMonth = -1

        composeRule.setContent {
            MaterialTheme {
                MonthPickerDialog(
                    onDismiss = {},
                    onMonthSelected = { selectedMonth = it }
                )
            }
        }

        composeRule.onNodeWithText("April").performClick()
        composeRule.runOnIdle {
            assertEquals(4, selectedMonth)
        }
    }
}
