package se.umu.johe6327.asteroids

import android.os.Parcel
import android.os.Parcelable

/**
 * Asteroid objects parameters and methods
 */
data class Asteroid(
    var width: Int,
    var height: Int,
    var x: Float,
    var y: Float,
    var speed: Int
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(width)
        parcel.writeInt(height)
        parcel.writeFloat(x)
        parcel.writeFloat(y)
        parcel.writeInt(speed)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Asteroid> {
        override fun createFromParcel(parcel: Parcel): Asteroid {
            return Asteroid(parcel)
        }

        override fun newArray(size: Int): Array<Asteroid?> {
            return arrayOfNulls(size)
        }
    }
}