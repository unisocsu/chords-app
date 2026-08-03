package com.chords.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChordTransposer {

    // מערך התווים בסולם הכרומטי (כולל דיאזים)
    private static final String[] NOTES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    
    // מיפוי חלופי לבמולים (כדי להמיר במוזל לדיאז לצורך חישוב פשוט)
    public static String normalizeChord(String chord) {
        return chord.replace("Db", "C#")
                    .replace("Eb", "D#")
                    .replace("Gb", "F#")
                    .replace("Ab", "G#")
                    .replace("Bb", "A#");
    }

    /**
     * מבצע טנספוזיציה לאקורד בודד לפי מספר צעדים (semitones)
     */
    public static String transposeChord(String chord, int semitones) {
        if (chord == null || chord.isEmpty()) return chord;

        // נרמול האקורד
        chord = normalizeChord(chord);

        // הפרדת שורש האקורד (למשל ה-'Am' יפוצל ל-'A' והתוספת 'm')
        // נחפש את התו הראשון או שני תווים אם יש דיאז (#)
        String root = "";
        String suffix = "";

        if (chord.length() > 1 && chord.charAt(1) == '#') {
            root = chord.substring(0, 2);
            suffix = chord.substring(2);
        } else {
            root = chord.substring(0, 1);
            suffix = chord.substring(1);
        }

        // מציאת האינדקס של השורש במערך התווים
        int currentIndex = -1;
        for (int i = 0; i < NOTES.length; i++) {
            if (NOTES[i].equals(root)) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1) {
            return chord; // אם לא נמצא אקורד תקני, נחזיר כמו שהוא
        }

        // חישוב האינדקס החדש במעגל (עם תמיכה במספרים שליליים עקב ירידה בסולם)
        int newIndex = (currentIndex + (semitones % 12) + 12) % 12;

        return NOTES[newIndex] + suffix;
    }

    /**
     * עובר על מחרוזת השיר/ה-JSON ומחליף את כל האקורדים שנמצאים בסוגריים מרובעים [C] בהתאם לטרנספוזיציה
     */
    public static String transposeSongContent(String content, int semitones) {
        if (semitones == 0) return content;

        // ביטוי רגולרי לזיהוי אקורדים בתוך סוגריים מרובעים, למשל [Am], [F#m7]
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
