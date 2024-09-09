package com.fogplix.tv.adapters;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.fogplix.tv.R;
import com.fogplix.tv.activities.PlayerActivity;
import com.fogplix.tv.helpers.MyDatabaseHandler;

import org.json.JSONArray;

@UnstableApi
public class EpisodesButtonsAdapter extends RecyclerView.Adapter<EpisodesButtonsAdapter.MyCustomViewHolder> {
    Activity activity;
    JSONArray allEpisodesJArray;
    String animeId;
    String animeTitle;

    public EpisodesButtonsAdapter(Activity activity, JSONArray allEpisodesJArray, String animeId, String animeTitle) {
        this.activity = activity;
        this.allEpisodesJArray = allEpisodesJArray;
        this.animeId = animeId;
        this.animeTitle = animeTitle;
    }

    @NonNull
    @Override
    public MyCustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View view = layoutInflater.inflate(R.layout.sample_episode_button_design, parent, false);
        return new EpisodesButtonsAdapter.MyCustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyCustomViewHolder holder, int position) {

        try {

            String episodeId = allEpisodesJArray.getJSONObject(holder.getBindingAdapterPosition()).getString("episodeId");
            String episodeNum = allEpisodesJArray.getJSONObject(holder.getBindingAdapterPosition()).getString("episodeNum");

            holder.episodeButton.setText(episodeNum);

            holder.episodeButton.setOnClickListener(view -> {
                Intent intent = new Intent(activity, PlayerActivity.class);
                intent.putExtra("episodeId", episodeId);
                activity.startActivity(intent);
            });

            MyDatabaseHandler databaseHandler = new MyDatabaseHandler(activity);

            if (episodeId.equals(databaseHandler.getLastWatchedEpisodeId(animeId))) {
                holder.episodeButton.setTextColor(activity.getColor(R.color.white));
                holder.episodeButton.setBackgroundColor(activity.getColor(R.color.teal_500));
            } else {
                holder.episodeButton.setTextColor(activity.getColor(R.color.teal_500));
                holder.episodeButton.setBackgroundColor(activity.getColor(R.color.black));
            }

            holder.episodeButton.setOnFocusChangeListener((view, hasFocus) -> {

                if (hasFocus) {
                    holder.episodeButton.setTextColor(activity.getColor(R.color.white));
                    holder.episodeButton.setBackgroundColor(activity.getColor(R.color.red));
                } else {
                    if (episodeId.equals(databaseHandler.getLastWatchedEpisodeId(animeId))) {
                        holder.episodeButton.setTextColor(activity.getColor(R.color.white));
                        holder.episodeButton.setBackgroundColor(activity.getColor(R.color.teal_500));
                    } else {
                        holder.episodeButton.setTextColor(activity.getColor(R.color.teal_500));
                        holder.episodeButton.setBackgroundColor(activity.getColor(R.color.black));
                    }
                }
            });
        } catch (Exception e) {
            Log.e("MADARA", "onBindViewHolder: ", e);
        }
    }

    @Override
    public int getItemCount() {
        return allEpisodesJArray.length();
    }

    public static class MyCustomViewHolder extends RecyclerView.ViewHolder {

        Button episodeButton;

        public MyCustomViewHolder(View itemView) {
            super(itemView);
            episodeButton = itemView.findViewById(R.id.episodeButton);
        }
    }
}
