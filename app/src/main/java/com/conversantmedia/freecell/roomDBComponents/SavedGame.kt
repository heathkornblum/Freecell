/**
 *
 *  SavedGame.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.roomDBComponents

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.conversantmedia.freecell.card.CardMove
import com.conversantmedia.freecell.card.CardValues

@Entity(tableName = "saved_game_table")
data class SavedGame(
        @PrimaryKey(autoGenerate = true) var id: Int?,
        @ColumnInfo(name = "deck") var deck: ArrayList<CardValues>?,
        @ColumnInfo(name = "back_moves") var backMoves: ArrayList<CardMove>?,
        @ColumnInfo(name = "forward_moves") var forwardMoves: ArrayList<CardMove>?,
        @ColumnInfo(name = "game_score") var score: Int?
){
    constructor(): this(null, null, null, null, null)
}


