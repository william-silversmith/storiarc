package com.storiarc.mobile.util;

import android.content.ContentValues;
import android.database.Cursor;

import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteDatabase;

import android.util.Log;

import java.util.HashMap;
import java.util.ArrayList;

public class SQL {
	
	private Cursor results;
	public final static int ERROR_INT = -999999999;
	
	public SQL(SQLiteDatabase db, String sql) {
		execute(db, sql, null);
	}
	
	public SQL(SQLiteDatabase db, String sql, String[] binds) {
		execute(db, sql, binds);
	}
	
	private void execute(SQLiteDatabase db, String sql, String[] binds) {
		results = db.rawQuery(sql, binds);
		results.moveToFirst();
	}
	
	public String singleValue() {
		results.moveToFirst();
		
		if (!results.isAfterLast()) {
			return results.getString(0);
		}
		return null;
	}
	
	public int singleIntValue() {
		results.moveToFirst();
		
		if (!results.isAfterLast()) {
			return results.getInt(0);
		}
		return ERROR_INT;	
	}
	
	public ArrayList<String> columnValues() {
		ArrayList<String> vals = new ArrayList<String>();
		vals.ensureCapacity(results.getCount());
		
		results.moveToFirst();
		while(!results.isAfterLast()) {
			vals.add(results.getString(0));
			results.moveToNext();
		}
		
		return vals;
	}
	
	public ArrayList<HashMap<String, Object>> hashValues() {
		if (results.isAfterLast()) {
			return null;
		}
	
		ArrayList rows = new ArrayList<HashMap<String, Object>>();
		rows.ensureCapacity(results.getCount());
		results.moveToFirst();

		int count = results.getCount();
		for (int i = 0; i < count; i++) {
			rows.add(this.next());
		}		
		
		return rows;
	}
	
	public HashMap<String, Object> next() {
		if (results.isAfterLast()) {
			return null;
		}

		HashMap<String, Object> row = new HashMap<String, Object>();

		String[] columns = results.getColumnNames();
		for (int index = 0; index < columns.length; index++) {
			String col = columns[index];
			
			String val = results.getString(index);
			try {
				long num = Long.parseLong(val);
				row.put(col, num);
			}
			catch (NumberFormatException e) {
				row.put(col, val);	
			}
		}
		
		results.moveToNext();
		return row;
	}
	
	public void close() {
		results.close();
	}
	
	public int count() {
		return results.getCount();
	}
	
	public void requery() {
		results.requery();
	}
	
	public void deactivate() {
		results.deactivate();
	}
}