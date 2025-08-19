package za.co.lsmc.data.repositories

import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.lsmc.data.database.SiteSurveyDbHelper
import za.co.lsmc.data.entities.Site

class SiteRepository(private val dbHelper: SiteSurveyDbHelper) {

    suspend fun getAllSites(): Array<Site> = withContext(Dispatchers.IO) {
        var db: SQLiteDatabase? = null
        try {
            db = dbHelper.readableDatabase
            dbHelper.getAllSites(db)
        } finally {
            db?.close()
        }
    }

    suspend fun insertSite(name: String): Site = withContext(Dispatchers.IO) {
        var db: SQLiteDatabase? = null
        try {
            db = dbHelper.writableDatabase
            dbHelper.insertSite(db, name, null)
        } finally {
            db?.close()
        }
    }

    suspend fun deleteSite(site: Site): Unit = withContext(Dispatchers.IO) {
        var db: SQLiteDatabase? = null
        try {
            db = dbHelper.writableDatabase
            dbHelper.deleteSite(db, site)
        } finally {
            db?.close()
        }
    }

    suspend fun getSitePhotoCount(siteId: Long): Int = withContext(Dispatchers.IO) {
        var db: SQLiteDatabase? = null
        try {
            db = dbHelper.readableDatabase
            dbHelper.getPhotosBySite(db, siteId).size
        } finally {
            db?.close()
        }
    }
}