/**
 *
 *  AboutActivity.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.conversantmedia.freecell.R
import com.conversantmedia.freecell.game.tools.FreeCellObjectTools
import com.conversantmedia.freecell.game.tools.FreeCellTools
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    val tools = FreeCellTools()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tools.loadTheme(this, FreeCellObjectTools.theme)
        setContentView(R.layout.activity_about)
        setSupportActionBar(aboutToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
