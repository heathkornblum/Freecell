/**
 *
 *  FreecellRepository.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.roomDBComponents

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class FreecellRepository(private val freecellDao: FreecellDao, private val preferencesDao: PreferencesDao) {
    val savedGame: LiveData<SavedGame> = freecellDao.getSavedGame()
    val preferences: LiveData<PlayerPreferences> = preferencesDao.getPreferences()

    @WorkerThread
    suspend fun insert(savedGame: SavedGame) {
        freecellDao.insertNewGameSave(savedGame)
    }

    @WorkerThread
    suspend fun update(savedGame: SavedGame) {
        freecellDao.updateGame(savedGame)
    }

    @WorkerThread
    suspend fun delete() {
        freecellDao.deleteSavedGame()
    }

    @WorkerThread fun updatePreferences(preferences: PlayerPreferences) {
        preferencesDao.updatePreferences(preferences)
    }

}