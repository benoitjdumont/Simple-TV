package ca.benoitjdumont.simpletv

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity

/** Weather page: the latitude and longitude used for the forecast. */
class WeatherSettingsActivity : ComponentActivity() {

    private lateinit var lat: EditText
    private lateinit var lon: EditText
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_settings)

        lat = findViewById(R.id.edit_lat)
        lon = findViewById(R.id.edit_lon)
        status = findViewById(R.id.text_status)

        val (la, lo) = Prefs.coords(this)
        lat.setText(la)
        lon.setText(lo)

        findViewById<Button>(R.id.btn_save).setOnClickListener { save() }
    }

    private fun save() {
        val latVal = lat.text.toString().toDoubleOrNull()
        val lonVal = lon.text.toString().toDoubleOrNull()
        if (latVal == null || lonVal == null || latVal !in -90.0..90.0 || lonVal !in -180.0..180.0) {
            status.text = getString(R.string.bad_coords)
            return
        }
        Prefs.get(this).edit()
            .putString(Prefs.KEY_LAT, lat.text.toString().trim())
            .putString(Prefs.KEY_LON, lon.text.toString().trim())
            .apply()
        finish()
    }
}
