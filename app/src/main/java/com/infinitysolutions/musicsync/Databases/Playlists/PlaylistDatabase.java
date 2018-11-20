package com.infinitysolutions.musicsync.Databases.Playlists;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Playlist.class} , version = 1)
public abstract class PlaylistDatabase extends RoomDatabase {
    public abstract PlaylistDao playlistDao();
}
