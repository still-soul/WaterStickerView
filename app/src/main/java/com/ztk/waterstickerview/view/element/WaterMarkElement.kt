package com.ztk.waterstickerview.view.element

import android.content.Context
import android.view.View
import com.ztk.waterstickerview.bean.WaterMarkViewBean
import com.ztk.waterstickerview.utils.WaterMarkStickerProviderFactory
import com.ztk.waterstickerview.view.watermark.BaseWaterMarkView


/**
 * 智能水印View
 * @author xiaoman
 */
class WaterMarkElement(
    private val context: Context, val waterMarkViewBean: WaterMarkViewBean
) :
    DecorationElement(context, WaterMarkStickerProviderFactory.TYPE_WATER_MARK) {
    var waterMarkView: BaseWaterMarkView? = null

    override fun initView(): View {
        waterMarkView = WaterMarkStickerProviderFactory.getWaterView(
            context,
            waterMarkViewBean
        )
        return waterMarkView!!
    }

    /**
     * 更新水印高度
     */
    fun updateDecorationViewHeight() {
        waterMarkView?.post {
            mOriginHeight = waterMarkView!!.viewContent.measuredHeight.toFloat()
            mShowingViewParams.width = waterMarkView!!.viewContent.measuredWidth
            mShowingViewParams.height = mOriginHeight.toInt()
            //更新水印位置以及选中框的位置和高度
            update()
        }
    }
}