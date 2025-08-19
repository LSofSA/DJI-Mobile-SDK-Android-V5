package za.co.lsmc.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import dji.sampleV5.aircraft.R
import za.co.lsmc.models.SectionStatus

class SectionStatusAdapter(
    private val context: Context,
    private val sections: List<SectionStatus>
) : BaseAdapter() {

    override fun getCount(): Int = sections.size

    override fun getItem(position: Int): SectionStatus = sections[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.lssa_item_section_status, parent, false)

        val section = sections[position]
        val statusIcon = view.findViewById<ImageView>(R.id.lssaImageViewSectionStatus)
        val sectionName = view.findViewById<TextView>(R.id.lssaTextViewSectionName)

        sectionName.text = section.name

        if(section.isParent) {
            statusIcon.visibility = View.GONE
        } else {
            statusIcon.visibility = View.VISIBLE

            val statusDrawable = if (section.isCompleted) R.drawable.ic_check else R.drawable.ic_close
            val statusColor = ContextCompat.getColor(context, if (section.isCompleted) R.color.green else R.color.red)

            statusIcon.setImageResource(statusDrawable)
            statusIcon.setColorFilter(statusColor)
        }

        return view
    }
}