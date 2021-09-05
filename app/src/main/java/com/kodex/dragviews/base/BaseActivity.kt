package com.kodex.dragviews.base


import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.net.ConnectivityManager
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.kodex.dragviews.customeview.tooltip.SimpleTooltip
import com.kodex.dragviews.R

abstract class BaseActivity : AppCompatActivity(){


    override fun onStart() {
        super.onStart()

    }


    override fun onStop() {
        super.onStop()
    }


    public fun haveNetworkConnection(context: Context): Boolean {
        var haveConnectedWifi = false
        var haveConnectedMobile = false

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.allNetworkInfo
        for (ni in netInfo) {
            if (ni.typeName.equals("WIFI", ignoreCase = true))
                if (ni.isConnected)
                    haveConnectedWifi = true
            if (ni.typeName.equals("MOBILE", ignoreCase = true))
                if (ni.isConnected)
                    haveConnectedMobile = true
        }
        return haveConnectedWifi || haveConnectedMobile
    }

    fun animateItemShake(mContext: Context, viewToAnimate: View) {
        val animation = AnimationUtils.loadAnimation(mContext, R.anim.shake)
        val random = (Math.random() / 50f).toInt()
        animation.startOffset = random.toLong()
        animation.duration = (150 + random).toLong()
        viewToAnimate.startAnimation(animation)
        Handler().postDelayed({
            viewToAnimate.clearAnimation()
        }, 500)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    fun showToolTip(
        v: View,
        gravity: Int,
        layout: Int,
        text: String,
        showArrow: Boolean = false,
        padding: Int = R.dimen.dp_minus_50
    ): SimpleTooltip {
        val tooltip = SimpleTooltip.Builder(v.context)
            .anchorView(v)
            .focusable(true)
            .showArrow(showArrow)
            .text(text)
            .dismissOnOutsideTouch(true)
            .dismissOnInsideTouch(true)
            .modal(true)
            .gravity(gravity)
            .animated(false)
            .contentView(layout)
            .padding(padding)
            .focusable(true)
            .overlayMatchParent(true)
            .build()
        tooltip.show()
        return tooltip
    }

    fun getDeviceWidth():Int{
        val display = (this as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.x
    }
    fun getDeviceHeight():Int{
        val display = (this as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.y
    }


    fun callNoAnimationRecyclerView(): RecyclerView.ItemAnimator? {
        return object : RecyclerView.ItemAnimator() {
            override fun isRunning(): Boolean {
                return true
            }

            override fun animatePersistence(viewHolder: RecyclerView.ViewHolder, preLayoutInfo: ItemHolderInfo, postLayoutInfo: ItemHolderInfo): Boolean {
                return true
            }

            override fun runPendingAnimations() {

            }

            override fun endAnimation(item: RecyclerView.ViewHolder) {

            }

            override fun animateDisappearance(viewHolder: RecyclerView.ViewHolder, preLayoutInfo: ItemHolderInfo, postLayoutInfo: ItemHolderInfo?): Boolean {
                return true
            }

            override fun animateChange(oldHolder: RecyclerView.ViewHolder, newHolder: RecyclerView.ViewHolder, preLayoutInfo: ItemHolderInfo, postLayoutInfo: ItemHolderInfo): Boolean {
                return false
            }

            override fun animateAppearance(viewHolder: RecyclerView.ViewHolder, preLayoutInfo: ItemHolderInfo?, postLayoutInfo: ItemHolderInfo): Boolean {
                return true
            }

            override fun endAnimations() {

            }

        }
    }


}

fun View.viewVisible() {
    this.visibility = View.VISIBLE
}

fun View.viewGone() {
    this.visibility = View.GONE
}

fun View.viewInVisible() {
    this.visibility = View.INVISIBLE
}

fun View.viewVisibility(): Int {
    return this.visibility
}

fun View.isVisible(): Boolean {
    return this.visibility == View.VISIBLE
}

fun View.isGone(): Boolean {
    return this.visibility == View.GONE
}

fun View.isInVisible(): Boolean {
    return this.visibility == View.INVISIBLE
}

interface DoubleClickListener {

    fun onSingleClick(view: View)

    /**
     * Called when the user make a double click.
     */
    fun onDoubleClick(view: View)
}

class DoubleClick(private val doubleClickListener: DoubleClickListener, private var DOUBLE_CLICK_INTERVAL: Long)
    : View.OnClickListener {

    private val mHandler = Handler()
    private var clicks: Int = 0
    private var isBusy = false

    constructor(doubleClickListener: DoubleClickListener) : this(doubleClickListener, 200L) {
        DOUBLE_CLICK_INTERVAL = 200L // default time to wait the second click.
    }

    override fun onClick(view: View) {

        if (!isBusy) {
            isBusy = true
            clicks++
            mHandler.postDelayed({
                if (clicks >= 2) {  // Double tap.
                    doubleClickListener.onDoubleClick(view)
                }
                if (clicks == 1) {  // Single tap
                    doubleClickListener.onSingleClick(view)
                }
                clicks = 0
            }, DOUBLE_CLICK_INTERVAL)
            isBusy = false
        }

    }
}