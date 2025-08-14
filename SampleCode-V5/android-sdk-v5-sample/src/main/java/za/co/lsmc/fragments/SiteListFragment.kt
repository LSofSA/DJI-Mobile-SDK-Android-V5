package za.co.lsmc.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dji.sampleV5.aircraft.R
import za.co.lsmc.LSSAMainActivity
import za.co.lsmc.adapters.SiteListAdapter
import za.co.lsmc.data.Site
import za.co.lsmc.data.SiteSurveyDbHelper
import za.co.lsmc.viewmodels.LSSASiteSurveyViewModel

class SiteListFragment : Fragment() {

    private lateinit var lssaRecyclerViewSites: RecyclerView
    private lateinit var lssaEditTextSiteName: EditText
    private lateinit var lssaButtonAddSite: Button
    private lateinit var adapter: SiteListAdapter
    private val sites = mutableListOf<Site>()

    private val viewModel: LSSASiteSurveyViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.lssa_fragment_site_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dbHelper = SiteSurveyDbHelper(requireContext())
        viewModel.initDbHelper(dbHelper)

        lssaRecyclerViewSites = view.findViewById(R.id.lssaRecyclerViewSites)
        lssaEditTextSiteName = view.findViewById(R.id.lssaEditTextSiteName)
        lssaButtonAddSite = view.findViewById(R.id.lssaButtonAddSite)

        setupRecyclerView()
        setupAddButton()
        loadSites()

    }

    private fun setupRecyclerView() {
        adapter = SiteListAdapter(
            sites,
            onSiteClick = { site ->
                viewModel.site = site
                findNavController().navigate(R.id.action_siteList_to_siteDashboard)
            },
            onDeleteClick = { site ->
                viewModel.site = site
                showDeleteConfirmation()
            }
        )

        lssaRecyclerViewSites.layoutManager = LinearLayoutManager(requireContext())
        lssaRecyclerViewSites.adapter = adapter
    }

    private fun setupAddButton() {
        lssaButtonAddSite.setOnClickListener {
            val siteName = lssaEditTextSiteName.text.toString().trim()
            if (siteName.isNotEmpty()) {
                addSite(siteName)
            } else {
                Toast.makeText(requireContext(), "Please enter a site name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSites() {
       /* val db = dbHelper.readableDatabase
        val siteArray = dbHelper.getAllSites(db)*/

        sites.clear()
        sites.addAll(viewModel.loadSites())
        adapter.notifyDataSetChanged()
    }

    private fun addSite(name: String) {
        try {
            viewModel.addSite(name)
            loadSites()
            //adapter.notifyItemInserted(sites.size - 1)
            lssaEditTextSiteName.text.clear()
            Toast.makeText(requireContext(), "Site added successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error adding site: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Site")
            .setMessage("Are you sure you want to delete '${viewModel.site}'? This will also delete all related data.")
            .setPositiveButton("Delete") { _, _ ->
                deleteSite()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteSite() {
        try {
            viewModel.deleteSite()
            loadSites()
            viewModel.site = null
            Toast.makeText(requireContext(), "Site deleted successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error deleting site: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}