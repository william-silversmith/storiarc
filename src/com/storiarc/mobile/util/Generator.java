package com.storiarc.mobile.util;

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

public class Generator {

	Context ctx;
	SQLiteDatabase db;
	Random rand;
	
	public Generator(Context ctx, SQLiteDatabase db) {
		this.ctx = ctx;
		this.db = db;
		this.rand = new Random((new Date()).getTime());
	}
	
	public void generateExampleStories() {	
		int choice = rand.nextInt() % 2;
	
		if (choice == 0) {
			ex1();
		}
		else {
			ex2();
		}
	}
	
	public void ex1() {	
		Story s = new Story(db);
		s.title = "An Example Story";
		s.deadline = Util.addDays(new Date(), this.rand.nextInt(13));
		s.description = "An example description.";
		s.status = this.rand.nextInt() % 2;
		s.save(db);
		
		Note n1 = new Note(db);
		n1.storyid = s.id;
		n1.text = "A note. Touch me!";
		n1.save(db);
		
		Quote q1 = new Quote(db);
		q1.entity = "Quoted Entity";
		q1.date = new Date();
		q1.quote = "A quote. Touch me!";
		q1.context = "Some context.";
		q1.storyid = s.id;
		q1.save(db);
	}
	
	public void ex2() {
		Story s = new Story(db);
		s.title = "Storiarc Launches";
		s.deadline = Util.isoToDate("2011-04-20 22:58:32.012");
		s.description = "A story tracking app that helps you in the field.";
		s.status = this.rand.nextInt() % 2;
		s.save(db);
		
		Note n1 = new Note(db);
		n1.storyid = s.id;
		n1.text = "Don\'t think, just download!";
		n1.save(db);
		
		Quote q1 = new Quote(db);
		q1.entity = "Issac Newton";
		q1.date = null;
		q1.quote = "...as fundemental as gravitation.";
		q1.context = "While being parodied.";
		q1.storyid = s.id;
		q1.save(db);	
	}
}