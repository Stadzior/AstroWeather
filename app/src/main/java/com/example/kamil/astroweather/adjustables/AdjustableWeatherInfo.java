package com.example.kamil.astroweather.adjustables;

import java.util.ArrayList;
import java.util.List;

import zh.wang.android.apis.yweathergetter4a.WeatherInfo;

public class AdjustableWeatherInfo extends WeatherInfo {

    private static final int MAX_SIZE = 4;

    public void setTemperature(String temperature){
        super.setCurrentTemp(Integer.valueOf(temperature));
    }

    public void setPressure(String pressure) {
        super.setAtmospherePressure(pressure);
    }

    public void setLongitude(String longitude) {
        super.setConditionLon(longitude);
    }

    public void setLatitude(String latitude) {
        super.setConditionLat(latitude);
    }

    public void setWindSpeed(String windSpeed){
        super.setWindSpeed(windSpeed);
    }

    public void setWindDirection(String windDirection){
        super.setWindDirection(windDirection);
    }

    public void setHumidity(String humidity) {
        super.setAtmosphereHumidity(humidity);
    }

    public void setVisibility(String visibility) {
        super.setAtmosphereVisibility(visibility);
    }

    public void setCityName(String cityName) {
    super.setLocationCity(cityName);
    }

    public void setCountryName(String countryName) {
    super.setLocationCountry(countryName);
    }

    public List<AdjustableForecastInfo> forecasts;

    public AdjustableWeatherInfo(){
        forecasts = new ArrayList<>(MAX_SIZE);
    }

    public void addForecast(String day,String url,String desc){
        if(forecasts.size()<MAX_SIZE)
            forecasts.add(new AdjustableForecastInfo(day,url,desc));
    }
}
