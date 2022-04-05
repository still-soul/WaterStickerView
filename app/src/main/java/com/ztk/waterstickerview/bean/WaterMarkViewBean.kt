package com.ztk.waterstickerview.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 水印编辑数据bean
 * @author zhaotk
 */
@Parcelize
data class WaterMarkViewBean(
    /**
     * 当前宝宝bId
     */
    val bid: String? = null,
    /**
     * 身高数据
     */
    var height: String? = null,

    /**
     * 是否开启身高选项
     */
    var heightSwitch: Boolean = true,

    /**
     * 体重数据
     */
    var weight: String? = null,

    /**
     * 是否开启体重选项
     */
    var weightSwitch: Boolean = true,

    /**
     * 地址 (城市+地址)
     */
    var city: String? = null,

    /**
     * 只是地址
     */
    var address: String? = null,

    /**
     * 是否开启地址选项
     */
    var citySwitch: Boolean = true,

    /**
     * 照片日期
     */
    var date: String? = null,

    /**
     * 是否开启日期选项
     */
    val dateSwitch: Boolean = true,

    /**
     * 星期几
     */
    var week : String? = null,

    /**
     * 年龄
     */
    var age: String? = null,

    /**
     * 是否开启宝宝年龄选项
     */
    var ageSwitch: Boolean = true,

    /**
     * 宝宝性别
     */
    var sex: String? = null,

    /**
     * 是否开启性别选项
     */
    var sexSwitch: Boolean = true,

    /**
     * 宝宝名称
     */
    var name: String? = null,

    /**
     * 是否开启名称选项
     */
    val nameSwitch: Boolean = true,

    /**
     * 记录内容
     */
    var content: String? = null,

    /**
     * 是否开启内容选项
     */
    var contentSwitch: Boolean = true,

    /**
     * 水印类型
     */
    val waterMarkType: WaterMarkType,

    ) : Parcelable