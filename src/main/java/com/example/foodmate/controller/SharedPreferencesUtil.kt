package com.example.foodmate.controller

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.foodmate.network.ImageUtil
import java.io.IOException

object SharedPreferencesUtil {
    private const val PREF_NAME = "SessionPrefs"
    private const val KEY_SESSION_ID = "sessionId"
    private const val KEY_SESSION_PW = "sessionPw"
    private const val KEY_SESSION_NICKNAME = "sessionNickname"
    private const val KEY_LOGGED_IN = "isLoggedIn"
    private const val SESSION_IMAGE = "session_image"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(context: Context, sessionId: String, sessionPw: String, sessionNickname: String?) {
        val sharedPreferences = getSharedPreferences(context)
        sharedPreferences.edit().apply {
            putString(KEY_SESSION_ID, sessionId)
            putString(KEY_SESSION_PW, sessionPw)
            sessionNickname?.let { putString(KEY_SESSION_NICKNAME, it) }
            putBoolean(KEY_LOGGED_IN, true) // 로그인 상태를 true로 설정
            apply()
        }
    }

    fun getSessionId(context: Context): String? {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getString(KEY_SESSION_ID, null)
    }

    fun getSessionPw(context: Context): String? {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getString(KEY_SESSION_PW, null)
    }

    fun getSessionNickname(context: Context): String? {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getString(KEY_SESSION_NICKNAME, null)
    }

    fun setLoggedIn(context: Context, isLoggedIn: Boolean) {
        val sharedPreferences = getSharedPreferences(context)
        sharedPreferences.edit().apply {
            putBoolean(KEY_LOGGED_IN, isLoggedIn)
            apply()
        }
    }

    fun checkLoggedIn(context: Context): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getBoolean(KEY_LOGGED_IN, false)
    }

    fun updateSession(context: Context, id: String, password: String, nickname: String, bitmap: Bitmap) {
        saveSession(context, id, password, nickname)
        saveImage(context, bitmap)
    }

    fun reloadSession(context: Context, sessionId: String, sessionPw: String, sessionNickname: String?, bitmap: Bitmap) {
        val sharedPreferences = getSharedPreferences(context)
        val loadedSessionId = sharedPreferences.getString(KEY_SESSION_ID, null)

        if (loadedSessionId == sessionId) {
            saveSession(context, sessionId, sessionPw, sessionNickname)
            saveImage(context, bitmap)
        }
    }

    fun clearSession(context: Context) {
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    fun checkSessionExists(context: Context): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        val sessionId = sharedPreferences.getString(KEY_SESSION_ID, null)
        val sessionPw = sharedPreferences.getString(KEY_SESSION_PW, null)
        val sessionNickname = sharedPreferences.getString(KEY_SESSION_NICKNAME, null)

        return !sessionId.isNullOrEmpty() && !sessionPw.isNullOrEmpty() && !sessionNickname.isNullOrEmpty()
    }

    fun reloadSessionAfterWithdrawal(context: Context, sessionId: String, sessionPw: String, sessionNickname: String?) {
        val sharedPreferences = getSharedPreferences(context)
        val loadedSessionId = sharedPreferences.getString(KEY_SESSION_ID, null)

        if (loadedSessionId == sessionId) {
            val savedSessionPw = sharedPreferences.getString(KEY_SESSION_PW, null)
            val savedSessionNickname = sharedPreferences.getString(KEY_SESSION_NICKNAME, null)
            saveSession(context, sessionId, savedSessionPw ?: "", savedSessionNickname)
        }
    }

    fun saveImage(context: Context, bitmap: Bitmap) {
        try {
            val encodedImage = ImageUtil.encodeBitmapToBase64(bitmap)
            val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            editor.putString(SESSION_IMAGE, encodedImage)
            editor.apply()
        } catch (e: IOException) {
            Log.e("SharedPreferencesUtil", "Failed to save image: ${e.message}")
        }
    }

    fun getImage(context: Context): Bitmap? {
        try {
            val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val encodedImage = sharedPreferences.getString(SESSION_IMAGE, null)
            if (encodedImage != null) {
                val byteArray = Base64.decode(encodedImage, Base64.DEFAULT)
                return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            }
        } catch (e: ClassCastException) {
            Log.e("SharedPreferencesUtil", "Failed to load image: ${e.message}")
        } catch (e: IOException) {
            Log.e("SharedPreferencesUtil", "Failed to load image: ${e.message}")
        }
        return null
    }
}