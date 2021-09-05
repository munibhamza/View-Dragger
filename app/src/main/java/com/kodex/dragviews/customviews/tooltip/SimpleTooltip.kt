@file:Suppress("unused", "UNCHECKED_CAST")

package com.kodex.dragviews.customeview.tooltip

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatTextView
import androidx.annotation.*
import com.kodex.dragviews.R


/**
 *
 * Um tooltip que pode ser utilizado para exibição de dicas.
 *
 * A tooltip that can be used to display tips on the screen.
 *
 * @author Created by douglas on 05/05/16.
 * @see PopupWindow
 */
@SuppressLint("SyntheticAccessor")
class SimpleTooltip private constructor(builder: Builder) : PopupWindow.OnDismissListener {

    var mContext: Context?
    private var mOnDismissListener: OnDismissListener? = null
    private var mOnShowListener: OnShowListener? = null
    private var mPopupWindow: PopupWindow? = null
    private var mGravity: Int
    private var mArrowDirection: Int = 0
    private var mDismissOnInsideTouch: Boolean = false
    private var mDismissOnOutsideTouch: Boolean = false
    private var mModal: Boolean = false
    lateinit var mContentView: View
    private var mContentLayout: View? = null
    @IdRes
    var mTextViewId: Int
    private var mText: CharSequence
    var mAnchorView: View? = null
    private var mTransparentOverlay: Boolean
    private var mOverlayOffset: Float
    private var mOverlayMatchParent: Boolean
    var mMaxWidth: Float = 100F
    private var mOverlay: View? = null
    private var mRootView: ViewGroup? = null
    private var mShowArrow: Boolean = false
    private var mArrowView: AppCompatImageView? = null
    private var mArrowDrawable: Drawable?
    var mAnimated: Boolean = false
    private var mAnimator: AnimatorSet? = null
    private var mMargin: Float = 10F
    private var mPadding: Float = 10F
    private var mAnimationPadding: Float = 10F
    private var mAnimationDuration: Long = 10
    private var mArrowWidth: Float = 10F
    private var mArrowHeight: Float = 10F
    private var mFocusable: Boolean = false
    private var dismissed = false
    private var mHighlightShape = OverlayView.HIGHLIGHT_SHAPE_OVAL

    /**
     * <div class="pt">Indica se o tooltip está sendo exibido na tela.</div>
     * <div class=en">Indicate whether this tooltip is showing on screen.</div>
     *
     * @return <div class="pt"><tt>true</tt> se o tooltip estiver sendo exibido, <tt>false</tt> caso contrário</div>
     * <div class="en"><tt>true</tt> if the popup is showing, <tt>false</tt> otherwise</div>
     */
    val isShowing: Boolean
        get() = mPopupWindow != null && mPopupWindow!!.isShowing

    private var mPopupWindowTouchListener = View.OnTouchListener { v, event ->
        if (event.x > 0 && event.x < v.width &&
                event.y > 0 && event.y < v.height
        ) {
            if (mDismissOnInsideTouch) {
                dismiss()
                return@OnTouchListener mModal
            }
            return@OnTouchListener false
        }
        if (event.action == MotionEvent.ACTION_UP) {
            v.performClick()
        }
        mModal
    }

    private var mOverlayTouchListener = View.OnTouchListener { v, event ->
        if (mDismissOnOutsideTouch) {
            dismiss()
        }
        if (event.action == MotionEvent.ACTION_UP) {
            v.performClick()
        }
        mModal
    }

