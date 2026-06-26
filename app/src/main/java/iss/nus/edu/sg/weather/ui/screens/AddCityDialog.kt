package iss.nus.edu.sg.weather.ui.screens

import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import iss.nus.edu.sg.weather.data.local.CityDatabase
import iss.nus.edu.sg.weather.ui.theme.SunnyDayTop
import iss.nus.edu.sg.weather.ui.theme.White70
import iss.nus.edu.sg.weather.ui.theme.White90

@Composable
fun AddCityDialog(
    onSelectCity: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val results = remember(query) { CityDatabase.search(query) }
    val context = LocalContext.current

    // Voice input launcher
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()?.let { query = it }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("添加城市", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Medium)
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "关闭", tint = White70) }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = query, onValueChange = { query = it },
                placeholder = { Text("输入或说出城市名", color = White70, fontSize = 18.sp) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = White90,
                    focusedBorderColor = SunnyDayTop, unfocusedBorderColor = White70,
                    cursorColor = Color.White
                ),
                leadingIcon = { Icon(Icons.Default.Search, null, tint = White70) },
                trailingIcon = {
                    IconButton(onClick = {
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "说出城市名")
                            }
                            voiceLauncher.launch(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "此设备不支持语音输入", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Mic, "语音输入", tint = SunnyDayTop)
                    }
                }
            )

            if (query.isNotBlank() && results.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                ) {
                    items(results) { city ->
                        Column(
                            Modifier.fillMaxWidth().clickable { onSelectCity(city.cityId) }.padding(12.dp)
                        ) {
                            Text(city.name, color = Color.White, fontSize = 18.sp)
                            Text(city.province, color = White70, fontSize = 14.sp)
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    }
                }
            } else if (query.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text("未找到匹配城市", color = White70, fontSize = 16.sp)
            }
        }
    }
}
