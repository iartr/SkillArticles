package ru.skillbranch.skillarticles.viewmodels.auth

import androidx.lifecycle.SavedStateHandle
import ru.skillbranch.skillarticles.data.repositories.RootRepository
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand
import ru.skillbranch.skillarticles.viewmodels.base.Notify

class AuthViewModel(handle: SavedStateHandle) : BaseViewModel<AuthState>(handle, AuthState()), IAuthViewModel {
    private val repository = RootRepository

    init {
        subscribeOnDataSource(repository.isAuth()) { isAuth, state ->
            state.copy(isAuth = isAuth)
        }
    }

    override fun handleLogin(login: String, pass: String, dest: Int?) {
        launchSafety {
            repository.login(login, pass)
            navigate(NavigationCommand.FinishLogin(dest))
        }

    }

    fun handleRegister(name: String, login: String, password: String, dest: Int?) {
        if (name.isEmpty() || login.isEmpty() || password.isEmpty()) {
            notify(Notify.ErrorMessage("Name, login, password it is required fields and not must be empty"))
            return
        }

        if (!validateName(name)) {
            notify(Notify.ErrorMessage("The name must be at least 3 characters long and contain only letters and numbers and can also contain the characters \"-\" and \"_\""))
            return
        }

        if (!validateEmail(login)) {
            notify(Notify.ErrorMessage("Incorrect Email entered"))
            return
        }

        if (!validatePassword(password)) {
            notify(Notify.ErrorMessage("Password must be at least 8 characters long and contain only letters and numbers"))
            return
        }


        /*launchSafety {
            repository.login(login, password)
            navigate(NavigationCommand.FinishLogin(dest))
        }*/

    }

    private fun validateEmail(email: String): Boolean {
        val reg = "^\\S+@\\S+\\.\\S+$".toRegex()
        return reg.matches(email)
    }

    private fun validatePassword(password: String): Boolean {
        val reg = "^[a-zA-Z0-9]{8,}\$".toRegex()
        return reg.matches(password)
    }

    private fun validateName(name: String): Boolean {
        val reg = "^[a-zA-Z0-9_-]{2,}\$".toRegex()
        return reg.matches(name)
    }
}

data class AuthState(val isAuth: Boolean = false) : IViewModelState