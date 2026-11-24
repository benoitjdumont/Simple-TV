package ca.benoitjdumont.simpletv

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.widget.ImageView
import androidx.documentfile.provider.DocumentFile

/** Shows nothing, one image, or a slideshow of a folder behind the home screen. */
class Background(
    private val context: Context,
    private val view: ImageView,
    private val handler: Handler
) {
    private var images: List<Uri> = emptyList()
    private var index = 0

    private val advance = object : Runnable {
        override fun run() {
            if (images.size > 1) {
                index = (index + 1) % images.size
                show(images[index])
            }
            handler.postDelayed(this, intervalMs())
        }
    }

    fun start() {
        stop()
        val prefs = Prefs.get(context)
        when (prefs.getString(Prefs.KEY_MODE, Prefs.MODE_NONE)) {
            Prefs.MODE_IMAGE -> {
                val uri = prefs.getString(Prefs.KEY_IMAGE_URI, null)
                if (uri != null) show(Uri.parse(uri)) else view.setImageDrawable(null)
            }
            Prefs.MODE_SLIDESHOW -> {
                images = listImages(prefs.getString(Prefs.KEY_FOLDER_URI, null))
                if (images.isNotEmpty()) {
                    index = 0
                    show(images[0])
                    if (images.size > 1) handler.postDelayed(advance, intervalMs())
                } else {
                    view.setImageDrawable(null)
                }
            }
            else -> view.setImageDrawable(null)
        }
    }

    fun stop() {
        handler.removeCallbacks(advance)
    }

    private fun intervalMs(): Long =
        Prefs.get(context).getInt(Prefs.KEY_INTERVAL, 60) * 1000L

    private fun listImages(folder: String?): List<Uri> {
        if (folder == null) return emptyList()
        return try {
            val dir = DocumentFile.fromTreeUri(context, Uri.parse(folder)) ?: return emptyList()
            dir.listFiles()
                .filter { it.isFile && it.type?.startsWith("image/") == true }
                .map { it.uri }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun show(uri: Uri) {
        view.setImageBitmap(decode(uri))
    }

    /** Decodes at most about 1080p so a large photo does not eat the TV's memory. */
    private fun decode(uri: Uri): Bitmap? = try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

        var sample = 1
        while (bounds.outWidth / (sample * 2) >= 1920 && bounds.outHeight / (sample * 2) >= 1080) {
            sample *= 2
        }
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
    } catch (e: Exception) {
        null
    }
}
