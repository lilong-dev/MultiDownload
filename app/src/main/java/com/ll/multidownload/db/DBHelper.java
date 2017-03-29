package com.ll.multidownload.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by admin on 2017/3/28.
 */

public class DBHelper extends SQLiteOpenHelper{
    private static final String DB_NAME = "download.db";
    private static final int DB_VERSION = 1;
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table  if not exists download(_id INTEGER PRIMARY KEY AUTOINCREMENT,thread_id INTEGER,start_pos INTEGER,end_pos INTEGER," +
                "complete_size INTEGER,download_url char)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
