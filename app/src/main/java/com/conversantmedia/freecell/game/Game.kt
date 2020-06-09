/**
 *
 *  Game.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.game

import android.content.Context
import com.conversantmedia.freecell.card.CardValues
import com.conversantmedia.freecell.cardViewHolders.TableauColumn
import com.conversantmedia.freecell.game.tools.FreeCellObjectTools

class Game(val context : Context, val deck: Deck, val tableau: ArrayList<TableauColumn>) {
    
    private val TAG = this.javaClass.simpleName


    /*
     * Prepare a parcelable deck in parallel to the game deck for state restore
     */
    val parcelableDeck = ParcelableDeck()

    /*
     * deal() -- Deal cards into 8 tableau columns.  The cards are dealt 1 per column
     * until the deck is empty.
     */
    fun deal() {
        FreeCellObjectTools.dealing = true
        while (!deck.isEmpty()) {
            for (index in 0 until tableau.size) {
                val tableauColumn: TableauColumn = tableau[index]
                if (!deck.isEmpty()) {
                    val card = deck.removeAt(0)
                    val cardValues = CardValues(card.getSuit(), card.getRank())
                    // creating a parcelable deck for game restore
                    parcelableDeck.add(cardValues)
                    tableauColumn.addCard(card)
                }
                if (tableauColumn.equals(tableau[tableau.size - 1])) break
            }
        }
        FreeCellObjectTools.parcelableDeck = parcelableDeck
        FreeCellObjectTools.dealing = false
    }

}