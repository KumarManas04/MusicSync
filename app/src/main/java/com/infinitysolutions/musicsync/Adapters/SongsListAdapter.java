package com.infinitysolutions.musicsync.Adapters;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.infinitysolutions.musicsync.R;

public class SongsListAdapter extends RecyclerView.Adapter<SongsListAdapter.ViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private BitmapFactory.Options mBitmapOptions = new BitmapFactory.Options();

    private static OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void setAndPlaySong(long albumId, String songName, String artistName, String uriData);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
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

    public SongsListAdapter(Context context, Cursor cursor){
        mContext = context;
        mCursor = cursor;
    }

    @NonNull
    @Override
    public SongsListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.songs_list_item_view,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsListAdapter.ViewHolder holder, final int position) {
        mCursor.moveToPosition(position);
        mBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        mBitmapOptions.inDither = false;
        Uri mArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(
                mArtworkUri,
                mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)));

        Glide.with(mContext)
                .load(albumArtUri)
                .apply(new RequestOptions().placeholder(R.drawable.song_placeholder))
                .into(holder.mSongArtImageView);

        holder.mSongNameTextView.setText(mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
        holder.mArtistNameTextView.setText(mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCursor.moveToPosition(position);
                mOnItemClickListener.setAndPlaySong(
                        mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)),
                        mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                        mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                        mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
}