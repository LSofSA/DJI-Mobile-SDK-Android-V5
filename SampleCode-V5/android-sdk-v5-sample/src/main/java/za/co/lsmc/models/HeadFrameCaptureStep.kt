package za.co.lsmc.models

import android.util.TypedValue
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dji.sampleV5.aircraft.R
import kotlinx.coroutines.launch
import za.co.lsmc.data.entities.HeadFrame
import za.co.lsmc.ui.adapters.HeadFrameListAdapter
import za.co.lsmc.ui.fragments.TowerScanFragment
import za.co.lsmc.viewmodels.LSSASiteSurveyViewModel

class HeadFrameCaptureStep(
    private val fragment: TowerScanFragment,
    private val viewModel: LSSASiteSurveyViewModel
) : TowerScanStep {

    private lateinit var recyclerView: RecyclerView
    private lateinit var instructionsTextView: TextView
    private lateinit var headFrameAdapter: HeadFrameListAdapter
    private val headFrames = mutableListOf<HeadFrame>()

    override fun getActionButtonText(): String = "+ Head Frame"

    override fun getLayoutId(): Int = R.layout.lssa_layout_step_headframe_capture

    override fun setupView(view: View, fragment: TowerScanFragment) {
        recyclerView = view.findViewById(R.id.recyclerViewHeadFrames)
        instructionsTextView = view.findViewById(R.id.instructionsTextView)

        setupRecyclerView()

        viewModel.headFrames.observe(fragment.viewLifecycleOwner) { headFrameList ->
            headFrames.clear()
            headFrames.addAll(headFrameList.sortedBy { it.altitudeM })
            headFrameAdapter.notifyDataSetChanged()
            updateInstructionsVisibility()
        }

        fragment.lifecycleScope.launch {
            viewModel.refreshHeadFrames()
        }
    }

    private fun setupRecyclerView() {
        headFrameAdapter = HeadFrameListAdapter(
            headFrames = headFrames,
            onDeleteClick = { headFrame ->
                showDeleteHeadFrameConfirmation(headFrame)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(fragment.requireContext())
        recyclerView.adapter = headFrameAdapter

        updateInstructionsVisibility()
    }

    private fun updateInstructionsVisibility() {
        if (headFrames.isEmpty()) {
            instructionsTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            instructionsTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    override fun onActionButtonClick() {
        val currentAltitude = fragment.getCurrentAltitude()

        if (currentAltitude <= 0) {
            Toast.makeText(fragment.requireContext(), "Invalid altitude", Toast.LENGTH_SHORT).show()
            return
        }

        fragment.lifecycleScope.launch {
            val result = viewModel.addHeadFrame(currentAltitude)
            if (result.isSuccess) {
                Toast.makeText(fragment.requireContext(), "Head frame added at ${String.format("%.2f", currentAltitude)}m", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(fragment.requireContext(), result.exceptionOrNull()?.message ?: "Error adding head frame", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNextButtonClick(): Boolean {
        if (headFrames.isEmpty()) {
            Toast.makeText(fragment.requireContext(), "Please add at least one head frame before proceeding", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun showDeleteHeadFrameConfirmation(headFrame: HeadFrame) {
        val dialog = AlertDialog.Builder(fragment.requireContext())
            .setTitle("Delete Head Frame")
            .setMessage(String.format("Are you sure you want to delete head frame at %.2fm?", headFrame.altitudeM))
            .setPositiveButton("Delete") { _, _ ->
                deleteHeadFrame(headFrame)
            }
            .setNegativeButton("Cancel", null)
            .show()

        val typedValue = TypedValue()
        fragment.requireContext().theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(typedValue.data)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(typedValue.data)
    }

    private fun deleteHeadFrame(headFrame: HeadFrame) {
        fragment.lifecycleScope.launch {
            val result = viewModel.deleteHeadFrame(headFrame)
            if (result.isSuccess) {
                Toast.makeText(fragment.requireContext(), "Head frame deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(fragment.requireContext(), "Error deleting head frame: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}