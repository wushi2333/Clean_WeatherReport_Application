package iss.nus.edu.sg.weather.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color

/**
 * Apple-style frosted glass effect.
 * Semi-transparent white background that creates a blur-like overlay appearance
 * when placed on top of gradient backgrounds.
 */
fun Modifier.frostedGlass(): Modifier = this.drawBehind {
    drawRect(color = Color.White.copy(alpha = 0.18f))
}
