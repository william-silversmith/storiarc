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

public abstract class EditDBI extends Activity {

	protected DatabaseInterface element;
	protected SQLiteDatabase db;
	
	protected static final int CONFIRMEXITDIALOGID = 100000;
	
	public abstract void syncWithForm();
	public abstract void saveElement();
	public abstract void syncWithObject();	
	
	@Override
	public void onStart() {
		super.onStart();
		db = (SQLiteDatabase)(new DatabaseOpener(this)).getWritableDatabase();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		db = (SQLiteDatabase)(new DatabaseOpener(this)).getWritableDatabase();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		db.close();
	}
	
	@Override
	public void onBackPressed() {	
		syncWithForm();
		
		if (this.element.isChanged()) {
			this.showDialog(CONFIRMEXITDIALOGID);
		}
		else {
			this.finish();
		}
	}
		
	protected Dialog createExitConfirmationDialog() {
		AlertDialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true)
				.setMessage(getString(R.string.confirmsave_message)) // For some reason, this is required to allow dynamic setting of the message
				.setPositiveButton(getString(R.string.confirmsave_positive), new DialogInterface.OnClickListener() {
				   public void onClick(DialogInterface dialog, int id) {
						EditDBI.this.syncWithForm();
						EditDBI.this.saveElement();
						EditDBI.this.finish();
				   }
			   })
			   .setNeutralButton(getString(R.string.confirmsave_neutral), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}					   
			   })
			   .setNegativeButton(getString(R.string.confirmsave_negative), new DialogInterface.OnClickListener() {
				   public void onClick(DialogInterface dialog, int id) {
						EditDBI.this.finish();
				   }
			   });
		return builder.create();
	}

	public void onClickSaveAndContinue(View v) {
		this.syncWithForm();
		this.saveElement();
		this.finish();
	}
}