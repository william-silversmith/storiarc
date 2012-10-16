package com.storiarc.mobile.model;

import com.storiarc.mobile.R;
import com.storiarc.mobile.model.*;
import com.storiarc.mobile.util.*;

import android.text.ClipboardManager;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.content.*;
import android.net.Uri;

import android.util.Log;

import java.net.URL;
import java.util.*;

public class Communicator {

	Context ctx;
	Story story;
	SQLiteDatabase db;

	final Uri eventsUri = Uri.parse("content://com.android.calendar/events");
	final Uri reminderUri = Uri.parse("content://com.android.calendar/reminders");

	public Communicator(Context ctx, SQLiteDatabase db, Story s) {
		this.ctx = ctx;
		this.db = db;
		this.story = s;
	}
	
	public Communicator(Context ctx, SQLiteDatabase db, long storyid) {
		this.ctx = ctx;
		this.db = db;
		this.story = new Story(this.db, storyid);
	}
	
	public Intent getEmailIntent() {
		return this.getEmailIntent("","");
	}
	
	public String getEmailBody() {
		ArrayList<DatabaseInterface> textelements = new ArrayList<DatabaseInterface>();
		textelements.addAll(this.story.getNotes(this.db));
		textelements.addAll(this.story.getQuotes(this.db));
		Collections.sort(textelements, Util.created_cmp_desc);
		
		String body = Util.scnvl(this.story.description, this.story.description + "\n\n", "");
		
		String longdate = "";
		int count = 1;
		for (DatabaseInterface elem : textelements) {
			String newtext = "";		
			String newdate = Util.longDateFormat(elem.created);
			
			if (!longdate.equals(newdate)) {
				newtext += newdate + "\n\n";
				longdate = newdate;
				//newtext = "Created " + newtext;
			}
			
			newtext += "(" + Util.timeFormat(elem.created) + ") ";
			if (elem.getClass() == Note.class) {
				newtext += formatNote((Note)elem);
			}
			else if (elem.getClass() == Quote.class) {
				newtext += formatQuote((Quote)elem);
			}
			
			body += newtext + "\n\n";
			count++;
		}
		
		body += "\n" + ctx.getString(R.string.email_footer);
		
		return body;
	}
	
	
	public String formatNote(Note note) {
		return note.text.trim();
	}
	
	public String formatQuote(Quote quote) {
		String text = "";
		if (!Util.isBlank(quote.quote)) {
			 text += "\"" + quote.quote + "\"";
	 		 text += "\n\n- " + Util.snvl(quote.entity, "an unspecified entity").trim();
		}
		else {
			text += Util.snvl(quote.entity, "an unspecified entity").trim() + " communicated something of importance, but it was not recorded.";
		}
		
		text += "\n  " + Util.snvl(Util.APDatePlusTimeFormat(quote.date), "Date Unknown");

		if (!Util.isBlank(quote.context)) {
			text += "\n\n" + quote.context;
		}
		
		return text;
	}
	
	public Intent getEmailIntent(String subject, String toemailaddr) {
	
		ctx.getString(R.string.email_footer);
	
		Intent emailIntent = new Intent(Intent.ACTION_SEND);  
		//emailIntent.setType("message/rfc822");
		emailIntent.setType("text/plain"); // twitter likes this one
		if (Util.isBlank(subject)) {
			subject = Util.snvl(story.title, this.ctx.getString(R.string.email_defaultsubject));
		}
		
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		
		if (!Util.isBlank(toemailaddr)) {
			emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {toemailaddr});
		}
		
		String body = this.getEmailBody();		
		emailIntent.putExtra(Intent.EXTRA_TEXT, body);
		
		
		/*
		emailIntent.setType("jpeg/image");
		
		ArrayList<Image> imgs = story.getImages(this.db);
		Image i = imgs.get(0);
		Log.w("image uri", i.uri.toString());
		
		emailIntent.putExtra(Intent.EXTRA_STREAM, i.uri);
		*/
		
