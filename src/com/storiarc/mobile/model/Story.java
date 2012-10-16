package com.storiarc.mobile.model;

import com.storiarc.mobile.*;
import com.storiarc.mobile.model.*;
import com.storiarc.mobile.util.*;

import android.view.View;
import android.widget.TextView;
import android.database.Cursor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.content.ContentValues;
import android.view.View;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Story 
	extends DatabaseInterface {

	public static final String TABLE = "story";

	public String title;
	public String description;
	public int status;
	public Date deadline;

	public ArrayList<DatabaseInterface> notes, quotes, images, videos, places, audios;
	
	public Story(SQLiteDatabase db) {
		super(-1);
		create(db);
	}
	
	public Story(SQLiteDatabase db, long id) {
		super(id);
		create(db);
	}
	
	private void create(SQLiteDatabase db) {		
		notes = new ArrayList<DatabaseInterface>();
		quotes = new ArrayList<DatabaseInterface>();
		images = new ArrayList<DatabaseInterface>();
		videos = new ArrayList<DatabaseInterface>();
		places = new ArrayList<DatabaseInterface>();
		audios = new ArrayList<DatabaseInterface>();
		
		this.load(db);
	}
	
	public boolean lightweightSave(SQLiteDatabase db) {
		ContentValues cv = new ContentValues();
		Util.putOrNull(cv, "title", this.title);
		Util.putOrNull(cv, "description", this.description);
		Util.putOrNull(cv, "deadline", Util.isoDateFormat(this.deadline));
		cv.put("status", this.status);
		
		return this.saveToDatabase(db, cv);	
	}
		
	/* DatabaseInterface Interface */

	public boolean save(SQLiteDatabase db) {
		boolean success = true;		
		
		ArrayList<DatabaseInterface> all = this.getAllElements(db);
		for (DatabaseInterface elem : all) {
			if (this.deleted != null) {
				elem.deleted = new Date();
			}		
			success = success && elem.save(db);
		}
		
		return success && this.lightweightSave(db);
	}

	@Override
	public boolean load(SQLiteDatabase db) {	
		if (this.id == -1) {
			return true;
		}

		boolean success = false;

		Cursor result = db.rawQuery("SELECT * FROM " + this.TABLE + " WHERE id = " + this.id, null);
		result.moveToFirst();
		if (!result.isAfterLast()) {				
			this.populateFromCursor(result);
			success = true;
		}
		result.close();
		return success;	
	}
	
	public void populateFromCursor(Cursor row) {
		int index = row.getColumnIndex("id");
		this.id = row.getLong(index);

		index = row.getColumnIndex("title");
		this.title = row.getString(index);
		
		index = row.getColumnIndex("description");
		this.description = row.getString(index);
		
		index = row.getColumnIndex("status");
		this.status = row.getInt(index);
		
		index = row.getColumnIndex("deadline");
		this.deadline = Util.isoToDate(row.getString(index));
		
		index = row.getColumnIndex("created");
		this.created = Util.isoToDate(row.getString(index));
		
		index = row.getColumnIndex("deleted");
		this.deleted = Util.isoToDate(row.getString(index));
		
		index = row.getColumnIndex("lastmodified");
		this.lastmodified = Util.isoToDate(row.getString(index));		
	}
	
	private ArrayList<DatabaseInterface> mash() {
		ArrayList<DatabaseInterface> all = new ArrayList<DatabaseInterface>();
		all.addAll(notes);
		all.addAll(quotes);
		all.addAll(images);
		all.addAll(videos);
		all.addAll(places);
		all.addAll(audios);
		return all;
	}
	
	public boolean isEmpty(SQLiteDatabase db) {
		String[] tablenames = { Note.TABLE, Quote.TABLE, Image.TABLE, Video.TABLE, Audio.TABLE };
		int count = 0;
		
		boolean isempty = true;
		isempty = isempty && Util.isBlank(this.title);
		isempty = isempty && Util.isBlank(this.description);
		isempty = isempty && (this.deadline == null);
		
		for (String tbl : tablenames) {
			String querystr = "select count(*) from " + tbl + " where deleted is null and storyid = " + this.id;
			Cursor results = db.rawQuery(querystr, null);
			results.moveToFirst();
			count += results.getInt(0);
			results.close();
			
			if (count != 0) {
				return false;
			}
		}
		
		return isempty;
	}
	
	/*
	public boolean add(StoryElement elem) {
		if (this.id < 1) {
			return false;
		}
	
		elem.storyid = this.id;
		
		// Add all elems to an array, then save the array when save is called.
		
		return true;
	}
	*/
	
	public static ArrayList<Story> getAllStories(SQLiteDatabase db) {
		return Story.getAllStories(db, false);
	}
	
	public static ArrayList<Story> getAllStories(SQLiteDatabase db, boolean finddeleted) {
		ArrayList<Integer> idlist = new ArrayList<Integer>();
		
		String querystr = "select * from " + TABLE;
		
		if (finddeleted == false) {
			querystr += " where deleted is null";
		 } 
		
		ArrayList<Story> storylist = new ArrayList<Story>();
		Cursor results = db.rawQuery(querystr, null);
		results.moveToFirst();
		while (!results.isAfterLast()) {
			Story newstory = new Story(db);
			newstory.populateFromCursor(results);
			storylist.add(newstory);
			results.moveToNext();
		}
		results.close();
		
		return storylist;
	}
	
	public ArrayList<DatabaseInterface> getAllElements(SQLiteDatabase db) {
		ArrayList<DatabaseInterface> all = new ArrayList<DatabaseInterface>();
		all.addAll(this.getNotes(db));
		all.addAll(this.getQuotes(db));
		all.addAll(this.getImages(db));
		
		return all;
	}
	
	public ArrayList<Note> getNotes(SQLiteDatabase db) {
		return (ArrayList<Note>)this._getAll(db, Note.class, false);
	}
	
	public ArrayList<Quote> getQuotes(SQLiteDatabase db) {
		return (ArrayList<Quote>)this._getAll(db, Quote.class, false);
	}
	
	public ArrayList<Image> getImages(SQLiteDatabase db) {
		return (ArrayList<Image>)this._getAll(db, Image.class, false);
	}
	
	/* klass is the name of a DatabaseInterface class */
	private ArrayList _getAll(SQLiteDatabase db, Class klass, boolean finddeleted) {
		
		String tablename = "";
		try {
			tablename = (String)klass.getField("TABLE").get(null);
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		} 
		catch (NoSuchFieldException e) { 
			e.printStackTrace(); 
		}
		
		String querystr = "select id from " + tablename + " where storyid = " + this.id;
		
		if (finddeleted == false) {
			querystr += " and deleted is null";
		 } 
				
		if (klass.isInstance(Recording.class)) {
			Object[] fakeparameters = { null, -1L };
			Recording rec = (Recording)Util.manufacture(klass, fakeparameters);
			querystr += " and type = \'" + rec.getRecordingType() + "\'";
		}
		
		ArrayList<Long> idlist = new ArrayList<Long>();

		Cursor results = db.rawQuery(querystr, null);
		results.moveToFirst();
		while (!results.isAfterLast()) {
			idlist.add(results.getLong(0));
			results.moveToNext();
		}
		results.close();

		ArrayList klasslist = new ArrayList();
		
		for (Long elementid : idlist) {
			Object[] parameters = { db, elementid.longValue() };
			Object obj = Util.manufacture(klass, parameters);
			klasslist.add(obj);
		}
		
		return klasslist;
	}
	
	public String getTableName() {
		return TABLE;
	}
	
	public boolean isComplete() {
		return status == 1;
	}
	
	public boolean isEscalated() {
		if (this.deadline == null) {
			return false;
		}
	
		double daystodeadline = Util.deltaDays(new Date(), this.deadline);
		return daystodeadline >= -1.5 && daystodeadline <= 1.5;
	}
	
	public void setTitle(String newtitle) {
		if (!Util.isEqual(this.title, newtitle)) {
			this.changed = true;
			this.title = newtitle;
		}
	}
	
	public void setDescription(String desc) {
		if (!Util.isEqual(this.description, desc)) {
			this.changed = true;
			this.description = desc;
		}
	}
	
	public void setStatus(int newstatus) {	
		if (this.status != newstatus) {
			this.changed = true;
			this.status = newstatus;
		}
	}
		
	public void setDeadline(Date newdeadline) {
		if (!Util.isEqual(this.deadline, newdeadline)) {
			this.changed = true;
			this.deadline = newdeadline;
		}
	}	
}




