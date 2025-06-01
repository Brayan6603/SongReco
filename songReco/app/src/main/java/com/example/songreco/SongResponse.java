package com.example.songreco;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "songs")
public class SongResponse implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String artist;
    public String album;
    public long timestamp;
    public String spotifyUrl;
    public String youtubeUrl;
}