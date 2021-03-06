package com.infinitysolutions.musicsync.Fragments;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.infinitysolutions.musicsync.Adapters.PlaylistsAdapter;
import com.infinitysolutions.musicsync.Adapters.SongsListAdapter;
import com.infinitysolutions.musicsync.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaylistSongsFragment extends Fragment {

    public PlaylistSongsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playlist_songs, container, false);
    }
}
