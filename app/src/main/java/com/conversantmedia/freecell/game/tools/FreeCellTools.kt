/**
 *
 *  FreeCellTools.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.game.tools

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import androidx.constraintlayout.widget.ConstraintLayout
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.widget.Button
import android.widget.FrameLayout
import com.conversantmedia.freecell.activities.MenuActivity
import com.conversantmedia.freecell.card.*
import com.conversantmedia.freecell.cardViewHolders.CardGroup
import com.conversantmedia.freecell.cardViewHolders.FoundationCell
import com.conversantmedia.freecell.cardViewHolders.FreeCell
import com.conversantmedia.freecell.cardViewHolders.TableauColumn
import com.conversantmedia.freecell.R
import com.conversantmedia.freecell.game.*
import conversant.tagmanager.sdk.CNVRTagManager
import conversant.tagmanager.sdk.CNVRTagSyncEvent
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlin.collections.ArrayList
import java.util.Random

class FreeCellTools {
    private val TAG = this.javaClass.simpleName

    private var movesSize = 0L
    val foundations = ArrayList<FoundationCell>()
    val freecells = ArrayList<FreeCell>()
    val tableau = ArrayList<TableauColumn>()
    private val freeCellCards = ArrayList<PlayingCard>()
    private val blanks = ArrayList<EmptyCell>()

    constructor()

    /*
     * How many cards can be moved?
     */
    fun maxMoves(): Int {
        if (FreeCellObjectTools.emptyColumns == 0) {
            return FreeCellObjectTools.emptyFreeCells + 1
        } else {
            return (FreeCellObjectTools.emptyColumns * 2) * (FreeCellObjectTools.emptyFreeCells + 1)
        }
    }

    /*
     * Start MenuActivity
     * @layout is a view parent used for its Context
     */
    fun openMenu(layout: View) {
        val intent = Intent(layout.context, MenuActivity::class.java)
        layout.context.startActivity(intent)
    }

    fun disableAllCards(layout: View) {
        gatherAssets(layout)
        for (tableauColumn in tableau) {
            for (card in tableauColumn) {
                card?.setOnTouchListener(null)
            }
        }
        for (freecell in freecells) {
            freecell.getChildAt(0)?.setOnTouchListener(null)
        }
    }

    fun enableAllCards(layout: View) {
        val touchListener = CardTouchListener(layout.context)
        gatherAssets(layout)
        for (tableauColumn in tableau) {
            for (card in tableauColumn) {
                card?.setOnTouchListener(touchListener)
            }
        }
        for (freecell in freecells) {
            freecell.getChildAt(0)?.setOnTouchListener(touchListener)
        }
    }
    /*
     * Prevents buttons from being touched during events such as animation
     * @view is a view parent used for its Context
     */
    fun disableUI(view: View) {
        view.backButton.setOnClickListener(null)
        view.resetButton.setOnClickListener(null)
        view.forwardButton.setOnClickListener(null)
        view.button_menu.setOnClickListener { null }
    }

    /*
     * At the end of an event such as animation, turn buttons back on
     * @layout is a view parent used for its Context
     */
    fun enableUI(layout: View) {
        layout.backButton.setOnClickListener {
            goBack(layout)
        }
        layout.resetButton.setOnClickListener {
            resetGame(layout)
        }
        layout.forwardButton.setOnClickListener {
            goForward(layout)
        }
        layout.button_menu.setOnClickListener {
            openMenu(layout)
        }
        if (layout.victory.visibility == View.VISIBLE) {
            animateAwayVictoryButton(layout)
        }
    }

    /*
     * Animate Away Victory Button
     */
    private fun animateAwayVictoryButton(layout: View) {
        layout.victory.animate()
                .setDuration(1000L)
                .alpha(0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        layout.victory.visibility = View.GONE
                    }
                })
    }

    /*
     * Moves a card or group of cards forward one move, animated
     * Single card moves are treated differently than group card moves
     * @layout is a view parent used for its Context
     */
    fun goForward(layout: View) {
        clean(layout)
        if (!FreeCellObjectTools.forwardMoves.isEmpty()) {
            disableUI(layout)
            var move = FreeCellObjectTools.forwardMoves[FreeCellObjectTools.forwardMoves.size - 1]
            var groupSize = move.groupSize
            var fromStart = 0
            var toStart = 0
            var xDistance = 0
            var yDistance = 0
            var originalSize = 0
            var cardPos = 0
            var fromLocation = IntArray(2)
            var toLocation = IntArray(2)
            val listOfAnimatorSets = ArrayList<AnimatorSet>()
            val groupAnimatorSet = AnimatorSet()
            if (groupSize == 1) {

                val toView = layout.findViewById<ViewGroup>(move.toId)
                val fromView = layout.findViewById<ViewGroup>(move.fromId)
                fromView.bringToFront()

                fromView.getLocationOnScreen(fromLocation)
                toView.getLocationOnScreen(toLocation)

                xDistance = toLocation[0] - fromLocation[0]
                yDistance = toLocation[1] - fromLocation[1]

                fromStart = fromView.childCount
                toStart = toView.childCount

                if (toView !is TableauColumn) {
                    yDistance -= (fromView.childCount - 1) * layout.resources.getDimension(R.dimen.topMarginConst).toInt()

                } else if (fromView !is TableauColumn) {
                    yDistance += (toView.childCount) * layout.resources.getDimension(R.dimen.topMarginConst).toInt()
                } else {
                    yDistance = (toStart - fromStart + 1) * layout.resources.getDimension(R.dimen.topMarginConst).toInt()
                }

                val card = fromView.getChildAt(fromView.childCount - 1) as PlayingCard
                val newCard = PlayingCard(card.getSuitObj(), card.getRank(), layout.context)
                val xObjectAnimator = ObjectAnimator.ofFloat(card, "translationX", xDistance.toFloat())
                val yObjectAnimator = ObjectAnimator.ofFloat(card, "translationY", yDistance.toFloat())
                val animatorSet = AnimatorSet()
                animatorSet.playTogether(xObjectAnimator, yObjectAnimator)
                animatorSet.duration = FreeCellObjectTools.animationDuration
                animatorSet.interpolator = FreeCellObjectTools.animatorInterpolator
                animatorSet.start()
                animatorSet.addListener(object : Animator.AnimatorListener{
                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        moveCard(card, fromView as CardGroup, toView as CardGroup, 1, 0, true, newCard)
                        enableUI(layout)
                    }

                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationStart(animation: Animator?) {

                    }
                })
                FreeCellObjectTools.forwardMoves.remove(move)
            } else {
                while (groupSize > 0) {
                    move = FreeCellObjectTools.forwardMoves[FreeCellObjectTools.forwardMoves.size - groupSize]
                    val toId = move.toId
                    val fromId = move.fromId
                    val toView = layout.findViewById<ViewGroup>(toId)
                    val fromView = layout.findViewById<ViewGroup>(fromId)
                    fromView.bringToFront()

                    fromStart = fromView.childCount - groupSize
                    originalSize = fromView.childCount
                    cardPos++
                    toStart = toView.childCount - 1 + cardPos
                    originalSize += cardPos
                    val card = fromView.getChildAt(fromView.childCount - groupSize--) as? PlayingCard

                    fromView.getLocationOnScreen(fromLocation)
                    toView.getLocationOnScreen(toLocation)

                    xDistance = toLocation[0] - fromLocation[0]
                    yDistance = (toStart - fromStart) * layout.resources.getDimension(R.dimen.topMarginConst).toInt()


                    card?.let {
                        val xObjectAnimator = ObjectAnimator.ofFloat(card, "translationX", xDistance.toFloat())
                        val yObjectAnimator = ObjectAnimator.ofFloat(card, "translationY", yDistance.toFloat())

                        val animatorSet = AnimatorSet()
                        animatorSet.duration = FreeCellObjectTools.animationDuration
                        animatorSet.playTogether(xObjectAnimator, yObjectAnimator)

                        val newCard = PlayingCard(card.getSuitObj(), card.getRank(), layout.context)

                        animatorSet.addListener(object : Animator.AnimatorListener{
                            override fun onAnimationCancel(animation: Animator?) {
                            }
                            override fun onAnimationEnd(animation: Animator?) {
                                moveCard(card,
                                        fromView as CardGroup, toView as CardGroup, move.groupSize, 0, true, newCard)
                            }
                            override fun onAnimationRepeat(animation: Animator?) {
                            }
                            override fun onAnimationStart(animation: Animator?) {
                            }

                        })
                        listOfAnimatorSets.add(animatorSet)

                        FreeCellObjectTools.forwardMoves.remove(move)
                    }


                }

                groupAnimatorSet.playTogether(listOfAnimatorSets as MutableCollection<Animator>)
                groupAnimatorSet.duration = FreeCellObjectTools.animationDuration
                groupAnimatorSet.interpolator = FreeCellObjectTools.animatorInterpolator
                groupAnimatorSet.addListener(object : Animator.AnimatorListener{
                    override fun onAnimationCancel(animation: Animator?) {
                    }
                    override fun onAnimationEnd(animation: Animator?) {
                        enableUI(layout)
                    }
                    override fun onAnimationRepeat(animation: Animator?) {
                    }
                    override fun onAnimationStart(animation: Animator?) {
                    }
                })
                groupAnimatorSet.start()
            }

        }
        arrows(layout)
    }

    /*
     * Go back one move, animated
     * @layout is a view parent used for its Context
     */
    fun goBack(layout: View) {
        clean(layout)
        if (FreeCellObjectTools.moves.size > 0) {
            var move = FreeCellObjectTools.moves[FreeCellObjectTools.moves.size - 1]
            val animateBack = animatedBackMove(move, layout)
            animateBack.interpolator = FreeCellObjectTools.animatorInterpolator
            animateBack.addListener(object : Animator.AnimatorListener{
                override fun onAnimationStart(animation: Animator?) {
                    disableUI(layout)
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    enableUI(layout)
                    updateScore(1, layout)

                }
            } )
            animateBack.start()
        }
    }

    /*
     * Back move animation. Single card moves are treated differently than group card moves.
     * @returns AnimatorSet
     * @move is a CardMove object
     * @layout is a view parent used for its Context
     */
    private fun animatedBackMove(move: CardMove, layout: View): AnimatorSet {
        var theMove = move
        var groupSize = move.groupSize
        var originalSize = 0
        var cardPos = 0
        var toStart = 0
        var fromStart = 0
        val groupAnimatorSet = AnimatorSet()
        var listOfAnimatorSets = ArrayList<AnimatorSet>()
        movesSize = if (movesSize < FreeCellObjectTools.moves.size) {
            FreeCellObjectTools.moves.size.toLong()
        } else {
            movesSize
        }

        if (groupSize > 1) {
            while (groupSize > 0) {
                theMove = FreeCellObjectTools.moves[FreeCellObjectTools.moves.size - groupSize]
                val toId = theMove.toId
                val fromId = theMove.fromId

                val fromView = layout.findViewById<ViewGroup>(fromId)
                val toView = layout.findViewById<ViewGroup>(toId)
                toView.bringToFront()
                toStart = toView.childCount - groupSize
                originalSize = toView.childCount
                cardPos++
                fromStart = fromView.childCount - 1 + cardPos
                originalSize += cardPos
                val card = toView.getChildAt(toView.childCount - groupSize) as PlayingCard
                groupSize--

                val fromLocation = IntArray(2)
                fromView.getLocationOnScreen(fromLocation)
                val toLocation = IntArray(2)
                toView.getLocationOnScreen(toLocation)

                val xDistance = fromLocation[0] - toLocation[0]
                val yDistance = (fromStart - toStart) * layout.resources.getDimension(R.dimen.topMarginConst).toInt()

                val xObjectAnimator = ObjectAnimator.ofFloat(card, "translationX", xDistance.toFloat())
                val yObjectAnimator = ObjectAnimator.ofFloat(card, "translationY", yDistance.toFloat())

                val animatorSet = AnimatorSet()

                animatorSet.duration = FreeCellObjectTools.animationDuration
                animatorSet.interpolator = FreeCellObjectTools.animatorInterpolator

                animatorSet.playTogether(listOf(xObjectAnimator, yObjectAnimator))
                val newCard = PlayingCard(card.getSuitObj(), card.getRank(), layout.context)
                animatorSet.addListener(object : Animator.AnimatorListener{
                    override fun onAnimationEnd(animation: Animator?) {

                        moveCard(card,
                                toView as CardGroup, fromView as CardGroup, groupSize, 0, false, newCard)

                        FreeCellObjectTools.bleached = false
                        clean(layout)
                    }
                    override fun onAnimationRepeat(animation: Animator?) {
                    }
                    override fun onAnimationCancel(animation: Animator?) {
                    }
                    override fun onAnimationStart(animation: Animator?) {
                    }
                })
                listOfAnimatorSets.add(animatorSet)

                FreeCellObjectTools.forwardMoves.add(theMove)
                FreeCellObjectTools.moves.remove(theMove)
            }
            groupAnimatorSet.playTogether(listOfAnimatorSets as MutableList<Animator>)
            return groupAnimatorSet
        } else {
            val toId = theMove.toId
            val fromId = theMove.fromId
            val fromView = layout.findViewById<ViewGroup>(fromId)
            val toView = layout.findViewById<ViewGroup>(toId)
            toView.bringToFront()
            val card = toView.getChildAt(toView.childCount - 1) as PlayingCard

            val fromLocation = IntArray(2)
            fromView.getLocationOnScreen(fromLocation)
            val toLocation = IntArray(2)
            toView.getLocationOnScreen(toLocation)

            val xDistance = fromLocation[0] - toLocation[0]
            var yDistance = fromLocation[1] - toLocation[1]
            if (toView !is TableauColumn) {
                yDistance += fromView.childCount * layout.resources.getDimension(R.dimen.topMarginConst).toInt()
            } else if (theMove.groupSize == 1) {
                yDistance -= (toView.childCount - fromView.childCount - 1) * layout.resources.getDimension(R.dimen.topMarginConst).toInt()
            }

            val xObjectAnimator = ObjectAnimator.ofFloat(card, "translationX", xDistance.toFloat())
            val yObjectAnimator = ObjectAnimator.ofFloat(card, "translationY", yDistance.toFloat())
            val animatorSet = AnimatorSet()

            animatorSet.interpolator = FreeCellObjectTools.animatorInterpolator
            animatorSet.duration = FreeCellObjectTools.animationDuration

            animatorSet.playTogether(listOf(xObjectAnimator, yObjectAnimator))
            val newCard = PlayingCard(card.getSuitObj(), card.getRank(), layout.context)
            animatorSet.addListener(object : Animator.AnimatorListener{
                override fun onAnimationEnd(animation: Animator?) {

                    moveCard(card,
                            toView as CardGroup, fromView as CardGroup, theMove.groupSize, 0, false, newCard)

                    FreeCellObjectTools.bleached = false
                    clean(layout)
                }
                override fun onAnimationRepeat(animation: Animator?) {
                }
                override fun onAnimationCancel(animation: Animator?) {
                }
                override fun onAnimationStart(animation: Animator?) {
                }
            })
            FreeCellObjectTools.forwardMoves.add(theMove)
            FreeCellObjectTools.moves.remove(theMove)
            return animatorSet
        }

    }

    /*
     * Exploding cards animation, also used for game reset
     * @layout is a view parent used for its Context
     */
    fun dealAnimation(layout: View) {
        for (column in tableau) {

            for (card in column) {
                column.bringToFront()
                val random = Random()
                var randomInt = Random().nextInt()
                var posNeg = if (randomInt % 2 > 0) { -1 } else { 1 }
                val xObjectAnimator = ObjectAnimator.ofFloat(card, "translationX", (posNeg.toFloat()) * random.nextFloat()* 500)
                randomInt = Random().nextInt()
                posNeg = if (randomInt % 2 > 0) { -1 } else { 1 }
                val yObjectAnimator = ObjectAnimator.ofFloat(card, "translationY", (posNeg.toFloat()) * random.nextFloat()* 500)

                xObjectAnimator.duration = 1500
                xObjectAnimator.reverse()
                yObjectAnimator.duration = 1500
                yObjectAnimator.reverse()

                xObjectAnimator.interpolator = OvershootInterpolator()
                yObjectAnimator.interpolator = OvershootInterpolator()

                xObjectAnimator.addListener(object : Animator.AnimatorListener{
                    override fun onAnimationStart(animation: Animator?) {
                        disableUI(layout)
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        enableUI(layout)
                    }

                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        enableUI(layout)
                    }
                })



            }
        }
    }

    /*
     * utility for autoPlay()
     * @foundationCell is a FoundationCell, CardGroup
     * @card is a Playing Card
     * @returns true if the card being compared to foundation is okay to autoplay move into
     * the foundation cell, false otherwise
     *
     */
    private fun compareFoundationToCard(foundationCell: FoundationCell, card: PlayingCard) : Boolean{
        if (foundationCell.cardShowing() == null) {
            if (card.getRank() == 1) {
                return true
            }
        } else if (card.getSuit() == (foundationCell.cardShowing() as PlayingCard).getSuit()) {
            if (card.getRank() == (foundationCell.cardShowing() as PlayingCard).getRank() + 1) {
                return true

            }
        }
        return false
    }

    /*
     * Utility to pull all CardGroup assets together for various play
     * @layout is a view parent used for its Context
     */
    fun gatherAssets(layout : View) {
        if (foundations.isEmpty()) {
            foundations.add(layout.foundationCell1)
            foundations.add(layout.foundationCell2)
            foundations.add(layout.foundationCell3)
            foundations.add(layout.foundationCell4)
        }
        if (freecells.isEmpty()) {
            freecells.add(layout.freecell1)
            freecells.add(layout.freecell2)
            freecells.add(layout.freecell3)
            freecells.add(layout.freecell4)
        }
        if (tableau.isEmpty()) {
            tableau.add(layout.tableauColumn1)
            tableau.add(layout.tableauColumn2)
            tableau.add(layout.tableauColumn3)
            tableau.add(layout.tableauColumn4)
            tableau.add(layout.tableauColumn5)
            tableau.add(layout.tableauColumn6)
            tableau.add(layout.tableauColumn7)
            tableau.add(layout.tableauColumn8)
        }

    }


    /*
     * The blanks are the card images at the back of a tableau column
     * @layout is a view parent used for its Context
     */
    private fun gatherBlanks(layout: View) {
        if (blanks.isEmpty()) {
            blanks.add(layout.emptyCell1)
            blanks.add(layout.emptyCell2)
            blanks.add(layout.emptyCell3)
            blanks.add(layout.emptyCell4)
            blanks.add(layout.emptyCell5)
            blanks.add(layout.emptyCell6)
            blanks.add(layout.emptyCell7)
            blanks.add(layout.emptyCell8)
        }

    }

    /*
     * moves cards when some value can be added to the Foundation.
     * @layout is a view parent used for its Context
     */
    fun autoPlay(layout: View) {
        var done = false
        if (FreeCellObjectTools.autoPlay) {
            if (tableau.isEmpty()) {
                gatherAssets(layout)
            }

            val tableauColumnEnds = ArrayList<PlayingCard>()

            for (indexA in 0 until tableau.size) {
                val tableauColumn = tableau[indexA]
                if (tableauColumn.childCount > 0) {
                    tableauColumnEnds.add(tableauColumn.getChildAt(tableauColumn.childCount - 1) as PlayingCard)
                }
            }

            for (indexB in 0 until freecells.size) {
                if (freecells[indexB].childCount > 0) {
                    freeCellCards.add(freecells[indexB].getChildAt(0) as PlayingCard)
                }
            }

            for (i in 0 until foundations.size) {
                for (j in 0 until freeCellCards.size) {
                    if (freeCellCards[j].parent is FreeCell) {
                        if (compareFoundationToCard(foundations[i], freeCellCards[j])) {
                            // recursion happens in the animation listener here:
                            animateCardAutoplay(foundations[i], freeCellCards[j])
                            foundations[i].bringToFront()
                            moveCard(freeCellCards[j], freeCellCards[j].parent as CardGroup, foundations[i], 1, 1, true, null)

                        }
                    }
                }
                for (j in 0 until tableauColumnEnds.size) {
                    if (tableauColumnEnds[j].parent is TableauColumn) {
                        if (compareFoundationToCard(foundations[i], tableauColumnEnds[j])) {
                            // recursion happens in the animation listener here:
                            animateCardAutoplay(foundations[i], tableauColumnEnds[j])
                            foundations[i].bringToFront()
                            moveCard(tableauColumnEnds[j], tableauColumnEnds[j].parent as CardGroup, foundations[i], 1, 1, true, null)
                        }
                    }
                }
            }
        }
    }

    /*
     * utility for autoplay animation
     * @destination is where the card will be put
     * @card is the card being moved, also used for its starting location information
     */
    private fun animateCardAutoplay(destination: ViewGroup, card: PlayingCard) {
        val startLocation = IntArray(2)
        val endLocation = IntArray(2)
        val layout = destination.parent as View

        card.getLocationOnScreen(startLocation)
        destination.getLocationOnScreen(endLocation)

        val xMove = startLocation[0] - endLocation[0]
        val yMove = startLocation[1] - endLocation[1]

        val xAnimator = ObjectAnimator.ofFloat(card, "translationX", xMove.toFloat(), 0F)
        val yAnimator = ObjectAnimator.ofFloat(card, "translationY", yMove.toFloat(), 0F)

        val animatorSet = AnimatorSet()
        animatorSet.let {
            it.playTogether(xAnimator, yAnimator)
            it.duration = FreeCellObjectTools.animationDuration
            it.interpolator = FreeCellObjectTools.animatorInterpolator
            it.addListener(object : Animator.AnimatorListener{
                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    FreeCellObjectTools.bleached = false
                    clean(layout)
                    enableAllCards(layout)
                    autoPlay(layout)
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    disableAllCards(layout)
                }
            })
            it.start()
        }

    }



    /*
     * move card from one CardGroup to another
     * @card card being moved
     * @from starting CardGroup
     * @to ending CardGroup
     * @groupSize is this card being moved as part of a stack?
     * @isAutoPlay a value to be used for Achievements in a later release
     * @doCount determines whether or not to count this among GameMoves
     * @newCard a copy of the card being moved.  This helps with animation
     */
    fun moveCard(card: PlayingCard?, from: CardGroup?, to: CardGroup?, groupSize: Int, isAutoPlay: Int, doCount: Boolean, newCard: PlayingCard?, shadowHolder: FrameLayout? = null, indexOfCard: Int? = null) {

        val index = if (indexOfCard != null)
            {
                indexOfCard
            } else {
                (from as ViewGroup).indexOfChild(card)
            }

        var moveCard: PlayingCard? = null


        if (shadowHolder == null) {
            moveCard = card?.let {
                from?.removeCard(it)
            }
        } else {
            shadowHolder.removeView(card)
            moveCard = card
        }


        newCard?.let{ to?.addCard(it) }

        if (newCard == null) {
            moveCard?.let{to?.addCard(it) }
        }

        from as ViewGroup?
        to as ViewGroup?

        if (from != null &&
                to != null ) {
            val gameMove = CardMove(to?.javaClass!!.simpleName, to.id, from?.javaClass!!.simpleName, from.id, index, groupSize, isAutoPlay, System.currentTimeMillis())
            if (!FreeCellObjectTools.dealing && doCount) FreeCellObjectTools.moves.add(gameMove)
            arrows(from.parent as View)
        }


    }

    fun updateScore(changeBy: Int, layout: View) {
        FreeCellObjectTools.gameScore += changeBy
        (layout as View).score.text = layout.resources.getString(R.string.score, FreeCellObjectTools.gameScore)
    }

    /*
     * turn cards white and empty cells blank
     * @layout is a view parent used for its Context
     */
    fun clean(layout: View) {
        val foundations = ArrayList<FoundationCell>()
        foundations.add(layout.findViewById(R.id.foundationCell1))
        foundations.add(layout.findViewById(R.id.foundationCell2))
        foundations.add(layout.findViewById(R.id.foundationCell3))
        foundations.add(layout.findViewById(R.id.foundationCell4))

        val freecells = ArrayList<FreeCell>()
        freecells.add(layout.findViewById(R.id.freecell1))
        freecells.add(layout.findViewById(R.id.freecell2))
        freecells.add(layout.findViewById(R.id.freecell3))
        freecells.add(layout.findViewById(R.id.freecell4))

        val tableau = arrayListOf<TableauColumn>(
                layout.tableauColumn1,
                layout.tableauColumn2,
                layout.tableauColumn3,
                layout.tableauColumn4,
                layout.tableauColumn5,
                layout.tableauColumn6,
                layout.tableauColumn7,
                layout.tableauColumn8
        )

        gatherBlanks(layout)

        for (column in tableau) {

            for (card in column) {
                card.background = getVersionDrawable(layout, R.drawable.playing_card, "white", true)
                card.visibility = View.VISIBLE
            }
        }
        for (cell in freecells) {
            if (cell.childCount > 0 ) {
                val childCard = cell.getChildAt(0) as PlayingCard
                childCard.background = getVersionDrawable(layout, R.drawable.playing_card, "white", true)
                childCard.visibility = View.VISIBLE
            } else {
                cell.background = getVersionDrawable(layout, R.drawable.emptycell, "clear", true)
            }

        }
        for (foundation in foundations) {
            foundation.background = getVersionDrawable(layout, R.drawable.emptycell, "clear", true)
            if (foundation.childCount > 0) {
                for (card in foundation) {
                    card.background =  getVersionDrawable(layout, R.drawable.playing_card, "white", true)
                    card.visibility = View.VISIBLE
                }
            }
        }
        for (blank in blanks) {
            blank.background = getVersionDrawable(layout, R.drawable.emptycell, "clear", true)
        }

        FreeCellObjectTools.bleached = true

    }


    /*
     * Utility for switching on back and forward arrows
     * @layout is a view parent used for its Context
     */
    fun arrows(layout: View) {
        if (FreeCellObjectTools.moves.isEmpty()) {
            layout.backButton.text = null
        } else {
            layout.backButton.text = "\u21b6"
        }

        if (FreeCellObjectTools.forwardMoves.isEmpty()) {
            layout.forwardButton.text = null
        } else {
            layout.forwardButton.text = "\u21b7"
        }
    }

    /*
     * uses API version based Drawable for cards and card images, handles color change animation
     * @viewItem a view, usually a PlayingCard
     * @drawableId is passed to getDrawable
     * @color a string description of what the intended color should be
     * @animate to animate or not to animate (usually to animate)
     */
    fun getVersionDrawable(viewItem: View, drawableId: Int, color: String, animate: Boolean): Drawable {

        // TODO: Eliminate colored card drawables in favor of a single playing card drawable:
        //        val drawableId = R.drawable.playing_card
        val tint: Int = FreeCellObjectTools.highlightTint

        val gradientDrawable: GradientDrawable
        val clearColor = android.graphics.Color.argb(0, 255, 255, 255)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            gradientDrawable = viewItem.resources.getDrawable(drawableId, null) as GradientDrawable
        } else {
            gradientDrawable = viewItem.resources.getDrawable(drawableId) as GradientDrawable
        }

        when(color) {
            "red" -> {

                animateColor(gradientDrawable, android.graphics.Color.argb(255,255, tint, tint), Color.WHITE, animate)
            }
            "green" -> {

                if (viewItem is FreeCell || viewItem is FoundationCell || viewItem is ConstraintLayout) {
                    animateColor(gradientDrawable, android.graphics.Color.argb(FreeCellObjectTools.maxHighlightValue - tint, tint, 255, tint), clearColor, true)
                } else {
                    animateColor(gradientDrawable, android.graphics.Color.rgb(tint, 255, tint), animate = true)
                }
            }
            "white" -> {
                animateColor(gradientDrawable, android.graphics.Color.argb(255, 255, 255, 255), animate = true)
            }
            "clear" -> {
                animateColor(gradientDrawable, clearColor, clearColor, true)
            }
            else -> {
                animateColor(gradientDrawable, android.graphics.Color.argb(255, 255, 255, 255), animate = true)
            }
        }

        return gradientDrawable
    }

    /*
     * card color animation
     * @target is a View, usually a card
     * @toColor the target color
     * @fromColor original color, usually white
     * @animate sure, animate
     */
    private fun animateColor(target: Any, toColor: Int, fromColor: Int = Color.WHITE, animate: Boolean) {
        target as GradientDrawable

        if (animate) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                val animateColor = ObjectAnimator.ofArgb(target as GradientDrawable, "color", fromColor, toColor)
                animateColor.duration = 300
                animateColor.start()
            } else {
                target.setColor(toColor)
            }
        } else {
            target.setColor(toColor)
        }


    }

    /*
     * Since the blanks are not part of the TableauColumn, they need to be colored on their own
     * @layout is a view parent used for its Context
     */
    fun highlightEmptyColumns(layout: ConstraintLayout) {
        gatherBlanks(layout)
        for (blank in blanks) {
            blank.background = getVersionDrawable(layout, R.drawable.emptycell, "green", true)
        }
    }

    fun deal(shuffledDeck: Deck, layout: View): ParcelableDeck {
        FreeCellObjectTools.dealing = true
        val parcelableDeck = ParcelableDeck()
        gatherAssets(layout)
        while (!shuffledDeck.isEmpty()) {
            for (index in 0 until tableau.size) {
                val tableauColumn: TableauColumn = tableau[index]
                if (!shuffledDeck.isEmpty()) {
                    val card = shuffledDeck.removeAt(0)
                    val cardValues = CardValues(card.getSuit(), card.getRank())
                    parcelableDeck.add(cardValues)
                    // creating a parcelable deck for game restore
                    tableauColumn.addCard(card)
                }
                if (tableauColumn.equals(tableau[tableau.size - 1])) break
            }
        }
        FreeCellObjectTools.dealing = false
        return parcelableDeck
    }

    /*
     * give up, but don't start a new game
     * @layout is a view parent used for its Context
     */
    fun resetGame(layout: View) {
        if (!FreeCellObjectTools.moves.isEmpty()) {
            val deckCopy = FreeCellObjectTools.parcelableDeck.clone()
            clearGame(layout)
            gatherAssets(layout)
            val game = Game(layout.context, Deck(deckCopy as ParcelableDeck, layout.context), tableau)
            game.deal()
            dealAnimation(layout)
            arrows(layout)
            FreeCellObjectTools.bleached = false
            clean(layout)
            FreeCellObjectTools.gameScore = 0
            updateScore(0, layout)
        }
    }

    /*
     * reset the board and prepare for new or loaded game info
     * @layout is a view parent used for its Context
     */
    fun clearGame(layout: View) {
        clean(layout)
        if (tableau.isEmpty()) {
            gatherAssets(layout)
        }
        for (freecell in freecells) {
            freecell.removeAllViews()
        }
        for (foundation in foundations) {
            foundation.removeAllViews()
        }
        for (tableauColumn in tableau) {
            tableauColumn.removeAllViews()
        }

        FreeCellObjectTools.moves.clear()
        FreeCellObjectTools.parcelableDeck.clear()
        FreeCellObjectTools.forwardMoves.clear()
        FreeCellObjectTools.emptyColumns = 8
        FreeCellObjectTools.emptyFreeCells = 4
        FreeCellObjectTools.gameScore = 0
        layout.score.text = layout.resources.getString(R.string.score, 0)
    }

    /*
     * Did I win???
     * @layout is a view parent used for its Context
     */
    fun checkVictory(layout: View): Boolean {
        // start with a positive outlook
        var victory = true
        gatherAssets(layout)
        for (column in tableau) {
            if (column.childCount > 0) {
                return false
            }
        }
        for (freecell in freecells) {
            if (freecell.childCount > 0 ) {
                return false
            }
        }
        val event = CNVRTagSyncEvent.Builder(arrayOf("gameWin"), "freecell/game").build()
        CNVRTagManager.getSdk().fireEvent(event)
        return true
    }

    /*
     * show the You Win! button which starts a new game when touched
     * @layout is a view parent used for its Context
     */
    fun celebrate(layout: View) {

        layout.victory as Button
        layout.victory.apply {
            visibility = View.VISIBLE
            alpha = 0f
            animate()
                    .alpha(1f)
                    .setDuration(1000L)
                    .setListener(null)
        }

        layout.victory.visibility = View.VISIBLE

    }


    fun highlightDropZones(layout: View, card: PlayingCard, originParent: CardGroup, columnSize: Int) {
        gatherAssets(layout)
        for (tableauColumns in tableau) {
            tableauColumns.highlightDropZone(card, originParent)
        }
        if (columnSize == 1) {
            for (freecell in freecells) {
                freecell.highlightDropZone(card, originParent)
            }
            for (foundation in foundations) {
                foundation.highlightDropZone(card, originParent)
            }
        }
    }

    /*
     * Create a pattern of four grey suits, used in some CardGroup backgrounds
     * @canvas a canvas on which to paint
     * @cardWidth good to know
     * @paint Paint
     * @resources things like pixel density
     */
    fun fourSuits(canvas: Canvas, cardWidth: Int, paint: Paint, resources: Resources) {
        val height = (cardWidth.toFloat() * 1.4).toInt()
        val spade: Bitmap = scaledGreySuit(cardWidth, Suit.SPADE, resources)
        val diamond: Bitmap = scaledGreySuit(cardWidth, Suit.DIAMOND, resources)
        val heart: Bitmap = scaledGreySuit(cardWidth, Suit.HEART, resources)
        val club: Bitmap = scaledGreySuit(cardWidth, Suit.CLUB, resources)
        canvas.drawBitmap(spade, ((cardWidth/2) - spade.width).toFloat(), height * 0.1F, paint)
        canvas.drawBitmap(diamond, (cardWidth/2).toFloat(), height * 0.1F, paint)
        canvas.drawBitmap(heart, ((cardWidth/2) - spade.width).toFloat(), height * 0.6F, paint)
        canvas.drawBitmap(club, (cardWidth/2).toFloat(), height * 0.6F, paint)
    }

    /*
     * @return a card suit image
     * @position Top or Center
     * @card the card which needs a suit
     * @resources things like density
     */
    fun scaledSuit(position: Char, card: PlayingCard, resources: Resources) : Bitmap {
        val size: Float = if (position == 'c') {
            card.width * .8F
        } else {
            card.width * .3F
        }

        val suit = (card).getSuitObj()
        val drawableInt: Int = when (suit) {
            Suit.DIAMOND -> R.drawable.diamond
            Suit.SPADE -> R.drawable.spade
            Suit.HEART -> R.drawable.heart
            Suit.CLUB -> R.drawable.club
        }
        val suitImg = BitmapFactory.decodeResource(resources, drawableInt)
        return Bitmap.createScaledBitmap(suitImg, size.toInt(), size.toInt(), true)
    }

    /*
     * @return a bitmap representing a grey suit based on the needs of fourSuits()
     * @width good to know
     * @suit which suit?
     * @resources things like density
     */
    fun scaledGreySuit(width: Int, suit: Suit, resources: Resources) : Bitmap {
        val size: Float = width * 0.5F
        val drawableInt: Int = when (suit) {
            Suit.DIAMOND -> R.drawable.greydiamond
            Suit.SPADE -> R.drawable.greyspade
            Suit.HEART -> R.drawable.greyheart
            Suit.CLUB -> R.drawable.greyclub
        }
        val suitImg = BitmapFactory.decodeResource(resources, drawableInt)
        return Bitmap.createScaledBitmap(suitImg, size.toInt(), size.toInt(), true)
    }

    /*
     * Called at the start of an activity
     */
    fun loadTheme(context: Context, themeId: Int = 24) {
        context as Activity
        when (themeId) {
            0 -> {
                context.setTheme(R.style.DippedConeTheme)
            }
            1 -> {
                context.setTheme(R.style.HotFudgeSundaeTheme)
            }
            2 -> {
                context.setTheme(R.style.BostonCreamPieTheme)
            }
            3 -> {
                context.setTheme(R.style.BlackForestCakeTheme)
            }
            4 -> {
                context.setTheme(R.style.TiramisuTheme)
            }
            5 -> {
                context.setTheme(R.style.CherriesJubileeTheme)
            }
            6 -> {
                context.setTheme(R.style.StrawberryShortcakeTheme)
            }
            7 -> {
                context.setTheme(R.style.CarrotCakeTheme)
            }
            8 -> {
                context.setTheme(R.style.PumpkinPieTheme)
            }
            9 -> {
                context.setTheme(R.style.CreamsicleTheme)
            }
            10 -> {
                context.setTheme(R.style.LemonMeringuePieTheme)
            }
            11 -> {
                context.setTheme(R.style.LemonBarTheme)
            }
            12 -> {
                context.setTheme(R.style.KeyLimePieTheme)
            }
            13 -> {
                context.setTheme(R.style.MintChipTheme)
            }
            14 -> {
                context.setTheme(R.style.CandyAppleTheme)
            }
            15 -> {
                context.setTheme(R.style.KiwiTheme)
            }
            16 -> {
                context.setTheme(R.style.BombPopTheme)
            }
            17 -> {
                context.setTheme(R.style.BlueberryParfaitTheme)
            }
            18 -> {
                context.setTheme(R.style.BerryCobblerTheme)
            }
            19 -> {
                context.setTheme(R.style.CottonCandyTheme)
            }
            20 -> {
                context.setTheme(R.style.SMoresTheme)
            }
            21 -> {
                context.setTheme(R.style.CherryCheesecakeTheme)
            }
            22 -> {
                context.setTheme(R.style.BirthdayCakeTheme)
            }
            23 -> {
                context.setTheme(R.style.CookiesNCreamTheme)
            }
            24 -> {
                context.setTheme(R.style.LicoriceTheme)
            }
            else -> {
                context.setTheme(R.style.LicoriceTheme)
            }
        }
    }
}


