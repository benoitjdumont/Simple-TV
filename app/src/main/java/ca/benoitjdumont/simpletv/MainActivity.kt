package ca.benoitjdumont.simpletv

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
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
import kotlin.math.min

class MainActivity : ComponentActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var background: Background
    private lateinit var clock: TextView
    private lateinit var date: TextView
    private lateinit var weather: TextView
    private lateinit var folderList: RecyclerView

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
        folderList = findViewById(R.id.folder_list)

        // Up to 5 folder tiles per row. The first row sits at the bottom, extra rows go above.
        val density = resources.displayMetrics.density
        val usableWidth = resources.displayMetrics.widthPixels - (96 * density).toInt()
        val tileWidth = (168 * density).toInt()
        val span = min(5, max(1, usableWidth / tileWidth))
        val layout = GridLayoutManager(this, span)
        // GridLayoutManager does not support stackFromEnd; reverseLayout puts the first row at the bottom.
        layout.reverseLayout = true
        folderList.layoutManager = layout

        findViewById<ImageButton>(R.id.btn_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload every time, so renamed folders and new apps show up.
        folderList.adapter = FolderTileAdapter(Folders.load(this)) { folder ->
            startActivity(
                Intent(this, FolderActivity::class.java)
                    .putExtra(FolderActivity.EXTRA_ID, folder.id)
            )
        }

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
