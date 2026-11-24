package ca.benoitjdumont.simpletv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

/** Grid of apps, as text (optionally with icons). Click launches the app. */
class AppAdapter(private val items: List<AppEntry>, private val showIcons: Boolean) :
    RecyclerView.Adapter<AppAdapter.Holder>() {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.icon)
        val label: TextView = view.findViewById(R.id.label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_app_text, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        holder.icon.visibility = if (showIcons) View.VISIBLE else View.GONE
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
