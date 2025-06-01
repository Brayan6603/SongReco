package com.example.songreco;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SongDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_detail);

        SongResponse song = (SongResponse) getIntent().getSerializableExtra("song");

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvArtist = findViewById(R.id.tvArtist);
        TextView tvAlbum = findViewById(R.id.tvAlbum);
        Button btnSpotify = findViewById(R.id.btnSpotify);
        Button btnYoutube = findViewById(R.id.btnYoutube);

        tvTitle.setText(song.title);
        tvArtist.setText(song.artist);
        tvAlbum.setText(song.album);

        btnSpotify.setOnClickListener(v -> openUrl(song.spotifyUrl));
        btnYoutube.setOnClickListener(v -> openUrl(song.youtubeUrl));
    }

    private void openUrl(String url) {
        if (url != null && !url.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Enlace no disponible", Toast.LENGTH_SHORT).show();
        }
    }
}