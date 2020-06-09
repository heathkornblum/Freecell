/**
 *
 *  PlayerPreferences.kt
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

@Entity(tableName = "preferences")
data class PlayerPreferences(
        @PrimaryKey(autoGenerate = true) var id: Int?,
        @ColumnInfo(name = "autoplay") var autoplay: Boolean,
        @ColumnInfo(name = "vibrate") var vibrate: Boolean,
        @ColumnInfo(name = "animation_speed") var animationSpeed: Long,
        @ColumnInfo(name = "hint_highlights") var hintHighlights: Long,
        @ColumnInfo(name = "theme") var themeId: Int?
) {
    constructor(): this(null, true, true, 300L, 0L, 0)
}
