package com.storiarc.mobile.util;

import com.storiarc.mobile.util.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseOpener extends SQLiteOpenHelper {
    public static final String NAME = "storiarc.db";
    public static final int VERSION = 1;
	
	final String[] schema = {
		"CREATE TABLE story ("
           	+ "id INTEGER PRIMARY KEY,"
           	+ "title TEXT,"
           	+ "description TEXT,"
           	+ "deadline TEXT,"
			+ "status INTEGER," // 0 = incomplete, 1 = complete, 
			+ "deleted TEXT," // a date
			+ "created TEXT NOT NULL,"
           	+ "lastmodified TEXT"
           + ")",
		"CREATE TABLE note ("
              + "id INTEGER PRIMARY KEY,"
              + "text TEXT,"
              + "storyid INTEGER,"
			  + "deleted TEXT," // a date
			  + "created TEXT NOT NULL,"
              + "lastmodified TEXT"
			  //+ "placeid INTEGER"
           + ")",
		"CREATE TABLE quote ("
              + "id INTEGER PRIMARY KEY,"
			  + "storyid INTEGER,"
              + "entity TEXT,"
              + "quote TEXT,"
			  + "context TEXT,"
			  + "created TEXT NOT NULL,"
			  + "deleted TEXT," // a date
              + "date TEXT,"
              + "lastmodified TEXT"
			  //+ "placeid INTEGER"
           + ")",
		"CREATE TABLE recording ("
            + "id INTEGER PRIMARY KEY,"
			+ "storyid INTEGER,"
            + "uri TEXT,"
            + "type TEXT,"
            + "created TEXT NOT NULL,"
            + "lastmodified TEXT,"
            + "text TEXT,"
			+ "deleted TEXT" // a date
		    //+ "placeid INTEGER"
           + ")",
		"CREATE TABLE place ("
             + "id INTEGER PRIMARY KEY,"
			 + "storyid INTEGER,"
             + "name TEXT,"
             + "address TEXT,"
             + "latitude REAL,"
             + "longitude REAL,"                
             + "created TEXT NOT NULL,"
             + "lastmodified TEXT,"
             + "text TEXT,"
			 + "deleted TEXT" // a date
           + ")",
		"CREATE TABLE googlecalendar ("
			+ "id INTEGER PRIMARY KEY,"
			+ "storyid INTEGER,"
			+ "eventid STRING" // using full uri in case a numbering system changes
		+ ")",
	};

   public DatabaseOpener(Context context) {
       super(context, NAME, null, VERSION);   
   }
   
   @Override
   public void onCreate(SQLiteDatabase db) {
		for ( String tableschema : schema ) {
			db.execSQL(tableschema);
		}
   }
   
   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      
   }  
}










