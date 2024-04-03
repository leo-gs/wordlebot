package com.mine.helpfle.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mine.helpfle.Letter
import com.mine.helpfle.R
import com.mine.helpfle.ui.GridActivity.KeyboardStateManager
import java.io.Serializable

const val TAG_DIALOG_WORDLEBOT = "DIALOG_WORDLEBOT"

class WordleBotDialogFragment : DialogFragment() {

    companion object {
        fun newInstance() : WordleBotDialogFragment {
            return WordleBotDialogFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG_DIALOG_ENDOFGAME, "onCreate dialog fragment")
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dialog_wordlebot, container, false).apply {

            val solutions = generatePossibleCombos()

            findViewById<TextView>(R.id.dialog_outcome_text).text = getString(R.string.dialog_header_wordlebot, solutions.count())

            findViewById<RecyclerView>(R.id.dialog_wordlebot_rv).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = SolutionsAdapter(solutions.toList())
            }


            // Set positive button action
            findViewById<AppCompatButton>(R.id.dialog_btn_positive).setOnClickListener {
                (activity as GridActivity).onWordleBotDialogDismissed()
            }
//            // Set negative button action
//            findViewById<AppCompatButton>(R.id.dialog_btn_dismiss).setOnClickListener {
//                (activity as GridActivity).onWordleBotDialogDismissed()
//            }
        }
    }

    private fun generatePossibleCombos() : Sequence<String> {

        val knownAt = arrayOfNulls<String>(5)
        val knownIn = arrayListOf<String>()
        val knownNotAt = (0..4).map { hashSetOf<String>() }

        val knownNotIn = HashSet<String>()

        (activity as GridActivity).letterList.forEachIndexed { i, L ->
            val wIndex = i % 5
            when (L.state) {
                Letter.STATE.KNOWN_CORRECT_POSITION -> {
                    knownAt[wIndex] = L.letter
                    knownIn.add(L.letter)
                }
                Letter.STATE.KNOWN_SOMEWHERE_IN_WORD -> {
                    knownIn.add(L.letter)
                    knownNotAt[wIndex].add(L.letter)
                }
                Letter.STATE.KNOWN_NOT_IN_WORD -> {
                    knownNotIn.add(L.letter)
                }
                Letter.STATE.UNKNOWN -> { }
            }

        }

        val letters = HashSet<String>().also {
            ('A'..'Z').forEach { l ->
                it.add(l.toString())
            }
        }
        val possibleLettersAt : List<HashSet<String>> = (0..4).map { wIdx ->
                val lAt = knownAt[wIdx]
                if (lAt != null) {
                    hashSetOf(lAt)
                } else {
                    (letters subtract knownNotIn subtract knownNotAt[wIdx]) as HashSet<String>
                }
            }

        for (i in 0..4) {
            Log.d(TAG_DIALOG_WORDLEBOT, "possibleLettersAt[$i] = ${possibleLettersAt[i]}")
        }
        return sequence {
            possibleLettersAt[0].forEach { a ->
                possibleLettersAt[1].forEach { b ->
                    possibleLettersAt[2].forEach { c ->
                        possibleLettersAt[3].forEach { d ->
                            possibleLettersAt[4].forEach { e ->
                                yield("$a$b$c$d$e")
                            }
                        }
                    }
                }
            }
        }.filter { str ->
            knownIn.all { l -> str.contains(l) }
        }
    }
}

class SolutionsAdapter(private val solutions : List<String>) : RecyclerView.Adapter<SolutionsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val solution = solutions[position]
        holder.textView.text = solution
    }

    override fun getItemCount(): Int {
        return solutions.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(android.R.id.text1)
    }

}