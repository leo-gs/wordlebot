package com.mine.helpfle.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.GridView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.mine.helpfle.Letter
import com.mine.helpfle.Letter.STATE
import com.mine.helpfle.R
import com.mine.helpfle.data.IDatabase
import com.mine.helpfle.data.DatabaseHelper

const val TABLE_EXTRAS_SOLUTION = "solution"
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
        solution = intent.extras!!.getString(TABLE_EXTRAS_SOLUTION).toString().uppercase()

        gridView = findViewById(R.id.table_gridview)

        letterList = ArrayList<Letter>().also {
            for (i in 0..29) {
                it.add(Letter())
            }
        }

        cursor = GridViewCursor()

        gridAdapter = GridAdapter(this, letterList)
        gridView.adapter = gridAdapter


        keyboardLayout = findViewById(R.id.keyboard_layout)
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

        keyboardLayout.findViewById<ActionButton>(R.id.btn_delete).setOnClickListener {
            notifyDeletePressed()
        }
    }

    /* Helper function for adding buttons to keyboard */
    private fun createKeyboardButton(letter: String) : KeyboardButton {
        val kb = KeyboardButton(this, letter)
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
                            cursor.won && i == 4 -> showDialog(
                                getString(R.string.dialog_outcome_won),
                                IDatabase.OUTCOME.WON
                            )

                            cursor.guessesRemaining() == 0 && i == 4 -> showDialog(
                                getString(R.string.dialog_outcome_lost),
                                IDatabase.OUTCOME.LOST
                            )

                            else -> { Log.d(TAG_GRIDANIMATION, "empty lambda") }
                        }
                    }
                }

            gridAdapter.notifyDataSetChanged()

            // Update keyboard colors with new information
            guessedLetters.forEach { letter ->
                keyboardLayout
                    .findViewWithTag<KeyboardButton>(letter)
                    .also { kb ->
                        letterManager.get(letter).also { newState ->
                            kb.updateColor(
                                background = renderColor(Letter.backgroundColor(newState)),
                                text = renderColor(Letter.textColor(newState))
                            )
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

    private fun showDialog(title : String, outcome : IDatabase.OUTCOME) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        builder
            .setMessage("Start a new game?")
            .setTitle(title)
            .setPositiveButton("New game") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(this, TableActivity::class.java)

                database.onFinishGame(outcome)
                intent.putExtra(TABLE_EXTRAS_SOLUTION, database.getCurrentSolution())

                finish()
                startActivity(intent)
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    inner class GridViewCursor {
        private var cursor = 0
        private var currentRow = 0
        private lateinit var guessBuffer : String
        var won = false

        fun isRowComplete() : Boolean {
            return currentRow * 5 + 4 < cursor
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
