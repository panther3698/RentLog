package com.devchiradhi.rentlog.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object Spacing {
    val xs   = 4.dp
    val sm   = 8.dp
    val sm2  = 12.dp  // between sm and md — replaces "sm + xs" composites
    val md   = 16.dp
    val lg   = 24.dp
    val xl   = 32.dp
    val xxl  = 48.dp
    val xxxl = 64.dp
}

object Radius {
    val sm   = RoundedCornerShape(8.dp)
    val md   = RoundedCornerShape(12.dp)
    val lg   = RoundedCornerShape(16.dp)
    val xl   = RoundedCornerShape(24.dp)
    val xxl  = RoundedCornerShape(32.dp)
    val pill = RoundedCornerShape(50)
}

// Named shadow elevations — use with Modifier.shadow(Elevation.X, shape)
object Elevation {
    val none    = 0.dp
    val low     = 2.dp   // Regular cards, form fields
    val medium  = 8.dp   // Floating buttons, icon buttons
    val high    = 16.dp  // Primary action buttons, prominent cards
    val premium = 24.dp  // Hero cards, summary headers
}
