package se.umu.johe6327.asteroids

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.widget.ImageView
import androidx.annotation.RequiresApi
import java.util.UUID

/**
 * Alien objects parameters and methods.
 */

data class Alien(
    val id: String = UUID.randomUUID().toString(),
    var x: Float,
    var y: Float,
    var deltaX: Int,
    var deltaY: Int,
    val rightBorder: Float,
    val leftBorder: Float,
    var movingRight: Boolean
) : Parcelable {

    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readBoolean()
    )

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeFloat(x)
        parcel.writeFloat(y)
        parcel.writeInt(deltaX)
        parcel.writeInt(deltaY)
        parcel.writeFloat(rightBorder)
        parcel.writeFloat(leftBorder)
        parcel.writeBoolean(movingRight)
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
}
