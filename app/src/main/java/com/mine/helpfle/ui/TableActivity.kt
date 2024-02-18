package com.mine.helpfle.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.GridView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentTransaction
import com.mine.helpfle.Letter
import com.mine.helpfle.Letter.STATE
import com.mine.helpfle.R
import com.mine.helpfle.data.IDatabase
import com.mine.helpfle.data.IDatabase.OUTCOME
import com.mine.helpfle.data.DatabaseHelper

const val EXTRAS_TABLE_SOLUTION = "TABLE_SOLUTION"
const val TAG_TABLE_ACTIVITY = "TABLE_ACTIVITY"

class TableActivity : AppCompatActivity() {

    private lateinit var cursor : GridViewCursor
    lateinit var letterList : ArrayList<Letter>
    private lateinit var letterManager : KeyboardStateManager
    private lateinit var database : IDatabase
    private lateinit var solution : String
    private lateinit var gridView : GridView
    private lateinit var gridAdapter : GridAdapter
    private lateinit var keyboardLayout : ConstraintLayout

    // TODO: implement "helping" dialog / activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table)
        solution = intent.extras!!.getString(EXTRAS_TABLE_SOLUTION).toString().uppercase()

        gridView = findViewById(R.id.table_gridview)

        letterList = ArrayList<Letter>().also {
            for (i in 0..29) {
                it.add(Letter())
            }
        }

        cursor = GridViewCursor()

        gridAdapter = GridAdapter(this, letterList)
        gridView.adapter = gridAdapter
        gridView.isVerticalScrollBarEnabled = false


        keyboardLayout = findViewById(R.id.keyboard)
        letterManager = KeyboardStateManager()
        database = DatabaseHelper(this, "wordle-solutions.txt","wordle-dictionary.txt" )

        val keyboardTopRow = keyboardLayout.findViewById<LinearLayout>(R.id.keyboard_row_top)
        resources.getStringArray(R.array.keyboard_topRow).map { letter ->
            keyboardTopRow.addView(createKeyboardButton(letter))
        }

        val keyboardMidRow = keyboardLayout.findViewById<LinearLayout>(R.id.keyboard_row_middle)
        resources.getStringArray(R.array.keyboard_middleRow).map { letter ->
            keyboardMidRow.addView(createKeyboardButton(letter))
        }

        val keyboardBottomRow = keyboardLayout.findViewById<LinearLayout>(R.id.keyboard_row_bottom)
        resources.getStringArray(R.array.keyboard_bottomRow).map { letter ->
            keyboardBottomRow.addView(createKeyboardButton(letter))
        }

        keyboardLayout.findViewById<ActionButton>(R.id.btn_enter).setOnClickListener {
            notifyEnterPressed()
        }


