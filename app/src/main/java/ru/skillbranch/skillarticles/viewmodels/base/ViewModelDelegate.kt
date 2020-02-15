package ru.skillbranch.skillarticles.viewmodels.base

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ViewModelDelegate<T: ViewModel>(private val clazz: Class<T>, private val arg: Any?) : ReadOnlyProperty<FragmentActivity, T> {
    private lateinit var value: T

    override fun getValue(thisRef: FragmentActivity, property: KProperty<*>): T {
        if (!::value.isInitialized) {
            value = when (arg) {
                null -> ViewModelProviders.of(thisRef).get(clazz)
                else -> ViewModelProviders.of(thisRef, ViewModelFactory(arg)).get(clazz)
            }
        }
        return value
    }
}