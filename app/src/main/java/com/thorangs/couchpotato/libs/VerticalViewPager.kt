package com.thorangs.couchpotato.libs

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.thorangs.couchpotato.ui.dashboard.GraphsPagerView
import com.thorangs.couchpotato.ui.records.RecordsView

class VerticalViewPager : ViewPager {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    init {
        setPageTransformer(true, VerticalPagerTransformer())
        overScrollMode = OVER_SCROLL_NEVER
    }

    private fun swapXY(ev: MotionEvent): MotionEvent {
        val width = width.toFloat()
        val height = height.toFloat()

        val newX = ev.y / height * width
        val newY = ev.x / width * height

        ev.setLocation(newX, newY)

        return ev
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val intercepted = super.onInterceptTouchEvent(swapXY(ev))
        swapXY(ev) // return touch coordinates to original reference frame for any child views
        return intercepted
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean = super.onTouchEvent(swapXY(ev))

    inner class VerticalPagerTransformer : PageTransformer {
        override fun transformPage(view: View, position: Float) = when {
            position < -1 -> // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.alpha = 0f
            position <= 1 -> { // [-1,1]
                view.alpha = 1f

                // Counteract the default slide transition
                view.translationX = view.width.times(-position)

                //set Y position to swipe in from top
                val yPosition = position * view.height
                view.translationY = yPosition

            }
            else -> // (1,+Infinity]
                // This page is way off-screen to the right.
                view.alpha = 0f
        }
    }

    class VerticalPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment = when (position) {
            0 -> GraphsPagerView()
            else -> RecordsView()
        }

        override fun getCount() = 2
    }
}