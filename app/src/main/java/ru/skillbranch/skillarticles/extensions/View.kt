package ru.skillbranch.skillarticles.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.navigation.NavDestination
import com.google.android.material.bottomnavigation.BottomNavigationView

fun View.setMarginOptionally(top: Int = 0, right: Int = 0, bottom: Int = 0, left: Int = 0) {
    (layoutParams as ViewGroup.MarginLayoutParams).run {
        leftMargin = left
        rightMargin = right
        topMargin = top
        bottomMargin = bottom
    }
    requestLayout()
}

fun View.setPaddingOptionally(
    left:Int = paddingLeft,
    top : Int = paddingTop,
    right : Int = paddingRight,
    bottom : Int = paddingBottom
){
    setPadding(left, top, right, bottom)
}

fun BottomNavigationView.selectDestination(destination: NavDestination) {
    menu.findItem(destination.id)?.let {
        it.isChecked = true
    } ?: run { menu.children.last().isChecked = true }
}