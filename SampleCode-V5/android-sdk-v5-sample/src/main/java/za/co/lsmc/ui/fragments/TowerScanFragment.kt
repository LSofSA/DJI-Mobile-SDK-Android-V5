package za.co.lsmc.ui.fragments

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dji.sampleV5.aircraft.R
import za.co.lsmc.data.Category
import za.co.lsmc.data.TowerScanStepItem
import za.co.lsmc.data.database.SiteSurveyDbHelper
import za.co.lsmc.data.entities.HeadFrame
import za.co.lsmc.ui.adapters.HeadFrameListAdapter
import za.co.lsmc.ui.adapters.TowerScanStepAdapter
import za.co.lsmc.viewmodels.LSSASiteSurveyViewModel

class TowerScanFragment : Fragment() {

    private lateinit var lssaTowerScanAbortButton: Button
    private lateinit var lssaTowerScanNextButton: Button
    private lateinit var lssaTowerScanActionButton: Button
    private lateinit var recyclerViewHeadFrames: RecyclerView
    private lateinit var lssaTowerCaptureAltitudeTextView: TextView
    private lateinit var lssaTowerScanInstructionsTextView: TextView

    private lateinit var dbHelper: SiteSurveyDbHelper
    private lateinit var headFrameAdapter: HeadFrameListAdapter
    private lateinit var towerScanAdapter: TowerScanStepAdapter

    private val headFrames = mutableListOf<HeadFrame>()
    private var isNewScan: Boolean = true
    private val viewModel: LSSASiteSurveyViewModel by activityViewModels()
    private var currentAltitude: Double = 0.0
    private var currentStep = 0
    private var selectedOptions: List<String> = emptyList()
    private var isPOICaptureMode = false

    companion object {
        fun newInstance(): TowerScanFragment {
            return TowerScanFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.lssa_fragment_tower_scan_capture, container, false)
    }

    private fun handleScanType() {
        if (isNewScan) {
            currentStep = 0
        } else {
            loadExistingHeadFrames()
            determineCurrentStep()
        }
    }

    private fun determineCurrentStep() {
        val db = dbHelper.readableDatabase
        db.use { db ->
            viewModel.site?.let{
                val photos = dbHelper.getPhotosBySite(db, it.id)
                val hasPOI = photos.any { it.category == Category.POI }

                when {
                    headFrames.isEmpty() -> currentStep = 0
                    !hasPOI -> currentStep = 1
                    else -> currentStep = 2
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = SiteSurveyDbHelper(requireContext())

        arguments?.let{
            isNewScan = it.getBoolean("isNewScan", true)
            selectedOptions = it.getStringArrayList("selectedOptions") ?: emptyList()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lssaTowerScanAbortButton = view.findViewById(R.id.lssaTowerScanAbortButton)
        lssaTowerScanNextButton = view.findViewById(R.id.lssaTowerScanNextButton)
        lssaTowerScanActionButton = view.findViewById(R.id.lssaTowerScanActionButton)
        recyclerViewHeadFrames = view.findViewById(R.id.recyclerViewHeadFrames)
        lssaTowerCaptureAltitudeTextView = view.findViewById(R.id.lssaTowerCaptureAltitudeTextView)
        lssaTowerScanInstructionsTextView = view.findViewById(R.id.lssaTowerScanInstructionsTextView)

        setupRecyclerView()
        setupListeners()
        handleScanType()
        startAltitudeUpdates()
        updateStepDisplay()
    }

    private fun loadExistingHeadFrames() {
        val db = dbHelper.readableDatabase
        db.use { database ->
            viewModel.site?.let {
                val existingHeadFrames =  dbHelper.getHeadFramesBySite(database, it.id)
                headFrames.clear()
                if (existingHeadFrames.isNotEmpty()) {
                    headFrames.addAll(existingHeadFrames.sortedBy { it.altitudeM })
                }
                headFrameAdapter.notifyDataSetChanged()
                updateInstructionsVisibility()
            }
        }
    }

    private fun startAltitudeUpdates() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                currentAltitude += (Math.random() - 0.5) * 2.0
                if (currentAltitude < 10.0) currentAltitude = 10.0
                if (currentAltitude > 100.0) currentAltitude = 100.0

                updateAltitudeDisplay()
                handler.postDelayed(this, 500)
            }
        }
        handler.post(runnable)
    }

    private fun updateAltitudeDisplay() {
        lssaTowerCaptureAltitudeTextView.text = String.format("%.2fm", currentAltitude)
    }

    private fun updateInstructionsVisibility() {
        if (headFrames.isEmpty()) {
            lssaTowerScanInstructionsTextView.visibility = View.VISIBLE
            recyclerViewHeadFrames.visibility = View.GONE
        } else {
            lssaTowerScanInstructionsTextView.visibility = View.GONE
            recyclerViewHeadFrames.visibility = View.VISIBLE
        }
    }

    private fun showDeleteHeadFrameConfirmation(headFrame: HeadFrame) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete Head Frame")
            .setMessage( String.format("Are you sure you want to delete head frame at %.2fm?",headFrame.altitudeM) )
            .setPositiveButton("Delete") { _, _ ->
                deleteHeadFrame(headFrame)
            }
            .setNegativeButton("Cancel", null)
            .show()

        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(typedValue.data)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(typedValue.data)
    }

