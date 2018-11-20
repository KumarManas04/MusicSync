package com.infinitysolutions.musicsync.Databases.PlaylistSongs;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface PlaylistSongsDao {

    @Query("Select * from playlistsongs")
    List<PlaylistSongs> getAll();

    @Query("SELECT * FROM PlaylistSongs WHERE title LIKE :name LIMIT 1")
    PlaylistSongs findByName(String name);

    @Insert
    void insertAll(PlaylistSongs... playlistSongs);

    @Update
    void update(PlaylistSongs playlistSongs);

    @Delete
    void delete(PlaylistSongs playlistSongs);

}
