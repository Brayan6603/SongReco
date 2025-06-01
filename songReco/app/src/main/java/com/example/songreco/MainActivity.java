package com.example.songreco;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private Button btnRecord;
    private TextView tvStatus;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRecord = findViewById(R.id.btnRecord);
        tvStatus = findViewById(R.id.tvStatus);

        btnRecord.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
            } else {
                startRecording();
            }
        });
    }

    private void startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }

        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            audioFilePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/audio_" + timeStamp + ".3gp";

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            btnRecord.setText("DETENER");
            tvStatus.setText("Grabando...");
        } catch (IOException e) {
            Log.e("MainActivity", "Error al grabar audio: " + e.getMessage());
            Toast.makeText(this, "Error al iniciar grabación", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }

        isRecording = false;
        btnRecord.setText("GRABAR");
        tvStatus.setText("Enviando para reconocimiento...");

        recognizeSong();
    }

    private void recognizeSong() {
        File audioFile = new File(audioFilePath);
        ACRCloudService.recognizeSong(this, audioFile, new ACRCloudService.RecognitionCallback() {
            @Override
            public void onSuccess(SongResponse song) {
                tvStatus.setText("Canción reconocida!");

                // Guardar en base de datos
                AppDatabase db = AppDatabase.getDatabase(MainActivity.this);
                new Thread(() -> {
                    db.songDao().insert(song);
                    runOnUiThread(() -> showSongDetails(song));
                }).start();
            }

            @Override
            public void onError(String error) {
                tvStatus.setText("Error: " + error);
            }
        });
    }

    private void showSongDetails(SongResponse song) {
        Intent intent = new Intent(this, SongDetailActivity.class);
        intent.putExtra("song", song);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(this, "Permiso de micrófono denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}