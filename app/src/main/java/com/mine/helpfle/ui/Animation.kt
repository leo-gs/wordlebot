package com.mine.helpfle.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ObjectAnimator.ofFloat
import android.view.View
import android.view.animation.LinearInterpolator

class Animation {

    companion object {

        fun wiggle(view: View): AnimatorSet {
            val rotate = AnimatorSet().apply {
                playSequentially(
                    ofFloat(view, "rotation", -5F, 5F).apply {
                        duration = 50
                        repeatCount = 4
                        repeatMode = ObjectAnimator.REVERSE
                    },
                    ofFloat(view, "rotation", -5F, 0F).apply {
                        duration = 10
                    }
                )
            }

            val translate = AnimatorSet().apply {
                playSequentially(
                    ofFloat(view, "translationX", -10F, 10F).apply {
                        duration = 50
                        repeatCount = 4
                        repeatMode = ObjectAnimator.REVERSE
                    },
                    ofFloat(view, "translationX", -10F, 0F).apply {
                        duration = 10
                    }
                )
            }

            return AnimatorSet().apply {
                interpolator = LinearInterpolator()
                playTogether(rotate, translate)
            }
        }

        fun flip(view: View, offset : Int) : AnimatorSet {

            val scale = AnimatorSet().apply {
                interpolator = LinearInterpolator()
                playSequentially(
                    ofFloat(view, "scaleX", 1F, 0F).apply {
                        duration = 200
                    },
                    ofFloat(view, "scaleX", 0F, 1F).apply {
                        duration = 200
                    }
                )
            }

            val translate = AnimatorSet().apply {
                playSequentially(
                    ofFloat(view, "translationX", 0F, view.width * 0.5F).apply {
                        duration = 200
                    },
                    ofFloat(view, "translationX", view.width * 0.5F, 0F).apply {
                        duration = 200
                    }
                )
            }

//            val bgChange = AnimatorSet().apply {
//                ObjectAnimator.ofInt(view, "backgroundColor", R.color.state_in_word).apply {
//                    duration = 100
//                    startDelay = 100
//                }
//            }
// TODO: delay background color change so it syncs with card flip animation
            return AnimatorSet().apply {
                startDelay = offset * 190L
                playTogether(scale)
            }

        }
    }

}