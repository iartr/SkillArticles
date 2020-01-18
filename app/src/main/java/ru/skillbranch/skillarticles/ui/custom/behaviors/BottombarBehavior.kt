package ru.skillbranch.skillarticles.ui.custom.behaviors

import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.custom.Bottombar

class BottombarBehavior: CoordinatorLayout.Behavior<Bottombar>() {
    private var topBound = 0
    private var bottomBound = 0
    private var interceptingEvents = false
    lateinit var dragHelper: ViewDragHelper

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: Bottombar,
        layoutDirection: Int
    ): Boolean {
        //onLayout child on parent
        parent.onLayoutChild(child, layoutDirection)
        if (!::dragHelper.isInitialized) initialize(parent, child)
        //if open add offset
        if (child.isClose) ViewCompat.offsetTopAndBottom(child, bottomBound - topBound)
        //handle onLayout manually
        return true
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: Bottombar,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: Bottombar,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        // dy < 0: scroll down
        // dy > 0: scroll up
        val offset = MathUtils.clamp(child.translationY + dy, 0f, child.minHeight.toFloat())
        if (offset != child.translationY) child.translationY = offset
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: Bottombar,
        ev: MotionEvent
    ): Boolean {
        when(ev.actionMasked){
            //if action down in child area -> intercept
            MotionEvent.ACTION_DOWN -> interceptingEvents = parent.isPointInChildBounds(child, ev.x.toInt(), ev.y.toInt())
            //if action cancel or up -> not intercept
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> interceptingEvents = false
        }

        return if(interceptingEvents) dragHelper.shouldInterceptTouchEvent(ev)
        else false
    }

    override fun onTouchEvent(
        parent: CoordinatorLayout,
        child: Bottombar,
        ev: MotionEvent
    ): Boolean {
        //delegate handle touch event to drag helper
        dragHelper.processTouchEvent(ev)
        return true
    }

    private fun initialize(parent: CoordinatorLayout, child: Bottombar) {
        dragHelper = ViewDragHelper.create(parent, 1f, DragHelperCallback())
        topBound = parent.height - child.height
        bottomBound = parent.height - child.minHeight
        val webView = child.findViewById<WebView>(R.id.webview)
        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://static-maps.yandex.ru/1.x/?ll=-18.783719,64.881884&size=400,300&l=sat&z=9")
    }

    inner class DragHelperCallback : ViewDragHelper.Callback() {
        //mark view draggable
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child is Bottombar
        }

        //define vertical drag range
        override fun getViewVerticalDragRange(child: View): Int {
            return bottomBound - topBound
        }

        //define drag bounds
        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return MathUtils.clamp(top, topBound, bottomBound)
        }

        //if view released (action uo or action cancel)
        override fun onViewReleased(view: View, xvel: Float, yvel: Float) {
            view as Bottombar

            //if drag down -> close
            val needClose = yvel>0

            //if view position not top bound or bottom bound smooth scroll to bound
            val startSettling = dragHelper.settleCapturedViewAt(0, if(needClose) bottomBound else topBound)
            if(startSettling){
                //if need scroll then after animation frame call Settle runnable
                ViewCompat.postOnAnimation(view, SettleRunnable(view, { view.isClose = needClose}))
            } else{
                //else set isClose
                view.isClose = needClose
            }
        }

    }

    private inner class SettleRunnable(private val view:View, private val animationEnd:()->Unit): Runnable{
        override fun run() {
            //if animation continue (not end )
            if(dragHelper.continueSettling(true)){
                //repeat after animation frame
                ViewCompat.postOnAnimation(view, this)
            }else{
                //else invoke animation end handler
                animationEnd.invoke()
            }
        }
    }

}