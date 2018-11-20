package com.infinitysolutions.musicsync.Databases.Playlists;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Playlist {

    @PrimaryKey(autoGenerate = true)
    private int pId;

    @ColumnInfo(name = "playlistName")
    private String playlistName;

    @ColumnInfo(name = "tableName")
    private String tableName;

    @ColumnInfo(name = "songsCount")
    private int songsCount;

    public int getPId(){
        return pId;
    }

    public void setPId(int pId){
        this.pId = pId;
    }

    public String getPlaylistName(){
        return playlistName;
    }

    public String getTableName(){
        return tableName;
    }

    public void setPlaylistName(String playlistName){
        this.playlistName = playlistName;
    }

    public void setTableName(String tableName){
        this.tableName = tableName;
    }

    public int getSongsCount(){
        return songsCount;
    }

    public void setSongsCount(int songsCount){
        this.songsCount = songsCount;
    }
}
