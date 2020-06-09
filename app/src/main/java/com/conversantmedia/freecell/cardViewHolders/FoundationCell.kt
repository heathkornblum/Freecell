/**
 *
 *  FoundationCell.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.cardViewHolders

import android.content.Context
import android.graphics.*
import androidx.constraintlayout.solver.widgets.Rectangle
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.conversantmedia.freecell.game.tools.FreeCellTools
import com.conversantmedia.freecell.card.PlayingCard
import com.conversantmedia.freecell.game.tools.FreeCellObjectTools
import com.conversantmedia.freecell.R


class FoundationCell : FrameLayout, CardGroup, Iterable<PlayingCard> {

    /*
     * Foundation cells are a one-way transaction, can only have cards added.
     * The exception to that rule is during a back move.
     */

    constructor(_context: Context): super(_context)
    constructor(_context: Context, attributeSet: AttributeSet): super(_context, attributeSet)
    private val TAG = this.javaClass.simpleName
    private val tools = FreeCellTools()

    private val cards = ArrayList<PlayingCard>()
    val textPaint: Paint

    lateinit var rectangle: Rectangle

    private val outRect = Rect()
    private val location = IntArray(2)

    /*
     * Looking for an Ace or a card whose suit matches and whose rank is one higher than what's
     * showing
     * Also, if this card comes from the middle of a tableau column, reject the drop
     */
    override fun validateDrop(card: PlayingCard, originParent: CardGroup) : Boolean? {
        val cardShowing: PlayingCard? = cardShowing()
        if ( cardShowing == null && card.getRank() == 1) return true
        else if (cardShowing != null) {
            if (cardShowing.getSuit() != card.getSuit()) return false
            if (cardShowing.getRank() != card.getRank() -1 ) return false
            return true
        } else {
            return false
        }
    }

    fun cardShowing(): PlayingCard? = getChildAt(childCount - 1) as PlayingCard?


    init {
        setWillNotDraw(false)
        val cardRectangle = tools.getVersionDrawable(this, R.drawable.emptycell, "clear", true)
        background = cardRectangle

        textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#666666")
            style = Paint.Style.FILL_AND_STROKE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }


    }

    /*
     * Add card to the foundation cell, removing any existing top margin put in place by the Tableau Column
     */
    override fun addCard(card: PlayingCard): Boolean? {
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParams.topMargin = 0
        this.addView(card, layoutParams)
        // cards added to this location are no longer
        // draggable lest exceptions be thrown
        card.setOnTouchListener(null)
        cards.add(card)
        if (tools.checkVictory(this.parent as View)) {
            tools.celebrate(this.parent as View)
        }
        return true
    }

    override fun removeCard(card: PlayingCard): PlayingCard {
        this.removeView(card)
        cards.remove(card)
        return card
    }

    override fun iterator(): Iterator<PlayingCard> {
        return cards.iterator()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val centerX = width / 2F
        val yPos = (height * 0.7F)
        textPaint.textSize  = width*0.9F
        canvas?.drawText("A", centerX, yPos, textPaint)
    }

    override fun cardInBounds(card: PlayingCard, x: Int, y: Int): Boolean {
        this.getDrawingRect(outRect)
        this.getLocationOnScreen(location)
        outRect.offset(location[0], location[1])
        outRect.set(outRect.left - this.width.div(2), outRect.top - this.height.div(2),outRect.right + this.width.div(2), outRect.bottom + this.height.div(2))
        return outRect.contains(x, y)
    }

    override fun highlightDropZone(card: PlayingCard, originParent: CardGroup) {
        if (this.childCount == 0 && card.getRank() == 1) {
            this.background = tools.getVersionDrawable(this, R.drawable.playing_card_green_light_freecell, "green", true)
            FreeCellObjectTools.bleached = false
        } else {
            val cardShowing: PlayingCard? = cardShowing()
                if (cardShowing?.getSuit() == card.getSuit() && cardShowing?.getRank() == card.getRank() -1 ) {
                cardShowing?.background = tools.getVersionDrawable(cardShowing, R.drawable.playing_card_green_light_target, "green", true)
            }
        }
    }

}