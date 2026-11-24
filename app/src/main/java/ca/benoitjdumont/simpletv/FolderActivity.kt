package ca.benoitjdumont.simpletv

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/** Shows the apps of one folder as text tiles. */
class FolderActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ID = "folder_id"
    }

    private lateinit var title: TextView
    private lateinit var empty: TextView
    private lateinit var list: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder)
        title = findViewById(R.id.title)
        empty = findViewById(R.id.empty)
        list = findViewById(R.id.list)
        list.layoutManager = GridLayoutManager(this, 5)
    }

    override fun onResume() {
        super.onResume()
        val id = intent.getStringExtra(EXTRA_ID)
        val folder = Folders.load(this).firstOrNull { it.id == id } ?: run {
            finish()
            return
        }

        title.text = Folders.displayName(this, folder)
        val all = AppsLoader.load(this)
        val shown = if (folder.id == Folders.ALL_ID) all else all.filter { it.key in folder.apps }

        empty.visibility = if (shown.isEmpty()) View.VISIBLE else View.GONE
        list.adapter = AppAdapter(shown, Prefs.showIcons(this))
    }
}
