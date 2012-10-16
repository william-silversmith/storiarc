package com.storiarc.mobile.model;

import com.storiarc.mobile.*;
import com.storiarc.mobile.util.Util;

import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore.Images.Thumbnails;
import android.provider.MediaStore;


import java.util.Date;
import java.io.File;
import java.io.FileOutputStream;

public class Image 
	extends DatabaseInterface
	implements Recording {

	public static final String TABLE = "recording";
	public static final String RECORDINGTYPE = "image";

	public long storyid = -1;
	public Uri uri;
	public String text;
	
	public Bitmap bitmap;
	
	public Image(SQLiteDatabase db) {
		super();
		this.load(db);
	}
	
	public Image(SQLiteDatabase db, long id) {
		super(id);
		this.load(db);
	}
	
	public Image(SQLiteDatabase db, Long id) {
		super(id.longValue());
		this.load(db);		
	}
	
	public void setStory(long storyid) {
		this.storyid = storyid;
	}
	
	public void setStory(Story story) {
		this.storyid = story.id;
	}
	
	public void setUri(Uri uri) {
		this.uri = uri;
	}
	
	public void setText(String s) {
		this.text = s;
	}
	
	/* DatabaseOpener Interface */
	
	public boolean save(SQLiteDatabase db) {
		if (this.storyid == -1L) {
			return false;
		}			
			
		ContentValues cv = new ContentValues();
		cv.put("storyid", this.storyid);
		cv.put("uri", this.uri.toString());
		Util.putOrNull(cv, "text", this.text);
		cv.put("type", getRecordingType());
		return this.saveToDatabase(db, cv);		
	}
	
	@Override
	public boolean load(SQLiteDatabase db) {
		if (Util.externalStorageReadable()) {
			return super.load(db);			
		}
		return false;
	}	
	
	public void populateFromCursor(Cursor row) {
		int index;		
		index = row.getColumnIndex("id");
		this.id = row.getLong(index);
		
		index = row.getColumnIndex("storyid");
		this.storyid = row.getLong(index);

		index = row.getColumnIndex("uri");
		this.uri = Uri.parse(row.getString(index));
		
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
		return true;
	}	
	
	public void materialize() {
		
	}
	
	public String toString() {
		return "An image of some sort.";
	}

	public String getTableName() {
		return TABLE;
	}
	
	public String getRecordingType() {
		return RECORDINGTYPE;
	}
	
	public Bitmap getThumbnail(Context ctx) {
		long mediastoreimageid = this.getMediaStoreImageId(ctx);
		if (mediastoreimageid < 0) {
			return null;
		}
		return MediaStore.Images.Thumbnails.getThumbnail(
			ctx.getContentResolver(),
			mediastoreimageid, 
			Thumbnails.MINI_KIND, 
			null);	
	}
	
	public long getMediaStoreImageId(Context ctx)	{
		long result = -1L;
		File imgfile = new File(this.uri.toString());
		Cursor c = ctx.getContentResolver().query(
			MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			new String[] { MediaStore.Images.Media._ID + "", MediaStore.Images.Media.DISPLAY_NAME },
			MediaStore.Images.Media.DATA + " = ?",
			new String[]{ imgfile.getAbsolutePath().substring(6) }, 
			null);
			
		c.moveToFirst();
		if (!c.isAfterLast()) {
			result = c.getLong(0);
			c.moveToNext();
		}
		c.close();
		return result;
	}
}