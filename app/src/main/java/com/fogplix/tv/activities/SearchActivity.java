package com.fogplix.tv.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.Toast;

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
    private String lastSearchedKeyword = "";
    private boolean firstTimeSearch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.searchET.setOnEditorActionListener((textView, actionId, keyEvent) -> {

            CustomMethods.hideKeyboard(this, textView);

            if (actionId == EditorInfo.IME_ACTION_DONE && !binding.searchET.getText().toString().isEmpty()){

                binding.noAnimeContainer.setVisibility(View.GONE);
                binding.searchPageImage.setVisibility(View.GONE);

                keyword = binding.searchET.getText().toString().trim();

                if (!keyword.equalsIgnoreCase(lastSearchedKeyword)){        //it means new keyword and reset everything

                    firstTimeSearch = true;
                    alreadyReachedLastPage = false;

                    binding.loaderProgressInCenter.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.INVISIBLE);

                    lastSearchedKeyword = keyword;

                    page = 1;

                    allAnime = new JSONArray();

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

                    searchAnime(keyword, page);
                }

                return true;
            }
            else{
                Toast.makeText(this, "No text entered.", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        binding.backBtn.setOnClickListener(view -> onBackPressed());

        CustomMethods.showKeyBoard(this, binding.searchET);

        //..........................................................................................

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                currentItems = layoutManager.getChildCount();
                totalItems = layoutManager.getItemCount();
                scrollOutItems = layoutManager.findFirstVisibleItemPosition();

                if (isScrolling && (currentItems + scrollOutItems == totalItems)){

                    isScrolling = false;
                    page = page + 1;
                    searchAnime(keyword, page);
                }
            }
        });

        //..........................................................................................

        binding.micIconBtn.setOnClickListener(v -> {

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

            try {
                startActivityForResult(intent, RESULT_SPEECH_CODE);
            }
            catch (Exception e) {
                e.printStackTrace();
                CustomMethods.errorAlert(this, "Error", e.getMessage(), "Ok", false);
            }
        });
    }
//--------------------------------------------------------------------------------------------------

    private void searchAnime(String keyword, int page){

        String searchPageLink = getString(R.string.gogoanime_url) + "/search.html?keyword=" + keyword + "&page=" + page;

        if (!alreadyReachedLastPage){

            if (page > 1){
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

                        if (resultAnime.length() <= 0){
                            alreadyReachedLastPage = true;
                        }

                        if (resultAnime.length() <= 0 && firstTimeSearch){
                            binding.noAnimeContainer.setVisibility(View.VISIBLE);

                        } else{

                            firstTimeSearch = false;

                            int startPosition = rvAdapter.getItemCount(); // Get the current item count

                            CustomMethods.mergeTwoJsonArray(allAnime, resultAnime);

                            int itemCount = rvAdapter.getItemCount() - startPosition; // Calculate the number of inserted items

                            rvAdapter.notifyItemRangeInserted(startPosition, itemCount);
                        }

                    } catch (JSONException e){
                        e.printStackTrace();
                        CustomMethods.errorAlert(SearchActivity.this, "Error (Json)", e.getMessage(), "OK", false);
                    }
                }

                @Override
                public void onScrapeFailed(String error) {
                    alreadyReachedLastPage = true;
                    if (firstTimeSearch){
                        binding.noAnimeContainer.setVisibility(View.VISIBLE);
                    }
                }
            });

            scraper.scrapeAnime(searchPageLink);
        }
    }


//--------------------------------------------------------------------------------------------------

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_SPEECH_CODE){

            if(resultCode == RESULT_OK && data != null){

                ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                String oldTxt = binding.searchET.getText().toString();

                if (!oldTxt.equals("")){
                    oldTxt += " ";
                }
                assert text != null;
                binding.searchET.setText(oldTxt + text.get(0));
                binding.searchET.setSelection(binding.searchET.getText().length());
            }
        }
    }


//--------------------------------------------------------------------------------------------------

}