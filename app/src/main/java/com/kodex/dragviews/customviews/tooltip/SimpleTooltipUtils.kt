package com.kodex.dragviews.customeview.tooltip

import android.content.Context
import android.content.res.Resources
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes

/**
 * SimpleTooltipUtils
 * Created by douglas on 09/05/16.
 */
object SimpleTooltipUtils {

    fun calculateRectOnScreen(view: View): RectF {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return RectF(
            location[0].toFloat(),
            location[1].toFloat(),
            (location[0] + view.measuredWidth).toFloat(),
            (location[1] + view.measuredHeight).toFloat()
        )
    }

    fun calculateRectInWindow(view: View): RectF {
        val location = IntArray(2)
        view.getLocationInWindow(location)
        return RectF(
            location[0].toFloat(),
            location[1].toFloat(),
            (location[0] + view.measuredWidth).toFloat(),
            (location[1] + view.measuredHeight).toFloat()
        )
    }

//    fun dpFromPx(px: Float): Float {
//        return px / Resources.getSystem().displayMetrics.density
//    }

    fun pxFromDp(dp: Float): Float {
        return dp * Resources.getSystem().displayMetrics.density
    }

    fun setWidth(view: View, width: Float) {
        var params: ViewGroup.LayoutParams? = view.layoutParams
        if (params == null) {
            params = ViewGroup.LayoutParams(width.toInt(), view.height)
        } else {
            params.width = width.toInt()
        }
        view.layoutParams = params
    }

    fun tooltipGravityToArrowDirection(tooltipGravity: Int): Int {
        return when (tooltipGravity) {
            Gravity.START -> ArrowDrawable.RIGHT
            Gravity.END -> ArrowDrawable.LEFT
            Gravity.TOP -> ArrowDrawable.BOTTOM
            Gravity.BOTTOM -> ArrowDrawable.TOP
            Gravity.CENTER -> ArrowDrawable.TOP
            else -> throw IllegalArgumentException("Gravity must have be CENTER, START, END, TOP or BOTTOM.")
        }
    }

    fun setX(view: View, x: Int) {
        view.x = x.toFloat()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//
//        } else {
//            val marginParams = getOrCreateMarginLayoutParams(view)
//            marginParams.leftMargin = x - view.left
//            view.layoutParams = marginParams
//        }
    }

    fun setY(view: View, y: Int) {
        view.y = y.toFloat()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            view.y = y.toFloat()
//        } else {
//            val marginParams = getOrCreateMarginLayoutParams(view)
//            marginParams.topMargin = y - view.top
//            view.layoutParams = marginParams
//        }
    }

    private fun getOrCreateMarginLayoutParams(view: View): ViewGroup.MarginLayoutParams {
        val lp = view.layoutParams
        return if (lp != null) {
            lp as? ViewGroup.MarginLayoutParams ?: ViewGroup.MarginLayoutParams(lp)
        } else {
            ViewGroup.MarginLayoutParams(view.width, view.height)
        }
    }

    fun removeOnGlobalLayoutListener(view: View, listener: ViewTreeObserver.OnGlobalLayoutListener) {
        view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//
//        } else {
//
//            view.viewTreeObserver.removeGlobalOnLayoutListener(listener)
//        }
    }

    fun setTextAppearance(tv: AppCompatTextView, @StyleRes textAppearanceRes: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tv.setTextAppearance(textAppearanceRes)
        } else {
//            tv.setTextAppearance(tv.mContext, textAppearanceRes)
            tv.setTextAppearance(textAppearanceRes)
        }
    }

    fun getColor(context: Context, @ColorRes colorRes: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getColor(colorRes)
        } else {

            context.getColor(colorRes)
        }
    }

    fun getDrawable(context: Context, @DrawableRes drawableRes: Int): Drawable? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getDrawable(drawableRes)
        } else {

            context.getDrawable(drawableRes)
        }
    }

    /**
     * Verify if the first child of the rootView is a FrameLayout.
     * Used for cases where the Tooltip is created inside a Dialog or DialogFragment.
     *
     * @param anchorView
     * @return FrameLayout or anchorView.getRootView()
     */
    fun findFrameLayout(anchorView: View): ViewGroup {
        var rootView = anchorView.rootView as ViewGroup
        if (rootView.childCount == 1 && rootView.getChildAt(0) is FrameLayout) {
            rootView = rootView.getChildAt(0) as ViewGroup
        }
        return rootView
    }
}
