package iss.nus.edu.sg.weather.data.remote

import iss.nus.edu.sg.weather.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val JWT_HOST = "https://${BuildConfig.QWEATHER_API_HOST}/"

    private val jwtInterceptor = Interceptor { chain ->
        val token = JwtManager.getToken(
            BuildConfig.QWEATHER_KID, BuildConfig.QWEATHER_SUB, BuildConfig.QWEATHER_PRIVATE_KEY
        )
        chain.proceed(chain.request().newBuilder()
            .header("Authorization", "Bearer $token").build())
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(jwtInterceptor)
            .build()
    }

    val api: QWeatherApi by lazy {
        Retrofit.Builder().baseUrl(JWT_HOST).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(QWeatherApi::class.java)
    }
}
