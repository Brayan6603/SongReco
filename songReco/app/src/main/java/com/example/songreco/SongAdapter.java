package com.example.songreco;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    private final List<SongResponse> songs;

    public SongAdapter(List<SongResponse> songs) {
        this.songs = songs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SongResponse song = songs.get(position);
        holder.tvTitle.setText(song.title);
        holder.tvArtist.setText(song.artist);
        holder.tvAlbum.setText(song.album);

        String time = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(song.timestamp));
        holder.tvTime.setText(time);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle, tvArtist, tvAlbum, tvTime;

        public ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvItemTitle);
            tvArtist = view.findViewById(R.id.tvItemArtist);
            tvAlbum = view.findViewById(R.id.tvItemAlbum);
            tvTime = view.findViewById(R.id.tvItemTime);
        }
    }
}