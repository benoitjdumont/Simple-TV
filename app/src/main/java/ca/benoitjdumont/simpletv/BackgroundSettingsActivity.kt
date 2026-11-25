package ca.benoitjdumont.simpletv

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import java.io.File
import kotlin.concurrent.thread

/**
 * Background page: none, one image, or a slideshow from a folder.
 * The chosen photos are copied into the app's own storage, so they survive reboots.
 */
class BackgroundSettingsActivity : ComponentActivity() {

    companion object {
        private const val MAX_SLIDES = 50
    }

    private var mode = Prefs.MODE_NONE
    private var imageUri: String? = null
    private var folderUri: String? = null
    private var interval = 60

    private lateinit var btnMode: Button
    private lateinit var btnInterval: Button
    private lateinit var status: TextView

    // Picking an image or folder also selects the matching background mode.
    private val pickImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            status.text = getString(R.string.copying)
            thread {
                val dest = File(filesDir, "background.img")
                val ok = try {
                    copyTo(uri, dest)
                    true
                } catch (e: Exception) {
                    false
                }
                runOnUiThread {
                    if (ok) {
                        imageUri = Uri.fromFile(dest).toString()
                        mode = Prefs.MODE_IMAGE
                        status.text = getString(R.string.image_selected)
                        updateLabels()
                    } else {
                        status.text = getString(R.string.copy_failed)
                    }
                }
            }
        }
    }

    private val pickFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            status.text = getString(R.string.copying)
            thread {
                val dir = File(filesDir, "slides")
                dir.deleteRecursively()
                dir.mkdirs()
                val count = try {
                    val photos = DocumentFile.fromTreeUri(this, uri)?.listFiles()
                        ?.filter { it.isFile && it.type?.startsWith("image/") == true }
                        ?.take(MAX_SLIDES)
                        ?: emptyList()
                    photos.forEachIndexed { i, photo -> copyTo(photo.uri, File(dir, "slide_$i")) }
                    photos.size
                } catch (e: Exception) {
                    0
                }
                runOnUiThread {
                    if (count > 0) {
                        folderUri = dir.absolutePath
                        mode = Prefs.MODE_SLIDESHOW
                        status.text = getString(R.string.folder_selected)
                        updateLabels()
                    } else {
                        status.text = getString(R.string.copy_failed)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_background_settings)

        val prefs = Prefs.get(this)
        mode = prefs.getString(Prefs.KEY_MODE, Prefs.MODE_NONE) ?: Prefs.MODE_NONE
        imageUri = prefs.getString(Prefs.KEY_IMAGE_URI, null)
        folderUri = prefs.getString(Prefs.KEY_FOLDER_URI, null)
        interval = prefs.getInt(Prefs.KEY_INTERVAL, 60)

        btnMode = findViewById(R.id.btn_mode)
        btnInterval = findViewById(R.id.btn_interval)
        status = findViewById(R.id.text_status)

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

    private fun copyTo(src: Uri, dest: File) {
        contentResolver.openInputStream(src)?.use { input ->
            dest.outputStream().use { input.copyTo(it) }
        } ?: throw IllegalStateException("cannot open $src")
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
            .apply()
        finish()
    }
}
