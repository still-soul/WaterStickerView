package com.ztk.waterstickerview.view.element

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.ztk.waterstickerview.utils.WaterMarkStickerProviderFactory

/**
 * 激萌贴纸view
 * @author xiaoman
 */
class ImageViewElement(private val context: Context, private val imageUrl: Bitmap?, private val originWidth: Float, private val originHeight: Float) :
    DecorationElement(context,WaterMarkStickerProviderFactory.TYPE_CUTE_STICKER) {
    override fun setInitialSize() {
        mOriginWidth = originWidth
        mOriginHeight = originHeight
    }

    override fun initView(): View {
        val stickerImageView = AppCompatImageView(context)
        stickerImageView.setImageBitmap(imageUrl)
        return stickerImageView
    }
}