package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val SyneFontFamily = FontFamily(
    Font(R.font.syne, FontWeight.Bold),
    Font(R.font.syne, FontWeight.ExtraBold)
)

val DmSansFontFamily = FontFamily(
    Font(R.font.dm_sans, FontWeight.Normal),
    Font(R.font.dm_sans, FontWeight.Medium),
    Font(R.font.dm_sans, FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = SyneFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = SyneFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = SyneFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SyneFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = SyneFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SyneFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)
