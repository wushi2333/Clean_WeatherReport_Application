package iss.nus.edu.sg.weather.data.model

data class City(
    var id: Long = 0,
    val name: String,
    val cityId: String,
    val latitude: String = "",
    val longitude: String = "",
    val adm1: String = "",
    var sortOrder: Int = 0,
    val source: String = "manual"  // "default" | "gps" | "manual"
)
