package iss.nus.edu.sg.weather.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography = Typography(
    titleLarge = TextStyle(       // City name
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,
        fontSize = 34.sp, lineHeight = 40.sp
    ),
    displayLarge = TextStyle(     // Temperature
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Thin,
        fontSize = 88.sp, lineHeight = 92.sp, letterSpacing = (-2).sp
    ),
    headlineMedium = TextStyle(   // Weather description
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,
        fontSize = 28.sp, lineHeight = 34.sp
    ),
    bodyLarge = TextStyle(        // Info row labels + values
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,
        fontSize = 24.sp, lineHeight = 30.sp
    ),
    bodyMedium = TextStyle(       // Forecast day label + temps
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,
        fontSize = 22.sp, lineHeight = 28.sp
    ),
    bodySmall = TextStyle(        // Small helper text
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 22.sp
    ),
    labelLarge = TextStyle(       // Buttons
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,
        fontSize = 22.sp, lineHeight = 28.sp
    )
)
