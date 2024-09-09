package com.fogplix.tv.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fogplix.tv.R;
import com.fogplix.tv.adapters.GenreCardAdapter;

public class GenresFragment extends Fragment {

    private final String[] genresArray = {
            "action","adventure","adult-cast","cars","comedy","crime","dementia",
            "demons","detective","drama","dub","ecchi","family","fantasy","game","gourmet","harem",
            "historical","horror","josei","kids","magic","martial-arts","mecha",
            "military","mystery","parody","police","psychological","romance",
            "samurai","school","sci-fi","seinen","shoujo","shoujo-ai","shounen",
            "shounen-ai","space","sports","super-power","supernatural",
            "suspense","thriller","vampire","yaoi","yuri","isekai"
    };

    private RecyclerView recyclerView;

    private int[] colorsArray;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View allViews = inflater.inflate(R.layout.fragment_genres, container, false);

        initVars(allViews);

        GenreCardAdapter adapter = new GenreCardAdapter(requireContext(), genresArray, colorsArray);
        recyclerView.setAdapter(adapter);

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(),6);
        recyclerView.setLayoutManager(layoutManager);

        return allViews;
    }

    private void initVars(View allViews) {
        colorsArray = getResources().getIntArray(R.array.genre_card_colors);
        recyclerView = allViews.findViewById(R.id.recyclerView);
    }
}