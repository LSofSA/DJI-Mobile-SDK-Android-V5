package za.co.lsmc.ui.fragments

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.GridView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dji.sampleV5.aircraft.R
import za.co.lsmc.models.enums.Category
import za.co.lsmc.data.database.SiteSurveyDbHelper
import za.co.lsmc.data.entities.Photo
import za.co.lsmc.models.ActionButton
import za.co.lsmc.models.SectionStatus
import za.co.lsmc.ui.adapters.ActionButtonAdapter
import za.co.lsmc.ui.adapters.SectionStatusAdapter
import za.co.lsmc.ui.dialogs.PhotoGalleryDialog
import za.co.lsmc.viewmodels.LSSASiteSurveyViewModel

class SiteDashboardFragment : Fragment() {

    private val viewModel: LSSASiteSurveyViewModel by activityViewModels()

    private lateinit var lssaTextViewSiteName: TextView
    private lateinit var lssaSiteDashboardButtonBack: Button
    private lateinit var lssaButtonDelete: Button
    private lateinit var lssaListViewSections: ListView
    private lateinit var lssaGridViewActions: GridView

    private lateinit var sectionStatusAdapter: SectionStatusAdapter
    private lateinit var actionButtonAdapter: ActionButtonAdapter

    private var photos: List<Photo> = emptyList()

    companion object {
        fun newInstance(): SiteDashboardFragment {
            return SiteDashboardFragment()
        }
        private const val TAG = "SiteDashboardFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView called")
        return inflater.inflate(R.layout.lssa_fragment_site_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        lssaTextViewSiteName = view.findViewById(R.id.lssaTextViewSiteName)
        lssaSiteDashboardButtonBack = view.findViewById(R.id.lssaSiteDashboardButtonBack)
        lssaButtonDelete = view.findViewById(R.id.lssaButtonDelete)
        lssaListViewSections = view.findViewById(R.id.lssaListViewSections)
        lssaGridViewActions = view.findViewById(R.id.lssaGridViewActions)

        setupAdapters()
        setupClickListeners()
        loadSiteData()
        observePhotos()
    }

    private fun setupAdapters() {
        Log.d(TAG, "setupAdapters called")
        val sections = createSectionsList()
        Log.d(TAG, "Created ${sections.size} sections")

        sectionStatusAdapter = SectionStatusAdapter(requireContext(), sections)
        lssaListViewSections.adapter = sectionStatusAdapter

        lssaListViewSections.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            Log.d(TAG, "ListView item clicked at position: $position")
            val section = sections[position]
            Log.d(TAG, "Clicked section: ${section.name}, isParent: ${section.isParent}, category: ${section.category}")

            if (!section.isParent && section.category != null) {
                Log.d(TAG, "Valid section clicked, showing photo gallery for category: ${section.category}")
                showPhotoGallery(section.category)
            } else {
                Log.d(TAG, "Section is parent or has no category, ignoring click")
            }
        }

