package com.chords.app;

public class SongLine {
    private final String lyrics;
    private final String chords;

    public SongLine(String lyrics, String chords) {
        this.lyrics = lyrics;
        this.chords = chords;
    }

    public String getLyrics() {
        return lyrics;
    }

    public String getChords() {
        return chords;
    }
}
