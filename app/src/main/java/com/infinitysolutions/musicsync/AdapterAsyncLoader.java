package com.infinitysolutions.musicsync;

import android.arch.persistence.room.Room;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.infinitysolutions.musicsync.Adapters.PlaylistsAdapter;
import com.infinitysolutions.musicsync.Databases.Playlists.Playlist;
import com.infinitysolutions.musicsync.Databases.Playlists.PlaylistDao;
import com.infinitysolutions.musicsync.Databases.Playlists.PlaylistDatabase;

public class AdapterAsyncLoader extends AsyncTaskLoader<PlaylistsAdapter> {

    private Context mContext;

    public AdapterAsyncLoader(Context context) {
        super(context);
        Log.d("HelloWorld", "Reached constructor of AdapterAsyncLoader");
        mContext = context;
    }

    @Override
    public PlaylistsAdapter loadInBackground() {
        Log.d("HelloWorld", "Reached loadInBackground AdapterAsyncLoader");
        PlaylistDatabase pDb = Room.databaseBuilder(mContext, PlaylistDatabase.class, "Playlists").build();
        PlaylistDao playlistDao = pDb.playlistDao();
        return new PlaylistsAdapter(playlistDao.getAll());
    }
}
