package za.co.lsmc.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dji.sampleV5.aircraft.R
import za.co.lsmc.data.Site
import java.text.SimpleDateFormat
import java.util.Locale

class SiteListAdapter(
    private val sites: List<Site>,
    private val onSiteClick: (Site) -> Unit,
    private val onDeleteClick: (Site) -> Unit
) : RecyclerView.Adapter<SiteListAdapter.SiteViewHolder>() {

    class SiteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lssaTextViewSiteName: TextView = itemView.findViewById(R.id.lssaTextViewSiteName)
        val lssaTextViewSiteDate: TextView = itemView.findViewById(R.id.lssaTextViewSiteDate)
        val lssaButtonDeleteSite: ImageButton = itemView.findViewById(R.id.lssaButtonDeleteSite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lssa_item_site, parent, false)
        return SiteViewHolder(view)
    }

    override fun onBindViewHolder(holder: SiteViewHolder, position: Int) {
        val site = sites[position]

        holder.lssaTextViewSiteName.text = site.name
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        holder.lssaTextViewSiteDate.text = site.completed?.let { dateFormat.format(it) } ?: "Completed"

        holder.itemView.setOnClickListener {
            onSiteClick(site)
        }

        holder.lssaButtonDeleteSite.setOnClickListener {
            onDeleteClick(site)
        }
    }

    override fun getItemCount(): Int = sites.size
}