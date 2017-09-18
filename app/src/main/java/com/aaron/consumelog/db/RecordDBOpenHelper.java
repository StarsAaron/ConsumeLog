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
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if(!db.isReadOnly()) { // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists Record (" +
                "_id integer primary key autoincrement," +
                "r_year integer[UNSIGNED] NOT NULL," +
                "r_month integer[UNSIGNED] NOT NULL," +
                "r_day integer[UNSIGNED] NOT NULL," +
                "r_date Date null," +
                "r_type varchar(5) NOT NULL," +
                "r_consume float[UNSIGNED] NOT NULL," +
                "r_description varchar(40) null," +
                "r_consumeType varchar(10) null," +
                "r_record_date timestamp NOT NULL default CURRENT_TIMESTAMP)");
//        db.execSQL("create table if not exists Record_Pic(" +
//                "_picId integer primary Key autoincrement," +
//                "_recordId integer," +
//                "_picPath varchar(50) "+
//                "foreign key(_recordId) references Record(_id) on delete cascade)");
    }
    //ALLOW_INVALID_DATES

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
