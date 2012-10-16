package com.storiarc.mobile.model;

import com.storiarc.mobile.*;
import com.storiarc.mobile.util.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.view.View;
import android.util.Log;

import java.util.*;

public class Quote 
	extends DatabaseInterface {
	
	public static final String TABLE = "quote";	

	public long storyid = -1L;
	public String entity;
	public String quote;
	public String context;
	public Date date;	
	
	public Quote() {
		super(-1L);
	}
	
	public Quote(SQLiteDatabase db) {
		super(-1L);
	}
	
	public Quote(SQLiteDatabase db, long id) {
		super(id);
		this.load(db);
	}
	
	public Quote(SQLiteDatabase db, Long id) {
		super(id.longValue());
		this.load(db);
	}	
	
	/* Database Interface */
	
	public boolean save(SQLiteDatabase db) {
		if (this.storyid == -1L) {
			return false;
		}
			
		ContentValues cv = new ContentValues();
		cv.put("storyid", this.storyid);
		Util.putOrNull(cv, "entity", this.entity);
		Util.putOrNull(cv, "quote", this.quote);
		Util.putOrNull(cv, "context", this.context);
		Util.putOrNull(cv, "date", Util.isoDateFormat(this.date));
		
		return this.saveToDatabase(db, cv);
	}

	public void populateFromCursor(Cursor row) {
		int index;		
		index = row.getColumnIndex("id");
		this.id = row.getLong(index);
		
		index = row.getColumnIndex("storyid");
		this.storyid = row.getLong(index);

		index = row.getColumnIndex("entity");
		this.entity = row.getString(index);
		
		index = row.getColumnIndex("quote");
		this.quote = row.getString(index);
		
		index = row.getColumnIndex("context");
		this.context = row.getString(index);
		
		index = row.getColumnIndex("date");
		this.date = Util.isoToDate(row.getString(index));

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
		return Util.isBlank(this.quote) && Util.isBlank(this.entity) && Util.isBlank(this.context);
	}

	public void setQuote(String newquote) {
		if (!Util.isEqual(this.quote, newquote)) {
			this.changed = true;
			this.quote = newquote;
		}
	}
	
	public void setEntity(String newentity) {
		if (!Util.isEqual(this.entity, newentity)) {
			this.changed = true;
			this.entity = newentity;
		}		
	}
	
	public void setContext(String newcontext) {
		if (!Util.isEqual(this.context, newcontext)) {
			this.changed = true;
			this.context = newcontext;
		}		
	}
	
	public void setDate(Date newdate) {
		if (!Util.isEqual(this.date, newdate)) {
			this.changed = true;
			this.date = newdate;
		}			
	}

	public static ArrayList<String> getAllEntities(SQLiteDatabase db) {
		SQL sql = new SQL(db, "SELECT DISTINCT entity FROM " + Quote.TABLE + " WHERE deleted is null");
		ArrayList<String> vals = sql.columnValues();
		sql.close();
		
		return vals;
	}


	public String toString() {
		return "Quote: " + this.quote;
	}

	public String getTableName() {
		return TABLE;
	}
}