package com.mine.helpfle

import android.animation.AnimatorSet
import androidx.cardview.widget.CardView

class Letter(letter: String) {

    var anim : (CardView) -> AnimatorSet? = { null }

    companion object {
        fun backgroundColor(state : STATE) : Int {
            return when (state) {
                STATE.UNKNOWN -> R.color.primary
                STATE.KNOWN_NOT_IN_WORD -> R.color.state_not_in_word
                STATE.KNOWN_SOMEWHERE_IN_WORD -> R.color.state_in_word
                STATE.KNOWN_CORRECT_POSITION -> R.color.state_correct_position
            }
        }

        fun textColor(state : STATE) : Int {
            return when (state) {
                STATE.UNKNOWN -> R.color.on_primary
                STATE.KNOWN_NOT_IN_WORD -> R.color.on_primary_deemph
                STATE.KNOWN_SOMEWHERE_IN_WORD -> R.color.on_primary
                STATE.KNOWN_CORRECT_POSITION -> R.color.on_primary
            }
        }
    }

    constructor() : this("")

    var letter : String
    var state : STATE = STATE.UNKNOWN

    init {
        this.letter = letter
    }

    enum class STATE { KNOWN_CORRECT_POSITION, KNOWN_SOMEWHERE_IN_WORD, KNOWN_NOT_IN_WORD, UNKNOWN }

    override fun toString(): String {
        return letter
    }

    fun backgroundColor() : Int {
        return Companion.backgroundColor(this.state)
    }

    fun textColor() : Int {
        return Companion.textColor(this.state)
    }

    fun clearAnim() {
        anim = { null }
    }
}