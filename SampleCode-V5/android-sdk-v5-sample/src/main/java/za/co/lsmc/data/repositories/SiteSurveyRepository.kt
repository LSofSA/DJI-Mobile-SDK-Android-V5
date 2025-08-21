package za.co.lsmc.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.lsmc.models.enums.Category
import za.co.lsmc.data.database.SiteSurveyDbHelper
import za.co.lsmc.data.entities.HeadFrame
import za.co.lsmc.data.entities.Photo
import za.co.lsmc.data.entities.Save
import za.co.lsmc.data.entities.Site
import za.co.lsmc.data.entities.TowerScan

class SiteSurveyRepository(private val dbHelper: SiteSurveyDbHelper) {

    suspend fun getAllSites(): List<Site> = withContext(Dispatchers.IO) {
        dbHelper.readableDatabase.use { db ->
            dbHelper.getAllSites(db)
        }
    }

    suspend fun insertSite(name: String): Site = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.use { db ->
            dbHelper.insertSite(db, name)
        }
    }

    suspend fun deleteSite(site: Site): Unit = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.use { db ->
            dbHelper.deleteSite(db, site)
        }
    }

    suspend fun updateSite(site: Site): Unit = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.use { db ->
            dbHelper.updateSite(db, site)
        }
    }

    suspend fun getSite(id: Long): Site? = withContext(Dispatchers.IO) {
        dbHelper.readableDatabase.use { db ->
            dbHelper.getSite(db, id)
        }
    }

    suspend fun insertPhoto(siteId: Long, filename: String, category: Category, number: Double): Photo = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.use { db ->
            dbHelper.insertPhoto(db, siteId, filename, category, number)
        }
    }

    suspend fun deletePhoto(photo: Photo): Unit = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.use { db ->
            dbHelper.deletePhoto(db, photo)
        }
    }

    suspend fun updatePhoto(photo: Photo): Unit = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.use { db ->
            dbHelper.updatePhoto(db, photo)
        }
    }

    suspend fun getPhoto(id: Long): Photo? = withContext(Dispatchers.IO) {
        dbHelper.readableDatabase.use { db ->
            dbHelper.getPhoto(db, id)
        }
    }

    suspend fun getPhotosBySite(siteId: Long): List<Photo> = withContext(Dispatchers.IO) {
        dbHelper.readableDatabase.use { db ->
            dbHelper.getPhotosBySite(db, siteId)
        }
    }

    suspend fun getPhotosBySiteAndCategory(siteId: Long, category: Category): List<Photo> = withContext(Dispatchers.IO) {
        dbHelper.readableDatabase.use { db ->
            dbHelper.getPhotosBySiteAndCategory(db, siteId, category)
        }
    }

    suspend fun getMaxPhotoNumberForCategory(siteId: Long, category: Category): Double? = withContext(Dispatchers.IO) {
        dbHelper.readableDatabase.use { db ->
            dbHelper.getMaxPhotoNumberForCategory(db, siteId, category)
        }
    }

    suspend fun hasPhotoForCategory(siteId: Long, category: Category): Boolean = withContext(Dispatchers.IO) {
        dbHelper.readableDatabase.use { db ->
            dbHelper.getPhotosBySiteAndCategory(db, siteId, category).isNotEmpty()
        }
    }

    suspend fun insertHeadFrame(siteId: Long, altitude: Double): HeadFrame = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.use { db ->
            dbHelper.insertHeadFrame(db, siteId, altitude)
        }
    }

    suspend fun deleteHeadFrame(headFrame: HeadFrame): Unit = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.use { db ->
            dbHelper.deleteHeadFrame(db, headFrame)
        }
    }

    suspend fun updateHeadFrame(headFrame: HeadFrame): Unit = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.use { db ->
            dbHelper.updateHeadFrame(db, headFrame)
        }
    }

    suspend fun getHeadFrame(id: Long): HeadFrame? = withContext(Dispatchers.IO) {
        dbHelper.readableDatabase.use { db ->
            dbHelper.getHeadFrame(db, id)
        }
    }

    suspend fun getHeadFramesBySite(siteId: Long): List<HeadFrame> = withContext(Dispatchers.IO) {
        dbHelper.readableDatabase.use { db ->
            dbHelper.getHeadFramesBySite(db, siteId)
        }
    }

    suspend fun insertTowerScan(siteId: Long, lat: Double? = null, long: Double? = null, alt: Double? = null, rad: Double? = null): TowerScan = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.use { db ->
            dbHelper.insertTowerScan(db, siteId, lat, long, alt, rad)
        }
    }

    suspend fun deleteTowerScan(siteId: Long): Unit = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.use { db ->
            dbHelper.deleteTowerScan(db, siteId)
        }
    }

    suspend fun updateTowerScan(siteId: Long, lat: Double? = null, long: Double? = null, alt: Double? = null, rad: Double? = null): Unit = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.use { db ->
            dbHelper.updateTowerScan(db, siteId, lat, long, alt, rad)
        }
    }

    suspend fun getTowerScan(siteId: Long): TowerScan? = withContext(Dispatchers.IO) {
        dbHelper.readableDatabase.use { db ->
            dbHelper.getTowerScan(db, siteId)
        }
    }

    suspend fun markTowerScanComplete(siteId: Long): Unit = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.use { db ->
            val timestamp = System.currentTimeMillis()
            dbHelper.insertPhoto(db, siteId, "tower_scan_complete_${timestamp}.jpg", Category.TSO, 0.0)
        }
    }

    suspend fun insertSave(siteId: Long, startLat: Double, startLong: Double, stopLat: Double, stopLong: Double, category: Category, number: Double): Save = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.use { db ->
            dbHelper.insertSave(db, siteId, startLat, startLong, stopLat, stopLong, category, number)
        }
    }

    suspend fun deleteSave(siteId: Long): Unit = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.use { db ->
            dbHelper.deleteSave(db, siteId)
        }
    }

    suspend fun updateSave(save: Save): Unit = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.use { db ->
            dbHelper.updateSave(db, save)
        }
    }

    suspend fun getSave(siteId: Long): Save? = withContext(Dispatchers.IO) {
        dbHelper.readableDatabase.use { db ->
            dbHelper.getSave(db, siteId)
        }
    }
}