/**
 *
 *  FreeCell.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.cardViewHolders

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.conversantmedia.freecell.*
import com.conversantmedia.freecell.card.CardTouchListener
import com.conversantmedia.freecell.card.PlayingCard
import com.conversantmedia.freecell.game.tools.FreeCellObjectTools
import com.conversantmedia.freecell.game.tools.FreeCellTools

class FreeCell: RelativeLayout, CardGroup {

    private val TAG = this.javaClass.simpleName

    constructor(_context: Context) : super(_context)
    constructor(_context: Context, attributeSet: AttributeSet) : super(_context, attributeSet)

    private val tools = FreeCellTools()
    val touchListener = CardTouchListener(context)
    lateinit var paint: Paint

    private val outRect = Rect()
    private val location = IntArray(2)

    init {
        setWillNotDraw(false)
        val cardRectangle = tools.getVersionDrawable(this, R.drawable.emptycell, "clear", true)
        background = cardRectangle
        paint = Paint(Paint.ANTI_ALIAS_FLAG)

    }

    override fun validateDrop(card: PlayingCard, originParent: CardGroup) : Boolean? {
        return (this.childCount == 0)
    }


    override fun addCard(card: PlayingCard): Boolean? {

        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParams.topMargin = 0
        this.addView(card, layoutParams)
        FreeCellObjectTools.emptyFreeCells--
        card.setOnTouchListener(touchListener)

        return true
    }

    override fun removeCard(card: PlayingCard): PlayingCard {
        this.removeView(card)
        FreeCellObjectTools.emptyFreeCells++
        return card
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            tools.fourSuits(canvas, width, paint, resources)
        }

    }

    override fun cardInBounds(card: PlayingCard, x: Int, y: Int): Boolean {
        this.getDrawingRect(outRect)
        this.getLocationOnScreen(location)
        outRect.offset(location[0], location[1])
        outRect.set(outRect.left - this.width.div(2), outRect.top - this.height.div(2),outRect.right + this.width.div(2), outRect.bottom + this.height.div(2))
        return outRect.contains(x, y)
    }

    override fun highlightDropZone(card: PlayingCard, originParent: CardGroup) {
        if (this.childCount == 0) {
            this.background = tools.getVersionDrawable(this, R.drawable.playing_card_green_light_freecell, "green", true)
            FreeCellObjectTools.bleached = false
        } else {
            val myCard = this.getChildAt(0) as? PlayingCard
            myCard?.let {
                if (it.color() != card.color() && it.getRank() == card.getRank() + 1){
                    it.background = tools.getVersionDrawable(it, R.drawable.playing_card_red_light, "red", true)
                }
            }
        }
    }

}