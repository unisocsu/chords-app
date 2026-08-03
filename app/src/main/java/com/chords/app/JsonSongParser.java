package com.chords.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonSongParser {

    public static Song parseSong(String jsonString) throws JSONException {
        JSONObject root = new JSONObject(jsonString);

        String title = root.optString("title", "");
        String artist = root.optString("artist", "");
        int durationSeconds = root.optInt("durationSeconds", 0);
        String originalKey = root.optString("originalKey", "");
        int originalBpm = root.optInt("originalBpm", 0);
        int transposeSteps = root.optInt("transposeSteps", 0);
        String albumArtPath = root.optString("albumArtPath", "");
        String audioPath = root.optString("audioPath", "");

        List<SongLine> lines = new ArrayList<>();
        JSONArray linesArray = root.getJSONArray("lines");

        for (int i = 0; i < linesArray.length(); i++) {
            JSONObject lineObj = linesArray.getJSONObject(i);
            String section = lineObj.optString("section", "");

            List<ChordToken> tokens = new ArrayList<>();
            JSONArray tokensArray = lineObj.getJSONArray("tokens");

            for (int j = 0; j < tokensArray.length(); j++) {
                JSONObject tokenObj = tokensArray.getJSONObject(j);
                String chord = tokenObj.optString("chord", "");
                String text = tokenObj.optString("text", "");
                tokens.add(new ChordToken(chord, text));
            }

            lines.add(new SongLine(section, tokens));
        }

        return new Song(title, artist, durationSeconds, originalKey, originalBpm, 
                    transposeSteps, albumArtPath, audioPath, lines);
    }
}
