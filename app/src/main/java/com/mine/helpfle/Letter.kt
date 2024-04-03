package com.mine.helpfle

import com.mine.helpfle.ui.GridAnimation
import com.mine.helpfle.ui.NoAnim

class Letter(letter: String) {

    var anim : GridAnimation = NoAnim { }

    var letter : String
    var state : STATE = STATE.UNKNOWN

    init {
        this.letter = letter
    }

    interface StateColor {
        val backgroundColor : Int
        val textColor : Int
    }

    enum class STATE(override val backgroundColor: Int, override val textColor: Int) : StateColor {
        KNOWN_CORRECT_POSITION(R.color.state_correct_position, R.color.on_state_correct_position),
        KNOWN_SOMEWHERE_IN_WORD(R.color.state_in_word, R.color.on_state_in_word),
        KNOWN_NOT_IN_WORD(R.color.state_not_in_word, R.color.on_state_not_in_word),
        UNKNOWN(R.color.state_unknown, R.color.on_state_unknown)
    }

    override fun toString(): String {
        return letter
    }

    fun clearAnim() {
        anim = NoAnim { }
    }

    companion object {
        fun empty() : Letter {
            return Letter("")
        }
    }
}