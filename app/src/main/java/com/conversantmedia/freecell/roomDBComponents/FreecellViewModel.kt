/**
 *
 *  FreecellViewModel.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.roomDBComponents

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class FreecellViewModel(application: Application): AndroidViewModel(application) {

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    private val scope = CoroutineScope(coroutineContext)


    private val repository: FreecellRepository

    val savedGame: LiveData<SavedGame>
    val preferences: LiveData<PlayerPreferences>

    init {
        val freecellDao = FreecellDB.getDatabase(application).freecellDao()
        val preferencesDao = FreecellDB.getDatabase(application).preferencesDao()
        repository = FreecellRepository(freecellDao, preferencesDao)
        savedGame = repository.savedGame
        preferences = repository.preferences

    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }



    fun insert(savedGame: SavedGame) = scope.launch(Dispatchers.IO) {
        repository.insert(savedGame)
    }

    fun update(savedGame: SavedGame) = scope.launch(Dispatchers.IO) {
        repository.update(savedGame)
    }

    fun delete() = scope.launch(Dispatchers.IO) {
        repository.delete()
    }

    fun updatePreferences(preferences: PlayerPreferences) = scope.launch(Dispatchers.IO) {
        repository.updatePreferences(preferences)
    }

}