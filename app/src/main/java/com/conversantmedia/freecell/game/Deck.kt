/**
 *
 *  Deck.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.game

import android.content.Context
import android.view.View
import com.conversantmedia.freecell.card.CardValues
import com.conversantmedia.freecell.card.PlayingCard
import com.conversantmedia.freecell.card.Suit

class Deck() : ArrayList<PlayingCard>() {

    private val TAG = this.javaClass.simpleName

    private lateinit var _context: Context
    private var _parcelableDeck: ParcelableDeck? = null

    /*
     * Two constructors depending on whether this is a new game or
     * one being restored via ParcelableDeck
     */
    constructor(context: Context) : this() {
        _context = context


        for (suit in Suit.values()) {
            var cardId = 0
            when (suit) {
                Suit.CLUB -> cardId = 300
                Suit.HEART -> cardId = 400
                Suit.SPADE -> cardId = 500
                Suit.DIAMOND -> cardId = 600
            }
            for (index in 1 .. 13) {
                val card = PlayingCard(suit, index, _context)
                card.id = cardId + index
                this.add(card)
            }
        }
    }

    constructor(parcelableDeck: ParcelableDeck, context: Context) : this() {
        _parcelableDeck = parcelableDeck
        _context = context

        if (_parcelableDeck != null) {

            for (index in 0 until _parcelableDeck!!.size) {
                val suit = when(_parcelableDeck!![index].suit) {
                    "DIAMOND" -> Suit.DIAMOND
                    "SPADE" -> Suit.SPADE
                    "CLUB" -> Suit.CLUB
                    "HEART" -> Suit.HEART
                    else -> Suit.CLUB
                }
                val rank = _parcelableDeck!![index].rank
                val card = PlayingCard(suit, rank, _context)
                card.id = View.generateViewId()
                this.add(card)
            }

        }
    }

    constructor(arrayDeck: ArrayList<CardValues>, context: Context) : this() {
        _context = context

        if (arrayDeck != null) {

            for (index in 0 until arrayDeck!!.size) {
                val suit = when(arrayDeck!![index].suit) {
                    "DIAMOND" -> Suit.DIAMOND
                    "SPADE" -> Suit.SPADE
                    "CLUB" -> Suit.CLUB
                    "HEART" -> Suit.HEART
                    else -> Suit.CLUB
                }
                val rank = arrayDeck!![index].rank
                val card = PlayingCard(suit, rank, _context)
                card.id = View.generateViewId()
                this.add(card)
            }

        }
    }

}