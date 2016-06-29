package com.example.kamil.astroweather;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.kamil.astroweather.adjustables.AdjustableWeatherInfo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import zh.wang.android.apis.yweathergetter4a.WeatherInfo;
import zh.wang.android.apis.yweathergetter4a.YahooWeather;
import zh.wang.android.apis.yweathergetter4a.YahooWeatherInfoListener;


public class MainActivity extends AppCompatActivity implements YahooWeatherInfoListener {

    private static SectionsPagerAdapter mSectionsPagerAdapter;
    private static ViewPager mViewPager;
    public static HashMap<String,Fragment> currentPages;
    private static TextView clock;
    public static boolean isTablet;
    private static YahooWeather.UNIT mUnit;
    private static String mCityName;
    private static DbManager dbManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isTablet = determineIfIsTablet();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);


        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        SetUpViewPagerWithAdapter();

        RestoreFragmentsStates(savedInstanceState);

        SetUpRefreshButton();

        SetUpDatabase("AstroWeather");

        SetUpConstants();

        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        SetUpSpinnerItems(spinner);
        AttachSpinnerOnItemSelectedListener(spinner);

        ClockThreadStart();

    }

  private AdjustableWeatherInfo GetStoredWeatherInfo() {
        Cursor resultSet = dbManager.FetchTable("WeatherInfo");
        AdjustableWeatherInfo weatherInfo = null;
        if(resultSet != null && resultSet.getCount()>0){
        weatherInfo = new AdjustableWeatherInfo();
        resultSet.moveToLast();

        weatherInfo.setCityName(getStringCellValue(resultSet, "CityName"));
        weatherInfo.setCountryName(getStringCellValue(resultSet, "CountryName"));
        weatherInfo.setCurrentConditionIconURL(getStringCellValue(resultSet, "CurrentConditionIconURL"));
        weatherInfo.setCurrentText(getStringCellValue(resultSet, "CurrentConditionText"));
        weatherInfo.setTemperature(getStringCellValue(resultSet, "Temperature"));
        weatherInfo.setPressure(getStringCellValue(resultSet, "Pressure"));
        weatherInfo.setWindSpeed(getStringCellValue(resultSet, "WindSpeed"));
        weatherInfo.setWindDirection(getStringCellValue(resultSet, "WindDirection"));
        weatherInfo.setLongitude(getStringCellValue(resultSet, "Longitude"));
        weatherInfo.setLatitude(getStringCellValue(resultSet, "Latitude"));
        weatherInfo.setHumidity(resultSet.getString(resultSet.getColumnIndex("Humidity")));
        weatherInfo.setVisibility(resultSet.getString(resultSet.getColumnIndex("Visibility")));
        weatherInfo.addForecast(
                getStringCellValue(resultSet, "ForecastDay2")
                , getStringCellValue(resultSet, "ForecastIconURL2")
                , getStringCellValue(resultSet, "ForecastDesc2"));
        weatherInfo.addForecast(
                getStringCellValue(resultSet,"ForecastDay3")
                , getStringCellValue(resultSet, "ForecastIconURL3")
                , getStringCellValue(resultSet, "ForecastDesc3"));
        weatherInfo.addForecast(
                getStringCellValue(resultSet,"ForecastDay4")
                , getStringCellValue(resultSet, "ForecastIconURL4")
                , getStringCellValue(resultSet, "ForecastDesc4"));
        weatherInfo.addForecast(
                getStringCellValue(resultSet,"ForecastDay5")
                , getStringCellValue(resultSet, "ForecastIconURL5")
                , getStringCellValue(resultSet, "ForecastDesc5"));
        weatherInfo.setExpirationDate(System.currentTimeMillis()+ TimeUnit.MINUTES.toMillis(15));
        }
        return weatherInfo;
    }

    private String getStringCellValue(Cursor resultSet,String columnName){
        return resultSet.getString(resultSet.getColumnIndex(columnName));
    }

    private void SetUpSpinnerItems(Spinner spinner) {
        List<String> locations = new ArrayList<>();

        Cursor resultSet = dbManager.FetchColumn("Location", "Name");
        if(resultSet.getCount()>0) {
            String locationName;
            resultSet.moveToLast();
            do {
                locationName = resultSet.getString(0);
                if(!locations.contains(locationName)) {
                    locations.add(resultSet.getString(0));
                }
                resultSet.moveToPrevious();
            } while(!resultSet.isBeforeFirst());
            resultSet.close();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, locations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if (spinner != null) {
            spinner.setAdapter(adapter);
        }
    }

    private void SetUpDatabase(String dbName) {
        dbManager = new DbManager();
        dbManager.database = openOrCreateDatabase(dbName, MODE_PRIVATE, null);
        //dbManager.database.execSQL("DROP TABLE WeatherInfo");
        dbManager.database.execSQL("CREATE TABLE IF NOT EXISTS Location (Name VARCHAR)");
        StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS WeatherInfo (");
        builder.append("CityName VARCHAR,");
        builder.append("CountryName VARCHAR,");
        builder.append("CurrentConditionText VARCHAR,");
        builder.append("CurrentConditionIconURL VARCHAR,");
        builder.append("Longitude VARCHAR,");
        builder.append("Latitude VARCHAR,");
        builder.append("Temperature VARCHAR,");
        builder.append("Pressure VARCHAR,");
        builder.append("WindSpeed VARCHAR,");
        builder.append("WindDirection VARCHAR,");
        builder.append("Humidity VARCHAR,");
        builder.append("Visibility VARCHAR,");
        builder.append("ForecastDay2 VARCHAR,");
        builder.append("ForecastIconURL2 VARCHAR,");
        builder.append("ForecastDesc2 VARCHAR,");
        builder.append("ForecastDay3 VARCHAR,");
        builder.append("ForecastIconURL3 VARCHAR,");
        builder.append("ForecastDesc3 VARCHAR,");
        builder.append("ForecastDay4 VARCHAR,");
        builder.append("ForecastIconURL4 VARCHAR,");
        builder.append("ForecastDesc4 VARCHAR,");
        builder.append("ForecastDay5 VARCHAR,");
        builder.append("ForecastIconURL5 VARCHAR,");
        builder.append("ForecastDesc5 VARCHAR,");
        builder.append("ExpirationDate VARCHAR");
        builder.append(");");
        dbManager.database.execSQL(builder.toString());
    }

    private void SetUpConstants() {
        mUnit = getIntent().getBooleanExtra("Units",true) ? YahooWeather.UNIT.CELSIUS : YahooWeather.UNIT.FAHRENHEIT;
        if(mUnit == null)
            mUnit = YahooWeather.UNIT.CELSIUS;
        mCityName = getIntent().getStringExtra("City");
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        if(mCityName == null) {
            if(spinner != null && spinner.getSelectedItem() != null){
                mCityName = spinner.getSelectedItem().toString();
            }
            else{
                mCityName = "";
            }
        }
        else{
            if(!mCityName.isEmpty())
                dbManager.InsertInto("Location", "Name", mCityName);
        }
    }

    private void AttachSpinnerOnItemSelectedListener(final Spinner spinner) {
        if (spinner != null) {
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    mCityName = spinner.getSelectedItem().toString();
                    AdjustableWeatherInfo data = GetStoredWeatherInfo();
                    if(data == null || data.getLocationCity().compareTo(mCityName)!=0 || data.IsExpired()){
                        QueryForData();
                    }else{
                        RefreshData(data);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });
        }
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

        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(thread, e);
            }
        });
    }

    private void handleUncaughtException(Thread thread, Throwable e) {
        e.printStackTrace();
    }

    private void SetUpRefreshButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AdjustableWeatherInfo data = GetStoredWeatherInfo();
                    if(data == null || data.getLocationCity().compareTo(mCityName)!=0 || data.IsExpired()){
                        QueryForData();
                    }else{
                        RefreshData(data);
                    }
                }
            });
        }
    }

    private void SetUpViewPagerWithAdapter() {
        /*
      The {@link ViewPager} that will host the section contents.
     */
        mViewPager = (ViewPager) findViewById(R.id.container);
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
            RefreshData(weatherInfo);
            StoreDataInDatabase(weatherInfo);
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

    private void StoreDataInDatabase(WeatherInfo weatherInfo) {
        String[] columns = dbManager.FetchColumnNames("WeatherInfo");
        ArrayList<String> values = new ArrayList<>();
        values.add(mCityName);
        values.add(weatherInfo.getLocationCountry());
        values.add(weatherInfo.getCurrentText());
        values.add(weatherInfo.getCurrentConditionIconURL());
        values.add(weatherInfo.getConditionLon());
        values.add(weatherInfo.getConditionLat());
        values.add(String.valueOf(weatherInfo.getCurrentTemp()));
        values.add(weatherInfo.getAtmospherePressure());
        values.add(weatherInfo.getWindSpeed());
        values.add(weatherInfo.getWindDirection());
        values.add(weatherInfo.getAtmosphereHumidity());
        values.add(weatherInfo.getAtmosphereVisibility());
        values.add(weatherInfo.getForecastInfo2().getForecastDay());
        values.add(weatherInfo.getForecastInfo2().getForecastConditionIconURL());
        values.add(weatherInfo.getForecastInfo2().getForecastText());
        values.add(weatherInfo.getForecastInfo3().getForecastDay());
        values.add(weatherInfo.getForecastInfo3().getForecastConditionIconURL());
        values.add(weatherInfo.getForecastInfo3().getForecastText());
        values.add(weatherInfo.getForecastInfo4().getForecastDay());
        values.add(weatherInfo.getForecastInfo4().getForecastConditionIconURL());
        values.add(weatherInfo.getForecastInfo4().getForecastText());
        values.add(weatherInfo.getForecastInfo5().getForecastDay());
        values.add(weatherInfo.getForecastInfo5().getForecastConditionIconURL());
        values.add(weatherInfo.getForecastInfo5().getForecastText());
        values.add(String.valueOf(System.currentTimeMillis()));
        dbManager.InsertInto("WeatherInfo", columns, values);
    }

    private void RefreshData(WeatherInfo weatherInfo) {
        if(weatherInfo!=null) {
            RefreshTodayForecast(weatherInfo);
            RefreshNextFourDaysForecast(weatherInfo);
        }
    }

    private void RefreshData(AdjustableWeatherInfo weatherInfo) {
        if(weatherInfo!=null) {
            RefreshTodayForecast(weatherInfo);
            RefreshNextFourDaysForecast(weatherInfo);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            if (fab != null) {
                Snackbar.make(fab, "Restored data from memory.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    private void RefreshNextFourDaysForecast(WeatherInfo weatherInfo) {
        String fragmentName = isTablet ? "sunFragment" : "moonFragment";
        View fragmentView = currentPages.get(fragmentName).getView();
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

    private void RefreshNextFourDaysForecast(AdjustableWeatherInfo weatherInfo) {
        String fragmentName = isTablet ? "sunFragment" : "moonFragment";
        View fragmentView = currentPages.get(fragmentName).getView();
        updateValueOnScreen(fragmentView, R.id.forecastDay1, weatherInfo.forecasts.get(0).getForecastDay());
        updateCurrentConditionIcon(fragmentView, R.id.forecastIcon1, weatherInfo.forecasts.get(0).getForecastConditionIconURL());
        updateValueOnScreen(fragmentView, R.id.forecastDesc1,weatherInfo.forecasts.get(0).getForecastText());

        updateValueOnScreen(fragmentView, R.id.forecastDay2, weatherInfo.forecasts.get(1).getForecastDay());
        updateCurrentConditionIcon(fragmentView, R.id.forecastIcon2, weatherInfo.forecasts.get(1).getForecastConditionIconURL());
        updateValueOnScreen(fragmentView, R.id.forecastDesc2,weatherInfo.forecasts.get(1).getForecastText());

        updateValueOnScreen(fragmentView, R.id.forecastDay3, weatherInfo.forecasts.get(2).getForecastDay());
        updateCurrentConditionIcon(fragmentView, R.id.forecastIcon3, weatherInfo.forecasts.get(2).getForecastConditionIconURL());
        updateValueOnScreen(fragmentView, R.id.forecastDesc3,weatherInfo.forecasts.get(2).getForecastText());

        updateValueOnScreen(fragmentView, R.id.forecastDay4, weatherInfo.forecasts.get(3).getForecastDay());
        updateCurrentConditionIcon(fragmentView, R.id.forecastIcon4, weatherInfo.forecasts.get(3).getForecastConditionIconURL());
        updateValueOnScreen(fragmentView, R.id.forecastDesc4, weatherInfo.forecasts.get(3).getForecastText());
    }

    private void RefreshTodayForecast(WeatherInfo weatherInfo) {
        View fragmentView = currentPages.get("sunFragment").getView();

        updateCurrentConditionIcon(fragmentView, R.id.currentIcon, weatherInfo.getCurrentConditionIconURL());
        updateValueOnScreen(fragmentView, R.id.conditionsDesc, weatherInfo.getCurrentText().replace(" ","\n"));

        String cityAndCountry = mCityName.contains(" ") ? mCityName.substring(0,mCityName.indexOf(' ')) : mCityName;
        cityAndCountry += ",\n"+ weatherInfo.getLocationCountry();
        updateValueOnScreen(fragmentView, R.id.city,cityAndCountry);

        char longitudeSign = weatherInfo.getConditionLon().contains("-") ? 'W' : 'E';
        String fixedLongitude = String.valueOf(Math.round(Double.valueOf(weatherInfo.getConditionLon()))) + '°' + longitudeSign;

        char latitudeSign = weatherInfo.getConditionLat().contains("-") ? 'N' : 'S';
        String fixedLatitude = String.valueOf(Math.round(Double.valueOf(weatherInfo.getConditionLat()))) + '°' + latitudeSign;

        updateValueOnScreen(fragmentView, R.id.coordinates,fixedLongitude + " " + fixedLatitude);

        int temp = weatherInfo.getCurrentTemp();
        String formattedTemp = (mUnit == YahooWeather.UNIT.FAHRENHEIT) ? String.valueOf(temp) + "°F" : String.valueOf(YahooWeather.turnFtoC(temp)) + "°C";

        updateValueOnScreen(fragmentView, R.id.temperature, formattedTemp);
        updateValueOnScreen(fragmentView, R.id.preassure, weatherInfo.getAtmospherePressure() + "hPa");

        double windSpeed = Double.valueOf(weatherInfo.getWindSpeed());
        String formattedWindSpeed = (mUnit == YahooWeather.UNIT.CELSIUS) ? String.valueOf(windSpeed) + "KM/H" : String.valueOf(TurnKMtoMile(windSpeed))+ "MPH";
        String formattedWind = formattedWindSpeed + " " + GetWindDirectionDescription(Integer.valueOf(weatherInfo.getWindDirection()));

        updateValueOnScreen(fragmentView, R.id.wind, formattedWind);

        updateValueOnScreen(fragmentView, R.id.humidity, weatherInfo.getAtmosphereHumidity() + "%");

        double visibility = Double.valueOf(weatherInfo.getAtmosphereVisibility());
        String formattedVisibility = (mUnit == YahooWeather.UNIT.CELSIUS) ? String.valueOf(visibility) + "KM" : String.valueOf(TurnKMtoMile(visibility)) + "M";

        updateValueOnScreen(fragmentView, R.id.visibility, formattedVisibility);
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
                if(urls[0] != null)
                {
                    return BitmapFactory.decodeStream(urls[0].openConnection().getInputStream());
                }
                else{
                    return null;
                }
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
            yahooWeather.queryYahooWeatherByPlaceName(getApplicationContext(), PolishSignsResolver.removePolishSignsFromText(mCityName), this);
        }
        else{
            if (fab != null) {
                Snackbar.make(fab, "There is no internet connection. Data can be deprecated.", Snackbar.LENGTH_LONG)
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
        }else{
            if (id == R.id.action_about) {
                Intent myIntent = new Intent(MainActivity.this, AboutActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
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
