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
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

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

    public static boolean isTablet;

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
        getFixedCoordinates(intent);

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

    private void getFixedCoordinates(Intent intent) {
        longitude = intent.getDoubleExtra("Longitude", 0.0);
        latitude = intent.getDoubleExtra("Latitude", 0.0);
        longitude = longitude < 0.0 ? 0.0 : longitude;
        latitude = latitude < 0.0 ? 0.0 : latitude;
        longitude = longitude > 180 ? 180.0 : longitude;
        latitude = latitude > 90 ? 90.0 : latitude;
    }

    public AstroCalculator.Location buildAstroLocation(double longitude,double latitude,boolean isEastern,boolean isNorthern){
        double fixedLongitude = isEastern ? longitude : -longitude;
        double fixedLatitude = isNorthern ? latitude : -latitude;
        return new AstroCalculator.Location(fixedLatitude,fixedLongitude);
    }

    public AstroDateTime buildAstroDate(Date now){
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int timeZoneOffset = cal.getTimeZone().getRawOffset()/1000/60/60;
        boolean isDaylightSaving = cal.getTimeZone().inDaylightTime(now);
        return new AstroDateTime(year,month,dayOfMonth,now.getHours(),now.getMinutes(),now.getSeconds(),timeZoneOffset,isDaylightSaving);
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

    public static void refreshSunValues(View fragmentView){
        AstroCalculator.SunInfo sunInfo = calculator.getSunInfo();

        updateValueOnScreen(fragmentView,R.id.sunriseValue,formatValue(sunInfo.getSunrise(),ValueType.TIME));
        updateValueOnScreen(fragmentView,R.id.sunriseAzimuth, formatValue(sunInfo.getAzimuthRise(), ValueType.NUMBER));
        updateValueOnScreen(fragmentView,R.id.sunsetValue,formatValue(sunInfo.getSunset(), ValueType.TIME));
        updateValueOnScreen(fragmentView,R.id.sunsetAzimuth, formatValue(sunInfo.getAzimuthSet(), ValueType.NUMBER));
        updateValueOnScreen(fragmentView,R.id.dawnValue,formatValue(sunInfo.getTwilightMorning(), ValueType.TIME));
        updateValueOnScreen(fragmentView,R.id.twilightValue,formatValue(sunInfo.getTwilightEvening(), ValueType.TIME));

    }

    public static void refreshMoonValues(View fragmentView){
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

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(myIntent);
            finish();
        }

        return super.onOptionsItemSelected(item);
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
