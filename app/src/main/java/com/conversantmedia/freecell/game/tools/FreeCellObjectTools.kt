/**
 *
 *  FreeCellObjectTools.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.game.tools

import android.view.animation.AccelerateDecelerateInterpolator
import com.conversantmedia.freecell.card.CardMove
import com.conversantmedia.freecell.game.ParcelableDeck

object FreeCellObjectTools {

    // starting with 4 free cells
    var emptyFreeCells: Int = 4

    // flag to tell when game is done dealing initial cards, set to false by Game.deal()
    var dealing = true

    // Empty Tableau Columns
    var emptyColumns: Int = 8

    // cards are white?
    var bleached = false

    var autoPlay = true
    var parcelableDeck = ParcelableDeck()
    var moves = ArrayList<CardMove>()
    var forwardMoves = ArrayList<CardMove>()

    var animationDuration = 500L
    val maxAutoPlayDuration = 2000L
    val animatorInterpolator = AccelerateDecelerateInterpolator()

    val maxHighlightValue = 255
    var highlightTint: Int = 155

    var vibratePref = true

    var actionBarHeight = 29

    var gameScore = 0

    var theme = 24

}