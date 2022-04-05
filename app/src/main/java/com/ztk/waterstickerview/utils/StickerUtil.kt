package com.ztk.waterstickerview.utils

import android.content.Context
import com.ztk.waterstickerview.view.DecorationView.Companion.initDecorationView

/**
 * sticker framework 的工具类
 * @author zhaotk
 */
object StickerUtil {
    /**
     * sticker framework 的初始化入口，需要在使用前调用一次
     * [context] 上下文
     */
    fun initialize(context: Context) {
        initDecorationView(context.resources, context)
    }
}