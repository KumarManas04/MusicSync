package com.infinitysolutions.musicsync;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import static com.infinitysolutions.musicsync.Contract.ValuesContract.SHARED_PREF_ALBUM_ID;
import static com.infinitysolutions.musicsync.Contract.ValuesContract.SHARED_PREF_ARTIST_NAME;
import static com.infinitysolutions.musicsync.Contract.ValuesContract.SHARED_PREF_NAME;
import static com.infinitysolutions.musicsync.Contract.ValuesContract.SHARED_PREF_SONG_NAME;
import static com.infinitysolutions.musicsync.Contract.ValuesContract.SHARED_PREF_URI_DATA;

public class PlayerActivity extends AppCompatActivity {
    private ImageView mSongCoverImageView;
    private ImageView mSongCoverBackgroundImageView;
    private TextView mSongTextView;
    private TextView mArtistTextView;
    private MediaPlayerService mediaPlayerService;
    private SeekBar mSeekBar;
    private Handler handler;
    private ImageButton playPauseButton;
    private Intent playerIntent;
    private boolean ready;
    private boolean playWhenConnected;
    private String mTitle;
    private String mArtist;
    private long mAlbumId;
    private String mUriData;
    private TextView mDurationTextView;
    private TextView mCurrentTextView;
    private int mRepeatMode;
    private int mShuffleMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Objects.requireNonNull(getSupportActionBar()).hide();

        mSongCoverImageView = (ImageView) findViewById(R.id.song_cover_image_view);
        mSongCoverImageView.setClipToOutline(true);
        mSongCoverBackgroundImageView = (ImageView) findViewById(R.id.background_song_cover_image_view);
        mSongTextView = (TextView) findViewById(R.id.player_title_text_view);
        mArtistTextView = (TextView) findViewById(R.id.player_artist_text_view);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setEnabled(false);
        playPauseButton = (ImageButton) findViewById(R.id.play_pause_button);
        playerIntent = new Intent(this, MediaPlayerService.class);
        mDurationTextView = (TextView) findViewById(R.id.total_duration_text_view);
        mCurrentTextView = (TextView) findViewById(R.id.current_position_text_view);
        handler = new Handler();

        mRepeatMode = 0;

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mediaPlayerService.seekToPosition(progress * mediaPlayerService.getMediaDuration() / 200);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        ready = false;
        playWhenConnected = false;

        if (isServiceRunning()) {
            bindToMediaService();
        }else{
            mDurationTextView.setText("00:00");
            mCurrentTextView.setText("00:00");
            handler.removeCallbacks(mShowProgress);
            playPauseButton.setImageResource(R.drawable.play);
            SharedPreferences sharedPrefs = this.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
            mTitle = sharedPrefs.getString(SHARED_PREF_SONG_NAME,intent.getStringExtra("title"));
            mArtist = sharedPrefs.getString(SHARED_PREF_ARTIST_NAME,intent.getStringExtra("artist"));
            mUriData = sharedPrefs.getString(SHARED_PREF_URI_DATA,intent.getStringExtra("uriData"));
            mAlbumId = sharedPrefs.getLong(SHARED_PREF_ALBUM_ID,intent.getLongExtra("albumId", 0));
            setPlayerDetails(mTitle, mArtist, mAlbumId);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.InfinitySolutions.ACTION_PLAY_PAUSE");
        filter.addAction("com.InfinitySolutions.DELETE_INTENT");
        filter.addAction("com.InfinitySolutions.TRACK_SKIPPED");
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isServiceRunning()) {
            unbindService(mediaPlayerConnection);
        }
        unregisterReceiver(receiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        Intent intent = new Intent(this,MainActivity.class);
//        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void stopServiceAndDisconnect() {
        unbindService(mediaPlayerConnection);
        mediaPlayerService = null;
        ready = false;
    }

    private void bindToMediaService() {
        playerIntent.putExtra("title", mTitle);
        playerIntent.putExtra("artist", mArtist);
        playerIntent.putExtra("albumId", mAlbumId);
        playerIntent.putExtra("uriData", mUriData);
        bindService(playerIntent, mediaPlayerConnection, Context.BIND_AUTO_CREATE);
    }

