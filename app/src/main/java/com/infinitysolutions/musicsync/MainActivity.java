package com.infinitysolutions.musicsync;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.infinitysolutions.musicsync.MediaPlayerService.MediaPlayerBinder;

import java.util.Objects;

import static com.infinitysolutions.musicsync.Contract.ValuesContract.RS_HANDLE_PERM;
import static com.infinitysolutions.musicsync.Contract.ValuesContract.SHARED_PREF_ALBUM_ID;
import static com.infinitysolutions.musicsync.Contract.ValuesContract.SHARED_PREF_ARTIST_NAME;
import static com.infinitysolutions.musicsync.Contract.ValuesContract.SHARED_PREF_NAME;
import static com.infinitysolutions.musicsync.Contract.ValuesContract.SHARED_PREF_SONG_NAME;
import static com.infinitysolutions.musicsync.Contract.ValuesContract.SHARED_PREF_URI_DATA;
import static com.infinitysolutions.musicsync.Contract.ValuesContract.SNACKBAR_STORAGE_PERMISSION_REQUEST_MESSAGE;

public class MainActivity extends AppCompatActivity {

    private MediaPlayerService mediaPlayerService;
    private Intent playerIntent;
    private Boolean ready;
    private Button playPauseButton;
    private ProgressBar mProgressBar;
    private Handler handler;
    private boolean playWhenConnected;
    private RecyclerView songsListRecyclerView;
    private ImageView mSongArtImageView;
    private TextView mSongNameTextView;
    private TextView mArtistNameTextView;
    private boolean isSwipeDetected;
    private String mSongName;
    private String mArtistName;
    private long mAlbumId;
    private String mUriData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        Log.d("Hey buddy", "Activity onCreate");
        ready = false;
        handler = new Handler();
        playPauseButton = (Button) findViewById(R.id.play_pause);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mSongArtImageView = (ImageView) findViewById(R.id.song_art_image_view);
        mSongNameTextView = (TextView) findViewById(R.id.song_name_text_view);
        mArtistNameTextView = (TextView) findViewById(R.id.artist_name_text_view);
        songsListRecyclerView = (RecyclerView) findViewById(R.id.songs_recycler_view);
        playerIntent = new Intent(this, MediaPlayerService.class);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        songsListRecyclerView.setLayoutManager(layoutManager);

        SimpleGestureListener simpleGestureListener = new SimpleGestureListener();
        simpleGestureListener.setListener(new SimpleGestureListener.Listener() {
            @Override
            public void onScrollVertical(float dy) {
                if (dy > 0) {
                    if (!isSwipeDetected) {
                        isSwipeDetected = true;
                        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                        intent.putExtra("title", mSongName);
                        intent.putExtra("artist", mArtistName);
                        intent.putExtra("albumId", mAlbumId);
                        intent.putExtra("uriData", mUriData);
                        startActivity(intent);
                    }
                }
            }
        });
        final GestureDetector detector = new GestureDetector(this, simpleGestureListener);