        keyboardLayout.findViewById<ActionButton>(R.id.btn_delete).apply {
            // short / normal click: delete one letter
            this.setOnClickListener { notifyDeletePressed() }
            // long click: empty row
            this.setOnLongClickListener {
                while (!cursor.isRowEmpty()) {
                    notifyDeletePressed()
                }
                true
            }
        }

    }

    /* Helper function for adding buttons to keyboard */
    private fun createKeyboardButton(letter: String) : KeyboardLetter {
        val kb = KeyboardLetter(this).also {
            it.letter = letter
            it.bgColor = renderColor(LetterTextView.backgroundColor(STATE.UNKNOWN))
            it.textColor = renderColor(LetterTextView.textColor(STATE.UNKNOWN))
        }
        kb.setOnClickListener {
            notifyKeyboardButtonPressed(letter)
        }

        kb.tag = letter
        return kb
    }

    /* These functions pass user events to the cursor to update/validate input and then notify
    * UI of changes */
    private fun notifyEnterPressed() {
        Log.v(TAG_TABLE_ACTIVITY, "Enter pressed")

        // If the row isn't completely filled in, do nothing
        if (!cursor.isRowComplete()) {
            return
        }

        // TODO: use coroutine to prevent blocking UI thread on word lookup
        val validWord = cursor.onEnterPressed()

        if (validWord) {
            val guessedLetters = cursor.processGuess()

            cursor.getRowOffset().let { idx -> idx.minus(5)..idx.minus(5).plus(4) }
                .forEachIndexed { i, idx ->
                    letterList[idx].anim = Flip {
                        when {
                            // user won game --> show end of game dialog
                            cursor.won && i == 4 -> finishGame(OUTCOME.WON, 6 - cursor.guessesRemaining())

                            // user ran out of guesses --> show end of game dialog
                            cursor.guessesRemaining() == 0 && i == 4 -> finishGame(OUTCOME.LOST, null)

                            // reset animations and continue game
                            else -> { }
                        }
                    }
                }

            gridAdapter.notifyDataSetChanged()

            // Update keyboard colors with new information
            guessedLetters.forEach { letter ->
                keyboardLayout
                    .findViewWithTag<LetterTextView>(letter)
                    .also { kb ->
                        letterManager.get(letter).also { newState ->
                            kb.bgColor = renderColor(LetterTextView.backgroundColor(newState))
                            kb.textColor = renderColor(LetterTextView.textColor(newState))
                        }
                    }
            }

        } else {
            cursor.getRowOffset().let { i -> i..i.plus(4) }.forEach { i ->
                letterList[i].anim = Wiggle {}
            }
            gridAdapter.notifyDataSetChanged()
        }

    }

    private fun renderColor(colorId : Int) : Int {
        return resources.getColor(colorId, this.theme)
    }

    // TODO: allow hard input
    private fun notifyKeyboardButtonPressed(letter : String) {
        Log.v(TAG_TABLE_ACTIVITY, "Keyboard Button pressed: $letter")

        letterList.forEach { it.clearAnim() }

        cursor.onLetterPressed(letter).also {
                validAction -> if (validAction) gridAdapter.notifyDataSetChanged()
        }
    }

    private fun notifyDeletePressed() {
        Log.v(TAG_TABLE_ACTIVITY, letterList.toString())
        cursor.onBackPressed().also { validAction ->
            if (validAction) {
                letterList.forEach { it.clearAnim() }
                gridAdapter.notifyDataSetChanged()
            }
        }
        Log.i(TAG_TABLE_ACTIVITY, "Delete pressed")
    }

    // TODO: dialog theme / colors
    private fun finishGame(outcome : OUTCOME, numGuesses : Int?) {
        database.onFinishGame(outcome, numGuesses)
        val stats = database.getUserStats()

        val transaction = supportFragmentManager.beginTransaction()
        // For a polished look, specify a transition animation.
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity.
        transaction
            .add(
                android.R.id.content,
                EndOfGameDialogFragment
                    .newInstance(outcome, stats)
            )
            .addToBackStack(null)
            .commit()
    }

    fun onStartNewGame() {
        val intent = Intent(this, TableActivity::class.java)
        intent.putExtra(EXTRAS_TABLE_SOLUTION, database.getCurrentSolution())

        finish()
        startActivity(intent)
    }

    fun onEndGameDialogDismissed() {
        val intent = Intent(this, MainActivity::class.java)
        finish()
        startActivity(intent)
    }

    inner class GridViewCursor {
        private var cursor = 0
        private var currentRow = 0
        private lateinit var guessBuffer : String
        var won = false

        fun isRowComplete() : Boolean {
            return currentRow * 5 + 4 < cursor
        }

        fun isRowEmpty() : Boolean {
            return currentRow * 5 == cursor
        }

        fun onEnterPressed() : Boolean {
            // row is complete- get guess from current row
            guessBuffer = yieldGuess()

            return if (!database.lookupGuess(guessBuffer)) {
                // dictionary doesn't contain word
                false
            } else {
                // move cursor to next row
                currentRow++
                true
            }
        }

        fun onLetterPressed(letter: String) : Boolean {
            return if (currentRow * 5 + 4 < cursor) {
                // user needs to enter their guess or backspace
                false
            } else {
                letterList[cursor].letter = letter
                cursor ++
                true
            }
        }

        fun onBackPressed() : Boolean {
            return if (currentRow * 5 >= cursor) {
                // user is at the start of a new row
                false
            } else {
                cursor--
                letterList[cursor].letter = ""
                true
            }
        }

        private fun yieldGuess() : String {
            val guess = letterList.subList(currentRow * 5, cursor).joinToString(separator = "")
            Log.i(TAG_TABLE_ACTIVITY, "guess = $guess")
            return guess
        }

        fun getRowOffset() : Int {
            return currentRow * 5
        }

        fun guessesRemaining() : Int {
            return 6 - currentRow
        }

        fun processGuess() : List<String> {
            val rowInset = (currentRow - 1) * 5

            // Pull guess from buffer
            val guess = guessBuffer.toCharArray()

            // Make a copy of the solution string so we don't modify the original
            val solutionCopy : CharArray = solution.toCharArray()

            // Look for green letters
            guess.mapIndexed { i, char ->
                if (solutionCopy[i] == char) {
                    letterList[rowInset + i].state = STATE.KNOWN_CORRECT_POSITION
                    solutionCopy[i] = '-'
                    guess[i] = '_'
                }
            }

            // Look for yellow letters
            guess.mapIndexed { i, char ->
                val j = solutionCopy.indexOf(char)
                if (j >= 0) {
                    letterList[rowInset + i].state = STATE.KNOWN_SOMEWHERE_IN_WORD
                    solutionCopy[j] = '-'
                    guess[i] = '_'
                }
            }

            // Remaining letters in guess
            guess.mapIndexed { i, char ->
                if (char != '_') {
                    letterList[rowInset + i].state = STATE.KNOWN_NOT_IN_WORD
                }
            }

            // Update letter manager with new letter information
            val guessedLetters = letterList.subList(rowInset, rowInset + 5)
            guessedLetters.forEach { letter ->
                letterManager.update(letter.letter, letter.state)
            }

            Log.d(TAG_TABLE_ACTIVITY, "$guessBuffer == $solution")
            if (guessBuffer == solution) {
                won = true
            }

            // Reset buffer
            guessBuffer = ""

            // Return keys to update in keyboard
            return guessedLetters.map { letter -> letter.letter }
        }
    }

    class KeyboardStateManager {

        private var states : HashMap<String, STATE> = HashMap()

        init {
            for (c in 'A'..'Z') {
                states[c.toString()] = STATE.UNKNOWN
            }
        }

        fun update(letter : String, state : STATE) {
            states[letter] = minOf(states[letter]!!, state)
        }

        fun get(letter: String): STATE {
            return states[letter]!!
        }
    }
}
