package com.chords.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChordTransposer {

    private static final String[] NOTES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    
    public static String normalizeChord(String chord) {
        return chord.replace("Db", "C#")
                    .replace("Eb", "D#")
                    .replace("Gb", "F#")
                    .replace("Ab", "G#")
                    .replace("Bb", "A#");
    }

    public static String transposeChord(String chord, int semitones) {
        if (chord == null || chord.isEmpty()) return chord;
        chord = normalizeChord(chord);

        String root;
        String suffix;

        if (chord.length() > 1 && chord.charAt(1) == '#') {
            root = chord.substring(0, 2);
            suffix = chord.substring(2);
        } else {
            root = chord.substring(0, 1);
            suffix = chord.substring(1);
        }

        int currentIndex = -1;
        for (int i = 0; i < NOTES.length; i++) {
            if (NOTES[i].equals(root)) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1) return chord;

        int newIndex = (currentIndex + (semitones % 12) + 12) % 12;
        return NOTES[newIndex] + suffix;
    }

    public static String transposeSongContent(String content, int semitones) {
        if (semitones == 0) return content;

        Pattern pattern = Pattern.compile("\\[([A-Gb#m7sus24]+\\b)]");
        Matcher matcher = pattern.matcher(content);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String chordInside = matcher.group(1);
            String transposed = transposeChord(chordInside, semitones);
            matcher.appendReplacement(sb, "[" + transposed + "]");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
