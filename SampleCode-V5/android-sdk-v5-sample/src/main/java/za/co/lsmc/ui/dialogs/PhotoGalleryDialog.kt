package za.co.lsmc.ui.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dji.sampleV5.aircraft.R
import za.co.lsmc.ui.adapters.PhotoGalleryAdapter
import za.co.lsmc.data.Category
import za.co.lsmc.data.entities.Photo
import za.co.lsmc.viewmodels.LSSASiteSurveyViewModel

class PhotoGalleryDialog(
    private val context: Context,
    private val category: Category,
    private val viewModel: LSSASiteSurveyViewModel,
    private val onTakePhotosClick: (Category) -> Unit
) {

    private lateinit var dialog: AlertDialog
    private lateinit var adapter: PhotoGalleryAdapter
    private lateinit var rvPhotoGallery: RecyclerView
    private lateinit var llEmptyState: LinearLayout
    private lateinit var tvGalleryTitle: TextView
    private lateinit var tvPhotoCount: TextView
    private lateinit var btnClose: Button

    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.lssa_fragment_photos_list, null)

        initViews(dialogView)
        setupRecyclerView()
        setupClickListeners()
        loadPhotos()

        dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            (context.resources.displayMetrics.heightPixels * 0.8).toInt()
        )

        dialog.show()
    }

    private fun initViews(dialogView: View) {
        rvPhotoGallery = dialogView.findViewById(R.id.rvPhotoGallery)
        llEmptyState = dialogView.findViewById(R.id.llEmptyState)
        tvGalleryTitle = dialogView.findViewById(R.id.tvGalleryTitle)
        tvPhotoCount = dialogView.findViewById(R.id.tvPhotoCount)
        btnClose = dialogView.findViewById(R.id.btnClose)

        tvGalleryTitle.text = "${category.name.replace("_", " ")} Photos"
    }

    private fun setupRecyclerView() {
        val spanCount = when {
            category == Category.POI || category == Category.TSO -> 1
            else -> 2
        }

        rvPhotoGallery.layoutManager = GridLayoutManager(context, spanCount)

        adapter = PhotoGalleryAdapter(
            emptyList(),
            onPhotoClick = { photo ->
                showPhotoDetail(photo)
            },
            onDeleteClick = { photo ->
                showDeletePhotoConfirmation(photo)
            }
        )

        rvPhotoGallery.adapter = adapter
    }

    private fun setupClickListeners() {
        btnClose.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun loadPhotos() {
        viewModel.getPhotosForCurrentSiteByCategory(category).observe(context as LifecycleOwner) { photos ->
            adapter.updatePhotos(photos)
            updateUI(photos)
        }
    }

    private fun updateUI(photos: List<Photo>) {
        val photoCount = photos.size
        tvPhotoCount.text = "$photoCount photo${if (photoCount != 1) "s" else ""}"

        if (photos.isEmpty()) {
            rvPhotoGallery.visibility = View.GONE
            llEmptyState.visibility = View.VISIBLE
        } else {
            rvPhotoGallery.visibility = View.VISIBLE
            llEmptyState.visibility = View.GONE
        }
    }

    private fun showPhotoDetail(photo: Photo) {
        AlertDialog.Builder(context)
            .setTitle("Photo Details")
            .setMessage(
                "Filename: ${photo.filename}\n" +
                        "Category: ${photo.category.name.replace("_", " ")}\n" +
                        "Number: ${photo.number.toInt()}\n" +
                        "Site ID: ${photo.siteId}"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDeletePhotoConfirmation(photo: Photo) {
        AlertDialog.Builder(context)
            .setTitle("Delete Photo")
            .setMessage("Are you sure you want to delete '${photo.filename}'?")
            .setPositiveButton("Delete") { _, _ ->
                deletePhoto(photo)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePhoto(photo: Photo) {
        viewModel.deletePhoto(photo.id)
    }
}