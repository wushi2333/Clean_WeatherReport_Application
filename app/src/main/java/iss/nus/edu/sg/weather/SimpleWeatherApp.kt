package iss.nus.edu.sg.weather

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import iss.nus.edu.sg.weather.data.local.PrefsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SimpleWeatherApp : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        // Clean old weather cache on startup (background)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            PrefsManager(this@SimpleWeatherApp).cleanOldCache()
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizeBytes(5 * 1024 * 1024) // 5MB
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .maxSizeBytes(10 * 1024 * 1024) // 10MB
                    .build()
            }
            .build()
    }
}
