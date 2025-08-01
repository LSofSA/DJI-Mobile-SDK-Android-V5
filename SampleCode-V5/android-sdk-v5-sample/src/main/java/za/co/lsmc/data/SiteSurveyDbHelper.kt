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

    public fun insertHeadFrame(db: SQLiteDatabase, headFrame: HeadFrame) : Long {
        val values = ContentValues().apply {
            put(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_SITE_ID, headFrame.siteId)
            put(SiteSurveyDbContact.HeadFrameTable.COLUMN_NAME_ALTITUDE, headFrame.altitudeM)
        }
        return db.insert(SiteSurveyDbContact.HeadFrameTable.TABLE_NAME, null, values)
    }

    
}