    private fun updateStepDisplay() {
        if (isPOICaptureMode) {
            setupPOICaptureMode()
        } else {
            when (currentStep) {
                0 -> setupHeadFrameStep()
                1 -> setupPOIStep()
                2 -> setupRadiusStep()
                3 -> setupOrbitStep()
            }
        }
    }

    private fun setupHeadFrameStep() {
        Log.d("TowerScan", "setupHeadFrameStep - headFrames size: ${headFrames.size}")

        if (headFrames.isEmpty()) {
            lssaTowerScanInstructionsTextView.visibility = View.VISIBLE
            recyclerViewHeadFrames.visibility = View.GONE
        } else {
            lssaTowerScanInstructionsTextView.visibility = View.GONE
            recyclerViewHeadFrames.visibility = View.VISIBLE
            headFrameAdapter.notifyDataSetChanged()
            val items = headFrames.mapIndexed { index, headFrame ->
                TowerScanStepItem.HeadFrameItem(headFrame, index + 1)
            }

            towerScanAdapter = TowerScanStepAdapter(items) { headFrame ->
                showDeleteHeadFrameConfirmation(headFrame)
            }
            recyclerViewHeadFrames.adapter = towerScanAdapter

        }

        lssaTowerScanActionButton.text = "+ Head Frame"
        lssaTowerScanActionButton.visibility = View.VISIBLE
        lssaTowerScanNextButton.text = "Next"
    }

    private fun setupPOIStep() {
        lssaTowerScanInstructionsTextView.visibility = View.GONE
        recyclerViewHeadFrames.visibility = View.VISIBLE

        val items = headFrames.mapIndexed { index, headFrame ->
            TowerScanStepItem.HeadFrameItem(headFrame, index + 1)
        }

        towerScanAdapter = TowerScanStepAdapter(items)
        recyclerViewHeadFrames.adapter = towerScanAdapter

        lssaTowerScanActionButton.text = "Set Center"
        lssaTowerScanActionButton.visibility = View.VISIBLE
    }

    private fun setupPOICaptureMode() {
        lssaTowerScanInstructionsTextView.visibility = View.GONE
        recyclerViewHeadFrames.visibility = View.VISIBLE

        val items = listOf(
            TowerScanStepItem.InfoItem("POI Center Capture Mode"),
            TowerScanStepItem.InfoItem("Position drone at POI center"),
            TowerScanStepItem.InfoItem("Take photo when ready")
        )

        towerScanAdapter = TowerScanStepAdapter(items)
        recyclerViewHeadFrames.adapter = towerScanAdapter

        lssaTowerScanActionButton.text = "Capture POI"
        lssaTowerScanActionButton.visibility = View.VISIBLE
//        lssaTowerScanNextButton.text = "Cancel"
    }

    private fun setupRadiusStep() {
        lssaTowerScanInstructionsTextView.visibility = View.GONE
        recyclerViewHeadFrames.visibility = View.VISIBLE

        val items = listOf(
            TowerScanStepItem.InfoItem("${headFrames.size} Head Frames"),
            TowerScanStepItem.InfoItem("${calculateTotalOrbits()} Orbits"),
            TowerScanStepItem.InfoItem("${calculateEstimatedTime()}")
        )

        towerScanAdapter = TowerScanStepAdapter(items)
        recyclerViewHeadFrames.adapter = towerScanAdapter

        lssaTowerScanActionButton.text = "Set Radius"
        lssaTowerScanActionButton.visibility = View.VISIBLE
    }

    private fun setupOrbitStep() {
        lssaTowerScanInstructionsTextView.visibility = View.GONE
        recyclerViewHeadFrames.visibility = View.VISIBLE

        val items = listOf(
            TowerScanStepItem.InfoItem("Orbit configuration"),
            TowerScanStepItem.InfoItem("Ready to start")
        )

        towerScanAdapter = TowerScanStepAdapter(items)
        recyclerViewHeadFrames.adapter = towerScanAdapter

        lssaTowerScanActionButton.visibility = View.GONE
    }

    private fun calculateTotalOrbits(): Int {
        return headFrames.size * 4
    }

    private fun calculateEstimatedTime(): String {
        val minutes = headFrames.size * 2
        return "$minutes min $((minutes % 60) * 10) sec"
    }

    private fun deleteHeadFrame(headFrame: HeadFrame) {
        val db = dbHelper.writableDatabase
        try {
            dbHelper.deleteHeadFrame(db, headFrame)
            headFrameAdapter.removeHeadFrame(headFrame)
            Toast.makeText(requireContext(), "Head frame deleted", Toast.LENGTH_SHORT).show()
            updateInstructionsVisibility()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error deleting head frame: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            db.close()
        }
    }

