package ca.benoitjdumont.simpletv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * One row per folder on the manage screen.
 * Name opens the folder. Rename and delete only for user folders. Apps button for all but "All apps".
 */
class FolderRowAdapter(
    private val folders: List<Folder>,
    private val nameOf: (Folder) -> String,
    private val onOpen: (Folder) -> Unit,
    private val onRename: (Folder) -> Unit,
    private val onPick: (Folder) -> Unit,
    private val onDelete: (Folder) -> Unit
) : RecyclerView.Adapter<FolderRowAdapter.Holder>() {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val rename: Button = view.findViewById(R.id.btn_rename)
        val pick: Button = view.findViewById(R.id.btn_pick)
        val delete: Button = view.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_folder_row, parent, false))

    override fun getItemCount(): Int = folders.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val folder = folders[position]
        val isAll = folder.id == Folders.ALL_ID
        val userFolder = !Folders.isBuiltIn(folder)

        holder.name.text = nameOf(folder)
        holder.name.setOnClickListener { onOpen(folder) }
        holder.rename.visibility = if (userFolder) View.VISIBLE else View.GONE
        holder.delete.visibility = if (userFolder) View.VISIBLE else View.GONE
        holder.pick.visibility = if (isAll) View.GONE else View.VISIBLE

        holder.rename.setOnClickListener { onRename(folder) }
        holder.pick.setOnClickListener { onPick(folder) }
        holder.delete.setOnClickListener { onDelete(folder) }
    }
}
