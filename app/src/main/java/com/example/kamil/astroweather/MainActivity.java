package com.example.kamil.astroweather;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

    private static YahooWeather.UNIT mUnit;
    private static String mCityName;

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.performClick();

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
                    QueryForData();
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
        currentPages = new HashMap<>();
        if(getSupportFragmentManager().getFragments() != null) {
            if (isTablet) {
                currentPages.put("commonFragment", getSupportFragmentManager().getFragment(savedInstanceState, "commonFragment"));
            } else {
                currentPages.put("sunFragment", getSupportFragmentManager().getFragment(savedInstanceState, "sunFragment"));
                currentPages.put("moonFragment", getSupportFragmentManager().getFragment(savedInstanceState, "moonFragment"));
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(weatherInfo != null) {


            RefreshTodayForecast(weatherInfo);

            RefreshNextFourDaysForecast(weatherInfo);

            if (fab != null) {
                Snackbar.make(fab, "Data has been refreshed.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
        else{
            View fragmentView = currentPages.get("sunFragment").getView();
            updateValueOnScreen(fragmentView, R.id.city, mCityName);
            if (fab != null) {
                Snackbar.make(fab, "Chosen location is invalid.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    private void RefreshNextFourDaysForecast(WeatherInfo weatherInfo) {
        View fragmentView = currentPages.get("moonFragment").getView();
        updateValueOnScreen(fragmentView, R.id.forecastDay1, weatherInfo.getForecastInfo2().getForecastDay());
        updateCurrentConditionIcon(fragmentView, R.id.forecastIcon1, weatherInfo.getForecastInfo2().getForecastConditionIconURL());
        updateValueOnScreen(fragmentView, R.id.forecastDesc1,weatherInfo.getForecastInfo2().getForecastText());

        updateValueOnScreen(fragmentView, R.id.forecastDay2, weatherInfo.getForecastInfo3().getForecastDay());
        updateCurrentConditionIcon(fragmentView, R.id.forecastIcon2, weatherInfo.getForecastInfo3().getForecastConditionIconURL());
        updateValueOnScreen(fragmentView, R.id.forecastDesc2,weatherInfo.getForecastInfo3().getForecastText());

        updateValueOnScreen(fragmentView, R.id.forecastDay3, weatherInfo.getForecastInfo4().getForecastDay());
        updateCurrentConditionIcon(fragmentView, R.id.forecastIcon3, weatherInfo.getForecastInfo4().getForecastConditionIconURL());
        updateValueOnScreen(fragmentView, R.id.forecastDesc3,weatherInfo.getForecastInfo4().getForecastText());

        updateValueOnScreen(fragmentView, R.id.forecastDay4, weatherInfo.getForecastInfo5().getForecastDay());
        updateCurrentConditionIcon(fragmentView, R.id.forecastIcon4, weatherInfo.getForecastInfo5().getForecastConditionIconURL());
        updateValueOnScreen(fragmentView, R.id.forecastDesc4, weatherInfo.getForecastInfo5().getForecastText());
    }

    private void RefreshTodayForecast(WeatherInfo weatherInfo) {
        View fragmentView = currentPages.get("sunFragment").getView();
        updateValueOnScreen(fragmentView, R.id.city, mCityName);
        char longitudeSign = weatherInfo.getConditionLon().contains("-") ? 'W' : 'E';
        updateValueOnScreen(fragmentView, R.id.longitude, String.valueOf(Math.round(Double.valueOf(weatherInfo.getConditionLon()))) + '°' + longitudeSign); //E-W
        char latitudeSign = weatherInfo.getConditionLat().contains("-") ? 'N' : 'S';
        updateValueOnScreen(fragmentView, R.id.latitude, String.valueOf(Math.round(Double.valueOf(weatherInfo.getConditionLat()))) + '°' + latitudeSign); //N-S
        int temp = weatherInfo.getCurrentTemp();
        String formattedTemp = (mUnit == YahooWeather.UNIT.FAHRENHEIT) ? String.valueOf(temp) + "°F" : String.valueOf(YahooWeather.turnFtoC(temp)) + "°C";
        updateValueOnScreen(fragmentView, R.id.temperature, formattedTemp);
        updateValueOnScreen(fragmentView, R.id.preassure, weatherInfo.getAtmospherePressure() + "hPa");
        updateValueOnScreen(fragmentView, R.id.conditionsDesc, weatherInfo.getCurrentText());
        updateValueOnScreen(fragmentView, R.id.windDirection, GetWindDirectionDescription(Integer.valueOf(weatherInfo.getWindDirection())));
        double windSpeed = Double.valueOf(weatherInfo.getWindSpeed());
        String formattedWindSpeed = (mUnit == YahooWeather.UNIT.CELSIUS) ? String.valueOf(windSpeed) + "KM/H" : String.valueOf(TurnKMtoMile(windSpeed))+ "MPH";
        updateValueOnScreen(fragmentView, R.id.windSpeed, formattedWindSpeed);
        updateValueOnScreen(fragmentView, R.id.humidity, weatherInfo.getAtmosphereHumidity() + "%");
        double visibility = Double.valueOf(weatherInfo.getAtmosphereVisibility());
        String formattedVisibility = (mUnit == YahooWeather.UNIT.CELSIUS) ? String.valueOf(visibility) + "KM" : String.valueOf(TurnKMtoMile(visibility)) + "M";
        updateValueOnScreen(fragmentView, R.id.visibility, formattedVisibility);
        updateCurrentConditionIcon(fragmentView, R.id.currentIcon, weatherInfo.getCurrentConditionIconURL());
    }

    private String GetWindDirectionDescription(int windDirection) {
        if(windDirection>0 && windDirection<90){
            return "EN";
        }else{
            if(windDirection == 90){
                return "E";
            }else{
                if(windDirection>90 && windDirection<180){
                    return "ES";
                }else{
                    if(windDirection == 180){
                        return "S";
                    }else{
                        if(windDirection>180 && windDirection<270){
                            return "WS";
                        }else{
                            if(windDirection == 270){
                                return "W";
                            }else{
                                if(windDirection>270 && windDirection<360){
                                    return "WN";
                                }else{
                                    if(windDirection == 360){
                                        return "N";
                                    }else{
                                        return "No wind";
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private double TurnKMtoMile(double km) {
        return Math.round(km*0.621371192);
    }

    private double TurnMileToKM(double mile){
        return Math.round(mile*1.609344);
    }

    private void updateCurrentConditionIcon(View fragmentView, int controlId,String iconURL) {
        ImageView imageView = (ImageView) (fragmentView != null ? fragmentView.findViewById(controlId) : null);
        URL url = null;
        try {
            url = new URL(iconURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Bitmap currentConditionImage = null;
        try {
            currentConditionImage = new DownloadCurrentConditionIconAsync().execute(url).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (imageView != null) {
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


    public void QueryForData(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if(isNetworkAvailable()) {
            if (fab != null) {
                Snackbar.make(fab, "Refreshing... please wait...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            YahooWeather yahooWeather = new YahooWeather();
            mUnit = getIntent().getBooleanExtra("Units",true) ? YahooWeather.UNIT.CELSIUS : YahooWeather.UNIT.FAHRENHEIT;
            if(mUnit == null)
                mUnit = YahooWeather.UNIT.CELSIUS;
            mCityName = getIntent().getStringExtra("City");
            if(mCityName == null)
                mCityName = "London";
            yahooWeather.queryYahooWeatherByPlaceName(getApplicationContext(), PolishSignsResolver.removePolishSignsFromText(mCityName), this);
        }
        else{
            if (fab != null) {
                Snackbar.make(fab, "There is no internet connection data can be deprecated.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
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
