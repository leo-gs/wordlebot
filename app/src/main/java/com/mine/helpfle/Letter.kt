package com.mine.helpfle

import com.mine.helpfle.ui.GridAnimation
import com.mine.helpfle.ui.NoAnim

class Letter(letter: String) {

    var anim : GridAnimation = NoAnim { }

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

    fun clearAnim() {
        anim = NoAnim { }
    }
}