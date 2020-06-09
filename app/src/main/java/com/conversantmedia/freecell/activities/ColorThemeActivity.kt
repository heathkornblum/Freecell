/**
 *
 *  ColorThemeActivity.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.activities

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity;
import com.conversantmedia.freecell.R
import com.conversantmedia.freecell.game.tools.FreeCellObjectTools
import com.conversantmedia.freecell.game.tools.FreeCellTools
import conversant.tagmanager.sdk.CNVRTagManager

import kotlinx.android.synthetic.main.activity_color_theme.*

class ColorThemeActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName
    val tools = FreeCellTools()
    lateinit var settingsIntent: Intent
    lateinit var themeButtonMap: HashMap<Int, Button>
    lateinit var markerView: Drawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tools.loadTheme(this, FreeCellObjectTools.theme)
        setContentView(R.layout.activity_color_theme)
        setSupportActionBar(aboutToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        settingsIntent = Intent(this, SettingsActivity::class.java)
        CNVRTagManager.initialize(this)
        themeButtonMap = setThemeButtons()
        themeButtonMap.forEach {
            it.value.setOnClickListener(object : View.OnClickListener{
                override fun onClick(v: View?) {
                    saveTheme(it.key)
                }
            })
            if (FreeCellObjectTools.theme == it.key) {
                it.value.text = "\u2B55".plus(" ").plus(it.value.text.toString()).plus(" ").plus("\u2B55")
            }
        }
    }

    private fun saveTheme(themeId: Int) {
        FreeCellObjectTools.theme = themeId
        settingsIntent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        startActivity(settingsIntent)
    }

    private fun setThemeButtons() : HashMap<Int, Button> = hashMapOf(
            0 to dippedConeButton,
            1 to hotFudgeSundaeButton,
            2 to bostonCreamPieButton,
            3 to blackForestCakeButton,
            4 to tiramisuButton,
            5 to cherriesJubileeButton,
            6 to strawberryShortcakeButton,
            7 to carrotCakeButton,
            8 to pumpkinPieButton,
            9 to creamsicleButton,
            10 to lemonMeringuePieButton,
            11 to lemonBarButton,
            12 to keyLimePieButton,
            13 to mintChipButton,
            14 to candyAppleButton,
            15 to kiwiButton,
            16 to bombPopButton,
            17 to blueberryParfaitButton,
            18 to berryCobblerButton,
            19 to cottonCandyButton,
            20 to sMoresButton,
            21 to cherryCheesecakeButton,
            22 to birthdayCakeButton,
            23 to cookiesNCreamButton,
            24 to licoriceButton
        )

    override fun onBackPressed() {
        val intent = Intent(this, SettingsActivity::class.java)

        startActivity(intent)
    }
}
