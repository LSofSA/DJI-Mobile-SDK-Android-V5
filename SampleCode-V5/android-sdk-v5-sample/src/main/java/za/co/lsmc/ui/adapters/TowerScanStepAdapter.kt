package za.co.lsmc.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dji.sampleV5.aircraft.R
import za.co.lsmc.data.entities.HeadFrame
import za.co.lsmc.data.TowerScanStepItem
import java.util.Locale

class TowerScanStepAdapter(
    private val items: List<TowerScanStepItem>,
    private val onDeleteClick: ((HeadFrame) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_HEAD_FRAME = 0
        const val TYPE_INFO_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is TowerScanStepItem.HeadFrameItem -> TYPE_HEAD_FRAME
            is TowerScanStepItem.InfoItem -> TYPE_INFO_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEAD_FRAME -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.lssa_item_headframe, parent, false)
                HeadFrameViewHolder(view)
            }
            TYPE_INFO_ITEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.lssa_item_tower_scan_info, parent, false)
                InfoViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is TowerScanStepItem.HeadFrameItem -> {
                val headFrameHolder = holder as HeadFrameViewHolder
                headFrameHolder.bind(item.headFrame, item.number, onDeleteClick)
            }
            is TowerScanStepItem.InfoItem -> {
                val infoHolder = holder as InfoViewHolder
                infoHolder.bind(item.text)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class HeadFrameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewHeadFrameNumber: TextView = itemView.findViewById(R.id.lssaTextViewHeadFrameNumber)
        private val textViewHeadFrameAltitude: TextView = itemView.findViewById(R.id.lssaTextViewHeadFrameAltitude)
        private val buttonDeleteHeadFrame: Button = itemView.findViewById(R.id.lssaButtonDeleteHeadFrame)

        fun bind(headFrame: HeadFrame, number: Int, onDeleteClick: ((HeadFrame) -> Unit)?) {
            textViewHeadFrameNumber.text = "Head Frame $number"
            textViewHeadFrameAltitude.text = String.format("%.2f m", headFrame.altitudeM, Locale.getDefault())

            if (onDeleteClick != null) {
                buttonDeleteHeadFrame.visibility = View.VISIBLE
                buttonDeleteHeadFrame.setOnClickListener {
                    onDeleteClick(headFrame)
                }
            } else {
                buttonDeleteHeadFrame.visibility = View.GONE
            }
        }
    }

    class InfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewInfo: TextView = itemView.findViewById(R.id.lssaTextViewInfo)

        fun bind(text: String) {
            textViewInfo.text = text
        }
    }
}

