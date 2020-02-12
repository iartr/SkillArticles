package ru.skillbranch.skillarticles.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState

abstract class BaseActivity<T: BaseViewModel<out IViewModelState>> : AppCompatActivity() {
    protected abstract var viewModel: T
    protected abstract val layout: Int

    // set listeners, configure views
    abstract fun setupViews()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)
        setupViews()
    }
}