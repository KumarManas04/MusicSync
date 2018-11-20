package com.infinitysolutions.musicsync.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infinitysolutions.musicsync.AdapterAsyncLoader;
import com.infinitysolutions.musicsync.Adapters.PlaylistsAdapter;
import com.infinitysolutions.musicsync.R;


public class PlaylistViewFragment extends Fragment
        implements
        LoaderManager.LoaderCallbacks<PlaylistsAdapter>,
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
        getActivity().getSupportLoaderManager().restartLoader(0,null,this).forceLoad();
        return rootView;
    }

    public void refreshPlaylists(){
        Log.d("HelloWorld","Reached refreshPlaylists");
        getActivity().getSupportLoaderManager().restartLoader(0,null,this).forceLoad();
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

    @NonNull
    @Override
    public android.support.v4.content.Loader<PlaylistsAdapter> onCreateLoader(int id, Bundle args) {
        Log.d("HelloWorld","Reached onCreateLoader");
        return new AdapterAsyncLoader(mContext);
    }

    @Override
    public void onLoadFinished(@NonNull android.support.v4.content.Loader<PlaylistsAdapter> loader, PlaylistsAdapter playlistsAdapter) {
        playlistsAdapter.setOnItemClickListener(this);
        if(playlistsAdapter.getItemCount() == 0){
            mPlaylistRecyclerView.setVisibility(View.GONE);
            recyclerEmptyTextView.setVisibility(View.VISIBLE);
        }else{
            mPlaylistRecyclerView.setVisibility(View.VISIBLE);
            recyclerEmptyTextView.setVisibility(View.GONE);
        }
        mPlaylistRecyclerView.setAdapter(playlistsAdapter);
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<PlaylistsAdapter> loader) {

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
