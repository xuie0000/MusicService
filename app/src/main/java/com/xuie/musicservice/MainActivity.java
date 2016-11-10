package com.xuie.musicservice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.xuie.musicservice.media.IMediaPlaybackService;
import com.xuie.musicservice.media.Media;
import com.xuie.musicservice.media.MediaPlaybackService;
import com.xuie.musicservice.widget.VisualizerView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    @BindView(R.id.visualizer_view) VisualizerView visualizerView;

    private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, action);
            if (action.equals(MediaPlaybackService.META_CHANGED)) {
                updateTrackInfo();
                setPauseButtonImage();
                queueNextRefresh(1);
            } else if (action.equals(MediaPlaybackService.PLAYSTATE_CHANGED)) {
                setPauseButtonImage();
            }
        }
    };

    private void queueNextRefresh(long delay) {
        Message msg = mHandler.obtainMessage(REFRESH);
        mHandler.removeMessages(REFRESH);
        mHandler.sendMessageDelayed(msg, delay);
    }

    private static final int REFRESH = 0;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH:
                    long next = refreshNow();
                    queueNextRefresh(next);
                    break;
            }
        }
    };

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.previous) ImageView previous;
    @BindView(R.id.play_pause) ImageView playPause;
    @BindView(R.id.next) ImageView next;
    @BindView(R.id.seek_bar) SeekBar seekBar;
    @BindView(R.id.fab) FloatingActionButton fab;

    private IMediaPlaybackService mService;
    private boolean isBind;
    private Visualizer mVisualizer;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = IMediaPlaybackService.Stub.asInterface(iBinder);
            updateTrackInfo();
            long next = refreshNow();
            queueNextRefresh(next);
        }

        @Override public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        fab.setVisibility(View.GONE);
        startService(new Intent(this, MediaPlaybackService.class));

        previous.setOnClickListener(view -> {
            if (mService == null) {
                return;
            }
            try {
                mService.prev();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });


        next.setOnClickListener(view -> {
            if (mService == null) {
                return;
            }
            try {
                mService.next();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        playPause.setOnClickListener(view -> {
            if (mService == null) {
                return;
            }
            try {
                if (mService.isPlaying()) {
                    mService.pause();
                } else if (mService.getQueuePosition() != -1) {
                    mService.start();
                } else {
                    mService.play();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                if (mService == null)
                    return;

                try {
                    mService.seek(seekBar.getProgress());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MediaPlaybackService.class);
        isBind = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        IntentFilter f = new IntentFilter();
        f.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
        f.addAction(MediaPlaybackService.META_CHANGED);
        registerReceiver(mStatusListener, new IntentFilter(f));

        long next = refreshNow();
        queueNextRefresh(next);
    }

    @Override protected void onStop() {
        super.onStop();
        mHandler.removeMessages(REFRESH);
        unregisterReceiver(mStatusListener);
        if (isBind) {
            unbindService(serviceConnection);
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, MediaPlaybackService.class));
    }

    private long refreshNow() {
        if (mService == null)
            return 1000;
        try {
            long pos = mService.position();
//            Log.d(TAG, "pos:" + pos);
            seekBar.setProgress((int) pos);
            return 300;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 1000;
    }

    private void setPauseButtonImage() {
        if (mService == null)
            return;

        try {
            playPause.setImageResource(mService.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);

            if (mVisualizer != null) {
                mVisualizer.setEnabled(mService.isPlaying());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void updateTrackInfo() {
        if (mService == null) {
            return;
        }
        try {
            Media media = mService.getCurrentMedia();
            if (media != null) {
                title.setText(media.getTitle());
            }

            Log.d(TAG, "audioSessionId : " + mService.getAudioSessionId());
            if (mVisualizer != null) {
                mVisualizer.setEnabled(false);
            }

            mVisualizer = new Visualizer(mService.getAudioSessionId());
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            visualizerView.setVisualizer(mVisualizer);
            mVisualizer.setEnabled(mService.isPlaying());

            if (mService.getQueuePosition() != -1) {
                seekBar.setMax((int) mService.duration());
            }
        } catch (RemoteException e) {
            finish();
        }
    }

}
