package ca.benoitjdumont.simpletv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/** One tile per folder on the home screen. Click opens the folder in its own window. */
class FolderTileAdapter(
    private val folders: List<Folder>,
    private val onOpen: (Folder) -> Unit
) : RecyclerView.Adapter<FolderTileAdapter.Holder>() {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_folder_tile, parent, false))

    override fun getItemCount(): Int = folders.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val folder = folders[position]
        holder.label.text = Folders.displayName(holder.itemView.context, folder)
        holder.itemView.setOnClickListener { onOpen(folder) }
    }
}
