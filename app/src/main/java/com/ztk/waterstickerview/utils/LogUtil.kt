package com.ztk.waterstickerview.utils

import android.text.TextUtils
import android.util.Log

/**
 * @author xiaoman
 */
object LogUtil {
    private const val TAG = "LogUtil"
    fun i(tag: String?, msg: String?) {

        if (!TextUtils.isEmpty(msg)) {
            Log.i(tag, msg!!)
        }
    }

    fun v(tag: String?, msg: String?) {
        if (!TextUtils.isEmpty(msg)) {
            Log.v(tag, msg!!)
        }

    }

    fun w(tag: String?, msg: String?) {
        if (!TextUtils.isEmpty(msg)) {
            Log.w(tag, msg!!)
        }
    }

    fun e(tag: String?, msg: String?) {
        if (!TextUtils.isEmpty(msg)) {
            Log.e(tag, msg!!)
        }
    }

    fun all(msg: String?) {
        if (!TextUtils.isEmpty(msg)) {
            Log.e("all", msg!!)
        }
    }

    fun i(msg: String?) {
        if (!TextUtils.isEmpty(msg)) {
            Log.i(TAG, msg!!)
        }
    }

    fun w(msg: String?) {
        if (!TextUtils.isEmpty(msg)) {
            Log.w(TAG, msg!!)
        }
    }

    fun e(msg: String?) {
        if (!TextUtils.isEmpty(msg)) {
            Log.e(TAG, msg!!)
        }
    }

    fun v(msg: String?) {
        e(msg)
    }

    fun d(msg: String?) {
        v(msg)
    }

    fun d(tag: String?, msg: String?) {
        if (!TextUtils.isEmpty(msg)) {
            Log.d(tag, msg!!)
        }
    }

}