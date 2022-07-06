package info.penadidik.utils.extension

import android.content.Context
import android.widget.Toast

fun Context.showToast(value: String) = Toast.makeText(this, value, Toast.LENGTH_SHORT).show()