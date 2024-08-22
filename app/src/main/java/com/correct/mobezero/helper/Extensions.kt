package com.correct.mobezero.helper

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun Fragment.onBackPressed(function: () -> Unit) {
    (this.activity as AppCompatActivity).supportFragmentManager
    this.requireActivity().onBackPressedDispatcher.addCallback(
        this.requireActivity() /* lifecycle owner */,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                function()
            }
        })
}

fun TextView.setSpannable(
    startIndex: Int,
    endIndex: Int,
    text: String = "",
    color: Int,
    executableFun: () -> Unit
) {
    val spannableString = if (text.isNotEmpty()) {
        SpannableString(text)
    } else {
        val currentText = if (this.text is Spannable) {
            this.text as Spannable
        } else {
            SpannableString(this.text.toString())
        }
        currentText
    }

    val clickableSpan: ClickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            executableFun()
        }
    }

    spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    spannableString.setSpan(
        ForegroundColorSpan(color),
        startIndex,
        endIndex,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    this.text = spannableString
    this.movementMethod = LinkMovementMethod.getInstance() // Make the links clickable
}

fun String.generateShortName(): String {
    val arr = this.split(" ")
    if (arr.size > 1) {
        return "${arr[0][0]}${arr[1][0]}"
    } else {
        return "${arr[0][0]}"
    }
}

fun Context.displayDialog(layoutID: Int): Dialog {
    val dialog = Dialog(this)
    dialog.setContentView(layoutID)
    dialog.window?.setLayout(MATCH_PARENT, WRAP_CONTENT)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.setCancelable(true)
    dialog.setCanceledOnTouchOutside(true)
    dialog.show()
    return dialog
}

fun EditText.backSpace() {
    val pos: Int = this.selectionEnd
    val sb: StringBuilder = StringBuilder(this.text.toString())
    if (pos > 0) {
        sb.deleteCharAt(pos - 1)
        this.setText(sb.toString())
        this.setSelection(pos - 1)
    }
}