package za.co.lsmc.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dji.sampleV5.aircraft.R
import za.co.lsmc.data.entities.Photo

class PhotoGalleryAdapter(
    private var photos: List<Photo>,
    private val onPhotoClick: (Photo) -> Unit,
    private val onDeleteClick: (Photo) -> Unit
) : RecyclerView.Adapter<PhotoGalleryAdapter.PhotoViewHolder>(){
    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPhotoThumbnail: ImageView = view.findViewById(R.id.ivPhotoThumbnail)
        val tvPhotoFilename: TextView = view.findViewById(R.id.tvPhotoFilename)
        val tvPhotoNumber: TextView = view.findViewById(R.id.tvPhotoNumber)
        val btnDeletePhoto: ImageButton = view.findViewById(R.id.btnDeletePhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lssa_item_photo_view, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]

        holder.tvPhotoFilename.text = photo.filename
        holder.tvPhotoNumber.text = "#${photo.number.toInt()}"

        holder.ivPhotoThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)

        holder.itemView.setOnClickListener {
            onPhotoClick(photo)
        }

        holder.btnDeletePhoto.setOnClickListener {
            onDeleteClick(photo)
        }
    }

    override fun getItemCount() = photos.size

    fun updatePhotos(newPhotos: List<Photo>) {
        photos = newPhotos
        notifyDataSetChanged()
    }
}
