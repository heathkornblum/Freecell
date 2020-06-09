/**
 *
 *  EmptyCell.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.card

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.res.ResourcesCompat
import android.util.AttributeSet
import android.view.View
import com.conversantmedia.freecell.game.tools.FreeCellTools
import com.conversantmedia.freecell.R

class EmptyCell: View {
    constructor(_context: Context) :
            this(_context, null)
    constructor(_context: Context, _attributeSet: AttributeSet?) :
            this (_context, _attributeSet, 0)
    constructor(_context: Context, _attributeSet: AttributeSet?, _defStyleAttr: Int) :
            super(_context, _attributeSet, _defStyleAttr) {
        val cardRectangle = ResourcesCompat.getDrawable(resources, R.drawable.emptycell, null)

        background = cardRectangle
    }

    /*
     * These values are set by onMeasure() and used by onDraw
     */
    var parentWidth = 0
    var cardHeight = 0
    val tools = FreeCellTools()
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        parentWidth = MeasureSpec.getSize(widthMeasureSpec) + paddingLeft + paddingRight
        cardHeight = (parentWidth.toFloat() * 1.4).toInt()
        setMeasuredDimension(parentWidth, cardHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            tools.fourSuits(canvas, parentWidth, paint, resources)
        }
    }
}