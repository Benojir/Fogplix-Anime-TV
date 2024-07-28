package com.fogplix.tv.model;

public class AnimeFavoriteListModel {

    private String animeId = "";
    private String animeName = "";
    private String animeImageUrl = "";
    private String animeServer = "";

    public AnimeFavoriteListModel(){

    }

    public AnimeFavoriteListModel(String animeId, String animeName, String animeImageUrl, String animeServer){
        this.animeId = animeId;
        this.animeName = animeName;
        this.animeImageUrl = animeImageUrl;
        this.animeServer = animeServer;
    }

    public String getAnimeId() {
        return animeId;
    }

    public void setAnimeId(String animeId) {
        this.animeId = animeId;
    }

    public String getAnimeName() {
        return animeName;
    }

    public String getAnimeServer() {
        return animeServer;
    }

    public void setAnimeName(String animeName) {
        this.animeName = animeName;
    }

    public String getAnimeImageUrl() {
        return animeImageUrl;
    }

    public void setAnimeImageUrl(String animeImageUrl) {
        this.animeImageUrl = animeImageUrl;
    }

    public void setAnimeServer(String animeServer) {
        this.animeServer = animeServer;
    }
}
