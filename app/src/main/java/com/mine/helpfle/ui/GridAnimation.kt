package com.mine.helpfle.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ObjectAnimator.ofFloat
import android.animation.ValueAnimator
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
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

        val letterView = listItemView.findViewById<CardLetterTextView>(R.id.letter_view)
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
                    letterView.bgColor,
                    bgColor
                ).also { va ->
                    va.duration = dur - 100
                    va.addUpdateListener {
                        val newColor = it.animatedValue as Int
                        letterView.bgColor = newColor
                    }
                    va.interpolator = AccelerateDecelerateInterpolator()
                }
            )
        }

        val textCol = AnimatorSet().apply {
            startDelay = 100 + rowIdx * (dur - 30)
            playSequentially(
                ValueAnimator.ofObject(
                    ArgbEvaluator(),
                    letterView.textColor,
                    txtColor
                ).also { va ->
                    va.duration = dur - 100
                    va.addUpdateListener {
                        val newColor = it.animatedValue as Int
                        letterView.textColor = newColor
                    }
                    va.interpolator = AccelerateDecelerateInterpolator()
                }
            )
        }

//        val textColor = AnimatorSet().apply {
//            startDelay = rowIdx * (dur - 30)
//            playSequentially(
//                ofInt(letterView, "textColor", letterView.textView.textColors.defaultColor, txtColor).apply {
//                    duration = dur
//                },
//                ofInt(letterView, "textColor", txtColor, txtColor).apply {
//                    duration = dur
//                }
//            )
//        }

        val textAlpha = AnimatorSet().apply {
            playSequentially(
                ofFloat(letterView, "alpha", 0F, 0F).apply {
                    duration = dur
                },
                ofFloat(letterView, "alpha", 0F, 1F).apply {
                    startDelay = rowIdx * (dur - 30)
                    duration = 50
                }
            )
        }

        return AnimatorSet().apply {
            playTogether(scale, bgTint, textCol, textAlpha)
            this.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onAnimationComplete()
                }
            })
        }

    }
}
