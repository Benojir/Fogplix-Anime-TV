package com.fogplix.tv.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.fogplix.tv.R;
import com.fogplix.tv.activities.GenreViewActivity;

import java.util.Random;

public class GenreCardAdapter extends RecyclerView.Adapter<GenreCardAdapter.MyCustomViewHolder> {

    private final Context context;
    private final String[] allGenresArray;
    private final int[] colorsArray;


    public GenreCardAdapter(Context context, String[] allGenresArray, int[] colorsArray) {
        this.context = context;
        this.allGenresArray = allGenresArray;
        this.colorsArray = colorsArray;
    }

    @NonNull
    @Override
    public GenreCardAdapter.MyCustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.sample_genre_card_design, parent, false);
        return new GenreCardAdapter.MyCustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreCardAdapter.MyCustomViewHolder holder, int position) {

        int colorPos = new Random().nextInt(colorsArray.length);
        int color = colorsArray[colorPos];

        holder.cardView.setCardBackgroundColor(color);

        String genre = allGenresArray[position].replace("-", " ");
        holder.genreTV.setText(genre);

        holder.cardView.setOnClickListener(view -> {
            Intent intent = new Intent(context, GenreViewActivity.class);
            intent.putExtra("genre", allGenresArray[position]);
            context.startActivity(intent);
        });

        holder.cardView.setOnLongClickListener(view -> {
            Toast.makeText(context, allGenresArray[position].toUpperCase(), Toast.LENGTH_SHORT).show();

            Vibrator vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibe.vibrate(VibrationEffect.createOneShot(100, 1));
            return false;
        });

        holder.itemView.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                holder.cardView.setCardBackgroundColor(context.getColor(R.color.white));
                holder.genreTV.setTextColor(context.getColor(R.color.black));
            } else {
                holder.cardView.setCardBackgroundColor(color);
                holder.genreTV.setTextColor(context.getColor(R.color.white));
            }
        });
    }

    @Override
    public int getItemCount() {
        return allGenresArray.length;
    }

    public static class MyCustomViewHolder extends RecyclerView.ViewHolder {

        TextView genreTV;
        CardView cardView;

        public MyCustomViewHolder(View itemView) {
            super(itemView);
            genreTV = itemView.findViewById(R.id.genreTV);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
