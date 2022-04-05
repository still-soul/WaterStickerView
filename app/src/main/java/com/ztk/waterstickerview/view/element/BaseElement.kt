package com.ztk.waterstickerview.view.element

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import com.ztk.waterstickerview.consts.WaterMarkConstant
import com.ztk.waterstickerview.view.ElementContainerView
import com.ztk.waterstickerview.utils.LogUtil
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 各种元素的展示基类，用于容纳数据和被展示的 view
 * 目前有贴纸view[ImageViewElement]、智能水印view[WaterMarkElement]
 * 如需新增类型类似以上两种
 * @author xiaoman
 */
abstract class BaseElement protected constructor(private val mContext: Context, stickerType : String) : Cloneable {
    /**
     * 图像的层级
     */
    var mZIndex = -1

    /**
     * 初始化后相对 mElementContainerView 中心 的移动距离
     */
    var mMoveX = 0f

    /**
     * 初始化后相对 mElementContainerView 中心 的移动距离
     */
    var mMoveY = 0f

    /**
     * 初始化时内容的宽度
     */
    protected var mOriginWidth = 0f

    /**
     * 初始化时内容的高度
     */
    protected var mOriginHeight = 0f

    /**
     * 可绘制的区域
     */
    protected var mEditRect: Rect? = null

    /**
     * 图像顺时针旋转的角度
     */
    var mRotate = 0f

    /**
     * 图像缩放的大小
     */
    var mScale = 1.0f

    /**
     * 图像的透明度
     */
    var mAlpha = 1.0f

    /**
     * 是否处于选中状态
     */
    private var mIsSelected = false

    /**
     * 是否处于单指移动的状态
     */
    private var isSingerFingerMove = false

    /**
     * 是否处于双指旋转缩放的状态
     */
    private var isDoubleFingerScaleAndRotate = false

    /**
     * element 中 mElementShowingView 的父 View，用于包容所有的 element 需要显示的 view
     */
    protected var mElementContainerView: ElementContainerView? = null

    /**
     * 用于展示内容的 view
     */
    var mElementShowingView: View? = null

    /**
     * 内容区域左右向外延伸的一段距离，用于扩展元素的可点击区域
     */
    protected var mRedundantAreaLeftRight = 0

    /**
     * 内容区域上下向外延伸的一段距离，用于扩展元素的可点击区域
     */
    protected var mRedundantAreaTopBottom = 0

    /**
     * 是否让 showing view 响应选中该 元素 之后的点击事件
     */
    private var mIsResponseSelectedClick = false

    /**
     * 是否在刷新 showing view 的时候，真正修改 height、width 之类的参数。一般来说只是使用 scale 和 rotate 来刷新 view
     */
    private var isRealChangeShowingView = false

    init {
        MAX_SCALE_FACTOR = if ( WaterMarkConstant.TYPE_CUTE_STICKER == stickerType)12f else 4f
    }

    /**
     * 设置初始固定尺寸
     */
    protected open fun setInitialSize() {
        // Intentionally empty, this can be optionally implemented by subclasses.
    }

    /**
     * 当前 element 被添加到 DecorationContainerView 中
     */
    open fun add(elementContainerView: ElementContainerView?, showEdit: Boolean) {
        mElementContainerView = elementContainerView
        if (mElementShowingView == null) {
            setInitialSize()
            mElementShowingView = initView()
            val with =
                if (mOriginWidth == 0f) FrameLayout.LayoutParams.WRAP_CONTENT else mOriginWidth.toInt()
            val height =
                if (mOriginHeight == 0f) FrameLayout.LayoutParams.WRAP_CONTENT else mOriginHeight.toInt()
            val showingViewLayoutParams = FrameLayout.LayoutParams(with, height)
            mElementShowingView!!.layoutParams = showingViewLayoutParams
            mElementContainerView!!.addView(mElementShowingView)
            mElementShowingView!!.visibility = View.INVISIBLE
        } else {
            update()
        }
    }

    /**
     * 初始化需要展示的 view，返回需要展示的 view
     */
    protected abstract fun initView(): View

    /**
     * 当前 element 被从 DecorationContainerView 中删除
     */
    open fun remove() {
        mElementContainerView?.removeView(mElementShowingView)
        mElementContainerView = null
    }

    /**
     * 当前 element 开始单指移动
     */
    open fun onSingleFingerMoveStart() {
        isSingerFingerMove = true
    }

