package com.example.kamil.astroweather;

/**
 * Created by Kamil on 2016-06-27.
 */
public class PolishSignsResolver {

    public static String removePolishSignsFromText(String text){
        return text
                .replace("ą","a")
                .replace("Ą","A")
                .replace("ć","c")
                .replace("Ć","C")
                .replace("Ę","E")
                .replace("ę","e")
                .replace("Ł","L")
                .replace("ł","l")
                .replace("Ń","N")
                .replace("ń","n")
                .replace("Ó","O")
                .replace("ó","o")
                .replace("Ś","S")
                .replace("ś","s")
                .replace("Ź","Z")
                .replace("ź","z")
                .replace("Ż","Z")
                .replace("ż","z");
    }
}
