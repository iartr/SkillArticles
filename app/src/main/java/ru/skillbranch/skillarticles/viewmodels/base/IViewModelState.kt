package ru.skillbranch.skillarticles.viewmodels.base

import androidx.lifecycle.SavedStateHandle

interface IViewModelState {
    /**
    * override if need save state in bundle
    */
    fun save(outState: SavedStateHandle) {
        // default implementation
    }

    /**
     * override if need restore state from bundle
     */
    fun restore(savedState: SavedStateHandle) : IViewModelState {
        // default implementation
        return this
    }
}