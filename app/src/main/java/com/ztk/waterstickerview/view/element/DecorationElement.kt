package com.ztk.waterstickerview.view.element

import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import com.ztk.waterstickerview.view.ElementContainerView
import com.ztk.waterstickerview.utils.AppUtil
import com.ztk.waterstickerview.utils.LogUtil
import com.ztk.waterstickerview.view.DecorationView
import kotlin.math.atan2

/**
 * element 的装饰框基类
 * @author xiaoman
 */
abstract class DecorationElement  constructor(val mContext: Context,val stickerType : String) :
    BaseElement(mContext,stickerType) {
    private var mDecorationView: DecorationView? = null
    protected var mShowingViewParams = MarginLayoutParams(0, 0)

    var showEdit = false

    /**
     * 是否处于单指旋转缩放的状态
     */
    var isSingleFingerScaleAndRotate = false
        protected set

    companion object {
        private const val TAG = "DecorationElement"
        var ELEMENT_SCALE_ROTATE_ICON_WIDTH = 84 // 旋转按钮的宽度
        private var REDUNDANT_AREA_LEFT_RIGHT = 40 // 延伸区域的宽度
    }

    init {
        REDUNDANT_AREA_LEFT_RIGHT = AppUtil.dip2px(mContext, 12f)
        mRedundantAreaTopBottom = REDUNDANT_AREA_LEFT_RIGHT
        mRedundantAreaLeftRight = REDUNDANT_AREA_LEFT_RIGHT
    }

    override fun add(elementContainerView: ElementContainerView?, showEdit: Boolean) {
        this.showEdit = showEdit
        super.add(elementContainerView, showEdit)
        mElementShowingView?.post {
            if (mOriginWidth == 0f){
                mOriginWidth = (mElementShowingView?.measuredWidth ?:0).toFloat()
            }
            if (mOriginHeight == 0f){
                mOriginHeight = (mElementShowingView?.measuredHeight ?:0).toFloat()
            }

            mShowingViewParams =
                MarginLayoutParams(mOriginWidth.toInt(), mOriginHeight.toInt())
            mShowingViewParams.leftMargin = REDUNDANT_AREA_LEFT_RIGHT
            mShowingViewParams.topMargin = REDUNDANT_AREA_LEFT_RIGHT
            mShowingViewParams.rightMargin = REDUNDANT_AREA_LEFT_RIGHT
            mShowingViewParams.bottomMargin = REDUNDANT_AREA_LEFT_RIGHT
            mDecorationView = initDecorationView(showEdit)
            mElementContainerView?.addView(mDecorationView)
        }
    }

    /**
     * 初始化边框装饰 view，子类可以实现自己的样式
     */
    protected fun initDecorationView(showEdit: Boolean): DecorationView {
        val decorationView = DecorationView(mContext)
        decorationView.setDecorationElement(this, showEdit)
        val decorationViewLayoutParams = FrameLayout.LayoutParams(0, 0)
        decorationView.layoutParams = decorationViewLayoutParams
        return decorationView
    }

    override fun update() {
        super.update()
        val decorationViewLayoutParams =
            mDecorationView?.layoutParams as FrameLayout.LayoutParams
        decorationViewLayoutParams.width =
            (ELEMENT_SCALE_ROTATE_ICON_WIDTH * 2 + mShowingViewParams.width * mScale + mShowingViewParams.leftMargin + mShowingViewParams.rightMargin).toInt()
        decorationViewLayoutParams.height =
            (ELEMENT_SCALE_ROTATE_ICON_WIDTH * 2 + mOriginHeight * mScale + mShowingViewParams.topMargin + mShowingViewParams.bottomMargin).toInt()
        mDecorationView?.apply {
            x = getRealX(mMoveX, decorationViewLayoutParams.width)
            y = getRealY(mMoveY, decorationViewLayoutParams.height)
            layoutParams = decorationViewLayoutParams
            rotation = mRotate
            alpha = mAlpha
            bringToFront()
        }
        if (mElementShowingView?.visibility == View.INVISIBLE){
            mElementShowingView!!.visibility = View.VISIBLE
        }
    }

    override fun select() {
        super.select()
        mDecorationView?.visibility = View.VISIBLE
    }

    override fun unSelect() {
        super.unSelect()
        mDecorationView?.visibility = View.GONE
    }

    override fun remove() {
        mElementContainerView?.removeView(mDecorationView)
        super.remove()
    }

    override fun onSingleFingerMoveStart() {
        super.onSingleFingerMoveStart()
        mDecorationView?.visibility = View.GONE
    }

    override fun onSingleFingerMoveEnd() {
        super.onSingleFingerMoveEnd()
        mDecorationView?.visibility = View.VISIBLE
    }

    override fun onDoubleFingerScaleAndRotateStart(deltaRotate: Float, deltaScale: Float) {
        super.onDoubleFingerScaleAndRotateStart(deltaRotate, deltaScale)
        mDecorationView?.visibility = View.GONE
    }

    override fun onDoubleFingerScaleAndRotateEnd() {
        super.onDoubleFingerScaleAndRotateEnd()
        mDecorationView?.visibility = View.VISIBLE
    }

    /**
     * 当前 Element 开始单指旋转缩放
     */
    fun onSingleFingerScaleAndRotateStart() {
        mDecorationView?.visibility = View.GONE
        isSingleFingerScaleAndRotate = true
    }

    /**
     * 当前 Element 单指旋转缩放中
     */
    fun onSingleFingerScaleAndRotateProcess(motionEventX: Float, motionEventY: Float) {
        scaleAndRotateForSingleFinger(motionEventX, motionEventY)
    }

    /**
     * 当前 Element 单指旋转缩放结束
     */
    fun onSingleFingerScaleAndRotateEnd() {
        mDecorationView?.visibility = View.VISIBLE
        isSingleFingerScaleAndRotate = true
    }

    /**
     * @return 坐标是否处于旋转缩放按钮区域中
     * [motionEventX] 点击的X坐标
     * [motionEventY] 点击的Y坐标
     */
    fun isInScaleAndRotateButton(motionEventX: Float, motionEventY: Float): Boolean {
        return isPointInTheRect(
            motionEventX, motionEventY,
            scaleAndRotateButtonRect
        )
    }

    /**
     * @return 坐标是否处于删除按钮区域中
     * [motionEventX] 点击的X坐标
     * [motionEventY] 点击的Y坐标
     */
    fun isInRemoveButton(motionEventX: Float, motionEventY: Float): Boolean {
        return isPointInTheRect(motionEventX, motionEventY, removeButtonRect)
    }

    /**
     * @return 坐标是否处于编辑按钮区域中
     * [motionEventX] 点击的X坐标
     * [motionEventY] 点击的Y坐标
     */
    fun isInEditButton(motionEventX: Float, motionEventY: Float): Boolean {
        return isPointInTheRect(motionEventX, motionEventY, editButtonRect)
    }

    /**
     * 计算单指缩放的旋转角度
     * [motionEventX] 点击的X坐标
     * [motionEventY] 点击的Y坐标
     */
    private fun scaleAndRotateForSingleFinger(motionEventX: Float, motionEventY: Float) {
        val originWholeRect = originRedundantRect
        val halfWidth = originWholeRect.width() / 2.0f
        val halfHeight = originWholeRect.height() / 2.0f
        val newRadius = PointF.length(
            motionEventX - originWholeRect.centerX(),
            motionEventY - originWholeRect.centerY()
        )
        val oldRadius = PointF.length(halfWidth, halfHeight)
        mScale = newRadius / oldRadius
        mScale = if (mScale < MIN_SCALE_FACTOR) MIN_SCALE_FACTOR else mScale
        mScale = if (mScale > MAX_SCALE_FACTOR) MAX_SCALE_FACTOR else mScale
        mRotate = Math
            .toDegrees(
                atan2(halfWidth.toDouble(), halfHeight.toDouble())
                        - atan2(
                    (motionEventX - originWholeRect.centerX()).toDouble(),
                    (motionEventY - originWholeRect.centerY()).toDouble()
                )
            ).toFloat()
        mRotate = getCanonicalRotation(mRotate)
        LogUtil.d(
            TAG,
            "scaleAndRotateForSingleFinger mScale:" + mScale + ",mRotate:" + mRotate + ",x:"
                    + motionEventX + ",y:"
                    + motionEventY + ",rect:" + originWholeRect + ",newRadius:" + newRadius + "oldRadius:"
                    + oldRadius
        )
    }

    /**
     * @return 包括旋转、删除按钮的最小矩形区域
     */
    override val wholeRect: Rect
        get() {
            val redundantAreaRect = redundantAreaRect
            return Rect(
                redundantAreaRect.left - ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2,
                redundantAreaRect.top - ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2,
                redundantAreaRect.right + ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2,
                redundantAreaRect.bottom + ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2
            )
        }

    /**
     * 获取 元素 原始(没有 scale 过)的区域，包括延伸区域
     *
     * @return
     */
    protected val originRedundantRect: Rect
        get() {
            val viewCenterX = (mEditRect?.centerX() ?: 0).toFloat()
            val viewCenterY = (mEditRect?.centerY() ?: 0).toFloat()
            val contentWidth = mOriginWidth
            val contentHeight = mOriginHeight
            val originContentRect = Rect(
                (viewCenterX + mMoveX
                        - contentWidth / 2).toInt(),
                (viewCenterY + mMoveY
                        - contentHeight / 2).toInt(),
                (viewCenterX + mMoveX
                        + contentWidth / 2).toInt(),
                (viewCenterY + mMoveY
                        + contentHeight / 2).toInt()
            )
            return Rect(
                originContentRect.left - mRedundantAreaLeftRight,
                originContentRect.top - mRedundantAreaLeftRight,
                originContentRect.right + mRedundantAreaTopBottom,
                originContentRect.bottom + mRedundantAreaTopBottom
            )
        }

    /**
     * @return 获取 元素 包括延伸区域的区域
     */
    protected val redundantAreaRect: Rect
        get() {
            val dstDrawRect = contentRect
            return Rect(
                dstDrawRect.left - mRedundantAreaLeftRight,
                dstDrawRect.top - mRedundantAreaLeftRight,
                dstDrawRect.right + mRedundantAreaTopBottom,
                dstDrawRect.bottom + mRedundantAreaTopBottom
            )
        }

    /**
     * @return 获取 元素 删除按钮在 @EditRect 坐标下的 Rect
     */
    protected val removeButtonRect: Rect
        get() {
            val redundantAreaRect = redundantAreaRect
            return Rect(
                redundantAreaRect.left - ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2,
                redundantAreaRect.top - ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2,
                redundantAreaRect.left + ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2,
                redundantAreaRect.top + ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2
            )
        }

    /**
     * @return 元素 旋转缩放按钮在 @EditRect 坐标下的 Rect
     */
    protected val scaleAndRotateButtonRect: Rect
        get() {
            val redundantAreaRect = redundantAreaRect
            return Rect(
                redundantAreaRect.right - ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2,
                redundantAreaRect.bottom - ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2,
                redundantAreaRect.right + ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2,
                redundantAreaRect.bottom + ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2
            )
        }


    /**
     * @return 元素 旋转缩放按钮在 @EditRect 坐标下的 Rect
     */
    protected val editButtonRect: Rect
        get() {
            val redundantAreaRect = redundantAreaRect
            return Rect(
                redundantAreaRect.right - ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2,
                redundantAreaRect.top - ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2,
                redundantAreaRect.right + ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2,
                redundantAreaRect.top + ELEMENT_SCALE_ROTATE_ICON_WIDTH / 2
            )
        }

}