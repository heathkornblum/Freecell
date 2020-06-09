/**
 *
 *  CardTouchListener.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.card

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.conversantmedia.freecell.cardViewHolders.CardGroup
import com.conversantmedia.freecell.cardViewHolders.TableauColumn
import com.conversantmedia.freecell.game.tools.FreeCellObjectTools
import com.conversantmedia.freecell.game.tools.FreeCellTools
import com.conversantmedia.freecell.R
import com.conversantmedia.freecell.cardViewHolders.FreeCell


class CardTouchListener(context: Context): View.OnTouchListener {

    private val TAG = this.javaClass.simpleName
    val tools = FreeCellTools()
    val column = ArrayList<PlayingCard>()
    var originalParent: ViewGroup? = null
    val thisListener = this

    /*
     * This determines whether a card can be dragged.
     * @return true if draggable, else false
     */

    override fun onTouch(playingCard: View?, event: MotionEvent?): Boolean {
        playingCard as PlayingCard?
        val shadowHolder = (playingCard?.parent?.parent as View).findViewById<FrameLayout>(R.id.shadowHolder)
        val layout = shadowHolder.parent
        val coords = IntArray(2)
        // this compensates for the nav bar and action bar offsets
        (layout as View).getLocationOnScreen(coords)

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {


                // special treatment for group moves, otherwise get to it
                playingCard?.let {
                    if (it.parent !is CardGroup) {
                        return false
                    }
                    originalParent = it.parent as ViewGroup

                    when(originalParent) {
                        is TableauColumn -> {
                            val cardParent = it.parent as? TableauColumn
                            cardParent?.let {
                                if (cardParent.childCount > cardParent.indexOfChild(playingCard) + 1) {
                                    // investigate color and rank order
                                    var index = cardParent?.indexOfChild(playingCard)
                                    var looperCard = playingCard as PlayingCard
                                    val movingCount = cardParent.childCount - index
                                    // if there are not enough free cells, cancel this action
                                    Log.d(TAG, "max moves: ${tools.maxMoves()}")
                                    if (movingCount > tools.maxMoves()) return false
                                    column.add(playingCard)
                                    while (index < cardParent.childCount + 1) {
                                        val nextCard = cardParent.getChildAt(++index)

                                        if (nextCard != null && nextCard !is EmptyCell) {
                                            nextCard as PlayingCard
                                            if (nextCard.color() != looperCard.color() &&
                                                    nextCard.getRank() == looperCard.getRank() - 1) {
                                                looperCard = nextCard
                                                column.add(looperCard)
                                            } else {
                                                tools.clean(cardParent.parent as View)
                                                column.clear()
                                                return false
                                            }

                                        }
                                    }

                                } else {
                                    column.add(playingCard)
                                }
                            }
                        }

                        is FreeCell -> {
                            column.add(it as PlayingCard)
                        }

                        else -> {
                            column.clear()
                            return false
                        }
                    }


                    if (!column.isEmpty()) {
                        shadowHolder.visibility = View.VISIBLE
                        shadowHolder.parent.bringChildToFront(shadowHolder)
                        var topMarginHeight = 0
                        val topMarginConst = (shadowHolder.parent as View).resources.getDimension(R.dimen.topMarginConst)
                        for (card in column) {
                            if (card.parent is CardGroup) {
                                (card.parent as CardGroup).removeCard(card)
                                val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                                params.topMargin = topMarginHeight
                                topMarginHeight += topMarginConst.toInt()
                                shadowHolder.addView(card, params)
                                shadowHolder.x = event.rawX - coords[0] - it.width.div(2)
                                shadowHolder.y = event.rawY - coords[1] - it.height.div(6).times(5)
                            }

                        }
                    }
                    tools.highlightDropZones(layout as View, it as PlayingCard, originalParent as CardGroup, column.size)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                playingCard?.let {
                    shadowHolder.x = event.rawX - coords[0] - it.width.div(2)
                    shadowHolder.y = event.rawY - coords[1] - it.height.div(6).times(5)
                }

            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                playingCard?.let {
                    tools.disableAllCards(layout)
                    var dropOK = false
                    val cardIndexSeed = if (originalParent is FreeCell) {
                        0
                    } else {
                        (originalParent as ViewGroup).childCount
                    }
                    tools.gatherAssets(layout as View)
                    for (tableauColumn in tools.tableau) {
                        if (tableauColumn.cardInBounds(it as PlayingCard, event.rawX.toInt(), event.rawY.toInt()) &&
                                        tableauColumn.validateDrop(it, originalParent as CardGroup) == true && tableauColumn != originalParent) {
                            val endingX = (tableauColumn as View).x
                            var endingY = (tableauColumn as View).y
                            endingY += (tableauColumn.childCount * layout.resources.getDimension(R.dimen.topMarginConst)).toFloat()
                            if (shadowHolder.childCount > 0) {
                                shadowHolder.animate()
                                        .translationX(endingX)
                                        .translationY(endingY)
                                        .setDuration(800L)
                                        .setInterpolator(FreeCellObjectTools.animatorInterpolator)
                                        .setListener(object : AnimatorListenerAdapter() {
                                            override fun onAnimationStart(animation: Animator?) {
                                                for (index in 0 until shadowHolder.childCount) {
                                                    shadowHolder.getChildAt(index).setOnTouchListener(null)

                                                }
                                            }

                                            override fun onAnimationEnd(animation: Animator?) {
                                                val groupSize = shadowHolder.childCount
                                                while (shadowHolder.childCount > 0) {
                                                    tools.moveCard(shadowHolder.getChildAt(0) as PlayingCard, originalParent as? CardGroup, tableauColumn, groupSize, 0, true, null, shadowHolder, cardIndexSeed)
                                                }
                                                tools.enableAllCards(layout)
                                                shadowHolder.visibility = View.GONE
                                                tools.updateScore(2, layout)
                                                tools.autoPlay(layout)
                                            }
                                        })
                                dropOK = true
                            }
                        }
                    }
                    for (freecell in tools.freecells) {
                        if (freecell.cardInBounds(it as PlayingCard, event.rawX.toInt(), event.rawY.toInt()) &&
                                freecell.validateDrop(it, originalParent as CardGroup) == true &&
                                column.size == 1 && freecell != originalParent) {

                            shadowHolder.animate()
                                    .translationX(freecell.x)
                                    .translationY(freecell.y)
                                    .setDuration(800L)
                                    .setInterpolator(FreeCellObjectTools.animatorInterpolator)
                                    .setListener(object : AnimatorListenerAdapter() {
                                        override fun onAnimationStart(animation: Animator?) {
                                            it.setOnTouchListener(null)
                                        }
                                        override fun onAnimationEnd(animation: Animator?) {
                                            tools.moveCard(it, originalParent as? CardGroup, freecell, 1, 0, true, null, shadowHolder, cardIndexSeed)
                                            tools.enableAllCards(layout)
                                            shadowHolder.visibility = View.GONE
                                            tools.autoPlay(layout)
                                            tools.updateScore(2, layout)
                                        }
                                    })
                            dropOK = true

                        }
                    }
                    for (foundationCell in tools.foundations) {
                        if (foundationCell.cardInBounds(it as PlayingCard, event.rawX.toInt(), event.rawY.toInt()) &&
                                foundationCell.validateDrop(it, originalParent as CardGroup) == true &&
                                column.size == 1) {
                            shadowHolder.animate()
                                    .translationX(foundationCell.x)
                                    .translationY(foundationCell.y)
                                    .setDuration(800L)
                                    .setInterpolator(FreeCellObjectTools.animatorInterpolator)
                                    .setListener(object : AnimatorListenerAdapter() {
                                        override fun onAnimationStart(animation: Animator?) {
                                            it.setOnTouchListener(null)
                                        }
                                        override fun onAnimationEnd(animation: Animator?) {
                                            tools.moveCard(it, originalParent as? CardGroup, foundationCell, 1, 0, true, null, shadowHolder, cardIndexSeed)
                                            tools.enableAllCards(layout)
                                            shadowHolder.visibility = View.GONE
                                            tools.autoPlay(layout)
                                        }
                                    })
                            dropOK = true
                        }
                    }
                    if (!dropOK) {
                        val endingX = (originalParent as View).x
                        var endingY = (originalParent as View).y
                        if (originalParent is TableauColumn) {
                            endingY += ((originalParent as TableauColumn).childCount * layout.resources.getDimension(R.dimen.topMarginConst)).toFloat()
                        }
                        if (shadowHolder.childCount > 0) {
                            shadowHolder.animate()
                                    .translationX(endingX)
                                    .translationY(endingY)
                                    .setDuration(800L)
                                    .setInterpolator(FreeCellObjectTools.animatorInterpolator)
                                    .setListener(object : AnimatorListenerAdapter() {
                                        override fun onAnimationStart(animation: Animator?) {
                                            for (index in 0 until shadowHolder.childCount) {
                                                shadowHolder.getChildAt(index).setOnTouchListener(null)

                                            }
                                        }
                                        override fun onAnimationEnd(animation: Animator?) {
                                            while (shadowHolder.childCount > 0) {
                                                val moveChild = shadowHolder.getChildAt(0)
                                                shadowHolder.removeView(moveChild)
                                                (originalParent as CardGroup).addCard(moveChild as PlayingCard)
                                            }
                                            tools.enableAllCards(layout)

                                            shadowHolder.visibility = View.GONE
                                        }
                                    })
                        }
                    }
                    tools.clean(layout)
                    if (dropOK) {
                        FreeCellObjectTools.forwardMoves.clear()
                        tools.arrows(layout)
                    }
                    column.clear()

                }
            }



        }

        return true
    }
}