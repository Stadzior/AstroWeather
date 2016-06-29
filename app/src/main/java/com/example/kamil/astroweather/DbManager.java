package com.example.kamil.astroweather;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbManager {
    public SQLiteDatabase database;

    public void InsertInto(String tableName,String columnName,String value){
        database.execSQL("INSERT INTO "+ tableName + " (" + columnName + ") VALUES ('" + value + "');");
    }

    public Cursor FetchColumn(String tableName, String columnName) {
        return database.rawQuery("Select "+columnName+" from "+tableName,null);
    }

    public Cursor FetchTable(String tableName) {
        return database.rawQuery("Select * from "+tableName,null);
    }
}
