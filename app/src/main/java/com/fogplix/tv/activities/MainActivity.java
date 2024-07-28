package com.fogplix.tv.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.fogplix.tv.BuildConfig;
import com.fogplix.tv.R;
import com.fogplix.tv.adapters.FragmentAdapter;
import com.fogplix.tv.helpers.CustomMethods;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private TextView importantNoticeTV;
    private NavigationView navigationView;
    private boolean keepSplashScreen = true;
    private boolean doubleBackPressed = false;

    @SuppressLint("SetTextI18n")
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

        ImageButton navLeftBtn = ownToolbar.findViewById(R.id.navbarLeftBtn);

        navLeftBtn.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this, R.drawable.favorite_48));
        navLeftBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FavoriteActivity.class)));

        ownToolbar.findViewById(R.id.navbarRightBtn).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SearchActivity.class)));

        ImageButton navInfoBtn = ownToolbar.findViewById(R.id.navInfoBtn);

        navInfoBtn.setOnClickListener(v -> {

            PopupMenu popupMenu = new PopupMenu(MainActivity.this, navInfoBtn);
            popupMenu.getMenuInflater().inflate(R.menu.top_right_popup_menu, popupMenu.getMenu());

            // Set up the click listener for the menu items
            popupMenu.setOnMenuItemClickListener(item -> {

                if (item.getItemId() == R.id.show_version){
                    Toast.makeText(this, BuildConfig.VERSION_NAME, Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    return false;
                }
            });

            // Show the popup menu
            popupMenu.show();
        });

        ////////////////////////////////////////////////////////////////////////////////////////////

        CustomMethods.checkForUpdateOnStartApp(this);
        CustomMethods.checkNewNotice(this, importantNoticeTV);
        CustomMethods.checkPlayableServersStatus(this);

        if (!CustomMethods.isInternetOn(this)) {
            Toast.makeText(this, "No internet connection.", Toast.LENGTH_LONG).show();
            doubleBackPressed = true;
            onBackPressed();
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        View headView = navigationView.getHeaderView(0);

        ((TextView) headView.findViewById(R.id.header_layout_version_tv)).setText("Version: " + BuildConfig.VERSION_NAME);

        headView.findViewById(R.id.closeDrawerBtn).setOnClickListener(v -> drawerLayout.closeDrawers());

        navigationViewItemClickedActions(navigationView);

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

    private void navigationViewItemClickedActions(NavigationView navigationView) {

        navigationView.setNavigationItemSelectedListener(item -> {

            if (item.getItemId() == R.id.favorite_anime_menu_item) {

                startActivity(new Intent(getApplicationContext(), FavoriteActivity.class));

                new Handler().postDelayed(() -> {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                }, 500);
            } else if (item.getItemId() == R.id.report_bug_action) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.official_telegram_group))));
            } else if (item.getItemId() == R.id.share_action) {
                Intent intent1 = new Intent(Intent.ACTION_SEND);
                intent1.setType("text/plain");
                intent1.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.app_sharing_message) + "\n" + getString(R.string.official_website));
                startActivity(Intent.createChooser(intent1, "Share via"));
            } else if (item.getItemId() == R.id.more_apps_action) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.more_apps))));
            } else if (item.getItemId() == R.id.visit_website) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.official_website))));
            } else if (item.getItemId() == R.id.visit_telegram) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.official_telegram_channel))));
            }
            return false;
        });
    }

//    ----------------------------------------------------------------------------------------------

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackPressed) {
                super.onBackPressed();
            } else {
                this.doubleBackPressed = true;
                Snackbar.make(drawerLayout, "Double press to exit!", Snackbar.LENGTH_LONG).show();

                new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackPressed = false, 2000);
            }
        }
    }
//    ----------------------------------------------------------------------------------------------

    private void initVars() {
        drawerLayout = findViewById(R.id.home_page_drawerlayout);
        viewPager2 = findViewById(R.id.fragmentContainerViewPager2Main);
        tabLayout = findViewById(R.id.tabLayout);
        navigationView = findViewById(R.id.navigation_drawer);
        importantNoticeTV = findViewById(R.id.important_notice_tv);
    }
}