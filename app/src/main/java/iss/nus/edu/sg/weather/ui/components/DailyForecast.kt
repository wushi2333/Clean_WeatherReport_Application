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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import iss.nus.edu.sg.weather.data.model.DailyForecast

@Composable
fun DailyForecastRow(dailyList: List<DailyForecast>, modifier: Modifier = Modifier) {
    if (dailyList.isEmpty()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.22f))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        dailyList.take(4).forEachIndexed { idx, day -> ForecastCard(day, idx) }
    }
}

@Composable
private fun ForecastCard(day: DailyForecast, index: Int) {
    val emoji = dayEmoji(day.textDay)

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(indexLabel(index, day.fxDate),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium)

        Spacer(Modifier.height(6.dp))

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://api.qweather.com/icons/${day.iconDay}.png").crossfade(true).build(),
            contentDescription = day.textDay,
            modifier = Modifier.size(40.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(2.dp))

        Text("$emoji ${day.textDay}",
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center)

        Spacer(Modifier.height(6.dp))

        Text("${day.tempMin}° ~ ${day.tempMax}°",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.SemiBold)
    }
}

private fun indexLabel(i: Int, fallback: String): String = when (i) {
    0 -> "今天"; 1 -> "明天"; 2 -> "后天"; 3 -> "大后天"
    else -> {
        try {
            val (y, m, d) = fallback.split("-").map { it.toInt() }
            "${m}月${d}日"
        } catch (_: Exception) { fallback }
    }
}

private fun dayEmoji(text: String): String = when {
    text.contains("晴") -> "☀️"
    text.contains("云") -> "⛅"
    text.contains("阴") -> "☁️"
    text.contains("雨") || text.contains("雷") -> "🌧️"
    text.contains("雪") -> "❄️"
    text.contains("雾") || text.contains("霾") -> "🌫️"
    else -> ""
}
