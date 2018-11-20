package com.infinitysolutions.musicsync.Databases.PlaylistSongs;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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