    private fun showAbortConfirmation() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Abort Tower Scan")
            .setMessage("Are you sure you want to abort the tower scan? Any unsaved progress will be lost.")
            .setPositiveButton("Abort") { _, _ ->
                findNavController().popBackStack()
            }
            .setNegativeButton("Cancel", null)
            .show()

        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(typedValue.data)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(typedValue.data)
    }

    private fun proceedToNextStep() {
        when (currentStep) {
            0 -> {
                if (headFrames.isEmpty()) {
                    Toast.makeText(requireContext(), "Please add at least one head frame before proceeding", Toast.LENGTH_SHORT).show()
                    return
                }
                currentStep = 1
                updateStepDisplay()
            }
            1 -> {
                checkPOICenterAndProceed()
            }
            2 -> {
                proceedToOrbitalRadius()
            }
            3 -> {
                proceedToOrbits()
            }
        }
    }

    private fun proceedToOrbits() {
        Toast.makeText(requireContext(), "Proceeding to Orbits configuration", Toast.LENGTH_SHORT).show()
    }

    private fun checkPOICenterAndProceed() {
        val db = dbHelper.readableDatabase
        try {
            viewModel.site?.let{ site ->
                val photos = dbHelper.getPhotosBySite(db, site.id)
                val hasPOIPhoto = photos.any { it.category == Category.POI }

                if (hasPOIPhoto) {
                    currentStep = 2
                    updateAltitudeDisplay()
                } else {
                    Toast.makeText(requireContext(), "Please take a POI photo first", Toast.LENGTH_SHORT).show()
                }
            }
        } finally {
            db.close()
        }
    }


    private fun setupRecyclerView() {
        headFrameAdapter = HeadFrameListAdapter(
            headFrames = headFrames,
            onDeleteClick = { headFrame ->
                showDeleteHeadFrameConfirmation(headFrame)}
        )

        recyclerViewHeadFrames.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewHeadFrames.adapter = headFrameAdapter  // ADD THIS LINE
    }


    private fun proceedToOrbitalRadius() {
        val bundle = Bundle().apply {
            viewModel.site?.id?.let { putLong("siteId", it) }
            putInt("headFrameCount", headFrames.size)
        }

        Toast.makeText(requireContext(), "Proceeding to Orbital Radius setup", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to orbital radius fragment
        // findNavController().navigate(R.id.action_towerScanFragment_to_orbitalRadiusFragment, bundle)
    }

    override fun onResume() {
        super.onResume()
        startAltitudeUpdates()
    }

    private fun setupListeners() {
        lssaTowerScanAbortButton.setOnClickListener {
            showAbortConfirmation()
        }

        lssaTowerScanNextButton.setOnClickListener {
            if (isPOICaptureMode) {
                cancelPOICapture()
            } else {
                proceedToNextStep()
            }
        }

        lssaTowerScanActionButton.setOnClickListener {
            if (isPOICaptureMode) {
                capturePOIPhoto()
            } else {
                when (currentStep) {
                    0 -> addCurrentHeadFrame()
                    1 -> enterPOICaptureMode()
                    2 -> setRadius()
                    3 -> configureOrbits()
                }
            }
        }
    }

    private fun setRadius() {
        Toast.makeText(requireContext(), "Still lazy, must do this later", Toast.LENGTH_SHORT).show()
    }

    private fun configureOrbits() {
        Toast.makeText(requireContext(), "Still lazy, must do this later", Toast.LENGTH_SHORT).show()
    }

    private fun enterPOICaptureMode() {
        isPOICaptureMode = true
        updateStepDisplay()
        Toast.makeText(requireContext(), "POI Capture Mode - Position drone at center point", Toast.LENGTH_LONG).show()
    }

    private fun cancelPOICapture() {
        isPOICaptureMode = false
        updateStepDisplay()
    }

    private fun capturePOIPhoto() {
        val db = dbHelper.writableDatabase
        try {
            viewModel.site?.let{
                val newPhoto = dbHelper.insertPhoto(db, it.id, "poi_${System.currentTimeMillis()}.jpg", Category.POI, 0.0)
                isPOICaptureMode = false
                updateStepDisplay()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error capturing POI photo: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            db.close()
        }
    }

    private fun addCurrentHeadFrame() {
        if (currentAltitude <= 0) {
            Toast.makeText(requireContext(), "Invalid altitude", Toast.LENGTH_SHORT).show()
            return
        }

        if (headFrames.any { kotlin.math.abs(it.altitudeM - currentAltitude) < 0.001 }) {
            Toast.makeText(requireContext(), "Head frame already exists at this altitude", Toast.LENGTH_SHORT).show()
            return
        }

        val db = dbHelper.writableDatabase
        try {

            viewModel.site?.let {
                val newHeadFrame = dbHelper.insertHeadFrame(db, it.id, currentAltitude)
                headFrameAdapter.addHeadFrame(newHeadFrame)
                updateInstructionsVisibility()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error adding head frame: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            db.close()
        }
    }
}