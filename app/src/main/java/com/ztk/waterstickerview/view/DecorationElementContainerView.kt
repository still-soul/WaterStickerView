package com.ztk.waterstickerview.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.ztk.waterstickerview.view.element.BaseElement
import com.ztk.waterstickerview.view.element.DecorationElement
import com.ztk.waterstickerview.utils.LogUtil


/**
 * 存放element的容器
 * @author xiaoman
 */
open class DecorationElementContainerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ElementContainerView(context, attrs, defStyleAttr) {
    private var mDecorationActionMode: DecorationActionMode? = null
    private var mIsRunOnFlingAnimation = true
    private var onEditClickCallBack : OnEditClickCallBack?=null

    /**
     * 取消选中、删除
     */
    private fun unSelectDeleteAndUpdateTopElement() {
        unSelectElement()
        deleteElement()
    }

    /**
     * 添加、选中、更新
     * [baseElement] 要添加的view
     * [showEdit] 是否需要显示编辑图标 默认不显示
     */
    fun addSelectAndUpdateElement(
        baseElement: BaseElement?, showEdit: Boolean = false
    ) {
        addElement(baseElement, showEdit)
        selectElement(baseElement)
        selectElement?.mElementShowingView?.post {
            update()
        }
    }

    /**
     * 设置编辑的监听事件
     */
    fun setOnEditClickCallBack(onEditClickCallBack: OnEditClickCallBack){
        this.onEditClickCallBack = onEditClickCallBack
    }
    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (!mIsRunOnFlingAnimation) {
            return false
        }
        if (selectElement !is DecorationElement) return false
        if (findElementByPosition(e2?.x ?: 0f, e2?.y ?: 0f) !is DecorationElement?) return false
        return true
    }

    override fun downSelectTapOtherAction(event: MotionEvent): Boolean {
        mDecorationActionMode = DecorationActionMode.NONE
        val x = event.x
        val y = event.y
        val selectedDecorationElement = selectElement as DecorationElement
        if (selectedDecorationElement.isInScaleAndRotateButton(x, y)) {
            // 开始进行单指旋转缩放
            mDecorationActionMode = DecorationActionMode.SINGER_FINGER_SCALE_AND_ROTATE
            selectedDecorationElement.onSingleFingerScaleAndRotateStart()
            callListener(object : Consumer<ElementActionListener?> {
                override fun accept(t: ElementActionListener?) {
                    (t as? DecorationElementActionListener)?.onSingleFingerScaleAndRotateStart(
                        selectedDecorationElement
                    )
                }
            })
            LogUtil.d("downSelectTapOtherAction selected scale and rotate")
            return true
        }
        if (selectedDecorationElement.isInRemoveButton(x, y)) {
            mDecorationActionMode = DecorationActionMode.CLICK_BUTTON_DELETE
            LogUtil.d("downSelectTapOtherAction selected delete")
            return true
        }
        if (selectedDecorationElement.isInEditButton(x, y)) {
            mDecorationActionMode = DecorationActionMode.CLICK_BUTTON_EDIT
            LogUtil.d("downSelectTapOtherAction selected edit")
            return true
        }
        return false
    }

    override fun scrollSelectTapOtherAction(event: MotionEvent?, distance: FloatArray): Boolean {
        if (selectElement == null) {
            LogUtil.d("detectorSingleFingerRotateAndScale scale and rotate but not select")
            return false
        }
        if (mDecorationActionMode == DecorationActionMode.CLICK_BUTTON_DELETE) {
            //删除
            return true
        }
        if (mDecorationActionMode == DecorationActionMode.CLICK_BUTTON_EDIT) {
            //编辑
            return true
        }
        if (mDecorationActionMode == DecorationActionMode.SINGER_FINGER_SCALE_AND_ROTATE) {
            val selectedDecorationElement = selectElement as DecorationElement
            selectedDecorationElement.onSingleFingerScaleAndRotateProcess(
                event?.x ?: 0f,
                event?.y ?: 0f
            )
            update()
            // 在单指旋转缩放过程中
            callListener(object : Consumer<ElementActionListener?> {
                override fun accept(t: ElementActionListener?) {
                    (t as? DecorationElementActionListener)?.onSingleFingerScaleAndRotateProcess(
                        selectedDecorationElement
                    )
                }

            })
            LogUtil.d(
                "scrollSelectTapOtherAction scale and rotate |||||||||| distanceX:" + distance[0]
                        + "distanceY:" + distance[1] + "x:" + event?.x + "y:" + event?.y
            )
            return true
        }
        return false
    }

    override fun upSelectTapOtherAction(event: MotionEvent): Boolean {
        if (selectElement == null) {
            LogUtil.w("upSelectTapOtherAction delete but not select ")
            return false
        }
        val selectedDecorationElement = selectElement as DecorationElement
        if (mDecorationActionMode == DecorationActionMode.CLICK_BUTTON_DELETE
            && selectedDecorationElement.isInRemoveButton(event.x, event.y)
        ) {
            //删除
            unSelectDeleteAndUpdateTopElement()
            mDecorationActionMode = DecorationActionMode.NONE
            LogUtil.d("upSelectTapOtherAction delete")
            return true
        }
        if (mDecorationActionMode == DecorationActionMode.CLICK_BUTTON_EDIT
            && selectedDecorationElement.isInEditButton(event.x, event.y)
        ) {
            //编辑
            val showEdit = selectedDecorationElement.showEdit
            if (showEdit){
                onEditClickCallBack?.onEditClick()
            }
            mDecorationActionMode = DecorationActionMode.NONE
            LogUtil.d("upSelectTapOtherAction delete")
            return true
        }
        if (mDecorationActionMode == DecorationActionMode.SINGER_FINGER_SCALE_AND_ROTATE) {
            //缩放和旋转
            selectedDecorationElement.onSingleFingerScaleAndRotateEnd()
            callListener(object : Consumer<ElementActionListener?> {
                override fun accept(t: ElementActionListener?) {
                    (t as? DecorationElementActionListener)?.onSingleFingerScaleAndRotateEnd(
                        selectedDecorationElement
                    )
                }

            })
            mDecorationActionMode = DecorationActionMode.NONE
            LogUtil.d("upSelectTapOtherAction scale and rotate end")
            return true
        }
        return false
    }

    interface DecorationElementActionListener : ElementActionListener {
        /**
         * 选中了元素之后，对元素单指缩放旋转开始的回调
         *
         * @param element
         */
        fun onSingleFingerScaleAndRotateStart(element: DecorationElement?)

        /**
         * 选中了元素之后，对元素单指缩放旋转过程的回调
         *
         * @param element
         */
        fun onSingleFingerScaleAndRotateProcess(element: DecorationElement?)

        /**
         * 一次单指 缩放旋转 结束
         *
         * @param element
         */
        fun onSingleFingerScaleAndRotateEnd(element: DecorationElement?)
    }

    /**
     * 事件类型
     */
    enum class DecorationActionMode {
        /**
         * 重置操作
         */
        NONE,

        /**
         * 旋转和缩放
         */
        SINGER_FINGER_SCALE_AND_ROTATE,

        /**
         * 删除
         */
        CLICK_BUTTON_DELETE,

        /**
         * 编辑
         */
        CLICK_BUTTON_EDIT
    }

    interface OnEditClickCallBack {
        /**
         * 点击编辑按钮
         */
        fun onEditClick()
    }

}