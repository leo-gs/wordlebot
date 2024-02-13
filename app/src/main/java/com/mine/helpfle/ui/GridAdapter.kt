package com.mine.helpfle.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.mine.helpfle.Letter
import com.mine.helpfle.R

class GridAdapter(context : Context, letterList : ArrayList<Letter>) : ArrayAdapter<Letter> (context, 0, letterList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.view_table_cell, parent, false)
        }

        val letter = getItem(position)!!

        val cellView = listItemView!!.findViewById<CardView>(R.id.cv_table_cell)
        val cellText = listItemView.findViewById<TextView>(R.id.tv_table_cell)

        val bgColor = context.resources.getColor(letter.backgroundColor(), context.theme)
        val txtColor = context.resources.getColor(letter.textColor(), context.theme)

        letter.anim.getAnimation(listItemView, position % 5, bgColor, txtColor).also {
            it.addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    cellView.background.setTint(bgColor)
                    cellText.setTextColor(txtColor)
                    cellText.text = letter.letter
                }
            })
            it.start()
        }

        return listItemView
    }
}

