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
import za.co.lsmc.models.enums.Category
import za.co.lsmc.data.database.SiteSurveyDbHelper
import za.co.lsmc.data.entities.HeadFrame
import za.co.lsmc.data.entities.Photo
import za.co.lsmc.data.entities.Site
import za.co.lsmc.data.repositories.SiteSurveyRepository

class LSSASiteSurveyViewModel: ViewModel() {
    private lateinit var repository: SiteSurveyRepository
    private var isRepositoriesInitialized = false

    var site: Site? = null
        set(value) {
            field = value
            if (value != null && isRepositoriesInitialized) {
                initializePhotoCounter()
            }
        }

    private val _photos = MutableLiveData<List<Photo>>()
    val photos: LiveData<List<Photo>> = _photos

    private val _sites = MutableLiveData<List<Site>>()
    val sites: LiveData<List<Site>> = _sites

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _headFrames = MutableLiveData<List<HeadFrame>>()
    val headFrames: LiveData<List<HeadFrame>> = _headFrames

    private val photoCounters = mutableMapOf<Category, Double>().apply {
        Category.values().forEach { category ->
            this[category] = 1.0
        }
    }

    fun initDbHelper(dbHelper: SiteSurveyDbHelper) {
        this.repository = SiteSurveyRepository(dbHelper)
        this.isRepositoriesInitialized = true

        site?.let { initializePhotoCounter() }
    }

    fun isInitialized(): Boolean = isRepositoriesInitialized

    fun loadSites() {
        if (!isRepositoriesInitialized) {
            _error.value = "Repositories not initialized. Call initDbHelper() first."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val sitesList = repository.getAllSites()
                _sites.value = sitesList
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load sites: ${e.message}"
                _sites.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun addSite(name: String) {
        if (!isRepositoriesInitialized) {
            _error.value = "Repositories not initialized. Call initDbHelper() first."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newSite = repository.insertSite(name)
                val updatedSites = repository.getAllSites()
                _sites.value = updatedSites
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to add site: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSite() {
        if (!isRepositoriesInitialized) {
            _error.value = "Repositories not initialized. Call initDbHelper() first."
            return
        }

        site?.let { currentSite ->
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    repository.deleteSite(currentSite)

                    val updatedSites = repository.getAllSites()
                    _sites.value = updatedSites
                    _error.value = null
                } catch (e: Exception) {
                    _error.value = "Failed to delete site: ${e.message}"
                    Log.e("ViewModel", "Error deleting site", e)
                } finally {
                    _isLoading.value = false
                }
            }
        } ?: run {
            _error.value = "No site selected for deletion"
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
                val savedPhoto = repository.insertPhoto(
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

        site?.let {
            viewModelScope.launch {
                try {
                    val photos = repository.getPhotosBySiteAndCategory(it.id, category)
                    photosLiveData.postValue(photos)
                } catch (e: Exception) {
                    Log.e("ViewModel", "Error getting photos by category", e)
                    photosLiveData.postValue(emptyList())
                }
            }
        } ?:  {
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

        site?.let {
            viewModelScope.launch {
                try {
                    val photos = repository.getPhotosBySite(it.id)
                    photosLiveData.postValue(photos)
                } catch (e: Exception) {
                    Log.e("ViewModel", "Error getting photos", e)
                    photosLiveData.postValue(emptyList())
                }
            }
        } ?: { photosLiveData.value = emptyList() }

        return photosLiveData
    }

    fun getSitePhotoCount(siteId: Long): Int {
        if (!isRepositoriesInitialized) {
            return 0
        }

        return runBlocking {
            try {
                repository.getPhotosBySite(siteId).size
            } catch (e: Exception) {
                Log.e("LSSASiteSurveyViewModel", "Error getting photo count", e)
                0
            }
        }
    }

    fun deletePhoto(photo: Photo) {
        if (!isRepositoriesInitialized) {
            return
        }

        viewModelScope.launch {
            try {
                repository.deletePhoto(photo)
            } catch (e: Exception) {
                _error.value = "Failed to delete photo: ${e.message}"
                Log.e("ViewModel", "Error deleting photo", e)
            }
        }
    }

    fun getPhotoCounterForCategory(category: Category): Double {
        return photoCounters[category] ?: 1.0
    }

    fun initializePhotoCounter() {
        if (!isRepositoriesInitialized) {
            return
        }

        val currentSite = site ?: return

        viewModelScope.launch {
            try {
                for (category in Category.values()) {
                    try {
                        val maxNumber = repository.getMaxPhotoNumberForCategory(currentSite.id, category)
                        photoCounters[category] = (maxNumber ?: 0.0) + 1.0
                        kotlinx.coroutines.delay(10)
                    } catch (e: Exception) {
                        Log.e("ViewModel", "Error initializing counter for category $category", e)
                        photoCounters[category] = 1.0
                    }
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error initializing photo counters", e)
                resetPhotoCounters()
            }
        }
    }

    suspend fun addHeadFrame(altitude: Double): Result<HeadFrame> {
        return try {
            site?.let { currentSite ->
                val newHeadFrame = repository.insertHeadFrame(currentSite.id, altitude)
                refreshHeadFrames()
                Result.success(newHeadFrame)
            } ?: Result.failure(Exception("No site selected"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshHeadFrames() {
        site?.let { currentSite ->
            val headFrames = repository.getHeadFramesBySite(currentSite.id)
            _headFrames.postValue(headFrames)
        }
    }

    suspend fun refreshPhotos() {
        site?.let { currentSite ->
            val photos = repository.getPhotosBySite(currentSite.id)
            _photos.postValue(photos)
        }
    }


    suspend fun deleteHeadFrame(headFrame: HeadFrame): Result<Unit> {
        return try {
            repository.deleteHeadFrame(headFrame)
            refreshHeadFrames()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHeadFramesForCurrentSite(): List<HeadFrame> {
        return site?.let { currentSite ->
            repository.getHeadFramesBySite(currentSite.id)
        } ?: emptyList()
    }

    suspend fun capturePhoto(filename: String, category: Category, altitude: Double = 0.0): Result<Photo> {
        return try {
            site?.let { currentSite ->
                val newPhoto = repository.insertPhoto(currentSite.id, filename, category, altitude)
                refreshPhotos()
                Result.success(newPhoto)
            } ?: Result.failure(Exception("No site selected"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasPhotoForCategory(category: Category): Boolean {
        if (!isRepositoriesInitialized) {
            Log.w("ViewModel", "Repositories not initialized when checking photo category")
            return false
        }

        return try {
            site?.let { currentSite ->
                repository.hasPhotoForCategory(currentSite.id, category)
            } ?: false
        } catch (e: Exception) {
            Log.e("ViewModel", "Error checking photo existence for category $category", e)
            false
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

    suspend fun setTowerScanRadius(radius: Double): Result<Unit> {
        if (!isRepositoriesInitialized) {
            return Result.failure(Exception("Repositories not initialized"))
        }

        return try {
            site?.let { currentSite ->
                repository.updateTowerScan(currentSite.id, rad = radius)
                Result.success(Unit)
            } ?: Result.failure(Exception("No site selected"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTowerScanRadius(): Double? {
        if (!isRepositoriesInitialized) return null

        return try {
            site?.let { currentSite ->
                repository.getTowerScan(currentSite.id)?.radius
            }
        } catch (e: Exception) {
            Log.e("ViewModel", "Error getting tower scan radius", e)
            null
        }
    }
}