package com.fogplix.tv.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.media3.common.util.UnstableApi;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.fogplix.tv.R;
import com.fogplix.tv.activities.PlayerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

@UnstableApi
public class ViewDialog {

    private final Activity activity;

    public ViewDialog(Activity activity) {
        this.activity = activity;
    }

    @SuppressLint("SetTextI18n")
    public void choosePlayServer(String episodeId, String animeTitle, String animeId) {

        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_play_server_selector);

        // Set dialog width to match screen width
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(Objects.requireNonNull(dialog.getWindow()).getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(layoutParams);

        ((TextView) dialog.findViewById(R.id.episodeNumTV)).setText("Episode " + CustomMethods.extractEpisodeNumberFromId(episodeId));

        Button server_1_btn = dialog.findViewById(R.id.server_1_btn);
        Button server_2_btn = dialog.findViewById(R.id.server_2_btn);
        Button server_3_btn = dialog.findViewById(R.id.server_3_btn);

//        ------------------------------------------------------------------------------------------

        server_1_btn.setOnFocusChangeListener((view, b) -> {

            if (b) {
                server_1_btn.setBackgroundColor(activity.getColor(R.color.green));
            } else {
                server_1_btn.setBackgroundColor(activity.getColor(R.color.red));
            }
        });

        server_2_btn.setOnFocusChangeListener((view, b) -> {

            if (b) {
                server_2_btn.setBackgroundColor(activity.getColor(R.color.green));
            } else {
                server_2_btn.setBackgroundColor(activity.getColor(R.color.red));
            }
        });

        server_3_btn.setOnFocusChangeListener((view, b) -> {

            if (b) {
                server_3_btn.setBackgroundColor(activity.getColor(R.color.green));
            } else {
                server_3_btn.setBackgroundColor(activity.getColor(R.color.red));
            }
        });
//        ------------------------------------------------------------------------------------------

        server_1_btn.setOnClickListener(view -> {

            HPSharedPreference hpSharedPreference = new HPSharedPreference(activity);

            boolean server_status = hpSharedPreference.getPlayableServersStatus("server_1");

            if (server_status) {

                ProgressDialog pd = new ProgressDialog(activity);
                pd.setMessage("Generating playable link...");
                pd.setCancelable(false);
                pd.show();

                GenerateDirectLink generateDirectLink = new GenerateDirectLink(activity);

                generateDirectLink.generate(episodeId, new GenerateDirectLink.OnGenerateDirectLink() {
                    @Override
                    public void onComplete(JSONObject object) {

                        pd.dismiss();

                        try {
                            String refererUrl = object.getString("referer");
                            String videoHLSUrl = object.getString("videoHLSUrl");
                            String videoHLSUrl2 = object.getString("videoHLSUrl2");


                            Intent intent = new Intent(activity, PlayerActivity.class);
                            intent.putExtra("episodeId", episodeId);
                            intent.putExtra("animeId", animeId);
                            intent.putExtra("animeTitle", animeTitle);
                            intent.putExtra("refererUrl", refererUrl);
                            intent.putExtra("videoHLSUrl", videoHLSUrl);
                            intent.putExtra("videoHLSUrl2", videoHLSUrl2);
                            activity.startActivity(intent);

                            dialog.dismiss();

                        } catch (JSONException e) {
                            Toast.makeText(activity, "Something went wrong.", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailed(String error) {
                        pd.dismiss();
                        dialog.dismiss();
                        Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(activity, "This server is not working currently. Try another server.", Toast.LENGTH_SHORT).show();
            }
        });

//        ------------------------------------------------------------------------------------------

        server_2_btn.setOnClickListener(view -> {

            HPSharedPreference hpSharedPreference = new HPSharedPreference(activity);

            boolean server_status = hpSharedPreference.getPlayableServersStatus("server_2");

            if (server_status) {

                String consumet_api = activity.getString(R.string.consumet_streaming_link_api) + episodeId;

                ProgressDialog pd = new ProgressDialog(activity);
                pd.setMessage("Generating playable link...");
                pd.setCancelable(false);
                pd.show();


                StringRequest objectRequest1 = new StringRequest(Request.Method.GET, consumet_api, response -> {

                    try {

                        JSONObject object = new JSONObject(response);

                        if (object.has("message")) {
                            pd.dismiss();
                            CustomMethods.errorAlert(activity, "Error", object.getString("error"), "Ok", true);
                        } else {

                            String refererUrl = object.getJSONObject("headers").getString("Referer");
                            String videoHLSUrl = "";
                            String videoHLSUrl2 = "";

                            JSONArray sources = object.getJSONArray("sources");

                            for (int i = 0; i < sources.length(); i++) {

                                JSONObject source_obj = sources.getJSONObject(i);

                                if (source_obj.getString("quality").equalsIgnoreCase("default")) {
                                    videoHLSUrl = source_obj.getString("url");
                                }

                                if (source_obj.getString("quality").equalsIgnoreCase("backup")) {
                                    videoHLSUrl2 = source_obj.getString("url");
                                }
                            }

                            pd.dismiss();

                            Intent intent = new Intent(activity, PlayerActivity.class);
                            intent.putExtra("episodeId", episodeId);
                            intent.putExtra("animeId", animeId);
                            intent.putExtra("animeTitle", animeTitle);
                            intent.putExtra("refererUrl", refererUrl);
                            intent.putExtra("videoHLSUrl", videoHLSUrl);
                            intent.putExtra("videoHLSUrl2", videoHLSUrl2);
                            activity.startActivity(intent);

                            dialog.dismiss();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(activity, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        CustomMethods.errorAlert(activity, "Error (Try another server)", e.getMessage() + "\n", "Ok", false);
                    }
                }, error -> {
                    pd.dismiss();
                    CustomMethods.errorAlert(activity, "Error (Try another server.)", error.getMessage(), "Ok", false);
                });

                Cache cache = new DiskBasedCache(activity.getCacheDir(), 1024 * 1024); // 1MB cap
                Network network = new BasicNetwork(new HurlStack());


                RequestQueue queue = new RequestQueue(cache, network);
                objectRequest1.setRetryPolicy((new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)));
                queue.add(objectRequest1);
                queue.start();

                dialog.dismiss();
            } else {
                Toast.makeText(activity, "This server is not working currently. Try another server.", Toast.LENGTH_SHORT).show();
            }
        });

//        ------------------------------------------------------------------------------------------

        server_3_btn.setOnClickListener(v -> {

            HPSharedPreference hpSharedPreference = new HPSharedPreference(activity);

            boolean server_status = hpSharedPreference.getPlayableServersStatus("server_3");

            if (server_status) {

                String vidcdn_api = activity.getString(R.string.VIDCDN_API_URL) + episodeId;

                ProgressDialog pd = new ProgressDialog(activity);
                pd.setMessage("Generating playable link...");
                pd.setCancelable(false);
                pd.show();

                StringRequest objectRequest1 = new StringRequest(Request.Method.GET, vidcdn_api, response -> {

                    try {

                        JSONObject object = new JSONObject(response);

                        if (object.has("error")) {
                            pd.dismiss();
                            CustomMethods.errorAlert(activity, "Error", object.getString("error"), "Ok", true);
                        } else {

                            String refererUrl = object.getString("Referer");
                            String videoHLSUrl = object.getJSONArray("sources").getJSONObject(0).getString("file");
                            String videoHLSUrl2 = object.getJSONArray("sources_bk").getJSONObject(0).getString("file");

                            pd.dismiss();

                            Intent intent = new Intent(activity, PlayerActivity.class);
                            intent.putExtra("episodeId", episodeId);
                            intent.putExtra("animeId", animeId);
                            intent.putExtra("animeTitle", animeTitle);
                            intent.putExtra("refererUrl", refererUrl);
                            intent.putExtra("videoHLSUrl", videoHLSUrl);
                            intent.putExtra("videoHLSUrl2", videoHLSUrl2);
                            activity.startActivity(intent);

                            dialog.dismiss();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(activity, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        CustomMethods.errorAlert(activity, "Error (Try another server)", e.getMessage() + "\n", "Ok", true);
                    }
                }, error -> {
                    pd.dismiss();
                    CustomMethods.errorAlert(activity, "Error (Try another server.)", error.getMessage(), "Ok", true);
                });

                Cache cache = new DiskBasedCache(activity.getCacheDir(), 1024 * 1024); // 1MB cap
                Network network = new BasicNetwork(new HurlStack());


                RequestQueue queue = new RequestQueue(cache, network);
                objectRequest1.setRetryPolicy((new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)));
                queue.add(objectRequest1);
                queue.start();

                dialog.dismiss();
            } else {
                Toast.makeText(activity, "This server is not working currently. Try another server.", Toast.LENGTH_SHORT).show();
            }
        });

//        ------------------------------------------------------------------------------------------

        dialog.show();
    }

//    ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

}
