package za.co.lsmc.fragments

import android.os.Bundle
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
import za.co.lsmc.adapters.ActionButtonAdapter
import za.co.lsmc.adapters.SectionStatusAdapter
import za.co.lsmc.data.Category
import za.co.lsmc.data.Photo
import za.co.lsmc.models.ActionButton
import za.co.lsmc.models.SectionStatus
import za.co.lsmc.viewmodels.LSSASiteSurveyViewModel

class SiteDashboardFragment : Fragment() {

    private val viewModel: LSSASiteSurveyViewModel by activityViewModels()

    private lateinit var lssaTextViewSiteName: TextView
    private lateinit var lssaButtonBack: Button
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
        lssaButtonBack = view.findViewById(R.id.lssaButtonBack)
        lssaButtonDelete = view.findViewById(R.id.lssaButtonDelete)
        lssaListViewSections = view.findViewById(R.id.lssaListViewSections)
        lssaGridViewActions = view.findViewById(R.id.lssaGridViewActions)

        setupAdapters()
        setupClickListeners()
        loadSiteData()
    }

    private fun setupAdapters() {
        val sections = createSectionsList()
        sectionStatusAdapter = SectionStatusAdapter(requireContext(), sections)
        lssaListViewSections.adapter = sectionStatusAdapter

        lssaListViewSections.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val section = sections[position]
            if (!section.isParent && section.category != null) {
                onSectionClicked(section.category)
            }
        }

        val actions = createActionsList()
        actionButtonAdapter = ActionButtonAdapter(requireContext(), actions) { action ->
            action.action()
        }
        lssaGridViewActions.adapter = actionButtonAdapter
    }

    private fun setupClickListeners() {
        lssaButtonBack.setOnClickListener {
            findNavController().popBackStack()
            //requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        lssaButtonDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    // actions for now will just display a toast, implementation still under works
    private fun onSectionClicked(category: Category) {
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

    //LEFT SIDE "actions"
    private fun createSectionsList(): List<SectionStatus> {
        return listOf(

            //self-explanatory
            SectionStatus("POI Center Photo", Category.POI, hasPhotoForCategory(Category.POI), false, 0),
            SectionStatus("TSO Orbit Photos", Category.TSO, hasPhotoForCategory(Category.TSO), false, 0),
            SectionStatus("Access Route / Road Photos", Category.ACCESS_ROUTE, hasPhotoForCategory(Category.ACCESS_ROUTE), false, 0),
            SectionStatus("Feeder Run (Ladder) Photos", Category.FEEDER_RUN, hasPhotoForCategory(Category.FEEDER_RUN), false, 0),

            // head frames and its sub sections
            SectionStatus("Head Frame Photos", null, false, true, 0),
            SectionStatus("   Down Orbit", Category.HF_DOWN, hasPhotoForCategory(Category.HF_DOWN), false, 1),
            SectionStatus("   Level Orbit", Category.HF_LEVEL, hasPhotoForCategory(Category.HF_LEVEL), false, 1),
            SectionStatus("   Panoramic Orbit", Category.HF_PANORAMIC, hasPhotoForCategory(Category.HF_PANORAMIC), false, 1),
            SectionStatus("   Up Orbit", Category.HF_UP, hasPhotoForCategory(Category.HF_UP), false, 1)
        )
    }

    //RIGHT SIDE "actions" in the grid
    private fun createActionsList(): List<ActionButton> {
        return listOf(
            ActionButton(
                title = "New Tower Scan",
                description = "Start a new tower scan",
                isEnabled = true
            ) {
                startNewTowerScan()
            },
            ActionButton(
                title = "Continue Tower Scan",
                description = "Resume an existing tower scan",
                isEnabled = photos.any { it.category == Category.TSO }
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
            /*ActionButton(
                title = "Head Frame Photos",
                description = "Document head frames",
                isEnabled = true
            ) {
                startHeadFramePhotos()
            },*/
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

    //todo: move this stuff to the view model
    private fun hasPhotoForCategory(category: Category): Boolean {
        return photos.any { photo -> photo.category == category }
    }

    private fun loadSiteData() {
        lssaTextViewSiteName.text = viewModel.site?.name ?: throw IllegalStateException("A site has not been selected.")

        updateSectionStatuses()
    }

    private fun updateSectionStatuses() {
        val updatedSections = createSectionsList()
        sectionStatusAdapter = SectionStatusAdapter(requireContext(), updatedSections)
        lssaListViewSections.adapter = sectionStatusAdapter

        lssaListViewSections.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val section = updatedSections[position]
            if (!section.isParent && section.category != null) {
                onSectionClicked(section.category)
            }
        }

        val updatedActions = createActionsList()
        actionButtonAdapter = ActionButtonAdapter(requireContext(), updatedActions) { action ->
            action.action()
        }
        lssaGridViewActions.adapter = actionButtonAdapter
    }

    private fun startNewTowerScan() {
        Toast.makeText(requireContext(), "Starting new tower scan", Toast.LENGTH_SHORT).show()
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
        Toast.makeText(requireContext(), "Starting Head Frame photo: $type", Toast.LENGTH_SHORT).show()
    }

    private fun startHeadFramePhotos() {
        Toast.makeText(requireContext(), "Starting Head Frame photos", Toast.LENGTH_SHORT).show()
    }

    private fun startAccessRoadPhotos() {
        findNavController().navigate(R.id.action_siteDashboardFragment_to_LSSA_Fragment_Access_Route)
        Toast.makeText(requireContext(), "Starting Access Road photos", Toast.LENGTH_SHORT).show()
    }

    private fun startFeederRunPhotos() {
        Toast.makeText(requireContext(), "Starting Feeder Run photos", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmation() {
        // positive and negative buttons don't show at all... no clue if its the code or if maybe the text is just not visible
        // ¯\_ (ツ)_/¯
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Site")
            .setMessage("Are you sure you want to delete '${viewModel.site?.name}'? This will also delete all related data.")
            .setPositiveButton("Delete") { _, _ ->
                deleteSite()
            }
            .setNegativeButton("Cancel", null)
            .show()
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