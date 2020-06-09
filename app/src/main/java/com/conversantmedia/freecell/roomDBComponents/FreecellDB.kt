/**
 *
 *  FreecellDB.kt
 *
 *  Freecell, By Conversant
 *  Created By <a href="heathkornblum@gmail.com>Heath Kornblum</a>, Conversant, Inc.
 *  Last Updated April 22, 2019
 *  Copyright 2019 Conversant, Inc.
 *
 **/

package com.conversantmedia.freecell.roomDBComponents

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SavedGame::class, PlayerPreferences::class], version = 9)
@TypeConverters(com.conversantmedia.freecell.roomDBComponents.Converters::class)
abstract class FreecellDB: RoomDatabase() {
    abstract fun freecellDao(): FreecellDao
    abstract fun preferencesDao(): PreferencesDao

    companion object {
        @Volatile
        private var INSTANCE: FreecellDB? = null

        fun getDatabase(context: Context): FreecellDB {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        FreecellDB::class.java,
                        "Freecell_database"
                )
                        .fallbackToDestructiveMigration()
                        .build()
                INSTANCE = instance
                return instance
            }

        }

    }

}

