package com.fogplix.tv.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.fogplix.tv.R;
import com.fogplix.tv.helpers.MyDatabaseHandler;
import com.fogplix.tv.helpers.ViewDialog;

import org.json.JSONArray;

@UnstableApi
public class EpisodesButtonsAdapter extends RecyclerView.Adapter<EpisodesButtonsAdapter.MyCustomViewHolder> {
    Context context;
    JSONArray allEpisodesJArray;
    String animeId;
    String animeTitle;

    public EpisodesButtonsAdapter(Context context, JSONArray allEpisodesJArray, String animeId, String animeTitle){
        this.context = context;
        this.allEpisodesJArray = allEpisodesJArray;
        this.animeId = animeId;
        this.animeTitle = animeTitle;
    }

    @NonNull
    @Override
    public MyCustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
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
                ViewDialog viewDialog = new ViewDialog((Activity) context);
                viewDialog.choosePlayServer(episodeId, animeTitle, animeId);
            });

            MyDatabaseHandler databaseHandler = new MyDatabaseHandler(context);

            if (episodeId.equals(databaseHandler.getLastWatchedEpisodeId(animeId))){
                holder.episodeButton.setTextColor(context.getColor(R.color.white));
                holder.episodeButton.setBackgroundColor(context.getColor(R.color.teal_500));
            } else{
                holder.episodeButton.setTextColor(context.getColor(R.color.teal_500));
                holder.episodeButton.setBackgroundColor(context.getColor(R.color.black));
            }

            holder.episodeButton.setOnFocusChangeListener((view, hasFocus) -> {

                if (hasFocus){
                    holder.episodeButton.setTextColor(context.getColor(R.color.white));
                    holder.episodeButton.setBackgroundColor(context.getColor(R.color.red));
                } else {
                    if (episodeId.equals(databaseHandler.getLastWatchedEpisodeId(animeId))){
                        holder.episodeButton.setTextColor(context.getColor(R.color.white));
                        holder.episodeButton.setBackgroundColor(context.getColor(R.color.teal_500));
                    } else{
                        holder.episodeButton.setTextColor(context.getColor(R.color.teal_500));
                        holder.episodeButton.setBackgroundColor(context.getColor(R.color.black));
                    }
                }
            });
        }
        catch (Exception e) {
            Log.e("MADARA", "onBindViewHolder: ", e);
        }
    }

    @Override
    public int getItemCount() {
        return allEpisodesJArray.length();
    }

    public static class MyCustomViewHolder extends RecyclerView.ViewHolder{

        Button episodeButton;

        public MyCustomViewHolder(View itemView) {
            super(itemView);
            episodeButton = itemView.findViewById(R.id.episodeButton);
        }
    }
}
