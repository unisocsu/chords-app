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
    private TextView tvTransposeValue, tvTempoValue;
    
    private int transposeOffset = 0;
    private float currentTempoFactor = 1.0f;
    
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
        tvTempoValue = findViewById(R.id.tvTempoValue);
        
        Button btnTransposeDown = findViewById(R.id.btnTransposeDown);
        Button btnTransposeUp = findViewById(R.id.btnTransposeUp);
        Button btnTempoDown = findViewById(R.id.btnTempoDown);
        Button btnTempoUp = findViewById(R.id.btnTempoUp);
        Button btnPlayAudio = findViewById(R.id.btnPlayAudio);

        songId = getIntent().getStringExtra("SONG_ID");
        String uriStr = getIntent().getStringExtra("AUDIO_URI");
        if (uriStr != null) {
            audioUri = Uri.parse(uriStr);
        }

        playbackManager = new SongPlaybackManager();

        btnOpenMenu.setOnClickListener(v -> drawerLayout.open());

        loadSongPackageData();

        switchAutoScrollTempo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                switchVoiceSync.setChecked(false);
                Toast.makeText(this, "הופעלה גלילה אוטומטית לפי קצב", Toast.LENGTH_SHORT).show();
            }
        });

        switchVoiceSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                switchAutoScrollTempo.setChecked(false);
                Toast.makeText(this, "הופעלה גלילה חכמה לפי קול", Toast.LENGTH_SHORT).show();
            }
        });

        // טרנספוזיציה (טקסט ואודיו)
        btnTransposeDown.setOnClickListener(v -> {
            transposeOffset--;
            tvTransposeValue.setText(String.valueOf(transposeOffset));
            updateChordsTransposition();
            applyAudioTransposition(transposeOffset);
        });

        btnTransposeUp.setOnClickListener(v -> {
            transposeOffset++;
            tvTransposeValue.setText(String.valueOf(transposeOffset));
            updateChordsTransposition();
            applyAudioTransposition(transposeOffset);
        });

        // שינוי מהירות BPM
        btnTempoDown.setOnClickListener(v -> {
            if (currentTempoFactor > 0.5f) {
                currentTempoFactor -= 0.1f;
                tvTempoValue.setText(String.format(java.util.Locale.US, "%.1fx", currentTempoFactor));
                applyTempoChange(currentTempoFactor);
            }
        });

        btnTempoUp.setOnClickListener(v -> {
            if (currentTempoFactor < 2.0f) {
                currentTempoFactor += 0.1f;
                tvTempoValue.setText(String.format(java.util.Locale.US, "%.1fx", currentTempoFactor));
                applyTempoChange(currentTempoFactor);
            }
        });

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
                tvSongContent.setText(jsonContent);
            } else {
                tvSongContent.setText("שגיאה: לא נמצאו נתונים שמורים לחבילה זו.");
            }
        }
    }

    private void updateChordsTransposition() {
        String originalContent = SongDataPersistence.loadSongJsonFromFile(this, songId);
        if (originalContent != null) {
            String transposedContent = ChordTransposer.transposeSongContent(originalContent, transposeOffset);
            tvSongContent.setText(transposedContent);
        }
    }

    private void applyAudioTransposition(int semitones) {
        // שליחת נתוני האודיו למנוע ה-C++ לשם שינוי הסולם (Pitch Shifting)
        Toast.makeText(this, "משנה סולם אודיו ל-" + semitones, Toast.LENGTH_SHORT).show();
    }

    private void applyTempoChange(float factor) {
        // שליחת נתוני האודיו למנוע ה-C++ לשם שינוי המהירות (Time-Stretching)
        Toast.makeText(this, "משנה מהירות קצב ל-" + factor, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playbackManager.stopSong();
    }
}