    private var mLocationLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {

        override fun onGlobalLayout() {
            val popup = mPopupWindow
            if (popup == null || dismissed) return

            if (mMaxWidth > 0 && mContentView.width > mMaxWidth) {
                SimpleTooltipUtils.setWidth(mContentView, mMaxWidth)
                popup.update(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                return
            }

            SimpleTooltipUtils.removeOnGlobalLayoutListener(popup.contentView, this)
            popup.contentView.viewTreeObserver.addOnGlobalLayoutListener(mArrowLayoutListener)
            val location = calculePopupLocation()
            popup.isClippingEnabled = true
            popup.update(location.x.toInt(), location.y.toInt(), popup.width, popup.height)
            popup.contentView.requestLayout()
            createOverlay()
        }
    }

    var mArrowLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val popup = mPopupWindow
            if (popup == null || dismissed) return

            SimpleTooltipUtils.removeOnGlobalLayoutListener(popup.contentView, this)

            popup.contentView.viewTreeObserver.addOnGlobalLayoutListener(mAnimationLayoutListener)
            popup.contentView.viewTreeObserver.addOnGlobalLayoutListener(mShowLayoutListener)
            if (mShowArrow) {
                val achorRect = SimpleTooltipUtils.calculateRectOnScreen(mAnchorView ?: return)
                val contentViewRect = SimpleTooltipUtils.calculateRectOnScreen(mContentLayout
                        ?: return)
                var x: Float = mArrowView?.left?.toFloat() ?: 0.0F
                var y: Float
                y = (mContentLayout?.paddingTop ?: 0) + SimpleTooltipUtils.pxFromDp(2f)
                val centerY = contentViewRect.height() / 2f - (mArrowView?.height ?: 0) / 2f
                val newY = centerY - (contentViewRect.centerY() - achorRect.centerY())
                if (newY > y) {
                    y = if (newY + (mArrowView?.height?.toFloat()
                                    ?: 0.0F) + y > contentViewRect.height()) {
                        contentViewRect.height() - (mArrowView?.height?.toFloat()
                                ?: 0.0F) - y
                    } else {
                        newY
                    }
                }
                x += +1
                SimpleTooltipUtils.setX(mArrowView ?: return, x.toInt())
                SimpleTooltipUtils.setY(mArrowView ?: return, y.toInt())
            }
            popup.contentView.requestLayout()
        }
    }

    var mShowLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val popup = mPopupWindow
            if (popup == null || dismissed) return

            SimpleTooltipUtils.removeOnGlobalLayoutListener(popup.contentView, this)

            if (mOnShowListener != null)
                mOnShowListener?.onShow(this@SimpleTooltip)
            mOnShowListener = null

            mContentLayout?.visibility = View.VISIBLE
        }
    }

    var mAnimationLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val popup = mPopupWindow
            if (popup == null || dismissed) return

            SimpleTooltipUtils.removeOnGlobalLayoutListener(popup.contentView, this)

            if (mAnimated) startAnimation()

            popup.contentView.requestLayout()
        }
    }

    /**
     * <div class="pt">Listener utilizado para chamar o <tt>SimpleTooltip#dismiss()</tt> quando a <tt>View</tt> root é encerrada sem que a tooltip seja fechada.
     * Pode ocorrer quando a tooltip é utilizada dentro de Dialogs.</div>
     */
    private var mAutoDismissLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val popup = mPopupWindow
        if (popup == null || dismissed) return@OnGlobalLayoutListener

        if (mRootView?.isShown != true) dismiss()
    }


    init {
        mContext = builder.context
        mGravity = builder.gravity
        mArrowDirection = builder.arrowDirection
        mDismissOnInsideTouch = builder.dismissOnInsideTouch
        mDismissOnOutsideTouch = builder.dismissOnOutsideTouch
        mModal = builder.modal
        mContentView = builder.contentView!!
        mTextViewId = builder.textViewId
        mText = builder.text
        mAnchorView = builder.anchorView
        mTransparentOverlay = builder.transparentOverlay
        mOverlayOffset = builder.overlayOffset
        mOverlayMatchParent = builder.overlayMatchParent
        mMaxWidth = builder.maxWidth
        mShowArrow = builder.showArrow
        mArrowWidth = builder.arrowWidth
        mArrowHeight = builder.arrowHeight
        mArrowDrawable = builder.arrowDrawable
        mAnimated = builder.animated
        mMargin = builder.margin
        mPadding = builder.padding
        mAnimationPadding = builder.animationPadding
        mAnimationDuration = builder.animationDuration
        mOnDismissListener = builder.onDismissListener
        mOnShowListener = builder.onShowListener
        mFocusable = builder.focusable
        mRootView = SimpleTooltipUtils.findFrameLayout(mAnchorView!!)
        mHighlightShape = builder.highlightShape

        init()
    }

    private fun init() {
        configPopupWindow()
        configContentView()
    }

    private fun configPopupWindow() {
        mPopupWindow = PopupWindow(mContext, null, mDefaultPopupWindowStyleRes)
        mPopupWindow?.setOnDismissListener(this)
        mPopupWindow?.width = ViewGroup.LayoutParams.WRAP_CONTENT
        mPopupWindow?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        mPopupWindow?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mPopupWindow?.isClippingEnabled = false
        mPopupWindow?.isFocusable = mFocusable
    }


    fun show() {
        verifyDismissed()

        mContentLayout?.viewTreeObserver?.addOnGlobalLayoutListener(mLocationLayoutListener)
        mContentLayout?.viewTreeObserver?.addOnGlobalLayoutListener(mAutoDismissLayoutListener)

        mRootView?.post {
            if (mRootView?.isShown == true)
                mPopupWindow?.showAtLocation(mRootView, Gravity.NO_GRAVITY, mRootView?.width!!, mRootView?.height
                !!)
            else
                Log.e(TAG, "Tooltip cannot be shown, root view is invalid or has been closed.")
        }
    }

    private fun verifyDismissed() {
        require(!dismissed) { "Tooltip has ben dismissed." }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createOverlay() {
        mOverlay = if (mTransparentOverlay) View(mContext) else OverlayView(
                mContext ?: return,
                mAnchorView,
                mHighlightShape,
                mOverlayOffset
        )
        if (mOverlayMatchParent)
            mOverlay?.layoutParams =
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        else
            mOverlay?.layoutParams = ViewGroup.LayoutParams(mRootView?.width!!, mRootView?.height!!)
        mOverlay?.setOnTouchListener(mOverlayTouchListener)
        mRootView?.addView(mOverlay)
    }

    private fun calculePopupLocation(): PointF {
        val location = PointF()

        val anchorRect = SimpleTooltipUtils.calculateRectInWindow(mAnchorView!!)
        val anchorCenter = PointF(anchorRect.centerX(), anchorRect.centerY())

        when (mGravity) {
            Gravity.START -> {
                location.x = anchorRect.left - mPopupWindow!!.contentView.width.toFloat() - mMargin
                location.y = anchorCenter.y - mPopupWindow!!.contentView.height / 2f
            }
            Gravity.END -> {
                location.x = anchorRect.right + mMargin
                location.y = anchorCenter.y - mPopupWindow!!.contentView.height / 2f
            }
            Gravity.TOP -> {
                location.x = anchorCenter.x - mPopupWindow!!.contentView.width / 2f
                location.y = anchorRect.top - mPopupWindow!!.contentView.height.toFloat() - mMargin
            }
            Gravity.BOTTOM -> {
                location.x = anchorCenter.x - mPopupWindow!!.contentView.width / 3f + 90
                location.y = anchorRect.bottom + mMargin - 45
            }
            Gravity.CENTER -> {
                location.x = anchorCenter.x - mPopupWindow!!.contentView.width / 2f
                location.y = anchorCenter.y - mPopupWindow!!.contentView.height / 2f
            }
            else -> throw IllegalArgumentException("Gravity must have be CENTER, START, END, TOP or BOTTOM.")
        }

        return location
    }

    private fun configContentView() {
        if (mContentView is AppCompatTextView) {
            val tv = mContentView as AppCompatTextView?
            tv?.text = mText
        }

        mContentView.setPadding(mPadding.toInt(), mPadding.toInt(), mPadding.toInt(), mPadding.toInt())

        val linearLayout = LinearLayout(mContext)
        linearLayout.layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        linearLayout.orientation =
                if (mArrowDirection == ArrowDrawable.LEFT || mArrowDirection == ArrowDrawable.RIGHT) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
        val layoutPadding = (if (mAnimated) mAnimationPadding.toInt() else 0)
        linearLayout.setPadding(layoutPadding, layoutPadding, layoutPadding, layoutPadding)

        if (mShowArrow) {
            mArrowView = AppCompatImageView(mContext!!)
            mArrowView?.setImageDrawable(mArrowDrawable)
            val arrowLayoutParams: LinearLayout.LayoutParams = if (mArrowDirection == ArrowDrawable.TOP || mArrowDirection == ArrowDrawable.BOTTOM) {
                LinearLayout.LayoutParams(mArrowWidth.toInt(), mArrowHeight.toInt(), 0f)
            } else {
                LinearLayout.LayoutParams(mArrowHeight.toInt(), mArrowWidth.toInt(), 0f)
            }

            arrowLayoutParams.gravity = Gravity.CENTER
            mArrowView?.layoutParams = arrowLayoutParams

            if (mArrowDirection == ArrowDrawable.BOTTOM || mArrowDirection == ArrowDrawable.RIGHT) {
                linearLayout.addView(mContentView)
                linearLayout.addView(mArrowView)
            } else {
                linearLayout.addView(mArrowView)
                linearLayout.addView(mContentView)
            }
        } else {
            linearLayout.addView(mContentView)
        }

        val contentViewParams =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0f)
        contentViewParams.gravity = Gravity.CENTER
        mContentView.layoutParams = contentViewParams

        if (mDismissOnInsideTouch || mDismissOnOutsideTouch)
            mContentView.setOnTouchListener(mPopupWindowTouchListener)

        mContentLayout = linearLayout
        mContentLayout?.visibility = View.INVISIBLE
        mPopupWindow?.contentView = mContentLayout
    }

    fun dismiss() {
        if (dismissed)
            return

        dismissed = true
        if (mPopupWindow != null) {
            mPopupWindow?.dismiss()
        }
    }

    fun <T : View> findViewById(id: Int): T {

        return mContentLayout!!.findViewById<View>(id) as T
    }

    override fun onDismiss() {
        dismissed = true

        if (mAnimator != null) {
            mAnimator?.removeAllListeners()
            mAnimator?.end()
            mAnimator?.cancel()
            mAnimator = null
        }

        if (mRootView != null && mOverlay != null) {
            mRootView?.removeView(mOverlay)
        }
        mRootView = null
        mOverlay = null

        if (mOnDismissListener != null)
            mOnDismissListener?.onDismiss(this)
        mOnDismissListener = null

        SimpleTooltipUtils.removeOnGlobalLayoutListener(mPopupWindow?.contentView
                ?: View(mContext), mLocationLayoutListener)
        SimpleTooltipUtils.removeOnGlobalLayoutListener(mPopupWindow?.contentView
                ?: View(mContext), mArrowLayoutListener)
        SimpleTooltipUtils.removeOnGlobalLayoutListener(mPopupWindow?.contentView
                ?: View(mContext), mShowLayoutListener)
        SimpleTooltipUtils.removeOnGlobalLayoutListener(mPopupWindow?.contentView
                ?: View(mContext), mAnimationLayoutListener)
        SimpleTooltipUtils.removeOnGlobalLayoutListener(mPopupWindow?.contentView
                ?: View(mContext), mAutoDismissLayoutListener)

        mPopupWindow = null
    }

    fun startAnimation() {
        val property = if (mGravity == Gravity.TOP || mGravity == Gravity.BOTTOM) "translationY" else "translationX"

        val anim1 = ObjectAnimator.ofFloat(mContentLayout, property, -mAnimationPadding, mAnimationPadding)
        anim1.duration = mAnimationDuration
        anim1.interpolator = AccelerateDecelerateInterpolator()

        val anim2 = ObjectAnimator.ofFloat(mContentLayout, property, mAnimationPadding, -mAnimationPadding)
        anim2.duration = mAnimationDuration
        anim2.interpolator = AccelerateDecelerateInterpolator()
        mAnimator = AnimatorSet()
        mAnimator?.playSequentially(anim1, anim2)
        mAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (!dismissed && isShowing) {
                    animation.start()
                }
            }
        })
        mAnimator?.start()
    }

    interface OnDismissListener {
        fun onDismiss(tooltip: SimpleTooltip)
    }

    interface OnShowListener {
        fun onShow(tooltip: SimpleTooltip)
    }

    /**
     * <div class="pt">Classe responsável por facilitar a criação do objeto <tt>SimpleTooltip</tt>.</div>
     * <div class="en">Class responsible for making it easier to build the object <tt>SimpleTooltip</tt>.</div>
     *
     * @author Created by douglas on 05/05/16.
     */
    class Builder(val context: Context) {
        var dismissOnInsideTouch = true
        var dismissOnOutsideTouch = true
        var modal = false
        var contentView: View? = null
        @IdRes
        var textViewId = android.R.id.text1
        var text: CharSequence = ""
        var anchorView: View? = null
        var arrowDirection = ArrowDrawable.AUTO
        var gravity = Gravity.BOTTOM
        var transparentOverlay = true
        var overlayOffset = -1f
        var overlayMatchParent = true
        var maxWidth: Float = 0.toFloat()
        var showArrow = true
        var arrowDrawable: Drawable? = null
        var animated = false
        var margin = -1f
        var padding = -1f
        var animationPadding = -1f
        var onDismissListener: OnDismissListener? = null
        var onShowListener: OnShowListener? = null
        var animationDuration: Long = 0
        private var backgroundColor: Int = 0
        var textColor: Int = 0
        private var arrowColor: Int = 0
        var arrowHeight: Float = 0.toFloat()
        var arrowWidth: Float = 0.toFloat()
        var focusable: Boolean = false
        var highlightShape = OverlayView.HIGHLIGHT_SHAPE_OVAL

        @Throws(IllegalArgumentException::class)
        fun build(): SimpleTooltip {
            validateArguments()
            if (backgroundColor == 0) {
                backgroundColor = SimpleTooltipUtils.getColor(context, mDefaultBackgroundColorRes)
            }
            if (textColor == 0) {
                textColor = SimpleTooltipUtils.getColor(context, mDefaultTextColorRes)
            }
            if (contentView == null) {
                val tv = AppCompatTextView(context)
                SimpleTooltipUtils.setTextAppearance(tv, mDefaultTextAppearanceRes)
                tv.setBackgroundColor(backgroundColor)
                tv.setTextColor(textColor)
                contentView = tv
            }
            if (arrowColor == 0) {
                arrowColor = SimpleTooltipUtils.getColor(context, mDefaultArrowColorRes)
            }
            if (margin < 0) {
                margin = context.resources.getDimension(mDefaultMarginRes)
            }
            if (padding < 0) {
                padding = context.resources.getDimension(mDefaultPaddingRes)
            }
            if (animationPadding < 0) {
                animationPadding = context.resources.getDimension(mDefaultAnimationPaddingRes)
            }
            if (animationDuration == 0L) {
                animationDuration = context.resources.getInteger(mDefaultAnimationDurationRes).toLong()
            }
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
//
//            }
            animated = false
            if (showArrow) {
                if (arrowDirection == ArrowDrawable.AUTO)
                    arrowDirection = SimpleTooltipUtils.tooltipGravityToArrowDirection(gravity)
                if (arrowDrawable == null)
                    arrowDrawable = ArrowDrawable(arrowColor, arrowDirection)
                if (arrowWidth == 0f)
                    arrowWidth = context.resources.getDimension(mDefaultArrowWidthRes)
                if (arrowHeight == 0f)
                    arrowHeight = context.resources.getDimension(mDefaultArrowHeightRes)
            }
            if (highlightShape < 0 || highlightShape > OverlayView.HIGHLIGHT_SHAPE_RECTANGULAR) {
                highlightShape = OverlayView.HIGHLIGHT_SHAPE_OVAL
            }
            if (overlayOffset < 0) {
                overlayOffset = context.resources.getDimension(mDefaultOverlayOffsetRes)
            }
            return SimpleTooltip(this)
        }

        @Throws(IllegalArgumentException::class)
        private fun validateArguments() {
            requireNotNull(anchorView) { "Anchor view not specified." }
        }

        /**
         * <div class="pt">Define um novo conteúdo customizado para o tooltip.</div>
         *
         * @param textView <div class="pt">novo conteúdo para o tooltip.</div>
         * @return this
         * @see Builder.contentView
         * @see Builder.contentView
         * @see Builder.contentView
         */
        private fun contentView(textView: AppCompatTextView): Builder {
            this.contentView = textView
            this.textViewId = 0
            return this
        }

        /**
         * <div class="pt">Define um novo conteúdo customizado para o tooltip.</div>
         *
         * @param contentView <div class="pt">novo conteúdo para o tooltip, pode ser um <tt>[ViewGroup]</tt> ou qualquer componente customizado.</div>
         * @param textViewId  <div class="pt">resId para o <tt>[AppCompatTextView]</tt> existente dentro do <tt>[Builder.contentView]</tt>. Padrão é <tt>android.R.id.text1</tt>.</div>
         * @return this
         * @see Builder.contentView
         * @see Builder.contentView
         * @see Builder.contentView
         */
        fun contentView(contentView: View, @IdRes textViewId: Int): Builder {
            this.contentView = contentView
            this.textViewId = textViewId
            return this
        }

        /**
         * <div class="pt">Define um novo conteúdo customizado para o tooltip.</div>
         *
         * @param contentViewId <div class="pt">layoutId que será inflado como o novo conteúdo para o tooltip.</div>
         * @param textViewId    <div class="pt">resId para o <tt>[AppCompatTextView]</tt> existente dentro do <tt>[Builder.contentView]</tt>. Padrão é <tt>android.R.id.text1</tt>.</div>
         * @return this
         * @see Builder.contentView
         * @see Builder.contentView
         * @see Builder.contentView
         */
        private fun contentView(@LayoutRes contentViewId: Int, @IdRes textViewId: Int): Builder {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            this.contentView = inflater.inflate(contentViewId, null, false)
            this.textViewId = textViewId
            return this
        }

        /**
         * <div class="pt">Define um novo conteúdo customizado para o tooltip.</div>
         *
         * @param contentViewId <div class="pt">layoutId que será inflado como o novo conteúdo para o tooltip.</div>
         * @return this
         * @see Builder.contentView
         * @see Builder.contentView
         * @see Builder.contentView
         */
        fun contentView(@LayoutRes contentViewId: Int): Builder {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            this.contentView = inflater.inflate(contentViewId, null, false)
            this.textViewId = 0
            return this
        }

        /**
         * <div class="pt">Define se o tooltip será fechado quando receber um clique dentro de sua área. Padrão é <tt>true</tt>.</div>
         *
         * @param dismissOnInsideTouch <div class="pt"><tt>true</tt> para fechar quando receber o click dentro, <tt>false</tt> caso contrário.</div>
         * @return this
         * @see Builder.dismissOnOutsideTouch
         */
        fun dismissOnInsideTouch(dismissOnInsideTouch: Boolean): Builder {
            this.dismissOnInsideTouch = dismissOnInsideTouch
            return this
        }

        /**
         * <div class="pt">Define se o tooltip será fechado quando receber um clique fora de sua área. Padrão é <tt>true</tt>.</div>
         *
         * @param dismissOnOutsideTouch <div class="pt"><tt>true</tt> para fechar quando receber o click fora, <tt>false</tt> caso contrário.</div>
         * @return this
         * @see Builder.dismissOnInsideTouch
         */
        fun dismissOnOutsideTouch(dismissOnOutsideTouch: Boolean): Builder {
            this.dismissOnOutsideTouch = dismissOnOutsideTouch
            return this
        }

        /**
         * <div class="pt">Define se a tela fiacrá bloqueada enquanto o tooltip estiver aberto.
         * Esse parâmetro deve ser combinado com <tt>[Builder.dismissOnInsideTouch]</tt> e <tt>[Builder.dismissOnOutsideTouch]</tt>.
         * Padrão é <tt>false</tt>.</div>
         *
         * @param modal <div class="pt"><tt>true</tt> para bloquear a tela, <tt>false</tt> caso contrário.</div>
         * @return this
         * @see Builder.dismissOnInsideTouch
         * @see Builder.dismissOnOutsideTouch
         */
        fun modal(modal: Boolean): Builder {
            this.modal = modal
            return this
        }

        /**
         * <div class="pt">Define o texto que sera exibido no <tt>[AppCompatTextView]</tt> dentro do tooltip.</div>
         *
         * @param text <div class="pt">texto que sera exibido.</div>
         * @return this
         */
        fun text(text: CharSequence): Builder {
            this.text = text
            return this
        }

        /**
         * <div class="pt">Define o texto que sera exibido no <tt>[AppCompatTextView]</tt> dentro do tooltip.</div>
         *
         * @param textRes <div class="pt">id do resource da String.</div>
         * @return this
         */
        fun text(@StringRes textRes: Int): Builder {
            this.text = context.getString(textRes)
            return this
        }

        /**
         * <div class="pt">Define para qual <tt>[View]</tt> o tooltip deve apontar. Importante ter certeza que esta <tt>[View]</tt> já esteja pronta e exibida na tela.</div>
         * <div class="en">Set the target <tt>[View]</tt> that the tooltip will point. Make sure that the anchor <tt>[View]</tt> shold be showing in the screen.</div>
         *
         * @param anchorView <div class="pt"><tt>View</tt> para qual o tooltip deve apontar</div>
         * <div class="en"><tt>View</tt> that the tooltip will point</div>
         * @return this
         */
        fun anchorView(anchorView: View): Builder {
            this.anchorView = anchorView
            return this
        }

        /**
         * <div class="pt">Define a para qual lado o tooltip será posicionado em relação ao <tt>anchorView</tt>.
         * As opções existentes são <tt>[Gravity.START]</tt>, <tt>[Gravity.END]</tt>, <tt>[Gravity.TOP]</tt> e <tt>[Gravity.BOTTOM]</tt>.
         * O padrão é <tt>[Gravity.BOTTOM]</tt>.</div>
         *
         * @param gravity <div class="pt">lado para qual o tooltip será posicionado.</div>
         * @return this
         */
        fun gravity(gravity: Int): Builder {
            this.gravity = gravity
            return this
        }

        /**
         * <div class="pt">Define a direção em que a seta será criada.
         * As opções existentes são <tt>[ArrowDrawable.LEFT]</tt>, <tt>[ArrowDrawable.TOP]</tt>, <tt>[ArrowDrawable.RIGHT]</tt>,
         * <tt>[ArrowDrawable.BOTTOM]</tt> e <tt>[ArrowDrawable.AUTO]</tt>.
         * O padrão é <tt>[ArrowDrawable.AUTO]</tt>. <br></br>
         * Esta opção deve ser utilizada em conjunto com  <tt>Builder#showArrow(true)</tt>.</div>
         *
         * @param arrowDirection <div class="pt">direção em que a seta será criada.</div>
         * @return this
         */
        fun arrowDirection(arrowDirection: Int): Builder {
            this.arrowDirection = arrowDirection
            return this
        }

        /**
         * <div class="pt">Define se o fundo da tela será escurecido ou transparente enquanto o tooltip estiver aberto. Padrão é <tt>true</tt>.</div>
         *
         * @param transparentOverlay <div class="pt"><tt>true</tt> para o fundo transparente, <tt>false</tt> para escurecido.</div>
         * @return this
         */
        private fun transparentOverlay(transparentOverlay: Boolean): Builder {
            this.transparentOverlay = transparentOverlay
            return this
        }

        /**
         * <div class="pt">Define a largura máxima do Tooltip.</div>
         *
         * @param maxWidthRes <div class="pt">resId da largura máxima.</div>
         * @return <tt>this</tt>
         * @see Builder.maxWidth
         */
        private fun maxWidth(@DimenRes maxWidthRes: Int): Builder {
            this.maxWidth = context.resources.getDimension(maxWidthRes)
            return this
        }

        /**
         * <div class="pt">Define a largura máxima do Tooltip. Padrão é <tt>0</tt> (sem limite).</div>
         *
         * @param maxWidth <div class="pt">largura máxima em pixels.</div>
         * @return <tt>this</tt>
         * @see Builder.maxWidth
         */
        private fun maxWidth(maxWidth: Float): Builder {
            this.maxWidth = maxWidth
            return this
        }

        /**
         * <div class="pt">Define se o tooltip será animado enquanto estiver aberto. Disponível a partir do Android API 11. Padrão é <tt>false</tt>.</div>
         *
         * @param animated <div class="pt"><tt>true</tt> para tooltip animado, <tt>false</tt> caso contrário.</div>
         * @return this
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        fun animated(animated: Boolean): Builder {
            this.animated = animated
            return this
        }

        /**
         * <div class="pt">Define o tamanho do deslocamento durante a animação. Padrão é o valor de <tt>[R.dimen.simpletooltip_animation_padding]</tt>.</div>
         *
         * @param animationPadding <div class="pt">tamanho do deslocamento em pixels.</div>
         * @return <tt>this</tt>
         * @see Builder.animationPadding
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        fun animationPadding(animationPadding: Float): Builder {
            this.animationPadding = animationPadding
            return this
        }

        /**
         * <div class="pt">Define o tamanho do deslocamento durante a animação. Padrão é <tt>[R.dimen.simpletooltip_animation_padding]</tt>.</div>
         *
         * @param animationPaddingRes <div class="pt">resId do tamanho do deslocamento.</div>
         * @return <tt>this</tt>
         * @see Builder.animationPadding
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        fun animationPadding(@DimenRes animationPaddingRes: Int): Builder {
            this.animationPadding = context.resources.getDimension(animationPaddingRes)
            return this
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        fun animationDuration(animationDuration: Long): Builder {
            this.animationDuration = animationDuration
            return this
        }

        /**
         * <div class="pt">Define o padding entre a borda do Tooltip e seu conteúdo. Padrão é o valor <tt>[R.dimen.simpletooltip_padding]</tt>.</div>
         *
         * @param padding <div class="pt">tamanho do padding em pixels.</div>
         * @return <tt>this</tt>
         * @see Builder.padding
         */
        fun padding(padding: Float): Builder {
            this.padding = padding
            return this
        }

        /**
         * <div class="pt">Define o padding entre a borda do Tooltip e seu conteúdo. Padrão é <tt>[R.dimen.simpletooltip_padding]</tt>.</div>
         *
         * @param paddingRes <div class="pt">resId do tamanho do padding.</div>
         * @return <tt>this</tt>
         * @see Builder.padding
         */
        fun padding(@DimenRes paddingRes: Int): Builder {
            this.padding = context.resources.getDimension(paddingRes)
            return this
        }


        /**
         * <div class="pt">Define a margem entre o Tooltip e o <tt>anchorView</tt>. Padrão é o valor de <tt>[R.dimen.simpletooltip_margin]</tt>.</div>
         *
         * @param margin <div class="pt">tamanho da margem em pixels.</div>
         * @return <tt>this</tt>
         * @see Builder.margin
         */
        private fun margin(margin: Float): Builder {
            this.margin = margin
            return this
        }

        /**
         * <div class="pt">Define a margem entre o Tooltip e o <tt>anchorView</tt>. Padrão é <tt>[R.dimen.simpletooltip_margin]</tt>.</div>
         *
         * @param marginRes <div class="pt">resId do tamanho da margem.</div>
         * @return <tt>this</tt>
         * @see Builder.margin
         */
        private fun margin(@DimenRes marginRes: Int): Builder {
            this.margin = context.resources.getDimension(marginRes)
            return this
        }

        fun textColor(textColor: Int): Builder {
            this.textColor = textColor
            return this
        }

        fun backgroundColor(@ColorInt backgroundColor: Int): Builder {
            this.backgroundColor = backgroundColor
            return this
        }

        /**
         * <div class="pt">Indica se deve ser gerada a seta indicativa. Padrão é <tt>true</tt>.</div>
         * <div class="en">Indicates whether to be generated indicative arrow. Default is <tt>true</tt>.</div>
         *
         * @param showArrow <div class="pt"><tt>true</tt> para exibir a seta, <tt>false</tt> caso contrário.</div>
         * <div class="en"><tt>true</tt> to show arrow, <tt>false</tt> otherwise.</div>
         * @return this
         */
        fun showArrow(showArrow: Boolean): Builder {
            this.showArrow = showArrow
            return this
        }

        fun arrowDrawable(arrowDrawable: Drawable): Builder {
            this.arrowDrawable = arrowDrawable
            return this
        }

        fun arrowDrawable(@DrawableRes drawableRes: Int): Builder {
            this.arrowDrawable = SimpleTooltipUtils.getDrawable(context, drawableRes)
            return this
        }

        fun arrowColor(@ColorInt arrowColor: Int): Builder {
            this.arrowColor = arrowColor
            return this
        }

        /**
         * <div class="pt">Altura da seta indicativa. Esse valor é automaticamente definido em Largura ou Altura conforme a <tt>[Gravity]</tt> configurada.
         * Este valor sobrescreve <tt>[R.dimen.simpletooltip_arrow_height]</tt></div>
         * <div class="en">Height of the arrow. This value is automatically set in the Width or Height as the <tt>[Gravity]</tt>.</div>
         *
         * @param arrowHeight <div class="pt">Altura em pixels.</div>
         * <div class="en">Height in pixels.</div>
         * @return this
         * @see Builder.arrowWidth
         */
        fun arrowHeight(arrowHeight: Float): Builder {
            this.arrowHeight = arrowHeight
            return this
        }

        /**
         * <div class="pt">Largura da seta indicativa. Esse valor é automaticamente definido em Largura ou Altura conforme a <tt>[Gravity]</tt> configurada.
         * Este valor sobrescreve <tt>[R.dimen.simpletooltip_arrow_width]</tt></div>
         * <div class="en">Width of the arrow. This value is automatically set in the Width or Height as the <tt>[Gravity]</tt>.</div>
         *
         * @param arrowWidth <div class="pt">Largura em pixels.</div>
         * <div class="en">Width in pixels.</div>
         * @return this
         */
        private fun arrowWidth(arrowWidth: Float): Builder {
            this.arrowWidth = arrowWidth
            return this
        }

        fun onDismissListener(onDismissListener: OnDismissListener): Builder {
            this.onDismissListener = onDismissListener
            return this
        }

        fun onShowListener(onShowListener: OnShowListener): Builder {
            this.onShowListener = onShowListener
            return this
        }

        /**
         * <div class="pt">Habilita o foco no conteúdo da tooltip. Padrão é <tt>false</tt>.</div>
         * <div class="en">Enables focus in the tooltip content. Default is <tt>false</tt>.</div>
         *
         * @param focusable <div class="pt">Pode receber o foco.</div>
         * <div class="en">Can receive focus.</div>
         * @return this
         */
        fun focusable(focusable: Boolean): Builder {
            this.focusable = focusable
            return this
        }

        /**
         * <div class="pt">Configura o formato do Shape em destaque. <br></br>
         * <tt>[OverlayView.HIGHLIGHT_SHAPE_OVAL]</tt> - Destaque oval (padrão). <br></br>
         * <tt>[OverlayView.HIGHLIGHT_SHAPE_RECTANGULAR]</tt> - Destaque retangular. <br></br>
        </div> *
         *
         *
         * <div class="en">Configure the the Shape type. <br></br>
         * <tt>[OverlayView.HIGHLIGHT_SHAPE_OVAL]</tt> - Shape oval (default). <br></br>
         * <tt>[OverlayView.HIGHLIGHT_SHAPE_RECTANGULAR]</tt> - Shape rectangular. <br></br>
        </div> *
         *
         * @param highlightShape <div class="pt">Formato do Shape.</div>
         * <div class="en">Shape type.</div>
         * @return this
         * @see OverlayView.HIGHLIGHT_SHAPE_OVAL
         *
         * @see OverlayView.HIGHLIGHT_SHAPE_RECTANGULAR
         *
         * @see Builder.transparentOverlay
         */
        fun highlightShape(highlightShape: Int): Builder {
            this.highlightShape = highlightShape
            return this
        }

        /**
         * Este valor sobrescreve <tt>[R.dimen.simpletooltip_overlay_offset]</tt></div>
         * <div class="en">Margin between [Builder.anchorView] and highlight Shape border.
         * This value override <tt>[R.dimen.simpletooltip_overlay_offset]</tt></div>
         *
         * @return this
         * @see Builder.anchorView
         * @see Builder.transparentOverlay
         */
        fun overlayOffset(@Dimension overlayOffset: Float): Builder {
            this.overlayOffset = overlayOffset
            return this
        }

        /**
         * <div class="pt">Define o comportamento do overlay. Utilizado para casos onde a view de Overlay não pode ser MATCH_PARENT.
         * Como em uma Dialog ou DialogFragment.</div>
         * <div class="en">Sets the behavior of the overlay view. Used for cases where the Overlay view can not be MATCH_PARENT.
         * Like in a Dialog or DialogFragment.</div>
         *
         * @return this
         */
        fun overlayMatchParent(overlayMatchParent: Boolean): Builder {
            this.overlayMatchParent = overlayMatchParent
            return this
        }
    }

    companion object {

        var TAG = SimpleTooltip::class.java.simpleName

        // Default Resources
        var mDefaultPopupWindowStyleRes = android.R.attr.popupWindowStyle
        var mDefaultTextAppearanceRes = R.style.simpletooltip_default
        var mDefaultBackgroundColorRes = R.color.simpletooltip_background
        var mDefaultTextColorRes = R.color.simpletooltip_text
        var mDefaultArrowColorRes = R.color.simpletooltip_arrow
        var mDefaultMarginRes = R.dimen.simpletooltip_margin
        var mDefaultPaddingRes = R.dimen.simpletooltip_padding
        var mDefaultAnimationPaddingRes = R.dimen.simpletooltip_animation_padding
        var mDefaultAnimationDurationRes = R.integer.simpletooltip_animation_duration
        var mDefaultArrowWidthRes = R.dimen.simpletooltip_arrow_width
        var mDefaultArrowHeightRes = R.dimen.simpletooltip_arrow_height
        var mDefaultOverlayOffsetRes = R.dimen.simpletooltip_overlay_offset
    }
}