        val actions = createActionsList()
        actionButtonAdapter = ActionButtonAdapter(requireContext(), actions) { action ->
            action.action()
        }
        lssaGridViewActions.adapter = actionButtonAdapter
    }

    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners called")
        lssaSiteDashboardButtonBack.setOnClickListener {
            Log.d(TAG, "Back button clicked")
            findNavController().popBackStack()
        }

        lssaButtonDelete.setOnClickListener {
            Log.d(TAG, "Delete button clicked")
            showDeleteConfirmation()
        }
    }

    private fun observePhotos() {
        Log.d(TAG, "observePhotos called")
        viewModel.getPhotosForCurrentSite().observe(viewLifecycleOwner) { photoList ->
            Log.d(TAG, "Photos observed: ${photoList.size} photos")
            photos = photoList
            updateSectionStatuses()
        }
    }

    private fun showPhotoGallery(category: Category) {
        Log.d(TAG, "showPhotoGallery called for category: $category")
        try {
            val dialog = PhotoGalleryDialog(
                requireContext(),
                category,
                viewModel
            ) { selectedCategory ->
                Log.d(TAG, "Take photos clicked for category: $selectedCategory")
                onSectionClicked(selectedCategory)
            }
            Log.d(TAG, "PhotoGalleryDialog created, showing...")
            dialog.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing photo gallery", e)
            Toast.makeText(requireContext(), "Error showing photo gallery: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onSectionClicked(category: Category) {
        Log.d(TAG, "onSectionClicked called for category: $category")
        when (category) {
            Category.POI -> startPOIPhoto()
            Category.TSO -> startTOSPhotos()
            Category.HF_DOWN -> startHeadFramePhoto("DOWN")
            Category.HF_LEVEL -> startHeadFramePhoto("LEVEL")
            Category.HF_PANORAMIC -> startHeadFramePhoto("PANORAMIC")
            Category.HF_UP -> startHeadFramePhoto("UP")
            Category.ACCESS_ROUTE -> startAccessRoadPhotos()
            Category.FEEDER_RUN -> startFeederRunPhotos()
        }
    }

    private fun createSectionsList(): List<SectionStatus> {
        Log.d(TAG, "createSectionsList called")
        return listOf(
            SectionStatus("POI Center Photo", Category.POI, hasPhotoForCategory(Category.POI), false, 0),
            SectionStatus("TSO Orbit Photos", Category.TSO, hasPhotoForCategory(Category.TSO), false, 0),
            SectionStatus("Access Route / Road Photos", Category.ACCESS_ROUTE, hasPhotoForCategory(
                Category.ACCESS_ROUTE), false, 0),
            SectionStatus("Feeder Run (Ladder) Photos", Category.FEEDER_RUN, hasPhotoForCategory(
                Category.FEEDER_RUN), false, 0),
            SectionStatus("Head Frame Photos", null, false, true, 0),
            SectionStatus("   Down Orbit", Category.HF_DOWN, hasPhotoForCategory(Category.HF_DOWN), false, 1),
            SectionStatus("   Level Orbit", Category.HF_LEVEL, hasPhotoForCategory(Category.HF_LEVEL), false, 1),
            SectionStatus("   Panoramic Orbit", Category.HF_PANORAMIC, hasPhotoForCategory(Category.HF_PANORAMIC), false, 1),
            SectionStatus("   Up Orbit", Category.HF_UP, hasPhotoForCategory(Category.HF_UP), false, 1)
        )
    }

    private fun createActionsList(): List<ActionButton> {
        val hasTowerScanData = photos.any { it.category == Category.TSO }
        val hasHeadFrames = viewModel.site?.let { site ->
            val db = SiteSurveyDbHelper(requireContext()).readableDatabase
            val headFrames = SiteSurveyDbHelper(requireContext()).getHeadFramesBySite(db, site.id)
            db.close()
            headFrames.isNotEmpty()
        } ?: false

        return listOf(
            ActionButton(
                title = "New Tower Scan",
                description = if (hasTowerScanData) "Start over with new scan" else "Start a new tower scan",
                isEnabled = true
            ) {
                if (hasTowerScanData) {
                    showNewScanConfirmation()
                } else {
                    startNewTowerScan()
                }
            },
            ActionButton(
                title = "Continue Tower Scan",
                description = when {
                    !hasHeadFrames -> "No scan data found"
                    hasTowerScanData -> "Scan appears complete"
                    else -> "Resume existing scan"
                },
                isEnabled = hasHeadFrames && !hasTowerScanData
            ) {
                continueTowerScan()
            },
            ActionButton(
                title = "POI Center Photo",
                description = "Take POI center shot",
                isEnabled = true
            ) {
                startPOIPhoto()
            },
            ActionButton(
                title = "Access Road Photos",
                description = "Document access routes",
                isEnabled = true
            ) {
                startAccessRoadPhotos()
            },
            ActionButton(
                title = "Cable Run / Ladder Photos",
                description = "Document feeder runs",
                isEnabled = true
            ) {
                startFeederRunPhotos()
            }
        )
    }

    private fun showNewScanConfirmation() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Start New Tower Scan")
            .setMessage("A tower scan already exists for this site. Starting a new scan will overwrite existing data. Continue?")
            .setPositiveButton("Start New") { _, _ ->
                startNewTowerScan()
            }
            .setNegativeButton("Cancel", null)
            .show()

        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(typedValue.data)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(typedValue.data)
    }

    private fun hasPhotoForCategory(category: Category): Boolean {
        return photos.any { photo -> photo.category == category }
    }

    private fun loadSiteData() {
        Log.d(TAG, "loadSiteData called")
        lssaTextViewSiteName.text = viewModel.site?.name ?: throw IllegalStateException("A site has not been selected.")
        viewModel.initializePhotoCounter()
    }

    private fun updateSectionStatuses() {
        Log.d(TAG, "updateSectionStatuses called")
        val updatedSections = createSectionsList()
        sectionStatusAdapter = SectionStatusAdapter(requireContext(), updatedSections)
        lssaListViewSections.adapter = sectionStatusAdapter

        lssaListViewSections.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            Log.d(TAG, "ListView item clicked at position: $position (updated)")
            val section = updatedSections[position]
            Log.d(TAG, "Clicked section: ${section.name}, isParent: ${section.isParent}, category: ${section.category}")

            if (!section.isParent && section.category != null) {
                Log.d(TAG, "Valid section clicked, showing photo gallery for category: ${section.category}")
                showPhotoGallery(section.category)
            } else {
                Log.d(TAG, "Section is parent or has no category, ignoring click")
            }
        }

        val updatedActions = createActionsList()
        actionButtonAdapter = ActionButtonAdapter(requireContext(), updatedActions) { action ->
            action.action()
        }
        lssaGridViewActions.adapter = actionButtonAdapter
    }

    private fun startNewTowerScan() {
        findNavController().navigate(R.id.action_siteDashboardFragment_to_towerScanFragment)
    }

    private fun continueTowerScan() {
        Toast.makeText(requireContext(), "Continuing tower scan", Toast.LENGTH_SHORT).show()
    }

    private fun startPOIPhoto() {
        Toast.makeText(requireContext(), "Starting POI photo", Toast.LENGTH_SHORT).show()
    }

    private fun startTOSPhotos() {
        Toast.makeText(requireContext(), "Starting TOS photos", Toast.LENGTH_SHORT).show()
    }

    private fun startHeadFramePhoto(type: String) {
    }

    private fun startHeadFramePhotos() {
    }

    private fun startAccessRoadPhotos() {
        findNavController().navigate(R.id.action_siteDashboardFragment_to_LSSA_Fragment_Access_Route)
    }

    private fun startFeederRunPhotos() {
        findNavController().navigate(R.id.action_siteDashboardFragment_to_feederRunFragment)
    }

    private fun showDeleteConfirmation() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete Site")
            .setMessage("Are you sure you want to delete '${viewModel.site?.name}'? This will also delete all related data.")
            .setPositiveButton("Delete") { _, _ ->
                deleteSite()
            }
            .setNegativeButton("Cancel", null)
            .show()

        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(typedValue.data)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(typedValue.data)
    }

    private fun deleteSite() {
        try {
            viewModel.deleteSite()
            Toast.makeText(requireContext(), "Site deleted successfully", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error deleting site: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}