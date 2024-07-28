package com.fogplix.tv.callbacks;

import org.json.JSONArray;

public interface AnimeScraperCallback {
    void onScrapeComplete(JSONArray results);
    void onScrapeFailed(String error);
}
