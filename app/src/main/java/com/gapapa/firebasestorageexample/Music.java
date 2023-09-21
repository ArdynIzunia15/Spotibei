package com.gapapa.firebasestorageexample;

public class Music {
    private String title;
    private String artist;
    private int index;
    private boolean isPlaying;

    public Music(String title, String artist, int index, boolean isPlaying) {
        this.title = title;
        this.artist = artist;
        this.isPlaying = isPlaying;
        this.index = index;
    }

    public int getIndex(){
        return index;
    }
    public void setIndex(int index){
        this.index = index;
    }

    public String getTitle() {
        return title.replaceFirst(" ", "");
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public String getWholeName(){
        String str = artist + "-" + title;
        str = str.replace(".mp3", "");
        return str;
    }
}
