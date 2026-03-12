package com.example.musicplayer

import android.net.Uri

data class Song(
    val name: String,
    val artist: String,
    val uri: Uri
)
