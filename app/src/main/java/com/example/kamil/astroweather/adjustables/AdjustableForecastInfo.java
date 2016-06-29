package com.example.kamil.astroweather.adjustables;

/**
 * Created by Kamil on 2016-06-29.
 */
public class AdjustableForecastInfo {
    private String ForecastDay;
    private String ForecastConditionIconURL;
    private String ForecastText;

    public AdjustableForecastInfo(String day,String url,String desc){
        setForecastDay(day);
        setForecastConditionIconURL(url);
        setForecastText(desc);
    }

    public String getForecastDay() {
        return ForecastDay;
    }

    public void setForecastDay(String forecastDay) {
        ForecastDay = forecastDay;
    }

    public String getForecastConditionIconURL() {
        return ForecastConditionIconURL;
    }

    public void setForecastConditionIconURL(String forecastConditionIconURL) {
        ForecastConditionIconURL = forecastConditionIconURL;
    }

    public String getForecastText() {
        return ForecastText;
    }

    public void setForecastText(String forecastText) {
        ForecastText = forecastText;
    }
}
