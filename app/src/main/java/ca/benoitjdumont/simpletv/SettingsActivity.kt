package ca.benoitjdumont.simpletv

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class SettingsActivity : ComponentActivity() {

    private var mode = Prefs.MODE_NONE
    private var imageUri: String? = null
    private var folderUri: String? = null
    private var interval = 60

    private lateinit var btnMode: Button
    private lateinit var btnInterval: Button
    private lateinit var status: TextView
    private lateinit var lat: EditText
    private lateinit var lon: EditText

    private val pickImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            imageUri = uri.toString()
            status.text = getString(R.string.image_selected)
        }
    }

    private val pickFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            runCatching { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            folderUri = uri.toString()
            status.text = getString(R.string.folder_selected)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = Prefs.get(this)
        mode = prefs.getString(Prefs.KEY_MODE, Prefs.MODE_NONE) ?: Prefs.MODE_NONE
        imageUri = prefs.getString(Prefs.KEY_IMAGE_URI, null)
        folderUri = prefs.getString(Prefs.KEY_FOLDER_URI, null)
        interval = prefs.getInt(Prefs.KEY_INTERVAL, 60)

        btnMode = findViewById(R.id.btn_mode)
        btnInterval = findViewById(R.id.btn_interval)
        status = findViewById(R.id.text_status)
        lat = findViewById(R.id.edit_lat)
        lon = findViewById(R.id.edit_lon)

        val (la, lo) = Prefs.coords(this)
        lat.setText(la)
        lon.setText(lo)

        btnMode.setOnClickListener {
            mode = when (mode) {
                Prefs.MODE_NONE -> Prefs.MODE_IMAGE
                Prefs.MODE_IMAGE -> Prefs.MODE_SLIDESHOW
                else -> Prefs.MODE_NONE
            }
            updateLabels()
        }
        findViewById<Button>(R.id.btn_pick_image).setOnClickListener {
            pickImage.launch(arrayOf("image/*"))
        }
        findViewById<Button>(R.id.btn_pick_folder).setOnClickListener {
            pickFolder.launch(null)
        }
        btnInterval.setOnClickListener {
            interval = when (interval) {
                30 -> 60
                60 -> 300
                else -> 30
            }
            updateLabels()
        }
        findViewById<Button>(R.id.btn_save).setOnClickListener { save() }

        updateLabels()
    }

    private fun updateLabels() {
        val modeName = when (mode) {
            Prefs.MODE_IMAGE -> getString(R.string.mode_image)
            Prefs.MODE_SLIDESHOW -> getString(R.string.mode_slideshow)
            else -> getString(R.string.mode_none)
        }
        btnMode.text = getString(R.string.settings_mode, modeName)
        btnInterval.text = getString(R.string.settings_interval, interval)
    }

    private fun save() {
        val latVal = lat.text.toString().toDoubleOrNull()
        val lonVal = lon.text.toString().toDoubleOrNull()
        if (latVal == null || lonVal == null || latVal !in -90.0..90.0 || lonVal !in -180.0..180.0) {
            status.text = getString(R.string.bad_coords)
            return
        }
        if (mode == Prefs.MODE_IMAGE && imageUri == null) {
            status.text = getString(R.string.need_image)
            return
        }
        if (mode == Prefs.MODE_SLIDESHOW && folderUri == null) {
            status.text = getString(R.string.need_folder)
            return
        }

        Prefs.get(this).edit()
            .putString(Prefs.KEY_MODE, mode)
            .putString(Prefs.KEY_IMAGE_URI, imageUri)
            .putString(Prefs.KEY_FOLDER_URI, folderUri)
            .putInt(Prefs.KEY_INTERVAL, interval)
            .putString(Prefs.KEY_LAT, lat.text.toString().trim())
            .putString(Prefs.KEY_LON, lon.text.toString().trim())
            .apply()
        finish()
    }
}
