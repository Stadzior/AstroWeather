package com.example.kamil.astroweather;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PlaceholderFragment extends Fragment {

    private static final String SECTION_NAME_PROPERTY = "section_name";

    public PlaceholderFragment() {
    }

    private int mPosition;

    public static PlaceholderFragment newInstance(int position) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        String sectionTitle = position == 0 ? "Today" : "Next four days";
        fragment.mPosition = position;
        args.putString(SECTION_NAME_PROPERTY, sectionTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;

        if(MainActivity.isTablet){
            rootView = inflater.inflate(R.layout.fragment_common, container, false);
            //MainActivity.refreshSunValues(rootView);
            //MainActivity.refreshMoonValues(rootView);
        }else{
            if(mPosition == 0){
                rootView = inflater.inflate(R.layout.fragment_sun, container, false);
                //MainActivity.refreshSunValues(rootView);
            }
            else{
                rootView = inflater.inflate(R.layout.fragment_moon, container, false);
                //MainActivity.refreshMoonValues(rootView);
            }
        }

        return rootView;
    }
}
