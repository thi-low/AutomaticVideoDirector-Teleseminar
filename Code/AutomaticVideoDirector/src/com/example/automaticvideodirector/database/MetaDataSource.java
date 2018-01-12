package com.example.automaticvideodirector.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MetaDataSource {
	
	//VARIABLES
	private static final String DEBUG_TAG ="MetaDataSource";
	
	private SQLiteDatabase database;
	private VideoDatabaseHelper dbHelper;
	private String[] allColumns = {	VideoDatabaseHelper.COLUMN_ID, 
									VideoDatabaseHelper.COLUMN_FILENAME, 
									VideoDatabaseHelper.COLUMN_TIMESTAMP, 
									VideoDatabaseHelper.COLUMN_DURATION, 
									VideoDatabaseHelper.COLUMN_WIDTH, 
									VideoDatabaseHelper.COLUMN_HEIGHT,
									VideoDatabaseHelper.COLUMN_SHAKING,
									VideoDatabaseHelper.COLUMN_TILT,
									VideoDatabaseHelper.COLUMN_SERVERID,
									VideoDatabaseHelper.COLUMN_STATUS		};
	
	public MetaDataSource(Context context) {
	    dbHelper = new VideoDatabaseHelper(context);
	}
	
	public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
	    dbHelper.close();
	    database.close();
	}
	
	//AFTER A VIDEOFILE IS CREATED, NEW METADATA WILL BE INSERTED TO THE DATABASE AND HAS TO BE SEND VIA HTTP_POST TO THE SERVER
	public long insertMetaData(MetaData data) {
		Log.i(DEBUG_TAG,"INSERT METADATA IN TABLE");
	    ContentValues values = new ContentValues(); 
	    values.put(VideoDatabaseHelper.COLUMN_FILENAME, data.getVideoFile());
	    values.put(VideoDatabaseHelper.COLUMN_TIMESTAMP, data.getTimeStamp());
	    values.put(VideoDatabaseHelper.COLUMN_DURATION, data.getDuration());
	    values.put(VideoDatabaseHelper.COLUMN_WIDTH, data.getWidth());
	    values.put(VideoDatabaseHelper.COLUMN_HEIGHT, data.getHeight());
	    values.put(VideoDatabaseHelper.COLUMN_SHAKING, data.getShaking());
	    values.put(VideoDatabaseHelper.COLUMN_TILT, data.getTilt());
	    values.put(VideoDatabaseHelper.COLUMN_SERVERID, 0);
	    values.put(VideoDatabaseHelper.COLUMN_STATUS, "false");
 
	    long insertId= database.insert(VideoDatabaseHelper.TABLE_METADATA, null, values);
	    Cursor cursor = database.query(VideoDatabaseHelper.TABLE_METADATA, allColumns, VideoDatabaseHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
	        cursor.moveToFirst();
	        Log.d(DEBUG_TAG, "ID:"+cursor.getLong(0));
	        Log.d(DEBUG_TAG, "Filename:"+cursor.getString(1));
	        Log.d(DEBUG_TAG, "Timestamp:"+cursor.getString(2));
	        Log.d(DEBUG_TAG, "Duration:"+cursor.getInt(3));
	        Log.d(DEBUG_TAG, "width:"+cursor.getString(4));
	        Log.d(DEBUG_TAG, "height:"+cursor.getString(5));
	        Log.d(DEBUG_TAG, "shaking:"+cursor.getInt(6));
	        Log.d(DEBUG_TAG, "tilt:"+cursor.getInt(7));
	        Log.d(DEBUG_TAG, "serverId:"+cursor.getLong(8));
	        Log.d(DEBUG_TAG, "status:"+cursor.getString(9));
	        cursor.close();
	    return insertId;
	}
	
	
	//GETS THE METADATA OF A SPECIFIC REQUESTED VIDEOFILE.--->PARAMETERS: SERVERID,  RETURN:VIDEOFILE RELATED METADATA
	public MetaData selectMetaData(Integer serverId){
		
		String selectQuery = "SELECT * FROM " + VideoDatabaseHelper.TABLE_METADATA 
				+ " WHERE " + VideoDatabaseHelper.COLUMN_SERVERID + " = " + serverId;
		Cursor cursor = database.rawQuery(selectQuery, null);
		MetaData newMetaData = null;
		System.out.println(cursor.getColumnCount());
		System.out.println(cursor.getCount());
		if (cursor!=null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				newMetaData = new MetaData();
	        	newMetaData.setId(cursor.getLong(0));
	        	newMetaData.setVideoFile(cursor.getString(1));
	        	newMetaData.setTimeStamp(cursor.getString(2));
	        	newMetaData.setDuration(cursor.getInt(3));
	        	newMetaData.setWidth(cursor.getInt(4));
	        	newMetaData.setHeight(cursor.getInt(5));
	        	newMetaData.setShaking(cursor.getInt(6));
	        	newMetaData.setTilt(cursor.getInt(7));
	        	newMetaData.setServerId(cursor.getLong(8));
	        	newMetaData.setStatus(cursor.getString(9));
	        }
		cursor.close();
		return newMetaData;
	}
	
	
	//UPDATE DB WITH NEW SERVERID
	public void updateServerId(int serverID, String filename){
		ContentValues values = new ContentValues();	
		values.put(VideoDatabaseHelper.COLUMN_SERVERID, serverID);
		Log.d("DATABASEUPDATE",filename);
		Log.d("DATABASEUPDATE",VideoDatabaseHelper.COLUMN_FILENAME);
		long d = database.update(VideoDatabaseHelper.TABLE_METADATA, values, VideoDatabaseHelper.COLUMN_FILENAME+"='"+filename+"'",null);
		Log.d("DATABASEUPDATE", String.valueOf(d));
	}
	
	//UPDATE DB WITH NEW STATUS
	public void updateStatus(String filename){
		ContentValues values = new ContentValues();	
		values.put(VideoDatabaseHelper.COLUMN_STATUS, "true");
		database.update(VideoDatabaseHelper.TABLE_METADATA, values, VideoDatabaseHelper.COLUMN_FILENAME+"='"+filename+"'",null);
	}
	

}
