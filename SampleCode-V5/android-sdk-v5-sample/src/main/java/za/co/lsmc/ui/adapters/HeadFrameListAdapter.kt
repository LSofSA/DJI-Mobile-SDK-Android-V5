package za.co.lsmc.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dji.sampleV5.aircraft.R
import za.co.lsmc.data.entities.HeadFrame
import java.util.Locale

class HeadFrameListAdapter(
    private val headFrames: MutableList<HeadFrame>,
    private val onDeleteClick: (HeadFrame) -> Unit,
) : RecyclerView.Adapter<HeadFrameListAdapter.HeadFrameViewHolder>() {

    class HeadFrameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewHeadFrameNumber: TextView = itemView.findViewById(R.id.lssaTextViewHeadFrameNumber)
        val textViewHeadFrameAltitude: TextView = itemView.findViewById(R.id.lssaTextViewHeadFrameAltitude)
        val buttonDeleteHeadFrame: Button = itemView.findViewById(R.id.lssaButtonDeleteHeadFrame)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeadFrameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lssa_item_headframe, parent, false)
        return HeadFrameViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeadFrameViewHolder, position: Int) {
        val headFrame = headFrames[position]

        holder.textViewHeadFrameNumber.text = "Head Frame ${position + 1}"
        holder.textViewHeadFrameAltitude.text = String.format("%.2f m",headFrame.altitudeM, Locale.getDefault())

        holder.buttonDeleteHeadFrame.setOnClickListener {
            onDeleteClick(headFrame)
        }
    }

    override fun getItemCount(): Int = headFrames.size

    fun addHeadFrame(headFrame: HeadFrame) {
        val insertPosition = findInsertPosition(headFrame.altitudeM)
        headFrames.add(insertPosition, headFrame)
        notifyItemInserted(insertPosition)
        notifyItemRangeChanged(insertPosition, headFrames.size - insertPosition)
    }

    fun removeHeadFrame(headFrame: HeadFrame) {
        val position = headFrames.indexOf(headFrame)
        if (position >= 0) {
            headFrames.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, headFrames.size - position)
        }
    }

    private fun findInsertPosition(altitude: Double): Int {
        for (i in headFrames.indices) {
            if (altitude > headFrames[i].altitudeM) {
                return i
            }
        }
        return headFrames.size
    }

    fun updateHeadFrames(newHeadFrames: List<HeadFrame>) {
        headFrames.clear()
        headFrames.addAll(newHeadFrames.sortedBy { it.altitudeM })
        notifyDataSetChanged()
    }
}