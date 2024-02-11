package com.mine.helpfle.ui

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton

const val TAG_KEYBOARDBUTTON = "KEYBOARDBUTTON"

open class Btn : AppCompatButton {

    constructor(context: Context, label: String) : super(context) {
        text = label
        contentDescription = label
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, int: Int) : super(context, attrs, int)
}

class KeyboardButton : Btn {

    constructor(context: Context, label: String) : super(context, label) {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1F
        ).also { it.setMargins(5, 5, 5, 5) }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, int: Int) : super(context, attrs, int)

    fun updateColor(background : Int, text : Int? = null) {
        backgroundTintList = ColorStateList.valueOf(background)

        if (text != null) {
            setTextColor(ColorStateList.valueOf(text))
        }
    }
}

class ActionButton : Btn {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, int: Int) : super(context, attrs, int)
}