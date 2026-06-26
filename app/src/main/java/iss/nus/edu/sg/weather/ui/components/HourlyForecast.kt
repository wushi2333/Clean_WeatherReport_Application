package iss.nus.edu.sg.weather.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import iss.nus.edu.sg.weather.data.model.HourlyForecast

/**
 * Horizontally scrollable 24-hour forecast — Apple Weather style.
 */
@Composable
fun HourlyForecastRow(hourlyList: List<HourlyForecast>, modifier: Modifier = Modifier) {
    if (hourlyList.isEmpty()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.22f))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        hourlyList.take(24).forEachIndexed { i, h -> HourlyItem(h, isFirst = i == 0) }
    }
}

@Composable
private fun HourlyItem(h: HourlyForecast, isFirst: Boolean) {
    val hour = formatHour(h.fxTime, isFirst)
    val pop = h.pop.toIntOrNull() ?: 0

    val weatherEmoji = when {
        h.text.contains("晴") -> "☀️"
        h.text.contains("云") -> "⛅"
        h.text.contains("阴") -> "☁️"
        h.text.contains("雨") || h.text.contains("雷") -> "🌧️"
        h.text.contains("雪") -> "❄️"
        h.text.contains("雾") || h.text.contains("霾") -> "🌫️"
        else -> ""
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(hour, fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))

        // Weather icon from CDN
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://api.qweather.com/icons/${h.icon}.png").crossfade(true).build(),
            contentDescription = h.text,
            modifier = Modifier.size(34.dp),
            contentScale = ContentScale.Fit
        )

        // Weather text with emoji
        Text(
            text = if (weatherEmoji.isNotEmpty()) "$weatherEmoji ${h.text}" else h.text,
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )

        Text("${h.temp}°",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f))

        Text(
            text = if (pop > 0) "💧$pop%" else "",
            fontSize = 14.sp,
            color = Color(0xFFB0E0FF),
            textAlign = TextAlign.Center
        )
    }
}

/** Extract "HH:MM" from fxTime. First item = "现在". */
private fun formatHour(fxTime: String, isFirst: Boolean): String {
    if (isFirst) return "现在"
    if (fxTime.length < 13) return fxTime
    return try {
        val sep = if (fxTime.contains("T")) "T" else " "
        val afterSep = fxTime.substringAfter(sep)
        val h = afterSep.substring(0, 2).toInt()
        val m = afterSep.substring(3, 5)
        "${h}:${m}"
    } catch (_: Exception) { fxTime }
}
