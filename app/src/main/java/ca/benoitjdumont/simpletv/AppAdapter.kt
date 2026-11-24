package ca.benoitjdumont.simpletv

import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

data class AppEntry(val label: String, val icon: Drawable, val intent: Intent)

class AppAdapter(private val items: List<AppEntry>) : RecyclerView.Adapter<AppAdapter.Holder>() {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.icon)
        val label: TextView = view.findViewById(R.id.label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        holder.icon.setImageDrawable(item.icon)
        holder.label.text = item.label
        holder.itemView.setOnClickListener {
            try {
                holder.itemView.context.startActivity(item.intent)
            } catch (e: Exception) {
                Toast.makeText(holder.itemView.context, item.label, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
