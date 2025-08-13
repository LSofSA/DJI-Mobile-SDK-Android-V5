package za.co.lsmc.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.lsmc.data.Site
import za.co.lsmc.data.SiteSurveyDbHelper
import java.util.Date

class LSSASiteSurveyViewModel() : ViewModel() {
    private lateinit var dbHelper: SiteSurveyDbHelper
    var site: Site? = null

    fun initDbHelper(dbHelper: SiteSurveyDbHelper) {
        this.dbHelper = dbHelper
    }

    fun loadSites() : Array<Site> {
        var db: SQLiteDatabase? = null
        try {
            db = dbHelper.readableDatabase
            return dbHelper.getAllSites(db)
        } finally {
            db?.close()
        }
    }

    fun addSite(name: String) : Site {
        var db: SQLiteDatabase? = null
        var newSite: Site
        try {
            db = dbHelper.writableDatabase
            newSite = dbHelper.insertSite(db, name, null)
        } finally {
            db?.close()
        }
        return newSite
    }

    fun deleteSite() {
        var db: SQLiteDatabase? = null
        try {
            db = dbHelper.writableDatabase
            dbHelper.deleteSite(db, site ?: throw IllegalStateException("A site has not been selected."))
        } finally {
            db?.close()
        }
    }
}

class LSSASiteSurveyViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LSSASiteSurveyViewModel::class.java)) {
            return LSSASiteSurveyViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}