package com.xuie.musicservice;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.xuie.musicservice.media.IMediaPlaybackService;
import com.xuie.musicservice.media.Media;
import com.xuie.musicservice.media.MediaPlaybackService;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class MediaControlFragment extends Fragment {
    private static final String TAG = "MediaControlFragment";

    @BindView(R.id.seek_bar) SeekBar seekBar;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.previous) ImageView previous;
    @BindView(R.id.play_pause) ImageView playPause;
    @BindView(R.id.next) ImageView next;

    private IMediaPlaybackService mService;
    private boolean isBind;

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

    private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
//            Log.d(TAG, action);
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

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_media_control, container, false);
        ButterKnife.bind(this, v);

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

        Intent intent = new Intent(getActivity(), MediaPlaybackService.class);
        isBind = getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        IntentFilter f = new IntentFilter();
        f.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
        f.addAction(MediaPlaybackService.META_CHANGED);
        getActivity().registerReceiver(mStatusListener, new IntentFilter(f));

        long next = refreshNow();
        queueNextRefresh(next);

        return v;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeMessages(REFRESH);
        if (isBind && getActivity() != null) {
            getActivity().unbindService(serviceConnection);
        }

        if (getActivity() != null) {
            getActivity().unregisterReceiver(mStatusListener);
        }
    }

    private long refreshNow() {
        if (mService == null)
            return 1000;
        try {
            long pos = mService.position();
            seekBar.setProgress((int) pos);
            return 300;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 1000;
    }

    private void setPauseButtonImage() {
        try {
            if (mService != null && mService.isPlaying()) {
                playPause.setImageResource(android.R.drawable.ic_media_pause);
            } else {
                playPause.setImageResource(android.R.drawable.ic_media_play);
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

            if (mService.getQueuePosition() != -1) {
                seekBar.setMax((int) mService.duration());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
