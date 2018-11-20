package com.infinitysolutions.musicsync.Fragments;


import androidx.room.Room;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infinitysolutions.musicsync.Adapters.PlaylistsAdapter;
import com.infinitysolutions.musicsync.Databases.Playlists.Playlist;
import com.infinitysolutions.musicsync.Databases.Playlists.PlaylistDao;
import com.infinitysolutions.musicsync.Databases.Playlists.PlaylistDatabase;
import com.infinitysolutions.musicsync.R;

import java.util.ArrayList;
import java.util.List;


public class PlaylistViewFragment extends Fragment
        implements
        PlaylistsAdapter.OnPlaylistClickListener{

    private RecyclerView mPlaylistRecyclerView;
    private TextView recyclerEmptyTextView;
    private OnFragmentInteractionListener mPlaylistSelectListener;
    private Context mContext;

    public PlaylistViewFragment() {
        // Required empty public constructor
    }

    public static PlaylistViewFragment newInstance(){
        return new PlaylistViewFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playlist_view,container,false);
        mPlaylistRecyclerView = (RecyclerView)rootView.findViewById(R.id.playlist_songs_recycler_view);
        recyclerEmptyTextView = (TextView)rootView.findViewById(R.id.recycler_empty_view);
        LinearLayout createPlaylistButton = (LinearLayout)rootView.findViewById(R.id.create_playlist_button);
        createPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlaylistSelectListener.createPlaylist();
            }
        });

        Log.d("HelloWorld","Reached onCreateView");
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        mPlaylistRecyclerView.setLayoutManager(layoutManager);
        loadPlaylists();
        return rootView;
    }

    public void refreshPlaylists(){
        Log.d("HelloWorld","Reached refreshPlaylists");
        loadPlaylists();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mPlaylistSelectListener = (OnFragmentInteractionListener) context;
            mContext = context;
        } else {
            throw new ClassCastException(context.toString() + "Error onAttach of fragment");
        }
    }

    private void loadPlaylists(){
        final List<Playlist> playlists = new ArrayList<Playlist>();

        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(msg.what == 0){
                    PlaylistsAdapter playlistsAdapter = new PlaylistsAdapter(playlists);
                    playlistsAdapter.setOnItemClickListener(PlaylistViewFragment.this);
                    if(playlistsAdapter.getItemCount() == 0){
                        mPlaylistRecyclerView.setVisibility(View.GONE);
                        recyclerEmptyTextView.setVisibility(View.VISIBLE);
                    }else{
                        mPlaylistRecyclerView.setVisibility(View.VISIBLE);
                        recyclerEmptyTextView.setVisibility(View.GONE);
                    }
                    mPlaylistRecyclerView.setAdapter(playlistsAdapter);
                }
                return true;
            }
        });

        Thread thread = new Thread(){
            @Override
            public void run() {
                PlaylistDatabase pDb = Room.databaseBuilder(mContext, PlaylistDatabase.class, "Playlists").build();
                PlaylistDao playlistDao = pDb.playlistDao();
                playlists.clear();
                playlists.addAll(playlistDao.getAll());
                handler.sendEmptyMessage(0);
            }
        };

        thread.start();
    }

    @Override
    public void openPlaylist(String tableName) {
        Log.d("HelloWorld","openPlaylist called " + tableName);
        mPlaylistSelectListener.openPlaylist(tableName);
    }

    public interface OnFragmentInteractionListener{
        void openPlaylist(String tableName);
        void createPlaylist();
    }
}
