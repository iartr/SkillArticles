package ru.skillbranch.skillarticles.extensions

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.view.iterator
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
    for (item in menu.iterator()) {
        if (matchDestination(destination, item.itemId)) {
            item.isChecked = true
        }
    }
}

fun BottomNavigationView.selectItem(itemId: Int?){
    itemId?: return
    for (item in menu.iterator()) {
        if(item.itemId == itemId) {
            item.isChecked = true
            break
        }
    }
}

private fun matchDestination(destination: NavDestination, @IdRes destId: Int) : Boolean{
    var currentDestination: NavDestination? = destination
    while (currentDestination!!.id != destId && currentDestination.parent != null) {
        currentDestination = currentDestination.parent
    }
    return currentDestination.id == destId
}