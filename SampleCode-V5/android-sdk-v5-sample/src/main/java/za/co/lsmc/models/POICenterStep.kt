package za.co.lsmc.models

import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dji.sampleV5.aircraft.R
import kotlinx.coroutines.launch
import za.co.lsmc.data.entities.HeadFrame
import za.co.lsmc.models.enums.Category
import za.co.lsmc.ui.adapters.TowerScanStepAdapter
import za.co.lsmc.ui.fragments.TowerScanFragment
import za.co.lsmc.viewmodels.LSSASiteSurveyViewModel

class POICenterStep(
    private val fragment: TowerScanFragment,
    private val viewModel: LSSASiteSurveyViewModel
) : TowerScanStep {

    private lateinit var recyclerView: RecyclerView
    private val headFrames = mutableListOf<HeadFrame>()
    private var isPOICaptureMode = false

    override fun getActionButtonText(): String =
        if (isPOICaptureMode) "Capture POI" else "Set Center"

    override fun getLayoutId(): Int = R.layout.lssa_layout_step_poi_center

    override fun setupView(view: View, fragment: TowerScanFragment) {
        recyclerView = view.findViewById(R.id.recyclerViewPOI)

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
        val items = if (isPOICaptureMode) {
            listOf(
                TowerScanStepItem.InfoItem("POI Center Capture Mode"),
                TowerScanStepItem.InfoItem("Position drone at POI center"),
                TowerScanStepItem.InfoItem("Take photo when ready")
            )
        } else {
            headFrames.mapIndexed { index, headFrame ->
                TowerScanStepItem.HeadFrameItem(headFrame, index + 1)
            }
        }

        val adapter = TowerScanStepAdapter(items)
        recyclerView.layoutManager = LinearLayoutManager(fragment.requireContext())
        recyclerView.adapter = adapter
    }

    override fun onActionButtonClick() {
            capturePOIPhoto()
    }

    private fun capturePOIPhoto() {
        fragment.lifecycleScope.launch {
            try {
                val result = viewModel.capturePhoto("poi_${System.currentTimeMillis()}.jpg", Category.POI)

                if (result.isSuccess) {
                    isPOICaptureMode = false
                    updateDisplay()
                    fragment.refreshCurrentStep()
                    Toast.makeText(
                        fragment.requireContext(),
                        "POI photo captured (replaced any existing POI photo)",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        fragment.requireContext(),
                        "Error capturing POI photo: ${result.exceptionOrNull()?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    fragment.requireContext(),
                    "Error capturing POI photo: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onNextButtonClick(): Boolean {
        if (isPOICaptureMode) {
            isPOICaptureMode = false
            updateDisplay()
            fragment.refreshCurrentStep()
            return false
        }

        fragment.lifecycleScope.launch {
            try {
                val hasPOI = viewModel.hasPhotoForCategory(Category.POI)
                if (!hasPOI) {
                    Toast.makeText(fragment.requireContext(), "Please take a POI photo first", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(fragment.requireContext(), "Error checking POI photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }
}