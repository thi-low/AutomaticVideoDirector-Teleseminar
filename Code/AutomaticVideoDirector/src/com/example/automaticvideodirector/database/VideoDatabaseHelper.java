package com.example.automaticvideodirector.database;

import android.content.Context;
import android.database.sqlite.*;
import android.util.Log;

public class VideoDatabaseHelper extends SQLiteOpenHelper {
	
	
	//VARIABLES
	private static final String DEBUG_TAG = "VideoDatabaseHelper";
	
    private static final String DATABASE_NAME = "metadata.db";
    private static final int DATABASE_VERSION = 1;
    
	public static final String TABLE_METADATA = "metadata";
    
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FILENAME = "filename";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_WIDTH = "width";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_SHAKING = "shaking";
    public static final String COLUMN_TILT = "tilt";
    public static final String COLUMN_SERVERID = "serverId";
    public static final String COLUMN_STATUS = "status";
    

    
    
    
    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
        + TABLE_METADATA + "(" + COLUMN_ID + " integer primary key autoincrement, "
        + COLUMN_FILENAME + " text,"+ COLUMN_TIMESTAMP + " text,"+ COLUMN_DURATION + " integer,"
        + COLUMN_WIDTH + " int," + COLUMN_HEIGHT + " int,"+ COLUMN_SHAKING + " integer," +COLUMN_TILT+" integer,"
        + COLUMN_SERVERID+" integer," + COLUMN_STATUS+" text)";
    
    
    VideoDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
        Log.i(DEBUG_TAG,"DATABASE SUCCESFULLY CREATED");
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
}