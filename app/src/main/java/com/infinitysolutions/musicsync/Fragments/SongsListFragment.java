package com.infinitysolutions.musicsync.Fragments;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.infinitysolutions.musicsync.R;
import com.infinitysolutions.musicsync.Adapters.SongsListAdapter;

public class SongsListFragment extends Fragment implements SongsListAdapter.OnItemClickListener{

    private RecyclerView mSongsRecyclerView;
    private TextView recyclerEmptyTextView;
    private OnFragmentInteractionListener mSongChangeListener;
    private Context mContext;

    public SongsListFragment() {
        // Required empty public constructor
    }

    public static SongsListFragment newInstance(){
        return new SongsListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_songs_list,container,false);
        mSongsRecyclerView = (RecyclerView)rootView.findViewById(R.id.songs_recycler_view);
        recyclerEmptyTextView = (TextView)rootView.findViewById(R.id.recycler_empty_view);
        loadSongsList();
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SongsListFragment.OnFragmentInteractionListener) {
            mSongChangeListener = (SongsListFragment.OnFragmentInteractionListener) context;
            mContext = context;
        } else {
            throw new ClassCastException(context.toString() + "Error onAttach of fragment");
        }

        if(mSongsRecyclerView == null){
            Log.d("Hello","Null recyclerView");
        }
    }

    private void loadSongsList(){
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        mSongsRecyclerView.setLayoutManager(layoutManager);

        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = mContext.getContentResolver().query(
                musicUri,
                null,
                MediaStore.Audio.Media.DATA + " like ? ",
                new String[]{"%MusicSync%"},
                null
        );

        if (cursor != null) {
            cursor.moveToFirst();
            if(cursor.getCount() == 0){
                mSongsRecyclerView.setVisibility(View.GONE);
                recyclerEmptyTextView.setVisibility(View.VISIBLE);
            }else {
                mSongsRecyclerView.setVisibility(View.VISIBLE);
                recyclerEmptyTextView.setVisibility(View.GONE);
                SongsListAdapter songsListAdapter = new SongsListAdapter(mContext, cursor);
                songsListAdapter.setOnItemClickListener(this);
                mSongsRecyclerView.setAdapter(songsListAdapter);
            }
        }
    }

    @Override
    public void setAndPlaySong(long albumId, String songName, String artistName, String uriData) {
        mSongChangeListener.playSong(albumId,songName,artistName,uriData);
    }

    public interface OnFragmentInteractionListener{
        void playSong(long albumId, String songName, String artistName, String uriData);
    }
}
