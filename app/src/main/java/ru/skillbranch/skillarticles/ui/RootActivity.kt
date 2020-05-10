package ru.skillbranch.skillarticles.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_root.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.selectDestination
import ru.skillbranch.skillarticles.extensions.selectItem
import ru.skillbranch.skillarticles.ui.base.BaseActivity
import ru.skillbranch.skillarticles.ui.custom.Bottombar
import ru.skillbranch.skillarticles.viewmodels.RootViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand
import ru.skillbranch.skillarticles.viewmodels.base.Notify

class RootActivity : BaseActivity<RootViewModel>() {

    override val layout: Int = R.layout.activity_root
    public override val viewModel: RootViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_articles, R.id.nav_bookmarks, R.id.nav_transcriptions, R.id.nav_profile)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        nav_view.setOnNavigationItemSelectedListener {
            viewModel.navigate(NavigationCommand.To(it.itemId))
            true
        }

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            nav_view.selectDestination(destination)

            if (destination.id == R.id.nav_auth) nav_view.selectItem(arguments?.get("private_destination") as Int?)

            if (viewModel.currentState.isAuth && destination.id == R.id.nav_auth) {
                controller.popBackStack()
                viewModel.navigate(NavigationCommand.To(R.id.nav_profile, arguments))
            }
        }
    }

    override fun subscribeOnState(state: IViewModelState) {
        // DO something
    }

    override fun renderNotification(notify: Notify) {
        val snackbar = Snackbar.make(container, notify.message, Snackbar.LENGTH_LONG)
        snackbar.anchorView = findViewById<Bottombar?>(R.id.bottombar) ?: nav_view

        when (notify) {
            is Notify.TextMessage -> { }

            is Notify.ActionMessage -> {
                snackbar.setAction(notify.actionLabel) { notify.actionHandler.invoke() }
            }

            is Notify.ErrorMessage -> {
                with(snackbar) {
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    setAction(notify.errLabel) { notify.errHandler?.invoke() }
                }
            }
        }

        snackbar.show()
    }
}
