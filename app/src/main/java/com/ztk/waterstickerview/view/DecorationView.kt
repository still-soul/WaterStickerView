package com.ztk.waterstickerview.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.ztk.waterstickerview.view.element.DecorationElement
import com.ztk.waterstickerview.R
import com.ztk.waterstickerview.utils.AppUtil


/**
 * 绘制四周的边框和装饰的 view
 * @author xiaoman
 */
open class DecorationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mDecorationElement: DecorationElement? = null

    /**
     * 是否显示编辑按钮，默认不显示
     */
    private var showEdit = false

    companion object {
        private const val DECORATION_OUT_BOX_LINE_WIDTH = 2

        /**
         * 文字/贴纸移除按钮
         */
        private var sRemoveButtonBitmap: Bitmap? = null

        /**
         * 文字/贴纸旋转缩放按钮
         */
        private var sScaleAndRotateButtonBitmap : Bitmap? = null

        private var sEditBitmap : Bitmap? = null

        private val sLinePaint = Paint()
        private var sIsInit = false

        @JvmStatic
        fun initDecorationView(resources: Resources?, context: Context?) {
            if (resources == null || context == null || sIsInit) {
                return
            }
            //删除按钮
            sRemoveButtonBitmap = BitmapFactory.decodeResource(
                resources, R.mipmap.icon_sticker_delete
            )

            //旋转缩放按钮
            sScaleAndRotateButtonBitmap = BitmapFactory.decodeResource(
                resources, R.mipmap.icon_sticker_scale
            )
            //编辑按钮
            sEditBitmap =  BitmapFactory.decodeResource(
                resources, R.mipmap.icon_sticker_edit
            )

            sLinePaint.color = -0x1
            sLinePaint.style = Paint.Style.STROKE
            sLinePaint.isAntiAlias = true
            sLinePaint.strokeWidth = DECORATION_OUT_BOX_LINE_WIDTH.toFloat()
            sIsInit = true
            DecorationElement.ELEMENT_SCALE_ROTATE_ICON_WIDTH = sRemoveButtonBitmap?.width ?: AppUtil.dip2px(context,24f)
        }
    }

    init {
        if (!sIsInit) {
            throw RuntimeException("need call initDecorationView")
        }
    }

    fun setDecorationElement(decorationElement: DecorationElement?, showEdit : Boolean) {
        mDecorationElement = decorationElement
        this.showEdit = showEdit
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mDecorationElement == null) {
            return
        }
        canvas.save()

        val bitmapWidth = sRemoveButtonBitmap?.width ?: AppUtil.dip2px(context,24f)
        val bitmapHeight = sRemoveButtonBitmap?.height ?: AppUtil.dip2px(context,24f)
        // 绘制内容外面的框，只有处于选中态的时候才绘制
        val outBoxRect = Rect(
            bitmapWidth ,
            bitmapHeight ,
            width -  bitmapWidth,
            height - bitmapHeight
        )
        canvas.drawRect(outBoxRect, sLinePaint)
        canvas.drawFilter = PaintFlagsDrawFilter(
            0,
            Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG
        )

        sRemoveButtonBitmap?.let {
            // 绘制删除按钮
            canvas.drawBitmap(
                it,
                Rect(
                    0, 0, bitmapWidth,
                    bitmapHeight
                ),
                Rect(
                    bitmapWidth/2,
                    bitmapHeight/2,
                    bitmapWidth + bitmapWidth/2,
                    bitmapHeight + bitmapHeight/2
                ), sLinePaint
            )
        }

        sScaleAndRotateButtonBitmap?.let {
            // 绘制旋转缩放按钮
            canvas.drawBitmap(
                it,
                Rect(
                    0, 0, it.width,
                    it.height
                ),
                Rect(
                    width - it.width - it.width/2,
                    height - it.height - it.height/2,
                    width - it.width/2,
                    height - it.height/2
                ), sLinePaint
            )
        }

        if (showEdit && sEditBitmap != null){
            //绘制编辑按钮
            canvas.drawBitmap(
                sEditBitmap!!,
                Rect(
                    0, 0, sEditBitmap!!.width,
                    sEditBitmap!!.height
                ),
                Rect(
                    width - sEditBitmap!!.width - sEditBitmap!!.width/2,
                    sEditBitmap!!.height/2,
                    width  - sEditBitmap!!.width/2,
                    sEditBitmap!!.height + bitmapHeight/2
                ), sLinePaint
            )

        }
        canvas.restore()
    }


}