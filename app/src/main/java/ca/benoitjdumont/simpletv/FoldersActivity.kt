package ca.benoitjdumont.simpletv

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/** Create, rename, delete folders, choose their apps, and open them. */
class FoldersActivity : ComponentActivity() {

    private lateinit var list: RecyclerView
    private var folders: MutableList<Folder> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folders)
        list = findViewById(R.id.list)
        list.layoutManager = LinearLayoutManager(this)
        findViewById<Button>(R.id.btn_new_folder).setOnClickListener { askName(null) }
    }

    override fun onResume() {
        super.onResume()
        reload()
    }

    private fun reload() {
        folders = Folders.load(this)
        list.adapter = FolderRowAdapter(
            folders,
            nameOf = { Folders.displayName(this, it) },
            onOpen = { startActivity(Intent(this, FolderActivity::class.java).putExtra(FolderActivity.EXTRA_ID, it.id)) },
            onRename = { askName(it) },
            onPick = { openPicker(it) },
            onDelete = { confirmDelete(it) }
        )
    }

    private fun askName(folder: Folder?) {
        val input = EditText(this).apply {
            setText(folder?.name ?: "")
            hint = getString(R.string.folder_name_hint)
            setSingleLine(true)
        }
        AlertDialog.Builder(this)
            .setTitle(if (folder == null) R.string.btn_new_folder else R.string.btn_rename)
            .setView(input)
            .setPositiveButton(R.string.btn_ok) { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    if (folder == null) folders.add(Folder(Folders.newId(), name, mutableListOf()))
                    else folder.name = name
                    Folders.save(this, folders)
                    reload()
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun confirmDelete(folder: Folder) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.confirm_delete, folder.name))
            .setPositiveButton(R.string.btn_delete) { _, _ ->
                folders.remove(folder)
                Folders.save(this, folders)
                reload()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun openPicker(folder: Folder) {
        startActivity(
            Intent(this, AppPickerActivity::class.java)
                .putExtra(AppPickerActivity.EXTRA_ID, folder.id)
        )
    }
}
