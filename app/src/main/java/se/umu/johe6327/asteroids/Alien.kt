package se.umu.johe6327.asteroids

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.widget.ImageView
import androidx.annotation.RequiresApi

/**
 * Alien objects parameters and methods.
 */

data class Alien(
    var x: Float,
    var y: Float,
    var deltaX: Int,
    var deltaY: Int,
    val rightBorder: Float,
    val leftBorder: Float,
    var wallR: Boolean,
    var wallL: Boolean

) : Parcelable {

    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readBoolean(),
        parcel.readBoolean()
    )

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(x)
        parcel.writeFloat(y)
        parcel.writeInt(deltaX)
        parcel.writeInt(deltaY)
        parcel.writeFloat(rightBorder)
        parcel.writeFloat(leftBorder)
        parcel.writeBoolean(wallR)
        parcel.writeBoolean(wallL)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Alien> {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): Alien {
            return Alien(parcel)
        }

        override fun newArray(size: Int): Array<Alien?> {
            return arrayOfNulls(size)
        }
    }

    fun movement(alien: ImageView, currentAliens: MutableList<Alien>, index: Int) {
        if (index >= 0 && index < currentAliens.size) {
            alien.translationY += deltaY
            currentAliens[index].y += deltaY

            fun moveRight(){
                alien.translationX += deltaX
                currentAliens[index].x += deltaX
            }
            fun moveLeft(){
                alien.translationX -= deltaX
                currentAliens[index].x -= deltaX
            }
            if (!wallL) {
                moveLeft()
                if (alien.translationX == leftBorder){
                    wallL = true
                    wallR = false
                }
            }
            if (!wallR) {
                moveRight()
                if (alien.translationX == rightBorder){
                    wallR = true
                    wallL = false
                }
            }
        }

    }
}
