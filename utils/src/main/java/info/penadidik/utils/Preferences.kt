package info.penadidik.utils

import android.content.Context
import com.pawoon.syncdbsdk.extension.getStringFromPref
import com.pawoon.syncdbsdk.extension.saveToPref

class Preferences(private val context: Context) {

    fun saveStringData(keyword: String, data: String) {
        context.saveToPref(keyword, data)
    }

    fun getStringData(keyword: String): String {
        return context.getStringFromPref(keyword)
    }
}