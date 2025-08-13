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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dji.sampleV5.aircraft.R
import za.co.lsmc.LSSAMainActivity
import za.co.lsmc.adapters.SiteListAdapter
import za.co.lsmc.data.Site
import za.co.lsmc.data.SiteSurveyDbHelper

class SiteListFragment : Fragment() {

    private lateinit var lssaRecyclerViewSites: RecyclerView
    private lateinit var lssaEditTextSiteName: EditText
    private lateinit var lssaButtonAddSite: Button
    private lateinit var adapter: SiteListAdapter
    private lateinit var dbHelper: SiteSurveyDbHelper
    private val sites = mutableListOf<Site>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.lssa_fragment_site_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = SiteSurveyDbHelper(requireContext())

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
                (activity as LSSAMainActivity).loadSiteDashboard(site.id)
            },
            onDeleteClick = { site ->
                showDeleteConfirmation(site)
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
        val db = dbHelper.readableDatabase
        val siteArray = dbHelper.getAllSites(db)
        sites.clear()
        sites.addAll(siteArray)
        adapter.notifyDataSetChanged()
        db.close()
    }

    private fun addSite(name: String) {
        val db = dbHelper.writableDatabase
        try {
            val newSite = dbHelper.insertSite(db, name, null)
            sites.add(newSite)
            adapter.notifyItemInserted(sites.size - 1)
            lssaEditTextSiteName.text.clear()
            Toast.makeText(requireContext(), "Site added successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error adding site: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            db.close()
        }
    }

    private fun showDeleteConfirmation(site: Site) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Site")
            .setMessage("Are you sure you want to delete '${site.name}'? This will also delete all related data.")
            .setPositiveButton("Delete") { _, _ ->
                deleteSite(site)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteSite(site: Site) {
        val db = dbHelper.writableDatabase
        try {
            dbHelper.deleteSite(db, site)

            val position = sites.indexOf(site)
            if (position >= 0) {
                sites.removeAt(position)
                adapter.notifyItemRemoved(position)
                Toast.makeText(requireContext(), "Site deleted successfully", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error deleting site: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            db.close()
        }
    }
}