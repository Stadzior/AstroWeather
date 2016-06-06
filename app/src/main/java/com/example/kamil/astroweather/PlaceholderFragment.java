package com.example.kamil.astroweather;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.kamil.astroweather.MainActivity;

public class PlaceholderFragment extends Fragment {

    private static final String SECTION_NAME_PROPERTY = "section_name";

    public PlaceholderFragment() {
    }

    private static String sectionTitle;

    public static PlaceholderFragment newInstance(int position) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        sectionTitle = position == 0 ? "Sun" : "Moon";
        args.putString(SECTION_NAME_PROPERTY,sectionTitle);
        fragment.setArguments(args);
        return fragment;
    }

    private static int tabCounter = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        if(tabCounter>1) tabCounter = 0;

        if(MainActivity.isTablet){
            rootView = inflater.inflate(R.layout.fragment_common, container, false);
            MainActivity.refreshSunValues(rootView);
            MainActivity.refreshMoonValues(rootView);
        }else{
            if(tabCounter == 0){
                rootView = inflater.inflate(R.layout.fragment_sun, container, false);
                MainActivity.refreshSunValues(rootView);
            }
            else{
                rootView = inflater.inflate(R.layout.fragment_moon, container, false);
                MainActivity.refreshMoonValues(rootView);
            }
        }
        tabCounter++;

        return rootView;
    }
}
