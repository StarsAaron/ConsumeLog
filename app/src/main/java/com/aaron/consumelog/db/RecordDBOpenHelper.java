package com.aaron.consumelog.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by stars on 2015/7/26.
 */
public class RecordDBOpenHelper extends SQLiteOpenHelper {
    public RecordDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table Record(_id integer primary key autoincrement,r_year integer[UNSIGNED],r_month integer[UNSIGNED],r_day integer[UNSIGNED],r_date Date,r_type varchar(5),r_consume float[UNSIGNED],r_description varchar(40),r_consumeType varchar(10),r_record_date timestamp NOT NULL default CURRENT_TIMESTAMP)");
    }
    //ALLOW_INVALID_DATES

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
