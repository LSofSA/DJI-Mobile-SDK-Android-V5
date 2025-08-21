package za.co.lsmc.models

import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dji.sampleV5.aircraft.R
import za.co.lsmc.ui.adapters.TowerScanStepAdapter
import za.co.lsmc.ui.fragments.TowerScanFragment
import za.co.lsmc.viewmodels.LSSASiteSurveyViewModel

class OrbitStep(
    private val fragment: TowerScanFragment,
    private val viewModel: LSSASiteSurveyViewModel
) : TowerScanStep {

    private lateinit var recyclerView: RecyclerView
    private var isOrbitStarted = false
    private var isOrbitComplete = false

    override fun getActionButtonText(): String = when {
        isOrbitComplete -> "Complete"
        isOrbitStarted -> "Stop Orbit"
        else -> "Start Orbit"
    }

    override fun getLayoutId(): Int = R.layout.lssa_layout_step_orbit

    override fun setupView(view: View, fragment: TowerScanFragment) {
        recyclerView = view.findViewById(R.id.recyclerViewOrbit)
        updateDisplay()
    }

    private fun updateDisplay() {
        val items = if (isOrbitComplete) {
            listOf(
                TowerScanStepItem.InfoItem("✓ Orbit Complete"),
                TowerScanStepItem.InfoItem("All photos captured"),
                TowerScanStepItem.InfoItem("Ready to finish")
            )
        } else if (isOrbitStarted) {
            listOf(
                TowerScanStepItem.InfoItem("Orbit in progress..."),
                TowerScanStepItem.InfoItem("Capturing photos"),
                TowerScanStepItem.InfoItem("Please wait")
            )
        } else {
            listOf(
                TowerScanStepItem.InfoItem("Orbit configuration ready"),
                TowerScanStepItem.InfoItem("Ready to start capture")
            )
        }

        val adapter = TowerScanStepAdapter(items)
        recyclerView.layoutManager = LinearLayoutManager(fragment.requireContext())
        recyclerView.adapter = adapter
    }

    override fun onActionButtonClick() {
        when {
            isOrbitComplete -> {
                Toast.makeText(fragment.requireContext(), "Completing tower scan...", Toast.LENGTH_SHORT).show()
            }
            isOrbitStarted -> {
                stopOrbit()
            }
            else -> {
                startOrbit()
            }
        }
    }

    private fun startOrbit() {
        isOrbitStarted = true
        updateDisplay()
        fragment.refreshCurrentStep()
        simulateOrbitProcess()

        Toast.makeText(fragment.requireContext(), "Starting orbit capture...", Toast.LENGTH_SHORT).show()
    }

    private fun stopOrbit() {
        isOrbitStarted = false
        updateDisplay()
        fragment.refreshCurrentStep()
        Toast.makeText(fragment.requireContext(), "Orbit stopped", Toast.LENGTH_SHORT).show()
    }

    private fun simulateOrbitProcess() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.postDelayed({
            if (isOrbitStarted) {
                completeOrbit()
            }
        }, 5000)
    }

    private fun completeOrbit() {
        isOrbitStarted = false
        isOrbitComplete = true

        saveOrbitCompletion()

        updateDisplay()
        fragment.refreshCurrentStep()

        Toast.makeText(fragment.requireContext(), "Orbit capture complete!", Toast.LENGTH_LONG).show()
    }

    private fun saveOrbitCompletion() {

    }

    override fun onNextButtonClick(): Boolean {
        return if (isOrbitComplete) {
            true
        } else {
            Toast.makeText(fragment.requireContext(), "Please complete the orbit first", Toast.LENGTH_SHORT).show()
            false
        }
    }
}