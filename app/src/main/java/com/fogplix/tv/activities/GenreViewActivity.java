package com.fogplix.tv.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.fogplix.tv.R;
import com.fogplix.tv.adapters.ItemsListAdapter;
import com.fogplix.tv.callbacks.AnimeScraperCallback;
import com.fogplix.tv.databinding.ActivityGenreViewBinding;
import com.fogplix.tv.databinding.OwnToolbarBinding;
import com.fogplix.tv.helpers.CustomMethods;
import com.fogplix.tv.helpers.Scraper;

import org.json.JSONArray;
import org.json.JSONException;

public class GenreViewActivity extends AppCompatActivity {

    private ActivityGenreViewBinding binding;
    private GridLayoutManager layoutManager;
    private ItemsListAdapter rvAdapter;
    private final JSONArray allAnime = new JSONArray();
    private boolean alreadyReachedLastPage = false;
    private int page = 1;
    private String genre;
    private boolean firstTimeSearch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGenreViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        OwnToolbarBinding ownToolbarBinding = binding.ownToolbar;
        ownToolbarBinding.ownToolbarTV.setVisibility(View.VISIBLE);
        ownToolbarBinding.imageViewMiddle.setVisibility(View.GONE);
        ownToolbarBinding.navbarRightBtn.setVisibility(View.GONE);

        genre = getIntent().getStringExtra("genre");

        if (genre != null) {
            String genreName = genre.replace("-", " ");
            ownToolbarBinding.ownToolbarTV.setText(CustomMethods.capitalize(genreName));
        }

        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        rvAdapter = new ItemsListAdapter(GenreViewActivity.this, allAnime);
        binding.recyclerView.setAdapter(rvAdapter);

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                layoutManager = new GridLayoutManager(GenreViewActivity.this, 8);
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                layoutManager = new GridLayoutManager(GenreViewActivity.this, 3);
                break;
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                layoutManager = new GridLayoutManager(GenreViewActivity.this, 10);
                break;
            default:
                layoutManager = new GridLayoutManager(GenreViewActivity.this, 6);
        }

        binding.recyclerView.setLayoutManager(layoutManager);

        loadAnime(page);

        binding.loadMoreAnimeBtn.setOnFocusChangeListener((view, b) -> {
            if (b){
                binding.loadMoreAnimeBtn.setBackgroundColor(getColor(R.color.orange));
            } else {
                binding.loadMoreAnimeBtn.setBackgroundColor(getColor(R.color.fade_blue));
            }
        });

        binding.loadMoreAnimeBtn.setOnClickListener(view -> {
            page = page + 1;
            loadAnime(page);
        });

        ////////////////////////////////////////////////////////////////////////////////////////////

        ownToolbarBinding.navbarLeftBtn.setOnClickListener(view -> onBackPressed());
    }

//    ----------------------------------------------------------------------------------------------

    private void loadAnime(int page){

        String genrePageLink = getString(R.string.gogoanime_url) + "/genre/" + genre + "?page=" + page;

        if (!alreadyReachedLastPage){

            if (page > 1){
                binding.loaderProgressBottom.setVisibility(View.VISIBLE);
                binding.loadMoreAnimeBtn.setVisibility(View.GONE);
            }

            Scraper scraper = new Scraper(GenreViewActivity.this, new AnimeScraperCallback() {
                @Override
                public void onScrapeComplete(JSONArray resultAnime) {

                    binding.loaderProgressBottom.setVisibility(View.GONE);
                    binding.loaderProgressCenter.setVisibility(View.GONE);
                    binding.loadMoreAnimeBtn.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.VISIBLE);

                    try {

                        if (resultAnime.length() <= 0){
                            alreadyReachedLastPage = true;
                        }

                        firstTimeSearch = false;

                        int startPosition = rvAdapter.getItemCount(); // Get the current item count

                        CustomMethods.mergeTwoJsonArray(allAnime, resultAnime);

                        int itemCount = rvAdapter.getItemCount() - startPosition; // Calculate the number of inserted items

                        int focusedItemPosition = layoutManager.findLastVisibleItemPosition();
                        View focusedItemView = layoutManager.findViewByPosition(focusedItemPosition);

                        rvAdapter.notifyItemRangeInserted(startPosition, itemCount);

                        layoutManager.scrollToPositionWithOffset(focusedItemPosition, 0);
                        if (focusedItemView != null) {
                            focusedItemView.requestFocus();
                        }

                    } catch (JSONException e){
                        e.printStackTrace();
                        CustomMethods.errorAlert(GenreViewActivity.this, "Error (Json)", e.getMessage(), "OK", false);
                    }
                }

                @Override
                public void onScrapeFailed(String error) {
                    alreadyReachedLastPage = true;
                    binding.loaderProgressBottom.setVisibility(View.GONE);
                    binding.loaderProgressCenter.setVisibility(View.GONE);
                    binding.loadMoreAnimeBtn.setVisibility(View.GONE);

                    if (firstTimeSearch){
                        CustomMethods.errorAlert(GenreViewActivity.this, "Error SC", error, "OK", false);
                    }
                }
            });

            scraper.scrapeAnime(genrePageLink);
        }
    }
}