package ca.benoitjdumont.simpletv

import android.content.Context
import android.content.SharedPreferences

/** All user settings in one place. */
object Prefs {
    const val MODE_NONE = "none"
    const val MODE_IMAGE = "image"
    const val MODE_SLIDESHOW = "slideshow"

    const val KEY_MODE = "mode"
    const val KEY_IMAGE_URI = "image_uri"
    const val KEY_FOLDER_URI = "folder_uri"
    const val KEY_INTERVAL = "interval_s"
    const val KEY_LAT = "lat"
    const val KEY_LON = "lon"
    const val KEY_ICONS = "show_icons"

    private const val FILE = "simple_tv"
    private const val DEFAULT_LAT = "45.5017"   // Montréal, change it in Settings
    private const val DEFAULT_LON = "-73.5673"

    fun get(context: Context): SharedPreferences =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun coords(context: Context): Pair<String, String> {
        val p = get(context)
        return Pair(
            p.getString(KEY_LAT, DEFAULT_LAT) ?: DEFAULT_LAT,
            p.getString(KEY_LON, DEFAULT_LON) ?: DEFAULT_LON
        )
    }

    fun showIcons(context: Context): Boolean = get(context).getBoolean(KEY_ICONS, false)
}
