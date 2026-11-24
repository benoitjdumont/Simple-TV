package ca.benoitjdumont.simpletv

import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
    private lateinit var btnHome: Button
    private lateinit var homeStatus: TextView

    // Returns from the system "default home app" dialog; refresh the status line.
    private val requestHome = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        updateHomeStatus()
    }

    // Picking an image or folder also selects the matching background mode.
    private val pickImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            imageUri = uri.toString()
            mode = Prefs.MODE_IMAGE
            status.text = getString(R.string.image_selected)
            updateLabels()
        }
    }

    private val pickFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            runCatching { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            folderUri = uri.toString()
            mode = Prefs.MODE_SLIDESHOW
            status.text = getString(R.string.folder_selected)
            updateLabels()
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
        btnHome = findViewById(R.id.btn_default_home)
        homeStatus = findViewById(R.id.home_status)
        btnHome.setOnClickListener { askDefaultHome() }

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

    override fun onResume() {
        super.onResume()
        updateHomeStatus()
    }

    /** True when Simple TV is the app Android opens with the Home button. */
    private fun isDefaultHome(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val match = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return match?.activityInfo?.packageName == packageName
    }

    private fun updateHomeStatus() {
        homeStatus.text = getString(if (isDefaultHome()) R.string.home_yes else R.string.home_no)
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
