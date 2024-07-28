package com.fogplix.tv.helpers;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.fogplix.tv.R;
import com.fogplix.tv.callbacks.AnimeScraperCallback;
import com.fogplix.tv.callbacks.DetailsScraperCallback;
import com.fogplix.tv.callbacks.RecentScraperCallback;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Objects;

public class Scraper {

    private final Activity activity;
    private RecentScraperCallback recentScraperCallback = null;
    private DetailsScraperCallback detailsScraperCallback = null;
    private AnimeScraperCallback animeScraperCallback = null;

    public Scraper(Activity activity, RecentScraperCallback recentScraperCallback) {
        this.activity = activity;
        this.recentScraperCallback = recentScraperCallback;
    }

    public Scraper(Activity activity, DetailsScraperCallback detailsScraperCallback) {
        this.activity = activity;
        this.detailsScraperCallback = detailsScraperCallback;
    }

    public Scraper(Activity activity, AnimeScraperCallback animeScraperCallback) {
        this.activity = activity;
        this.animeScraperCallback = animeScraperCallback;
    }

    public void scrapeRecent(int page, int type) {

        new Thread(() -> {
            try {
                JSONArray allAnime = new JSONArray();

                String url = activity.getString(R.string.gogoload_recent) + "?page=" + page + "&type=" + type;

                Document document = Jsoup.connect(url)
                        .userAgent(activity.getString(R.string.user_agent))
                        .header("Accept-Language", "en-GB,en;q=0.5")
                        .get();

                Element episodes = document.select(".items").first();

                assert episodes != null;
                Elements allListTags = episodes.select("li");

                for (Element allListTag : allListTags) {

                    JSONObject object = new JSONObject();

                    String animeId;
                    String episodeId;
                    String animeTitle;
                    String episodeNum;
                    String subOrDub = "";
                    String animeImg;


                    episodeId = Objects.requireNonNull(allListTag.select("a").first()).attr("href").trim();
                    episodeId = episodeId.substring(1);

                    animeTitle = Objects.requireNonNull(allListTag.select("a").first()).attr("title").trim();

                    animeImg = Objects.requireNonNull(allListTag.select("img").first()).attr("src").trim();

                    if (document.getElementsByClass("ic-DUB").size() > 0) {
                        subOrDub = "DUB";
                    }
                    if (document.getElementsByClass("ic-SUB").size() > 0) {
                        subOrDub = "SUB";
                    }

                    episodeNum = CustomMethods.extractEpisodeNumberFromId(episodeId).trim();

                    animeId = episodeId.replace("-episode-" + episodeNum, "").trim();

                    object.put("animeId", animeId);
                    object.put("episodeId", episodeId);
                    object.put("animeTitle", animeTitle);
                    object.put("episodeNum", episodeNum);
                    object.put("subOrDub", subOrDub);
                    object.put("animeImg", animeImg);

                    allAnime.put(object);
                }

                new Handler(Looper.getMainLooper()).post(() -> recentScraperCallback.onScrapeComplete(allAnime));

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> recentScraperCallback.onScrapeFailed(e.getMessage()));
            }
        }).start();
    }


    public void scrapeAnime(String url){

        new Thread(() -> {

            try {
                JSONArray allAnime = new JSONArray();

                Document document = Jsoup.connect(url)
                        .userAgent(activity.getString(R.string.user_agent))
                        .get();

                Elements liTags = document.select("ul.items").select("li");

                for (Element liTag : liTags) {

                    String animeImg = liTag.select("img").attr("src").trim();
                    String animeTitle = liTag.select(".name").text().trim();
                    String animeRelease = liTag.select(".released").text().toLowerCase();
                    animeRelease = animeRelease.replace("released:", "").trim();
                    String animeId = Objects.requireNonNull(liTag.select("a").first()).attr("href").trim();
                    animeId = animeId.replace("/category/","");
                    String episodeId = animeId + "-episode-1";

                    JSONObject object = new JSONObject();
                    object.put("animeTitle", animeTitle);
                    object.put("animeImg", animeImg);
                    object.put("releasedDate", animeRelease);
                    object.put("animeId", animeId);
                    object.put("episodeId", episodeId);

                    allAnime.put(object);
                }

                new Handler(Looper.getMainLooper()).post(() -> animeScraperCallback.onScrapeComplete(allAnime));

            } catch (Exception e){
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> animeScraperCallback.onScrapeFailed(e.getMessage()));
            }
        }).start();
    }

    public void scrapeDetails(String animeId, String episodeID) {

        new Thread(() -> {

            String episodeId2;

            if (episodeID == null || episodeID.isEmpty()){
                episodeId2 = animeId + "-episode-1";
            } else{
                episodeId2 = episodeID;
            }

            Document document;
            Connection.Response response;
            String detailsUrl = activity.getString(R.string.gogoanime_url) + "/category/" + animeId;

            try {
                response = Jsoup.connect(detailsUrl)
                        .userAgent(activity.getString(R.string.user_agent))
                        .header("Accept-Language", "en-GB,en;q=0.5")
                        .execute();

            } catch (Exception e) {

                String episodePageUrl = activity.getString(R.string.gogoanime_url) + "/" + episodeId2;

                try {
                    Document episodePageDoc = Jsoup.connect(episodePageUrl)
                            .userAgent(activity.getString(R.string.user_agent))
                            .header("Accept-Language", "en-GB,en;q=0.5")
                            .get();

                    Element animeIdContainerDiv = episodePageDoc.getElementsByClass("tv-info").first();

                    assert animeIdContainerDiv != null;
                    Element aTag = animeIdContainerDiv.getElementsByTag("a").first();

                    assert aTag != null;
                    detailsUrl = activity.getString(R.string.gogoanime_url) + aTag.attr("href");

                    response = Jsoup.connect(detailsUrl)
                            .userAgent(activity.getString(R.string.user_agent))
                            .header("Accept-Language", "en-GB,en;q=0.5")
                            .execute();

                } catch (Exception ex) {
                    Log.d("Uchiha", "onCreate ex: " + ex.getMessage());
                    response = null;
                }
            }

            try {

                if (response == null) {
                    new Handler(Looper.getMainLooper()).post(() -> CustomMethods.errorAlert(activity, "Error (Try using VPN)", activity.getString(R.string.isp_blocked), "Ok", true));
                } else {

                    JSONObject animeInfoJObj = new JSONObject();

                    document = response.parse();

                    Element episodePage = document.getElementById("episode_page");

                    assert episodePage != null;
                    Element aTag = episodePage.getElementsByClass("active").first();

                    assert aTag != null;
                    String lastEpisode = aTag.attr("ep_end");

                    String movieID = Objects.requireNonNull(document.select(".anime_info_episodes_next #movie_id").first()).attr("value");

                    String allEpisodesUrl = activity.getString(R.string.gogoload_list_episodes) + "?id=" + movieID + "&alias=" + animeId + "&ep_start=0&default_ep=0&ep_end=" + lastEpisode;

                    Document allEpisodesHTML = Jsoup.connect(allEpisodesUrl)
                            .userAgent(activity.getString(R.string.user_agent))
                            .get();

                    Elements liTags = allEpisodesHTML.getElementsByTag("li");

                    JSONArray episodesArray = new JSONArray();

                    for (Element liTag: liTags) {

                        String episodeId = Objects.requireNonNull(liTag.select("a").first()).attr("href").replace("/", "").trim();
                        String episodeNum = CustomMethods.extractEpisodeNumberFromId(episodeId);

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("episodeId", episodeId);
                        jsonObject.put("episodeNum", episodeNum);

                        episodesArray.put(jsonObject);
                    }


                    Element details = document.select(".anime_info_body_bg").first();

                    assert details != null;
                    Document innerHtml = Jsoup.parse(details.html());

                    String animeImg = Objects.requireNonNull(innerHtml.select("img").first()).attr("src");
                    String animeTitle = Objects.requireNonNull(innerHtml.select("h1").first()).text();
                    String type = "";
                    String releasedDate = "";
                    String status = "";
                    String synopsis = innerHtml.select("div.description").text();

                    JSONArray genresArray = new JSONArray();

                    Elements allPTags = innerHtml.select(".type");

                    for (int i = 0; i < allPTags.size(); i++) {

                        Document pTagDoc = Jsoup.parse(allPTags.get(i).html());

                        String spanTagValue = Objects.requireNonNull(pTagDoc.select("span").first()).text();

                        if (spanTagValue.toLowerCase().contains("type")) {
                            type = pTagDoc.text().replace(spanTagValue, "").trim();
                        }
                        if (spanTagValue.toLowerCase().contains("genre")) {

                            Elements genres = pTagDoc.getElementsByTag("a");

                            for (int k = 0; k < genres.size(); k++) {
                                genresArray.put(genres.get(k).attr("title"));
                            }
                        }
                        if (spanTagValue.toLowerCase().contains("released")) {
                            releasedDate = pTagDoc.text().replace(spanTagValue, "").trim();
                        }
                        if (spanTagValue.toLowerCase().contains("status")) {
                            status = pTagDoc.text().replace(spanTagValue, "").trim();
                        }
                    }

                    animeInfoJObj.put("animeTitle", animeTitle);
                    animeInfoJObj.put("animeImg", animeImg);
                    animeInfoJObj.put("type", type);
                    animeInfoJObj.put("releasedDate", releasedDate);
                    animeInfoJObj.put("status", status);
                    animeInfoJObj.put("genres", genresArray);
                    animeInfoJObj.put("synopsis", synopsis);
                    animeInfoJObj.put("totalEpisodes", lastEpisode);
                    animeInfoJObj.put("episodesList", episodesArray);

                    new Handler(Looper.getMainLooper()).post(()->detailsScraperCallback.onScrapingComplete(animeInfoJObj));
                }
            } catch (Exception e){
                new Handler(Looper.getMainLooper()).post(()->detailsScraperCallback.onScrapingFailed(e.getMessage()));
            }
        }).start();
    }
}
