package com.fogplix.tv.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fogplix.tv.R;
import com.fogplix.tv.adapters.EpisodesButtonsAdapter;
import com.fogplix.tv.callbacks.DetailsScraperCallback;
import com.fogplix.tv.databinding.ActivityDetailsBinding;
import com.fogplix.tv.helpers.CustomMethods;
import com.fogplix.tv.helpers.Scraper;

import org.json.JSONArray;
import org.json.JSONObject;

public class DetailsActivity extends AppCompatActivity {

    private ActivityDetailsBinding binding;
    private EpisodesButtonsAdapter episodesButtonsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(binding.getRoot());

        Intent intent = getIntent();

        String animeId = intent.getStringExtra("animeId");

        String episodeId = null;

        if (intent.hasExtra("episodeId")){
            episodeId = intent.getStringExtra("episodeId");
        }

        Scraper scraper = new Scraper(DetailsActivity.this, new DetailsScraperCallback() {
            @SuppressLint("UnsafeOptInUsageError")
            @Override
            public void onScrapingComplete(JSONObject animeDetails) {

                try {
                    binding.progressBarContainer.setVisibility(View.GONE);
                    binding.detailsContainerScrollView.setVisibility(View.VISIBLE);

                    Glide.with(DetailsActivity.this)
                            .load(animeDetails.getString("animeImg"))
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .placeholder(R.drawable.preload_thumb)
                            .into(binding.thumbnailImageView);

                    String animeTitle = animeDetails.getString("animeTitle");
                    String animeStatus = animeDetails.getString("status");
                    String animeType = animeDetails.getString("type");
                    String animeReleasedDate = animeDetails.getString("releasedDate");
                    String animeTotalEpisodes = animeDetails.getString("totalEpisodes");

                    binding.animeTitleTV.setText(animeTitle);
                    binding.statusTV.setText(CustomMethods.capitalize(animeStatus));
                    binding.animeTypeTV.setText(animeType);
                    binding.releaseDateTV.setText(animeReleasedDate);
                    binding.totalEpisodesTV.setText(animeTotalEpisodes);
                    binding.synopsysTV.setText(animeDetails.getString("synopsis"));

                    StringBuilder genres = new StringBuilder();

                    JSONArray genresArray = animeDetails.getJSONArray("genres");

                    for (int i = 0; i < genresArray.length(); i++) {
                        genres.append(", ").append(CustomMethods.capitalize(genresArray.getString(i)));
                    }
                    binding.genresTV.setText(genres.substring(1).trim());


                    JSONArray episodesListArray = animeDetails.getJSONArray("episodesList");

                    episodesButtonsAdapter = new EpisodesButtonsAdapter(DetailsActivity.this, episodesListArray, animeId, animeTitle);
                    binding.episodesBtnRecyclerView.setAdapter(episodesButtonsAdapter);

                    GridLayoutManager layoutManager;

                    int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

                    switch (screenSize) {
                        case Configuration.SCREENLAYOUT_SIZE_LARGE:
                            layoutManager = new GridLayoutManager(DetailsActivity.this, 8);
                            break;
                        case Configuration.SCREENLAYOUT_SIZE_SMALL:
                            layoutManager = new GridLayoutManager(DetailsActivity.this, 3);
                            break;
                        case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                            layoutManager = new GridLayoutManager(DetailsActivity.this, 10);
                            break;
                        default:
                            layoutManager = new GridLayoutManager(DetailsActivity.this, 6);
                    }

//                    GridLayoutManager layoutManager = new GridLayoutManager(DetailsActivity.this, 3);
                    binding.episodesBtnRecyclerView.setLayoutManager(layoutManager);

                } catch (Exception e) {
                    e.printStackTrace();
                    CustomMethods.errorAlert(DetailsActivity.this, "Error (Json DT)", e.getMessage(), "OK", true);
                }
            }

            @Override
            public void onScrapingFailed(String error) {
                CustomMethods.errorAlert(DetailsActivity.this, "Error (Json DT)", error, "OK", true);
            }
        });

        scraper.scrapeDetails(animeId, episodeId);

        binding.backBtn.setOnClickListener(view -> onBackPressed());
        binding.searchBtn.setOnClickListener(view -> startActivity(new Intent(this, SearchActivity.class)));
    }

//    ----------------------------------------------------------------------------------------------

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();

        if (episodesButtonsAdapter != null) {
            episodesButtonsAdapter.notifyDataSetChanged();
        }
    }
}