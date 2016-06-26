package com.example.kamil.astroweather;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.astrocalculator.AstroDateTime;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import zh.wang.android.apis.yweathergetter4a.WeatherInfo;
import zh.wang.android.apis.yweathergetter4a.YahooWeather;
import zh.wang.android.apis.yweathergetter4a.YahooWeatherInfoListener;


public class MainActivity extends AppCompatActivity implements YahooWeatherInfoListener {

    private static SectionsPagerAdapter mSectionsPagerAdapter;
    public static HashMap<String,Fragment> currentPages;

    private static TextView clock;

    public static boolean isTablet;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        isTablet = determineIfIsTablet();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        RestoreFragmentsStates(savedInstanceState);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        SetUpViewPagerWithAdapter();

        SetUpRefreshButton();

        YahooWeather yahooWeather = new YahooWeather();
        yahooWeather.queryYahooWeatherByPlaceName(getApplicationContext(), "Lodz", this);

        ClockThreadStart();
    }


    private void ClockThreadStart() {
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

    private void SetUpRefreshButton() {
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
    }

    private void SetUpViewPagerWithAdapter() {
        /*
      The {@link ViewPager} that will host the section contents.
     */
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        if (mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(mViewPager);
        }
    }

    private void RestoreFragmentsStates(Bundle savedInstanceState) {
        currentPages =  new HashMap<String, Fragment>();
        if(getSupportFragmentManager().getFragments() != null) {
            if (isTablet) {
                currentPages.put("commonFragment", getSupportFragmentManager().getFragment(savedInstanceState, "commonFragment"));
            } else {
                currentPages.put("sunFragment", getSupportFragmentManager().getFragment(savedInstanceState, "sunFragment"));
                currentPages.put("moonFragment", getSupportFragmentManager().getFragment(savedInstanceState, "moonFragment"));
            }
        }
    }

    private boolean determineIfIsTablet() {
        int sizeOfScreen = (getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK);
        return sizeOfScreen ==
                Configuration.SCREENLAYOUT_SIZE_XLARGE || sizeOfScreen ==
                Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private static void updateValueOnScreen(View fragmentView,int textViewId,String value){
        TextView controlView = (TextView) fragmentView.findViewById(textViewId);
        controlView.setText(value);
    }

    @Override
    public void gotWeatherInfo(WeatherInfo weatherInfo) {
        if(weatherInfo != null) {
            View fragmentView = currentPages.get("sunFragment").getView();
            ImageView imageView = (ImageView) (fragmentView != null ? fragmentView.findViewById(R.id.sunriseIcon) : null);

            URL url = null;
            try {
                url = new URL(weatherInfo.getCurrentConditionIconURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Bitmap currentConditionImage = null;
            try {
                currentConditionImage = new DownloadCurrentConditionIconAsync().execute(url).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            imageView.setImageBitmap(currentConditionImage);
        }
    }

    private class DownloadCurrentConditionIconAsync extends AsyncTask<URL, WeatherInfo, Bitmap> {
        protected Bitmap doInBackground(URL... urls) {
            if(urls == null || urls.length > 1){
                throw new IllegalArgumentException("Something went wrong with downloading current condition icon.");
            }
            try {
                return BitmapFactory.decodeStream(urls[0].openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
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
//            fragmentView = currentPages.get("commonFragment").getView();
//            refreshSunValues(fragmentView);
//            refreshMoonValues(fragmentView);
        }
        else {
//            fragmentView = currentPages.get("sunFragment").getView();
//            refreshSunValues(fragmentView);
//            fragmentView = currentPages.get("moonFragment").getView();
//            refreshMoonValues(fragmentView);
        }
    }

//    public static void refreshSunValues(View fragmentView){
//        AstroCalculator.SunInfo sunInfo = calculator.getSunInfo();
//
//        updateValueOnScreen(fragmentView,R.id.sunriseValue,formatValue(sunInfo.getSunrise(),ValueType.TIME));
//        updateValueOnScreen(fragmentView,R.id.sunriseAzimuth, formatValue(sunInfo.getAzimuthRise(), ValueType.NUMBER));
//        updateValueOnScreen(fragmentView,R.id.sunsetValue,formatValue(sunInfo.getSunset(), ValueType.TIME));
//        updateValueOnScreen(fragmentView,R.id.sunsetAzimuth, formatValue(sunInfo.getAzimuthSet(), ValueType.NUMBER));
//        updateValueOnScreen(fragmentView,R.id.dawnValue,formatValue(sunInfo.getTwilightMorning(), ValueType.TIME));
//        updateValueOnScreen(fragmentView,R.id.twilightValue,formatValue(sunInfo.getTwilightEvening(), ValueType.TIME));
//
//    }
//
//    public static void refreshMoonValues(View fragmentView){
//        AstroCalculator.MoonInfo moonInfo = calculator.getMoonInfo();
//
//        updateValueOnScreen(fragmentView,R.id.moonriseValue,formatValue(moonInfo.getMoonrise(),ValueType.TIME));
//        updateValueOnScreen(fragmentView,R.id.moonsetValue,formatValue(moonInfo.getMoonset(),ValueType.TIME));
//        updateValueOnScreen(fragmentView,R.id.fullmoonValue,formatValue(moonInfo.getNextFullMoon(),ValueType.DATE));
//        updateValueOnScreen(fragmentView,R.id.newmoonValue,formatValue(moonInfo.getNextNewMoon(),ValueType.DATE));
//        updateValueOnScreen(fragmentView,R.id.moonPhaseValue,formatValue(moonInfo.getIllumination(),ValueType.PERCENT));
//        updateValueOnScreen(fragmentView,R.id.synodicDayValue,formatValue(moonInfo.getAge(),ValueType.NUMBER));
//
//    }

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
