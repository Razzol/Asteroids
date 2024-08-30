package se.umu.johe6327.asteroids

import android.os.Parcelable
import android.os.Parcel

/**
 *  Parcelable laser object.
 */

data class Laser(
    var x: Float,
    var y: Float,
    var speed: Int
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(x)
        parcel.writeFloat(y)
        parcel.writeInt(speed)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Laser> {
        override fun createFromParcel(parcel: Parcel): Laser {
            return Laser(parcel)
        }

        override fun newArray(size: Int): Array<Laser?> {
            return arrayOfNulls(size)
        }
    }
}