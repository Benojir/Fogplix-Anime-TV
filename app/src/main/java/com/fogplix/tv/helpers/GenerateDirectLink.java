package com.fogplix.tv.helpers;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.fogplix.tv.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;

public class GenerateDirectLink {

    private static final String TAG = "MADARA";
    private final Activity activity;
    private final boolean isProxyEnabled;
    private final String proxyBrowserLink;

    public GenerateDirectLink(Activity activity){
        this.activity = activity;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        isProxyEnabled = sharedPreferences.getBoolean("use_proxy", false);
        proxyBrowserLink = activity.getString(R.string.proxy_browser_link);
    }

    public void generate(String episodeId, OnGenerateDirectLink onGenerateDirectLink){

        new Thread(() -> {

            String episodeLink = activity.getString(R.string.gogoanime_url) + "/" + episodeId;

            if (isProxyEnabled) {
                try {
                    episodeLink = proxyBrowserLink + URLEncoder.encode(episodeLink, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "generate: ", e);
                    episodeLink = proxyBrowserLink + episodeLink;
                }
            }

            try {

                Document document = Jsoup.connect(episodeLink)
                        .userAgent(activity.getString(R.string.user_agent))
                        .get();

                Element animeIdContainer = document.select(".anime-info").first();
                Element previousEpisodeLinkContainer = document.select(".anime_video_body_episodes_l").first();
                Element nextEpisodeLinkContainer = document.select(".anime_video_body_episodes_r").first();

                String animeId = "Unknown";
                String animeTitle = "";
                String previousEpisodeId = "";
                String nextEpisodeId = "";

                if (animeIdContainer != null) {
                    Element animeIdA = animeIdContainer.selectFirst("a");
                    if (animeIdA != null) {
                        animeId = animeIdA.attr("href");
                        animeId = animeId.replace("/category/", "").trim();
                        animeTitle = animeIdA.text().trim();
                    }
                }

                if (previousEpisodeLinkContainer != null) {
                    Element previousEpisodeLinkA = previousEpisodeLinkContainer.selectFirst("a");
                    if (previousEpisodeLinkA != null) {
                        previousEpisodeId = previousEpisodeLinkA.attr("href");
                        previousEpisodeId = previousEpisodeId.replace("/", "").trim();
                    }
                }

                if (nextEpisodeLinkContainer != null) {
                    Element nextEpisodeLinkA = nextEpisodeLinkContainer.selectFirst("a");
                    if (nextEpisodeLinkA != null) {
                        nextEpisodeId = nextEpisodeLinkA.attr("href");
                        nextEpisodeId = nextEpisodeId.replace("/", "").trim();
                    }
                }

                String embedLink = Objects.requireNonNull(document.select("iframe").first()).attr("src").trim();

                URL embedLinkURL = new URL(embedLink);

                String embedLinkProtocol = embedLinkURL.getProtocol();
                String embedLinkHost = embedLinkURL.getHost();
                String query = embedLinkURL.getQuery();

                String idValue = CustomMethods.getIdFromQuery(query);

                Document html = Jsoup.connect(embedLink)
                        .userAgent(activity.getString(R.string.user_agent))
                        .get();

                String params = CustomMethods.generateEncryptAjaxParameters(html, idValue);

                String requestURL = embedLinkProtocol + "://" + embedLinkHost + "/encrypt-ajax.php" + "?" + params;

                URL url = new URL(requestURL);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", activity.getString(R.string.user_agent));
                connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                in.close();
                connection.disconnect();

                JSONObject responseData = new JSONObject(response.toString());

                JSONObject m3u8Data = CustomMethods.decryptEncryptAjaxResponse(responseData.getString("data"));

                JSONArray source1 = m3u8Data.getJSONArray("source");
                JSONArray source2 = m3u8Data.getJSONArray("source_bk");

                String videoHLSUrl = source1.getJSONObject(0).getString("file");
                String videoHLSUrl2 = source2.getJSONObject(0).getString("file");

                JSONObject episodeFinalInfo = new JSONObject();
                episodeFinalInfo.put("animeId", animeId);
                episodeFinalInfo.put("animeTitle", animeTitle);
                episodeFinalInfo.put("episodeId", episodeId);
                episodeFinalInfo.put("referer", embedLink);
                episodeFinalInfo.put("videoHLSUrl", videoHLSUrl);
                episodeFinalInfo.put("videoHLSUrl2", videoHLSUrl2);
                episodeFinalInfo.put("previousEpisodeId", previousEpisodeId);
                episodeFinalInfo.put("nextEpisodeId", nextEpisodeId);

                new Handler(Looper.getMainLooper()).post(() -> onGenerateDirectLink.onComplete(episodeFinalInfo));

            } catch (Exception e) {
                Log.e(TAG, "generate: ", e);
                new Handler(Looper.getMainLooper()).post(() -> onGenerateDirectLink.onFailed(e.getMessage()));
            }
        }).start();
    }

    public interface OnGenerateDirectLink{
        void onComplete(JSONObject object);
        void onFailed(String error);
    }
}
