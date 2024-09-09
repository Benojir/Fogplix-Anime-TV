package com.fogplix.tv.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AbsListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fogplix.tv.R;
import com.fogplix.tv.adapters.ItemsListAdapter;
import com.fogplix.tv.callbacks.AnimeScraperCallback;
import com.fogplix.tv.databinding.ActivitySearchBinding;
import com.fogplix.tv.helpers.CustomMethods;
import com.fogplix.tv.helpers.Scraper;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private GridLayoutManager layoutManager;
    private ItemsListAdapter rvAdapter;
    private JSONArray allAnime = new JSONArray();
    private String keyword = "";
    private int page = 1;
    private int currentItems, totalItems, scrollOutItems;
    private boolean isScrolling = false;
    private boolean alreadyReachedLastPage = false;
    private static final int RESULT_SPEECH_CODE = 541;
    private boolean firstTimeSearch = true;
    private final Handler handler = new Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CustomMethods.showKeyboard(this, binding.searchET);
        binding.searchET.requestFocus();

        binding.searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // Remove any pending search runnable
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                keyword = s.toString().trim();

                if (keyword.isEmpty() || keyword.length() < 2) {
                    return;
                }

                // Create a new Runnable to execute the search method
                searchRunnable = () -> resetEverythingAndPerformFirstSearch(keyword);

                // Post the Runnable with a delay
                handler.postDelayed(searchRunnable, 1500);
            }
        });

        binding.searchET.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                CustomMethods.showKeyboard(SearchActivity.this, binding.searchET);
            }
        });

        binding.backBtn.setOnClickListener(view -> onBackPressed());
        binding.backBtn.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.backBtn.setBackgroundResource(R.drawable.button_focused);
            } else {
                binding.backBtn.setBackgroundResource(R.drawable.button_default);
            }
        });

        binding.micIconBtn.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
            try {
                startActivityForResult(intent, RESULT_SPEECH_CODE);
            } catch (Exception e) {
                CustomMethods.errorAlert(this, "Error", e.getMessage(), "Ok", false);
            }
        });

        binding.micIconBtn.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.micIconBtn.setBackgroundResource(R.drawable.button_focused);
            } else {
                binding.micIconBtn.setBackgroundResource(R.drawable.button_default);
            }
        });
        //..........................................................................................

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                currentItems = layoutManager.getChildCount();
                totalItems = layoutManager.getItemCount();
                scrollOutItems = layoutManager.findFirstVisibleItemPosition();

                if (isScrolling && (currentItems + scrollOutItems == totalItems)) {

                    isScrolling = false;
                    page = page + 1;
                    searchAnime(keyword, page);
                }
            }
        });
    }

//--------------------------------------------------------------------------------------------------

    private void resetEverythingAndPerformFirstSearch(String query) {
        binding.noAnimeContainer.setVisibility(View.GONE);
        binding.searchPageImage.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.loaderProgressInCenter.setVisibility(View.VISIBLE);
        allAnime = new JSONArray();
        firstTimeSearch = true;
        alreadyReachedLastPage = false;
        rvAdapter = new ItemsListAdapter(SearchActivity.this, allAnime);
        binding.recyclerView.setAdapter(rvAdapter);

        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                layoutManager = new GridLayoutManager(SearchActivity.this, 8);
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                layoutManager = new GridLayoutManager(SearchActivity.this, 3);
                break;
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                layoutManager = new GridLayoutManager(SearchActivity.this, 10);
                break;
            default:
                layoutManager = new GridLayoutManager(SearchActivity.this, 6);
        }

        binding.recyclerView.setLayoutManager(layoutManager);
        page = 1;
        searchAnime(query, page);
    }

//--------------------------------------------------------------------------------------------------

    private void searchAnime(String keyword, int page) {

        String searchPageLink = getString(R.string.gogoanime_url) + "/search.html?keyword=" + keyword + "&page=" + page;

        if (!alreadyReachedLastPage) {

            if (page > 1) {
                binding.loaderProgressOnBottom.setVisibility(View.VISIBLE);
            }

            Scraper scraper = new Scraper(SearchActivity.this, new AnimeScraperCallback() {
                @Override
                public void onScrapeComplete(JSONArray resultAnime) {
                    binding.noAnimeContainer.setVisibility(View.GONE);
                    binding.loaderProgressOnBottom.setVisibility(View.GONE);
                    binding.loaderProgressInCenter.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);

                    try {

                        if (resultAnime.length() <= 0) {
                            alreadyReachedLastPage = true;
                        }

                        if (resultAnime.length() <= 0 && firstTimeSearch) {
                            binding.noAnimeContainer.setVisibility(View.VISIBLE);
                        } else {
                            firstTimeSearch = false;

                            int startPosition = rvAdapter.getItemCount(); // Get the current item count

                            CustomMethods.mergeTwoJsonArray(allAnime, resultAnime);

                            int itemCount = rvAdapter.getItemCount() - startPosition; // Calculate the number of inserted items

                            rvAdapter.notifyItemRangeInserted(startPosition, itemCount);
                        }

                    } catch (JSONException e) {
                        CustomMethods.errorAlert(SearchActivity.this, "Error (Json)", e.getMessage(), "OK", false);
                    }
                }

                @Override
                public void onScrapeFailed(String error) {
                    alreadyReachedLastPage = true;
                    if (firstTimeSearch) {
                        binding.noAnimeContainer.setVisibility(View.VISIBLE);
                    }
                }
            });

            scraper.scrapeAnime(searchPageLink);
        }
    }

//--------------------------------------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_SPEECH_CODE) {

            if (resultCode == RESULT_OK && data != null) {

                ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                String oldTxt = binding.searchET.getText().toString();

                if (!oldTxt.isEmpty()) {
                    oldTxt += " ";
                }

                if (text != null) {
                    keyword = oldTxt + text.get(0);
                    binding.searchET.setText(keyword);
                    binding.searchET.setSelection(keyword.length());
                    resetEverythingAndPerformFirstSearch(keyword);
                }
            }
        }
    }


//--------------------------------------------------------------------------------------------------

}