package com.bugtsa.casher.ui.screens.settings

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bugtsa.casher.domain.prefs.PreferenceRepository
import com.bugtsa.casher.utils.ThemeHelper.lightMode
import toothpick.Toothpick
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsViewModelFactory @Inject constructor(private val app: Application) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T  =
            Toothpick.openScope(app).getInstance(modelClass) as T
}

class SettingsViewModel @Inject constructor(private val preferenceRepo: PreferenceRepository): ViewModel() {

    private val modeThemeLiveData = MutableLiveData<Boolean>()
    fun observeModelTheme(): LiveData<Boolean> = modeThemeLiveData

    init {
        modeThemeLiveData.value = preferenceRepo.getModeTheme() != lightMode
    }

    fun saveModeTheme(modeTheme: String) {
        preferenceRepo.saveModeTheme(modeTheme)
    }
}