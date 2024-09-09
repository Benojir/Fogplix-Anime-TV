package com.fogplix.tv.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.fogplix.tv.BuildConfig;
import com.fogplix.tv.R;
import com.fogplix.tv.adapters.FragmentAdapter;
import com.fogplix.tv.helpers.CustomMethods;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private TextView importantNoticeTV;
    private boolean keepSplashScreen = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> keepSplashScreen);
        new Handler().postDelayed(() -> keepSplashScreen = false, 3000);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        setContentView(R.layout.activity_main);

        initVars();

        ////////////////////////////////////////////////////////////////////////////////////////////

        View ownToolbar =  findViewById(R.id.ownToolbar);

        ImageButton favoritePageBtn = ownToolbar.findViewById(R.id.navbarLeftBtn);
        ImageButton searchBtn = ownToolbar.findViewById(R.id.navbarRightBtn);
        ImageButton navInfoBtn = ownToolbar.findViewById(R.id.navInfoBtn);

        favoritePageBtn.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this, R.drawable.favorite_48));
        favoritePageBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FavoriteActivity.class)));
        favoritePageBtn.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus){
                favoritePageBtn.setBackgroundResource(R.drawable.button_focused);
            } else {
                favoritePageBtn.setBackgroundResource(R.drawable.button_default);
            }
        });

        searchBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SearchActivity.class)));
        searchBtn.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus){
                searchBtn.setBackgroundResource(R.drawable.button_focused);
            } else {
                searchBtn.setBackgroundResource(R.drawable.button_default);
            }
        });


        navInfoBtn.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, navInfoBtn);
            popupMenu.getMenuInflater().inflate(R.menu.top_right_popup_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.open_settings){
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.show_version){
                    Toast.makeText(this, BuildConfig.VERSION_NAME, Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    return false;
                }
            });
            popupMenu.show();
        });

        navInfoBtn.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus){
                navInfoBtn.setBackgroundResource(R.drawable.button_focused);
            } else {
                navInfoBtn.setBackgroundResource(R.drawable.button_default);
            }
        });

        ////////////////////////////////////////////////////////////////////////////////////////////

        CustomMethods.checkForUpdateOnStartApp(this);
        CustomMethods.checkNewNotice(this, importantNoticeTV);

        if (!CustomMethods.isInternetOn(this)) {
            Toast.makeText(this, "No internet connection.", Toast.LENGTH_LONG).show();
            finish();
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentAdapter fragmentAdapter = new FragmentAdapter(fragmentManager, getLifecycle(), tabLayout.getTabCount());
        viewPager2.setAdapter(fragmentAdapter);
        viewPager2.setUserInputEnabled(true);

        //for changing the first tab icon color
        Objects.requireNonNull(Objects.requireNonNull(tabLayout.getTabAt(0)).getIcon()).setColorFilter(getColor(R.color.white), PorterDuff.Mode.SRC_IN);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());

                Objects.requireNonNull(tab.getIcon()).setColorFilter(getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Objects.requireNonNull(tab.getIcon()).setColorFilter(getColor(R.color.grey), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }

//    ----------------------------------------------------------------------------------------------

    private void initVars() {
        viewPager2 = findViewById(R.id.fragmentContainerViewPager2Main);
        tabLayout = findViewById(R.id.tabLayout);
        importantNoticeTV = findViewById(R.id.important_notice_tv);
    }
}