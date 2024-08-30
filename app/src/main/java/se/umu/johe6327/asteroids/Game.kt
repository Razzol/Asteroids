package se.umu.johe6327.asteroids

import android.graphics.Rect
import android.widget.ImageView

/**
 * Helping class that checks collisions between views.
 */
class Game {
    // Checks collision between laser and flying object
    fun laserCheck(laser: ImageView, flyingObject: ImageView): Boolean {
        val rect1 = Rect()
        val rect2 = Rect()
        var returnBoolean = false

        laser.getHitRect(rect1)
        flyingObject.getHitRect(rect2)
        if(rect1.intersect(rect2)){
            returnBoolean = true
        }
        return returnBoolean
    }

    // Checks collision between ship and asteroid
    fun collidesWithShip(img: ImageView, shipImageView: ImageView) : Boolean{
        val rect1 = Rect()
        val rect2 = Rect()

        img.getHitRect(rect1)
        shipImageView.getHitRect(rect2)

        return rect1.intersect(rect2)
    }
}