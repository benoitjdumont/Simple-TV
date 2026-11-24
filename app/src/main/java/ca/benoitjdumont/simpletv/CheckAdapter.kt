package ca.benoitjdumont.simpletv

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView

/** Checklist of apps for choosing what goes in a folder. */
class CheckAdapter(
    private val items: List<AppEntry>,
    private val selected: List<String>,
    private val onToggle: (String, Boolean) -> Unit
) : RecyclerView.Adapter<CheckAdapter.Holder>() {

    class Holder(val check: CheckBox) : RecyclerView.ViewHolder(check)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_check, parent, false) as CheckBox
        return Holder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        holder.check.setOnCheckedChangeListener(null)
        holder.check.text = item.label
        holder.check.isChecked = item.key in selected
        holder.check.setOnCheckedChangeListener { _, isChecked -> onToggle(item.key, isChecked) }
    }
}
