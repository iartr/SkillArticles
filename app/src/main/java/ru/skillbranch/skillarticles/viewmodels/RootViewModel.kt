package ru.skillbranch.skillarticles.viewmodels

import androidx.lifecycle.SavedStateHandle
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.repositories.RootRepository
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand

class RootViewModel(handle: SavedStateHandle) : BaseViewModel<RootState>(handle, RootState()) {
    private val repository = RootRepository
    private val privateRoutes = listOf(R.id.nav_profile)

    init {
        subscribeOnDataSource(repository.isAuth()) { isAuth, currentState ->
            currentState.copy(isAuth = isAuth)
        }
    }

    override fun navigate(navigationCommand: NavigationCommand) {
        when (navigationCommand) {
            is NavigationCommand.To -> {
                if (privateRoutes.contains(navigationCommand.destination) && !currentState.isAuth) {
                    super.navigate(NavigationCommand.StartLogin(navigationCommand.destination))
                } else {
                    super.navigate(navigationCommand)
                }
            }
            else -> super.navigate(navigationCommand)
        }
    }
}

data class RootState(val isAuth: Boolean = false) : IViewModelState