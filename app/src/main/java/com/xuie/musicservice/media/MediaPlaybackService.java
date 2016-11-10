package com.xuie.musicservice.media;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by xuie on 16-10-26.
 */
public class MediaPlaybackService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnSeekCompleteListener {
    private static final String TAG = "MediaPlaybackService";

    public static final String PLAYSTATE_CHANGED = "com.xuie.music.playstatechanged";
    public static final String META_CHANGED = "com.xuie.music.metachanged";

    private List<Media> mMusics;
    private MediaPlayer mMediaPlayer;

    private int mPlayPos = -1;


    @Override public void onCreate() {
        super.onCreate();
        mMusics = MediaSource.getInstance().getMusicList();

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable @Override public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    // ------------ MediaPlayer Listener ---------------------------------
    @Override public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onCompletion: ");
        next();
    }

    @Override public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.d(TAG, "onError: ");
        next();
        return false;
    }

    @Override public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onPrepared: ");
        notifyChange(PLAYSTATE_CHANGED);
    }

    @Override public void onSeekComplete(MediaPlayer mediaPlayer) {
    }

    // --------------------------------------------------------------------


    @Override public void onDestroy() {
        super.onDestroy();
        if (!checkPlayerNull()) {
            mMediaPlayer.release();
        }
    }

    public void play() {
        if (mMusics.size() <= 0) {
            Log.d(TAG, "play: size == 0");
            return;
        }

        if (mPlayPos == -1) {
            mPlayPos = 0;
        }
        Media media = mMusics.get(mPlayPos);

        if (media == null) {
            Log.d(TAG, "play: media is null");
            return;
        }
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(media.getPath());
            mMediaPlayer.prepare();
            start();
            notifyChange(META_CHANGED);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        mMediaPlayer.start();
        notifyChange(PLAYSTATE_CHANGED);
    }


    public void prev() {
        synchronized (this) {
            mPlayPos--;
            if (mPlayPos < 0) {
                mPlayPos = mMusics.size() - 1;
            }

            play();
        }
    }

    public void next() {
        synchronized (this) {
            mPlayPos++;
            if (mPlayPos > mMusics.size() - 1) {
                mPlayPos = 0;
            }

            play();
        }
    }

    public void pause() {
        synchronized (this) {
            if (isPlaying() && !checkPlayerNull()) {
                mMediaPlayer.pause();
            }
            notifyChange(PLAYSTATE_CHANGED);
        }
    }

    public void stop() {
        synchronized (this) {
            if (!checkPlayerNull()) {
                mMediaPlayer.stop();
            }
            notifyChange(PLAYSTATE_CHANGED);
        }
    }

    public int getQueuePosition() {
        return mPlayPos;
    }

    public boolean isPlaying() {
        return !checkPlayerNull() && mMediaPlayer.isPlaying();
    }

    public boolean checkPlayerNull() {
        return mMediaPlayer == null;
    }

    public long duration() {
        synchronized (this) {
            if (checkPlayerNull())
                return -1;
            return mMediaPlayer.getDuration();
        }
    }

    public long position() {
        synchronized (this) {
            if (checkPlayerNull())
                return -1;
            if (getQueuePosition() == -1)
                return -1;
            return mMediaPlayer.getCurrentPosition();
        }
    }

    public void seek(long pos) {
        synchronized (this) {
            if (!checkPlayerNull()) {
                if (pos < 0) pos = 0;
                if (pos > duration()) pos = duration();
                mMediaPlayer.seekTo((int) pos);
            }
        }
    }

    public Media getCurrentMedia() {
        synchronized (this) {
            if (checkPlayerNull() || mMusics.size() <= 0) {
                return null;
            }

            if (mPlayPos < 0 || mPlayPos > mMusics.size() - 1) {
                return null;
            }

            return mMusics.get(mPlayPos);
        }
    }

    public int getAudioSessionId() {
        return mMediaPlayer != null ? mMediaPlayer.getAudioSessionId() : 0;
    }


    static class ServiceStub extends IMediaPlaybackService.Stub {
        WeakReference<MediaPlaybackService> mService;

        public ServiceStub(MediaPlaybackService service) {
            mService = new WeakReference<>(service);
        }

        @Override public int getQueuePosition() throws RemoteException {
            return mService.get().getQueuePosition();
        }

        @Override public boolean isPlaying() throws RemoteException {
            return mService.get().isPlaying();
        }

        @Override public void stop() throws RemoteException {
            mService.get().stop();
        }

        @Override public void pause() throws RemoteException {
            mService.get().pause();
        }

        @Override public void start() throws RemoteException {
            mService.get().start();
        }

        @Override public void play() throws RemoteException {
            mService.get().play();
        }

        @Override public void prev() throws RemoteException {
            mService.get().prev();
        }

        @Override public void next() throws RemoteException {
            mService.get().next();
        }

        @Override public long duration() throws RemoteException {
            return mService.get().duration();
        }

        @Override public long position() throws RemoteException {
            return mService.get().position();
        }

        @Override public void seek(long pos) throws RemoteException {
            mService.get().seek(pos);
        }

        @Override public String getTrackName() throws RemoteException {
            return null;
        }

        @Override public String getAlbumName() throws RemoteException {
            return null;
        }

        @Override public long getAlbumId() throws RemoteException {
            return 0;
        }

        @Override public String getArtistName() throws RemoteException {
            return null;
        }

        @Override public long getArtistId() throws RemoteException {
            return 0;
        }

        @Override public void enqueue(List<Media> medias, int action) throws RemoteException {

        }

        @Override public void moveQueueItem(int from, int to) throws RemoteException {

        }

        @Override public void setQueuePosition(int index) throws RemoteException {

        }

        @Override public Media getCurrentMedia() throws RemoteException {
            return mService.get().getCurrentMedia();
        }

        @Override public List<Media> getMedias() throws RemoteException {
            return null;
        }

        @Override public int removeTracks(int first, int last) throws RemoteException {
            return 0;
        }

        @Override public int removeTrack(long id) throws RemoteException {
            return 0;
        }

        @Override public void setRepeatMode(int repeatmode) throws RemoteException {

        }

        @Override public int getRepeatMode() throws RemoteException {
            return 0;
        }

        @Override public int getAudioSessionId() throws RemoteException {
            return mService.get().getAudioSessionId();
        }
    }

    private final IBinder mBinder = new ServiceStub(this);

    void notifyChange(String what) {
        sendBroadcast(new Intent(what));
    }
}
