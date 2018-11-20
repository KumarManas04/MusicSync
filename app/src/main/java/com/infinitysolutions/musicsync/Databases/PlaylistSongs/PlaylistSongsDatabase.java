package com.infinitysolutions.musicsync.Databases.PlaylistSongs;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {PlaylistSongs.class} , version = 1)
public abstract class PlaylistSongsDatabase extends RoomDatabase {
    public abstract PlaylistSongsDao playlistSongsDao();
}
