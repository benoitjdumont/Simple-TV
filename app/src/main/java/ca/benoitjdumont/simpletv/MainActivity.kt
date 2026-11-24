package ca.benoitjdumont.simpletv

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var background: Background
    private lateinit var clock: TextView
    private lateinit var date: TextView
    private lateinit var weather: TextView
    private lateinit var apps: RecyclerView

    private val clockTick = object : Runnable {
        override fun run() {
            updateClock()
            handler.postDelayed(this, 30_000L)
        }
    }

    private val weatherTick = object : Runnable {
        override fun run() {
            refreshWeather()
            handler.postDelayed(this, 30 * 60_000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        background = Background(this, findViewById<ImageView>(R.id.background), handler)
        clock = findViewById(R.id.clock)
        date = findViewById(R.id.date)
        weather = findViewById(R.id.weather)
        apps = findViewById(R.id.apps)
        apps.layoutManager = GridLayoutManager(this, 6)

        findViewById<Button>(R.id.btn_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload the app list each time, so newly installed apps appear.
        apps.adapter = AppAdapter(loadApps())
        background.start()
        handler.post(clockTick)
        handler.post(weatherTick)
    }

    override fun onPause() {
        super.onPause()
        background.stop()
        handler.removeCallbacks(clockTick)
        handler.removeCallbacks(weatherTick)
    }

    private fun updateClock() {
        val now = Date()
        // Locale-aware formatting; the system language decides the date text.
        clock.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
        date.text = SimpleDateFormat("EEEE d MMMM", Locale.getDefault()).format(now)
    }

    private fun refreshWeather() {
        val (lat, lon) = Prefs.coords(this)
        thread {
            val text = try {
                WeatherFetcher.fetch(lat, lon)
            } catch (e: Exception) {
                getString(R.string.weather_unavailable)
            }
            runOnUiThread { weather.text = text }
        }
    }

    private fun loadApps(): List<AppEntry> {
        val pm = packageManager
        val leanback = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val mobile = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        return (pm.queryIntentActivities(leanback, 0) + pm.queryIntentActivities(mobile, 0))
            .filter { it.activityInfo.packageName != packageName }
            .distinctBy { it.activityInfo.packageName }
            .map { info ->
                AppEntry(
                    label = info.loadLabel(pm).toString(),
                    icon = info.loadIcon(pm),
                    intent = Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setComponent(ComponentName(info.activityInfo.packageName, info.activityInfo.name))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
            .sortedBy { it.label.lowercase() }
    }
}
