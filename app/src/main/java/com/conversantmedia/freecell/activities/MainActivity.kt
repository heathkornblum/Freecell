/**
 *
 *  MainActivity.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.conversantmedia.freecell.card.CardMove
import com.conversantmedia.freecell.card.CardValues
import com.conversantmedia.freecell.card.PlayingCard
import com.conversantmedia.freecell.cardViewHolders.CardGroup
import com.conversantmedia.freecell.cardViewHolders.FoundationCell
import com.conversantmedia.freecell.cardViewHolders.FreeCell
import com.conversantmedia.freecell.cardViewHolders.TableauColumn
import com.conversantmedia.freecell.game.*
import com.conversantmedia.freecell.game.tools.FreeCellObjectTools
import com.conversantmedia.freecell.game.tools.FreeCellTools
import com.conversantmedia.freecell.R
import com.conversantmedia.freecell.roomDBComponents.FreecellViewModel
import com.conversantmedia.freecell.roomDBComponents.PlayerPreferences
import com.conversantmedia.freecell.roomDBComponents.SavedGame
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import conversant.tagmanager.sdk.CNVRTagManager
import conversant.tagmanager.sdk.CNVRTagSyncEvent
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {


    private val DATE_FORMATTER = SimpleDateFormat("yyyyMMddHHmmss")

    private val TAG = this.javaClass.simpleName

    private lateinit var freecellViewModel: FreecellViewModel
    private var parcelableDeck = ParcelableDeck()

    lateinit var savedGame: SavedGame

    lateinit var deck: Deck

    lateinit var game: Game
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    var tableau: ArrayList<TableauColumn> = ArrayList()
    var intentExtra: Any? = null
    lateinit var prefs: SharedPreferences
    lateinit var thisContext: Context

    private var backMovesArrayList: ArrayList<CardMove> = ArrayList()
    private var forwardMovesArrayList: ArrayList<CardMove> = ArrayList()

    val tools = FreeCellTools()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tools.loadTheme(this, FreeCellObjectTools.theme)
        CNVRTagManager.initialize(this)
        setContentView(R.layout.activity_main)
        thisContext = this
        freecellViewModel = ViewModelProviders.of(this).get(FreecellViewModel::class.java)

        // retrieve Action Bar height and store in FreecellObjectTools
        val typedValue = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            FreeCellObjectTools.actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
        }

        prefs = getSharedPreferences("com.conversantmedia.freecell", Context.MODE_PRIVATE)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        intentExtra = intent?.extras?.get("reasonCode")

        tableau = arrayListOf(
                tableauColumn1,
                tableauColumn2,
                tableauColumn3,
                tableauColumn4,
                tableauColumn5,
                tableauColumn6,
                tableauColumn7,
                tableauColumn8)

        deck = Deck(this)
        game = Game(this, deck, tableau)


        backButton.setOnClickListener {
            tools.goBack(mainLayout)
        }

        button_menu.setOnClickListener {
            tools.openMenu(mainLayout)
        }

        resetButton.setOnClickListener {
            tools.resetGame(mainLayout)

        }

        forwardButton.setOnClickListener {
            tools.goForward(mainLayout)
        }

        score.text = resources.getString(R.string.score, FreeCellObjectTools.gameScore)

        // prepare victory button which is not visible, yet
        victory.setOnClickListener {
            v ->
            tools.enableUI(mainLayout)
        }

        freecellViewModel.preferences.observe(this, object: Observer<PlayerPreferences> {
            override fun onChanged(it: PlayerPreferences?) {
                if (it != null) {
                    FreeCellObjectTools.highlightTint = it.hintHighlights.toInt()
                    FreeCellObjectTools.animationDuration = it.animationSpeed
                    FreeCellObjectTools.vibratePref = it.vibrate
                    FreeCellObjectTools.autoPlay = it.autoplay
                }
            }
        })

        freecellViewModel.savedGame.observe(this, object: Observer<SavedGame> {

            override fun onChanged(it: SavedGame?) = viewModelChanged(it)

        })
    }

    private fun viewModelChanged(savedGame: SavedGame?) {
        if (savedGame != null) {
            tools.clearGame(mainLayout)
            if (!savedGame.deck.isNullOrEmpty()) {
                deck = Deck(savedGame.deck as ArrayList<CardValues>, thisContext)
            }
            if (!savedGame.backMoves.isNullOrEmpty()) {
                backMovesArrayList = savedGame.backMoves as ArrayList<CardMove>
                FreeCellObjectTools.moves = backMovesArrayList
            }
            if (!savedGame.forwardMoves.isNullOrEmpty()) {
                forwardMovesArrayList = savedGame.forwardMoves as ArrayList<CardMove>
                FreeCellObjectTools.forwardMoves = forwardMovesArrayList
            }
            restoreGame(deck, backMovesArrayList, savedGame.score ?: 0)


        } else {
            tools.clearGame(mainLayout)
            tools.clean(mainLayout)
            val deck = Deck(thisContext)
            deck.shuffle()
            parcelableDeck = tools.deal(deck, mainLayout)
            FreeCellObjectTools.parcelableDeck = parcelableDeck.clone() as ParcelableDeck
            tools.dealAnimation(mainLayout)
            tools.autoPlay(mainLayout)
            tools.arrows(mainLayout)
            val event = CNVRTagSyncEvent.Builder(arrayOf("newGame"), "freecell/mainMenu").build()
            CNVRTagManager.getSdk().fireEvent(event)
        }
    }

    override fun onResume() {
        super.onResume()

        FreeCellObjectTools.bleached = false
        tools.clean(mainLayout)

        toolBar.background = resources.getDrawable(R.drawable.toolbar, theme)


        tools.arrows(mainLayout)

        CNVRTagManager.updateGooglePlayServices { errorCode ->
            val googleApiAvailability = GoogleApiAvailability.getInstance()
            if (googleApiAvailability.isUserResolvableError(errorCode)) {
                googleApiAvailability.getErrorDialog(this@MainActivity, errorCode, 0).show()
            }
        }


        if (prefs.getBoolean("firstrun", true)) {

            val event = CNVRTagSyncEvent.Builder(arrayOf("install"), "freecell/install")
                    .withExtra("installTime", DATE_FORMATTER.format(Date()))
                    .build();
            CNVRTagManager.getSdk().fireEventWithGoogleInstallReferrer(event);

            prefs.edit().putBoolean("firstrun", false).commit()


        }



    }


    override fun onPause() {
        super.onPause()
        backMovesArrayList = FreeCellObjectTools.moves.clone() as ArrayList<CardMove>
        forwardMovesArrayList = FreeCellObjectTools.forwardMoves.clone() as ArrayList<CardMove>
        savedGame = SavedGame(1, parcelableDeck as ArrayList<CardValues>, backMovesArrayList,
                forwardMovesArrayList, FreeCellObjectTools.gameScore)
        freecellViewModel.savedGame.removeObservers(this)
        freecellViewModel.insert(savedGame)
    }

    override fun onDestroy() {
        super.onDestroy()
        tools.clearGame(mainLayout)
    }

    /*
     * Called any time the game needs to recover from UI teardown, including configuration changes
     * such as screen rotation.
     */
    private fun restoreGame(shuffledDeck: Deck,
                            gameMoves: ArrayList<CardMove>?,
                            gameScore: Int)
     {
        try {
            parcelableDeck.clear()
            parcelableDeck = tools.deal(shuffledDeck, mainLayout)
            FreeCellObjectTools.parcelableDeck = parcelableDeck.clone() as ParcelableDeck

            // Make sure that we are considered "dealing" at this time.  the game.deal() function unsets this value
            // before we get here
            FreeCellObjectTools.dealing = true
            if (gameMoves != null) {
                for (index in 0 until gameMoves.size ) {
                    var move = gameMoves[index]
                    val fromId = move.fromId
                    val toId = move.toId
                    val fromView = when(move.fromType) {
                        "TableauColumn" -> { findViewById<TableauColumn>(fromId) }
                        "FreeCell" -> { findViewById<FreeCell>(fromId) as FreeCell }
                        else -> { null }
                    }
                    val toView = when(move.toType) {
                        "TableauColumn" -> { findViewById<TableauColumn>(toId) }
                        "FreeCell" -> { findViewById<FreeCell>(toId) }
                        "FoundationCell" -> { findViewById<FoundationCell>(toId) }
                        else -> { null }
                    }
                    var groupSize = move.groupSize
                    if (groupSize == 1) {
                        tools.moveCard((fromView as ViewGroup).getChildAt(move.fromIndex) as PlayingCard,
                                fromView as CardGroup, toView as CardGroup, groupSize, 0, false, null)
                    } else {
                        var loopingIndex = move.fromIndex
                        move = gameMoves[index]
                        val toId = move.toId
                        val fromId = move.fromId
                        val fromView = findViewById<TableauColumn>(fromId)
                        val toView = findViewById<TableauColumn>(toId)
                        tools.moveCard(fromView.getChildAt(loopingIndex++) as PlayingCard, fromView, toView, groupSize, 0, false, null)
                    }
                }
                FreeCellObjectTools.gameScore = gameScore
                score.text = resources.getString(R.string.score, FreeCellObjectTools.gameScore)
            }
            // game is restored, turn off dealing flag
            FreeCellObjectTools.dealing = false
        } catch (exception: Exception) {
            Log.e(TAG, "Exception: ${exception.toString()}")
            FreeCellObjectTools.moves.clear()
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
            finish()
        }

        return
    }
}

