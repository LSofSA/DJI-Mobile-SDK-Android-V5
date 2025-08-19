package za.co.lsmc.data.repositories

import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.lsmc.data.Category
import za.co.lsmc.data.database.SiteSurveyDbHelper
import za.co.lsmc.data.entities.Photo

class PhotoRepository(private val dbHelper: SiteSurveyDbHelper) {

    suspend fun insertPhoto(
        siteId: Long,
        filename: String,
        category: Category,
        number: Double
    ): Photo = withContext(Dispatchers.IO) {
        var db: SQLiteDatabase? = null
        try {
            db = dbHelper.writableDatabase
            dbHelper.insertPhoto(db, siteId, filename, category, number)
        } finally {
            db?.close()
        }
    }

    suspend fun getPhotosBySite(siteId: Long): List<Photo> = withContext(Dispatchers.IO) {
        var db: SQLiteDatabase? = null
        try {
            db = dbHelper.readableDatabase
            dbHelper.getPhotosBySite(db, siteId).toList()
        } finally {
            db?.close()
        }
    }

    suspend fun getPhotosBySiteAndCategory(siteId: Long, category: Category): List<Photo> = withContext(
        Dispatchers.IO) {
        var db: SQLiteDatabase? = null
        try {
            db = dbHelper.readableDatabase
            dbHelper.getPhotosBySiteAndCategory(db, siteId, category)
        } finally {
            db?.close()
        }
    }

    suspend fun getMaxPhotoNumberForCategory(siteId: Long, category: Category): Double? = withContext(
        Dispatchers.IO) {
        var db: SQLiteDatabase? = null
        try {
            db = dbHelper.readableDatabase
            dbHelper.getMaxPhotoNumberForCategory(db, siteId, category)
        } finally {
            db?.close()
        }
    }

    suspend fun deletePhoto(photoId: Long): Unit = withContext(Dispatchers.IO) {
        var db: SQLiteDatabase? = null
        try {
            db = dbHelper.writableDatabase
            dbHelper.deletePhoto(db, photoId)
        } finally {
            db?.close()
        }
    }
}