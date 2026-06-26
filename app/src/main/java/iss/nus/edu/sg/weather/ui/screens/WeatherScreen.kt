package iss.nus.edu.sg.weather.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import iss.nus.edu.sg.weather.ui.components.WeatherPage
import iss.nus.edu.sg.weather.ui.theme.Black20
import iss.nus.edu.sg.weather.ui.theme.White
import iss.nus.edu.sg.weather.ui.theme.White50
import iss.nus.edu.sg.weather.ui.theme.White70
import iss.nus.edu.sg.weather.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteTargetCityId by remember { mutableStateOf<String?>(null) }

    // Request location permission on first launch, retry GPS if granted
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.retryGps() }
    LaunchedEffect(Unit) {
        permLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    // Close add dialog after city is selected (searchResults cleared)
    LaunchedEffect(uiState.searchResults) {
        if (showAddDialog && uiState.searchResults.isEmpty() && uiState.errorMessage == null) {
            showAddDialog = false
        }
    }

    val pagerState = rememberPagerState(
        initialPage = uiState.currentPageIndex,
        pageCount = { uiState.cities.size.coerceAtLeast(1) }
    )

    LaunchedEffect(pagerState.currentPage) { viewModel.setPageIndex(pagerState.currentPage) }
    LaunchedEffect(uiState.currentPageIndex) {
        if (uiState.currentPageIndex != pagerState.currentPage) {
            pagerState.animateScrollToPage(uiState.currentPageIndex, animationSpec = tween(300))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.cities.isEmpty()) {
            Box(Modifier.fillMaxSize().background(Color(0xFF3B82F6)), contentAlignment = Alignment.Center) {
                Text("加载中...", color = White, style = MaterialTheme.typography.headlineMedium)
            }
        } else {
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { scope.launch { viewModel.refreshCurrentCity() } },
                modifier = Modifier.fillMaxSize()
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1
                ) { page ->
                    val city = uiState.cities.getOrNull(page)
                    if (city != null) {
                        val info = uiState.weatherMap[city.cityId]
                        WeatherPage(
                            city = city,
                            now = info?.now,
                            dailyList = info?.dailyList ?: emptyList(),
                            hourlyList = info?.hourlyList ?: emptyList(),
                            modifier = Modifier.fillMaxSize(),
                            onLongPress = {
                if (city.source != "default") deleteTargetCityId = city.cityId
            }
                        )
                    }
                }
            }
        }

        // Page indicator dots (only when 2+ cities)
        if (uiState.cities.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.cities.forEachIndexed { i, _ ->
                    val isActive = i == pagerState.currentPage
                    Box(
                        Modifier
                            .size(if (isActive) 7.dp else 5.dp)
                            .background(
                                if (isActive) White else White50,
                                CircleShape
                            )
                    )
                }
            }
        }

        // Add city button (top-right)
        if (uiState.cities.isNotEmpty()) {
            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(12.dp)
                    .size(40.dp)
                    .background(Black20, CircleShape)
            ) {
                Icon(Icons.Default.Add, "添加城市", tint = White, modifier = Modifier.size(22.dp))
            }

        }
    }

    // Add city dialog
    if (showAddDialog) {
        AddCityDialog(
            onSelectCity = { cityId ->
                showAddDialog = false
                viewModel.addCity(cityId)
            },
            onDismiss = { showAddDialog = false; viewModel.clearError() }
        )
    }

    // Delete confirmation
    val deleteCity = uiState.cities.find { it.cityId == deleteTargetCityId }
    if (deleteCity != null) {
        AlertDialog(
            onDismissRequest = { deleteTargetCityId = null },
            title = { Text("删除城市", style = MaterialTheme.typography.headlineMedium) },
            text = { Text("确定要删除「${deleteCity.name}」吗？", style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteCity(deleteCity.cityId); deleteTargetCityId = null }) {
                    Text("删除", color = Color(0xFFEF4444), style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTargetCityId = null }) {
                    Text("取消", style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }
}
