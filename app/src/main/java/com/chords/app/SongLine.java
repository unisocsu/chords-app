package com.chords.app;

import java.util.List;

public class SongLine {
    private final String section;
    private final List<ChordToken> tokens;

    public SongLine(String section, List<ChordToken> tokens) {
        this.section = section;
        this.tokens = tokens;
    }

    public String getSection() {
        return section;
    }

    public List<ChordToken> getTokens() {
        return tokens;
    }
}
