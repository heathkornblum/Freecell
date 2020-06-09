/**
 *
 *  CardValues.kt
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
 * This is for parcelling the card without the View.
 */
data class CardValues(val suit: String, val rank: Int) : Parcelable, Serializable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(suit)
        parcel.writeInt(rank)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CardValues> {
        override fun createFromParcel(parcel: Parcel): CardValues {
            return CardValues(parcel)
        }

        override fun newArray(size: Int): Array<CardValues?> {
            return arrayOfNulls(size)
        }
    }
}

