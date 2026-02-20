package com.thanksplay.adesk.util

import android.util.Log

object ADeskLog {
    private const val TAG = "ADesk"
    private var isDebug = true
    
    fun init(debug: Boolean) {
        isDebug = debug
    }
    
    fun d(message: String) {
        if (isDebug) {
            Log.d(TAG, message)
        }
    }
    
    fun d(tag: String, message: String) {
        if (isDebug) {
            Log.d(tag, message)
        }
    }
    
    fun i(message: String) {
        if (isDebug) {
            Log.i(TAG, message)
        }
    }
    
    fun i(tag: String, message: String) {
        if (isDebug) {
            Log.i(tag, message)
        }
    }
    
    fun w(message: String) {
        if (isDebug) {
            Log.w(TAG, message)
        }
    }
    
    fun w(tag: String, message: String) {
        if (isDebug) {
            Log.w(tag, message)
        }
    }
    
    fun e(message: String) {
        Log.e(TAG, message)
    }
    
    fun e(tag: String, message: String) {
        Log.e(tag, message)
    }
    
    fun e(message: String, throwable: Throwable) {
        Log.e(TAG, message, throwable)
    }
    
    fun e(tag: String, message: String, throwable: Throwable) {
        Log.e(tag, message, throwable)
    }
    
    fun logException(message: String, throwable: Throwable) {
        if (isDebug) {
            e(message, throwable)
        }
    }
}
