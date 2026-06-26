package iss.nus.edu.sg.weather.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/**
 * Apple Weather-style dynamic background.
 * Icon codes based on QWeather standard:
 *   100       = sunny
 *   101-103   = partly cloudy
 *   104       = overcast
 *   150-153   = night clear/partly
 *   300-318   = rain
 *   400-407   = snow
 *   500-515   = fog/haze
 */
@Composable
fun WeatherBackground(
    iconCode: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val code = iconCode.replace("n", "").replace("d", "").toIntOrNull() ?: 100
    val isNight = iconCode.endsWith("n")

    val (top, bottom) = when {
        // Sunny / Clear
        code == 100 -> if (isNight) Color(0xFF0B0B2B) to Color(0xFF1B1B4B)
            else Color(0xFF2B7FF0) to Color(0xFF7EC8FF)
        // Partly cloudy
        code in 101..103 || code == 150 || code in 151..153 -> if (isNight) Color(0xFF12123A) to Color(0xFF2A2A5E)
            else Color(0xFF5599E0) to Color(0xFF9BC8F0)
        // Overcast
        code == 104 -> Color(0xFF4A4A5E) to Color(0xFF8A8A9E)
        // Rain
        code in 300..318 -> Color(0xFF2C3A4F) to Color(0xFF5B6F8A)
        // Snow
        code in 400..407 -> Color(0xFF7B8FA0) to Color(0xFFBCCFDF)
        // Fog / Haze
        code in 500..515 -> Color(0xFF8B8B96) to Color(0xFFB0B0B8)
        else -> if (isNight) Color(0xFF0B0B2B) to Color(0xFF1B1B4B)
            else Color(0xFF2B7FF0) to Color(0xFF7EC8FF)
    }

    // Only rain codes → rain animation; snow codes → snow animation
    val showRain = code in 300..318
    val showSnow = code in 400..407

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(top, bottom)))
    ) {
        if (showRain) RainAnimation()
        if (showSnow) SnowAnimation()
        content()
    }
}

// ─── Rain ─────────────────────────────────────────────────────

private data class RainDrop(val x: Float, val startY: Float, val length: Float, val duration: Int)

@Composable
private fun RainAnimation() {
    val tx = rememberInfiniteTransition(label = "rain")
    val drops = remember { List(30) { RainDrop(Random.nextFloat(), -0.3f * Random.nextFloat(), 0.01f + Random.nextFloat() * 0.025f, 500 + Random.nextInt(600)) } }
    drops.forEachIndexed { i, d ->
        val t by tx.animateFloat(0f, 1f, infiniteRepeatable(tween(d.duration, easing = LinearEasing), RepeatMode.Restart), "r$i")
        val a by tx.animateFloat(0.55f, 0.1f, infiniteRepeatable(tween(d.duration / 2), RepeatMode.Reverse), "ra$i")
        Canvas(Modifier.fillMaxSize()) {
            val sy = (d.startY + t * 1.4f) * size.height
            drawLine(Color.White.copy(alpha = a), Offset(d.x * size.width, sy), Offset(d.x * size.width + 1.5f, sy + d.length * size.height), 1.5f)
        }
    }
}

// ─── Snow ─────────────────────────────────────────────────────

private data class SnowFlake(val x: Float, val r: Float, val o: Float, val d: Int)

@Composable
private fun SnowAnimation() {
    val tx = rememberInfiniteTransition(label = "snow")
    val flakes = remember { List(25) { SnowFlake(Random.nextFloat(), 2f + Random.nextFloat() * 4f, 0.3f + Random.nextFloat() * 0.5f, 3000 + Random.nextInt(3000)) } }
    flakes.forEachIndexed { i, f ->
        val t by tx.animateFloat(0f, 1f, infiniteRepeatable(tween(f.d, easing = LinearEasing), RepeatMode.Restart), "s$i")
        val sw by tx.animateFloat(-0.025f, 0.025f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), "sw$i")
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color.White.copy(alpha = f.o), f.r, Offset((f.x + sw) * size.width, t * 1.1f * size.height))
        }
    }
}
