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

        dialog.show();
    }

//    ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

}
