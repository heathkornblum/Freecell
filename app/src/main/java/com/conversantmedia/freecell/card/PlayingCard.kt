/**
 *
 *  PlayingCard.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.card

import android.content.Context
import android.graphics.*
import androidx.core.content.res.ResourcesCompat
import android.util.AttributeSet
import android.view.*
import com.conversantmedia.freecell.game.tools.FreeCellTools
import com.conversantmedia.freecell.R


class PlayingCard : View {

    constructor(_suit: Suit, _rank: Int, _context: Context) :
            this(_suit, _rank, _context, null)
    constructor(_suit: Suit, _rank: Int, _context: Context, _attributeSet: AttributeSet?) :
            this (_suit, _rank, _context, _attributeSet, 0)
    constructor(_suit: Suit, _rank: Int, _context: Context, _attributeSet: AttributeSet?, _defStyleAttr: Int) :
            super(_context, _attributeSet, _defStyleAttr) {
        suit = _suit
        rank = _rank

        val cardRectangle = ResourcesCompat.getDrawable(resources, R.drawable.playing_card, null)
        background = cardRectangle
    }

    private val TAG = this.javaClass.simpleName
    private val tools = FreeCellTools()

    private val suit: Suit
    private val rank : Int
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL_AND_STROKE
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }

    private val cornerSymbolPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        Color.BLACK
        style = Paint.Style.STROKE
        textAlign = Paint.Align.LEFT
    }

    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.BLACK
        style = Paint.Style.FILL_AND_STROKE
    }


    var cardHeight: Int = 0

    private var parentWidth: Int = 0

    fun getSuitObj(): Suit = this.suit

    fun getSuit(): String  = this.suit.name

    fun getRank(): Int = this.rank

    lateinit var cornerSuit: Bitmap
    lateinit var centerSuit: Bitmap

    fun color(): Char  = when (suit) {
            Suit.SPADE, Suit.CLUB -> 'b'
            Suit.DIAMOND, Suit.HEART -> 'r'
        }

    override fun toString(): String  = "Suit: $suit, Rank: $rank"

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val rankString = when (rank) {
            11 -> "J"
            12 -> "Q"
            13 -> "K"
            1 -> "A"
            else -> rank.toString()
        }

        cornerSuit = tools.scaledSuit('s', this, resources)
        centerSuit = tools.scaledSuit('c', this, resources)

        val centerXpos = ((parentWidth - centerSuit.width) / 2F)
        val centerYpos = (((cardHeight).toFloat() - centerSuit.height)  * .9F)
        val rightCornerX = parentWidth * 0.6F
        val rightCornerY = parentWidth * 0.05F
        val leftCornerX = parentWidth * 0.1F
        val leftCornerY = parentWidth * 0.3F

        textPaint.textSize = parentWidth * 0.3F


        if (canvas != null) {
            canvas.drawBitmap(cornerSuit, rightCornerX, rightCornerY, cornerSymbolPaint)
            canvas.drawText(rankString, leftCornerX, leftCornerY, textPaint)
            canvas.drawBitmap(centerSuit, centerXpos, centerYpos, centerPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        parentWidth = MeasureSpec.getSize(widthMeasureSpec) + paddingLeft + paddingRight
        cardHeight = (parentWidth.toFloat() * 1.4).toInt()
        setMeasuredDimension(parentWidth, cardHeight)
    }


}