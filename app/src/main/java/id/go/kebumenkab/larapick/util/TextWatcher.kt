package id.go.kebumenkab.larapick.util

import android.text.Editable
import android.text.TextWatcher
import kotlin.reflect.KFunction0

fun createTextWatcher(onTextChanged: KFunction0<Boolean>): TextWatcher {
    return object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            onTextChanged()
        }
        override fun afterTextChanged(p0: Editable?) {}
    }
}