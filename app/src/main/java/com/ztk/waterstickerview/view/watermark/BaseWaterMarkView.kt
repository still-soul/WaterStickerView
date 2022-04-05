package com.ztk.waterstickerview.view.watermark

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.ztk.waterstickerview.bean.WaterMarkViewBean

/**
 * 智能贴纸view的基类
 * @author xiaoman
 */
abstract class BaseWaterMarkView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    val waterMarkViewBean: WaterMarkViewBean
) : FrameLayout(context, attrs, defStyleAttr) {

    /**
     * 水印内容view
     */
    lateinit var viewContent: View

    /**
     * 姓名最大长度
     */
    private val maxNameLength = 5

    /**
     * 位置最大长度
     */
    private val maxLocationLength = 10

    init {
        setLayout()
    }

    private fun setLayout() {
        LayoutInflater.from(context).inflate(getLayoutId(), this, true)
        initView()
        viewContent = findViewById(getContentViewId())
    }

    /**
     * 设置身高体重数据
     * [haveSuffix] 是否有前缀，默认有
     * [visibilityHeightView] 控制显示隐藏的身高view，默认tvHeight
     * [visibilityWeightView] 控制显示隐藏的体重view，默认tvWeight
     */
    fun setHeightAndWeightData(
        tvHeight: TextView,
        tvWeight: TextView,
        waterMarkViewBean: WaterMarkViewBean,
        haveSuffix: Boolean = true,
        visibilityHeightView: View? = null,
        visibilityWeightView: View? = null,
    ) {
        if (waterMarkViewBean.heightSwitch) {
            val height = waterMarkViewBean.height
            tvHeight.text = if (haveSuffix) {
                "身高：".plus(height).plus("cm")
            } else {
                height.plus("cm")
            }
            if (visibilityHeightView == null){
                tvHeight.visibility = VISIBLE
            }else{
                visibilityHeightView.visibility = VISIBLE
            }
        } else {
            if (visibilityHeightView == null){
                tvHeight.visibility = GONE
            }else{
                visibilityHeightView.visibility = GONE
            }
        }

        if (waterMarkViewBean.weightSwitch) {
            val weight = waterMarkViewBean.weight
            tvWeight.text = if (haveSuffix) {
                "体重：".plus(weight).plus("kg")
            } else {
                weight.plus("kg")
            }
            if (visibilityWeightView == null){
                tvWeight.visibility = VISIBLE
            }else{
                visibilityWeightView.visibility = VISIBLE
            }
        } else {
            if (visibilityWeightView == null){
                tvWeight.visibility = GONE
            }else{
                visibilityWeightView.visibility = GONE
            }
        }
    }

    /**
     * 设置宝宝姓名
     * [dynamicMaxEms] 是否根据年龄动态设置长度 默认true
     */
    fun setBabyName(
        nameTextView: TextView,
        waterMarkViewBean: WaterMarkViewBean,
        dynamicMaxEms: Boolean = true
    ) {
        var name = waterMarkViewBean.name
        name?.let {
            if (it.length > maxNameLength) {
                if (dynamicMaxEms) {
                    if (waterMarkViewBean.ageSwitch && !waterMarkViewBean.age.isNullOrEmpty()) {
                        name = it.subSequence(0, maxNameLength).toString().plus("...")
                    }
                } else {
                    name = it.subSequence(0, maxNameLength).toString().plus("...")
                }
            }
        }
        nameTextView.text = name
    }

    /**
     * 设置宝宝年龄
     * [suffix]前缀内容
     */
    fun setBabyAge(
        ageTextView: TextView,
        waterMarkViewBean: WaterMarkViewBean,
        suffix: String = "·"
    ) {
        val age = waterMarkViewBean.age
        if (!age.isNullOrEmpty() && waterMarkViewBean.ageSwitch) {
            ageTextView.text = suffix.plus(age)
            ageTextView.visibility = VISIBLE
        } else {
            ageTextView.visibility = GONE
        }
    }


    /**
     * 设置默认图片日期
     * [haveSuffix] 是否有前缀
     */
    fun setDate(
        textView: TextView,
        waterMarkViewBean: WaterMarkViewBean,
        haveSuffix: Boolean = true
    ) {
        val dateString = if (haveSuffix) {
            waterMarkViewBean.date.plus(" ").plus(waterMarkViewBean.week)
        } else {
            waterMarkViewBean.date
        }
        textView.text = dateString
    }

    /**
     * 设置默认图片地址
     * [haveSuffix] 是否有前缀，默认无
     * [visibilityView] 控制显示隐藏的view，默认textView
     */
    fun setLocation(
        textView: TextView,
        waterMarkViewBean: WaterMarkViewBean,
        haveSuffix: Boolean = false,
        visibilityView: View? = null
    ) {
        var city = waterMarkViewBean.city
        if (!city.isNullOrEmpty() && waterMarkViewBean.citySwitch) {
            if (city.length > maxLocationLength) {
                city = city.subSequence(0, maxLocationLength).toString().plus("...")
            }
            textView.text = if (haveSuffix) {
                "地点：".plus(city)
            } else {
                city
            }
            if (visibilityView == null){
                textView.visibility = VISIBLE
            }else{
                visibilityView.visibility = VISIBLE
            }
        } else {
            if (visibilityView == null){
                textView.visibility = GONE
            }else{
                visibilityView.visibility = GONE
            }
        }
    }

    /**
     * 设置宝宝性别
     * [visibilityView] 控制显示隐藏的view，默认textView
     */
    fun setSex(textView: TextView,waterMarkViewBean: WaterMarkViewBean, visibilityView: View? = null) {
        if (waterMarkViewBean.sexSwitch) {
            textView.text = "性别：".plus(waterMarkViewBean.sex)
            if (visibilityView == null){
                textView.visibility = VISIBLE
            }else{
                visibilityView.visibility = VISIBLE
            }
        } else {
            if (visibilityView == null){
                textView.visibility = GONE
            }else{
                visibilityView.visibility = GONE
            }
        }
    }

    /**
     * 设置日常记录
     */
    fun setDiary(textView: TextView, waterMarkViewBean: WaterMarkViewBean) {
        val diary = waterMarkViewBean.content
        if (!diary.isNullOrEmpty() && waterMarkViewBean.contentSwitch) {
            textView.text = diary
            textView.visibility = VISIBLE
        } else {
            textView.visibility = GONE
        }
    }

    /**
     * 获取布局文件id
     */
    abstract fun getLayoutId(): Int

    /**
     * 初始化view
     */
    abstract fun initView()

    /**
     * 获取控制水印view高度的控件id
     */
    abstract fun getContentViewId(): Int

    /**
     * 更新水印数据
     */
    abstract fun updateView(waterMarkViewCompleteBean: WaterMarkViewBean)

}