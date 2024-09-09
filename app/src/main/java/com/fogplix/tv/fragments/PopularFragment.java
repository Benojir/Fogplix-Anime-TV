package com.fogplix.tv.fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.fogplix.tv.R;
import com.fogplix.tv.adapters.ItemsListAdapter;
import com.fogplix.tv.callbacks.AnimeScraperCallback;
import com.fogplix.tv.databinding.FragmentPopularBinding;
import com.fogplix.tv.helpers.CustomMethods;
import com.fogplix.tv.helpers.Scraper;

import org.json.JSONArray;
import org.json.JSONException;

public class PopularFragment extends Fragment {

    private FragmentPopularBinding binding;
    private Activity activity;
    private ItemsListAdapter rvAdapter;
    private GridLayoutManager layoutManager;
    private final JSONArray allAnime = new JSONArray();
    private int page = 1;
    private boolean alreadyReachedLastPage = false;
    private boolean firstTimeSearch = true;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = getActivity();

        if (activity != null) {

            int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

            rvAdapter = new ItemsListAdapter(activity, allAnime);
            binding.recyclerView.setAdapter(rvAdapter);

            switch (screenSize) {
                case Configuration.SCREENLAYOUT_SIZE_LARGE:
                    layoutManager = new GridLayoutManager(activity, 8);
                    break;
                case Configuration.SCREENLAYOUT_SIZE_SMALL:
                    layoutManager = new GridLayoutManager(activity, 3);
                    break;
                case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                    layoutManager = new GridLayoutManager(activity, 10);
                    break;
                default:
                    layoutManager = new GridLayoutManager(activity, 6);
            }

            binding.recyclerView.setLayoutManager(layoutManager);

            loadAnime(page);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentPopularBinding.inflate(inflater, container, false);

        binding.loadMoreAnimeBtn.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus){
                binding.loadMoreAnimeBtn.setBackgroundColor(activity.getColor(R.color.red));
            } else {
                binding.loadMoreAnimeBtn.setBackgroundColor(activity.getColor(R.color.fade_blue));
            }
        });

        binding.loadMoreAnimeBtn.setOnClickListener(view -> {
            page = page + 1;
            loadAnime(page);
        });


        return binding.getRoot();
    }

//    ----------------------------------------------------------------------------------------------

    private void loadAnime(int page){

        String pageLink = activity.getString(R.string.gogoanime_url) + "/popular.html?page=" + page;

        if (!alreadyReachedLastPage){

            if (page > 1){
                binding.loaderProgressBottom.setVisibility(View.VISIBLE);
                binding.loadMoreAnimeBtn.setVisibility(View.GONE);
            }

            Scraper scraper = new Scraper(activity, new AnimeScraperCallback() {
                @Override
                public void onScrapeComplete(JSONArray resultAnime) {

                    binding.loaderProgressCenter.setVisibility(View.GONE);
                    binding.loaderProgressBottom.setVisibility(View.GONE);
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
                        Log.e("MADARA", "onScrapeComplete: ", e);
                        CustomMethods.errorAlert(activity, "Error", e.getMessage(), "OK", false);
                    }
                }

                @Override
                public void onScrapeFailed(String error) {
                    alreadyReachedLastPage = true;
                    binding.loaderProgressCenter.setVisibility(View.GONE);
                    binding.loaderProgressBottom.setVisibility(View.GONE);
                    binding.loadMoreAnimeBtn.setVisibility(View.GONE);

                    if (firstTimeSearch){
                        CustomMethods.errorAlert(activity, "Error PL", error, "OK", false);
                    }
                }
            });

            scraper.scrapeAnime(pageLink);

        }
    }
}