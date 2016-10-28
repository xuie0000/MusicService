package com.xuie.musicservice.media;

import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.xuie.musicservice.App;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuie on 16-8-31.
 */

public class MediaSource {

    private static final String TAG = "MediaSource";

    private static MediaSource instance;

    public static MediaSource getInstance() {
        if (instance == null) {
            synchronized (MediaSource.class) {
                instance = new MediaSource();
            }
        }
        return instance;
    }

    private List<Media> mMusicList = new ArrayList<>();
    private List<Media> mVideoList = new ArrayList<>();
    private List<Media> mImageList = new ArrayList<>();

    private MediaSource() {
        loadMusic();
        loadVideo();
        loadImage();
    }

    public List<Media> getMusicList() {
        return mMusicList;
    }

    public List<Media> getVideoList() {
        return mVideoList;
    }

    public List<Media> getImageList() {
        return mImageList;
    }

    private void loadMusic() {
        Cursor cursor = null;
        try {
            cursor = App.getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Audio.Media.IS_MUSIC + " = 1", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (cursor == null) {
                Log.e(TAG, "loadMedias cursor is null ");
                return;
            }

            mMusicList.clear();

            while (cursor.moveToNext()) {
                Media media = new Media();
                media.setSize(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));
                media.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                media.setDisplayName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
                media.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                media.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                media.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                media.setAlbumId(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                media.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                mMusicList.add(media);
            }

            Log.d(TAG, "loadMusic: " + mMusicList.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    private void loadVideo() {
        Cursor cursor = null;
        try {
            cursor = App.getContext().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
            if (cursor == null) {
                Log.e(TAG, "loadMedias cursor is null ");
                return;
            }

            mVideoList.clear();

            while (cursor.moveToNext()) {
                Media media = new Media();
                media.setSize(cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.SIZE)));
                media.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE)));
                media.setDisplayName(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)));
                media.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ARTIST)));
                media.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)));
                media.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID)));
//                media.setAlbumId(cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.ALBUM_ID)));
                media.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)));
                mVideoList.add(media);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    private void loadImage() {
        Cursor cursor = null;
        try {
            cursor = App.getContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, MediaStore.Images.Media.DEFAULT_SORT_ORDER);
            if (cursor == null) {
                Log.e(TAG, "loadMedias cursor is null ");
                return;
            }

            mImageList.clear();

            while (cursor.moveToNext()) {
                Media media = new Media();
                media.setSize(cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.SIZE)));
                media.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.TITLE)));
                media.setDisplayName(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)));
//                media.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.ARTIST)));
//                media.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.DURATION)));
                media.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
//                media.setAlbumId(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.ALBUM_ID)));
                media.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                mImageList.add(media);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }
}
