package com.storiarc.mobile.util;

import com.storiarc.mobile.model.DatabaseInterface;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.os.Environment;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.Comparator;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Util {
	
	public static final String packageroot = "com.storiarc.mobile";
	
	// ISO8601 date format: YYYY-MM-DD HH:MM:SS.SSS (not java's formatting)
	// SimpleDateFormat and ParsePosition are VERY not thread safe. Don't use static.
	//public static final SimpleDateFormat iso8601format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	//public static final SimpleDateFormat longformat = new SimpleDateFormat("MMMM dd, yyyy");

	public static final Comparator<DatabaseInterface> created_cmp_desc = new Comparator<DatabaseInterface>() {
		public int compare(DatabaseInterface e1, DatabaseInterface e2) {
			if (e1 == e2) {
				return 0;
			}
			if (e1 == null) {
				return 1;
			}								
			else if (e1.created != null && e2.created != null) {
				int ret = e1.created.compareTo(e2.created);
				return ret;
			}
			return -1;
		}
	};	
	
	public static final Comparator<DatabaseInterface> created_cmp_asc = new Comparator<DatabaseInterface>() {
		public int compare(DatabaseInterface e1, DatabaseInterface e2) {
			if (e1 == e2) {
				return 0;
			}
			if (e1 == null) {
				return -1;
			}								
			else if (e1.created != null && e2.created != null) {
				int ret = -e1.created.compareTo(e2.created);
				return ret;
			}
			return 1;
		}
	};	
	
	public static String longDateFormat(Date d) {
		if (d == null) {
			return "";
		}
		SimpleDateFormat longformat = new SimpleDateFormat("MMMM dd, yyyy");
		return longformat.format(d);
	}
	
	public static String slashDatePlusTimeFormat(Date d) {
		if (d == null) {
			return "";
		}
		
		String timesetcheck = (new SimpleDateFormat("kk:mm:ss:SSS")).format(d);
		
		SimpleDateFormat fmt;
		if (timesetcheck.equals("24:00:00:000")) {
			fmt = new SimpleDateFormat("M/d/yyyy");
		}
		
		fmt = new SimpleDateFormat("M/d/yyyy h:mm a");
		return fmt.format(d);
	}
	
	public static String APDatePlusTimeFormat(Date d) {
		if (d == null) {
			return "";
		}
		
		String timesetcheck = (new SimpleDateFormat("kk:mm:ss:SSS")).format(d);
		
		SimpleDateFormat fmt;
		if (timesetcheck.equals("24:00:00:000")) {
			fmt = new SimpleDateFormat("MMM d, yyyy");
		}
		else {
			fmt = new SimpleDateFormat("MMM d, yyyy h:mm a");
		}
		
		return fmt.format(d);	
	}
	
	public static String longDatePlusTimeFormat(Date d) {
		if (d == null) {
			return "";
		}
		
		String timesetcheck = (new SimpleDateFormat("kk:mm:ss:SSS")).format(d);
		
		if (timesetcheck.equals("24:00:00:000")) {
			return longDateFormat(d);
		}
		
		SimpleDateFormat longformat = new SimpleDateFormat("MMMM dd, yyyy h:mm a");
		return longformat.format(d);
	}
	
	public static String timeFormat(Date d) {
		if (d == null) {
			return "";
		}
		SimpleDateFormat longformat = new SimpleDateFormat("h:mm a");
		return longformat.format(d);
	}	
	
	public static String isoDateFormat(Date d) {
		if (d == null) {
			return "";
		}
	
		SimpleDateFormat databaseformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return databaseformat.format(d);
	}

	// We want to recover the time from the date
	public static Date datePickerToDate(Date original, int year, int monthOfYear, int dayOfMonth) {
		String time = "00:00";
		
		if (original != null) {
			SimpleDateFormat originalformat = new SimpleDateFormat("H:m");
			time = originalformat.format(original);
		}
	
		String dpdate = year + " " + monthOfYear + " " + dayOfMonth + " " + time;
		SimpleDateFormat dpformat = new SimpleDateFormat("yyyy M d H:m");
		ParsePosition pos = new ParsePosition(0);
		return dpformat.parse(dpdate, pos);		
	}
	
	public static Date timePickerToDate(Date basedate, int hourOfDay, int minute) {
		if (basedate == null) {
			basedate = new Date();
		}	
	
		SimpleDateFormat baseformat = new SimpleDateFormat("yyyy-MM-dd");
		String datetimestr = baseformat.format(basedate) + " " + hourOfDay + ":" + minute;
		SimpleDateFormat correctedformat = new SimpleDateFormat("yyyy-MM-dd H:m");
		ParsePosition pos = new ParsePosition(0);
		Date d = correctedformat.parse(datetimestr, pos);
		
		// When time has been set, we need a way to distinguish
		// when 12 means 12 and 12 means not set
		long msec = d.getTime();
		d.setTime(msec + 1);
		
		return d;
	}	

	public static Date isoToDate(String isodate) {
		if (isodate == null) {
			return null;
		}		
	
		SimpleDateFormat databaseformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		ParsePosition pos = new ParsePosition(0);
		return databaseformat.parse(isodate, pos);
	}	
	
	public static Date addDays(Date d, int days) {
		if (d == null) {
			return d;
		}
		
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(c.DAY_OF_YEAR, days);
		
		return c.getTime();
	}
	
	public static Object nvl(Object a, Object b) {
		if (a != null) {
			return a;
		}
		else if (b != null) {
			return b;
		}
		return null;
	}
	
	// String nvl: snvl
	public static String snvl(String a, String b) {
		if (a == null || a.length() == 0) {
			return b;
		}
		return a;
	}
	
	// String Conditional nvl: scnvl
	// If test is empty, a, else b
	public static String scnvl(String test, String a, String b) {
		if (test == null || test.length() == 0) {
			return b;
		}
		return a;
	}
	
	public static String scnvl(Object test, String a, String b) {
		if (test == null) {
			return b;
		}
		return a;
	}
	
	public static boolean isBlank(Object a) {
		return Util.isBlank(a.toString());
	}
	
	public static boolean isBlank(String a) {
		return (a == null || a.trim().equals(""));
	}
	
	public static boolean isEqual(Object a, Object b) {
		if (a == b) {
			return true;
		}
		else if (a == null || b == null) {
			return false;
		}
		
		return a.equals(b);		
	}
	
	public static void putOrNull(ContentValues cv, String column, String value) {
		if (value == "" || value == null) {
			cv.putNull(column);
		}
		else {
			cv.put(column, value);
		}
	}
	
	public static void putOrNull(ContentValues cv, String column, long value) {
		if (value <= 0) {
			cv.putNull(column);
		}
		else {
			cv.put(column, value);
		}
	}
	
	// Bug: Does not account for daylight savings time
	// Jodatime is supposed to be good for that
	public static double deltaDays(Date d1, Date d2) {
		if (d1 == null || d2 == null) {
			return -90000;
		}
	
		double delta = d2.getTime() - d1.getTime();
		double days = delta / (1000 * 60 * 60 * 24);
		return days;
	}
	
	public static boolean externalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		} 
		else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		} 
		return false;
	}
	
	public static boolean externalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		} 
		return false;		
	}
	
	public static Object manufacture(Class klass, Object[] params) {
		int prototypelen = params.length;
		Class[] prototype = new Class[prototypelen];
	
		for (int i = 0; i < prototypelen; i++) {
			prototype[i] = params[i].getClass();
		}
	
		Constructor constructor = null;
		try {
			constructor = klass.getConstructor(prototype); 
		}
		catch (NoSuchMethodException e) { 
			e.printStackTrace(); 
		}
		
		return manufacture(constructor, params);	
	}
	
	public static Object manufacture(Constructor constructor, Object[] params) {
		Object obj = null;
		
		try {
			obj = constructor.newInstance(params);
		}
		catch (InstantiationException e) {
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		finally {
			return obj;
		}	
	}	
}