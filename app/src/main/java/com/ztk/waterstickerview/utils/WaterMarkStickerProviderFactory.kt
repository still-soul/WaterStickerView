package com.ztk.waterstickerview.utils

import android.content.Context
import com.ztk.waterstickerview.R
import com.ztk.waterstickerview.bean.*
import com.ztk.waterstickerview.view.watermark.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 水印贴纸数据工具类
 * 如果需要新加一种水印类型：
 * [getWaterMarkList]中新增本地水印数据
 * [getWaterView]中新增水印view
 * [getWaterMarkEditList]中组装编辑弹窗所需的数据
 * @author xiaoman
 */
object WaterMarkStickerProviderFactory {

    /**
     * 智能水印类型
     */
    const val TYPE_WATER_MARK = "type_water_mark"

    /**
     * 激萌贴纸类型
     */
    const val TYPE_CUTE_STICKER = "type_cute_sticker"



    /**
     * @return 根据水印类型获取智能水印view
     * [context] 上下文
     * [waterMarkViewBean] 水印编辑数据
     */
    fun getWaterView(
        context: Context,
        waterMarkViewBean: WaterMarkViewBean
    ): BaseWaterMarkView {
        return when (waterMarkViewBean.waterMarkType) {
            //宝宝成长记录水印
            WaterMarkType.BABY_GROWTH_RECORD_TYPE ->
                BabyGrowthRecordWaterView(context, waterMarkViewBean = waterMarkViewBean)
            else -> {
                LogUtil.e("waterMarkType = ${waterMarkViewBean.waterMarkType}")
                //找不到水印类型，默认宝宝成长记录水印
                BabyGrowthRecordWaterView(context, waterMarkViewBean = waterMarkViewBean)
            }
        }
    }

    /**
     * 获取水印view显示的数据
     * [waterMarkType] 水印类型
     */
    suspend fun getWaterMarkViewData(
        waterMarkType: WaterMarkType
    ): WaterMarkViewBean = withContext(Dispatchers.Default) {
        return@withContext WaterMarkViewBean(

            waterMarkType = waterMarkType
        )
    }


