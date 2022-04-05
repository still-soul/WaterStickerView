package com.ztk.waterstickerview.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 水印编辑数据bean
 * @author xiaoman
 */
@Parcelize
data class WaterMarkEditBean(

    /**
     * 开关状态
     */
    var switchStyle: Boolean = true,

    /**
     * 标题
     */
    val titleString: String? = null,

    /**
     * 内容
     */
    var contentString: String? = null,

    /**
     * 是否可以开关
     */
    val switchCan: Boolean = true,

    /**
     * 水印编辑类型
     */
    val type: WaterMarkEditType

) : Parcelable

/**
 * 编辑类型
 */
enum class WaterMarkEditType {

    /**
     * 年龄
     */
    AGE,

    /**
     * 日期
     */
    DATE,

    /**
     * 姓名
     */
    NAME,

    /**
     * 性别
     */
    SEX,

    /**
     * 身高
     */
    HEIGHT,

    /**
     * 体重
     */
    WEIGHT,

    /**
     * 地点
     */
    LOCATION,

    /**
     * 成长记录
     */
    DIARY,

    /**
     * 编辑完成
     */
    COMPLETE
}
