package se.umu.johe6327.asteroids

import android.os.Parcelable
import android.os.Parcel
import java.util.UUID

/**
 *  Parcelable laser object.
 */

data class Laser(
    val id: String = UUID.randomUUID().toString(),
    var x: Float,
    var y: Float,
    var speed: Int
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
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