    /**
     * @return 水印编辑弹窗的数据
     */
//    suspend fun getWaterMarkEditList(
//        context: Context,
//        waterMarkType: WaterMarkType,
//        waterMarkViewBean: WaterMarkViewBean
//    ): MutableList<WaterMarkEditBean> = withContext(Dispatchers.Default){
//        val waterMarkEditList = mutableListOf<WaterMarkEditBean>()
//        when (waterMarkType) {
//            //宝宝成长记录水印
//            WaterMarkType.BABY_GROWTH_RECORD_TYPE -> {
//                addEditName(context,waterMarkViewBean, waterMarkEditList)
//                addEditDate(context,waterMarkViewBean, waterMarkEditList)
//                addEditSex(context,waterMarkViewBean, waterMarkEditList)
//                addEditAge(context,waterMarkViewBean, waterMarkEditList)
//                addEditHeight(context,waterMarkViewBean, waterMarkEditList)
//                addEditWeight(context,waterMarkViewBean, waterMarkEditList)
//                addEditLocation(context,waterMarkViewBean, waterMarkEditList)
//                addEditDiary(context,waterMarkViewBean, waterMarkEditList)
//            }
//            else -> {
//                LogUtil.e("waterMarkType = ${waterMarkViewBean.waterMarkType}")
//            }
//        }
//
//        return@withContext waterMarkEditList
//
//    }
//
//    /**
//     * 添加宝宝成长纪念水印数据
//     */
//    private fun addWaterMarkGrowthData(
//        context: Context,
//        waterMarkViewBean: WaterMarkViewBean,
//        waterMarkEditList: MutableList<WaterMarkEditBean>
//    ) {
//        addEditName(context,waterMarkViewBean, waterMarkEditList)
//        addEditDate(context,waterMarkViewBean, waterMarkEditList)
//        addEditAge(context,waterMarkViewBean, waterMarkEditList)
//        addEditSex(context,waterMarkViewBean, waterMarkEditList)
//        addEditHeight(context,waterMarkViewBean, waterMarkEditList)
//        addEditWeight(context,waterMarkViewBean, waterMarkEditList)
//        addEditLocation(context,waterMarkViewBean, waterMarkEditList)
//        addEditDiary(context,waterMarkViewBean, waterMarkEditList)
//    }
//
//    /**
//     * 添加日期地址水印数据
//     */
//    private fun addPlaceItem(
//        context: Context,
//        waterMarkViewBean: WaterMarkViewBean,
//        waterMarkEditList: MutableList<WaterMarkEditBean>
//    ) {
//        addEditName(context,waterMarkViewBean, waterMarkEditList)
//        addEditDate(context,waterMarkViewBean, waterMarkEditList)
//        addEditLocation(context,waterMarkViewBean, waterMarkEditList)
//        addEditDiary(context,waterMarkViewBean, waterMarkEditList)
//    }
//
//    /**
//     * 添加编辑弹窗的【宝宝姓名】item
//     */
//    private fun addEditName(
//        context: Context,
//        waterMarkViewBean: WaterMarkViewBean,
//        waterMarkEditList: MutableList<WaterMarkEditBean>
//    ) {
//        val waterMarkEditName = WaterMarkEditBean(
//            titleString = context.getString(R.string.string_name),
//            contentString = waterMarkViewBean.name,
//            type = WaterMarkEditType.NAME,
//            switchCan = false
//        )
//        waterMarkEditList.add(waterMarkEditName)
//    }
//
//    /**
//     * 添加编辑弹窗的【照片日期】item
//     */
//    private fun addEditDate(
//        context: Context,
//        waterMarkViewBean: WaterMarkViewBean,
//        waterMarkEditList: MutableList<WaterMarkEditBean>
//    ) {
//        val waterMarkEditDate = WaterMarkEditBean(
//            titleString = context.getString(R.string.string_date),
//            contentString = waterMarkViewBean.date,
//            type = WaterMarkEditType.DATE,
//            switchCan = false
//        )
//        waterMarkEditList.add(waterMarkEditDate)
//    }
//
//    /**
//     * 添加编辑弹窗的【宝宝年龄】item
//     */
//    private fun addEditAge(
//        context: Context,
//        waterMarkViewBean: WaterMarkViewBean,
//        waterMarkEditList: MutableList<WaterMarkEditBean>
//    ) {
//        val waterMarkEditAge = WaterMarkEditBean(
//            titleString = context.getString(R.string.string_age),
//            contentString = waterMarkViewBean.age,
//            type = WaterMarkEditType.AGE,
//            switchStyle = waterMarkViewBean.ageSwitch
//        )
//        waterMarkEditList.add(waterMarkEditAge)
//    }
//
//
//    /**
//     * 添加编辑弹窗的【性别】item
//     */
//    private fun addEditSex(
//        context: Context,
//        waterMarkViewBean: WaterMarkViewBean,
//        waterMarkEditList: MutableList<WaterMarkEditBean>
//    ) {
//        val waterMarkEditAge = WaterMarkEditBean(
//            titleString = context.getString(R.string.string_sex),
//            contentString = waterMarkViewBean.sex,
//            type = WaterMarkEditType.SEX,
//            switchStyle = waterMarkViewBean.sexSwitch
//        )
//        waterMarkEditList.add(waterMarkEditAge)
//    }
//
//    /**
//     * 添加编辑弹窗的【身高】item
//     */
//    private fun addEditHeight(
//        context: Context,
//        waterMarkViewBean: WaterMarkViewBean,
//        waterMarkEditList: MutableList<WaterMarkEditBean>
//    ) {
//        val waterMarkEditHeight = WaterMarkEditBean(
//            titleString = context.getString(R.string.string_height),
//            contentString = waterMarkViewBean.height,
//            type = WaterMarkEditType.HEIGHT,
//            switchStyle = waterMarkViewBean.heightSwitch
//        )
//        waterMarkEditList.add(waterMarkEditHeight)
//    }
//
//    /**
//     * 添加编辑弹窗的【体重】item
//     */
//    private fun addEditWeight(
//        context: Context,
//        waterMarkViewBean: WaterMarkViewBean,
//        waterMarkEditList: MutableList<WaterMarkEditBean>
//    ) {
//        val waterMarkEditWeight = WaterMarkEditBean(
//            titleString = context.getString(R.string.string_weight),
//            contentString = waterMarkViewBean.weight,
//            type = WaterMarkEditType.WEIGHT,
//            switchStyle = waterMarkViewBean.weightSwitch
//        )
//        waterMarkEditList.add(waterMarkEditWeight)
//    }
//
//    /**
//     * 添加编辑弹窗的【照片地点】item
//     */
//    private fun addEditLocation(
//        context: Context,
//        waterMarkViewBean: WaterMarkViewBean,
//        waterMarkEditList: MutableList<WaterMarkEditBean>
//    ) {
//        val city = waterMarkViewBean.city
//        val citySwitch = if (city.isNullOrEmpty()) {
//            false
//        } else {
//            waterMarkViewBean.citySwitch
//        }
//        val waterMarkEditLocation = WaterMarkEditBean(
//            titleString = context.getString(R.string.string_place),
//            contentString = city,
//            type = WaterMarkEditType.LOCATION,
//            switchStyle = citySwitch
//        )
//        waterMarkEditList.add(waterMarkEditLocation)
//    }
//
//    /**
//     * 添加编辑弹窗的【成长日记】item
//     */
//    private fun addEditDiary(
//        context: Context,
//        waterMarkViewBean: WaterMarkViewBean,
//        waterMarkEditList: MutableList<WaterMarkEditBean>
//    ) {
//        val waterMarkEditDiary = WaterMarkEditBean(
//            titleString = context.getString(R.string.string_diary),
//            contentString = waterMarkViewBean.content,
//            type = WaterMarkEditType.DIARY,
//            switchStyle = waterMarkViewBean.contentSwitch
//        )
//        waterMarkEditList.add(waterMarkEditDiary)
//    }


}