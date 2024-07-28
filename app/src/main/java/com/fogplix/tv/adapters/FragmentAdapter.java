package com.fogplix.tv.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.fogplix.tv.fragments.GenresFragment;
import com.fogplix.tv.fragments.MoviesFragment;
import com.fogplix.tv.fragments.PopularFragment;
import com.fogplix.tv.fragments.RecentUploadsFragment;

public class FragmentAdapter extends FragmentStateAdapter {

    int totalTab;

    public FragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, int totalTab) {
        super(fragmentManager, lifecycle);
        this.totalTab = totalTab;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        if (position == 0){
            return new RecentUploadsFragment(1);
        }
        else if (position == 1){
            return new RecentUploadsFragment(2);
        }
        else if (position == 2){
            return new RecentUploadsFragment(3);
        }
        else if (position == 3){
            return new GenresFragment();
        }
        else if (position == 4){
            return new PopularFragment();
        }
        else if (position == 5){
            return new MoviesFragment();
        }
        else {
            return new RecentUploadsFragment(1);
        }
    }

    @Override
    public int getItemCount() {
        return totalTab;
    }
}
