package za.co.lsmc.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import za.co.lsmc.data.Category
import za.co.lsmc.data.database.SiteSurveyDbHelper
import za.co.lsmc.data.entities.Photo
import za.co.lsmc.data.entities.Site
import za.co.lsmc.data.repositories.PhotoRepository
import za.co.lsmc.data.repositories.SiteRepository

class LSSASiteSurveyViewModel: ViewModel() {
    private lateinit var siteRepository: SiteRepository
    private lateinit var photoRepository: PhotoRepository
    private var isRepositoriesInitialized = false

    var site: Site? = null
        set(value) {
            field = value
            if (value != null && isRepositoriesInitialized) {
                initializePhotoCountersFromDatabase()
            }
        }

    private val _sites = MutableLiveData<List<Site>>()
    val sites: LiveData<List<Site>> = _sites

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val photoCounters = mutableMapOf<Category, Double>().apply {
        Category.values().forEach { category ->
            this[category] = 1.0
        }
    }

    fun initDbHelper(dbHelper: SiteSurveyDbHelper) {
        this.siteRepository = SiteRepository(dbHelper)
        this.photoRepository = PhotoRepository(dbHelper)
        this.isRepositoriesInitialized = true

        site?.let { initializePhotoCountersFromDatabase() }
    }

    fun isInitialized(): Boolean = isRepositoriesInitialized

    fun loadSites(): Array<Site> {
        if (!isRepositoriesInitialized) {
            throw IllegalStateException("Repositories not initialized. Call initDbHelper() first.")
        }

        return runBlocking {
            _isLoading.value = true
            try {
                val siteArray = siteRepository.getAllSites()
                _sites.value = siteArray.toList()
                _error.value = null
                siteArray
            } catch (e: Exception) {
                _error.value = "Failed to load sites: ${e.message}"
                Log.e("ViewModel", "Error loading sites", e)
                emptyArray()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addSite(name: String): Site {
        if (!isRepositoriesInitialized) {
            throw IllegalStateException("Repositories not initialized. Call initDbHelper() first.")
        }

        return runBlocking {
            _isLoading.value = true
            try {
                val newSite = siteRepository.insertSite(name)
                loadSitesAsync()
                _error.value = null
                newSite
            } catch (e: Exception) {
                _error.value = "Failed to add site: ${e.message}"
                Log.e("ViewModel", "Error adding site", e)
                throw e
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSite() {
        if (!isRepositoriesInitialized) {
            throw IllegalStateException("Repositories not initialized. Call initDbHelper() first.")
        }

        val currentSite = site ?: throw IllegalStateException("A site has not been selected.")

        viewModelScope.launch {
            _isLoading.value = true
            try {
                siteRepository.deleteSite(currentSite)
                site = null
                loadSitesAsync() // Refresh the list
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to delete site: ${e.message}"
                Log.e("ViewModel", "Error deleting site", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadSitesAsync() {
        viewModelScope.launch {
            try {
                val siteArray = siteRepository.getAllSites()
                _sites.value = siteArray.toList()
            } catch (e: Exception) {
                Log.e("ViewModel", "Error loading sites async", e)
            }
        }
    }

    fun savePhotoToDatabase(
        filename: String,
        category: Category,
        callback: (Photo) -> Unit
    ) {
        if (!isRepositoriesInitialized) {
            throw IllegalStateException("Repositories not initialized. Call initDbHelper() first.")
        }

        val currentSite = site ?: throw IllegalStateException("A site has not been selected.")

        viewModelScope.launch {
            try {
                val currentNumber = photoCounters[category] ?: 1.0
                val savedPhoto = photoRepository.insertPhoto(
                    currentSite.id,
                    filename,
                    category,
                    currentNumber
                )

                photoCounters[category] = currentNumber + 1.0

                withContext(Dispatchers.Main) {
                    callback(savedPhoto)
                }

            } catch (e: Exception) {
                Log.e("ViewModel", "Error saving photo to database", e)
                withContext(Dispatchers.Main) {
                    val currentNumber = photoCounters[category] ?: 1.0
                    callback(Photo(0, 0, filename, category, currentNumber))
                }
            }
        }
    }

    fun getPhotosForCurrentSiteByCategory(category: Category): LiveData<List<Photo>> {
        val photosLiveData = MutableLiveData<List<Photo>>()

        if (!isRepositoriesInitialized) {
            photosLiveData.value = emptyList()
            return photosLiveData
        }

        val currentSite = site

        if (currentSite != null) {
            viewModelScope.launch {
                try {
                    val photos = photoRepository.getPhotosBySiteAndCategory(currentSite.id, category)
                    photosLiveData.postValue(photos)
                } catch (e: Exception) {
                    Log.e("ViewModel", "Error getting photos by category", e)
                    photosLiveData.postValue(emptyList())
                }
            }
        } else {
            photosLiveData.value = emptyList()
        }

        return photosLiveData
    }

    fun getPhotosForCurrentSite(): LiveData<List<Photo>> {
        val photosLiveData = MutableLiveData<List<Photo>>()

        if (!isRepositoriesInitialized) {
            photosLiveData.value = emptyList()
            return photosLiveData
        }

        val currentSite = site

        if (currentSite != null) {
            viewModelScope.launch {
                try {
                    val photos = photoRepository.getPhotosBySite(currentSite.id)
                    photosLiveData.postValue(photos)
                } catch (e: Exception) {
                    Log.e("ViewModel", "Error getting photos", e)
                    photosLiveData.postValue(emptyList())
                }
            }
        } else {
            photosLiveData.value = emptyList()
        }

        return photosLiveData
    }

    fun getSitePhotoCount(siteId: Long): Int {
        if (!isRepositoriesInitialized) {
            return 0
        }

        return runBlocking {
            try {
                siteRepository.getSitePhotoCount(siteId)
            } catch (e: Exception) {
                Log.e("LSSASiteSurveyViewModel", "Error getting photo count", e)
                0
            }
        }
    }

    fun deletePhoto(photoId: Long) {
        if (!isRepositoriesInitialized) {
            return
        }

        viewModelScope.launch {
            try {
                photoRepository.deletePhoto(photoId)
                // Optionally refresh photos after deletion
            } catch (e: Exception) {
                _error.value = "Failed to delete photo: ${e.message}"
                Log.e("ViewModel", "Error deleting photo", e)
            }
        }
    }

    fun getPhotoCounterForCategory(category: Category): Double {
        return photoCounters[category] ?: 1.0
    }

    fun initializePhotoCountersFromDatabase() {
        if (!isRepositoriesInitialized) {
            return
        }

        val currentSite = site ?: return

        viewModelScope.launch {
            try {
                Category.values().forEach { category ->
                    val maxNumber = photoRepository.getMaxPhotoNumberForCategory(currentSite.id, category)
                    photoCounters[category] = (maxNumber ?: 0.0) + 1.0
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error initializing photo counters", e)
                resetPhotoCounters()
            }
        }
    }

    private fun resetPhotoCounters() {
        Category.values().forEach { category ->
            photoCounters[category] = 1.0
        }
    }

    fun clearError() {
        _error.value = null
    }
}