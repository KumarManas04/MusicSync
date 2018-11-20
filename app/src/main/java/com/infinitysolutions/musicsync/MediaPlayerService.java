package com.infinitysolutions.musicsync;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;

import java.util.Random;

import static com.infinitysolutions.musicsync.Contract.ValuesContract.SHARED_PREF_ALBUM_ID;
import static com.infinitysolutions.musicsync.Contract.ValuesContract.SHARED_PREF_ARTIST_NAME;
import static com.infinitysolutions.musicsync.Contract.ValuesContract.SHARED_PREF_NAME;
import static com.infinitysolutions.musicsync.Contract.ValuesContract.SHARED_PREF_SONG_NAME;
import static com.infinitysolutions.musicsync.Contract.ValuesContract.SHARED_PREF_URI_DATA;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener {

    private static MediaPlayer mMediaPlayer;
    private static MediaSessionCompat mMediaSessionCompat;
    private final IBinder mediaPlayerBinder = new MediaPlayerBinder();
    private AudioManager mAudioManager;
    private AudioFocusRequest mAudioFocusRequest;
    private String mSongName;
    private String mArtistName;
    private String mUriData;
    private long mAlbumId;
    private Cursor mCursor;
    private int mRepeatMode;
    private int songsCount;
    private int playedSongsCount;
    private int mShuffleMode;
    private char mShuffleArray[];

    public MediaPlayerService() {
        Log.d("Hey guys what's up", "Service constructor");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Hey buddy", "Service onCreate");
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.InfinitySolutions.ACTION_PLAY_PAUSE_MUSIC");
        filter.addAction("com.InfinitySolutions.DELETE_INTENT");
        filter.addAction("com.InfinitySolutions.NEXT_TRACK");
        filter.addAction("com.InfinitySolutions.PREVIOUS_TRACK");
        registerReceiver(receiver, filter);

        mRepeatMode = 0;
        playedSongsCount = 0;

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            AudioAttributes mAudioAttributes =
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build();
            mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(mAudioAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(afChangeListener).build();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Hey buddy", "Service onDestroy");

        //Writing last played song details to sharedPreferences
        SharedPreferences sharedPrefs = this.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(SHARED_PREF_SONG_NAME, mSongName);
        editor.putString(SHARED_PREF_ARTIST_NAME, mArtistName);
        editor.putString(SHARED_PREF_URI_DATA, mUriData);
        editor.putLong(SHARED_PREF_ALBUM_ID, mAlbumId);
        editor.apply();

        stopForeground(false);
        unregisterReceiver(receiver);
        mCursor.close();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Hey buddy", "Service onStart");

        mSongName = intent.getStringExtra("title");
        mArtistName = intent.getStringExtra("artist");
        mUriData = intent.getStringExtra("uriData");
        mAlbumId = intent.getLongExtra("albumId", 0);

        initMediaPlayer();
        initMediaSession();
        startForegroundMediaService();
        MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent);

        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        mCursor = this.getContentResolver().query(
                musicUri,
                null,
                MediaStore.Audio.Media.DATA + " like ? ",
                new String[]{"%MusicSync%"},
                null
        );

        songsCount = 1;
        if (mCursor != null) {
            mCursor.moveToFirst();
            while (mCursor.moveToNext()) {
                songsCount++;
            }
        }
        mShuffleArray = new char[songsCount];

        if (mCursor != null) {
            mCursor.moveToPosition(-1);
            while (mCursor.moveToNext()) {
                if (mAlbumId == mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))) {
                    break;
                }
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Hey buddy", "Service OnBind");
        return mediaPlayerBinder;
    }

    public String getSongName() {
        return mSongName;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public long getAlbumId() {
        return mAlbumId;
    }

    private boolean audioFocusGranted() {
        int res;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            res = mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
        } else {
            res = mAudioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        }
        return false;
    }

    public void stopMediaService() {
        stopSelf();
    }

    private void startForegroundMediaService() {
        int playPauseIcon = R.drawable.pause;
        boolean isOngoing = true;
        if (!isMediaPlaying()) {
            playPauseIcon = R.drawable.play;
            isOngoing = false;
        }
        startForeground(111, createNotification(playPauseIcon, isOngoing));
    }

    public boolean isMediaPlaying() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            return true;
        } else {
            return false;
        }
    }

    public void nextTrack() {
        if (mShuffleMode == 1) {
            if (playedSongsCount < songsCount) {
                int randomNo = 0;
                playedSongsCount++;
                do {
                    Random rand = new Random();
                    randomNo = rand.nextInt(songsCount);
                } while ((int) mShuffleArray[randomNo] != 0);
                mShuffleArray[randomNo] = 'u';

                mCursor.moveToPosition(randomNo);
                skipTrack();
            } else {
                playedSongsCount = 1;
                Random rand = new Random();
                int randomNo = 0;

                //Checking that last played song is not selected again
                do {
                    randomNo = rand.nextInt(songsCount);
                } while (randomNo == mCursor.getPosition());

                //Resetting the playlist
                for (int i = 0; i < songsCount; i++) {
                    mShuffleArray[i] = Character.MIN_VALUE;
                }
                mShuffleArray[randomNo] = 'u';
                mCursor.moveToPosition(randomNo);
                skipTrack();
            }
        } else {
            if (mCursor.getPosition() < mCursor.getCount() - 1) {
                mCursor.moveToNext();
            } else {
                mCursor.moveToFirst();
            }
        }
        skipTrack();
    }

    public void previousTrack() {
        if (mCursor.getPosition() > 0) {
            mCursor.moveToPrevious();
        } else {
            mCursor.moveToLast();
        }
        skipTrack();
    }

    private void skipTrack() {
        mAlbumId = mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        mSongName = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        mArtistName = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        mUriData = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

        setSongAndPlay(mAlbumId, mSongName, mArtistName, mUriData);

        Intent intent = new Intent("com.InfinitySolutions.TRACK_SKIPPED");
        intent.putExtra("title", mSongName);
        intent.putExtra("artist", mArtistName);
        intent.putExtra("albumId", mAlbumId);
        intent.putExtra("uriData", mUriData);
        sendBroadcast(intent);
    }

    public void playMusic() {
        Log.d("Hey guys what's up", "Play pressed");
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying() && audioFocusGranted()) {
            mMediaPlayer.start();
        }
        startForegroundMediaService();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(111, createNotification(R.drawable.pause, true));
    }

    public void pauseMusic() {
        Log.d("Hey guys what's up", "Pause pressed");
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
            } else {
                mAudioManager.abandonAudioFocus(afChangeListener);
            }
        }
        stopForeground(false);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(111, createNotification(R.drawable.play, false));
    }

    public void playPauseMusic() {
        if (isMediaPlaying()) {
            pauseMusic();
        } else {
            playMusic();
        }
        Intent intent = new Intent("com.InfinitySolutions.ACTION_PLAY_PAUSE");
        sendBroadcast(intent);
    }

    public void setSongAndPlay(long albumId, String title, String artist, String uriData) {
        try {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
            mMediaPlayer.reset();
            mUriData = uriData;
            mSongName = title;
            mArtistName = artist;
            mAlbumId = albumId;
            Uri mediaUri = Uri.parse(mUriData);
            mMediaPlayer = MediaPlayer.create(this, mediaUri);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.prepare();
            mMediaPlayer.seekTo(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        playPauseMusic();
    }

    private void initMediaPlayer() {
        Uri mediaUri = Uri.parse(mUriData);
        mMediaPlayer = MediaPlayer.create(this, mediaUri);
        mMediaPlayer.setOnCompletionListener(this);
    }

    private Notification createNotification(int playPauseIcon, boolean isOngoing) {
        String CHANNEL_ID = "music";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Music Sync";
            String description = "Music Player";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("title", mSongName);
        intent.putExtra("artist", mArtistName);
        intent.putExtra("albumId", mAlbumId);
        intent.putExtra("uriData", mUriData);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent playPauseIntent = new Intent("com.InfinitySolutions.ACTION_PLAY_PAUSE_MUSIC");
        PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(this, 1, playPauseIntent, 0);

        Intent deleteIntent = new Intent("com.InfinitySolutions.DELETE_INTENT");
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(this, 1, deleteIntent, 0);

        Intent nextTrackIntent = new Intent("com.InfinitySolutions.NEXT_TRACK");
        PendingIntent nextTrackPendingIntent = PendingIntent.getBroadcast(this, 1, nextTrackIntent, 0);

        Intent previousTrackIntent = new Intent("com.InfinitySolutions.PREVIOUS_TRACK");
        PendingIntent previousTrackPendingIntent = PendingIntent.getBroadcast(this, 1, previousTrackIntent, 0);

        RemoteViews expandedView = new RemoteViews(getPackageName(), R.layout.notification_large);
        RemoteViews compactView = new RemoteViews(getPackageName(), R.layout.notification_compact);

        expandedView.setImageViewResource(R.id.nl_previous_track_image_view, R.drawable.previous_track);
        expandedView.setImageViewResource(R.id.nl_play_pause_image_view, playPauseIcon);
        expandedView.setImageViewResource(R.id.nl_next_track_image_view, R.drawable.next_track);
        expandedView.setTextViewText(R.id.nl_song_name_text_view, mSongName);
        expandedView.setTextViewText(R.id.nl_artist_name_text_view, mArtistName);
        expandedView.setOnClickPendingIntent(R.id.nl_play_pause_image_view, playPausePendingIntent);
        expandedView.setOnClickPendingIntent(R.id.nl_next_track_image_view, nextTrackPendingIntent);
        expandedView.setOnClickPendingIntent(R.id.nl_previous_track_image_view, previousTrackPendingIntent);

        compactView.setImageViewResource(R.id.nc_previous_track_image_view, R.drawable.previous_track);
        compactView.setImageViewResource(R.id.nc_play_pause_image_view, playPauseIcon);
        compactView.setImageViewResource(R.id.nc_next_track_image_view, R.drawable.next_track);
        compactView.setTextViewText(R.id.nc_song_name_text_view, mSongName);
        compactView.setTextViewText(R.id.nc_artist_name_text_view, mArtistName);
        compactView.setOnClickPendingIntent(R.id.nc_play_pause_image_view, playPausePendingIntent);
        compactView.setOnClickPendingIntent(R.id.nc_next_track_image_view, nextTrackPendingIntent);
        compactView.setOnClickPendingIntent(R.id.nc_previous_track_image_view, previousTrackPendingIntent);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(mSongName)
                .setContentText(mArtistName)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .setCustomContentView(compactView)
                .setCustomBigContentView(expandedView)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                .setAutoCancel(false)
                .setSound(null)
                .setOngoing(isOngoing);

        Notification notification = notificationBuilder.build();

        NotificationTarget notificationTargetExpanded = new NotificationTarget(
                this,
                R.id.nl_song_art_image_view,
                expandedView,
                notification,
                111);

        NotificationTarget notificationTargetCompact = new NotificationTarget(
                this,
                R.id.nc_song_art_image_view,
                compactView,
                notification,
                111);

        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, mAlbumId);

        Glide
                .with(getApplicationContext())
                .asBitmap()
                .load(albumArtUri)
                .into(notificationTargetExpanded);

        Glide
                .with(getApplicationContext())
                .asBitmap()
                .load(albumArtUri)
                .into(notificationTargetCompact);

        return notification;
    }

    private void initMediaSession() {
        mMediaSessionCompat = new MediaSessionCompat(this, "Hello friends");
        PlaybackStateCompat.Builder mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackState.ACTION_PLAY |
                                PlaybackState.ACTION_PAUSE |
                                PlaybackState.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackState.ACTION_PLAY_PAUSE);

        mMediaSessionCompat.setPlaybackState(mStateBuilder.build());
        mMediaSessionCompat.setCallback(mediaSessionCompatCallback);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mMediaSessionCompat.setMediaButtonReceiver(pendingIntent);

        mMediaSessionCompat.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSessionCompat.setActive(true);
    }

    private void setMediaPlaybackState(int state) {
        PlaybackStateCompat.Builder playbackStateBuilder = new PlaybackStateCompat.Builder();
        if (state == PlaybackState.STATE_PLAYING) {
            playbackStateBuilder.setActions(PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_PAUSE);
        } else {
            playbackStateBuilder.setActions(PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_PLAY);
        }
        playbackStateBuilder.setState(state, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0);
        mMediaSessionCompat.setPlaybackState(playbackStateBuilder.build());
    }

    public int getMediaDuration() {
        return mMediaPlayer.getDuration();
    }

    public int getPlayingDuration() {
        return mMediaPlayer.getCurrentPosition();
    }

    public int getCurrentPosition() {
        if (mMediaPlayer != null) {
            int currPos = mMediaPlayer.getCurrentPosition();
            int duration = mMediaPlayer.getDuration();
            return (currPos * 200) / duration;
        }
        return 0;
    }

    public void seekToPosition(int pos) {
        mMediaPlayer.seekTo(pos);
    }

    public void setRepeatMode(int repeatMode) {
        mRepeatMode = repeatMode;
    }

    public int getRepeatMode() {
        return mRepeatMode;
    }

    public void setShuffleMode(int shuffleMode) {
        mShuffleMode = shuffleMode;
    }

    public int getShuffleMode() {
        return mShuffleMode;
    }

    private MediaSessionCompat.Callback mediaSessionCompatCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            playPauseMusic();
            setMediaPlaybackState(PlaybackState.STATE_PLAYING);
            super.onPlay();
        }

        @Override
        public void onPause() {
            playPauseMusic();
            setMediaPlaybackState(PlaybackState.STATE_PAUSED);
            super.onPause();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
        }
    };

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d("Hello", "Music play completed");
        pauseMusic();
        if (mRepeatMode == 2) {
            nextTrack();
        } else if (mRepeatMode == 1) {
            skipTrack();
        } else if (mRepeatMode == 0) {

            if (mShuffleMode == 1) {
                if (playedSongsCount < songsCount) {
                    int randomNo = 0;
                    playedSongsCount++;
                    do {
                        Random rand = new Random();
                        randomNo = rand.nextInt(songsCount);
                        Log.d("HeyBud", "random no. = " + randomNo);
                    } while ((int) mShuffleArray[randomNo] != 0);
                    mShuffleArray[randomNo] = 'u';

                    mCursor.moveToPosition(randomNo);
                    skipTrack();
                } else {
                    playedSongsCount = 0;
                    for (int i = 0; i < songsCount; i++) {
                        mShuffleArray[i] = Character.MIN_VALUE;
                    }
                }
            } else {
                if (mCursor.getPosition() < mCursor.getCount() - 1) {
                    mCursor.moveToNext();
                    skipTrack();
                }
            }
        }
    }

    public class MediaPlayerBinder extends Binder {
        MediaPlayerService getService() {
            Log.d("Hey buddy", "MediaPlayerBinder getService()");
            return MediaPlayerService.this;
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case "com.InfinitySolutions.ACTION_PLAY_PAUSE_MUSIC":
                    playPauseMusic();
                    break;
                case "com.InfinitySolutions.DELETE_INTENT":
                    stopMediaService();
                    break;
                case "com.InfinitySolutions.NEXT_TRACK":
                    nextTrack();
                    break;
                case "com.InfinitySolutions.PREVIOUS_TRACK":
                    previousTrack();
                    break;
            }
        }
    };

    AudioManager.OnAudioFocusChangeListener afChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        pauseMusic();
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        pauseMusic();
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                        mMediaPlayer.setVolume(0.2f, 0.2f);
                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        mMediaPlayer.setVolume(1, 1);
                        playMusic();
                    }
                }
            };
}
