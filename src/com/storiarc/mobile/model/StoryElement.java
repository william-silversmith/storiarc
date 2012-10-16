package com.storiarc.mobile.model;

import com.storiarc.mobile.model.*;
import com.storiarc.mobile.util.*;

import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Date;

abstract class StoryElement 
	extends DatabaseInterface {

	public long storyid = -1L;
	
	public StoryElement() {
		super();
	}
	
	public StoryElement(long id) {
		super(id);
	}
	
	public boolean load(SQLiteDatabase db) {
		boolean success = false;
	
		Cursor result = db.rawQuery("SELECT * FROM " + getTableName() + " WHERE id = " + this.id, null);
		result.moveToFirst();
		if (!result.isAfterLast()) {
			this.populateFromCursor(result);
			success = true;
		}
		result.close();
		return success;
	}	
	
	
	public void populateFromCursor(Cursor row) {
		int index;		
		index = row.getColumnIndex("id");
		this.id = row.getLong(index);
		
		index = row.getColumnIndex("storyid");
		this.storyid = row.getLong(index);
		
		index = row.getColumnIndex("created");
		this.created = Util.isoToDate(row.getString(index));
		
		index = row.getColumnIndex("deleted");
		this.deleted = Util.isoToDate(row.getString(index));
		
		index = row.getColumnIndex("lastmodified");
		this.lastmodified = Util.isoToDate(row.getString(index));	
	}
}