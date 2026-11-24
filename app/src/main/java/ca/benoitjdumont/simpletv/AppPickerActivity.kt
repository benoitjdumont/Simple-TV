package ca.benoitjdumont.simpletv

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/** Tick the apps that belong to one folder (or to favorites). Each tick saves immediately. */
class AppPickerActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ID = "folder_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_picker)

        val folders = Folders.load(this)
        val folder = folders.firstOrNull { it.id == intent.getStringExtra(EXTRA_ID) }
        if (folder == null || folder.id == Folders.ALL_ID) {
            finish()
            return
        }

        findViewById<TextView>(R.id.title).text = Folders.displayName(this, folder)
        val list: RecyclerView = findViewById(R.id.list)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = CheckAdapter(AppsLoader.load(this), folder.apps) { key, checked ->
            if (checked) {
                if (!folder.apps.contains(key)) folder.apps.add(key)
            } else {
                folder.apps.remove(key)
            }
            Folders.save(this, folders)
        }
    }
}
