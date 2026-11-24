package ca.benoitjdumont.simpletv

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

/** Current temperature from Open-Meteo (free, no API key). Call from a background thread. */
object WeatherFetcher {
    fun fetch(lat: String, lon: String): String {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true"
        )
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val temp = JSONObject(body).getJSONObject("current_weather").getDouble("temperature")
            return "${temp.roundToInt()} °C"
        } finally {
            conn.disconnect()
        }
    }
}
