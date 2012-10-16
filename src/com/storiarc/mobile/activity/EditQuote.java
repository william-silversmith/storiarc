package com.storiarc.mobile.activity;

import com.storiarc.mobile.*;
import com.storiarc.mobile.model.*;
import com.storiarc.mobile.util.*;

import android.view.*;
import android.widget.*;
import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import java.util.*;

public class EditQuote extends EditDBI {

	private Quote quote;

	AutoCompleteTextView entityedit;
	EditText noteedit;
	EditText contextedit;
	
	TextView datetimetext;

	TextView createdtext;
	TextView lastmodifiedtext;
	
	static final int DATEDIALOGID = 0;
	static final int TIMEDIALOGID = 1;
	
    private DatePickerDialog.OnDateSetListener dateSetListener =
		new DatePickerDialog.OnDateSetListener() {
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				Date newdate = Util.datePickerToDate(quote.date, year, monthOfYear+1, dayOfMonth); // +1 fixes off by one error
				quote.setDate(newdate);
				updateDateDisplay();
			}
		};		

	private TimePickerDialog.OnTimeSetListener timeSetListener =
		new TimePickerDialog.OnTimeSetListener() {
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				quote.setDate(Util.timePickerToDate(quote.date, hourOfDay, minute));
				updateDateDisplay();
			}
		};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editquote);
		
		db = (SQLiteDatabase)(new DatabaseOpener(this)).getReadableDatabase();
		Bundle extras = getIntent().getExtras(); 
		long quoteid = extras.getLong("id", -1);
		long storyid = extras.getLong("storyid", -1);
		quote = new Quote(db, quoteid);
		quote.storyid = storyid;
		this.element = quote;
		
		this.datetimetext = (TextView)this.findViewById(R.id.editquote_datetimetext);

		this.entityedit = (AutoCompleteTextView)this.findViewById(R.id.editquote_entityedit);
		this.noteedit = (EditText)this.findViewById(R.id.editquote_noteedit);
		this.contextedit = (EditText)this.findViewById(R.id.editquote_contextedit);

		this.createdtext = ((TextView)this.findViewById(R.id.editquote_created));
		this.lastmodifiedtext = ((TextView)this.findViewById(R.id.editquote_lastmodified));				
		
		/* Set Autocomplete for Entity */
		String[] entitieslist = (String[])entityAutocompletes().toArray(new String[0]);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, entitieslist);
		entityedit.setAdapter(adapter);
		
		if (!quote.isNew()) {
			this.syncWithObject();
		}
		
		updateDateDisplay();
		
		db.close();
	}
	
	public ArrayList<String> entityAutocompletes() {
		ArrayList<String> autocompletes = Quote.getAllEntities(this.db);
		
		// Examples if more stuff is needed:
		// http://www.higherpass.com/Android/Tutorials/Working-With-Android-Contacts/
		
        Cursor cur = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				autocompletes.add(name);
			}
		}
		
		return autocompletes;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Calendar c = Calendar.getInstance();
		if (quote.date != null) {
			c.setTime(quote.date);
		}
		
		if (id == DATEDIALOGID) {
			return new DatePickerDialog(
				this,
				dateSetListener,
				c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		}
		else if (id == TIMEDIALOGID) {
			return new TimePickerDialog(
				this, 
				timeSetListener, 
				c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
		}
		else if (id == CONFIRMEXITDIALOGID) {
			return this.createExitConfirmationDialog();
		}
				
		return null;
	}


	public void onClickActivateDatePicker(View v) {
		this.showDialog(DATEDIALOGID);
	}
	
	public void onClickActivateTimePicker(View v) {
		this.showDialog(TIMEDIALOGID);
	}
	
	public void onClickUnsetDate(View v) {
		this.quote.setDate(null);
		updateDateDisplay();
	}
	
	public void syncWithForm() {
		if (!quote.isNew() || !this.isFormBlank()) {
			this.quote.setQuote(this.noteedit.getText().toString());
			this.quote.setEntity(this.entityedit.getText().toString());
			this.quote.setContext(this.contextedit.getText().toString());
		}
		// Dates and times are set in the listeners
	}
	
	public boolean isFormBlank() {
		String formdata = noteedit.getText().toString() + entityedit.getText().toString() + contextedit.getText().toString();
		if (Util.isBlank(formdata) && quote.date == null) {
			return true;
		}
		return false;
	}
	
	public void saveElement() {
		if (this.quote.isEmpty(this.db)) {
			this.quote.hardDelete(this.db);
		}
		else {	
			this.quote.save(this.db);
		}	
	}
	
	private void updateDateDisplay() {
		String datetime = Util.snvl(Util.longDatePlusTimeFormat(quote.date), getString(R.string.editquote_nodate));
		this.datetimetext.setText(datetime);
	}
	
	
	public void syncWithObject() {
		this.noteedit.setText(this.quote.quote);
		this.entityedit.setText(this.quote.entity);
		this.contextedit.setText(this.quote.context);
		
		updateDateDisplay();
		
		if (quote.created != null) {
			createdtext.setText(getString(R.string.editquote_created) + " " + Util.longDatePlusTimeFormat(quote.created));
			createdtext.setVisibility(View.VISIBLE);
		}
		
		if (quote.lastmodified != null) {
			lastmodifiedtext.setText(getString(R.string.editquote_lastmodified) + " " + Util.longDatePlusTimeFormat(quote.lastmodified));
			lastmodifiedtext.setVisibility(View.VISIBLE);
		}		
	}	
}