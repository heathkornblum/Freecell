/**
 *
 *  TableauColumn.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.cardViewHolders

import android.content.Context
import android.os.Vibrator
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.conversantmedia.freecell.card.CardTouchListener
import com.conversantmedia.freecell.card.PlayingCard
import com.conversantmedia.freecell.game.tools.FreeCellObjectTools
import com.conversantmedia.freecell.game.tools.FreeCellTools
import com.conversantmedia.freecell.R
import android.graphics.Rect
import android.util.Log
import android.view.ViewGroup


class TableauColumn: FrameLayout, CardGroup, Iterable<PlayingCard> {
    constructor(_context: Context): this(_context, null)
    constructor(_context: Context, attributeSet: AttributeSet?): super(_context, attributeSet) {

    }

    val cardList = ArrayList<PlayingCard>()

    var layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT)

    private val TAG = this.javaClass.simpleName

    val touchListener = CardTouchListener(context)
    private val tools = FreeCellTools()
    private val outRect = Rect()
    private val location = IntArray(2)

    init {
        this.setLayoutParams(layoutParams)
        setWillNotDraw(false)
        associateBlank()
    }

    private fun associateBlank() {

    }

    override fun iterator(): Iterator<PlayingCard> {
        return cardList.iterator()
    }

    override fun validateDrop(card: PlayingCard, originParent: CardGroup): Boolean? {
        if (originParent == this) {
            return true
        }
        if (this.childCount > 0) {
            val bottomChild = this.getChildAt(this.childCount - 1) as PlayingCard
            return (bottomChild.color() != card.color()
                    && bottomChild.getRank() == card.getRank() + 1)

        } else if (originParent is TableauColumn){
            (this.parent as View).findViewById<FrameLayout>(R.id.shadowHolder).childCount
            val carryCount = (this.parent as View).findViewById<FrameLayout>(R.id.shadowHolder).childCount

            // account for the fact that when a column is entirely picked up, it's empty column should not affect the
            // empty column count, but it does.  This helps with that.
            var maxMoves = tools.maxMoves()
            if (originParent.childCount == 0) {
                maxMoves /= 2
            }
            if (maxMoves/2 < (carryCount)) {
                return false
            }
        }
        return true
    }

    override fun addCard(card: PlayingCard): Boolean? {

        if (childCount == 0) {
            FreeCellObjectTools.emptyColumns--
        }

        val marginConstant = resources.getDimension(R.dimen.topMarginConst).toInt() //20 * metrics.density).toInt()
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        params.topMargin = (this.childCount) * marginConstant


        card.setOnTouchListener(touchListener)

        addView(card, params)
        card.bringToFront()
        cardList.add(card)
        return true
    }

    override fun removeCard(card: PlayingCard): PlayingCard {
        this.removeView(card)
        if (this.childCount > 0) {
            val remainingChild = this.getChildAt(childCount - 1)
            remainingChild.setOnTouchListener(touchListener)
        }
        if (childCount == 0) {
            FreeCellObjectTools.emptyColumns++
        }

        cardList.remove(card)
        return card
    }

    override fun cardInBounds(card: PlayingCard, x: Int, y: Int): Boolean {
        this.getDrawingRect(outRect)
        this.getLocationOnScreen(location)
        outRect.offset(location[0], location[1])
        outRect.set(outRect.left - this.width.div(2), outRect.top,outRect.right + this.width.div(2), outRect.bottom)
        return outRect.contains(x, y)
    }

    fun compareCards(upperCard: PlayingCard, lowerCard: PlayingCard): Boolean = (upperCard.color() != lowerCard.color() && upperCard.getRank() == lowerCard.getRank() + 1)

    override fun highlightDropZone(card: PlayingCard, originParent: CardGroup) {
        if (originParent == this) {
            for (index in 0 .. this.childCount - 2) {
                val compareableCard = this.getChildAt(index) as? PlayingCard
                compareableCard?.let {
                    if (compareCards(it, card)) {
                        it.background = tools.getVersionDrawable(it, R.drawable.playing_card_red_light, "red", true)

                    }
                }
            }
            val bottomCard = this.getChildAt(this.childCount - 1) as? PlayingCard
            bottomCard?.let {
                if (compareCards(it, card)) {
                    it.background = tools.getVersionDrawable(it, R.drawable.playing_card_green_light, "green", true)
                }
            }
        } else {
            val bottomCard = this.getChildAt(this.childCount - 1) as? PlayingCard
            bottomCard?.let {
                if (compareCards(it, card)) {
                    for (pCard in this) {
                        pCard.background = tools.getVersionDrawable(pCard, R.drawable.playing_card_green_light, "green", true)
                    }
                } else {
                    for (pCard in this) {
                        if (compareCards(pCard, card)) {
                            pCard.background = tools.getVersionDrawable(pCard, R.drawable.playing_card_red_light, "red", true)
                        }
                    }
                }
            }
        }
        val carryCount = (this.parent as View).findViewById<FrameLayout>(R.id.shadowHolder).childCount
        var maxMoves = tools.maxMoves()
        if ((originParent as ViewGroup).childCount == 0) {
            maxMoves /= 2
        }
        if (maxMoves/2 >= (carryCount)) {
            tools.highlightEmptyColumns(this.parent as ConstraintLayout)
        }
    }
}