    /**
     * 当前 element 单指移动中
     */
    fun onSingleFingerMoveProcess(motionEventX: Float, motionEventY: Float) {
        mMoveX += motionEventX
        mMoveY += motionEventY
    }

    /**
     * 当前 element 单指移动结束
     */
    open fun onSingleFingerMoveEnd() {
        isSingerFingerMove = false
    }

    /**
     * 当前 element 开始双指旋转缩放
     */
    open fun onDoubleFingerScaleAndRotateStart(deltaRotate: Float, deltaScale: Float) {
        doubleFingerScaleAndRotate(deltaRotate, deltaScale)
        isDoubleFingerScaleAndRotate = true
    }

    /**
     * 当前 element 双指旋转缩放中
     */
    fun onDoubleFingerScaleAndRotateProcess(deltaRotate: Float, deltaScale: Float) {
        doubleFingerScaleAndRotate(deltaRotate, deltaScale)
    }

    /**
     * 当前 element 双指旋转缩放结束
     */
    open fun onDoubleFingerScaleAndRotateEnd() {
        isDoubleFingerScaleAndRotate = false
    }

    private fun doubleFingerScaleAndRotate(deltaRotate: Float, deltaScale: Float) {
        mScale *= deltaScale
        mScale = mScale.coerceAtLeast(MIN_SCALE_FACTOR)
        mScale = mScale.coerceAtMost(MAX_SCALE_FACTOR)
        mRotate += deltaRotate
        mRotate %= 360
    }

    /**
     * 当前 element 选中了
     */
    open fun select() {
        mZIndex = 0
        mIsSelected = true
        mElementShowingView?.bringToFront()
    }

    /**
     * 当前 element 取消选中了
     */
    open fun unSelect() {
        mIsSelected = false
    }

    /**
     * 刷新展示的 view
     */
    open fun update() {
        if (isRealChangeShowingView) {
            realChangeUpdate()
        } else {
            updateView()
        }
        mElementShowingView?.rotation = mRotate
    }

    private fun realChangeUpdate() {
        val showingViewLayoutParams =
            mElementShowingView?.layoutParams as? FrameLayout.LayoutParams
        showingViewLayoutParams?.width = (mOriginWidth * mScale).toInt()
        showingViewLayoutParams?.height = (mOriginHeight * mScale).toInt()
        if (!limitElementAreaLeftRight()) {
            mMoveX = if (mMoveX < 0) -1 * leftRightLimitLength else leftRightLimitLength
        }

        if (!limitElementAreaTopBottom()) {
            mMoveY = if (mMoveY < 0) -1 * bottomTopLimitLength else bottomTopLimitLength
        }
        mElementShowingView?.layoutParams = showingViewLayoutParams
    }


    private fun updateView() {
        mElementShowingView?.scaleX = mScale
        mElementShowingView?.scaleY = mScale
        if (!limitElementAreaLeftRight()) {
            mMoveX = if (mMoveX < 0) -1 * leftRightLimitLength else leftRightLimitLength
        }
        mElementShowingView?.translationX = getRealX(mMoveX)
        if (!limitElementAreaTopBottom()) {
            mMoveY = if (mMoveY < 0) -1 * bottomTopLimitLength else bottomTopLimitLength
        }
        mElementShowingView?.translationY = getRealY(mMoveY)
    }

    /**
     * @return 获取 view 在 mEditRect 中的真实位置
     */
    private fun getRealX(moveX: Float): Float {
        return (mEditRect?.centerX()?:0) + moveX - mOriginWidth / 2
    }

    protected fun getRealX(moveX: Float, width: Int): Float {
        return (mEditRect?.centerX()?:0) + moveX - width / 2
    }

    /**
     * @return 同 getRealX
     */
    private fun getRealY(moveY: Float): Float {
        return (mEditRect?.centerY()?:0) + moveY - mOriginHeight / 2
    }

    protected fun getRealY(moveY: Float, height: Int): Float {
        return (mEditRect?.centerY()?:0) + moveY - height / 2
    }

    /**
     * 限制 element 左右移动的区域
     * @return false 表示已达到限制区域，不可再继续移动
     */
    private fun limitElementAreaLeftRight(): Boolean {
        val halfLimitWidthLength = leftRightLimitLength
        LogUtil.d("limitElementAreaLeftRight halfWidth:$halfLimitWidthLength,moveX:$mMoveX")
        return -1 * halfLimitWidthLength <= mMoveX && mMoveX <= halfLimitWidthLength
    }

