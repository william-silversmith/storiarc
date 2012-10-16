package com.storiarc.mobile.model;

import com.storiarc.mobile.*;
import com.storiarc.mobile.util.Util;

import android.view.View;
import android.widget.TextView;
import android.location.Location;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

import java.util.Date;

public class Place 
	extends DatabaseInterface {

	public static final String TABLE = "place";
	
	public long storyid = -1;
	public String name;
	public String address;
	public double latitude;
	public double longitude;
	public String text;

	public Place(SQLiteDatabase db, long id) {
		super(id);
		this.load(db);
	}
	
	/* Database Interface */
	
	public boolean save(SQLiteDatabase db) {
		if (this.storyid == -1L) {
			return false;
		}		

		ContentValues cv = new ContentValues();
		cv.put("storyid", this.storyid);
		Util.putOrNull(cv, "name", this.name);
		Util.putOrNull(cv, "address", this.address);
		cv.put("latitude", this.latitude);
		cv.put("longitude", this.longitude);
		Util.putOrNull(cv, "text", this.text);
		
		return this.saveToDatabase(db, cv);
	}

	public boolean isEmpty(SQLiteDatabase db) {
		return true;
	}	

	public void populateFromCursor(Cursor row) {
	
	}
	
	public String getTableName() {
		return TABLE;
	}	
}