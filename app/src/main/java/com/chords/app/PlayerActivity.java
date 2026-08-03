package com.chords.app;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

public class PlayerActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ScrollView scrollViewSong;
    private TextView tvSongContent;
    
    private Switch switchAutoScrollTempo, switchVoiceSync;
    private TextView tvTransposeValue;
    private int transposeOffset = 0;
    
    private SongPlaybackManager playbackManager;
    private String songId;
    private Uri audioUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        drawerLayout = findViewById(R.id.drawerLayoutPlayer);
        scrollViewSong = findViewById(R.id.scrollViewSong);
        tvSongContent = findViewById(R.id.tvSongContent);
        
        Button btnOpenMenu = findViewById(R.id.btnOpenMenu);
        switchAutoScrollTempo = findViewById(R.id.switchAutoScrollTempo);
        switchVoiceSync = findViewById(R.id.switchVoiceSync);
        tvTransposeValue = findViewById(R.id.tvTransposeValue);
        
        Button btnTransposeDown = findViewById(R.id.btnTransposeDown);
        Button btnTransposeUp = findViewById(R.id.btnTransposeUp);
        Button btnPlayAudio = findViewById(R.id.btnPlayAudio);

        // קבלת נתונים מה-Intent שנשלח מהמסך הקודם
        songId = getIntent().getStringExtra("SONG_ID");
        String uriStr = getIntent().getStringExtra("AUDIO_URI");
        if (uriStr != null) {
            audioUri = Uri.parse(uriStr);
        }

        playbackManager = new SongPlaybackManager();

        // פתיחת תפריט הצד בלחיצה על הכפתור (עבור מסכים קטנים)
        btnOpenMenu.setOnClickListener(v -> drawerLayout.open());

        // טעינת קובץ ה-JSON השמור של השיר
        loadSongPackageData();

        // 1. הגדרת גלילה אוטומטית לפי קצב
        switchAutoScrollTempo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                switchVoiceSync.setChecked(false); // ביטול השני אם הראשון פעיל
                Toast.makeText(this, "הופעלה גלילה אוטומטית לפי קצב", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. הגדרת גלילה לפי זיהוי דיבור (מיקרופון)
        switchVoiceSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                switchAutoScrollTempo.setChecked(false);
                Toast.makeText(this, "הופעלה גלילה חכמה לפי קול", Toast.LENGTH_SHORT).show();
                // כאן נפעיל את לולאת הקריאה ל-NativeAudioEngine.findCurrentTimestampByVoice(...)
            }
        });

        // 3. טרנספוזיציה (+/-)
        btnTransposeDown.setOnClickListener(v -> {
            transposeOffset--;
            tvTransposeValue.setText(String.valueOf(transposeOffset));
            updateChordsTransposition();
        });

        btnTransposeUp.setOnClickListener(v -> {
            transposeOffset++;
            tvTransposeValue.setText(String.valueOf(transposeOffset));
            updateChordsTransposition();
        });

        // 4. השמעת שיר ברקע
        btnPlayAudio.setOnClickListener(v -> {
            if (audioUri != null) {
                playbackManager.playSong(this, audioUri);
                Toast.makeText(this, "מנגן את השיר ברקע...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "קובץ השמע לא נמצא בחבילה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSongPackageData() {
        if (songId != null) {
            String jsonContent = SongDataPersistence.loadSongJsonFromFile(this, songId);
            if (jsonContent != null) {
                // הצגת תוכן ה-JSON או פענוח שלו לטקסט מעוצב על המסך
                tvSongContent.setText("החבילה נטענה בהצלחה!\n\n[C] שלום [Am] עולם\n[F] כאן יהיו האקורדים [G] והמילים המסונכרנות.");
            } else {
                tvSongContent.setText("שגיאה: לא נמצאו נתונים שמורים לחבילה זו.");
            }
        }
    }

    private void updateChordsTransposition() {
        // עדכון הצגת האקורדים בהתאם לערך הטרנספוזיציה החדש
        Toast.makeText(this, "סולם שונה בהיסט: " + transposeOffset, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playbackManager.stopSong();
    }
}
