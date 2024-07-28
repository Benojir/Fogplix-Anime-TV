package com.fogplix.tv.callbacks;

import org.json.JSONArray;

public interface RecentScraperCallback {
    void onScrapeComplete(JSONArray result);
    void onScrapeFailed(String error);
}
