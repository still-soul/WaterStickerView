package com.ztk.waterstickerview

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.transition.Transition
import com.ztk.waterstickerview.bean.WaterMarkType
import com.ztk.waterstickerview.bean.WaterMarkViewBean
import com.ztk.waterstickerview.consts.WaterMarkConstant
import com.ztk.waterstickerview.utils.MyCustomTarget
import com.ztk.waterstickerview.utils.StickerUtil
import com.ztk.waterstickerview.utils.WaterMarkStickerProviderFactory
import com.ztk.waterstickerview.view.DecorationElementContainerView
import com.ztk.waterstickerview.view.element.ImageViewElement
import com.ztk.waterstickerview.view.element.WaterMarkElement
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {


    private lateinit var waterMarkStickerElementView: DecorationElementContainerView
    private var stickerType = WaterMarkConstant.TYPE_WATER_MARK

    private lateinit var waterMarkViewBean: WaterMarkViewBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initWaterMarkStickerView()

        findViewById<Button>(R.id.btWater).setOnClickListener {
            stickerType = WaterMarkConstant.TYPE_WATER_MARK
            showElementView()
        }

        findViewById<Button>(R.id.btSticker).setOnClickListener {
            stickerType = WaterMarkConstant.TYPE_CUTE_STICKER
            showElementView()
        }
    }

    private fun initWaterMarkStickerView() {
        StickerUtil.initialize(this)
        waterMarkStickerElementView = findViewById(R.id.elementView)
        waterMarkStickerElementView.setNeedAutoUnSelect(false)
    }

    private fun showElementView() {
        waterMarkStickerElementView.post {
            if (WaterMarkConstant.TYPE_WATER_MARK == stickerType) {
                //智能水印
                showWaterMarkView(WaterMarkType.BABY_GROWTH_RECORD_TYPE)
            } else {
                //激萌贴纸
                showStickerView("https://qimg.cdnmama.com/record/2021/11/02/1a4bfa8f67ec46f0b234eb88249016d9.png")
            }
        }
    }

    /**
     * 显示智能水印view
     */
    private fun showWaterMarkView(waterMarkType: WaterMarkType) {

        lifecycleScope.launch {
            waterMarkViewBean = WaterMarkStickerProviderFactory.getWaterMarkViewData(
                    waterMarkType = waterMarkType
            )
            var waterMarkElement =
                    WaterMarkElement(this@MainActivity, waterMarkViewBean)
            waterMarkStickerElementView.unSelectElement()
            waterMarkStickerElementView.setOnEditClickCallBack(object :
                    DecorationElementContainerView.OnEditClickCallBack {
                override fun onEditClick() {
                    waterMarkElement = waterMarkStickerElementView.selectElement as WaterMarkElement
                    //这里做编辑逻辑
                }
            })
            waterMarkStickerElementView.addSelectAndUpdateElement(waterMarkElement, true)
        }

    }

    /**
     * 显示贴纸view
     */
    private fun showStickerView(imageUrl: String) {
        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(object : MyCustomTarget<Bitmap?>() {

                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                        super.onResourceReady(resource, transition)
                        val imageViewElement = ImageViewElement(
                                this@MainActivity,
                                resource,
                                resource.width.toFloat(),
                                resource.height.toFloat()
                        )
                        waterMarkStickerElementView.unSelectElement()
                        waterMarkStickerElementView.addSelectAndUpdateElement(imageViewElement)
                    }

                })

    }
}