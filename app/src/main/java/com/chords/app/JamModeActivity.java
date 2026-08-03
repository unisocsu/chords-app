package com.chords.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class JamModeActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int SAMPLE_RATE = 16000;
    
    private TextView tvLiveChord;
    private Button btnToggleListening;
    
    private boolean isListening = false;
    private AudioRecord audioRecord;
    private Thread recordingThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jam_mode);

        tvLiveChord = findViewById(R.id.tvLiveChord);
        btnToggleListening = findViewById(R.id.btnToggleListening);

        btnToggleListening.setOnClickListener(v -> {
            if (!isListening) {
                checkPermissionAndStartListening();
            } else {
                stopListening();
            }
        });
    }

    private void checkPermissionAndStartListening() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
        } else {
            startListening();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening();
        } else {
            Toast.makeText(this, "נדרשת הרשאת מיקרופון לפעולה זו", Toast.LENGTH_SHORT).show();
        }
    }

    private void startListening() {
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        audioRecord.startRecording();
        
        isListening = true;
        btnToggleListening.setText("עצור האזנה");
        Toast.makeText(this, "מתחיל זיהוי אקורדים מהמיקרופון...", Toast.LENGTH_SHORT).show();

        recordingThread = new Thread(() -> {
            short[] buffer = new short[2048];
            while (isListening) {
                int readSize = audioRecord.read(buffer, 0, buffer.length);
                if (readSize > 0) {
                    // שליחת דגימות השמע למנוע ה-C++ לקבלת האקורד בזמן אמת
                    String detectedChord = NativeAudioEngine.processAudioBuffer(buffer, readSize);
                    
                    runOnUiThread(() -> tvLiveChord.setText(detectedChord));
                }
            }
        });
        recordingThread.start();
    }

    private void stopListening() {
        isListening = false;
        if (audioRecord != null) {
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            audioRecord = null;
        }
        btnToggleListening.setText("התחל האזנה");
        tvLiveChord.setText("---");
        Toast.makeText(this, "האזנה הופסקה", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopListening();
    }
}
