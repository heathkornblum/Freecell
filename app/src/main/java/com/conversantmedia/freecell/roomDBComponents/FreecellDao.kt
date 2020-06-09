/**
 *
 *  FreecellDao.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.roomDBComponents

import androidx.lifecycle.LiveData
import androidx.room.*
import com.conversantmedia.freecell.card.CardMove
import com.conversantmedia.freecell.card.CardValues
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson



@Dao
interface FreecellDao {

    @Query("SELECT * FROM saved_game_table where id = 1")
    fun getSavedGame(): LiveData<SavedGame>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNewGameSave(game: SavedGame)

    @Update
    fun updateGame(game: SavedGame)

    @Query("DELETE FROM saved_game_table")
    fun deleteSavedGame()

}
class Converters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromString(value: String): ArrayList<CardValues> {
            val listType = object : TypeToken<ArrayList<CardValues>>() {

            }.type
            return Gson().fromJson(value, listType)
        }

        @TypeConverter
        @JvmStatic
        fun fromArrayList(list: ArrayList<CardValues>): String {
            val gson = Gson()
            return gson.toJson(list)
        }

        @TypeConverter
        @JvmStatic
        fun cardMovesFromString(value: String): ArrayList<CardMove> {
            val listType = object : TypeToken<ArrayList<CardMove>>() {

            }.type
            return Gson().fromJson(value, listType)
        }

        @TypeConverter
        @JvmStatic
        fun cardMovesFromArrayList(list: ArrayList<CardMove>): String {
            val gson = Gson()
            return gson.toJson(list)
        }

    }

}

