package com.chords.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private TextView chordDisplayTextView;
    private Button toggleRecordingButton;

    private AudioRecorder audioRecorder;
    private NativeAudioEngine nativeAudioEngine;
    private boolean isRecording = false;

    // Direct interface callback for chord detection updates
    public interface OnChordDetectedListener {
        void onChordDetected(String chordName);
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startAudioProcessing();
                } else {
                    Toast.makeText(this, "Audio recording permission is required to analyze sound.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI Initialization
        chordDisplayTextView = findViewById(R.id.chordTextView);
        toggleRecordingButton = findViewById(R.id.startStopButton);

        // Engine Initialization
        nativeAudioEngine = new NativeAudioEngine();
        
        // AudioRecorder setup with UI thread boundary update
        audioRecorder = new AudioRecorder(nativeAudioEngine, chordName -> {
            runOnUiThread(() -> {
                if (chordDisplayTextView != null) {
                    chordDisplayTextView.setText(chordName);
                }
            });
        });

        if (toggleRecordingButton != null) {
            toggleRecordingButton.setOnClickListener(v -> {
                if (isRecording) {
                    stopAudioProcessing();
                } else {
                    checkPermissionAndStart();
                }
            });
        }
    }

    private void checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startAudioProcessing();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startAudioProcessing() {
        if (!isRecording && audioRecorder != null) {
            audioRecorder.startRecording();
            isRecording = true;
            if (toggleRecordingButton != null) {
                toggleRecordingButton.setText("Stop");
            }
        }
    }

    private void stopAudioProcessing() {
        if (isRecording && audioRecorder != null) {
            audioRecorder.stopRecording();
            isRecording = false;
            if (toggleRecordingButton != null) {
                toggleRecordingButton.setText("Start");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Mandatory release of microphone hardware on pause state
        if (isRecording) {
            stopAudioProcessing();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRecording) {
            stopAudioProcessing();
        }
    }
}
