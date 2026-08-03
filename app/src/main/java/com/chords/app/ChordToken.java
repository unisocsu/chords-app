package com.chords.app;

public class ChordToken {
    private final String chord;
    private final String text;

    public ChordToken(String chord, String text) {
        this.chord = chord;
        this.text = text;
    }

    public String getChord() { return chord; }
    public String getText() { return text; }
}
