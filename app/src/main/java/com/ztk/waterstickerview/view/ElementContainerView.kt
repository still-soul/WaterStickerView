package com.ztk.waterstickerview.view

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Build
import android.os.Vibrator
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.ztk.waterstickerview.utils.MultiTouchGestureDetector
import com.ztk.waterstickerview.view.element.BaseElement
import com.ztk.waterstickerview.utils.LogUtil
import com.ztk.waterstickerview.utils.MotionEventUtil.copyMotionEvent
import java.util.*

/**
 * 容纳元素的基类，用于接收各种手势操作和维持数据结构
 * @author xiaoman
 */
open class ElementContainerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    /**
     * 当前手势所处的模式
     */
    private var mMode = BaseActionMode.SELECTED_CLICK_OR_MOVE

    /**
     * 元素 可绘制的区域，也就是当前 View 的区域
     */
    private val mEditorRect = Rect()

    /**
     * 处理单指手势
     */
    private var mDetector: GestureDetector? = null

    /**
     * 处理双指手势
     */
    private var mMultiTouchGestureDetector: MultiTouchGestureDetector? = null

    /**
     * 是否处于双指手势状态
     */
    private var mIsInDoubleFinger = false

    /**
     * 是否需要自动取消选中
     */
    private var mIsNeedAutoUnSelect = true

    /**
     * 自动取消选中的时间，默认 2000 毫秒
     */
    private var mAutoUnSelectDuration: Long = 2000
    private var mUnSelectRunnable = Runnable { unSelectElement() }

    /**
     * 当前选中的元素
     */
    var selectElement: BaseElement? = null
        protected set
    private var mElementList = LinkedList<BaseElement>()

    /**
     * 监听列表
     */
    private var mElementActionListenerSet: MutableSet<ElementActionListener> = HashSet()

    /**
     * 储存当前 up down 事件，以便在需要的时候进行事件分发
     */
    private var mUpDownMotionEvent = arrayOfNulls<MotionEvent>(2)
    private var mVibrator: Vibrator? = null

    /**
     * 手指是否移动过
     */
    private var isMove = false

    protected fun init() {
        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            override fun onGlobalLayout() {
                viewLayoutComplete()
                if (width != 0 && height != 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
                LogUtil.d("mEditorRect:$mEditorRect")
            }
        })
        addDetector()
        mVibrator = context.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     * view layout 完成 width 和 height 都有了，这个时候可以做一些初始化的事情
     */
    protected fun viewLayoutComplete() {
        mEditorRect[0, 0, width] = height
    }

    /**
     * 添加手势检测器
     */
    private fun addDetector() {
        mDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                return this@ElementContainerView.onFling(e1, e2, velocityX, velocityY)
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                return singleFingerMove(e2, floatArrayOf(distanceX, distanceY))
            }
        })
        mMultiTouchGestureDetector =
            MultiTouchGestureDetector(context, object : MultiTouchGestureDetector.SimpleOnMultiTouchGestureListener() {
                var mIsMultiTouchBegin = false
                override fun onScaleOrRotate(detector: MultiTouchGestureDetector?) {
                    if (mIsInDoubleFinger) {
                        doubleFingerScaleAndRotateProcess(
                            detector?.getRotation() ?: 0f,
                            detector?.getScale() ?: 0f
                        )
                    } else {
                        doubleFingerScaleAndRotateStart(
                            detector?.getRotation() ?: 0f,
                            detector?.getScale() ?: 0f
                        )
                        mIsInDoubleFinger = true
                    }
                }

                override fun onMove(detector: MultiTouchGestureDetector?) {
                    if (mIsMultiTouchBegin) {
                        mIsMultiTouchBegin = false
                    } else {
                        selectElement?.onSingleFingerMoveProcess(
                            detector?.getMoveX() ?: 0f,
                            detector?.getMoveY() ?: 0f
                        )
                    }
                }

                override fun onBegin(detector: MultiTouchGestureDetector?): Boolean {
                    mIsMultiTouchBegin = true
                    return super.onBegin(detector)
                }

                override fun onEnd(detector: MultiTouchGestureDetector?) {
                    super.onEnd(detector)
                    mIsMultiTouchBegin = false
                }
            })
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (selectElement?.isShowingViewResponseSelectedClick == true) {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    mUpDownMotionEvent[0] = copyMotionEvent(ev)
                }
                MotionEvent.ACTION_UP -> {
                    mUpDownMotionEvent[1] = copyMotionEvent(ev)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        LogUtil.d(
            "mIsInDoubleFinger:"
                    + mIsInDoubleFinger + ",x0:" + event.x + ",y0:" + event.y
        )
        if (isDoubleFingerInSelectElement(event)) {
            mMultiTouchGestureDetector?.onTouchEvent(event)
        } else {
            if (mIsInDoubleFinger) {
                doubleFingerScaleAndRotateEnd()
                mIsInDoubleFinger = false
                return true
            }
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isMove = false
                    cancelAutoUnSelectDecoration()
                    singleFingerDown(event)
                }
                MotionEvent.ACTION_MOVE -> {
                    isMove = true
                    mDetector?.onTouchEvent(event)
                }
                MotionEvent.ACTION_UP -> {
                    autoUnSelectDecoration()
                    singleFingerUp(event)
                }
            }
        }
        return true
    }

    private fun isDoubleFingerInSelectElement(event: MotionEvent): Boolean {
        if (selectElement != null && event.pointerCount > 1) {
            val x0 = event.getX(0).toDouble()
            val y0 = event.getY(0).toDouble()
            val x1 = event.getX(1).toDouble()
            val y1 = event.getY(1).toDouble()
            if (selectElement!!.isInWholeDecoration(x0.toFloat(), y0.toFloat())
                || selectElement!!.isInWholeDecoration(x1.toFloat(), y1.toFloat())
            ) {
                LogUtil.d("isDoubleFingerInSelectElement -> x0:$x0,x1:$x1,y0:$y0,y1:$y1")
                return true
            }
        }
        return false
    }

    private fun singleFingerDown(e: MotionEvent) {
        val x = e.x
        val y = e.y
        mMode = BaseActionMode.SELECTED_CLICK_OR_MOVE
        val clickedElement = findElementByPosition(x, y)
        LogUtil.d("singleFingerDown -> x:$x,y:$y,clickedElement:$clickedElement,mSelectedElement:$selectElement")
        //选中状态下
        if (selectElement != null) {
            selectSingleFingerDown(clickedElement, e, x, y)
        }
        //未选中状态下
        else {
            if (clickedElement != null) {
                mMode = BaseActionMode.SELECT
                selectElement(clickedElement)
                update()
                LogUtil.d("singleFingerDown select new element")
            } else {
                mMode = BaseActionMode.SINGLE_TAP_BLANK_SCREEN
                LogUtil.d("singleFingerDown SINGLE_TAP_BLANK_SCREEN")
            }
        }
    }

    /**
     * 选中状态下的down事件
     */
    private fun selectSingleFingerDown(
        clickedElement: BaseElement?,
        e: MotionEvent,
        x: Float,
        y: Float
    ) {
        if (BaseElement.isSameElement(
                clickedElement,
                selectElement
            )
        ) {
            val result = downSelectTapOtherAction(e)
            if (result) {
                LogUtil.d("singleFingerDown other action")
                return
            }
            if (selectElement!!.isInWholeDecoration(x, y)) {
                mMode = BaseActionMode.SELECTED_CLICK_OR_MOVE
                LogUtil.d("singleFingerDown SELECTED_CLICK_OR_MOVE")
                return
            }
            LogUtil.e("singleFingerDown error not action")
        } else {
            if (clickedElement == null) {
                mMode = BaseActionMode.SINGLE_TAP_BLANK_SCREEN
                //点击水印、贴纸之外的地方取消选中
                unSelectElement()
                LogUtil.d("singleFingerDown SINGLE_TAP_BLANK_SCREEN")
            } else {
                mMode = BaseActionMode.SELECT
                unSelectElement()
                selectElement(clickedElement)
                update()
                LogUtil.d("singleFingerDown unSelect old element, select new element")
            }
        }
    }

    private fun singleFingerMove(e2: MotionEvent?, distanceXY: FloatArray): Boolean {
        LogUtil.d(
            "singleFingerMove move -> distanceX:" + distanceXY[0] + "distanceY:"
                    + distanceXY[1]
        )
        if (scrollSelectTapOtherAction(e2, distanceXY)) {
            return true
        } else {
            if (mMode == BaseActionMode.SELECTED_CLICK_OR_MOVE || mMode == BaseActionMode.SELECT || mMode == BaseActionMode.MOVE) {
                if (mMode == BaseActionMode.SELECTED_CLICK_OR_MOVE || mMode == BaseActionMode.SELECT) {
                    singleFingerMoveStart(distanceXY[0], distanceXY[1])
                } else {
                    singleFingerMoveProcess(distanceXY[0], distanceXY[1])
                }
                update()
                mMode = BaseActionMode.MOVE
                return true
            }
        }
        return false
    }

    private fun singleFingerUp(event: MotionEvent) {
        LogUtil.d("singleFingerUp -> x:" + event.x + ",y:" + event.y)
        if (!upSelectTapOtherAction(event)) {
            when (mMode) {
                BaseActionMode.SELECTED_CLICK_OR_MOVE -> {
                    val x = event.x
                    val y = event.y
                    if (mMode != BaseActionMode.SELECT
                        && selectElement?.isInWholeDecoration(x, y) == true
                        && !isMove
                    ) {
                        //选中状态下点击水印变为未选中状态
                        unSelectElement()
                    }else{
                        selectedClick()
                        update()
                    }
                    return
                }
                BaseActionMode.SINGLE_TAP_BLANK_SCREEN -> {
                    onClickBlank()
                    return
                }
                BaseActionMode.MOVE -> {
                    singleFingerMoveEnd()
                    return
                }
                else -> LogUtil.d("singleFingerUp other action")
            }
        }
    }

    private fun singleFingerMoveStart(distanceX: Float, distanceY: Float) {
        selectElement?.onSingleFingerMoveStart()
        callListener(object : Consumer<ElementActionListener?> {
            override fun accept(t: ElementActionListener?) {
                t?.onSingleFingerMoveStart(selectElement)
            }
        })
        LogUtil.d("singleFingerMoveStart move start -> distanceX:$distanceX,distanceY:$distanceY")
    }

    private fun singleFingerMoveProcess(distanceX: Float, distanceY: Float) {
        selectElement?.onSingleFingerMoveProcess(-distanceX, -distanceY)
        callListener(object : Consumer<ElementActionListener?> {
            override fun accept(t: ElementActionListener?) {
                t?.onSingleFingerMoveProcess(selectElement)
            }
        })
        LogUtil.d("singleFingerMoveStart move progress -> distanceX:$distanceX,distanceY:$distanceY")
    }

    private fun singleFingerMoveEnd() {
        callListener(object : Consumer<ElementActionListener?> {
            override fun accept(t: ElementActionListener?) {
                t?.onSingleFingerMoveEnd(selectElement)
            }
        })
        selectElement?.onSingleFingerMoveEnd()
        LogUtil.d("singleFingerMoveEnd move end")
    }

    protected fun doubleFingerScaleAndRotateStart(deltaRotate: Float, deltaScale: Float) {
        mMode = BaseActionMode.DOUBLE_FINGER_SCALE_AND_ROTATE
        selectElement?.onDoubleFingerScaleAndRotateStart(deltaRotate, deltaScale)
        update()
        callListener(object : Consumer<ElementActionListener?> {
            override fun accept(t: ElementActionListener?) {
                t?.onDoubleFingerScaleAndRotateStart(selectElement)
            }
        })
        mIsInDoubleFinger = true
        LogUtil.d("doubleFingerScaleAndRotateStart start -> deltaRotate:$deltaRotate,deltaScale:$deltaScale")
    }

    protected fun doubleFingerScaleAndRotateProcess(deltaRotate: Float, deltaScale: Float) {
        selectElement?.onDoubleFingerScaleAndRotateProcess(deltaRotate, deltaScale)
        update()
        callListener(object : Consumer<ElementActionListener?> {
            override fun accept(t: ElementActionListener?) {
                t?.onDoubleFingerScaleAndRotateProcess(selectElement)
            }
        })
        LogUtil.d("doubleFingerScaleAndRotateStart process -> deltaRotate:$deltaRotate,deltaScale:$deltaScale")
    }

    private fun doubleFingerScaleAndRotateEnd() {
        selectElement?.onDoubleFingerScaleAndRotateEnd()
        callListener(object : Consumer<ElementActionListener?> {
            override fun accept(t: ElementActionListener?) {
                t?.onDoubleFingerScaleRotateEnd(selectElement)
            }
        })
        mIsInDoubleFinger = false
        autoUnSelectDecoration()
        LogUtil.d("doubleFingerScaleAndRotateEnd end")
    }

    protected open fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return true
    }

    private fun onClickBlank() {
        callListener(object : Consumer<ElementActionListener?> {
            override fun accept(t: ElementActionListener?) {
                t?.onSingleTapBlankScreen(selectElement)
            }
        })
        LogUtil.d("onClickBlank")
    }

    /**
     * 按下了已经选中的元素，如果子类中有操作的话可以给它，优先级最高
     */
    protected open fun downSelectTapOtherAction(event: MotionEvent): Boolean {
        LogUtil.d("downSelectTapOtherAction :event:$event")
        return false
    }

    /**
     * 滑动已经选中的元素，如果子类中有操作的话可以给它，优先级最高
     * [event] 当前的触摸事件
     * [distance] size 为 2，里面分别为 x 轴的 delta 位移，和 y 轴的 delta 位移
     */
    protected open fun scrollSelectTapOtherAction(
        event: MotionEvent?,
        distance: FloatArray
    ): Boolean {
        LogUtil.d("scrollSelectTapOtherAction -> event:" + event + ",distanceX:" + distance[0] + ",distanceY:" + distance[1])
        return false
    }

    /**
     * 抬起已经选中的元素，如果子类中有操作的话可以给它，优先级最高
     */
    protected open fun upSelectTapOtherAction(event: MotionEvent): Boolean {
        LogUtil.d("upSelectTapOtherAction -> event:$event")
        return false
    }

    /**
     * 添加一个监听器
     * [elementActionListener] 监听器
     */
    fun addElementActionListener(elementActionListener: ElementActionListener?) {
        if (elementActionListener == null) {
            return
        }
        mElementActionListenerSet.add(elementActionListener)
    }

    /**
     * 移除一个监听器
     * [elementActionListener] 监听器
     */
    fun removeElementActionListener(elementActionListener: ElementActionListener) {
        mElementActionListenerSet.remove(elementActionListener)
    }

    val elementList: List<BaseElement>
        get() = mElementList

    /**
     * [element] 添加的元素
     * @return  加元素是否成功，如果元素已经存在，那么添加失败
     */
    fun addElement(element: BaseElement?, showEdit: Boolean): Boolean {
        LogUtil.i("addElement : element:$element")
        if (element == null) {
            LogUtil.w("addElement element is null")
            return false
        }
        if (mElementList.contains(element)) {
            LogUtil.w("addElement element is added")
            return false
        }
        for (i in mElementList.indices) {
            val nowElement = mElementList[i]
            nowElement.mZIndex++
        }
        element.mZIndex = 0
        element.setEditRect(mEditorRect)
        mElementList.addFirst(element)
        element.add(this, showEdit)
        callListener(object : Consumer<ElementActionListener?> {
            override fun accept(t: ElementActionListener?) {
                t?.onAdd(element)
            }
        })
        autoUnSelectDecoration()
        return true
    }

    /**
     * @return 默认删除最顶层的元素
     */
    fun deleteElement(): Boolean {
        return if (mElementList.size <= 0) {
            false
        } else deleteElement(mElementList.first)
    }

    /**
     * 删除一个元素，只能删除当前最顶层的元素
     * [element] 被删除的元素
     * @return 删除是否成功
     */
    private fun deleteElement(element: BaseElement?): Boolean {
        LogUtil.i("deleteElement: element:$element")
        if (element == null) {
            LogUtil.w("deleteElement element is null")
            return false
        }
        if (mElementList.first !== element) {
            LogUtil.w("deleteElement element is not in top")
            return false
        }
        mElementList.pop()
        for (i in mElementList.indices) {
            val nowElement = mElementList[i]
            nowElement.mZIndex--
        }
        element.remove()
        callListener(object : Consumer<ElementActionListener?> {
            override fun accept(t: ElementActionListener?) {
                t?.onDelete(element)
            }
        })
        return true
    }

    /**
     * 刷新界面，具体绘制逻辑在 @Element 中
     */
    fun update() {
        if (selectElement == null) {
            LogUtil.w("update error selected element is null")
            return
        }
        selectElement!!.update()
        LogUtil.d("update")
    }

    /**
     * [x]  container view 中的坐标
     * [y]  container view 中的坐标
     * @return 根据位置找到元素
     */
    fun findElementByPosition(x: Float, y: Float): BaseElement? {
        var realFoundedElement: BaseElement? = null
        for (i in mElementList.indices.reversed()) {
            val nowElement = mElementList[i]
            if (nowElement.isInWholeDecoration(x, y)) {
                realFoundedElement = nowElement
            }
        }
        LogUtil.d(
            "findElementByPosition:"
                    + realFoundedElement + ",x:" + x + ",y:" + y
        )
        return realFoundedElement
    }

    /**
     * 选中一个元素，如果需要选中的元素没有被添加到 container 中则选中失败
     * [element] 被选中的元素
     * @return 是否选中成功
     */
    fun selectElement(element: BaseElement?): Boolean {
        LogUtil.i("selectElement :$element")
        if (element == null) {
            LogUtil.w("selectElement element is null")
            return false
        }
        if (!mElementList.contains(element)) {
            LogUtil.w("selectElement element was not added")
            return false
        }
        for (i in mElementList.indices) {
            val nowElement = mElementList[i]
            if (element != nowElement
                && element.mZIndex > nowElement.mZIndex
            ) {
                nowElement.mZIndex++
            }
        }
        mElementList.removeAt(element.mZIndex)
        element.select()
        mElementList.addFirst(element)
        selectElement = element
        callListener(object : Consumer<ElementActionListener?> {
            override fun accept(t: ElementActionListener?) {
                t?.onSelect(element)
            }
        })
        return true
    }

    /**
     * 取消选中当前元素
     * @return true 表示被选中的 元素 取消选中成功，false 表示当前没有被选中的 元素
     */
    fun unSelectElement(): Boolean {
        LogUtil.i("unSelectElement:$selectElement")
        if (selectElement == null) {
            LogUtil.w("unSelectElement unSelect element is null")
            return false
        }
        if (!mElementList.contains(selectElement)) {
            LogUtil.w("unSelectElement unSelect element not in container")
            return false
        }
        callListener(object : Consumer<ElementActionListener?> {
            override fun accept(t: ElementActionListener?) {
                t?.onUnSelect(selectElement)
            }
        })
        selectElement?.unSelect()
        selectElement = null
        return true
    }

    /**
     * 选中之后再次点击选中的元素
     */
    private fun selectedClick() {
        if (selectElement == null) {
            LogUtil.w("selectedClick selected element is null")
            return
        }
        if (selectElement!!.isShowingViewResponseSelectedClick) {
            val left: Int = selectElement!!.mElementShowingView?.left ?: 0
            val top: Int = selectElement!!.mElementShowingView?.top ?: 0
            mUpDownMotionEvent[0]?.setLocation(
                mUpDownMotionEvent[0]!!.x - left,
                mUpDownMotionEvent[0]!!.y - top
            )
            rotateMotionEvent(mUpDownMotionEvent[0], selectElement!!)
            mUpDownMotionEvent[1]?.setLocation(
                mUpDownMotionEvent[1]!!.x - left,
                mUpDownMotionEvent[1]!!.y - top
            )
            rotateMotionEvent(mUpDownMotionEvent[1], selectElement!!)
            selectElement!!.mElementShowingView?.dispatchTouchEvent(mUpDownMotionEvent[0])
            selectElement!!.mElementShowingView?.dispatchTouchEvent(mUpDownMotionEvent[1])
        } else {
            LogUtil.d("不响应事件")
        }
        callListener(object : Consumer<ElementActionListener?> {
            override fun accept(t: ElementActionListener?) {
                t?.onSelectedClick(selectElement)
            }
        })
    }

    /**
     * 将 @event 旋转 @element 中的角度
     * [event] 手势事件
     * [element] 要旋转的view
     */
    private fun rotateMotionEvent(event: MotionEvent?, element: BaseElement) {
        if (element.mRotate != 0f) {
            val mInvertMatrix = Matrix()
            mInvertMatrix.postRotate(
                -element.mRotate, (
                        element.mElementShowingView?.width ?: 0f / 2).toFloat(), (
                        element.mElementShowingView?.height ?: 0f / 2).toFloat()
            )
            val point = floatArrayOf(event?.x ?: 0f, event?.y ?: 0f)
            mInvertMatrix.mapPoints(point)
            event?.setLocation(point[0], point[1])
        }
    }

    /**
     * 一定的时间之后自动取消当前元素的选中
     */
    private fun autoUnSelectDecoration() {
        if (mIsNeedAutoUnSelect) {
            cancelAutoUnSelectDecoration()
            postDelayed(mUnSelectRunnable, mAutoUnSelectDuration)
        }
    }

    /**
     * 取消自动取消选中
     */
    private fun cancelAutoUnSelectDecoration() {
        removeCallbacks(mUnSelectRunnable)
    }

    /**
     * 设置是否需要自动取消选中
     * [needAutoUnSelect] 是否需要自动取消选中
     */
    fun setNeedAutoUnSelect(needAutoUnSelect: Boolean) {
        mIsNeedAutoUnSelect = needAutoUnSelect
    }

    protected fun callListener(decorationActionListenerConsumer: Consumer<ElementActionListener?>) {
        for (elementActionListener in mElementActionListenerSet) {
            try {
                decorationActionListenerConsumer.accept(elementActionListener)
            } catch (e: Exception) {
                LogUtil.e(e.toString())
            }
        }
    }

    interface ElementActionListener {
        /**
         * 增加了一个元素之后的回调
         * [element] 添加的元素
         */
        fun onAdd(element: BaseElement?)

        /**
         * 删除了一个元素之后的回调
         * [element] 删除的元素
         */
        fun onDelete(element: BaseElement?)

        /**
         * 选中了一个元素之后再次点击该元素触发的事件
         * [element] 选中的元素
         */
        fun onSelectedClick(element: BaseElement?)

        /**
         * 选中了元素之后，对元素单指移动开始的回调
         * [element] 选中的元素
         */
        fun onSingleFingerMoveStart(element: BaseElement?)

        /**
         * 选中了元素之后，对元素单指移动过程的回调
         * [element] 选中的元素
         */
        fun onSingleFingerMoveProcess(element: BaseElement?)

        /**
         * 一次单指移动操作结束的回调
         * [element] 选中的元素
         */
        fun onSingleFingerMoveEnd(element: BaseElement?)

        /**
         * 选中了元素之后，对元素双指旋转缩放开始的回调
         * [element] 选中的元素
         */
        fun onDoubleFingerScaleAndRotateStart(element: BaseElement?)

        /**
         * 选中了元素之后，对元素双指旋转缩放过程的回调
         * [element] 选中的元素
         */
        fun onDoubleFingerScaleAndRotateProcess(element: BaseElement?)

        /**
         * 一次 双指旋转、缩放 操作结束的回调
         * [element] 选中的元素
         */
        fun onDoubleFingerScaleRotateEnd(element: BaseElement?)

        /**
         * 选中元素
         * [element] 选中的元素
         */
        fun onSelect(element: BaseElement?)

        /**
         * 取消选中元素
         * [element] 取消选中的元素
         */
        fun onUnSelect(element: BaseElement?)

        /**
         * 点击空白区域
         * [element] 选中的元素
         */
        fun onSingleTapBlankScreen(element: BaseElement?)
    }

    interface Consumer<T> {
        fun accept(t: T)
    }

    enum class BaseActionMode {
        MOVE, SELECT, SELECTED_CLICK_OR_MOVE, SINGLE_TAP_BLANK_SCREEN, DOUBLE_FINGER_SCALE_AND_ROTATE
    }

    init {
        init()
    }
}