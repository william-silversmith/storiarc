package com.storiarc.mobile.model;

import com.storiarc.mobile.*;
import com.storiarc.mobile.util.Util;

import android.view.View;
import android.widget.TextView;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.util.Log;

import java.util.Date;

public class Note 
	extends DatabaseInterface {

	public static final String TABLE = "note";
	
	public long storyid = -1L;
	//public long placeid = -1L;
	public String text;

	public Note(SQLiteDatabase db) {
		super();
		this.load(db);
	}
	
	public Note(SQLiteDatabase db, int id) {
		super(id);
		this.load(db);	
	}
	
	public Note(SQLiteDatabase db, long id) {
		super(id);
		this.load(db);
	}

	public Note(SQLiteDatabase db, Long id) {
		super(id.longValue());
		this.load(db);
	}
	
	/* DatabaseOpener Interface */
	
	public boolean save(SQLiteDatabase db) {
		if (this.storyid == -1L) {
			return false;
		}		
		
		ContentValues cv = new ContentValues();
		cv.put("storyid", this.storyid);
		Util.putOrNull(cv, "text", this.text);
		//Util.putOrNull(cv, "placeid", this.placeid);
		
		return this.saveToDatabase(db, cv);
	}
	
	public void populateFromCursor(Cursor row) {
		int index;		
		index = row.getColumnIndex("id");
		this.id = row.getLong(index);
		
		index = row.getColumnIndex("storyid");
		this.storyid = row.getLong(index);

		index = row.getColumnIndex("text");
		this.text = row.getString(index);

		index = row.getColumnIndex("created");
		this.created = Util.isoToDate(row.getString(index));
		
		index = row.getColumnIndex("deleted");
		this.deleted = Util.isoToDate(row.getString(index));
		
		index = row.getColumnIndex("lastmodified");
		this.lastmodified = Util.isoToDate(row.getString(index));
		
		//index = row.getColumnIndex("placeid");
		//this.placeid = row.getLong(index);
	}
	
	public boolean isEmpty(SQLiteDatabase db) {
		return this.isEmpty();
	}
	
	public boolean isEmpty() {
		return Util.isBlank(this.text);
	}
	
	public String getTableName() {
		return TABLE;
	}
	
	public void setText(String txt) {
		if (!Util.isEqual(this.text, txt)) {
			this.changed = true;
			this.text = txt;
		}
	}
	
	public String toString() {
		return "Note: " + this.text;
	}
}