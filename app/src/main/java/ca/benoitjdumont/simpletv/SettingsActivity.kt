package ca.benoitjdumont.simpletv

import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

/** Settings menu in the style of the Google TV settings list. Each entry opens its own page. */
class SettingsActivity : ComponentActivity() {

    // Returns from the system "default home app" dialog; refresh the subtitles.
    private val requestHome = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        updateRows()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<View>(R.id.row_folders).bindRow(R.drawable.ic_folder, R.string.folders_title) {
            startActivity(Intent(this, FoldersActivity::class.java))
        }
        findViewById<View>(R.id.row_background).bindRow(R.drawable.ic_image, R.string.section_background) {
            startActivity(Intent(this, BackgroundSettingsActivity::class.java))
        }
        findViewById<View>(R.id.row_home).bindRow(R.drawable.ic_home, R.string.section_home) {
            askDefaultHome()
        }
        findViewById<View>(R.id.row_weather).bindRow(R.drawable.ic_cloud, R.string.section_weather) {
            startActivity(Intent(this, WeatherSettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateRows()
    }

    private fun View.bindRow(icon: Int, title: Int, onClick: () -> Unit) {
        findViewById<ImageView>(R.id.icon).setImageResource(icon)
        findViewById<TextView>(R.id.title).setText(title)
        setOnClickListener { onClick() }
    }

    private fun View.subtitle(text: String) {
        findViewById<TextView>(R.id.subtitle).text = text
    }

    /** Shows the current value of each setting under its title. */
    private fun updateRows() {
        val modeName = when (Prefs.get(this).getString(Prefs.KEY_MODE, Prefs.MODE_NONE)) {
            Prefs.MODE_IMAGE -> getString(R.string.mode_image)
            Prefs.MODE_SLIDESHOW -> getString(R.string.mode_slideshow)
            else -> getString(R.string.mode_none)
        }
        val (la, lo) = Prefs.coords(this)

        findViewById<View>(R.id.row_folders).subtitle(getString(R.string.row_folders_sub))
        findViewById<View>(R.id.row_background).subtitle(modeName)
        findViewById<View>(R.id.row_home).subtitle(getString(if (isDefaultHome()) R.string.home_yes else R.string.home_no))
        findViewById<View>(R.id.row_weather).subtitle(getString(R.string.weather_coords, la, lo))
    }

    /** True when Simple TV is the app Android opens with the Home button. */
    private fun isDefaultHome(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val match = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return match?.activityInfo?.packageName == packageName
    }

    /** Opens the system dialog that asks which home app to use. Nothing is forced. */
    private fun askDefaultHome() {
        if (Build.VERSION.SDK_INT >= 29) {
            val rm = getSystemService(RoleManager::class.java)
            if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME)) {
                requestHome.launch(rm.createRequestRoleIntent(RoleManager.ROLE_HOME))
                return
            }
        }
        // Older Android, or no role dialog: open the default-apps page instead.
        try {
            startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }
}
