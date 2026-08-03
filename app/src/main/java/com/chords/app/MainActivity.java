package com.chords.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static.final int PICK_AUDIO_REQUEST = 1;
    private ListView listViewSongs;
    private ArrayList<String> preloadedSongsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main.xml.replace(".xml", "")); // תיקון נתיב פשוט ל-layout
        // לחלופין: setContentView(R.layout.activity_main);

        Button btnSelectFile = findViewById(R.id.btnSelectFile);
        listViewSongs = findViewById(R.id.listViewSongs);

        // טעינת רשימת שירים לדוגמה מתיקיית הנתונים (או קבצים מקומיים קיימים)
        loadPreloadedSongs();

        // כפתור בחירת קובץ שמע חדש מהמכשיר
        btnSelectFile.setOnClickListener(v -> openAudioFilePicker());

        // האזנה לבחירת שיר מתוך הרשימה המוכנה
        listViewSongs.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSongName = preloadedSongsList.get(position);
            openSongPlayer(selectedSongName, null);
        });
    }

    private void openAudioFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(Intent.createChooser(intent, "בחר קובץ שמע"), PICK_AUDIO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri audioUri = data.getData();
            if (audioUri != null) {
                // נחלץ את שם הקובץ או נשתמש בכתובת שלו כמזהה
                String songName = "custom_song_" + System.currentTimeMillis();
                
                // בדיקה האם כבר קיים קובץ JSON שמור עבור השיר הזה
                String existingJson = SongDataPersistence.loadSongJsonFromFile(this, songName);
                if (existingJson != null) {
                    Toast.makeText(this, "נמצא JSON שמור במכשיר! פותח נגן...", Toast.LENGTH_SHORT).show();
                    openSongPlayer(songName, audioUri);
                } else {
                    Toast.makeText(this, "השיר חדש, מתחיל ניתוח מנוע C++...", Toast.LENGTH_LONG).show();
                    // כאן נפעיל את הניתוח עם מנועי ה-C++ ונשמור את התוצאה
                    // ואז נפתח את הנגן המסונכרן
                    openSongPlayer(songName, audioUri);
                }
            }
        }
    }

    private void loadPreloadedSongs() {
        preloadedSongsList = new ArrayList<>();
        // הוספת שירים לדוגמה שיכולים להימצא בתיקיית הנתונים או ב-Assets
        preloadedSongsList.add("שיר לדוגמה 1 - ירושלים של זהב");
        preloadedSongsList.add("שיר לדוגמה 2 - אשרי האיש");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, preloadedSongsList);
        listViewSongs.setAdapter(adapter);
    }

    private void openSongPlayer(String songName, Uri audioUri) {
        // מעבר למסך הנגן והתצוגה המסונכרנת (שנבנה בהמשך)
        Toast.makeText(this, "פתיחת נגן עבור: " + songName, Toast.LENGTH_SHORT).show();
        
        // אפשר להעביר את ה-Uri או שם השיר באמצעות Intent למסך הבא
        // Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
        // intent.putExtra("SONG_NAME", songName);
        // if (audioUri != null) intent.putExtra("AUDIO_URI", audioUri.toString());
        // startActivity(intent);
    }
}
