package com.e.VoiceAssistant.utils

import android.util.Log
import com.e.VoiceAssistant.utils.Debug.DBG

object Debug { const val DBG=true }

fun printIfDebug(TAG: String?, message:String?){
    if (DBG){ println("$TAG : $message") }
}

fun printErrorIfDebug(TAG: String?, message:String?){
    if (DBG){  Log.e(TAG,message) }
}

fun printInfoIfDebug(TAG: String?, message:String?){
    if (DBG){  Log.i(TAG,message) }
}

fun printWarnIfDebug(TAG: String?, message:String?){
    if (DBG){  Log.w(TAG,message) }
}

fun printDebugIfDebug(TAG: String?, message:String?){
    if (DBG){  Log.d(TAG,message) }
}
