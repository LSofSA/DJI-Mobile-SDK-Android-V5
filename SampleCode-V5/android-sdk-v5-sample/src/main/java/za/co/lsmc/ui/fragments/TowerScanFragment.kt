package za.co.lsmc.ui.fragments

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dji.sampleV5.aircraft.R
import za.co.lsmc.data.database.SiteSurveyDbHelper
import za.co.lsmc.models.HeadFrameCaptureStep
import za.co.lsmc.models.OrbitRadiusStep
import za.co.lsmc.models.OrbitStep
import za.co.lsmc.models.PoiCenterStep
import za.co.lsmc.models.TowerScanStep
import za.co.lsmc.models.enums.Category
import za.co.lsmc.viewmodels.LSSASiteSurveyViewModel

class TowerScanFragment : Fragment() {

    private lateinit var lssaTowerScanAbortButton: Button
    private lateinit var lssaTowerScanNextButton: Button
    private lateinit var lssaTowerScanActionButton: Button
    private lateinit var stepContainer: FrameLayout
    private lateinit var lssaTowerCaptureAltitudeTextView: TextView

    private lateinit var dbHelper: SiteSurveyDbHelper
    private val viewModel: LSSASiteSurveyViewModel by activityViewModels()

    private var currentAltitude: Double = 0.0
    private var currentStepIndex = 0
    private var selectedOptions: List<String> = emptyList()
    private var isNewScan: Boolean = true

    private lateinit var steps: List<TowerScanStep>
    private var currentStepView: View? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = SiteSurveyDbHelper(requireContext())

        arguments?.let {
            isNewScan = it.getBoolean("isNewScan", true)
            selectedOptions = it.getStringArrayList("selectedOptions") ?: emptyList()
        }

        initializeSteps()
    }

    private fun initializeSteps() {
        steps = listOf(
            HeadFrameCaptureStep(this, viewModel),
            PoiCenterStep(this, viewModel),
            OrbitRadiusStep(this, viewModel),
            OrbitStep(this, viewModel)
        )

        if (!isNewScan) {
            determineCurrentStep()
        }
    }

    private fun determineCurrentStep() {
        val db = dbHelper.readableDatabase
        db.use { database ->
            viewModel.site?.let { site ->
                val headFrames = dbHelper.getHeadFramesBySite(database, site.id)
                val photos = dbHelper.getPhotosBySite(database, site.id)
                val hasPOI = photos.any { it.category == Category.POI }

                currentStepIndex = when {
                    headFrames.isEmpty() -> 0
                    !hasPOI -> 1
                    else -> 2
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lssaTowerScanAbortButton = view.findViewById(R.id.lssaTowerScanAbortButton)
        lssaTowerScanNextButton = view.findViewById(R.id.lssaTowerScanNextButton)
        lssaTowerScanActionButton = view.findViewById(R.id.lssaTowerScanActionButton)
        stepContainer = view.findViewById(R.id.stepContainer)
        lssaTowerCaptureAltitudeTextView = view.findViewById(R.id.lssaTowerCaptureAltitudeTextView)

        setupListeners()
        startAltitudeUpdates()
        loadCurrentStep()
    }

    private fun loadCurrentStep() {
        if (currentStepIndex >= steps.size) return

        val currentStep = steps[currentStepIndex]

        currentStepView?.let { stepContainer.removeView(it) }

        val inflater = LayoutInflater.from(requireContext())
        currentStepView = inflater.inflate(currentStep.getLayoutId(), stepContainer, false)
        stepContainer.addView(currentStepView)

        currentStepView?.let { currentStep.setupView(it, this) }

        lssaTowerScanActionButton.text = currentStep.getActionButtonText()
        currentStep.onResume()
    }

    private fun setupListeners() {
        lssaTowerScanAbortButton.setOnClickListener {
            showAbortConfirmation()
        }

        lssaTowerScanNextButton.setOnClickListener {
            val currentStep = steps[currentStepIndex]
            if (currentStep.onNextButtonClick()) {
                proceedToNextStep()
            }
        }

        lssaTowerScanActionButton.setOnClickListener {
            steps[currentStepIndex].onActionButtonClick()
        }
    }

    private fun proceedToNextStep() {
        if (currentStepIndex < steps.size - 1) {
            steps[currentStepIndex].onPause()
            currentStepIndex++
            loadCurrentStep()
        } else {
            completeTowerScan()
        }
    }

    private fun completeTowerScan() {
        showCompletionDialog()
    }

    private fun showCompletionDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Tower Scan Complete")
            .setMessage("Tower scan has been completed successfully. All data has been saved.")
            .setPositiveButton("Return to Dashboard") { _, _ ->
                navigateBackToDashboard()
            }
            .setCancelable(false)
            .show()

        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(typedValue.data)
    }

    private fun navigateBackToDashboard() {
        findNavController().popBackStack()
    }

    fun getCurrentAltitude(): Double = currentAltitude

    fun refreshCurrentStep() {
        loadCurrentStep()
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
}


