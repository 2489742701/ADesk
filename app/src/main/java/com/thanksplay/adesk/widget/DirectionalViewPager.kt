package com.thanksplay.adesk.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class DirectionalViewPagerContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    
    private var initialX = 0f
    private var initialY = 0f
    private var isHorizontalSwipe = false
    private var isVerticalSwipe = false
    
    private val touchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
    
    companion object {
        private const val MAX_SWIPE_ANGLE = 30.0
    }
    
    private var viewPager: androidx.viewpager2.widget.ViewPager2? = null
    
    override fun onFinishInflate() {
        super.onFinishInflate()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is androidx.viewpager2.widget.ViewPager2) {
                viewPager = child
                break
            }
        }
    }
    
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = ev.rawX
                initialY = ev.rawY
                isHorizontalSwipe = false
                isVerticalSwipe = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isHorizontalSwipe && !isVerticalSwipe) {
                    val dx = ev.rawX - initialX
                    val dy = ev.rawY - initialY
                    
                    val distance = sqrt(dx * dx + dy * dy)
                    
                    if (distance > touchSlop) {
                        val angle = calculateAngle(dx, dy)
                        
                        if (angle > MAX_SWIPE_ANGLE) {
                            isVerticalSwipe = true
                            return false
                        } else {
                            isHorizontalSwipe = true
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isHorizontalSwipe = false
                isVerticalSwipe = false
            }
        }
        
        return super.onInterceptTouchEvent(ev)
    }
    
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = ev.rawX
                initialY = ev.rawY
                isHorizontalSwipe = false
                isVerticalSwipe = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isHorizontalSwipe && !isVerticalSwipe) {
                    val dx = ev.rawX - initialX
                    val dy = ev.rawY - initialY
                    
                    val distance = sqrt(dx * dx + dy * dy)
                    
                    if (distance > touchSlop) {
                        val angle = calculateAngle(dx, dy)
                        
                        if (angle > MAX_SWIPE_ANGLE) {
                            isVerticalSwipe = true
                        } else {
                            isHorizontalSwipe = true
                        }
                    }
                }
                
                if (isVerticalSwipe) {
                    viewPager?.isUserInputEnabled = false
                } else {
                    viewPager?.isUserInputEnabled = true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isHorizontalSwipe = false
                isVerticalSwipe = false
                viewPager?.isUserInputEnabled = true
            }
        }
        
        return super.dispatchTouchEvent(ev)
    }
    
    private fun calculateAngle(dx: Float, dy: Float): Double {
        val absDx = abs(dx)
        val absDy = abs(dy)
        
        if (absDx < 1f && absDy < 1f) {
            return 0.0
        }
        
        val angleRadians = atan2(absDy.toDouble(), absDx.toDouble())
        return Math.toDegrees(angleRadians)
    }
}
