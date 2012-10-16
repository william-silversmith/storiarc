package com.storiarc.mobile.activity;

import com.storiarc.mobile.*;
import com.storiarc.mobile.util.*;
//import com.storiarc.mobile.model.google.*;
import com.storiarc.mobile.activity.*;

import android.content.*;
import android.view.*;
import android.app.*;
import android.preference.*;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.net.Uri;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;

import java.util.*;

public class StoriarcPreferences extends PreferenceActivity {

	ListPreference calendarpref;
	ListPreference googleaccountpref;
	
	private static final String CALENDAR_AUTH_TYPE = "cl";
	
	private static final int DIALOG_ACCOUNTS = 0;
	private static final int REQUEST_AUTHENTICATE = 8293;
	private static final String acctkey = "googleaccount";
	
	private String authtoken;

    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);

		SharedPreferences settings = getPreferenceManager().getSharedPreferences();
		String googleaccount = settings.getString(acctkey, null);

		ArrayList<String> calendarnames = new ArrayList<String>();
		ArrayList<String> calendarvalues = new ArrayList<String>();
		
		/* See: http://www.google.com/codesearch/p?hl=en&sa=N&cd=3&ct=rc#uX1GffpyOZk/core/java/android/provider/Calendar.java
		 * Get only calendars where we have edit permission
		 * As of 1/2/2011:
		 * 	public static final int NO_ACCESS = 0;
		 *	 FREEBUSY_ACCESS = 100
		 *	Can read all event details:
		 *	 READ_ACCESS = 200
		 *	 RESPOND_ACCESS = 300
		 *	 OVERRIDE_ACCESS = 400
		 *	Full access to modify the calendar, but not the access control settings:
		 *	 CONTRIBUTOR_ACCESS = 500
		 *	 EDITOR_ACCESS = 600
		 *	Full access to the calendar: 
		 *   OWNER_ACCESS = 700
		 *	Domain admin: 
		 *   ROOT_ACCESS = 800
		 */
		 
		String[] calnames, calvals; 
		 
		try { 
			Uri calendars = Uri.parse("content://com.android.calendar/calendars/");
			Cursor managedCursor = managedQuery(calendars, null, "access_level >= 500", null, null); 
			
			if (managedCursor.moveToFirst()) {
				 int nameColumn = managedCursor.getColumnIndex("name"); 
				 int idColumn = managedCursor.getColumnIndex("_id");
				 do {
					calendarnames.add(managedCursor.getString(nameColumn));
					calendarvalues.add(managedCursor.getString(idColumn));
				 } while (managedCursor.moveToNext());
			}
			
			calnames = (String[])calendarnames.toArray(new String[0]);
			calvals = (String[])calendarvalues.toArray(new String[0]);
		}
		catch (Exception e) {
			calnames = new String[] { "None" };
			calvals = new String[] { "-1" };
		}
		
		if (calnames.length == 0 || calvals.length == 0) {
			calnames = new String[] { "None" };
			calvals = new String[] { "-1" };
		}		
		
		calendarpref = (ListPreference)getPreferenceScreen().findPreference("calendarid calendar");
		calendarpref.setEntries(calnames);
		calendarpref.setEntryValues(calvals);
    }

	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_ACCOUNTS:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Select a Google account");
				final AccountManager manager = AccountManager.get(this);
				final Account[] accounts = manager.getAccountsByType("com.google");
				final int size = accounts.length;
				String[] names = new String[size];
				for (int i = 0; i < size; i++) {
				  names[i] = accounts[i].name;
				}
				builder.setItems(names, new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int which) {
					SharedPreferences settings = StoriarcPreferences.this.getPreferenceManager().getSharedPreferences();
					SharedPreferences.Editor editor = settings.edit();
					editor.putString(acctkey, accounts[which].name);
					editor.commit();
				  }
				});
				return builder.create();
		}
		return null;
	}
}


		
		