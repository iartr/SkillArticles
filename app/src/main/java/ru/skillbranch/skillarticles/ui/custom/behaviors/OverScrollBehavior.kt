package ru.skillbranch.skillarticles.ui.custom.behaviors

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.ColorUtils
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import ru.skillbranch.skillarticles.R
import kotlin.math.max
import kotlin.math.min

class OverScrollBehavior() : AppBarLayout.Behavior() {
    constructor(ctx: Context, attrs: AttributeSet) : this()

    private lateinit var targetView: View
    private lateinit var colapsingView: CollapsingToolbarLayout
    private var targetHeight: Int = 0
    private var parentHeight: Int = 0
    private var totalDy: Int = 0
    private var lastScale: Float = 0f
    private var lastBottom: Int = 0
    private var isStoped: Boolean = false


    override fun onLayoutChild(
        parent: CoordinatorLayout,
        abl: AppBarLayout,
        layoutDirection: Int
    ): Boolean {
        val superLayout = super.onLayoutChild(parent, abl, layoutDirection)
        if (!::targetView.isInitialized) initialize(abl)
        return superLayout
    }

    //observe only vertical axis
    override fun onStartNestedScroll(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        directTargetChild: View,
        target: View,
        nestedScrollAxes: Int,
        type: Int
    ): Boolean {
        isStoped = false
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        abl: AppBarLayout,
        target: View,
        type: Int
    ) {
        isStoped = true
        //restore size if scroll stop
        restore(abl)
        super.onStopNestedScroll(coordinatorLayout, abl, target, type)
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: AppBarLayout,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        val ablBottom = child.bottom

        //scale if scroll down and scroll up before scroll stop
        if ((dy < 0 && ablBottom >= parentHeight) || (dy > 0 && ablBottom > parentHeight)) {
            scale(child, dy)
        }else super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
    }


    private fun initialize(abl: AppBarLayout) {
        targetView = abl.findViewById(R.id.iv_product)
        colapsingView = abl.getChildAt(0) as CollapsingToolbarLayout
        parentHeight = abl.height
        targetHeight = targetView.height
    }

    //restore appbar size
    private fun restore(abl: AppBarLayout) {
        if (totalDy > 0) {
            totalDy = 0
            val anim = ValueAnimator.ofFloat(lastScale, 1f)
            anim.addUpdateListener {
                val value = it.animatedValue as Float
                targetView.scaleX = value
                targetView.scaleY = value
                val bottomValue =
                    (lastBottom - (lastBottom - parentHeight) * it.animatedFraction).toInt()
                abl.bottom = bottomValue
                colapsingView.bottom = bottomValue
                animateTitleColor(it.animatedFraction)
            }
            anim.start()
        }
    }

    //scale appbar and image
    private fun scale(abl: AppBarLayout, dY: Int) {
        //don`t scale if scroll end
        if (isStoped) return
        totalDy += -dY
        totalDy = min(totalDy, targetHeight)
        lastScale = max(1f, 1f + totalDy.toFloat() / targetHeight)
        targetView.scaleX = lastScale
        targetView.scaleY = lastScale

        lastBottom = parentHeight + (targetHeight / 2 * (lastScale - 1)).toInt()
        abl.bottom = lastBottom
        colapsingView.bottom = lastBottom
        animateTitleColor(1f - totalDy.toFloat()/targetHeight)
    }

    private fun animateTitleColor(fraction:Float){
        val alpha = MathUtils.clamp(255*fraction, 0f, 255f).toInt()
        val color = ColorUtils.setAlphaComponent(Color.WHITE, alpha)
        colapsingView.setExpandedTitleColor(color)
    }

}