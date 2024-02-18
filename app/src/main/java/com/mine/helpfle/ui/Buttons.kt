package com.mine.helpfle.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Dimension
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.getStringOrThrow
import com.mine.helpfle.Letter
import com.mine.helpfle.R

open class Btn : AppCompatButton {

    constructor(context: Context, label: String) : super(context) {
        text = label
        contentDescription = label
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
}

class ActionButton : Btn {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, int: Int) : super(context, attrs, int)
}

const val TAG_LETTERVIEW = "LETTERVIEW"

open class LetterTextView @JvmOverloads constructor (context: Context, attrs: AttributeSet? = null, defStyle : Int = 0)
    : ConstraintLayout(context, attrs, defStyle) {

    private var containerView : ConstraintLayout
    private var textView : TextView

    var bgColor : Int = resources.getColor(backgroundColor(Letter.STATE.UNKNOWN), context.theme)
        set(value) {
            field = value
            textView.background.setTint(bgColor)
            invalidate()
            requestLayout()
        }

    var textColor : Int  = resources.getColor(textColor(Letter.STATE.UNKNOWN), context.theme)
        set(value) {
            field = value
            textView.setTextColor(value)
            invalidate()
            requestLayout()
        }

    var textSize : Float = resources.getDimension(R.dimen.letterView_textSize)
        set(value) {
            field = value
            textView.setTextSize(Dimension.SP, value)
        }

    private var drawBorder : Boolean = false
        set(value) {
            field = value
            containerView.background = ResourcesCompat.getDrawable(
                resources,
                when (value) {
                    true -> R.drawable.rounded_corner_border
                    false -> R.drawable.rounded_corner
                },
                context.theme
            )
            invalidate()
            requestLayout()
        }

    var letter : String = ""
        set(value) {
            field = value
            textView.text = value
            invalidate()
            requestLayout()
        }

    var state : Letter.STATE = Letter.STATE.UNKNOWN
        set (value) {
            field = value
            resources.getColor(backgroundColor(value), context.theme).also { newCol ->
                textView.background.setTint(newCol)
                bgColor = newCol
            }

            resources.getColor(textColor(state), context.theme).also { newCol ->
                textView.setTextColor(newCol)
                textColor = newCol
            }
        }

    init {

        LayoutInflater.from(context).inflate(R.layout.letter_text_view, this)

        containerView = findViewById(R.id.letter_container_view)
        textView = findViewById(R.id.letter_text_view)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LetterTextView,
            0, 0
        ).apply {

            try {
                drawBorder = getBoolean(R.styleable.LetterTextView_drawBorder, false)
                letter = getString(R.styleable.LetterTextView_letter).toString()
                textSize = getDimension(R.styleable.LetterTextView_textSize, 30F)
                state = Letter.STATE.UNKNOWN
            } finally {
                recycle()
            }
        }
    }

    companion object {
        fun backgroundColor(state : Letter.STATE) : Int {
            return when (state) {
                Letter.STATE.UNKNOWN -> R.color.state_unknown
                Letter.STATE.KNOWN_NOT_IN_WORD -> R.color.state_not_in_word
                Letter.STATE.KNOWN_SOMEWHERE_IN_WORD -> R.color.state_in_word
                Letter.STATE.KNOWN_CORRECT_POSITION -> R.color.state_correct_position
            }
        }

        fun textColor(state : Letter.STATE) : Int {
            return when (state) {
                Letter.STATE.UNKNOWN -> R.color.on_state_unknown
                Letter.STATE.KNOWN_NOT_IN_WORD -> R.color.on_state_not_in_word
                Letter.STATE.KNOWN_SOMEWHERE_IN_WORD -> R.color.on_state_in_word
                Letter.STATE.KNOWN_CORRECT_POSITION -> R.color.on_state_correct_position
            }
        }
    }

}

class KeyboardLetter @JvmOverloads constructor (context: Context, attrs: AttributeSet? = null, defStyle : Int = 0)
    : LetterTextView(context, attrs, defStyle)  {

        init {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
            1F
            ).also { it.setMargins(5, 5, 5, 5) }
            textSize = resources.getDimension(R.dimen.keyboardLetter_textSize)
        }

}