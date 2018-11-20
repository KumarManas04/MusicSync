package com.infinitysolutions.musicsync.Databases.Playlists;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Playlist.class} , version = 1)
public abstract class PlaylistDatabase extends RoomDatabase {
    public abstract PlaylistDao playlistDao();
}