    /**
     * @return 同 limitElementAreaLeftRight 限制上下的范围
     */
    private fun limitElementAreaTopBottom(): Boolean {
        val halfLimitHeightLength = bottomTopLimitLength
        LogUtil.d("limitElementAreaLeftRight halfHeight:$halfLimitHeightLength,moveY:$mMoveY")
        return -1 * halfLimitHeightLength <= mMoveY && mMoveY <= halfLimitHeightLength
    }

    private val leftRightLimitLength: Float
        get() = ((mEditRect?.width()?:0) / 2 + wholeRect.width() / 2 - Element_LIMIT_AREA_WIDTH).toFloat()
    private val bottomTopLimitLength: Float
        get() = ((mEditRect?.height()?:0) / 2 + wholeRect.height() / 2 - Element_LIMIT_AREA_WIDTH).toFloat()

    /**
     * @param motionEventX X坐标
     * @param motionEventY Y坐标
     * @return 判断坐标是否处于整个元素中，包括延伸区域
     */
    fun isInWholeDecoration(motionEventX: Float, motionEventY: Float): Boolean {
        return isPointInTheRect(motionEventX, motionEventY, wholeRect)
    }

    protected fun isPointInTheRect(motionEventX: Float, motionEventY: Float, rect: Rect): Boolean {
        var afterRotatePoint = PointF(motionEventX, motionEventY)
        if (mRotate != 0f) {
            val mInvertMatrix = Matrix()
            mInvertMatrix.postRotate(
                -mRotate, contentRect.centerX().toFloat(),
                contentRect.centerY().toFloat()
            )
            val point = floatArrayOf(motionEventX, motionEventY)
            mInvertMatrix.mapPoints(point)
            afterRotatePoint = PointF(point[0], point[1])
        }
        LogUtil.d("isPointInTheRect rect:$rect,model:$this")
        return rect.contains(afterRotatePoint.x.toInt(), afterRotatePoint.y.toInt())
    }

    /**
     * @return 获取元素内容区域，不包括延伸区域
     */
    val contentRect: Rect
        get() {
            val viewCenterX = (mEditRect?.centerX() ?:0).toFloat()
            val viewCenterY = (mEditRect?.centerY()?:0).toFloat()
            val contentWidth = mOriginWidth * mScale
            val contentHeight = mOriginHeight * mScale
            return Rect(
                (viewCenterX + mMoveX
                        - contentWidth / 2).toInt(),
                (viewCenterY + mMoveY
                        - contentHeight / 2).toInt(),
                (viewCenterX + mMoveX
                        + contentWidth / 2).toInt(),
                (viewCenterY + mMoveY
                        + contentHeight / 2).toInt()
            )
        }

    /**
     * @return 获取元素整个区域，包括延伸区域
     */
    protected open val wholeRect: Rect
        get() {
            val dstDrawRect = contentRect
            return Rect(
                dstDrawRect.left - mRedundantAreaLeftRight,
                dstDrawRect.top - mRedundantAreaLeftRight,
                dstDrawRect.right + mRedundantAreaTopBottom,
                dstDrawRect.bottom + mRedundantAreaTopBottom
            )
        }

    fun setEditRect(editRect: Rect?) {
        mEditRect = editRect
    }

    /**
     * 只有在刷新的时候真正修改了 showing view 的 params 才能响应点击事件
     * 要不然事件分发会出错
     * @return 是否让 showing view 响应选中该 元素 之后的点击事件
     */
    val isShowingViewResponseSelectedClick: Boolean
        get() = mIsResponseSelectedClick && isRealChangeShowingView

    companion object {
        /**
         * 最小缩放倍数
         */
        const val MIN_SCALE_FACTOR = 0.3f

        /**
         * 最大缩放倍数
         */
        var MAX_SCALE_FACTOR = 4f

        /**
         * element 必须在屏幕中显示的最小宽度
         */
        private const val Element_LIMIT_AREA_WIDTH = 180
        fun getCanonicalRotation(rotation: Float): Float {
            if (abs(rotation % 90) < 3) {
                return ((rotation / 90).roundToInt() * 90).toFloat()
            }
            return if (abs(rotation % 45) < 3) {
                ((rotation / 45).roundToInt() * 45).toFloat()
            } else rotation
        }

        fun isSameElement(
            wsElementOne: BaseElement?,
            wsElementTwo: BaseElement?
        ): Boolean {
            return if (wsElementOne == null || wsElementTwo == null) {
                false
            } else {
                wsElementOne == wsElementTwo
            }
        }
    }
}