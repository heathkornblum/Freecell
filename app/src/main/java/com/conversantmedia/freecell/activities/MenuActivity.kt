/**
 *
 *  MenuActivity.kt
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.conversantmedia.freecell.game.tools.FreeCellTools
import kotlinx.android.synthetic.main.activity_menu.*
import com.conversantmedia.freecell.R
import com.conversantmedia.freecell.game.tools.FreeCellObjectTools
import com.conversantmedia.freecell.roomDBComponents.FreecellViewModel
import com.conversantmedia.freecell.roomDBComponents.PlayerPreferences
import com.conversantmedia.freecell.roomDBComponents.SavedGame
import com.google.firebase.analytics.FirebaseAnalytics
import conversant.tagmanager.sdk.CNVRTagManager
import conversant.tagmanager.sdk.CNVRTagSyncEvent

class MenuActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName
    val tools = FreeCellTools()
    lateinit var intentMainActivity: Intent
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    lateinit var freecellViewModel: FreecellViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        freecellViewModel = ViewModelProviders.of(this).get(FreecellViewModel::class.java)

        tools.loadTheme(this, FreeCellObjectTools.theme)

        setContentView(R.layout.activity_menu)
        freecellViewModel.preferences.observe(this, object : Observer<PlayerPreferences> {
            override fun onChanged(t: PlayerPreferences?) {
                if (FreeCellObjectTools.theme != t?.themeId && t?.themeId != null) {
                    FreeCellObjectTools.theme = t?.themeId ?: 0
                    recreate()
                }


            }
        })
        freecellViewModel.savedGame.observe(this, object : Observer<SavedGame> {
            override fun onChanged(t: SavedGame?) {
                if (t == null)
                    ButtonContinue.visibility = View.GONE
                else ButtonContinue.visibility = View.VISIBLE

            }
        })
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        intentMainActivity = Intent(this, MainActivity::class.java)
        intentMainActivity.flags = Intent.FLAG_ACTIVITY_NO_HISTORY

        CNVRTagManager.initialize(this)


        ButtonContinue.setOnClickListener {
            val event = CNVRTagSyncEvent.Builder(arrayOf("continueGame"), "freecell/mainMenu").build()
            CNVRTagManager.getSdk().fireEvent(event)
            this.startActivity(intentMainActivity)
        }

        ButtonNewGame.setOnClickListener{
            freecellViewModel.savedGame.removeObservers(this)
            freecellViewModel.delete()
            this.startActivity(intentMainActivity)
        }

        ButtonSettings.setOnClickListener{
            val event = CNVRTagSyncEvent.Builder(arrayOf("settingsOpened"), "freecell/settings").build()
            CNVRTagManager.getSdk().fireEvent(event)
            val intent = Intent(this, SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            this.startActivity(intent)
        }

        AboutButton.setOnClickListener{
            val event = CNVRTagSyncEvent.Builder(arrayOf("aboutOpened"), "freecell/about").build()
            CNVRTagManager.getSdk().fireEvent(event)
            val intent = Intent(this, AboutActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            this.startActivity(intent)
        }



    }

    override fun onResume() {
        super.onResume()
        freecellViewModel = ViewModelProviders.of(this).get(FreecellViewModel::class.java)
        freecellViewModel.savedGame.observe(this, object : Observer<SavedGame> {
            override fun onChanged(t: SavedGame?) {
                if (t == null)
                    ButtonContinue.visibility = View.GONE
                else ButtonContinue.visibility = View.VISIBLE

            }
        })
    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

}
