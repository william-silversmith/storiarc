package com.storiarc.mobile.activity;

import com.storiarc.mobile.model.Story;
import com.storiarc.mobile.model.Communicator;
import com.storiarc.mobile.*;
import com.storiarc.mobile.activity.*;
import com.storiarc.mobile.util.*;

import android.view.*;
import android.content.*;

import android.view.ContextMenu.ContextMenuInfo;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.preference.PreferenceManager;
import android.graphics.Color;
import android.util.Log;

import java.util.*;

public class StorySelector extends Activity {
	
	/* Lifecycle */
	
	private ListView storylister;
	private	int title_displaylimit;
	private int description_displaylimit;

	public static final int DELETEDIALOGID = 0;
	
	private Story selectedstory;
	
	static final Comparator<Story> story_cmp = new Comparator<Story>() {
			
			/*	Sort by deadline, then lexicographically. 
				Deadlines win over no deadlines.
			*/
			public int compare(Story s1, Story s2) {
				if (s1.status > s2.status) {
					return 1; // 1 is complete, 0 incomplete
				}
				else if (s2.status > s1.status) {
					return -1;
				}
				
				if (s1.deadline != null && s2.deadline != null) {
					if (s1.isEscalated() && !s2.isEscalated()) {
						return -1;
					}
					else if (!s1.isEscalated() && s2.isEscalated()) {
						return 1;
					}
				
					int ret = -s1.deadline.compareTo(s2.deadline);
					if (ret != 0) {
						return ret;
					} 
					return s1.title.compareTo(s2.title);
				}
				else if (s1.deadline != null) {
					return -1;
				}
				else if (s2.deadline != null) {
					return 1;
				}
				else {
					return 0;
				}
			}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setTitle(getString(R.string.storyselector_name));
		
		this.title_displaylimit = 32;
		this.description_displaylimit = 100;
		
		setContentView(R.layout.storyselector);
		
		storylister = (ListView)this.findViewById(R.id.storyselectorlistview);
		storylister.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> a, View v, int position, long id) {
					selectedstory = (Story)storylister.getItemAtPosition(position);
					launchStory(selectedstory);
				}
			}
		);

		storylister.setEmptyView(findViewById(R.id.storyselector_empty));
		registerForContextMenu(storylister);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		int populated = populateStories();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.storyselectorcontextmenu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem menuitem) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuitem.getMenuInfo();
		
		if (info != null) {
			selectedstory = (Story) Util.nvl(storylister.getAdapter().getItem(info.position), selectedstory);
		}
		
		SQLiteDatabase db = (SQLiteDatabase)(new DatabaseOpener(this)).getWritableDatabase();
		Communicator c = new Communicator(this, db, selectedstory);
		
		switch (menuitem.getItemId()) {
			case R.id.storyselectorcontextmenu_view:
				launchStory(selectedstory);
				break;
			case R.id.storyselectorcontextmenu_delete:
				showDialog(DELETEDIALOGID);
				break;
			case R.id.storyselectorcontextmenu_copy:
				c.copyToClipboard(selectedstory);
				break;
			case R.id.storyselectorcontextmenu_editmeta:
				editMetaInformation();
				break;
			case R.id.storyselectorcontextmenu_send:
				c.launchSendEmail();
				break;
		}
		
		db.close();
		
		return true;
	}
	
	public void editMetaInformation() {
		Intent launch = new Intent(this, EditMeta.class);
		launch.putExtra("storyid", selectedstory.id);
		this.startActivity(launch);	
	}

	public void onClickCreateStory(View v) {
		launchStory(null);
	}
	
	public void onClickGenerateExampleStories(View v) {
		SQLiteDatabase db = (SQLiteDatabase)(new DatabaseOpener(this)).getReadableDatabase();	
		Generator g = new Generator(this, db);
		g.generateExampleStories();
		db.close();
		
		populateStories();
	}
	
	private void launchStory(Story story) {
		Intent launch;
		if (story != null) {
			launch = new Intent(this, Overview.class);
			launch.putExtra("storyid", story.id);
		}
		else {
			launch = new Intent(this, EditMeta.class);
			launch.putExtra("storyid", -1L);
		}
		this.startActivity(launch);	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.storyselectormenu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menuitem) {
		super.onOptionsItemSelected(menuitem);
	
		switch (menuitem.getItemId()) {
			case R.id.storyselector_createstorymenubtn:
				launchStory(null);
				break;
			case R.id.storyselector_settingsmenubtn:
				Intent prefsintent = new Intent(this, StoriarcPreferences.class);
				this.startActivity(prefsintent);
				break;
			case R.id.storyselector_aboutmenubtn:
				Intent aboutintent = new Intent(this, About.class);
				this.startActivity(aboutintent);
				break;
			case R.id.storyselector_searchmenubtn:
				onSearchRequested();
				break;
			default:
				break;
		}
		
		return true;
	}
	
	private int populateStories() {
		SQLiteDatabase db = (SQLiteDatabase)(new DatabaseOpener(this)).getWritableDatabase();		
		ArrayList<Story> stories = Story.getAllStories(db);
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this); 
		boolean showcompleted = settings.getBoolean("show completed story", true);
		boolean firstrun = settings.getBoolean("firstrun", true);
		
		if (firstrun) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("firstrun", false);
			editor.commit();
			
			//Generator gen = new Generator(this, db);
			//gen.generateExampleStories();	
		}
		
		ArrayList<Story> filteredstories = new ArrayList<Story>();
		filteredstories.ensureCapacity(stories.size());

		for (Story s : stories) {
			if (showcompleted || !s.isComplete()) {
				filteredstories.add(s);
			}
		}
		
		Collections.sort(filteredstories, story_cmp);
		storylister.setAdapter(new StoryAdapter(filteredstories));
		db.close();
		
		return filteredstories.size();
	}	
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {	
		if (selectedstory == null) { return; }
	
		switch(id) {
			case DELETEDIALOGID:
				if (!Util.isBlank(selectedstory.title)) {
					((AlertDialog)dialog).setMessage("Delete \"" + selectedstory.title + "\"?");
				}
				else {
					((AlertDialog)dialog).setMessage("Delete this untitled story?");
				}
				break;
			default:
				break;
		}
	}
	
	protected Dialog onCreateDialog(int id) {
		AlertDialog dialog;
		switch(id) {
			case DELETEDIALOGID:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCancelable(true)
						.setMessage("") // For some reason, this is required to allow dynamic setting of the message
						.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
						   public void onClick(DialogInterface dialog, int id) {
								SQLiteDatabase db = (SQLiteDatabase)(new DatabaseOpener(StorySelector.this)).getWritableDatabase();
								selectedstory.softDelete(db);
								selectedstory.save(db);
								db.close();
								
								selectedstory = null;
								
								populateStories();
						   }
					   })
					   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						   public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
						   }
					   });
				dialog = builder.create();
				break;
			default:
				dialog = null;
		}
		return dialog;
	}	
	
	private class StoryAdapter extends ArrayAdapter<Story>	{
	
        public StoryAdapter(ArrayList<Story> stories) {
			super(StorySelector.this, R.layout.storylistitem, stories);			
        }
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Story story = this.getItem(position);
			View row = convertView;
			
			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.storylistitem, parent, false);
			}
			TextView titleview = (TextView)row.findViewById(R.id.storytitle);
			TextView descriptionview = (TextView)row.findViewById(R.id.storydescription);
			TextView deadline = (TextView)row.findViewById(R.id.storyselector_deadline);
			TextView completed = (TextView)row.findViewById(R.id.storyselector_completed);
			
			// Title Display
			
			String titletext = Util.snvl(story.title, getString(R.string.untitledstory) + " " + story.id);
			titleview.setText(titletext);
			
			// Deadline display
			
			Resources res = getResources();
			int completecolor; 

			if (story.isComplete()) {
				completecolor = res.getColor(R.color.completed);
				completed.setVisibility(View.VISIBLE);
				
				deadline.setTextColor(Color.BLACK);
			}
			else {
				deadline.setTextColor(getResources().getColor(R.color.deadline));
			
				completed.setVisibility(View.GONE);

				if (story.isEscalated()) {
					completecolor = res.getColor(R.color.escalated);
				}
				else {
					completecolor = res.getColor(android.R.color.transparent);
				}
			}
			row.setBackgroundColor(completecolor);									
			
			String deadlinetext = Util.snvl(Util.longDateFormat(story.deadline), getString(R.string.storyselector_nodeadline));
			deadline.setText(deadlinetext);
						
			// Description Display
			
			String descriptiontext = Util.snvl(story.description, "");
			if (descriptiontext.length() > description_displaylimit) {
				descriptiontext = descriptiontext.substring(0, description_displaylimit) + "...";
			}
			descriptiontext = descriptiontext.trim();
			descriptiontext = Util.snvl(descriptiontext, getString(R.string.nodescription));
			descriptionview.setText(descriptiontext);
			
						
			if (story.isComplete()) {
				completed.invalidate();
				deadline.invalidate();
			}
			
			return row;			
		}
	}
}
