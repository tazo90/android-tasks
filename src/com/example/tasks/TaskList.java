package com.example.tasks;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TaskList extends ListActivity {
				
	private static final int ACTIVITY_CREATE=0;
	private static final int ACTIVITY_EDIT=1;
	
	private TasksDbAdapter mDbHelper;
	private MyAdapter mListAdapter;
		
	
	/**
	 * Called when the activity is first created 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_list);
		mDbHelper = new TasksDbAdapter(this);		// pass context
		mDbHelper.open();
		fillData();
				
		registerForContextMenu(getListView());									
	}
		
	
	/**
	 * Fill out listView by tasks from database.
	 */
	@SuppressWarnings("deprecation")
	private void fillData() {
		// Get all of the rows from the database and create the item list
    	Cursor tasksCursor = mDbHelper.fetchAllTasks();    	
    	startManagingCursor(tasksCursor);
    	    	    	    	
    	mListAdapter = new MyAdapter(TaskList.this, tasksCursor);
    	// Assign adapter to ListView
    	setListAdapter(mListAdapter);
    	
    	
    	// Enables filtering for the contents of the given ListView
    	getListView().setTextFilterEnabled(true);
    	
    	EditText myFilter = (EditText) findViewById(R.id.myFilter);
    	myFilter.addTextChangedListener(new TextWatcher() {
    		
    		public void afterTextChanged(Editable s) {    			
    		}
    		
    		public void beforeTextChanged(CharSequence s, int start, int count, int after) {    			
    		}
    		
    		public void onTextChanged(CharSequence s, int start, int before, int count) {
    			mListAdapter.getFilter().filter(s);
    		}
    	});
    	
    	mListAdapter.setFilterQueryProvider(new FilterQueryProvider() {
    		public Cursor runQuery(CharSequence constraint) {    			    		
    			return mDbHelper.fetchAllTasksByName(constraint.toString());
    		}
    	});
    }
	
	/**
	 * Custom CursorAdapter
	 */
	private class MyAdapter extends CursorAdapter {
		
		private LayoutInflater mInflater;
		private Cursor c;
		
		public class ViewHolder {
			public TextView textView;
			public CheckBox checkBox;
		}
		
		@SuppressWarnings("deprecation")
		public MyAdapter(Context context, Cursor c) {
			super(context, c);
			this.mInflater = LayoutInflater.from(context);
			this.c = c;			
		}
		
		public MyAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
			this.mInflater = LayoutInflater.from(context);
			this.c = c;			
		}
		
		
		public View getView(int position, View convertView, ViewGroup parent) {
	        View rowView = convertView;
	        ViewHolder holder = null;	        
	        
	        final int pos = position;
	        if (rowView == null) {
	        	
	        	rowView = this.mInflater.inflate(R.layout.tasks_row, null);
	        	holder = new ViewHolder();
	            holder.textView = (TextView) rowView.findViewById(R.id.text1);
	            holder.checkBox = (CheckBox) rowView.findViewById(R.id.check);
	            	           	            
	            rowView.setTag(holder);	  	           	            
	        } else 
	            holder = (ViewHolder) rowView.getTag();	            	        
	        	     
	        // we get new cursor when we get view, we should use this because we will have exception: attempt to re-open already closed-object
	        Cursor cur = getCursor();
	        cur.moveToPosition(position);	        	       
	        
	        // fill textView 
	        holder.textView.setText(cur.getString(this.c.getColumnIndex(TasksDbAdapter.KEY_TITLE)));
	        // fill checkBox
	        holder.checkBox.setOnCheckedChangeListener(null);
	        holder.checkBox.setChecked(cur.getInt(this.c.getColumnIndex(TasksDbAdapter.KEY_SELECTED))==0? false:true);	        	       
	        
	        // listen on checkbox'es
	        holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton cb, boolean isClicked) {	
					
				    // get cursor of checked row
				    Cursor c = (Cursor) getListView().getItemAtPosition(pos);					    
				    // get rowId of checked row
				    Long rowId = c.getLong(c.getColumnIndex("_id"));				    				    
				    
					if (isClicked) {							
						mDbHelper.selectedTask(rowId, 1);																
					} else  {
					    mDbHelper.selectedTask(rowId, 0);					    
					}					
				}
			});
	        
	        return rowView;
	    } 
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return null;
		}
				
		@Override
		public void bindView(View view, Context context, Cursor cur) {			
		}
	}
		
	/**
	 * Will be called via the onClick attribute of the buttons in task_list.xml
	 * @param view
	 */
	public void onClick(View view)
	{
		switch(view.getId()) {
		case R.id.add:
			createTask();
			break;
		case R.id.del:
			Cursor c = mDbHelper.fetchAllTasks();
			boolean selected;
			int rowId;
			
			// loop through all rows in listView for searching checked rows to delete
			if (c.moveToFirst()) {
				do {					
					selected = (c.getInt(c.getColumnIndex(TasksDbAdapter.KEY_SELECTED)) == 0 ? false:true);
					rowId 	 = (c.getInt(c.getColumnIndex(TasksDbAdapter.KEY_ROWID)));
					if (selected) 
						mDbHelper.deleteTask(rowId);
					
				} while (c.moveToNext());
			}
			else Toast.makeText(this, "Lista pusta", Toast.LENGTH_SHORT).show();
			
			// refresh list view
			fillData();
			break;
		}
	}

	/**
	 * Elements of menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.task_list_menu, menu);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_insert:
			createTask();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	/**
	 * Elements of context menu
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		//menu.add(0, DELETE_ID, 0, R.string.menu_delete);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.task_list_context_menu, menu);
	}
		
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.context_menu_delete:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
			mDbHelper.deleteTask(info.id);
			fillData();
			return true;			
		}		
		return super.onContextItemSelected(item); 
	}
	
	
	/**
	 * Invoke TaskEdit intent
	 */
	private void createTask() {
		Intent i = new Intent(this, TaskEdit.class);		
		startActivityForResult(i, ACTIVITY_CREATE);		
	}
	
	/**
	 * Return position that we clicked on ListView
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l,  v, position, id);		
				
		// Indent pass information between activities
		Intent i = new Intent(this, TaskEdit.class);				
		
		i.putExtra(TasksDbAdapter.KEY_ROWID, id);
		startActivityForResult(i, ACTIVITY_EDIT);			
	}
	
	
	/**
	 * Return result of Activity
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
								
		// refresh the data in Views
		fillData();				
	}	
			
}
