package za.co.lsmc.viewmodels

import android.app.Application
import android.util.Log
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