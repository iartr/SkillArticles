package ru.skillbranch.skillarticles.ui.custom

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.Checkable
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.skillbranch.skillarticles.R

class CustomFab @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FloatingActionButton(context, attrs, defStyleAttr), Checkable {
    private var checked = false
    private val animation: AnimatorSet

    init {
        val colorAccent = context.getColor(R.color.color_accent)
        val colorWhite = context.getColor(android.R.color.white)

        val rotateAnim = ObjectAnimator.ofFloat(this, "rotation", 135f)

        val iconAnim = ValueAnimator.ofArgb(colorWhite, colorAccent)
        iconAnim.addUpdateListener { imageTintList = ColorStateList.valueOf(it.animatedValue as Int) }

        val bgAnim = ValueAnimator.ofArgb(colorAccent, colorWhite)
        bgAnim.addUpdateListener { backgroundTintList = ColorStateList.valueOf(it.animatedValue as Int) }

        animation = AnimatorSet().apply {
            interpolator = FastOutSlowInInterpolator()
            playTogether(rotateAnim, iconAnim, bgAnim)
        }
    }

    override fun performClick(): Boolean {
        toggle()
        return super.performClick()
    }

    override fun isChecked() = checked

    override fun toggle() {
        isChecked = !checked
    }

    override fun setChecked(check: Boolean) {
        if (checked == check) return
        checked = check
        playAnimation()
    }

    private fun playAnimation() {
        if (isChecked) animation.start() else animation.reverse()
    }
}