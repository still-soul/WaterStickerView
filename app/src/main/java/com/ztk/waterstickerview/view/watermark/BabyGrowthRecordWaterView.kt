package com.ztk.waterstickerview.view.watermark

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.ztk.waterstickerview.R
import com.ztk.waterstickerview.bean.WaterMarkViewBean

/**
 * 宝宝成长记录水印
 * @author xiaoman
 */
class BabyGrowthRecordWaterView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    waterMarkViewBean: WaterMarkViewBean
) : BaseWaterMarkView(context, attrs, defStyleAttr, waterMarkViewBean) {
    private lateinit var tvDate: TextView
    private lateinit var tvBabyName: TextView
    private lateinit var tvBabyAge: TextView
    private lateinit var tvBabyHeight: TextView
    private lateinit var tvBabyWeight: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvDiary: TextView
    override fun getLayoutId(): Int {
        return R.layout.water_record_baby_growth
    }

    override fun getContentViewId(): Int {
        return R.id.llContent
    }

    override fun initView() {
        tvDate = findViewById(R.id.tvDate)
        tvBabyName = findViewById(R.id.tvBabyName)
        tvBabyAge = findViewById(R.id.tvBabyAge)
        tvBabyHeight = findViewById(R.id.tvBabyHeight)
        tvBabyWeight = findViewById(R.id.tvBabyWeight)
        tvLocation = findViewById(R.id.tvLocation)
        tvDiary = findViewById(R.id.tvDiary)

        setViewData(waterMarkViewBean)
    }

    private fun setViewData(waterMarkViewBean: WaterMarkViewBean) {
//        setBabyName(tvBabyName, waterMarkViewBean,false)
//        setDate(tvDate, waterMarkViewBean)
//        setBabyAge(tvBabyAge, waterMarkViewBean, "年龄：")
//        setLocation(tvLocation, waterMarkViewBean)
//        setHeightAndWeightData(tvBabyHeight, tvBabyWeight, waterMarkViewBean)
//        setDiary(tvDiary, waterMarkViewBean)
    }

    /**
     * 更新编辑完的数据
     */
    override fun updateView(waterMarkViewCompleteBean: WaterMarkViewBean) {
        setViewData(waterMarkViewCompleteBean)
    }



}
