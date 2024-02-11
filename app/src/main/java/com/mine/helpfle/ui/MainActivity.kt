package com.mine.helpfle.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.mine.helpfle.R
import com.mine.helpfle.data.IDatabase
import com.mine.helpfle.data.DatabaseHelper
import kotlinx.coroutines.launch

const val TAG_MAINACTIVITY = "MAINACTIVITY"

class MainActivity : AppCompatActivity() {

    private lateinit var dbInstance : IDatabase
    private lateinit var word : String
    private lateinit var btn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.i(TAG_MAINACTIVITY, "Initializing database")

        btn = findViewById<Button>(R.id.button_main_begin).also {

            it.setOnClickListener {
                val intent = Intent(this, TableActivity::class.java)

                intent.putExtra(
                    TABLE_EXTRAS_SOLUTION,
                    word
                )

                startActivity(intent)
            }
        }

        // TODO: use coroutine to prevent DB from blocking UI
        dbInstance = DatabaseHelper(this, "wordle-solutions.txt","wordle-dictionary.txt" )
        word = dbInstance.getCurrentSolution()
        // TODO: update UI to change when btn is enabled
        btn.isEnabled = true

    }
}