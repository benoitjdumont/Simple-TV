package ca.benoitjdumont.simpletv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/** Simple text tiles, used for the folders on the home screen. */
class TileAdapter(private val labels: List<String>, private val onClick: (Int) -> Unit) :
    RecyclerView.Adapter<TileAdapter.Holder>() {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_tile, parent, false))

    override fun getItemCount(): Int = labels.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.text.text = labels[position]
        holder.itemView.setOnClickListener { onClick(holder.bindingAdapterPosition) }
    }
}
