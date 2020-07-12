package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.local.PrefManager

object RootRepository {
    private val prefs = PrefManager

    fun isAuth(): LiveData<Boolean> = prefs.isAuthLiveData
    fun setAuth(auth: Boolean) {
        prefs.isAuth = auth
    }
}