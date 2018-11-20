package com.infinitysolutions.musicsync.Adapters;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.infinitysolutions.musicsync.Databases.Playlists.Playlist;
import com.infinitysolutions.musicsync.R;

import java.util.List;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder> {

    private List<Playlist> mPlaylists;
    private static OnPlaylistClickListener mOnPlaylistClickListener;

    public interface OnPlaylistClickListener {
        void openPlaylist(String tableName);
    }

    public void setOnItemClickListener(OnPlaylistClickListener listener){
        mOnPlaylistClickListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

//        private ImageView mPlaylistImageView;
        private TextView mPlaylistNameTextView;
        private TextView mSongsCountTextView;

        public ViewHolder(View v) {
            super(v);
//            mPlaylistImageView = (ImageView)v.findViewById(R.id.item_playlist_image);
            mPlaylistNameTextView = (TextView)v.findViewById(R.id.item_playlist_text_view);
            mSongsCountTextView = (TextView)v.findViewById(R.id.item_songs_count_text_view);
        }
    }

    public PlaylistsAdapter(List<Playlist> playlists){
        mPlaylists = playlists;
    }

    @NonNull
    @Override
    public PlaylistsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item_view,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistsAdapter.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        Playlist playlist = mPlaylists.get(position);
        holder.mPlaylistNameTextView.setText(playlist.getPlaylistName());
        holder.mSongsCountTextView.setText(playlist.getSongsCount() + " songs");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnPlaylistClickListener.openPlaylist(mPlaylists.get(position).getTableName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlaylists.size();
    }
}
