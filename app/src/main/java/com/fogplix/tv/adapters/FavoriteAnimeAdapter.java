package com.fogplix.tv.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fogplix.tv.R;
import com.fogplix.tv.activities.DetailsActivity;
import com.fogplix.tv.helpers.MyDatabaseHandler;
import com.fogplix.tv.model.AnimeFavoriteListModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class FavoriteAnimeAdapter extends RecyclerView.Adapter<FavoriteAnimeAdapter.MyCustomViewHolder> {

    private final Activity activity;
    private final List<AnimeFavoriteListModel> favoriteAnimeList;

    public FavoriteAnimeAdapter(Activity activity, List<AnimeFavoriteListModel> favoriteAnimeList){
        this.activity = activity;
        this.favoriteAnimeList = favoriteAnimeList;
    }

    @NonNull
    @Override
    public MyCustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View view = layoutInflater.inflate(R.layout.sample_item_card_design, parent, false);
        return new FavoriteAnimeAdapter.MyCustomViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyCustomViewHolder holder, int position) {

        String animeId = favoriteAnimeList.get(holder.getBindingAdapterPosition()).getAnimeId();
        String animeTitle = favoriteAnimeList.get(holder.getBindingAdapterPosition()).getAnimeName();
        String animeImageUrl = favoriteAnimeList.get(holder.getBindingAdapterPosition()).getAnimeImageUrl();
        String animeServer = favoriteAnimeList.get(holder.getBindingAdapterPosition()).getAnimeServer();

        holder.animeNameTV.setText(animeTitle);

        if (animeTitle.toLowerCase().contains("(dub")){
            holder.isAnimeSubDubTV.setText("dub");
            holder.isAnimeSubDubTV.setBackgroundColor(activity.getColor(R.color.green));
        }
        else if (animeTitle.toLowerCase().contains("hindi")){
            holder.isAnimeSubDubTV.setText("hindi");
            holder.isAnimeSubDubTV.setBackgroundColor(activity.getColor(R.color.red));
        }
        else{
            holder.isAnimeSubDubTV.setText("sub");
            holder.isAnimeSubDubTV.setBackgroundColor(activity.getColor(R.color.fade_blue));
        }

        Glide.with(activity)
                .load(animeImageUrl)
                .placeholder(R.drawable.preload_thumb)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.imageView);


        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, DetailsActivity.class);
            intent.putExtra("animeId", animeId);
            intent.putExtra("server", animeServer);
            activity.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(view -> {

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity, R.style.BottomSheetDialog);
            bottomSheetDialog.setCancelable(true);
            bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            bottomSheetDialog.setContentView(R.layout.sample_remove_favorite_bottomsheet_layout);

            TextView animeTvInBottomSheet = bottomSheetDialog.findViewById(R.id.anime_title_tv);
            CardView removeFavActionCV = bottomSheetDialog.findViewById(R.id.remove_favorite_action_cv);

            if (animeTvInBottomSheet != null && removeFavActionCV != null) {
                animeTvInBottomSheet.setText(animeTitle);

                removeFavActionCV.setOnClickListener(v -> {

                    MyDatabaseHandler handler = new MyDatabaseHandler(activity);

                    if (handler.deleteAnimeFromFavorite(animeId)) {

                        favoriteAnimeList.remove(holder.getBindingAdapterPosition());
                        notifyItemRemoved(holder.getBindingAdapterPosition());

                        Toast.makeText(activity, "Removed from favorite list.", Toast.LENGTH_SHORT).show();

                        TextView textView = activity.findViewById(R.id.ownToolbar).findViewById(R.id.ownToolbarTV);
                        textView.setText("Favorites (" + favoriteAnimeList.size() + ")");

                        if (favoriteAnimeList.size() == 0){
                            activity.findViewById(R.id.noAnimeContainer).setVisibility(View.VISIBLE);
                        }
                    }
                    else{
                        Toast.makeText(activity, "Error: failed to remove.", Toast.LENGTH_SHORT).show();
                    }

                    bottomSheetDialog.dismiss();
                });

                bottomSheetDialog.show();
            }

            return false;
        });
    }

    @Override
    public int getItemCount() {
        return favoriteAnimeList.size();
    }

    public static class MyCustomViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView animeNameTV, isAnimeSubDubTV;

        public MyCustomViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.thumb_img_view);
            animeNameTV = itemView.findViewById(R.id.anime_title_tv);
            isAnimeSubDubTV = itemView.findViewById(R.id.is_anime_sub_dub_tv);
        }
    }
}
