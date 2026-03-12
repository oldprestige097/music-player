package com.example.musicplayer;

import android.net.Uri;

public class Song {
    private final String name;
    private final String artist;
    private final Uri uri;

    public Song(String name, String artist, Uri uri) {
        this.name = name;
        this.artist = artist;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public Uri getUri() {
        return uri;
    }
}
