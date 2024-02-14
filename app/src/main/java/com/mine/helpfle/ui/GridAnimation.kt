package com.mine.helpfle.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ObjectAnimator.ofFloat
import android.animation.ObjectAnimator.ofInt
import android.animation.ValueAnimator
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.mine.helpfle.R

const val TAG_GRIDANIMATION = "GRIDANIMATION"

abstract class GridAnimation(var onAnimationComplete: () -> Unit) {

    abstract fun getAnimation(listItemView: View, rowIdx : Int, bgColor: Int, txtColor: Int) : AnimatorSet

}

class NoAnim(onAnimationComplete: () -> Unit) : GridAnimation(onAnimationComplete) {
    override fun getAnimation(listItemView: View, rowIdx: Int, bgColor: Int, txtColor: Int): AnimatorSet {

        return AnimatorSet().apply {
            this.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onAnimationComplete
                }
            })
        }
    }
}

class Wiggle(onAnimationComplete: () -> Unit) : GridAnimation(onAnimationComplete) {

    override fun getAnimation(listItemView: View, rowIdx : Int, bgColor: Int, txtColor: Int) : AnimatorSet {
        val cellView = listItemView.findViewById<CardView>(R.id.cv_table_cell)

        val rotate = AnimatorSet().apply {
            playSequentially(
                ofFloat(cellView, "rotation", -5F, 5F).apply {
                    duration = 50
                    repeatCount = 4
                    repeatMode = ObjectAnimator.REVERSE
                },
                ofFloat(cellView, "rotation", -5F, 0F).apply {
                    duration = 10
                }
            )
        }

        val translate = AnimatorSet().apply {
            playSequentially(
                ofFloat(cellView, "translationX", -10F, 10F).apply {
                    duration = 50
                    repeatCount = 4
                    repeatMode = ObjectAnimator.REVERSE
                },
                ofFloat(cellView, "translationX", -10F, 0F).apply {
                    duration = 10
                }
            )
        }

        return AnimatorSet().apply {
            interpolator = LinearInterpolator()
            playTogether(rotate, translate)
            this.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onAnimationComplete
                }
            })
        }
    }
}

class Flip(onAnimationComplete: () -> Unit) : GridAnimation(onAnimationComplete) {

    override fun getAnimation(listItemView: View, rowIdx : Int, bgColor: Int, txtColor: Int) : AnimatorSet {

        Log.v(TAG_GRIDANIMATION, "getAnimation Flip(position=$rowIdx)")

        val cellView = listItemView.findViewById<CardView>(R.id.cv_table_cell)
        val txtView = listItemView.findViewById<TextView>(R.id.tv_table_cell)
        val dur = 300L

        val scale = AnimatorSet().apply {
            interpolator = LinearInterpolator()
            startDelay = 50 + rowIdx * (dur - 30)
            playSequentially(
                ofFloat(listItemView, "scaleX", 1F, 0F).apply {
                    duration = dur
                },
                ofFloat(listItemView, "scaleX", 0F, 1F).apply {
                    duration = dur
                }
            )
        }

        val bgTint = AnimatorSet().apply {
            startDelay = 100 + rowIdx * (dur - 30)
            playSequentially(
                ValueAnimator.ofObject(
                    ArgbEvaluator(),
                    cellView.backgroundTintList!!.defaultColor,
                    bgColor
                ).also { va ->
                    va.duration = dur - 100
                    va.addUpdateListener {
                        val value = it.animatedValue as Int
                        cellView.background.setTint(value)
                    }
                    va.interpolator = AccelerateDecelerateInterpolator()
                }
            )
        }

        val textColor = AnimatorSet().apply {
            startDelay = rowIdx * (dur - 30)
            playSequentially(
                ofInt(txtView, "textColor", txtView.textColors.defaultColor, txtView.textColors.defaultColor).apply {
                    duration = dur
                },
                ofInt(txtView, "textColor", txtColor, txtColor).apply {
                    duration = dur
                }
            )
        }

        val textAlpha = AnimatorSet().apply {
            playSequentially(
                ofFloat(txtView, "alpha", 0F, 0F).apply {
                    duration = dur
                },
                ofFloat(txtView, "alpha", 0F, 1F).apply {
                    startDelay = rowIdx * (dur - 30)
                    duration = 50
                }
            )
        }

        return AnimatorSet().apply {
            Log.v(TAG_TABLE_ACTIVITY, "position = $rowIdx, startDelay = $startDelay")
            playTogether(scale, bgTint, textColor, textAlpha)
            this.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    Log.d(TAG_GRIDANIMATION, "onAnimationComplete")
                    onAnimationComplete()
                }
            })
        }

    }
}
