package com.mine.helpfle.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.mine.helpfle.R
import com.mine.helpfle.data.IDatabase.OUTCOME
import com.mine.helpfle.data.UserStats
import java.io.Serializable

private const val EXTRAS_DIALOG_OUTCOME = "DIALOG_OUTCOME"
private const val EXTRAS_DIALOG_STATS = "DIALOG_STATS"
const val TAG_DIALOG_ENDOFGAME = "DIALOG_ENDOFGAME"

class EndOfGameDialogFragment : DialogFragment() {

    private lateinit var gameOutcome : OUTCOME
    private lateinit var userStats : UserStats

    companion object {
        fun newInstance(gameOutcome : OUTCOME, stats : UserStats) : EndOfGameDialogFragment {
            return EndOfGameDialogFragment().apply {
                arguments = Bundle().also { bundle ->
                    bundle.putSerializable(EXTRAS_DIALOG_OUTCOME, gameOutcome)
                    bundle.putSerializable(EXTRAS_DIALOG_STATS, stats)
                }
            }
        }
    }

    private fun getSerializableArgCompat(bundle : Bundle, extrasTag : String) : Serializable? {
        @Suppress("DEPRECATION")
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> bundle.getSerializable(extrasTag)!!
            else -> bundle.getSerializable(extrasTag)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG_DIALOG_ENDOFGAME, "onCreate dialog fragment")
        arguments?.let {
            gameOutcome = getSerializableArgCompat(it, EXTRAS_DIALOG_OUTCOME) as OUTCOME
            userStats = getSerializableArgCompat(it, EXTRAS_DIALOG_STATS) as UserStats
        }

        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dialog_endofgame, container, false).apply {

            findViewById<TextView>(R.id.dialog_outcome_text).text = resources.getString(
                when(gameOutcome) {
                    OUTCOME.WON -> R.string.dialog_outcome_won
                    OUTCOME.LOST -> R.string.dialog_outcome_lost
                }
            )

//            findViewById<ConstraintLayout>(R.id.dialog_header)
//                .setBackgroundColor(resources.getColor(
//                    when(gameOutcome) {
//                        OUTCOME.WON -> R.color.dialog_header_won_bg
//                        OUTCOME.LOST -> R.color.dialog_header_lost_bg
//                    }, context.theme))

            findViewById<TextView>(R.id.stats_numGames).text = resources
                .getQuantityString(R.plurals.stats_games_quantity, userStats.numGames, userStats.numGames)

            findViewById<TextView>(R.id.stats_gamesWon).text = resources
                .getQuantityString(R.plurals.stats_games_quantity, userStats.gamesWon, userStats.gamesWon)

            findViewById<TextView>(R.id.stats_gamesLost).text = resources
                .getQuantityString(R.plurals.stats_games_quantity, userStats.gamesLost, userStats.gamesLost)

            findViewById<TextView>(R.id.stats_avgGuesses).text = resources
                .getString(R.string.stats_avgGuesses_avg, userStats.avgGuesses)

            findViewById<TextView>(R.id.stats_currentStreak).text = resources
                .getQuantityString(R.plurals.stats_streak_quantity, userStats.currentStreak, userStats.currentStreak)

            findViewById<TextView>(R.id.stats_longestStreak).text = resources
                .getQuantityString(R.plurals.stats_streak_quantity, userStats.longestStreak, userStats.longestStreak)

            // Set positive button action
            findViewById<ConstraintLayout>(R.id.dialog_btn_newgame).setOnClickListener {
                (activity as GridActivity).onStartNewGame()
            }
//            // Set negative button action
//            findViewById<AppCompatButton>(R.id.dialog_btn_dismiss).setOnClickListener {
//                (activity as GridActivity).onEndGameDialogDismissed()
//            }
        }
    }
}

