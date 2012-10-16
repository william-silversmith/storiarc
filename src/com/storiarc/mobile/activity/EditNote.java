package com.storiarc.mobile.activity;

import com.storiarc.mobile.*;
import com.storiarc.mobile.model.*;
import com.storiarc.mobile.util.*;

import android.view.*;
import android.widget.*;
import android.content.*;
import android.app.*;

import android.os.Bundle;
import android.app.DatePickerDialog.OnDateSetListener;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

public class EditNote extends EditDBI {

	private Note note;

	EditText notetext;
	TextView createdtext;
	TextView lastmodifiedtext;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editnote);
		
		db = (SQLiteDatabase)(new DatabaseOpener(this)).getReadableDatabase();
		Bundle extras = getIntent().getExtras(); 
		long noteid = extras.getLong("id", -1);
		long storyid = extras.getLong("storyid", -1);
		note = new Note(db, noteid);
		note.storyid = storyid;
		this.element = note;

		this.notetext = ((EditText)this.findViewById(R.id.editnote_edit));
		this.createdtext = ((TextView)this.findViewById(R.id.editnote_created));
		this.lastmodifiedtext = ((TextView)this.findViewById(R.id.editnote_lastmodified));		
		
		if (!note.isNew()) {
			this.syncWithObject();
		}
		
		db.close();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == CONFIRMEXITDIALOGID) {
			return createExitConfirmationDialog();
		}
				
		return null;
	}
	
	public void syncWithForm() {	
		if (!note.isNew() || !Util.isBlank(notetext.getText().toString())) {
			note.setText(notetext.getText().toString());
		}
	}
	
	public void saveElement() {
		if (!note.isEmpty(this.db) && note.isChanged()) {
			note.save(this.db);
		}
		else if (note.isEmpty(this.db) && !note.isNew()) {
			note.hardDelete(this.db);
		}		
	}
			
	public void syncWithObject() {
		notetext.setText(note.text);
		
		if (note.created != null) {
			createdtext.setText(getString(R.string.editnote_created) + " " + Util.longDatePlusTimeFormat(note.created));
			createdtext.setVisibility(View.VISIBLE);
		}
		
		if (note.lastmodified != null) {
			lastmodifiedtext.setText(getString(R.string.editnote_lastmodified) + " " + Util.longDatePlusTimeFormat(note.lastmodified));
			lastmodifiedtext.setVisibility(View.VISIBLE);
		}		
	}	
}