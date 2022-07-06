package info.penadidik.utils.extension

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.Button
import android.widget.TextView
import info.penadidik.utils.R

interface DialogListener {
    fun onClickAction()
    fun onDismiss()
}

interface DialogTwoActionListener {
    fun onClickCancel()
    fun onClickOk()
    fun onDismiss()
}

fun Activity.showDialogMessage(message: String, actionOk: String, listener: DialogTwoActionListener) {
    // custom dialog
    val dialog = Dialog(this, R.style.DialogSlideAnim)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.setContentView(R.layout.dialog_message)
    dialog.setCancelable(false)

    dialog.setOnDismissListener { listener.onDismiss() }

    dialog.findViewById<TextView>(R.id.lblMessage).text = message
    dialog.findViewById<Button>(R.id.btnActionOk).text = actionOk
    dialog.findViewById<Button>(R.id.btnActionCancel).text = getString(R.string.alert_exit)

    dialog.findViewById<Button>(R.id.btnActionCancel).setOnClickListener {
        listener.onClickCancel()
        dialog.dismiss() }
    dialog.findViewById<Button>(R.id.btnActionOk).setOnClickListener {
        listener.onClickOk()
        dialog.dismiss() }

    if (!isFinishing) dialog.show()
}

fun Activity.showNotifConnect(listener: DialogTwoActionListener) {
    // custom dialog
    val dialog = Dialog(this, R.style.DialogSlideAnim)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.setContentView(R.layout.dialog_notification_connection)
    dialog.setCancelable(false)

    dialog.findViewById<Button>(R.id.btnNotifReject).setOnClickListener {
        listener.onClickCancel()
        dialog.dismiss() }
    dialog.findViewById<Button>(R.id.btnNotifAccept).setOnClickListener {
        listener.onClickOk()
        dialog.dismiss() }

    if (!isFinishing) dialog.show()
}