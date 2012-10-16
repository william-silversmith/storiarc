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
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import java.util.Date;

public class Audio 
	extends DatabaseInterface
	implements Recording {

	public static final String TABLE = "recording";
	public static final String RECORDINGTYPE = "audio";

	public long storyid = -1;
	public String filepath;
	public String text;
			
	public Audio(SQLiteDatabase db, long id) {
		super(id);
		this.load(db);
	}
	
	/* DatabaseOpener Interface */
	
	public boolean save(SQLiteDatabase db) {
		if (this.storyid == -1L) {
			return false;
		}			
	
		ContentValues cv = new ContentValues();
		cv.put("storyid", this.storyid);
		Util.putOrNull(cv, "filepath", this.filepath);
		Util.putOrNull(cv, "text", this.text);
		cv.put("type", getRecordingType());
		
		return this.saveToDatabase(db, cv);
	}
	
	@Override
	public boolean load(SQLiteDatabase db) {
		return false;
	}
	
	public void populateFromCursor(Cursor row) {
	
	}
	
	public boolean isEmpty(SQLiteDatabase db) {
		return true;
	}
	
	public String getTableName() {
		return TABLE;
	}
	
	public String getRecordingType() {
		return RECORDINGTYPE;
	}
}