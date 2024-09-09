package com.fogplix.tv.activities;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.fogplix.tv.R;
import com.fogplix.tv.adapters.FavoriteAnimeAdapter;
import com.fogplix.tv.databinding.ActivityFavoriteBinding;
import com.fogplix.tv.databinding.OwnToolbarBinding;
import com.fogplix.tv.helpers.MyDatabaseHandler;

public class FavoriteActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityFavoriteBinding binding = ActivityFavoriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        OwnToolbarBinding ownToolbarBinding = binding.ownToolbar;

        ownToolbarBinding.imageViewMiddle.setVisibility(View.GONE);
        ownToolbarBinding.navbarRightBtn.setVisibility(View.GONE);
        ownToolbarBinding.ownToolbarTV.setVisibility(View.VISIBLE);

        ownToolbarBinding.navbarLeftBtn.setOnClickListener(v -> onBackPressed());
        ownToolbarBinding.navbarLeftBtn.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ownToolbarBinding.navbarLeftBtn.setBackgroundResource(R.drawable.button_focused);
            } else {
                ownToolbarBinding.navbarLeftBtn.setBackgroundResource(R.drawable.button_default);
            }
        });


        MyDatabaseHandler handler = new MyDatabaseHandler(this);

        handler.getAllFavoriteAnime(favoriteLists -> {

            if (!favoriteLists.isEmpty()) {

                int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

                FavoriteAnimeAdapter adapter = new FavoriteAnimeAdapter(this, favoriteLists);
                binding.recyclerView.setAdapter(adapter);

                GridLayoutManager layoutManager;

                switch (screenSize) {
                    case Configuration.SCREENLAYOUT_SIZE_LARGE:
                        layoutManager = new GridLayoutManager(FavoriteActivity.this, 8);
                        break;
                    case Configuration.SCREENLAYOUT_SIZE_SMALL:
                        layoutManager = new GridLayoutManager(FavoriteActivity.this, 3);
                        break;
                    case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                        layoutManager = new GridLayoutManager(FavoriteActivity.this, 10);
                        break;
                    default:
                        layoutManager = new GridLayoutManager(FavoriteActivity.this, 6);
                }
                binding.recyclerView.setLayoutManager(layoutManager);

                String favoritePageTitle = "Favorites (" + favoriteLists.size() + ")";
                ownToolbarBinding.ownToolbarTV.setText(favoritePageTitle);

            } else {
                binding.recyclerView.setVisibility(View.GONE);
                binding.noAnimeContainer.setVisibility(View.VISIBLE);
            }
        });
    }
}