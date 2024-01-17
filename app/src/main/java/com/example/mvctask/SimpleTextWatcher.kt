package com.example.mvctask

import android.text.Editable
import android.text.TextWatcher

open class SimpleTextWatcher : TextWatcher {
    override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
        // Implementation not needed
    }

    override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
        // Implementation not needed
    }

    override fun afterTextChanged(editable: Editable?) {
        // Implementation not needed
    }
}
