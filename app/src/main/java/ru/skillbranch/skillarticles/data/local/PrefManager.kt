package ru.skillbranch.skillarticles.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import ru.skillbranch.skillarticles.App
import ru.skillbranch.skillarticles.data.models.AppSettings

object PrefManager {
    val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(App.applicationContext())
    }

    fun clearAll() {
        preferences.edit { clear() }
    }

    fun getAppSettings(): LiveData<AppSettings> {
        // TODO: implement me
        return MutableLiveData(AppSettings())
    }

    fun isAuth(): MutableLiveData<Boolean> {
        // TODO: implement me
        return MutableLiveData(false)
    }

    fun setAuth(auth: Boolean) {
        // TODO: implement me
    }
}