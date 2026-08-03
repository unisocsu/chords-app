package com.chords.app;

import java.util.List;

public class Song {
    private final String title;
    private final String artist;
    private final int durationSeconds;
    private final String originalKey;
    private final int originalBpm;
    private final int transposeSteps;
    private String albumArtPath;
    private String audioPath;
    private final List<SongLine> lines;

    public Song(String title, String artist, int durationSeconds, String originalKey,
                int originalBpm, int transposeSteps, String albumArtPath,
                String audioPath, List<SongLine> lines) {
        this.title = title;
        this.artist = artist;
        this.durationSeconds = durationSeconds;
        this.originalKey = originalKey;
        this.originalBpm = originalBpm;
        this.transposeSteps = transposeSteps;
        this.albumArtPath = albumArtPath;
        this.audioPath = audioPath;
        this.lines = lines;
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public int getDurationSeconds() { return durationSeconds; }
    public String getOriginalKey() { return originalKey; }
    public int getOriginalBpm() { return originalBpm; }
    public int getTransposeSteps() { return transposeSteps; }
    public String getAlbumArtPath() { return albumArtPath; }
    public String getAudioPath() { return audioPath; }
    public List<SongLine> getLines() { return lines; }

    public void setAlbumArtPath(String albumArtPath) { this.albumArtPath = albumArtPath; }
    public void setAudioPath(String audioPath) { this.audioPath = audioPath; }
}
