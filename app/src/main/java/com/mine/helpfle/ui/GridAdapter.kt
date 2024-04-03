package com.mine.helpfle.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.mine.helpfle.Letter
import com.mine.helpfle.R

class GridAdapter(context : Context, letterList : ArrayList<Letter>) : ArrayAdapter<Letter> (context, 0, letterList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.view_table_cell, parent, false)
        }

        val letter = getItem(position)!!

        val letterView = listItemView!!.findViewById<CardLetterTextView>(R.id.letter_view)

        letterView.state = letter.state

        letter.anim.getAnimation(listItemView, position % 5, letterView.bgColor, letterView.textColor).also {
            it.addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    letterView.letter = letter.letter
                }
            })
            it.start()
        }

        return listItemView
    }
}

