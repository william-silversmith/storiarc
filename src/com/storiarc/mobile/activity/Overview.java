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

public class Overview extends Activity {

	private Story story;
	private SQLiteDatabase db;

	ListView overviewlist;
	View metaview;
	
	static final int CAMERA_PIC_REQUEST = 1337;
	static final int DELETEDIALOGID = 0;
	
	static final int METAPOSITION = 0;
	
	public DatabaseInterface selectedelement;
	public Recording currentrecording;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.overview);
		
		db = (SQLiteDatabase)(new DatabaseOpener(this)).getReadableDatabase();
		Bundle extras = getIntent().getExtras(); 
		long storyid = extras.getLong("storyid", -1L);
		story = new Story(db, storyid);
		
		overviewlist = (ListView)this.findViewById(R.id.overview_elements);
		registerForContextMenu(overviewlist);
		
		overviewlist.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> a, View v, int position, long id) {
					DatabaseInterface elem = (DatabaseInterface)overviewlist.getItemAtPosition(position);
					Overview.this.viewElement(elem, position);
				}
			}
		);
				
		if (story.isComplete()) {
			// Nothing yet
		}
		
		metaview = getLayoutInflater().inflate(R.layout.overviewmeta, overviewlist, false);		
		overviewlist.addHeaderView(metaview, null, true);
		
		View textbar = getLayoutInflater().inflate(R.layout.overviewtextbar, overviewlist, false);
		overviewlist.addHeaderView(textbar, null, false);		
		
		View elementcreator = getLayoutInflater().inflate(R.layout.overviewaddelement, overviewlist, false);
		overviewlist.addHeaderView(elementcreator, null, false);
		
		db.close();
	}
	
	private void syncWithObject() {
		String desctext = Util.snvl(this.story.description, getString(R.string.nodescription)).trim();
	
		((TextView)metaview.findViewById(R.id.overviewmeta_title)).setText(Util.snvl(this.story.title, getString(R.string.untitledstory) + " " + this.story.id));
		((TextView)metaview.findViewById(R.id.overviewmeta_description)).setText(desctext);
		String deadlinetxt = Util.snvl(Util.longDateFormat(story.deadline), getString(R.string.overview_nodeadline));
		TextView deadlineview = (TextView)metaview.findViewById(R.id.overviewmeta_deadline);
		deadlineview.setText(deadlinetxt);

		ArrayList<DatabaseInterface> elems = this.story.getAllElements(this.db);
		Collections.sort(elems, Util.created_cmp_asc);
		overviewlist.setAdapter(new ElementAdapter(elems));
	}	
	
	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		db = (SQLiteDatabase)(new DatabaseOpener(this)).getWritableDatabase();
		this.story.load(this.db);
		syncWithObject();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		this.syncStoryToForm();
		if (story.isEmpty(this.db) == false || story.id != -1L) {
			story.lightweightSave(this.db);
		}
		db.close();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		if (((AdapterContextMenuInfo)menuInfo).position == METAPOSITION) {
			inflater.inflate(R.menu.overviewmetacontextmenu, menu);
			return;
		}
		inflater.inflate(R.menu.overviewcontextmenu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem menuitem) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuitem.getMenuInfo();
		
		if (info != null) {
			selectedelement = (DatabaseInterface)overviewlist.getAdapter().getItem(info.position);
		}
		else {
			return false;
		}
		
		//SQLiteDatabase db = (SQLiteDatabase)(new DatabaseOpener(this)).getWritableDatabase();
		Communicator c = new Communicator(this, this.db, this.story);
		
		switch (menuitem.getItemId()) {
			case R.id.overviewcontextmenu_viewmeta:
				editMetaInformation();
				break;
			case R.id.overviewcontextmenu_copy:
				c.copyToClipboard(selectedelement);
				break;
			case R.id.overviewcontextmenu_copymeta:
				c.copyToClipboard(this.story);
				break;
			case R.id.overviewcontextmenu_view:
				viewElement(selectedelement);
				break;
			case R.id.overviewcontextmenu_delete:
				showDialog(DELETEDIALOGID);
				break;
			case R.id.overviewcontextmenu_send:
				c.launchSendEmail();
				break;
			case R.id.overviewcontextmenu_tweet:
				c.launchTweet(selectedelement);
				break;
		}
		
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.overviewmenu, menu);
			
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menuitem) {
		super.onOptionsItemSelected(menuitem);
	
		syncStoryToForm();
		story.lightweightSave(this.db);
	
		Communicator c = new Communicator(this, this.db, this.story);
	
		switch (menuitem.getItemId()) {
			case R.id.overview_addnotebtn:
				launchNote();
				break;
			case R.id.overview_addquotebtn:
				launchQuote();
				break;
			/*case R.id.overview_addimagebtn:
				addImage();
				break;*/
			case R.id.overview_sendmenubtn:	
				c.launchSendEmail();
				break;
			case R.id.overview_editmetabtn:	
				editMetaInformation();
				break;
			default:
				break;
		}
		
		return true;
	}
	
	public void addImage(View v) {
		addImage();
	}
	
	// NEEDS WORK
	public void addImage() {
		if (Util.externalStorageWritable()) {
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			Image img = new Image(db);
			img.setStory(story);
			currentrecording = img;
			
			String filename = Util.snvl(story.title + "_","")                  + story.id + "_" + img.count(db) + ".jpg";
			File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);
			img.uri = Uri.fromFile(file);
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, img.uri);
			startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST); 
		}
		else {
			new AlertDialog.Builder(this)
				.setMessage(getString(R.string.externalstorage_nowrite))
				.setNegativeButton(getString(R.string.errormessageunderstood), null)
				.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					return;
				}}).show();
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		SQLiteDatabase tmpdb = (SQLiteDatabase)(new DatabaseOpener(this)).getWritableDatabase();
		if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK) {  
			Image img = (Image)currentrecording;
			img.save(tmpdb);
			
			final String filename = (new File(img.uri.toString())).getAbsolutePath().substring(6);
			
			// Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(this,
				new String[] { filename }, null,
				new MediaScannerConnection.OnScanCompletedListener() {
				public void onScanCompleted(String path, Uri uri) {
					Log.i("ExternalStorage", "Scanned " + path + ":");
					Log.i("ExternalStorage", "-> uri=" + uri);
				}
            });
		}
		tmpdb.close();
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {	
		switch(id) {
			case DELETEDIALOGID:
				((AlertDialog)dialog).setMessage(getString(R.string.overview_deleteelement));
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
								// Add logic to delete the image file here
						   
								SQLiteDatabase db = (SQLiteDatabase)(new DatabaseOpener(Overview.this)).getWritableDatabase();
								selectedelement.hardDelete(db);
								db.close();
								
								syncWithObject();
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
	
	public void editMetaInformation() {
		Intent launch = new Intent(this, EditMeta.class);
		launch.putExtra("storyid", story.id);
		this.startActivity(launch);	
	}
	
	public void viewElement(DatabaseInterface elem) {
		viewElement(elem, -1);
	}
	
	public void viewElement(DatabaseInterface elem, int position) {
		if (elem == null) {
			if (position == METAPOSITION) {
				editMetaInformation();
			}
		}					
		else if (elem.getClass() == Note.class) {
			launchNote(elem.id);
		}
		else if (elem.getClass() == Quote.class) {
			launchQuote(elem.id);
		}				
	}

	public void launchQuote(View v) {
		launchQuote();
	}

	public void launchQuote() {
		launchQuote(-1L);
	}
	
	public void launchQuote(long quoteid) {
		Intent launch = new Intent(this, EditQuote.class);
		if (quoteid < 0) {
			launch.putExtra("id", -1L);
			launch.putExtra("storyid", story.id);
		}
		else {
			launch.putExtra("id", quoteid);
			launch.putExtra("storyid", story.id);
		}
		this.startActivity(launch);	
	}
	

	public void launchNote(View v) {
		launchNote();
	}
	
	public void launchNote() {
		launchNote(-1);
	}
	
	public void launchNote(int noteid) {
		launchNote((long)noteid);
	}
	
	public void launchNote(long noteid) {
		Intent launch = new Intent(this, EditNote.class);
		if (noteid < 0) {
			launch.putExtra("id", -1L);
			launch.putExtra("storyid", story.id);
		}
		else {
			launch.putExtra("id", noteid);
			launch.putExtra("storyid", story.id);
		}
		this.startActivity(launch);
	}
	
	private void syncStoryToForm() {	
		// Nothing Yet
	}
	
	private boolean syncNote(View noteview, Note note) {
		TextView tvn = (TextView)noteview.findViewById(R.id.overviewnote_note);
		TextView meta = (TextView)noteview.findViewById(R.id.overviewnote_note_meta);

		if (tvn != null && meta != null) {
			tvn.setText(note.text);
			//getString(R.string.overviewnote_dateprefix) + " " + 
			meta.setText(Util.longDatePlusTimeFormat(note.lastmodified));
			
			return true;
		}
		
		return false;
	}
	
	private boolean syncQuote(View row, Quote quote) {
		TextView qtxt = (TextView)row.findViewById(R.id.overviewquote_quote);
		TextView entitytxt = (TextView)row.findViewById(R.id.overviewquote_entity);
		TextView metatxt = (TextView)row.findViewById(R.id.overviewquote_meta);
		TextView contexttxt = (TextView)row.findViewById(R.id.overviewquote_context);
		
		if (qtxt != null && entitytxt != null && metatxt != null && contexttxt != null) {
			qtxt.setText(Util.scnvl(quote.quote, "\"" + quote.quote + "\"", getString(R.string.overviewquote_noquote)));
			
			String etxt = Util.snvl(quote.entity, getString(R.string.overviewquote_noentity))
				+ Util.scnvl(quote.date, "\n" + Util.APDatePlusTimeFormat(quote.date), "");
			
			entitytxt.setText(etxt);
			contexttxt.setText(Util.snvl(quote.context, ""));

			metatxt.setText(Util.longDatePlusTimeFormat(quote.lastmodified));
			
			if (Util.isBlank(quote.context)) {
				contexttxt.setVisibility(View.GONE);
			}
			else {
				contexttxt.setVisibility(View.VISIBLE);
			}
			
			if (quote.lastmodified == null) {
				metatxt.setVisibility(View.GONE);
			}
			else {
				metatxt.setVisibility(View.VISIBLE);
			}
			
			return true;
		}
		
		return false;
	}
	
	private boolean syncImage(View row, Image image) {
		ImageView imgview = (ImageView)row.findViewById(R.id.overviewimage_image);  
		
		if (imgview != null) {
			//Log.w("imageuri", Util.snvl(image.uri.toString(), "no uri"));
			
			Bitmap thumbnail = image.getThumbnail(this);
			
			if (thumbnail == null) {
				//Log.w("Thumbnail was null.","bleh");
			}
			
			imgview.setImageBitmap(thumbnail);
			return true;
		}
		
		return false;
	}
	
	private class ElementAdapter extends ArrayAdapter<DatabaseInterface> {
	
		HashMap<Class, Integer> classlayoutmap;
	
        public ElementAdapter(ArrayList<DatabaseInterface> elements) {
			super(Overview.this, R.layout.dummy, elements);
			
			classlayoutmap = new HashMap<Class, Integer>();
			classlayoutmap.put(Note.class, R.layout.overviewnote);
			classlayoutmap.put(Quote.class, R.layout.overviewquote);
			classlayoutmap.put(Image.class, R.layout.overviewimage);
			//classlayoutmap.put(Video.class, R.layout.overviewvideo);
			//classlayoutmap.put(Audio.class, R.layout.overviewaudio);
        }
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			DatabaseInterface element = this.getItem(position);
			View row = convertView;
			
			if (row == null) {
				row = generateNewRow(parent, element);
			}
			
			// This fixes a bug where the row won't be null because it's reused
			// so when e.g. a note changes to a quote, the right fields don't exist
			if (populateRow(row, element) == false) {
				row = generateNewRow(parent, element);				
				populateRow(row, element);
			}
			
			return row;
		}
		
		private View generateNewRow(ViewGroup parent, DatabaseInterface element) {
			LayoutInflater inflater = getLayoutInflater();
			Integer layoutid = classlayoutmap.get(element.getClass());
			return inflater.inflate(layoutid.intValue(), parent, false);
		}
		
		private boolean populateRow(View row, DatabaseInterface element) {
			if (element.getClass() == Note.class) {
				return syncNote(row, (Note)element);
			}
			else if (element.getClass() == Quote.class) {
				return syncQuote(row, (Quote)element);
			}
			else if (element.getClass() == Image.class) {
				return syncImage(row, (Image)element);
			}
			
			return false;
		}
		
	}
	
}