package com.chords.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_AUDIO_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnFileExplorer = findViewById(R.id.btnFileExplorer);
        Button btnMediaLibrary = findViewById(R.id.btnMediaLibrary);
        Button btnWebSearch = findViewById(R.id.btnWebSearch);

        // 1. בחירה דרך מסייר הקבצים הרגיל של המכשיר
        btnFileExplorer.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            startActivityForResult(Intent.createChooser(intent, "בחר קובץ שמע"), PICK_AUDIO_REQUEST);
        });

        // 2. מעבר לספריית המוזיקה של המכשיר (אמנים, אלבומים וחיפוש)
        btnMediaLibrary.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MediaLibraryActivity.class);
            startActivity(intent);
        });

        // 3. פתיחת מסך WebView לחיפוש שירים ברשת
        btnWebSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WebSearchActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri audioUri = data.getData();
            if (audioUri != null) {
                // מעבר למסך הנגן והצגת האקורדים עבור הקובץ שנבחר
                openPlayerScreen(audioUri);
            }
        }
    }

    private void openPlayerScreen(Uri audioUri) {
        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
        intent.putExtra("AUDIO_URI", audioUri.toString());
        startActivity(intent);
    }
}
