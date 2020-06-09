/**
 *
 *  SettingsActivity.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.conversantmedia.freecell.card.PlayingCard
import com.conversantmedia.freecell.card.Suit
import com.conversantmedia.freecell.game.tools.FreeCellObjectTools
import com.conversantmedia.freecell.game.tools.FreeCellTools
import com.conversantmedia.freecell.R
import com.conversantmedia.freecell.roomDBComponents.FreecellViewModel
import com.conversantmedia.freecell.roomDBComponents.PlayerPreferences
import com.google.firebase.analytics.FirebaseAnalytics
import conversant.tagmanager.sdk.CNVRTagManager
import conversant.tagmanager.sdk.CNVRTagSyncEvent
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    val tools = FreeCellTools()
    lateinit var autoplaySeekbar: SeekBar
    lateinit var highlightSeekBar: SeekBar
    lateinit var sampleCard: PlayingCard
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    lateinit var preferences: PlayerPreferences
    lateinit var freecellViewModel: FreecellViewModel
    lateinit var intentColorTheme: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tools.loadTheme(this, FreeCellObjectTools.theme)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbarSettings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        CNVRTagManager.initialize(this)
        val context = this

        intentColorTheme = Intent(this, ColorThemeActivity::class.java)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        freecellViewModel = ViewModelProviders.of(this).get(FreecellViewModel::class.java)
        autoplaySeekbar = autoplaySpeed
        highlightSeekBar = highlightTint
        sampleCard = PlayingCard(Suit.SPADE, 1, settingsLayout.context)
        sampleCard.id = 666.toInt()
        sampleCardHolder.addView(sampleCard)
        sampleCardHolder.visibility = View.GONE

        autoplaySwitch.setOnClickListener {
            FreeCellObjectTools.autoPlay = autoplaySwitch.isChecked
        }

        autoplaySeekbar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    FreeCellObjectTools.animationDuration = FreeCellObjectTools.maxAutoPlayDuration - progress

                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            }
        )

        highlightSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    FreeCellObjectTools.highlightTint = FreeCellObjectTools.maxHighlightValue - progress

                    sampleCard.background = tools.getVersionDrawable(sampleCard, R.drawable.playing_card_tint_sample, "red", false)

                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    sampleCardHolder.visibility = View.VISIBLE
                    FreeCellObjectTools.bleached = false

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    sampleCardHolder.visibility = View.GONE
                }
            }
        )

        colorThemeButton.setOnClickListener {
            val event = CNVRTagSyncEvent.Builder(arrayOf("colorThemeButton"), "freecell/settings").build()
            CNVRTagManager.getSdk().fireEvent(event)
            intentColorTheme.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            this.startActivity(intentColorTheme)
        }

        freecellViewModel.preferences.observe(this, object: Observer<PlayerPreferences> {
            override fun onChanged(it: PlayerPreferences?) {
                if (it != null) {
                    displayPreferences(it)
                    autoplaySeekbar.progress = (FreeCellObjectTools.maxAutoPlayDuration - it.animationSpeed).toInt()
                    autoplaySwitch.isChecked = it.autoplay
                    highlightSeekBar.progress = (FreeCellObjectTools.maxHighlightValue - it.hintHighlights).toInt()
                } else {
                    autoplaySeekbar.progress = (FreeCellObjectTools.maxAutoPlayDuration - FreeCellObjectTools.animationDuration).toInt()
                    autoplaySwitch.isChecked = FreeCellObjectTools.autoPlay
                    highlightSeekBar.progress = (FreeCellObjectTools.maxHighlightValue - FreeCellObjectTools.highlightTint)
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        val preferences = PlayerPreferences(1,
                FreeCellObjectTools.autoPlay,
                FreeCellObjectTools.vibratePref,
                FreeCellObjectTools.animationDuration,
                FreeCellObjectTools.highlightTint.toLong(),
                FreeCellObjectTools.theme)

        freecellViewModel.preferences.removeObservers(this)
        freecellViewModel.updatePreferences(preferences)
    }

    private fun displayPreferences(preferences: PlayerPreferences) {
        autoplaySwitch.isChecked = preferences.autoplay
        autoplaySeekbar.progress = (FreeCellObjectTools.maxAutoPlayDuration - preferences.animationSpeed).toInt()
        highlightSeekBar.progress = (FreeCellObjectTools.maxHighlightValue - preferences.hintHighlights).toInt()
    }

    override fun onBackPressed() {
        val intent = Intent(this, MenuActivity::class.java)

        startActivity(intent)
    }
}
