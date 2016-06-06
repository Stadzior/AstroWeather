package com.example.kamil.astroweather;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        String key;
        if(MainActivity.isTablet){
            key = "commonFragment";
        }else{
            if(position == 0){
                key = "sunFragment";
            }
            else{
                key = "moonFragment";
            }
        }
        boolean alreadyHasASavedState = MainActivity.currentPages.containsKey(key);
        Fragment fragment;
        if(alreadyHasASavedState){
            fragment = MainActivity.currentPages.get(key);
        }
        else {
            fragment = PlaceholderFragment.newInstance(position + 1);
            MainActivity.currentPages.put(key,fragment);
        }
        return fragment;
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return MainActivity.isTablet ? 1 : 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(MainActivity.isTablet){
            return "All";
        }else{
            switch (position){
                case 0:
                    return "Sun";
                case 1:
                    return "Moon";
            }
        }
        return null;
    }
}
