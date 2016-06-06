package com.example.kamil.astroweather;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.astrocalculator.AstroCalculator;
import com.astrocalculator.AstroDateTime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private static SectionsPagerAdapter mSectionsPagerAdapter;
    public static HashMap<String,Fragment> currentPages;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private static TextView clock;

    private static TextView longitudeTextView;
    private static TextView latitudeTextView;

    private static boolean isTablet;

    private static double longitude;
    private static double latitude;

    private static char longitudeDirection;
    private static char latitudeDirection;

    private static int syncIntervalInMinutes;
    private static AstroCalculator calculator;
    private static AstroCalculator.Location location;
    private static AstroDateTime dateTime;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        int sizeOfScreen = (getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK);
        isTablet = sizeOfScreen ==
                Configuration.SCREENLAYOUT_SIZE_XLARGE || sizeOfScreen ==
                Configuration.SCREENLAYOUT_SIZE_LARGE;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        Restore the fragments instances

        currentPages =  new HashMap<String, Fragment>();
        if(getSupportFragmentManager().getFragments() != null) {
            if (isTablet) {
                currentPages.put("commonFragment", getSupportFragmentManager().getFragment(savedInstanceState, "commonFragment"));
            } else {
                currentPages.put("sunFragment", getSupportFragmentManager().getFragment(savedInstanceState, "sunFragment"));
                currentPages.put("moonFragment", getSupportFragmentManager().getFragment(savedInstanceState, "moonFragment"));
            }
        }

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        if (mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(mViewPager);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    refreshData();
                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                    if (fab != null) {
                        Snackbar.make(fab, "Data has been refreshed.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
            });
        }

        //Filling in settings in bottom text view
        Intent intent = getIntent();
        longitude = intent.getDoubleExtra("Longitude", 0.0);
        latitude = intent.getDoubleExtra("Latitude", 0.0);

        String directions = getIntent().getStringExtra("Directions");

        if (directions == null) directions = "E,N";

        longitudeDirection = directions.charAt(0);
        latitudeDirection = directions.charAt(2);
        dateTime = buildAstroDate(new Date(System.currentTimeMillis()));
        location = buildAstroLocation(longitude, latitude, longitudeDirection == 'E', latitudeDirection == 'N');
        calculator = new AstroCalculator(dateTime,location);
        longitudeTextView = (TextView) findViewById(R.id.longitude);
        latitudeTextView = (TextView) findViewById(R.id.latitude);

        longitudeTextView.setText(new StringBuilder().append(longitude).append("°").append(longitudeDirection).toString());
        latitudeTextView.setText(new StringBuilder().append(latitude).append("°").append(latitudeDirection).toString());

        syncIntervalInMinutes = intent.getIntExtra("syncIntervalInMinutes",15);

        if(syncIntervalInMinutes>0) {   // -1 -> NEVER
            //Thread for refreshing data
            Thread refreshThread = new Thread() {

                @Override
                public void run() {
                    try {
                        while (!isInterrupted()) {
                            Thread.sleep(syncIntervalInMinutes * 60 * 1000); // * 60(minutes to seconds) * 1000 (seconds to milliseconds)
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refreshData();
                                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                                    if (fab != null) {
                                        Snackbar.make(fab, "Data has been refreshed.", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                    }
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                    }
                }
            };

            refreshThread.start();
        }
        clock = (TextView) findViewById(R.id.current_time);

        //Thread for a clock
        Thread clockThread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SimpleDateFormat dataFormat = new SimpleDateFormat("HH:mm:ss");
                                clock.setText(dataFormat.format(new Date(System.currentTimeMillis())));
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        clockThread.start();
    }

    public AstroCalculator.Location buildAstroLocation(double longitude,double latitude,boolean isEastern,boolean isNorthern){
        double fixedLongitude = isEastern ? longitude : -longitude;
        double fixedLatitude = isNorthern ? latitude : -latitude;
        return new AstroCalculator.Location(fixedLongitude,fixedLatitude);
    }

    public AstroDateTime buildAstroDate(Date now){
        boolean isDaylightSaving = (now.getMonth()>2 && now.getDay()>27) && (now.getMonth()<10 && now.getDay()<30);
        return new AstroDateTime(now.getYear(),now.getMonth(),now.getDay(),now.getHours(),now.getMinutes(),now.getSeconds(),1,isDaylightSaving);
    }

    private static void updateValueOnScreen(View fragmentView,int textViewId,String value){
        TextView controlView = (TextView) fragmentView.findViewById(textViewId);
        controlView.setText(value);
    }

    public enum ValueType{
        NUMBER,DATE,TIME,PERCENT
    }
    private static String formatValue(Object value,ValueType type){
        switch(type){
            case NUMBER:
            {
                long longValue = Math.round((double)value);
                return String.valueOf(longValue);
            }
            case DATE:
            {
                AstroDateTime date = (AstroDateTime) value;
                return date.getDay() + "-" + date.getMonth() + "-" + date.getYear();
            }
            case TIME:
            {
                AstroDateTime date = (AstroDateTime) value;
                StringBuilder minutes = new StringBuilder().append(String.valueOf(date.getMinute()));
                if (minutes.length() == 1) {
                    minutes.append("0");
                    minutes.reverse();
                }
                return date.getHour() + ":" + minutes.toString();
            }
            case PERCENT:
            {
                return value.toString().substring(0,6) + "%";
            }
            default:
                return value.toString();
        }
    }

    public static void refreshData(){
        View fragmentView;
        if(isTablet){
            fragmentView = currentPages.get("commonFragment").getView();
            refreshSunValues(fragmentView);
            refreshMoonValues(fragmentView);
        }
        else {
            fragmentView = currentPages.get("sunFragment").getView();
            refreshSunValues(fragmentView);
            fragmentView = currentPages.get("moonFragment").getView();
            refreshMoonValues(fragmentView);
        }
    }

    private static void refreshSunValues(View fragmentView){
        AstroCalculator.SunInfo sunInfo = calculator.getSunInfo();

        updateValueOnScreen(fragmentView,R.id.sunriseValue,formatValue(sunInfo.getSunrise(),ValueType.TIME));
        updateValueOnScreen(fragmentView,R.id.sunriseAzimuth, formatValue(sunInfo.getAzimuthRise(), ValueType.NUMBER));
        updateValueOnScreen(fragmentView,R.id.sunsetValue,formatValue(sunInfo.getSunset(), ValueType.TIME));
        updateValueOnScreen(fragmentView,R.id.sunsetAzimuth, formatValue(sunInfo.getAzimuthSet(), ValueType.NUMBER));
        updateValueOnScreen(fragmentView,R.id.dawnValue,formatValue(sunInfo.getTwilightMorning(), ValueType.TIME));
        updateValueOnScreen(fragmentView,R.id.twilightValue,formatValue(sunInfo.getTwilightEvening(), ValueType.TIME));

    }

    private static void refreshMoonValues(View fragmentView){
        AstroCalculator.MoonInfo moonInfo = calculator.getMoonInfo();

        updateValueOnScreen(fragmentView,R.id.moonriseValue,formatValue(moonInfo.getMoonrise(),ValueType.TIME));
        updateValueOnScreen(fragmentView,R.id.moonsetValue,formatValue(moonInfo.getMoonset(),ValueType.TIME));
        updateValueOnScreen(fragmentView,R.id.fullmoonValue,formatValue(moonInfo.getNextFullMoon(),ValueType.DATE));
        updateValueOnScreen(fragmentView,R.id.newmoonValue,formatValue(moonInfo.getNextNewMoon(),ValueType.DATE));
        updateValueOnScreen(fragmentView,R.id.moonPhaseValue,formatValue(moonInfo.getIllumination(),ValueType.PERCENT));
        updateValueOnScreen(fragmentView,R.id.synodicDayValue,formatValue(moonInfo.getAge(),ValueType.NUMBER));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(myIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    private static final String SECTION_NAME_PROPERTY = "section_name";

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        public PlaceholderFragment() {
        }

        private static String sectionTitle;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
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
            if(isTablet){
                rootView = inflater.inflate(R.layout.fragment_common, container, false);
            }else{
                if(tabCounter == 0){
                    rootView = inflater.inflate(R.layout.fragment_sun, container, false);
                }
                else{
                    rootView = inflater.inflate(R.layout.fragment_moon, container, false);
                }
            }
            tabCounter++;
            return rootView;
        }
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (savedInstanceState != null) {
                //Restore the fragment's state here

            }
        }
        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);

            //Save the fragment's state here
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            String key;
            if(isTablet){
                key = "commonFragment";
            }else{
                if(position == 0){
                    key = "sunFragment";
                }
                else{
                    key = "moonFragment";
                }
            }
            boolean alreadyHasASavedState = currentPages.containsKey(key);
            Fragment fragment;
            if(alreadyHasASavedState){
                fragment = currentPages.get(key);
            }
            else {
                fragment = PlaceholderFragment.newInstance(position + 1);
                currentPages.put(key,fragment);
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return isTablet ? 1 : 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(isTablet){
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
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragments instances
        if(isTablet){
            getSupportFragmentManager().putFragment(outState, "commonFragment", currentPages.get("commonFragment"));
        }
        else{
            getSupportFragmentManager().putFragment(outState, "sunFragment", currentPages.get("sunFragment"));
            getSupportFragmentManager().putFragment(outState, "moonFragment", currentPages.get("moonFragment"));
        }

    }
}
