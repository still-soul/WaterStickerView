package com.ztk.waterstickerview.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 智能水印bean
 * @author xiaoman
 */
@Parcelize
data class WaterMarkBean(
    val drawable: Int,
    val smallDrawable: Int,
    val type: WaterMarkType
) : Parcelable

/**
 * 水印类型
 */
enum class WaterMarkType {

    /**
     * 宝宝成长记录水印
     */
    BABY_GROWTH_RECORD_TYPE,

    /**
     * 空
     */
    NONE
}