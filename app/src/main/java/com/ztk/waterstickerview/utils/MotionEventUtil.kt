package com.ztk.waterstickerview.utils

import android.view.MotionEvent
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * 手势事件工具类
 * @author xioaman
 */
object MotionEventUtil {
    @JvmStatic
    fun copyMotionEvent(motionEvent: MotionEvent?): MotionEvent? {
        val c: Class<*> = MotionEvent::class.java
        var motionEventMethod: Method? = null
        try {
            motionEventMethod = c.getMethod("copy")
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
        var copyMotionEvent: MotionEvent? = null
        try {
            copyMotionEvent = motionEventMethod!!.invoke(motionEvent) as MotionEvent
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return copyMotionEvent
    }
}