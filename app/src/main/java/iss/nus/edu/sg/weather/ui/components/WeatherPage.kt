package iss.nus.edu.sg.weather.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import iss.nus.edu.sg.weather.data.model.City
import iss.nus.edu.sg.weather.data.model.DailyForecast
import iss.nus.edu.sg.weather.data.model.HourlyForecast
import iss.nus.edu.sg.weather.data.model.NowWeather
import iss.nus.edu.sg.weather.ui.theme.White
import iss.nus.edu.sg.weather.ui.theme.White50
import iss.nus.edu.sg.weather.ui.theme.White70
import iss.nus.edu.sg.weather.ui.theme.White90

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun WeatherPage(
    city: City, now: NowWeather?, dailyList: List<DailyForecast>,
    hourlyList: List<HourlyForecast> = emptyList(), modifier: Modifier = Modifier,
    onLongPress: () -> Unit = {}
) {
    val iconCode = now?.icon ?: "100"
    val weatherEmoji = weatherEmoji(now?.text ?: "")

    WeatherBackground(iconCode = iconCode) {
        Column(
            modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(60.dp))

            Text(city.name,
                modifier = Modifier.combinedClickable(
                    onClick = {}, onLongClick = onLongPress
                ),
                style = MaterialTheme.typography.titleLarge, color = White)
            if (city.adm1.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(city.adm1, style = MaterialTheme.typography.bodySmall, color = White70)
            }

            Spacer(Modifier.height(12.dp))

            // Weather icon
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://api.qweather.com/icons/${iconCode}.png").crossfade(true).build(),
                contentDescription = now?.text ?: "天气",
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(4.dp))

            // Weather description with emoji
            Text(
                text = if (now != null) "$weatherEmoji ${now.text}" else "加载中...",
                style = MaterialTheme.typography.headlineMedium,
                color = White90
            )

            Spacer(Modifier.height(4.dp))

            // Temperature
            Text(
                text = if (now != null) "${now.temp}°" else "--°",
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Thin),
                color = White
            )

            Spacer(Modifier.height(16.dp))

            // Info row
            if (now != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoChip("体感", "${now.feelsLike}°", extraGap = true)
                    InfoChip("湿度", "${now.humidity}%", extraGap = true)
                    InfoChip("风向", "${now.windDir}${now.windScale}级")
                }
            }

            Spacer(Modifier.height(16.dp))

            // 24-hour forecast
            HourlyForecastRow(hourlyList = hourlyList)

            Spacer(Modifier.height(14.dp))

            // 3-day forecast
            DailyForecastRow(dailyList = dailyList)

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String, extraGap: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = White50)
        Spacer(Modifier.height(if (extraGap) 10.dp else 4.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = White90)
    }
}

/** Map Chinese weather text to emoji symbol */
private fun weatherEmoji(text: String): String = when {
    text.contains("晴") -> "☀️"
    text.contains("云") -> "⛅"
    text.contains("阴") -> "☁️"
    text.contains("雨") || text.contains("雷") -> "🌧️"
    text.contains("雪") -> "❄️"
    text.contains("雾") || text.contains("霾") || text.contains("尘") -> "🌫️"
    text.contains("风") -> "💨"
    else -> ""
}
