package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import okhttp3.MultipartBody
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.models.User
import ru.skillbranch.skillarticles.data.remote.NetworkManager
import ru.skillbranch.skillarticles.data.remote.req.EditProfileReq

interface IRepository {
    fun getProfile(): LiveData<User?>
    suspend fun uploadAvatar(body: MultipartBody.Part)
    suspend fun removeAvatar()
    suspend fun editProfile(name: String, about: String)
}

object ProfileRepository : IRepository {
    private val prefs = PrefManager
    private val network = NetworkManager.api

    override fun getProfile() = prefs.profileLive

    override suspend fun uploadAvatar(body: MultipartBody.Part) {
        val (url) = network.upload(body, prefs.accessToken)
        prefs.profile = prefs.profile!!.copy(avatar = url)
    }

    override suspend fun removeAvatar() {
        network.removeProfileAvatar(prefs.accessToken)
        prefs.profile = prefs.profile!!.copy(avatar = "")
    }

    override suspend fun editProfile(name: String, about: String) {
        val user = network.editProfile(EditProfileReq(name, about), prefs.accessToken)
        prefs.profile = user
    }
}