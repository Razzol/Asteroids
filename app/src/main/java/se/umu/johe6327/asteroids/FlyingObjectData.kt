package se.umu.johe6327.asteroids

import android.content.res.Resources
import kotlin.random.Random

/**
 * This class contains data for all the flying objects in the game.
 */

class FlyingObjectData {
    /**
     * Laser object data
     */
    val laserWidth = 30
    val laserHeight = 45
    val laserSpeed = 15

    /**
     * Alien object data
     */
    val scoreAlien = 200
    val alienWidth = 250
    val alienHeight = 150
    val alienX = (Resources.getSystem().displayMetrics.widthPixels/2).toFloat() -120
    val alienY = 150f
    val alienSpeedY = 7
    val alienSpeedX = 10
    val rightBorder = (Resources.getSystem().displayMetrics.widthPixels) - alienWidth.toFloat()
    val leftBorder = 0.toFloat()

    /**
     * Asteroid object data
     */
    val scoreAsteroid = 10
    val asteroidWidth = Random.nextInt(100,300)  // In pixels
    val asteroidsHeight = Random.nextInt(100,300)  // In pixels
    val asteroidX = Random.nextFloat()*(Resources.getSystem().displayMetrics.widthPixels-200)
    val asteroidY = 150f
    val deltaY = 12
}