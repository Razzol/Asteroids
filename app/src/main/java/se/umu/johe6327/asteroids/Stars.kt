package se.umu.johe6327.asteroids

import android.graphics.drawable.AnimationDrawable
import android.widget.ImageView

/**
 * Controls the animation of the star imageview.
 */
class Stars(
    starOne: ImageView,
    starTwo: ImageView,
    starThree: ImageView,
    starFour: ImageView,
    starFive: ImageView,
    starSix: ImageView,
    starSeven: ImageView,
    starEight: ImageView,
    starNine: ImageView,
    starTen: ImageView
) {
    // Get the background, which has been compiled to an AnimationDrawable object.
    private val starOneAnimation = starOne.background as AnimationDrawable
    private val starTwoAnimation = starTwo.background as AnimationDrawable
    private val starThreeAnimation = starThree.background as AnimationDrawable
    private val starFourAnimation = starFour.background as AnimationDrawable
    private val starFiveAnimation = starFive.background as AnimationDrawable
    private val starSixAnimation = starSix.background as AnimationDrawable
    private val starSevenAnimation = starSeven.background as AnimationDrawable
    private val starEightAnimation = starEight.background as AnimationDrawable
    private val starNineAnimation = starNine.background as AnimationDrawable
    private val starTenAnimation = starTen.background as AnimationDrawable
    // Start the animation (looped playback by default).
    fun start(){
        starOneAnimation.start()
        starTwoAnimation.start()
        starThreeAnimation.start()
        starFourAnimation.start()
        starFiveAnimation.start()
        starSixAnimation.start()
        starSevenAnimation.start()
        starEightAnimation.start()
        starNineAnimation.start()
        starTenAnimation.start()
    }

}