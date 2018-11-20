package com.infinitysolutions.musicsync.Databases.PlaylistSongs;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class PlaylistSongs {

    @PrimaryKey(autoGenerate = true)
    private int sId;

    @ColumnInfo(name="title")
    private String title;

    @ColumnInfo(name = "artist")
    private String artist;

    @ColumnInfo(name = "uriData")
    private String uriData;

    public void setSId(int sId){
        this.sId = sId;
    }

    public int getSId(){
        return sId;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getTitle(){
        return title;
    }

    public void setArtist(String artist){
        this.artist = artist;
    }

    public String getArtist(){
        return artist;
    }

    public void setUriData(String uriData){
        this.uriData = uriData;
    }

    public String getUriData(){
        return uriData;
    }
}
