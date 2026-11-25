package ca.benoitjdumont.simpletv

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
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

    companion object {
        private const val ROW_DP = 138   // one tile (130dp) plus its margins
        private const val SLIVER_DP = 28 // how much of the folder row stays visible at the bottom
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var background: Background
    private lateinit var clock: TextView
    private lateinit var date: TextView
    private lateinit var weather: TextView
    private lateinit var homeList: RecyclerView
    private var rowPx = 0

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
        homeList = findViewById(R.id.home_list)

        val density = resources.displayMetrics.density
        rowPx = (ROW_DP * density).toInt()
        val sliverPx = (SLIVER_DP * density).toInt()
        // The list runs below the screen edge, so only a sliver of the folder row shows.
        (homeList.layoutParams as FrameLayout.LayoutParams).bottomMargin = -(rowPx - sliverPx)

        // Up to 5 tiles per row. The first row sits at the bottom, extra rows go above.
        val usableWidth = resources.displayMetrics.widthPixels - (96 * density).toInt()
        val tileWidth = (168 * density).toInt()
        val span = min(5, max(1, usableWidth / tileWidth))
        val layout = GridLayoutManager(this, span)
        // GridLayoutManager does not support stackFromEnd; reverseLayout puts the first row at the bottom.
        layout.reverseLayout = true
        layout.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                if (homeList.adapter?.getItemViewType(position) == HomeAdapter.TYPE_BREAK) span else 1
        }
        homeList.layoutManager = layout
        homeList.clipChildren = false
        homeList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) = applyDrum()
        })
        homeList.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> applyDrum() }

        findViewById<ImageButton>(R.id.btn_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload every time, so renamed folders and new favorites show up.
        val folders = Folders.load(this)
        val favoriteKeys = folders.first { it.id == Folders.FAVORITES_ID }.apps
        val favorites = AppsLoader.load(this).filter { it.key in favoriteKeys }
        val shownFolders = folders.filter { it.id != Folders.FAVORITES_ID }

        homeList.adapter = HomeAdapter(shownFolders, favorites) { folder ->
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

    /** Rows get smaller and fainter the further they are from the folder row, like a turning drum. */
    private fun applyDrum() {
        val adapter = homeList.adapter ?: return
        val anchor = homeList.height - rowPx * 1.5f // centre of the favorite row just above the folders
        for (i in 0 until homeList.childCount) {
            val child = homeList.getChildAt(i)
            val pos = homeList.getChildAdapterPosition(child)
            if (pos == RecyclerView.NO_POSITION || adapter.getItemViewType(pos) != HomeAdapter.TYPE_APP) {
                child.alpha = 1f
                child.scaleX = 1f
                child.scaleY = 1f
                continue
            }
            val centre = (child.top + child.bottom) / 2f
            val distance = ((anchor - centre) / rowPx).coerceAtLeast(0f)
            child.alpha = (1f - 0.3f * distance).coerceIn(0.2f, 1f)
            val scale = (1f - 0.1f * distance).coerceIn(0.7f, 1f)
            child.scaleX = scale
            child.scaleY = scale
        }
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
