package za.co.lsmc.fragments

import za.co.lsmc.data.Site
import za.co.lsmc.data.SiteSurveyDbHelper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import dji.sampleV5.aircraft.R
import java.text.SimpleDateFormat

class SiteDashboardFragment : Fragment() {

    private lateinit var dbHelper: SiteSurveyDbHelper
    private var siteId: Long = -1
    private var site: Site? = null

    private lateinit var lssaTextViewSiteName: TextView
    private lateinit var lssaTextViewSiteStatus: TextView
    private lateinit var lssaTextViewPhotoCount: TextView
    private lateinit var lssaTextViewHeadFrameCount: TextView

    companion object {
        private const val ARG_SITE_ID = "site_id"

        fun newInstance(siteId: Long): SiteDashboardFragment {
            val fragment = SiteDashboardFragment()
            val args = Bundle()
            args.putLong(ARG_SITE_ID, siteId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            siteId = it.getLong(ARG_SITE_ID)
        }
        dbHelper = SiteSurveyDbHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.lssa_fragment_site_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lssaTextViewSiteName = view.findViewById(R.id.lssaTextViewSiteName)
        lssaTextViewSiteStatus = view.findViewById(R.id.lssaTextViewSiteStatus)
        lssaTextViewPhotoCount = view.findViewById(R.id.lssaTextViewPhotoCount)
        lssaTextViewHeadFrameCount = view.findViewById(R.id.lssaTextViewHeadFrameCount)

        loadSiteData()
    }

    private fun loadSiteData() {
        val db = dbHelper.readableDatabase

        try {
            site = dbHelper.getSite(db, siteId)
            site?.let { s ->
                lssaTextViewSiteName.text = s.name
                lssaTextViewSiteStatus.text = if (s.completed != null) "Completed" else SimpleDateFormat("MMM dd, yyyy").format(s.completed)

                val photos = dbHelper.getPhotosBySite(db, siteId)
                val headFrames = dbHelper.getHeadFramesBySite(db, siteId)

                lssaTextViewPhotoCount.text = "Total Photos: ${photos.size}"
                lssaTextViewHeadFrameCount.text = "Total Head Frames: ${headFrames.size}"
            }
        }
        finally {
            db.close()
        }
    }
}