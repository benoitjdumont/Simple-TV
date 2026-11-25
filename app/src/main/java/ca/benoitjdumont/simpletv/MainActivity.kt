package ca.benoitjdumont.simpletv

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
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
import kotlin.math.max

class MainActivity : ComponentActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var background: Background
    private lateinit var clock: TextView
    private lateinit var date: TextView
    private lateinit var weather: TextView
    private lateinit var favList: RecyclerView
    private lateinit var favEmpty: TextView

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
        favList = findViewById(R.id.fav_list)
        favEmpty = findViewById(R.id.fav_empty)

        // Favorites start at the bottom and grow upward, one row per screenful of width.
        val density = resources.displayMetrics.density
        val usableWidth = resources.displayMetrics.widthPixels - (96 * density).toInt()
        val tileWidth = (168 * density).toInt()
        val layout = GridLayoutManager(this, max(1, usableWidth / tileWidth))
        // GridLayoutManager does not support stackFromEnd; reverseLayout puts the first row at the bottom.
        layout.reverseLayout = true
        favList.layoutManager = layout

        findViewById<Button>(R.id.btn_folders).setOnClickListener {
            startActivity(Intent(this, FoldersActivity::class.java))
        }
        findViewById<Button>(R.id.btn_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload every time, so favorites and newly installed apps show up.
        val favoriteKeys = Folders.load(this).first { it.id == Folders.FAVORITES_ID }.apps
        val favorites = AppsLoader.load(this).filter { it.key in favoriteKeys }
        favList.adapter = AppAdapter(favorites)
        favEmpty.visibility = if (favorites.isEmpty()) View.VISIBLE else View.GONE

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
}
