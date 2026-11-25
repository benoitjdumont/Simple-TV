package ca.benoitjdumont.simpletv

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

/**
 * Everything on the home screen in one list, read from the bottom up:
 * the folder row first (at the very bottom), a full-width break, then the favorite apps above it.
 */
class HomeAdapter(
    private val folders: List<Folder>,
    private val apps: List<AppEntry>,
    private val onFolder: (Folder) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_FOLDER = 0
        const val TYPE_BREAK = 1
        const val TYPE_APP = 2
    }

    class FolderHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.label)
    }

    class AppHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.icon)
        val label: TextView = view.findViewById(R.id.label)
    }

    override fun getItemCount(): Int = folders.size + 1 + apps.size

    override fun getItemViewType(position: Int): Int = when {
        position < folders.size -> TYPE_FOLDER
        position == folders.size -> TYPE_BREAK
        else -> TYPE_APP
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_FOLDER -> FolderHolder(inflater.inflate(R.layout.item_folder_tile, parent, false))
            TYPE_APP -> AppHolder(inflater.inflate(R.layout.item_app_icon, parent, false))
            else -> object : RecyclerView.ViewHolder(
                View(parent.context).apply {
                    layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
                }
            ) {}
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FolderHolder -> {
                val folder = folders[position]
                holder.label.text = Folders.displayName(holder.itemView.context, folder)
                holder.itemView.setOnClickListener { onFolder(folder) }
            }
            is AppHolder -> {
                val app = apps[position - folders.size - 1]
                holder.icon.setImageDrawable(app.icon)
                holder.label.text = app.label
                holder.itemView.setOnClickListener {
                    try {
                        holder.itemView.context.startActivity(app.intent)
                    } catch (e: Exception) {
                        Toast.makeText(holder.itemView.context, app.label, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
