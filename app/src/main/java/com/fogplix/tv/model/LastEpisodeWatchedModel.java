package com.fogplix.tv.model;

public class LastEpisodeWatchedModel {

    private String animeId = "";
    private String episodeId = "";

    public LastEpisodeWatchedModel() {
    }

    public LastEpisodeWatchedModel(String animeId, String episodeId) {
        this.animeId = animeId;
        this.episodeId = episodeId;
    }

    public void setAnimeId(String animeId) {
        this.animeId = animeId;
    }

    public void setEpisodeId(String episodeId) {
        this.episodeId = episodeId;
    }

    public String getAnimeId() {
        return animeId;
    }

    public String getEpisodeId() {
        return episodeId;
    }
}
