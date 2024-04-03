package com.mine.helpfle.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Dimension
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.mine.helpfle.Letter
import com.mine.helpfle.R

abstract class LetterTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle : Int = 0)
    : ConstraintLayout(context, attrs, defStyle) {
    abstract var containerView : ConstraintLayout
    abstract var textView : TextView

    abstract val layoutId : Int

    enum class SHAPE(val drawableId : Int) {
        BORDER_BLACK(R.drawable.rounded_corner_border),
        BORDER_NONE(R.drawable.rounded_corner)
    }

    var bgColor : Int = resources.getColor(Letter.STATE.UNKNOWN.backgroundColor, context.theme)
        set(value) {
            field = value
            textView.background.setTint(bgColor)
            invalidate()
            requestLayout()
        }

    var textColor : Int = resources.getColor(Letter.STATE.UNKNOWN.textColor, context.theme)
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

    protected var shape : SHAPE = SHAPE.BORDER_NONE
        set(value) {
            field = value
            containerView.background = ResourcesCompat.getDrawable(
                resources,
                value.drawableId,
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
            resources.getColor(value.backgroundColor, context.theme).also { newCol ->
                textView.background.setTint(newCol)
                bgColor = newCol
            }
            resources.getColor(value.textColor, context.theme).also { newCol ->
                textView.setTextColor(newCol)
                textColor = newCol
            }
        }

}

class CardLetterTextView @JvmOverloads constructor (context: Context, attrs: AttributeSet? = null, defStyle : Int = 0)
    : LetterTextView(context, attrs, defStyle) {

    override var containerView: ConstraintLayout
    override var textView: TextView

    override val layoutId = R.layout.view_card_letter_text

    init {

        LayoutInflater.from(context).inflate(layoutId, this)

        containerView = findViewById(R.id.letter_container_view)
        textView = findViewById(R.id.letter_text_view)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LetterTextView,
            0, 0
        ).apply {

            try {
                getBoolean(R.styleable.LetterTextView_drawBorder, false).also { drawBorder ->
                    shape = if (drawBorder) SHAPE.BORDER_BLACK else SHAPE.BORDER_NONE
                }
                letter = getString(R.styleable.LetterTextView_letter).toString()
                textSize = getDimension(R.styleable.LetterTextView_textSize, 30F)
                state = Letter.STATE.UNKNOWN
            } finally {
                recycle()
            }
        }
    }
}

class KeyboardCardLetter @JvmOverloads constructor (context: Context, attrs: AttributeSet? = null, defStyle : Int = 0)
    : LetterTextView(context, attrs, defStyle)  {

    override var containerView: ConstraintLayout
    override var textView: TextView

    override val layoutId = R.layout.view_keyboard_letter_text

    init {

        LayoutInflater.from(context).inflate(layoutId, this)

        containerView = findViewById(R.id.letter_container_view)
        textView = findViewById(R.id.letter_text_view)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LetterTextView,
            0, 0
        ).apply {
            try {
                getBoolean(R.styleable.LetterTextView_drawBorder, false).also { drawBorder ->
                    shape = if (drawBorder) SHAPE.BORDER_BLACK else SHAPE.BORDER_NONE
                }
                letter = getString(R.styleable.LetterTextView_letter).toString()
                textSize = getDimension(R.styleable.LetterTextView_textSize, 30F)
                state = Letter.STATE.UNKNOWN
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1F
                ).also { it.setMargins(5, 5, 5, 5) }
                textSize = resources.getDimension(R.dimen.keyboardLetter_textSize)
            } finally {
                recycle()
            }
        }
    }
}