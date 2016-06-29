package com.example.kamil.astroweather;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DbManager {
    public SQLiteDatabase database;

    public void InsertInto(String tableName,String columnName,String value){
        database.execSQL("INSERT INTO " + tableName + " (" + columnName + ") VALUES ('" + value + "');");
    }

    public void InsertInto(String tableName,String[] columns,ArrayList<String> values){
        StringBuilder query = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        for(String columnName : columns){
            query.append(columnName).append(",");
        }

        query.deleteCharAt(query.length()-1).append(") VALUES (");

        for(String value : values){
            query.append("'").append(value).append("',");
        }

        query.deleteCharAt(query.length()-1).append(");");

        database.execSQL(query.toString());
    }

    public Cursor FetchColumn(String tableName, String columnName) {
        return database.rawQuery("Select "+columnName+" from "+tableName,null);
    }

    public Cursor FetchTable(String tableName) {
        return database.rawQuery("Select * from "+tableName,null);
    }

    public String[] FetchColumnNames(String tableName) {
        Cursor resultSet = FetchTable(tableName);
        return resultSet.getColumnNames();
    }
}