		return emailIntent;	
	}
	
	public void launchTweet(DatabaseInterface elem) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx); 
		boolean allowbranding = settings.getBoolean("allow tweet branding", false);	
	
		Intent tweetIntent = new Intent(Intent.ACTION_SEND); 
		//tweetIntent.setType("application/twitter");
		tweetIntent.setType("text/plain");
		
		String tweet = "";
		if (elem.getClass() == Note.class) {
			tweet = formatCopyNote((Note)elem); 
		}
		else if (elem.getClass() == Quote.class) {
			tweet = formatCopyQuote((Quote)elem);
		}

		if (allowbranding) {
		  tweet = "#storiarc " + tweet;
		}

		tweetIntent.putExtra(Intent.EXTRA_SUBJECT, tweet); 
		ctx.startActivity(Intent.createChooser(tweetIntent, null));
	}
	
	public void launchSendEmail() {
		Intent emailIntent = this.getEmailIntent();
		ctx.startActivity(Intent.createChooser(emailIntent, ctx.getString(R.string.sendemailwith)));
	}
	
	public void launchPostPosterous() {
		Intent emailIntent = this.getEmailIntent(story.title, "post@posterous.com");
		ctx.startActivity(Intent.createChooser(emailIntent, ctx.getString(R.string.sendemailwith))); 
	}	
	
	public void launchFeedback() {
		Intent emailIntent = new Intent(Intent.ACTION_SEND); 
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Storiarc Feedback");
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"feedback@storiarc.com"});		
		emailIntent.setType("message/rfc822");
		ctx.startActivity(emailIntent); 		
	}
	
	public void dropToDropbox() {
	
	}
	
	public void copyToClipboard(Story s) {
		String cliptext = Util.snvl(story.title, "Untitled Story " + story.id) + Util.scnvl(story.description, ": " + story.description, "");
		ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(ctx.CLIPBOARD_SERVICE);
		clipboard.setText(cliptext);
	}
	
	public void copyToClipboard(DatabaseInterface elem) {
		String cliptext = "";
		if (elem.getClass() == Note.class) {
			cliptext = formatCopyNote((Note)elem);
		}
		else if (elem.getClass() == Quote.class) {
			cliptext = formatCopyQuote((Quote)elem);
		}
		
		if (!Util.isBlank(cliptext)) {
			ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(ctx.CLIPBOARD_SERVICE);
			clipboard.setText(cliptext);
		}
	}
	
	public static String formatCopyNote(Note note) {
		String text = note.text.trim();
		return text;		
	}
	
	public static String formatCopyQuote(Quote quote) {
		String text = "";
		if (!Util.isBlank(quote.quote)) {
			 text += "\"" + quote.quote + "\"";
	 		 text += " - " + Util.snvl(quote.entity, "unknown").trim();
		}
		
		text += Util.scnvl(quote.date, " ("+Util.longDatePlusTimeFormat(quote.date)+")", ""); 
		
		if (!Util.isBlank(quote.context)) {
			text += " \n\n" + quote.context;
		}
		
		return text;	
	}
	
	
	/* Android Calendar Hidden API
	 * WARNING: Unstable, test for regressions
	 * See: http://www.google.com/codesearch/p?hl=en&sa=N&cd=3&ct=rc#uX1GffpyOZk/core/java/android/provider/Calendar.java
	 */	
	public boolean setDeadlineInGoogleCalendar(Date olddate) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx); 
		String calendarid = settings.getString("calendarid calendar", null);
		boolean allowreminders = settings.getBoolean("allow reminder calendar", false);
		
		if (calendarid == null) {
			return false;
		}
		
		SQL eventidquery = new SQL(db, "SELECT eventid FROM googlecalendar where storyid = " + story.id);
		String eventid = eventidquery.singleValue();
		eventidquery.close();
		
		// optimization to prevent updating google repeatedly
		if (!story.deadline.equals(olddate) || eventid == null) { 
			ContentValues event = eventDefaultValues();	
			
			if (eventid == null) {
				if (!story.isComplete()) {
					insertEventInGoogleCalendar(event, allowreminders);
				}
			}
			else {
				int affected;
				Uri updateEventUri = Uri.withAppendedPath(eventsUri, eventid);
				
				if (story.isComplete() || story.deadline == null) {
					affected = ctx.getContentResolver().delete(updateEventUri, null, null);
					db.execSQL("UPDATE googlecalendar SET eventid = null where storyid = " + story.id);					
				}
				else {
					affected = ctx.getContentResolver().update(updateEventUri, event, null, null);
					
					if (affected == 0) {
						insertEventInGoogleCalendar(event, allowreminders);
					}
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	private void insertEventInGoogleCalendar(ContentValues event, boolean allowreminders) {	
		// Add event
		Uri url = ctx.getContentResolver().insert(this.eventsUri, event);
		String eventid = url.getLastPathSegment();			
		
		// Add reminder
		if (allowreminders) {	
			ctx.getContentResolver().insert(this.reminderUri, reminderDefaultValues(eventid));
		}					
			
		// Update internal tracking
		ContentValues calvals = new ContentValues();
		calvals.put("eventid", eventid);
		
		int affected = db.update("googlecalendar", calvals, "storyid = " + story.id, null);
		if (affected == 0) {
			calvals.put("storyid", story.id);
			db.insert("googlecalendar", "eventid", calvals);
		}	
	}
	
	private ContentValues eventDefaultValues() {
		ContentValues event = new ContentValues();
		event.put("title", story.title);
		event.put("description", story.description);
		event.put("dtstart", story.deadline.getTime());
		event.put("dtend", story.deadline.getTime());
		event.put("allDay", 1);   // 0 for false, 1 for true
		event.put("transparency", 1); // No Scheduling conflicts
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx); 
		String calendarid = settings.getString("calendarid calendar", null);
		String privacylevel = settings.getString("visibility calendar", "0"); // 0 is default level
		boolean allowreminders = settings.getBoolean("allow reminder calendar", false);
		
		event.put("calendar_id", calendarid);
		event.put("visibility", privacylevel);
		event.put("hasAlarm", allowreminders);

		return event;
	}	
	
	private ContentValues reminderDefaultValues(String eventid) {
		ContentValues remindervals = new ContentValues();
		remindervals.put("minutes", 60); // one hour warning
		remindervals.put("method", 0); // default
		remindervals.put("event_id", eventid);
		
		return remindervals;
	}
}



