package com.storiarc.mobile.model;

import com.storiarc.mobile.util.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Date;

abstract public class DatabaseInterface {

	public long id = -1L;
	public Date created;
	public Date deleted;
	public Date lastmodified;

	public boolean changed;

	abstract public boolean save(SQLiteDatabase db);
	abstract public boolean isEmpty(SQLiteDatabase db);
	abstract public String getTableName();
	
	abstract public void populateFromCursor(Cursor row);
	
	public DatabaseInterface() {
		this.created = new Date();
	}
	
	public DatabaseInterface(long id) {
		this.id = id;
		this.created = new Date();
		this.changed = false;
	}
	
	public DatabaseInterface(int id) {
		this.id = (long)id;
		this.created = new Date();
		this.changed = false;
	}
	
	public boolean softDelete(SQLiteDatabase db) {
		if (this.id < 0L) {
			return false;
		}
		
		this.deleted = new Date();
		
		ContentValues cv = new ContentValues();
		cv.put("deleted", Util.isoDateFormat(this.deleted));
		String where = this.getTableName() + ".id = " + this.id;
		int numrows = db.update(this.getTableName(), cv, where, null);
		if (numrows < 1) {
			return false;
		}
		return true;
	}
	
	public boolean hardDelete(SQLiteDatabase db) {
		if (this.id <= -1L) {
			return true;
		}
	
		String where = this.getTableName() + ".id = " + this.id;
		int numrows = db.delete(this.getTableName(), where, null);
		if (numrows < 1) {
			return false;
		}
		return true;		
	}
	
	public boolean undelete(SQLiteDatabase db) {
		if (this.id < 0L) {
			return false;
		}
		
		ContentValues cv = new ContentValues();
		cv.putNull("deleted");
		String where = this.getTableName() + ".id = " + this.id;
		int numrows = db.update(this.getTableName(), cv, where, null);		
		if (numrows < 1) {
			return false;
		}
		return true;	
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
	
	protected boolean saveToDatabase(SQLiteDatabase db, ContentValues cv) {
		this.lastmodified = new Date();	
	
		// DO NOT PUT ID IN CV
		Util.putOrNull(cv, "created", Util.isoDateFormat(this.created));
		Util.putOrNull(cv, "lastmodified", Util.isoDateFormat(this.lastmodified));
		Util.putOrNull(cv, "deleted", Util.isoDateFormat(this.deleted));

		if (this.id < 0L) {
			this.id = db.insert(this.getTableName(), "deleted", cv);
			if (this.id == -1L) {
				return false;
			}
			return true;
		}
		else {
			String where = this.getTableName() + ".id = " + this.id;
			int numrows = db.update(this.getTableName(), cv, where, null);
			if (numrows < 1) {
				return false;
			}
			return true;
		}	
	}

	public boolean isNew() {
		return this.id < 0L;
	}

	public boolean isChanged() {
		return this.changed;
	}
	
	public void setDeleted(Date deletiondate) {
		if (Util.isEqual(this.deleted, deletiondate)) {
			this.changed = true;
			this.deleted = deletiondate;
		}
	}
	
	public long count(SQLiteDatabase db) {
		return this.count(db, false);
	}
	
	public long count(SQLiteDatabase db, boolean countdeleted) {
		String querystr = "select count(*) from " + this.getTableName();
		if (countdeleted == false) {
			querystr += " where deleted is null";
		}
		
		Cursor result = db.rawQuery(querystr, null);
		result.moveToFirst();
		long count = result.getLong(0);
		result.close();
		return count;
	}
}

