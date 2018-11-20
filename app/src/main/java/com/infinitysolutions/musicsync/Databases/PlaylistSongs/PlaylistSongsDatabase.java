package com.infinitysolutions.musicsync.Databases.PlaylistSongs;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {PlaylistSongs.class} , version = 1)
public abstract class PlaylistSongsDatabase extends RoomDatabase {
    public abstract PlaylistSongsDao playlistSongsDao();
}
