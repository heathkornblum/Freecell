/**
 *
 *  ParcelableDeck.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.game

import android.os.Parcel
import android.os.Parcelable
import com.conversantmedia.freecell.card.CardValues
import java.io.Serializable

class ParcelableDeck() : ArrayList<CardValues>(), Parcelable, Serializable {
    private val TAG = this.javaClass.simpleName


    constructor(parcel: Parcel) : this() {

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeArray(this.toArray())
    }

    override fun describeContents(): Int {
        return this.size
    }

    companion object CREATOR : Parcelable.Creator<ParcelableDeck> {
        override fun createFromParcel(parcel: Parcel): ParcelableDeck {
            return ParcelableDeck(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableDeck?> {
            return arrayOfNulls(size)
        }
    }
}

