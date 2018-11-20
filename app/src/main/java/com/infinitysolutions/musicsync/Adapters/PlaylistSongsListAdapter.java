package com.infinitysolutions.musicsync.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.infinitysolutions.musicsync.Databases.PlaylistSongs.PlaylistSongs;
import com.infinitysolutions.musicsync.R;

import java.util.ArrayList;
import java.util.List;

public class PlaylistSongsListAdapter extends RecyclerView.Adapter<PlaylistSongsListAdapter.ViewHolder> {

    private List<PlaylistSongs> mSongsList;
    private List<Long> albumIdsList;
    private Context mContext;

    public PlaylistSongsListAdapter(Context context,List<PlaylistSongs> songsList){
        mSongsList = songsList;
        mContext = context;
        albumIdsList = new ArrayList<Long>();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = mContext.getContentResolver().query(
                musicUri,
                null,
                MediaStore.Audio.Media.DATA + " like ? ",
                new String[]{"%MusicSync%"},
                null
        );
        if(cursor != null){
            String uriData;
            for(PlaylistSongs playlistSongs: mSongsList){
                uriData = playlistSongs.getUriData();
                cursor.moveToFirst();
                do{
                    if(uriData.equals(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))))
                        break;
                }while(cursor.moveToNext());
                albumIdsList.add(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)));
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView mSongArtImageView;
        private TextView mSongNameTextView;
        private TextView mArtistNameTextView;

        public ViewHolder(View v) {
            super(v);
            mSongArtImageView = (ImageView)v.findViewById(R.id.item_song_art_image);
            mSongNameTextView = (TextView)v.findViewById(R.id.item_song_text_view);
            mArtistNameTextView = (TextView)v.findViewById(R.id.item_artist_text_view);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.songs_list_item_view,parent,false);
        return new PlaylistSongsListAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaylistSongs playlistSongs = mSongsList.get(position);
        holder.mSongNameTextView.setText(playlistSongs.getTitle());
        holder.mArtistNameTextView.setText(playlistSongs.getArtist());

    }

    @Override
    public int getItemCount() {
        return mSongsList.size();
    }
}
