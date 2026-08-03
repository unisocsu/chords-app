package com.chords.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class JsonSongSerializer {

    public static String serializeSong(Song song) throws JSONException {
        JSONObject root = new JSONObject();

        root.put("title", song.getTitle());
        root.put("artist", song.getArtist());
        root.put("durationSeconds", song.getDurationSeconds());
        root.put("originalKey", song.getOriginalKey());
        root.put("originalBpm", song.getOriginalBpm());
        root.put("transposeSteps", song.getTransposeSteps());

        if (song.getAlbumArtPath() != null && !song.getAlbumArtPath().isEmpty()) {
            root.put("albumArtPath", new File(song.getAlbumArtPath()).getName());
        } else {
            root.put("albumArtPath", "");
        }

        if (song.getAudioPath() != null && !song.getAudioPath().isEmpty()) {
            root.put("audioPath", new File(song.getAudioPath()).getName());
        } else {
            root.put("audioPath", "");
        }

        JSONArray linesArray = new JSONArray();
        for (SongLine line : song.getLines()) {
            JSONObject lineObj = new JSONObject();
            lineObj.put("section", line.getSection());

            JSONArray tokensArray = new JSONArray();
            for (ChordToken token : line.getTokens()) {
                JSONObject tokenObj = new JSONObject();
                tokenObj.put("chord", token.getChord());
                tokenObj.put("text", token.getText());
                tokensArray.put(tokenObj);
            }

            lineObj.put("tokens", tokensArray);
            linesArray.put(lineObj);
        }

        root.put("lines", linesArray);

        return root.toString(2);
    }
}
