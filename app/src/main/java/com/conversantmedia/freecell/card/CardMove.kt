/**
 *
 *  CardMove.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.card

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

/*
  * a serialized, parceled card move
  */
data class CardMove(
        val toType: String?,
        val toId: Int,
        val fromType: String?,
        val fromId: Int,
        val fromIndex: Int,
        val groupSize: Int,
        val autoPlay: Int,
        val timeMillis: Long
) : Parcelable, Serializable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readLong()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(toType)
        parcel.writeInt(toId)
        parcel.writeString(fromType)
        parcel.writeInt(fromId)
        parcel.writeInt(fromIndex)
        parcel.writeInt(groupSize)
        parcel.writeInt(autoPlay)
        parcel.writeLong(timeMillis)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CardMove> {
        override fun createFromParcel(parcel: Parcel): CardMove {
            return CardMove(parcel)
        }

        override fun newArray(size: Int): Array<CardMove?> {
            return arrayOfNulls(size)
        }
    }
}
