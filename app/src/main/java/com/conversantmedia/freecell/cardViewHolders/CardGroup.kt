/**
 *
 *  CardGroup.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.cardViewHolders

import com.conversantmedia.freecell.card.PlayingCard

/*
 * All ViewGroups which directly hold cards are considered CardGroups
 */

interface CardGroup {
    fun addCard(card: PlayingCard) : Boolean?
    fun removeCard(card: PlayingCard) : PlayingCard?
    fun validateDrop(card: PlayingCard, originParent: CardGroup) : Boolean?
    fun cardInBounds(card: PlayingCard, x: Int, y: Int) : Boolean
    fun highlightDropZone(card: PlayingCard, originParent: CardGroup)
}