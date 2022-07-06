package com.pawoon.syncdbsdk.extension

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import info.penadidik.utils.AESCrypt
import info.penadidik.utils.BuildConfig
import java.security.GeneralSecurityException

/**
 * Basic Pref
 */
private fun Context.pref(): SharedPreferences {
    return this.getSharedPreferences(BuildConfig.LIBRARY_PACKAGE_NAME, Activity.MODE_PRIVATE)
}

/**
 * Checking pref
 */
fun Context.prefContains(strKey: String): Boolean {
    return this.pref().contains(encryptPref(strKey))
}

/**
 * Encrypt Pref
 */
private fun Context.encryptPref(value: String): String {
    val encrypted: String
    try {
        encrypted = AESCrypt.encrypt(BuildConfig.PREF_KEY, value)
    } catch (e: GeneralSecurityException) {
        //handle error
        return ""
    }
    return encrypted
}

/**
 * Decrypt Pref
 */
private fun Context.decryptPref(value: String): String {
    val decrypted: String
    try {
        decrypted = AESCrypt.decrypt(BuildConfig.PREF_KEY, value)
    } catch (e: GeneralSecurityException) {
        //handle error
        return ""
    }
    return decrypted
}


/**
 * save to pref - contex
 */
fun Context.saveToPref(strKey: String, value: Any?) {
    /* checking value type */
    val editor = this.pref().edit()
    when (value) {
        is String -> editor.putString(encryptPref(strKey), encryptPref(value))
        is Boolean -> editor.putBoolean(encryptPref(strKey), value)
        is Float -> editor.putFloat(encryptPref(strKey), value)
        is Int -> editor.putInt(encryptPref(strKey), value)
        is Long -> editor.putLong(encryptPref(strKey), value)
    }
    /* save pref */
    editor.apply()
}

/**
 * delete from pref - Context
 */
fun Context.deleteFromPref(vararg strKey: String) {
    val editor = this.pref().edit()
    strKey.forEach { key ->
        editor.remove(encryptPref(key))
    }
    /* save pref */
    editor.apply()
}

/**
 * get value from pref
 */
fun Context.getStringFromPref(strKey: String): String {
    val value = this.pref().getString(encryptPref(strKey), "") ?: ""
    var valueDecrypt = decryptPref(value)
    if (valueDecrypt.isBlank()) {
        valueDecrypt = ""
    }
    return valueDecrypt
}

fun Context.getBooleanFromPref(strKey: String): Boolean {
    return this.pref().getBoolean(encryptPref(strKey), false)
}

fun Context.getIntFromPref(strKey: String): Int {
    return this.pref().getInt(encryptPref(strKey), 0)
}

fun Context.getLongFromPref(strKey: String): Long {
    return this.pref().getLong(encryptPref(strKey), 0)
}

fun Context.getPrefString(key: String): String {
    return if (this.prefContains(key)) {
        this.getStringFromPref(key)
    } else {
        ""
    }
}

fun Context.getPrefBoolean(key: String): Boolean {
    return if (this.prefContains(key)) {
        this.getBooleanFromPref(key)
    } else {
        false
    }
}

fun Context.getPrefInt(key: String): Int {
    return if (this.prefContains(key)) {
        this.getIntFromPref(key)
    } else {
        0
    }
}

fun Context.getPrefLong(key: String): Long {
    return if (this.prefContains(key)) {
        this.getLongFromPref(key)
    } else {
        0
    }
}

fun Context.destroy() {
    this.pref().edit().clear().apply()
}