        findViewById(R.id.player_controls).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return detector.onTouchEvent(event);
            }
        });
    }

    @Override
    protected void onStart() {
        Log.d("Hey buddy", "Activity onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Hey buddy", "Activity onResume");

        //Checking and requesting storage permission
        int rs = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rs != PackageManager.PERMISSION_GRANTED) {
            //Permission not granted then request
            Log.d("MainActivity.class", "Permission not granted");
            final String[] permission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission[0])) {
                //Should show permission so requesting
                ActivityCompat.requestPermissions(this, permission, RS_HANDLE_PERM);
            } else {

                //Should not show permission dialog show snackbar prompt to ask permission
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(MainActivity.this, permission, RS_HANDLE_PERM);
                    }
                };

                View parentLayout = findViewById(R.id.parent);
                Snackbar.make(parentLayout, SNACKBAR_STORAGE_PERMISSION_REQUEST_MESSAGE, Snackbar.LENGTH_INDEFINITE)
                        .setAction("Ok", listener)
                        .setActionTextColor(this.getResources().getColor(R.color.snackbar_action_color))
                        .show();
            }
        } else {
            //Permission granted load songs list
            loadSongsList();
        }

        if (isServiceRunning()) {
            //Service already running binding to it
            ready = false;
            bindService(playerIntent, mediaPlayerConnection, Context.BIND_AUTO_CREATE);
        } else {
            handler.removeCallbacks(mShowProgress);
            playPauseButton.setBackgroundResource(R.drawable.play);
        }
        isSwipeDetected = false;
        playWhenConnected = false;
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.InfinitySolutions.ACTION_PLAY_PAUSE");
        filter.addAction("com.InfinitySolutions.DELETE_INTENT");
        filter.addAction("com.InfinitySolutions.TRACK_SKIPPED");
        registerReceiver(receiver, filter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RS_HANDLE_PERM) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission granted now load the songs list
                loadSongsList();
            } else {

                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setPositiveButton("Ok", listener)
                        .setTitle("Music Sync")
                        .setMessage("Couldn't obtain storage permission.Music Sync will now exit.")
                        .show();
            }
        } else {
            Log.d("MainActivity.class", "Unknown request code received");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Hey buddy", "Activity onPause");
        unregisterReceiver(receiver);
        if (isServiceRunning()) {
            unbindService(mediaPlayerConnection);
        }
    }

    public void openPlayer(View view) {
        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
        intent.putExtra("title", mSongName);
        intent.putExtra("artist", mArtistName);
        intent.putExtra("albumId", mAlbumId);
        intent.putExtra("uriData", mUriData);
        startActivity(intent);
    }

    public void setSongAndPlay(long albumId, String songName, String artistName, String uriData) {
        mSongName = songName;
        mArtistName = artistName;
        mAlbumId = albumId;
        mUriData = uriData;
        if (!ready) {
            playerIntent.putExtra("title", songName);
            playerIntent.putExtra("artist", artistName);
            playerIntent.putExtra("uriData", uriData);
            playerIntent.putExtra("albumId", albumId);
            setPlayerDetails(songName, artistName, albumId);
            playWhenConnected = true;
            Log.d("Hey buddy", "Starting service");
            bindService(playerIntent, mediaPlayerConnection, Context.BIND_AUTO_CREATE);
            startService(playerIntent);
        } else {
            setPlayerDetails(songName, artistName, albumId);
            mediaPlayerService.setSongAndPlay(albumId, songName, artistName, uriData);
        }
    }

    private void loadSongsList() {
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = this.getContentResolver().query(
                musicUri,
                null,
                MediaStore.Audio.Media.DATA + " like ? ",
                new String[]{"%MusicSync%"},
                null
        );

        String songName = "";
        String artistName = "";
        String uriData = "";
        long albumId = 0;

        if (cursor != null) {
            cursor.moveToFirst();
            SongsListAdapter songsListAdapter = new SongsListAdapter(this, cursor);
            songsListRecyclerView.setAdapter(songsListAdapter);

            songName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            artistName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            uriData = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        }

        //Retrieving last played song from sharedPreferences
        SharedPreferences sharedPrefs = this.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mSongName = sharedPrefs.getString(SHARED_PREF_SONG_NAME, songName);
        mArtistName = sharedPrefs.getString(SHARED_PREF_ARTIST_NAME, artistName);
        mUriData = sharedPrefs.getString(SHARED_PREF_URI_DATA, uriData);
        mAlbumId = sharedPrefs.getLong(SHARED_PREF_ALBUM_ID, albumId);

        playerIntent.putExtra("title", mSongName);
        playerIntent.putExtra("artist", mArtistName);
        playerIntent.putExtra("uriData", mUriData);
        playerIntent.putExtra("albumId", mAlbumId);

        setPlayerDetails(mSongName, mArtistName, mAlbumId);
    }

    public void playPause(View view) {
        if (!ready) {
            playWhenConnected = true;
            Log.d("Hey buddy", "Starting service");
            bindService(playerIntent, mediaPlayerConnection, Context.BIND_AUTO_CREATE);
            startService(playerIntent);
        } else {
            mediaPlayerService.playPauseMusic();
        }
    }

    private void setPlayPauseButtonBackground() {
        mProgressBar.setProgress(mediaPlayerService.getCurrentPosition());
        if (mediaPlayerService.isMediaPlaying()) {
            int pos = mediaPlayerService.getCurrentPosition();
            Log.d("Hey bids", "Media is playing");
            handler.postDelayed(mShowProgress, 1000 - (pos % 1000));
            playPauseButton.setBackgroundResource(R.drawable.pause);
        } else {
            handler.removeCallbacks(mShowProgress);
            Log.d("Hey bids", "Media is not playing");
            playPauseButton.setBackgroundResource(R.drawable.play);
        }
    }

    private void setPlayerDetails(String songName, String artistName, long albumId) {
        mSongNameTextView.setText(songName);
        mArtistNameTextView.setText(artistName);
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);

        Glide.with(this)
                .load(albumArtUri)
                .apply(new RequestOptions().placeholder(R.drawable.song_placeholder))
                .into(mSongArtImageView);
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.infinitysolutions.musicsync.MediaPlayerService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void stopServiceAndDisconnect() {
        unbindService(mediaPlayerConnection);
        mediaPlayerService = null;
        ready = false;
    }

    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            mProgressBar.setProgress(mediaPlayerService.getCurrentPosition());
            handler.postDelayed(mShowProgress, 1000 - (mediaPlayerService.getCurrentPosition() % 1000));
        }
    };

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case "com.InfinitySolutions.ACTION_PLAY_PAUSE":
                    setPlayPauseButtonBackground();
                    break;
                case "com.InfinitySolutions.DELETE_INTENT":
                    stopServiceAndDisconnect();
                    break;
                case "com.InfinitySolutions.TRACK_SKIPPED":
                    mUriData = intent.getStringExtra("uriData");
                    setPlayerDetails(
                            intent.getStringExtra("title"),
                            intent.getStringExtra("artist"),
                            intent.getLongExtra("albumId", 0)
                    );
                    break;
            }
        }
    };

    private ServiceConnection mediaPlayerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("HeyBuddy", "Service connected");
            MediaPlayerBinder mediaPlayerBinder = (MediaPlayerBinder) service;
            mediaPlayerService = mediaPlayerBinder.getService();
            mProgressBar.setMax(200);
            setPlayPauseButtonBackground();
            if (playWhenConnected) {
                mediaPlayerService.playPauseMusic();
                playWhenConnected = false;
            } else {
                setPlayerDetails(mediaPlayerService.getSongName(), mediaPlayerService.getArtistName(), mediaPlayerService.getAlbumId());
            }
            ready = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("Hey buddy", "Service disconnected");
            ready = false;
        }
    };
}