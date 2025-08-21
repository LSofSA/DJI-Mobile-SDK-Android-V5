package za.co.lsmc.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import za.co.lsmc.models.enums.Category
import za.co.lsmc.data.entities.HeadFrame
import za.co.lsmc.data.entities.Photo
import za.co.lsmc.data.entities.Save
import za.co.lsmc.data.entities.Site
import za.co.lsmc.data.entities.TowerScan
import java.util.Date

class SiteSurveyDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "SiteSurvey.db"
        const val LOG_TAG = "Site Survey DB Helper"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SiteSurveyDbContact.SiteTable.CREATE_TABLE)
        db.execSQL(SiteSurveyDbContact.HeadFrameTable.CREATE_TABLE)
        db.execSQL(SiteSurveyDbContact.PhotoTable.CREATE_TABLE)
        db.execSQL(SiteSurveyDbContact.TowerScanTable.CREATE_TABLE)
        db.execSQL(SiteSurveyDbContact.SaveTable.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        Log.wtf(LOG_TAG, "No upgrade path for first version")
        throw NotImplementedError("There is currently only one version and this function should not be called.")
    }

    fun insertSite(db: SQLiteDatabase, name: String, date: Date? = null): Site {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.SiteTable.COLUMN_NAME_NAME, name)
            put(SiteSurveyDbContact.SiteTable.COLUMN_NAME_COMPLETED, date?.time)
        }
        val id = db.insert(SiteSurveyDbContact.SiteTable.TABLE_NAME, null, values)
        return Site(id, name, date)
    }

    fun deleteSite(db: SQLiteDatabase, site: Site) {
        db.delete(SiteSurveyDbContact.SiteTable.TABLE_NAME, "${SiteSurveyDbContact.SiteTable.COLUMN_NAME_ID}=?", arrayOf(site.id.toString()))
    }

    fun updateSite(db: SQLiteDatabase, site: Site): Int {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.SiteTable.COLUMN_NAME_NAME, site.name)
            put(SiteSurveyDbContact.SiteTable.COLUMN_NAME_COMPLETED, site.completed?.time)
        }
        return db.update(SiteSurveyDbContact.SiteTable.TABLE_NAME, values, "${SiteSurveyDbContact.SiteTable.COLUMN_NAME_ID}=?", arrayOf(site.id.toString()))
    }

    fun getSite(db: SQLiteDatabase, id: Long): Site? {
        val projection = arrayOf(
            SiteSurveyDbContact.SiteTable.COLUMN_NAME_ID,
            SiteSurveyDbContact.SiteTable.COLUMN_NAME_NAME,
            SiteSurveyDbContact.SiteTable.COLUMN_NAME_COMPLETED
        )
        val selection = "${SiteSurveyDbContact.SiteTable.COLUMN_NAME_ID} = ?"
        val selectionArgs = arrayOf(id.toString())
        val cursor = db.query(
            SiteSurveyDbContact.SiteTable.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToNext()) {
                val completedColumnIndex = it.getColumnIndexOrThrow(SiteSurveyDbContact.SiteTable.COLUMN_NAME_COMPLETED)
                val completedDate = if (it.isNull(completedColumnIndex)) null else Date(it.getLong(completedColumnIndex))

                return Site(
                    it.getLong(it.getColumnIndexOrThrow(SiteSurveyDbContact.SiteTable.COLUMN_NAME_ID)),
                    it.getString(it.getColumnIndexOrThrow(SiteSurveyDbContact.SiteTable.COLUMN_NAME_NAME)),
                    completedDate
                )
            }
        }
        return null
    }

    fun getAllSites(db: SQLiteDatabase): List<Site> {
        val projection = arrayOf(
            SiteSurveyDbContact.SiteTable.COLUMN_NAME_ID,
            SiteSurveyDbContact.SiteTable.COLUMN_NAME_NAME,
            SiteSurveyDbContact.SiteTable.COLUMN_NAME_COMPLETED
        )
        val cursor = db.query(
            SiteSurveyDbContact.SiteTable.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )

        val items = mutableListOf<Site>()
        cursor.use {
            while (it.moveToNext()) {
                val completedColumnIndex = it.getColumnIndexOrThrow(SiteSurveyDbContact.SiteTable.COLUMN_NAME_COMPLETED)
                val completedDate = if (it.isNull(completedColumnIndex)) null else Date(it.getLong(completedColumnIndex))

                items.add(
                    Site(
                        it.getLong(it.getColumnIndexOrThrow(SiteSurveyDbContact.SiteTable.COLUMN_NAME_ID)),
                        it.getString(it.getColumnIndexOrThrow(SiteSurveyDbContact.SiteTable.COLUMN_NAME_NAME)),
                        completedDate
                    )
                )
            }
        }
        return items
    }

    fun insertPhoto(db: SQLiteDatabase, siteId: Long, filename: String, category: Category, number: Double): Photo {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_SITE_ID, siteId)
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_FILENAME, filename)
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_CATEGORY, category.ordinal)
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_NUMBER, number)
        }
        val id = db.insert(SiteSurveyDbContact.PhotoTable.TABLE_NAME, null, values)
        return Photo(id, siteId, filename, category, number)
    }

    fun deletePhoto(db: SQLiteDatabase, photo: Photo) {
        db.delete(SiteSurveyDbContact.PhotoTable.TABLE_NAME, "${SiteSurveyDbContact.PhotoTable.COLUMN_NAME_ID}=?", arrayOf(photo.id.toString()))
    }

    fun updatePhoto(db: SQLiteDatabase, photo: Photo): Int {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_SITE_ID, photo.siteId)
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_FILENAME, photo.filename)
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_CATEGORY, photo.category.ordinal)
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_NUMBER, photo.number)
        }
        return db.update(SiteSurveyDbContact.PhotoTable.TABLE_NAME, values, "${SiteSurveyDbContact.PhotoTable.COLUMN_NAME_ID}=?", arrayOf(photo.id.toString()))
    }

    fun getPhoto(db: SQLiteDatabase, id: Long): Photo? {
        val projection = arrayOf(
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_ID,
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_SITE_ID,
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_FILENAME,
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_CATEGORY,
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_NUMBER
        )
        val selection = "${SiteSurveyDbContact.PhotoTable.COLUMN_NAME_ID} = ?"
        val selectionArgs = arrayOf(id.toString())
        val cursor = db.query(
            SiteSurveyDbContact.PhotoTable.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToNext()) {
                return Photo(
                    it.getLong(it.getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_ID)),
                    it.getLong(it.getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_SITE_ID)),
                    it.getString(it.getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_FILENAME)),
                    Category.values()[it.getInt(it.getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_CATEGORY))],
                    it.getDouble(it.getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_NUMBER))
                )
            }
        }
        return null
    }

    fun getPhotosBySite(db: SQLiteDatabase, siteId: Long): List<Photo> {
        val projection = arrayOf(
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_ID,
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_SITE_ID,
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_FILENAME,
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_CATEGORY,
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_NUMBER
        )
        val selection = "${SiteSurveyDbContact.PhotoTable.COLUMN_NAME_SITE_ID} = ?"
        val selectionArgs = arrayOf(siteId.toString())
        val cursor = db.query(
            SiteSurveyDbContact.PhotoTable.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val items = mutableListOf<Photo>()
        cursor.use {
            while (it.moveToNext()) {
                items.add(
                    Photo(
                        it.getLong(it.getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_ID)),
                        it.getLong(it.getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_SITE_ID)),
                        it.getString(it.getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_FILENAME)),
                        Category.values()[it.getInt(it.getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_CATEGORY))],
                        it.getDouble(it.getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_NUMBER))
                    )
                )
            }
        }
        return items
    }

    fun getPhotosBySiteAndCategory(db: SQLiteDatabase, siteId: Long, category: Category): List<Photo> {
        val projection = arrayOf(
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_ID,
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_SITE_ID,
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_FILENAME,
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_CATEGORY,
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_NUMBER
        )
        val selection = "${SiteSurveyDbContact.PhotoTable.COLUMN_NAME_SITE_ID} = ? AND ${SiteSurveyDbContact.PhotoTable.COLUMN_NAME_CATEGORY} = ?"
        val selectionArgs = arrayOf(siteId.toString(), category.ordinal.toString())
        val orderBy = "${SiteSurveyDbContact.PhotoTable.COLUMN_NAME_NUMBER} ASC"

        val cursor = db.query(
            SiteSurveyDbContact.PhotoTable.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null, null,
            orderBy
        )

        val photos = mutableListOf<Photo>()
        cursor.use {
            while (it.moveToNext()) {
                photos.add(
                    Photo(
                        it.getLong(it.getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_ID)),
                        it.getLong(it.getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_SITE_ID)),
                        it.getString(it.getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_FILENAME)),
                        Category.values()[it.getInt(it.getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_CATEGORY))],
                        it.getDouble(it.getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_NUMBER))
                    )
                )
            }
        }
        return photos
    }

    fun getMaxPhotoNumberForCategory(db: SQLiteDatabase, siteId: Long, category: Category): Double? {
        val projection = arrayOf("MAX(${SiteSurveyDbContact.PhotoTable.COLUMN_NAME_NUMBER})")
        val selection = "${SiteSurveyDbContact.PhotoTable.COLUMN_NAME_SITE_ID} = ? AND ${SiteSurveyDbContact.PhotoTable.COLUMN_NAME_CATEGORY} = ?"
        val selectionArgs = arrayOf(siteId.toString(), category.ordinal.toString())

        val cursor = db.query(
            SiteSurveyDbContact.PhotoTable.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null, null, null
        )

        cursor.use {
            if (it.moveToFirst() && !it.isNull(0)) {
                return it.getDouble(0)
            }
        }
        return null
    }

    fun insertHeadFrame(db: SQLiteDatabase, siteId: Long, altitudeM: Double): HeadFrame {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_SITE_ID, siteId)
            put(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ALTITUDE, altitudeM)
        }
        val id = db.insert(SiteSurveyDbContact.HeadFrameTable.TABLE_NAME, null, values)
        return HeadFrame(id, siteId, altitudeM)
    }

    fun deleteHeadFrame(db: SQLiteDatabase, headFrame: HeadFrame) {
        db.delete(SiteSurveyDbContact.HeadFrameTable.TABLE_NAME, "${SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ID}=?", arrayOf(headFrame.id.toString()))
    }

    fun updateHeadFrame(db: SQLiteDatabase, headFrame: HeadFrame): Int {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_SITE_ID, headFrame.siteId)
            put(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ALTITUDE, headFrame.altitudeM)
        }
        return db.update(SiteSurveyDbContact.HeadFrameTable.TABLE_NAME, values, "${SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ID}=?", arrayOf(headFrame.id.toString()))
    }

    fun getHeadFrame(db: SQLiteDatabase, id: Long): HeadFrame? {
        val projection = arrayOf(
            SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ID,
            SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_SITE_ID,
            SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ALTITUDE
        )
        val selection = "${SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ID} = ?"
        val selectionArgs = arrayOf(id.toString())
        val cursor = db.query(
            SiteSurveyDbContact.HeadFrameTable.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToNext()) {
                return HeadFrame(
                    it.getLong(it.getColumnIndexOrThrow(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ID)),
                    it.getLong(it.getColumnIndexOrThrow(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_SITE_ID)),
                    it.getDouble(it.getColumnIndexOrThrow(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ALTITUDE))
                )
            }
        }
        return null
    }

    fun getHeadFramesBySite(db: SQLiteDatabase, siteId: Long): List<HeadFrame> {
        val projection = arrayOf(
            SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ID,
            SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_SITE_ID,
            SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ALTITUDE
        )
        val selection = "${SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_SITE_ID} = ?"
        val selectionArgs = arrayOf(siteId.toString())
        val cursor = db.query(
            SiteSurveyDbContact.HeadFrameTable.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val items = mutableListOf<HeadFrame>()
        cursor.use {
            while (it.moveToNext()) {
                items.add(
                    HeadFrame(
                        it.getLong(it.getColumnIndexOrThrow(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ID)),
                        it.getLong(it.getColumnIndexOrThrow(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_SITE_ID)),
                        it.getDouble(it.getColumnIndexOrThrow(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ALTITUDE))
                    )
                )
            }
        }
        return items
    }

    fun insertTowerScan(db: SQLiteDatabase, siteId: Long, lat: Double? = null, long: Double? = null, alt: Double? = null, rad: Double? = null): TowerScan {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_SITE_ID, siteId)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_RADIUS, rad)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_LATITUDE, lat)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_LONGITUDE, long)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_ALTITUDE, alt)
        }
        db.insert(SiteSurveyDbContact.TowerScanTable.TABLE_NAME, null, values)
        return TowerScan(siteId, lat, long, alt, rad)
    }

    fun deleteTowerScan(db: SQLiteDatabase, siteId: Long) {
        db.delete(SiteSurveyDbContact.TowerScanTable.TABLE_NAME, "${SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_SITE_ID}=?", arrayOf(siteId.toString()))
    }

    fun updateTowerScan(db: SQLiteDatabase, siteId: Long, lat: Double? = null, long: Double? = null, alt: Double? = null, rad: Double? = null): Int {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_SITE_ID, siteId)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_RADIUS, rad)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_LATITUDE, lat)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_LONGITUDE, long)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_ALTITUDE, alt)
        }
        return db.update(SiteSurveyDbContact.TowerScanTable.TABLE_NAME, values, "${SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_SITE_ID}=?", arrayOf(siteId.toString()))
    }

    fun getTowerScan(db: SQLiteDatabase, siteId: Long): TowerScan? {
        val projection = arrayOf(
            SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_SITE_ID,
            SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_LATITUDE,
            SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_LONGITUDE,
            SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_ALTITUDE,
            SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_RADIUS
        )
        val selection = "${SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_SITE_ID} = ?"
        val selectionArgs = arrayOf(siteId.toString())
        val cursor = db.query(
            SiteSurveyDbContact.TowerScanTable.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToNext()) {
                val latColumnIndex = it.getColumnIndexOrThrow(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_LATITUDE)
                val longColumnIndex = it.getColumnIndexOrThrow(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_LONGITUDE)
                val altColumnIndex = it.getColumnIndexOrThrow(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_ALTITUDE)
                val radiusColumnIndex = it.getColumnIndexOrThrow(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_RADIUS)

                return TowerScan(
                    it.getLong(it.getColumnIndexOrThrow(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_SITE_ID)),
                    if (it.isNull(latColumnIndex)) null else it.getDouble(latColumnIndex),
                    if (it.isNull(longColumnIndex)) null else it.getDouble(longColumnIndex),
                    if (it.isNull(altColumnIndex)) null else it.getDouble(altColumnIndex),
                    if (it.isNull(radiusColumnIndex)) null else it.getDouble(radiusColumnIndex)
                )
            }
        }
        return null
    }

    fun insertSave(db: SQLiteDatabase, siteId: Long, startLat: Double, startLong: Double, stopLat: Double, stopLong: Double, category: Category, number: Double): Save {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_SITE_ID, siteId)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_START_LATITUDE, startLat)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_START_LONGITUDE, startLong)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_STOP_LATITUDE, stopLat)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_STOP_LONGITUDE, stopLong)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_CATEGORY, category.ordinal)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_NUMBER, number)
        }
        db.insert(SiteSurveyDbContact.SaveTable.TABLE_NAME, null, values)
        return Save(siteId, startLat, startLong, category, number, stopLat, stopLong)
    }

    fun deleteSave(db: SQLiteDatabase, siteId: Long) {
        db.delete(SiteSurveyDbContact.SaveTable.TABLE_NAME, "${SiteSurveyDbContact.SaveTable.COLUMN_NAME_SITE_ID}=?", arrayOf(siteId.toString()))
    }

    fun updateSave(db: SQLiteDatabase, save: Save): Int {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_SITE_ID, save.siteId)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_START_LATITUDE, save.startLatitude)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_START_LONGITUDE, save.startLongitude)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_STOP_LATITUDE, save.stopLatitude)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_STOP_LONGITUDE, save.stopLongitude)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_CATEGORY, save.category.ordinal)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_NUMBER, save.number)
        }
        return db.update(SiteSurveyDbContact.SaveTable.TABLE_NAME, values, "${SiteSurveyDbContact.SaveTable.COLUMN_NAME_SITE_ID}=?", arrayOf(save.siteId.toString()))
    }

    fun getSave(db: SQLiteDatabase, siteId: Long): Save? {
        val projection = arrayOf(
            SiteSurveyDbContact.SaveTable.COLUMN_NAME_SITE_ID,
            SiteSurveyDbContact.SaveTable.COLUMN_NAME_START_LATITUDE,
            SiteSurveyDbContact.SaveTable.COLUMN_NAME_START_LONGITUDE,
            SiteSurveyDbContact.SaveTable.COLUMN_NAME_STOP_LATITUDE,
            SiteSurveyDbContact.SaveTable.COLUMN_NAME_STOP_LONGITUDE,
            SiteSurveyDbContact.SaveTable.COLUMN_NAME_CATEGORY,
            SiteSurveyDbContact.SaveTable.COLUMN_NAME_NUMBER
        )
        val selection = "${SiteSurveyDbContact.SaveTable.COLUMN_NAME_SITE_ID} = ?"
        val selectionArgs = arrayOf(siteId.toString())
        val cursor = db.query(
            SiteSurveyDbContact.SaveTable.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToNext()) {
                return Save(
                    it.getLong(it.getColumnIndexOrThrow(SiteSurveyDbContact.SaveTable.COLUMN_NAME_SITE_ID)),
                    it.getDouble(it.getColumnIndexOrThrow(SiteSurveyDbContact.SaveTable.COLUMN_NAME_START_LATITUDE)),
                    it.getDouble(it.getColumnIndexOrThrow(SiteSurveyDbContact.SaveTable.COLUMN_NAME_START_LONGITUDE)),
                    Category.values()[it.getInt(it.getColumnIndexOrThrow(SiteSurveyDbContact.SaveTable.COLUMN_NAME_CATEGORY))],
                    it.getDouble(it.getColumnIndexOrThrow(SiteSurveyDbContact.SaveTable.COLUMN_NAME_NUMBER)),
                    it.getDouble(it.getColumnIndexOrThrow(SiteSurveyDbContact.SaveTable.COLUMN_NAME_STOP_LATITUDE)),
                    it.getDouble(it.getColumnIndexOrThrow(SiteSurveyDbContact.SaveTable.COLUMN_NAME_STOP_LONGITUDE))
                )
            }
        }
        return null
    }
}