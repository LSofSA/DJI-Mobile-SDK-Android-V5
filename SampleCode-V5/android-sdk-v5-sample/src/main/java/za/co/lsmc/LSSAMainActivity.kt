package za.co.lsmc

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import dji.sampleV5.aircraft.R
import za.co.lsmc.fragments.SiteDashboardFragment
import za.co.lsmc.fragments.SiteListFragment
import za.co.lsmc.viewmodels.LSSASiteSurveyViewModel

class LSSAMainActivity : AppCompatActivity() {

    // methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lssa_main_activity)
    }

    fun loadSiteDashboard(siteId: Long) {
        val navController = findNavController(R.id.navLssaMainFragContainerView)
        val bundle = Bundle().apply {
            putLong("siteId", siteId)
        }
        navController.navigate(R.id.action_siteList_to_siteDashboard, bundle)
    }
}