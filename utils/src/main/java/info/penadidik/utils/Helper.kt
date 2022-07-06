package info.penadidik.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.net.URL

fun Activity.showSoftKeyboard(editText: EditText) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.hideSoftKeyboard() {
    val view = this.currentFocus
    if (view != null) {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun String.isValidURL(): Boolean {
    /* Try creating a valid URL */
    return try {
        URL(this).toURI()
        true
    } catch (e: Exception) {
        false
    }
    // If there was an Exception
    // while creating URL object
}

fun Context.loadUrl(url: String) {
    if (url.isValidURL()) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {}
    }
}

fun Activity.getRandomString(length: Int) : String {
    val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { charset.random() }
        .joinToString("")
}

fun Activity.showLog(class_name: AppCompatActivity, log: String) {
    if (BuildConfig.DEBUG) Log.d("HelperLog from "+class_name.packageName, log)
}