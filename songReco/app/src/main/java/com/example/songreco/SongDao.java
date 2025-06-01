package com.example.songreco;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SongDao {
    @Insert
    void insert(SongResponse song);

    @Query("SELECT * FROM songs ORDER BY timestamp DESC")
    List<SongResponse> getAllSongs();
}