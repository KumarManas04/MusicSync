package com.infinitysolutions.musicsync.Fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.infinitysolutions.musicsync.Adapters.PlaylistsAdapter;
import com.infinitysolutions.musicsync.Adapters.SongsListAdapter;
import com.infinitysolutions.musicsync.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaylistSongsFragment extends Fragment implements LoaderManager.LoaderCallbacks<SongsListAdapter> {


    public PlaylistSongsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playlist_songs, container, false);
    }

    @NonNull
    @Override
    public Loader<SongsListAdapter> onCreateLoader(int id, @Nullable Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<SongsListAdapter> loader, SongsListAdapter data) {

    }

    @Override
    public void onLoaderReset(@NonNull Loader<SongsListAdapter> loader) {

    }
}
