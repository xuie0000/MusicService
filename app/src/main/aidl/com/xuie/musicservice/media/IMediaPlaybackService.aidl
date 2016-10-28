// IMediaPlaybackService.aidl
package com.xuie.musicservice.media;

import com.xuie.musicservice.media.Media;

interface IMediaPlaybackService {
    int getQueuePosition();
    boolean isPlaying();
    void stop();
    void pause();
    void start();
    void play();
    void prev();
    void next();
    long duration();
    long position();
    void seek(long pos);
    String getTrackName();
    String getAlbumName();
    long getAlbumId();
    String getArtistName();
    long getArtistId();
    void enqueue(in List<Media> medias, int action);
    void moveQueueItem(int from, int to);
    void setQueuePosition(int index);
    Media getCurrentMedia();
    List<Media> getMedias();
    int removeTracks(int first, int last);
    int removeTrack(long id);
    void setRepeatMode(int repeatmode);
    int getRepeatMode();
    int getAudioSessionId();
}
