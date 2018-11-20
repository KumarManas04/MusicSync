package com.infinitysolutions.musicsync.Databases.Playlists;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlaylistDao {

    @Query("Select * from  playlist")
    List<Playlist> getAll();

    @Query("SELECT * FROM Playlist WHERE tableName LIKE :name LIMIT 1")
    Playlist findByName(String name);

    @Insert
    void insertAll(Playlist... playlist);

    @Update
    void update(Playlist playlist);

    @Delete
    void delete(Playlist playlist);
}
