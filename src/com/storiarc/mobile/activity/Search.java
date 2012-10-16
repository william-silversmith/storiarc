package com.storiarc.mobile.activity;

import com.storiarc.mobile.*;
import com.storiarc.mobile.model.*;
import com.storiarc.mobile.util.*;

import android.view.*;
import android.widget.*;
import android.app.*;
import android.os.*;
import android.view.ViewGroup.LayoutParams;

import android.view.ContextMenu.ContextMenuInfo;
import android.net.Uri;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.MediaStore;
import android.hardware.Camera;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore.Images.Thumbnails;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;

import android.util.Log;

import java.util.*;
import java.io.File;

public class Search extends ListActivity {
	
	SQLiteDatabase db;
	int totalsize;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		totalsize = 0;
		
		LinearLayout infobarlayout = (LinearLayout)getLayoutInflater().inflate(R.layout.searchinfobar, getListView(), false);
		TextView infobar = (TextView)infobarlayout.findViewById(R.id.search_infobar);
		getListView().addHeaderView(infobarlayout);
		
		db = (SQLiteDatabase)(new DatabaseOpener(this)).getReadableDatabase();
		
		Intent intent = getIntent();
		String query = "";
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
		  query = intent.getStringExtra(SearchManager.QUERY).trim();
		}
		
		HashMap<String, String[]> tablecols = new HashMap<String, String[]>();
		
		if (!Util.isBlank(query)) {			
			tablecols.put("story", new String[] {"title","description"});
			tablecols.put("note", new String[] {"text"});
			tablecols.put("quote", new String[] {"entity", "quote", "context"});

			ArrayList<Resultant> results = getMatchingItems(tablecols, query);
			
			infobar.setText("Displaying " + results.size() + " of " + totalsize + " results.");
			setListAdapter(new SearchAdapter(results));
		}
		else {
			infobar.setText("No query was specified.");		
			setListAdapter(new SearchAdapter(new ArrayList<Resultant>()));
		}
	}
	
	public ArrayList<Resultant> getMatchingItems(HashMap<String, String[]> tablecols, String query) {
		ArrayList<Resultant> results = new ArrayList<Resultant>();
		
		for (String table : (Set<String>)tablecols.keySet()) {		
			String[] cols = (String[])tablecols.get(table);
			
			for (String col : cols) {
				String storyidcol = "storyid";
			
				if (table.equals("story")) {
					storyidcol = "id";
				}
				
				String basequery = "from " + table + " where deleted is null and " + col + " like '%" + query + "%'";
				SQL sql = new SQL(db, "select " + storyidcol + ", " + col + ", lastmodified, created, deleted " + basequery + " limit 25");
				SQL countsql = new SQL(db, "select count(*) " + basequery);
				
				this.totalsize += countsql.singleIntValue();
				ArrayList<HashMap<String, Object>> values = sql.hashValues();
				
				sql.close();
				countsql.close();
				
				if (values == null) {
					continue;
				}
				
				for (HashMap<String, Object> row : values) {
					Resultant result = new Resultant((Long)row.get(storyidcol), (String)row.get(col));
					result.created = Util.isoToDate((String)row.get("created"));
					result.lastmodified = Util.isoToDate((String)row.get("lastmodified"));
					result.deleted = Util.isoToDate((String)row.get("deleted"));					
					results.add(result);
				}				
			}
		}
		
		return results;
	 } 
	
	@Override
	public void onPause() {
		super.onPause();
		db.close();
		this.finish();
	}
	
	@Override
	protected void onListItemClick (ListView l, View v, int position, long id)	{
		long storyid = v.getId();
		
		if (storyid >= 0) {
			Intent overviewintent = new Intent(this, Overview.class);
			overviewintent.putExtra("storyid", storyid);
			this.startActivity(overviewintent);
			this.finish();
		}
	}
	
	public class Resultant {
		public long id;
		public String match;
		public Date lastmodified;
		public Date created;
		public Date deleted;
		
		public Resultant(Long id, String match) {
			this.id = id.longValue();
			this.match = match;
		}
	}
	
	private class SearchAdapter extends ArrayAdapter {
				
        public SearchAdapter(ArrayList<Resultant> matches) {
			super(Search.this, R.layout.dummy, matches);
        }
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Resultant result = (Resultant)this.getItem(position);
			View row = convertView;
			
			if (row == null) {
				row = generateNewRow(parent, result);
			}
			
			// This fixes a bug where the row won't be null because it's reused
			// so when e.g. a note changes to a quote, the right fields don't exist
			if (populateRow(row, result) == false) {
				row = generateNewRow(parent, result);				
				populateRow(row, result);
			}
			
			return row;
		}
		
		private View generateNewRow(ViewGroup parent, Resultant result) {
			return getLayoutInflater().inflate(R.layout.searchitem, parent, false);
		}
		
		private boolean populateRow(View row, Resultant result) {
			TextView data = (TextView)row.findViewById(R.id.search_result);
			TextView title = (TextView)row.findViewById(R.id.search_titleresult);
			TextView timestamp = (TextView)row.findViewById(R.id.search_timestamp);
			
			data.setText(result.match);
			if (result.id >= 0) {
				SQL titlesql = new SQL(db, "select title from story where id = " + result.id);
				title.setText(titlesql.singleValue());
				titlesql.close();
			}
			else {
				title.setVisibility(View.GONE);
			}
			
			if (result.deleted != null) {
				String deletedtime = Util.longDatePlusTimeFormat(result.deleted);
				timestamp.setText("Deleted " + deletedtime);
			}
			else if (result.lastmodified != null) {
				String lastmodifiedtime = Util.longDatePlusTimeFormat(result.lastmodified);
				timestamp.setText(lastmodifiedtime);
			}
			else {
				timestamp.setVisibility(View.GONE);
			}
			
			row.setId((int)result.id);
			
			return true;
		}
		
	}	
}