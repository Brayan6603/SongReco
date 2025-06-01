package com.example.songreco;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SongAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            List<SongResponse> songs = db.songDao().getAllSongs();
            runOnUiThread(() -> {
                adapter = new SongAdapter(songs);
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }
}