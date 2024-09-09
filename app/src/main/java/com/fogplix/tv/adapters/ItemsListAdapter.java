package com.fogplix.tv.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fogplix.tv.R;
import com.fogplix.tv.activities.DetailsActivity;
import com.fogplix.tv.helpers.CustomMethods;
import com.fogplix.tv.helpers.MyDatabaseHandler;
import com.fogplix.tv.model.AnimeFavoriteListModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ItemsListAdapter extends RecyclerView.Adapter<ItemsListAdapter.MyCustomViewHolder> {

    private static final String TAG = "MADARA";
    private final JSONArray allAnime;
    private final Activity activity;

    public ItemsListAdapter(Activity activity, JSONArray allAnime) {
        this.allAnime = allAnime;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyCustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(activity);

        View view = layoutInflater.inflate(R.layout.sample_item_card_design, parent, false);

        return new MyCustomViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyCustomViewHolder holder, int position) {

        try {
            String animeTitle = allAnime.getJSONObject(holder.getBindingAdapterPosition()).getString("animeTitle");

            holder.animeNameTV.setText(animeTitle);

            if (allAnime.getJSONObject(holder.getBindingAdapterPosition()).has("subOrDub")) {

                if (allAnime.getJSONObject(holder.getBindingAdapterPosition()).getString("subOrDub").equalsIgnoreCase("DUB")) {
                    holder.isAnimeSubDubTV.setText("dub");
                    holder.isAnimeSubDubTV.setBackgroundColor(activity.getColor(R.color.green));
                } else {
                    holder.isAnimeSubDubTV.setText("sub");
                    holder.isAnimeSubDubTV.setBackgroundColor(activity.getColor(R.color.fade_blue));
                }
            } else {
                if (animeTitle.toLowerCase().contains("(dub)")) {
                    holder.isAnimeSubDubTV.setText("dub");
                    holder.isAnimeSubDubTV.setBackgroundColor(activity.getColor(R.color.green));
                } else {
                    holder.isAnimeSubDubTV.setText("sub");
                    holder.isAnimeSubDubTV.setBackgroundColor(activity.getColor(R.color.fade_blue));
                }
            }

            Glide.with(activity)
                    .load(allAnime.getJSONObject(holder.getBindingAdapterPosition()).getString("animeImg"))
                    .placeholder(R.drawable.preload_thumb)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(holder.imageView);

            if (allAnime.getJSONObject(holder.getBindingAdapterPosition()).has("releasedDate")) {

                holder.releaseDateTV.setVisibility(View.VISIBLE);
                holder.releaseDateTV.setText(allAnime.getJSONObject(holder.getBindingAdapterPosition()).getString("releasedDate"));
            }
        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: ", e);
            Toast.makeText(activity, "JSON Error", Toast.LENGTH_SHORT).show();
            CustomMethods.errorAlert(activity, "Error", e.getMessage(), "Ok", true);
        }

        float scale = activity.getResources().getDisplayMetrics().density;
        int marginDp = 3;
        int focusedMargin = (int) (marginDp * scale + 0.5f);
        int noFocusedMargin = (int) (0 * scale + 0.5f);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );


        holder.itemView.setOnFocusChangeListener((view, hasFocus) -> {

            if (hasFocus) {
                layoutParams.setMargins(focusedMargin, focusedMargin, focusedMargin, focusedMargin);
                holder.relativeLayout.setLayoutParams(layoutParams);
            } else {
                layoutParams.setMargins(noFocusedMargin, noFocusedMargin, noFocusedMargin, noFocusedMargin);
                holder.relativeLayout.setLayoutParams(layoutParams);
            }
        });

        String finalServer = "gogo";

        holder.itemView.setOnClickListener(view -> {
            try {
                Intent intent = new Intent(activity, DetailsActivity.class);
                intent.putExtra("animeId", allAnime.getJSONObject(holder.getBindingAdapterPosition()).getString("animeId"));
                intent.putExtra("episodeId", allAnime.getJSONObject(holder.getBindingAdapterPosition()).getString("episodeId"));
                activity.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "onBindViewHolder: ", e);
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        holder.itemView.setOnLongClickListener(view -> {

            try {

                String animeId = allAnime.getJSONObject(holder.getBindingAdapterPosition()).getString("animeId");
                String animeTitle = allAnime.getJSONObject(holder.getBindingAdapterPosition()).getString("animeTitle");
                String animeImageUrl = allAnime.getJSONObject(holder.getBindingAdapterPosition()).getString("animeImg");

                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity, R.style.BottomSheetDialog);
                bottomSheetDialog.setCancelable(true);
                bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheetDialog.setContentView(R.layout.sample_add_favorite_bottomsheet_layout);

                TextView animeTvInBottomSheet = bottomSheetDialog.findViewById(R.id.anime_title_tv);
                MaterialCardView addToFavActionCV = bottomSheetDialog.findViewById(R.id.add_to_favorite_action_cv);
                MaterialCardView watchNowActionCV = bottomSheetDialog.findViewById(R.id.watch_now_action_cv);

                if (animeTvInBottomSheet != null && addToFavActionCV != null && watchNowActionCV != null) {

                    animeTvInBottomSheet.setText(animeTitle);

                    //------------------------------------------------------------------------------

                    addToFavActionCV.setOnFocusChangeListener((view1, b) -> {
                        if (b){
                            addToFavActionCV.setStrokeWidth(3);
                        } else {
                            addToFavActionCV.setStrokeWidth(0);
                        }
                    });

                    watchNowActionCV.setOnFocusChangeListener((view1, b) -> {
                        if (b){
                            watchNowActionCV.setStrokeWidth(3);
                        } else {
                            watchNowActionCV.setStrokeWidth(0);
                        }
                    });

                    //------------------------------------------------------------------------------

                    addToFavActionCV.setOnClickListener(v -> {

                        MyDatabaseHandler handler = new MyDatabaseHandler(activity);

                        AnimeFavoriteListModel favoriteListModel = handler.getAnimeFromFavorite(animeId);

                        if (!favoriteListModel.getAnimeId().equalsIgnoreCase("")) {
                            Toast.makeText(activity, "Already added to favorite.", Toast.LENGTH_SHORT).show();
                        } else {
                            favoriteListModel.setAnimeId(animeId);
                            favoriteListModel.setAnimeName(animeTitle);
                            favoriteListModel.setAnimeImageUrl(animeImageUrl);
                            favoriteListModel.setAnimeServer(finalServer);

                            handler.addAnimeToFavorite(favoriteListModel);
                            Toast.makeText(activity, "Added to favorite list.", Toast.LENGTH_SHORT).show();
                        }

                        bottomSheetDialog.dismiss();
                    });

                    //------------------------------------------------------------------------------

                    watchNowActionCV.setOnClickListener(v -> {

                        Intent intent = new Intent(activity, DetailsActivity.class);
                        intent.putExtra("animeId", animeId);
                        intent.putExtra("server", finalServer);
                        activity.startActivity(intent);

                        bottomSheetDialog.dismiss();
                    });

                    bottomSheetDialog.show();
                }
            } catch (Exception e) {
                Log.e(TAG, "onBindViewHolder: ", e);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return allAnime.length();
    }

    @Override
    public long getItemId(int position) {
//        return super.getItemId(position);

        try {
            JSONObject item = allAnime.getJSONObject(position);
            // Return a unique and stable ID for the item.
            return item.getLong("id");
        } catch (JSONException e) {
            Log.e(TAG, "onBindViewHolder: ", e);
            return RecyclerView.NO_ID;
        }
    }

    public static class MyCustomViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        RelativeLayout relativeLayout;
        ImageView imageView;
        TextView animeNameTV, isAnimeSubDubTV, releaseDateTV;

        public MyCustomViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.rootCardView);
            relativeLayout = itemView.findViewById(R.id.relativeLayout);
            imageView = itemView.findViewById(R.id.thumb_img_view);
            animeNameTV = itemView.findViewById(R.id.anime_title_tv);
            isAnimeSubDubTV = itemView.findViewById(R.id.is_anime_sub_dub_tv);
            releaseDateTV = itemView.findViewById(R.id.releaseDateTV);
        }
    }
}