    private void setPlayerDetails(String title, String artist, long albumId) {
        mSongTextView.setText(title);
        mArtistTextView.setText(artist);
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
        Glide.with(this)
                .load(albumArtUri)
                .apply(new RequestOptions().placeholder(R.drawable.song_placeholder))
                .into(mSongCoverImageView);

        Glide.with(this)
                .load(albumArtUri)
                .apply(new RequestOptions().placeholder(R.drawable.song_placeholder))
                .into(mSongCoverBackgroundImageView);

        ImageButton rpBtn = (ImageButton)findViewById(R.id.repeat_button);
        switch(mRepeatMode){
            case 0:
                rpBtn.setImageResource(R.drawable.repeat_all);
                break;
            case 1:
                rpBtn.setImageResource(R.drawable.repeat_one_enabled);
                break;
            case 2:
                rpBtn.setImageResource(R.drawable.repeat_all_enabled);
                break;
        }

        ImageButton shBtn = (ImageButton)findViewById(R.id.shuffle_button);
        if(mShuffleMode == 1){
            shBtn.setImageResource(R.drawable.shuffle_enabled);
        }else{
            shBtn.setImageResource(R.drawable.shuffle);
        }
    }

    public void playPause(View view) {
        if (!ready) {
            playWhenConnected = true;
            Log.d("Hey buddy", "Starting service");
            bindToMediaService();
            startService(playerIntent);
        } else {
            mediaPlayerService.playPauseMusic();
        }
    }

    public void nextTrack(View view) {
        if (ready) {
            mediaPlayerService.nextTrack();
        }
    }

    public void previousTrack(View view) {
        if (ready) {
            mediaPlayerService.previousTrack();
        }
    }

    public void repeat(View view){
        ImageButton imgBtn = (ImageButton)view;
        switch(mRepeatMode){
            case 0:
                mRepeatMode = 1;
                imgBtn.setImageResource(R.drawable.repeat_one_enabled);
                break;
            case 1:
                mRepeatMode = 2;
                imgBtn.setImageResource(R.drawable.repeat_all_enabled);
                break;
            case 2:
                mRepeatMode = 0;
                imgBtn.setImageResource(R.drawable.repeat_all);
                break;
        }
        if(ready){
            mediaPlayerService.setRepeatMode(mRepeatMode);
        }
    }

    public void shuffle(View view){
        ImageButton imgBtn = (ImageButton)view;
        switch(mShuffleMode){
            case 0:
                mShuffleMode = 1;
                imgBtn.setImageResource(R.drawable.shuffle_enabled);
                break;
            case 1:
                mShuffleMode = 0;
                imgBtn.setImageResource(R.drawable.shuffle);
                break;
        }

        if(ready){
            mediaPlayerService.setShuffleMode(mShuffleMode);
        }
    }

    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            mSeekBar.setProgress(mediaPlayerService.getCurrentPosition());
            long millis = mediaPlayerService.getPlayingDuration();
            String time = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            mCurrentTextView.setText(time);
            handler.postDelayed(mShowProgress, 1000);
        }
    };

    private void setPlayPauseButtonBackground() {
        mSeekBar.setProgress(mediaPlayerService.getCurrentPosition());
        long millis = mediaPlayerService.getMediaDuration();
        String time = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        mDurationTextView.setText(time);
        millis = mediaPlayerService.getPlayingDuration();
        time = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        mCurrentTextView.setText(time);
        if (mediaPlayerService.isMediaPlaying()) {
            Log.d("Hey bids", "Media is playing");
            handler.postDelayed(mShowProgress, 1000);
            playPauseButton.setImageResource(R.drawable.pause);
        } else {
            handler.removeCallbacks(mShowProgress);
            Log.d("Hey bids", "Media is not playing");
            playPauseButton.setImageResource(R.drawable.play);
        }
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
            Log.d("Hey buddy", "Service connected");
            MediaPlayerService.MediaPlayerBinder mediaPlayerBinder = (MediaPlayerService.MediaPlayerBinder) service;
            mediaPlayerService = mediaPlayerBinder.getService();
            setPlayPauseButtonBackground();
            ready = true;

            //Check if repeat mode is already set in the service and then sync it with ui
            if(mediaPlayerService.getRepeatMode() == 0){
                mediaPlayerService.setRepeatMode(mRepeatMode);
            }else {
                mRepeatMode = mediaPlayerService.getRepeatMode();
            }

            //Check if shuffle mode is already set in the service and then sync it with ui
            if(mediaPlayerService.getShuffleMode() == 0){
                mediaPlayerService.setShuffleMode(mShuffleMode);
            }else{
                mShuffleMode = mediaPlayerService.getShuffleMode();
            }
            mSeekBar.setEnabled(true);
            if (playWhenConnected) {
                playWhenConnected = false;
                mediaPlayerService.playPauseMusic();
            }else{
                setPlayerDetails(mediaPlayerService.getSongName(),mediaPlayerService.getArtistName(),mediaPlayerService.getAlbumId());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("Hey buddy", "Service disconnected");
            mSeekBar.setEnabled(false);
        }
    };
}
