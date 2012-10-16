package com.storiarc.mobile.activity;

import com.storiarc.mobile.*;
import com.storiarc.mobile.model.*;
import com.storiarc.mobile.util.*;

import android.view.*;
import android.widget.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.view.ViewGroup.LayoutParams;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.widget.AdapterView.OnItemClickListener;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.hardware.Camera;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;

import android.util.Log;

import java.util.*;
import java.io.File;

public class EditMeta extends Activity {

	private Story story;
	private SQLiteDatabase db;

	Date olddeadline; // for determining if we should update google calendar

	EditText edittitle;
	EditText editdescription;
	TextView deadlinedisplay;
	CheckBox completedcheckbox;
	Button savebtn, setdeadlinebtn, removedeadlinebtn;

	static final int DEADLINEDIALOGID = 0;
	static final int CONFIRMEXITDIALOGID = 1;
	static final int CAMERA_PIC_REQUEST = 1337;
	
    private DatePickerDialog.OnDateSetListener DateSetListener =
		new DatePickerDialog.OnDateSetListener() {
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				Date newdate = Util.datePickerToDate(story.deadline, year, monthOfYear+1, dayOfMonth); // +1 fixes off by one error
				story.setDeadline(newdate);
				deadlinedisplay.setText(Util.snvl(Util.longDateFormat(story.deadline), getString(R.string.editmeta_nodeadline)));
			}
		};	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editmeta);
		
		db = (SQLiteDatabase)(new DatabaseOpener(this)).getReadableDatabase();
		Bundle extras = getIntent().getExtras(); 
		long storyid = extras.getLong("storyid", -1L);
		this.story = new Story(db, storyid);
		this.olddeadline = this.story.deadline;
				
		this.edittitle = ((EditText)this.findViewById(R.id.editmeta_edittitle));
		this.editdescription = ((EditText)this.findViewById(R.id.editmeta_editdescription));		
		this.deadlinedisplay = ((TextView)this.findViewById(R.id.editmeta_deadlinedisplay));
		this.completedcheckbox = ((CheckBox)this.findViewById(R.id.editmeta_completedcheckbox));
		this.savebtn = ((Button)this.findViewById(R.id.editmeta_savebtn));
		this.removedeadlinebtn = ((Button)this.findViewById(R.id.editmeta_setnodeadlinebtn));
		this.setdeadlinebtn = ((Button)this.findViewById(R.id.editmeta_setdeadlinebtn));
						
		if (this.story.isComplete()) {
			this.completedcheckbox.setChecked(true);
			this.setEnabledDeadlineWidgets(false);
		}
		
		if (!story.isNew()) {
			this.findViewById(R.id.editmeta_settingsblock).setVisibility(View.VISIBLE);
		}
		
		syncWithObject();
		db.close();
	}
	
	@Override
	public void onStart() {
		super.onStart();
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

	public void onClickActivateDatePicker(View v) {
		this.showDialog(DEADLINEDIALOGID);
	}
	
	public void onClickNullifyDeadline(View v) {
		this.deadlinedisplay.setText(getString(R.string.editmeta_nodeadline));
		this.story.setDeadline(null);
	}
	
	public void onClickSaveAndContinue(View v) {
		boolean isnew = story.isNew();
		this.saveStory();
		
		if (isnew) {
			Intent launch = new Intent(this, Overview.class);
			launch.putExtra("storyid", story.id);
			this.startActivity(launch);
		}
		
		this.finish();
	}
	
	public void saveStory() {
		this.syncStoryToForm();

		story.lightweightSave(this.db);
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this); 
		boolean updatecal = settings.getBoolean("allow update calendar", false);
		
		if (updatecal) {
			try {
				Communicator c = new Communicator(this, db, story);
				c.setDeadlineInGoogleCalendar(this.olddeadline);
			}
			catch (Exception e) {
				//Log.w("Storiarc", "Could not update Google Calendar.");
			}
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {	
		if (id == DEADLINEDIALOGID) {
			Calendar cal = Calendar.getInstance();
			if (story.deadline != null) {
				cal.setTime(story.deadline);
			}
			return new DatePickerDialog(
				this,
				DateSetListener,
				cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
		}
		else if (id == CONFIRMEXITDIALOGID) {
			AlertDialog dialog;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCancelable(true)
						.setMessage(getString(R.string.confirmsave_message)) // For some reason, this is required to allow dynamic setting of the message
						.setPositiveButton(getString(R.string.confirmsave_positive), new DialogInterface.OnClickListener() {
						   public void onClick(DialogInterface dialog, int id) {
								EditMeta.this.saveStory();
								EditMeta.this.finish();
						   }
					   })
					   .setNeutralButton(getString(R.string.confirmsave_neutral), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}					   
					   })
					   .setNegativeButton(getString(R.string.confirmsave_negative), new DialogInterface.OnClickListener() {
						   public void onClick(DialogInterface dialog, int id) {
								EditMeta.this.finish();
						   }
					   });
				dialog = builder.create();
				return dialog;	
		}
		return null;
	}

	@Override
	public void onBackPressed() {	
		this.syncStoryToForm();
			
		if (this.story.isChanged()) {
			this.showDialog(CONFIRMEXITDIALOGID);
		}
		else {
			this.finish();
		}
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.editmetamenu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menuitem) {
		super.onOptionsItemSelected(menuitem);
	
		this.saveStory();

		Communicator c = new Communicator(this, db, story);

		switch (menuitem.getItemId()) {
			case R.id.editmetamenu_sendmenubtn:
				c.launchSendEmail();
				break;
			default:
				break;
		}
		
		return true;
	}
	
	public void toggleCompletionStatus(View v) {
		toggleCompletionStatus();
	}
	
	public void toggleCompletionStatus() {
		if (!story.isComplete()) {
			story.setStatus(1);
			this.setEnabledDeadlineWidgets(false);
		}
		else {
			story.setStatus(0);
			this.setEnabledDeadlineWidgets(true);
		}		
	}
	
	private void setEnabledDeadlineWidgets(boolean enabled) {
		this.deadlinedisplay.setEnabled(enabled);
		this.removedeadlinebtn.setEnabled(enabled);
		this.setdeadlinebtn.setEnabled(enabled);	
	}	
				
	private void syncStoryToForm() {	
		this.story.setTitle(edittitle.getText().toString());
		this.story.setDescription(editdescription.getText().toString());
		//story.deadline is set in the date listener
		int newstatus = this.completedcheckbox.isChecked() ? 1 : 0;
		this.story.setStatus(newstatus);
	}
	
	private void syncWithObject() {
			
		edittitle.setText(story.title);
		editdescription.setText(story.description);
		
		String deadlinetext = Util.snvl(Util.longDateFormat(story.deadline), getString(R.string.editmeta_nodeadline));
		deadlinedisplay.setText(deadlinetext);		
	}
}