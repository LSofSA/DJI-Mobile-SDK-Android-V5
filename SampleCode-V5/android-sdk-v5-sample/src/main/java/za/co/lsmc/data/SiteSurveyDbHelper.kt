package za.co.lsmc.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.Date

class SiteSurveyDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        // If you change the database schema, you must increment the database version.
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

    //////////////////////
    ///// Site Table /////
    //////////////////////

    public fun insertSite(db: SQLiteDatabase, site: Site) : Long {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.SiteTable.COLUMN_NAME_NAME, site.name)
            put(SiteSurveyDbContact.SiteTable.COLUMN_NAME_COMPLETED, site.completed?.time)
        }
        return db.insert(SiteSurveyDbContact.SiteTable.TABLE_NAME, null, values)
    }

    public fun insertSite(db: SQLiteDatabase, name: String, date: Date? = null) : Site {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.SiteTable.COLUMN_NAME_NAME, name)
            put(SiteSurveyDbContact.SiteTable.COLUMN_NAME_COMPLETED, date?.time)
        }
        val id = db.insert(SiteSurveyDbContact.SiteTable.TABLE_NAME, null, values)
        return Site(id, name, date)
    }

    public fun deleteSite(db: SQLiteDatabase, site: Site) {
        db.delete(SiteSurveyDbContact.SiteTable.TABLE_NAME, "${SiteSurveyDbContact.SiteTable.COLUMN_NAME_ID}=?", arrayOf(site.id.toString()))
    }

    public fun deleteSite(db: SQLiteDatabase, id: Long) {
        db.delete(SiteSurveyDbContact.SiteTable.TABLE_NAME, "${SiteSurveyDbContact.SiteTable.COLUMN_NAME_ID}=?", arrayOf(id.toString()))
    }

    public fun updateSite(db: SQLiteDatabase, site: Site) : Int {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.SiteTable.COLUMN_NAME_ID, site.id)
            put(SiteSurveyDbContact.SiteTable.COLUMN_NAME_NAME, site.name)
            put(SiteSurveyDbContact.SiteTable.COLUMN_NAME_COMPLETED, site.completed?.time)
        }
        return db.update(SiteSurveyDbContact.SiteTable.TABLE_NAME, values, "${SiteSurveyDbContact.SiteTable.COLUMN_NAME_ID}=?", arrayOf(site.id.toString()))
    }

    public fun updateSite(db: SQLiteDatabase, id: Long, name: String, date: Date? = null) : Int {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.SiteTable.COLUMN_NAME_ID, id)
            put(SiteSurveyDbContact.SiteTable.COLUMN_NAME_NAME, name)
            put(SiteSurveyDbContact.SiteTable.COLUMN_NAME_COMPLETED, date?.time)
        }
        return db.update(SiteSurveyDbContact.SiteTable.TABLE_NAME, values, "${SiteSurveyDbContact.SiteTable.COLUMN_NAME_ID}=?", arrayOf(id.toString()))
    }

    public fun getSite(db: SQLiteDatabase, id: Long) : Site? {
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
        with(cursor) {
            if (moveToNext()) {
                val site = Site(
                    getLong(getColumnIndexOrThrow(SiteSurveyDbContact.SiteTable.COLUMN_NAME_ID)),
                    getString(getColumnIndexOrThrow(SiteSurveyDbContact.SiteTable.COLUMN_NAME_NAME)),
                    Date(getLong(getColumnIndexOrThrow(SiteSurveyDbContact.SiteTable.COLUMN_NAME_COMPLETED)))
                )
                close()
                return site
            }
        }
        cursor.close()
        return null
    }

    public fun getAllSites(db: SQLiteDatabase) : Array<Site> {
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
        with(cursor) {
            while (moveToNext()) {
                items.add(Site(
                    getLong(getColumnIndexOrThrow(SiteSurveyDbContact.SiteTable.COLUMN_NAME_ID)),
                    getString(getColumnIndexOrThrow(SiteSurveyDbContact.SiteTable.COLUMN_NAME_NAME)),
                    Date(getLong(getColumnIndexOrThrow(SiteSurveyDbContact.SiteTable.COLUMN_NAME_COMPLETED)))
                ))
            }
            close()
        }
        return items.toTypedArray()
    }

    ///////////////////////
    ///// Photo Table /////
    ///////////////////////

    public fun insertPhoto(db: SQLiteDatabase, photo: Photo) : Long {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_SITE_ID, photo.siteId)
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_FILENAME, photo.filename)
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_CATEGORY, photo.category.ordinal)
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_NUMBER, photo.number)
        }
        return db.insert(SiteSurveyDbContact.PhotoTable.TABLE_NAME, null, values)
    }

    public fun insertPhoto(db: SQLiteDatabase, siteId: Long, filename: String, cat: Category, number: Double) : Photo {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_SITE_ID, siteId)
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_FILENAME, filename)
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_CATEGORY, cat.ordinal)
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_NUMBER, number)
        }
        val id = db.insert(SiteSurveyDbContact.PhotoTable.TABLE_NAME, null, values)
        return Photo(id, siteId, filename, cat, number)
    }

    public fun deletePhoto(db: SQLiteDatabase, photo: Photo) {
        db.delete(SiteSurveyDbContact.PhotoTable.TABLE_NAME, "${SiteSurveyDbContact.PhotoTable.COLUMN_NAME_ID}=?", arrayOf(photo.id.toString()))
    }

    public fun deletePhoto(db: SQLiteDatabase, id: Long) {
        db.delete(SiteSurveyDbContact.PhotoTable.TABLE_NAME, "${SiteSurveyDbContact.PhotoTable.COLUMN_NAME_ID}=?", arrayOf(id.toString()))
    }

    public fun updatePhoto(db: SQLiteDatabase, photo: Photo) : Int {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_SITE_ID, photo.siteId)
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_FILENAME, photo.filename)
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_CATEGORY, photo.category.ordinal)
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_NUMBER, photo.number)
        }
        return db.update(SiteSurveyDbContact.PhotoTable.TABLE_NAME, values, "${SiteSurveyDbContact.PhotoTable.COLUMN_NAME_ID}=?", arrayOf(photo.id.toString()))
    }

    public fun updatePhoto(db: SQLiteDatabase, id: Long, siteId: Long, filename: String, cat: Category, number: Double) : Int {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_SITE_ID, siteId)
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_FILENAME, filename)
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_CATEGORY, cat.ordinal)
            put(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_NUMBER, number)
        }
        return db.update(SiteSurveyDbContact.PhotoTable.TABLE_NAME, values, "${SiteSurveyDbContact.PhotoTable.COLUMN_NAME_ID}=?", arrayOf(id.toString()))
    }

    public fun getPhoto(db: SQLiteDatabase, id: Long) : Photo? {
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
        with(cursor) {
            if (moveToNext()) {
                val photo = Photo(
                    getLong(getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_ID)),
                    getLong(getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_SITE_ID)),
                    getString(getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_FILENAME)),
                    Category.values()[getInt(getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_CATEGORY))],
                    getDouble(getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_NUMBER))
                )
                close()
                return photo
            }
        }
        cursor.close()
        return null
    }

    public fun getAllPhoto(db: SQLiteDatabase) : Array<Photo> {
        val projection = arrayOf(
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_ID,
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_SITE_ID,
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_FILENAME,
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_CATEGORY,
            SiteSurveyDbContact.PhotoTable.COLUMN_NAME_NUMBER
        )
        val cursor = db.query(
            SiteSurveyDbContact.PhotoTable.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )
        val items = mutableListOf<Photo>()
        with(cursor) {
            while (moveToNext()) {
                items.add(Photo(
                    getLong(getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_ID)),
                    getLong(getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_SITE_ID)),
                    getString(getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_FILENAME)),
                    Category.values()[getInt(getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_CATEGORY))],
                    getDouble(getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_NUMBER))
                ))
            }
            close()
        }
        return items.toTypedArray()
    }

    public fun getPhotosBySite(db: SQLiteDatabase, siteId: Long) : Array<Photo> {
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
        with(cursor) {
            while (moveToNext()) {
                items.add(Photo(
                    getLong(getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_ID)),
                    getLong(getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_SITE_ID)),
                    getString(getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_FILENAME)),
                    Category.values()[getInt(getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_CATEGORY))],
                    getDouble(getColumnIndexOrThrow(SiteSurveyDbContact.PhotoTable.COLUMN_NAME_NUMBER))
                ))
            }
            close()
        }
        return items.toTypedArray()
    }

    ////////////////////////////
    ///// Head Frame Table /////
    ////////////////////////////

    public fun insertHeadFrame(db: SQLiteDatabase, headFrame: HeadFrame) : Long {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_SITE_ID, headFrame.siteId)
            put(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ALTITUDE, headFrame.altitudeM)
        }
        return db.insert(SiteSurveyDbContact.HeadFrameTable.TABLE_NAME, null, values)
    }

    public fun insertHeadFrame(db: SQLiteDatabase, siteId: Long, altitudeM: Double) : HeadFrame {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_SITE_ID, siteId)
            put(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ALTITUDE, altitudeM)
        }
        val id = db.insert(SiteSurveyDbContact.HeadFrameTable.TABLE_NAME, null, values)
        return HeadFrame(id, siteId, altitudeM)
    }

    public fun deleteHeadFrame(db: SQLiteDatabase, headFrame: HeadFrame) {
        db.delete(SiteSurveyDbContact.HeadFrameTable.TABLE_NAME, "${SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ID}=?", arrayOf(headFrame.id.toString()))
    }

    public fun deleteHeadFrame(db: SQLiteDatabase, id: Long) {
        db.delete(SiteSurveyDbContact.HeadFrameTable.TABLE_NAME, "${SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ID}=?", arrayOf(id.toString()))
    }

    public fun updateHeadFrame(db: SQLiteDatabase, headFrame: HeadFrame) : Int {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_SITE_ID, headFrame.siteId)
            put(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ALTITUDE, headFrame.altitudeM)
        }
        return db.update(SiteSurveyDbContact.HeadFrameTable.TABLE_NAME, values, "${SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ID}=?", arrayOf(headFrame.id.toString()))
    }

    public fun updateHeadFrame(db: SQLiteDatabase, id: Long, siteId: Long, altitudeM: Double) : Int {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_SITE_ID, siteId)
            put(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ALTITUDE, altitudeM)
        }
        return db.update(SiteSurveyDbContact.HeadFrameTable.TABLE_NAME, values, "${SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ID}=?", arrayOf(id.toString()))
    }

    public fun getHeadFrame(db: SQLiteDatabase, id: Long) : HeadFrame? {
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
        with(cursor) {
            if (moveToNext()) {
                val headFrame = HeadFrame(
                    getLong(getColumnIndexOrThrow(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ID)),
                    getLong(getColumnIndexOrThrow(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_SITE_ID)),
                    getDouble(getColumnIndexOrThrow(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ALTITUDE))
                )
                close()
                return headFrame
            }
        }
        cursor.close()
        return null
    }

    public fun getAllHeadFrames(db: SQLiteDatabase) : Array<HeadFrame> {
        val projection = arrayOf(
            SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ID,
            SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_SITE_ID,
            SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ALTITUDE
        )
        val cursor = db.query(
            SiteSurveyDbContact.HeadFrameTable.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )
        val items = mutableListOf<HeadFrame>()
        with(cursor) {
            while (moveToNext()) {
                items.add(HeadFrame(
                    getLong(getColumnIndexOrThrow(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ID)),
                    getLong(getColumnIndexOrThrow(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_SITE_ID)),
                    getDouble(getColumnIndexOrThrow(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ALTITUDE))
                ))
            }
            close()
        }
        return items.toTypedArray()
    }

    public fun getHeadFramesBySite(db: SQLiteDatabase, siteId : Long) : Array<HeadFrame> {
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
        with(cursor) {
            while (moveToNext()) {
                items.add(HeadFrame(
                    getLong(getColumnIndexOrThrow(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ID)),
                    getLong(getColumnIndexOrThrow(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_SITE_ID)),
                    getDouble(getColumnIndexOrThrow(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ALTITUDE))
                ))
            }
            close()
        }
        return items.toTypedArray()
    }

    ////////////////////////////
    ///// Tower Scan Table /////
    ////////////////////////////

    public fun insertTowerScan(db: SQLiteDatabase, towerScan: TowerScan) : Long {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_SITE_ID, towerScan.siteId)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_RADIUS, towerScan.radius)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_LATITUDE, towerScan.poiLatitude)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_LONGITUDE, towerScan.poiLongitude)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_ALTITUDE, towerScan.poiAltitude)
        }
        return db.insert(SiteSurveyDbContact.TowerScanTable.TABLE_NAME, null, values)
    }

    public fun insertTowerScan(db: SQLiteDatabase, siteId: Long, lat: Double? = null, long: Double? = null, alt: Double? = null, rad: Double? = null) : TowerScan {
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

    public fun deleteTowerScan(db: SQLiteDatabase, towerScan: TowerScan) {
        db.delete(SiteSurveyDbContact.TowerScanTable.TABLE_NAME, "${SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_SITE_ID}=?", arrayOf(towerScan.siteId.toString()))
    }

    public fun deleteTowerScan(db: SQLiteDatabase, siteId: Long) {
        db.delete(SiteSurveyDbContact.TowerScanTable.TABLE_NAME, "${SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_SITE_ID}=?", arrayOf(siteId.toString()))
    }

    public fun updateTowerScan(db: SQLiteDatabase, towerScan: TowerScan) : Int {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_SITE_ID, towerScan.siteId)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_RADIUS, towerScan.radius)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_LATITUDE, towerScan.poiLatitude)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_LONGITUDE, towerScan.poiLongitude)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_ALTITUDE, towerScan.poiAltitude)
        }
        return db.update(SiteSurveyDbContact.TowerScanTable.TABLE_NAME, values, "${SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_SITE_ID}=?", arrayOf(towerScan.siteId.toString()))
    }

    public fun updateTowerScan(db: SQLiteDatabase, siteId: Long, lat: Double? = null, long: Double? = null, alt: Double? = null, rad: Double? = null) : Int {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_SITE_ID, siteId)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_RADIUS, rad)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_LATITUDE, lat)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_LONGITUDE, long)
            put(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_ALTITUDE, alt)
        }
        return db.update(SiteSurveyDbContact.TowerScanTable.TABLE_NAME, values, "${SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_SITE_ID}=?", arrayOf(siteId.toString()))
    }

    public fun getTowerScan(db: SQLiteDatabase, siteId: Long) : TowerScan? {
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
        with(cursor) {
            if (moveToNext()) {
                val towerScan = TowerScan(
                    getLong(getColumnIndexOrThrow(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_SITE_ID)),
                    getDouble(getColumnIndexOrThrow(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_LATITUDE)),
                    getDouble(getColumnIndexOrThrow(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_LONGITUDE)),
                    getDouble(getColumnIndexOrThrow(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_POI_ALTITUDE)),
                    getDouble(getColumnIndexOrThrow(SiteSurveyDbContact.TowerScanTable.COLUMN_NAME_RADIUS))
                )
                close()
                return towerScan
            }
        }
        cursor.close()
        return null
    }

    //////////////////////
    ///// Save Table /////
    //////////////////////

    public fun insertSave(db: SQLiteDatabase, save: Save) : Long {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_SITE_ID, save.siteId)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_START_LATITUDE, save.startLatitude)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_START_LONGITUDE, save.startLongitude)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_STOP_LATITUDE, save.stopLatitude)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_STOP_LONGITUDE, save.stopLongitude)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_CATEGORY, save.category.ordinal)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_NUMBER, save.number)
        }
        return db.insert(SiteSurveyDbContact.SaveTable.TABLE_NAME, null, values)
    }

    public fun insertSave(db: SQLiteDatabase, siteId: Long, startLat: Double, startLong: Double, stopLat: Double, stopLong: Double, cat: Int, num: Double) : Save {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_SITE_ID, siteId)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_START_LATITUDE, startLat)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_START_LONGITUDE, startLong)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_STOP_LATITUDE, stopLat)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_STOP_LONGITUDE, stopLong)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_CATEGORY, cat)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_NUMBER, num)
        }
        db.insert(SiteSurveyDbContact.SaveTable.TABLE_NAME, null, values)
        return Save(siteId, startLat, startLong, Category.POI, num, stopLat, stopLong)
    }

    public fun deleteSave(db: SQLiteDatabase, save: Save) {
        db.delete(SiteSurveyDbContact.SaveTable.TABLE_NAME, "${SiteSurveyDbContact.SaveTable.COLUMN_NAME_SITE_ID}=?", arrayOf(save.siteId.toString()))
    }

    public fun deleteSave(db: SQLiteDatabase, siteId: Long) {
        db.delete(SiteSurveyDbContact.SaveTable.TABLE_NAME, "${SiteSurveyDbContact.SaveTable.COLUMN_NAME_SITE_ID}=?", arrayOf(siteId.toString()))
    }

    public fun updateSave(db: SQLiteDatabase, save: Save) : Int {
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

    public fun updateSave(db: SQLiteDatabase, siteId: Long, startLat: Double, startLong: Double, stopLat: Double, stopLong: Double, cat: Int, num: Double) : Int {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_SITE_ID, siteId)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_START_LATITUDE, startLat)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_START_LONGITUDE, startLong)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_STOP_LATITUDE, stopLat)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_STOP_LONGITUDE, stopLong)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_CATEGORY, cat)
            put(SiteSurveyDbContact.SaveTable.COLUMN_NAME_NUMBER, num)
        }
        return db.update(SiteSurveyDbContact.SaveTable.TABLE_NAME, values, "${SiteSurveyDbContact.SaveTable.COLUMN_NAME_SITE_ID}=?", arrayOf(siteId.toString()))
    }

    public fun getSave(db: SQLiteDatabase, siteId: Long) : Save? {
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
        with(cursor) {
            if (moveToNext()) {
                val save = Save(
                    getLong(getColumnIndexOrThrow(SiteSurveyDbContact.SaveTable.COLUMN_NAME_SITE_ID)),
                    getDouble(getColumnIndexOrThrow(SiteSurveyDbContact.SaveTable.COLUMN_NAME_START_LATITUDE)),
                    getDouble(getColumnIndexOrThrow(SiteSurveyDbContact.SaveTable.COLUMN_NAME_START_LONGITUDE)),
                    Category.values()[getInt(getColumnIndexOrThrow(SiteSurveyDbContact.SaveTable.COLUMN_NAME_CATEGORY))],
                    getDouble(getColumnIndexOrThrow(SiteSurveyDbContact.SaveTable.COLUMN_NAME_NUMBER)),
                    getDouble(getColumnIndexOrThrow(SiteSurveyDbContact.SaveTable.COLUMN_NAME_STOP_LATITUDE)),
                    getDouble(getColumnIndexOrThrow(SiteSurveyDbContact.SaveTable.COLUMN_NAME_STOP_LATITUDE))
                )
                close()
                return save
            }
        }
        cursor.close()
        return null
    }
}