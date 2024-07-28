package com.fogplix.tv.activities;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.fogplix.tv.adapters.FavoriteAnimeAdapter;
import com.fogplix.tv.databinding.ActivityFavoriteBinding;
import com.fogplix.tv.databinding.OwnToolbarBinding;
import com.fogplix.tv.helpers.MyDatabaseHandler;
import com.fogplix.tv.model.AnimeFavoriteListModel;

import java.util.List;

public class FavoriteActivity extends AppCompatActivity {

    ActivityFavoriteBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        OwnToolbarBinding ownToolbarBinding = binding.ownToolbar;

        ownToolbarBinding.imageViewMiddle.setVisibility(View.GONE);
        ownToolbarBinding.navbarRightBtn.setVisibility(View.GONE);
        ownToolbarBinding.ownToolbarTV.setVisibility(View.VISIBLE);
        ownToolbarBinding.navbarLeftBtn.setOnClickListener(v -> onBackPressed());


        MyDatabaseHandler handler = new MyDatabaseHandler(this);

        List<AnimeFavoriteListModel> favoriteLists = handler.getAllFavoriteAnime();

        if (favoriteLists.size() > 0){

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

            ownToolbarBinding.ownToolbarTV.setText("Favorites (" + favoriteLists.size() + ")");
        }
        else{
            binding.recyclerView.setVisibility(View.GONE);
            binding.noAnimeContainer.setVisibility(View.VISIBLE);
        }
    }
}