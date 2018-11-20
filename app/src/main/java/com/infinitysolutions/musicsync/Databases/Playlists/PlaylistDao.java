package com.infinitysolutions.musicsync.Databases.Playlists;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

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
