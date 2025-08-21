package za.co.lsmc.models

import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dji.sampleV5.aircraft.R
import kotlinx.coroutines.launch
import za.co.lsmc.data.entities.HeadFrame
import za.co.lsmc.ui.adapters.TowerScanStepAdapter
import za.co.lsmc.ui.fragments.TowerScanFragment
import za.co.lsmc.viewmodels.LSSASiteSurveyViewModel

class OrbitRadiusStep(
    private val fragment: TowerScanFragment,
    private val viewModel: LSSASiteSurveyViewModel
) : TowerScanStep {

    private lateinit var recyclerView: RecyclerView
    private val headFrames = mutableListOf<HeadFrame>()

    override fun getActionButtonText(): String = "Set Radius"

    override fun getLayoutId(): Int = R.layout.lssa_layout_step_orbit_radius

    override fun setupView(view: View, fragment: TowerScanFragment) {
        recyclerView = view.findViewById(R.id.recyclerViewRadius)

        viewModel.headFrames.observe(fragment.viewLifecycleOwner) { headFrameList ->
            headFrames.clear()
            headFrames.addAll(headFrameList.sortedBy { it.altitudeM })
            updateDisplay()
        }

        fragment.lifecycleScope.launch {
            viewModel.refreshHeadFrames()
        }
    }

    private fun updateDisplay() {
        val items = listOf(
            TowerScanStepItem.InfoItem("${headFrames.size} Head Frames"),
            TowerScanStepItem.InfoItem("${calculateTotalOrbits()} Orbits"),
            TowerScanStepItem.InfoItem("${calculateEstimatedTime()}")
        )

        val adapter = TowerScanStepAdapter(items)
        recyclerView.layoutManager = LinearLayoutManager(fragment.requireContext())
        recyclerView.adapter = adapter
    }

    private fun calculateTotalOrbits(): Int = headFrames.size * 4

    private fun calculateEstimatedTime(): String {
        val minutes = headFrames.size * 2
        return "$minutes min $((minutes % 60) * 10) sec"
    }

    override fun onActionButtonClick() {
        Toast.makeText(fragment.requireContext(), "Setting radius...", Toast.LENGTH_SHORT).show()
        // TODO: Implement radius setting logic
    }

    override fun onNextButtonClick(): Boolean = true
}