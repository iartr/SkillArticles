package ru.skillbranch.skillarticles.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.floatingactionbutton.FloatingActionButton

fun FloatingActionButton.animateWithTranslation(translationX: Float, translationY: Float) {
    ObjectAnimator.ofPropertyValuesHolder(
        this,
        PropertyValuesHolder.ofFloat("translationX", this.translationX, translationX),
        PropertyValuesHolder.ofFloat("translationY", this.translationY, translationY)
    ).apply {
        duration = 300
        interpolator = FastOutSlowInInterpolator()
    }.start()
}

fun FloatingActionButton.init(translationX: Float, translationY: Float) {
    visibility = View.INVISIBLE

    addOnShowAnimationListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator?) {
            animateWithTranslation(translationX, translationY)
            super.onAnimationStart(animation)
        }
    })

    addOnHideAnimationListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator?) {
            animateWithTranslation(0f, 0f)
            super.onAnimationStart(animation)
        }
    })
}