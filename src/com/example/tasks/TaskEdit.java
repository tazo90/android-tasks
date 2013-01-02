package com.example.tasks;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TaskEdit extends Activity {

	private EditText mTitleText;
	private EditText mBodyText;
	private Long mRowId;
	private TasksDbAdapter mDbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDbHelper = new TasksDbAdapter(this);
		mDbHelper.open();			
					
		setContentView(R.layout.task_edit);					
		
		mTitleText = (EditText) findViewById(R.id.title);		
		mBodyText = (EditText) findViewById(R.id.body);		
						
		Button confirmButton = (Button) findViewById(R.id.confirm);
			 
		// we save state of fields in Bundle 
		mRowId = (savedInstanceState == null) ? null :
			(Long) savedInstanceState.getSerializable(TasksDbAdapter.KEY_ROWID);
		if (mRowId == null) {			
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(TasksDbAdapter.KEY_ROWID)
									: null;
			
			// set title of activity depending on whether mRowId is set or not (set mRowId means create, null means edit)
			if (mRowId == null) setTitle(R.string.title_activity_task_add);
			else setTitle(R.string.title_activity_task_edit);
		}		
		
		// fill fields depending on mRowId
		populateFields();
		
		confirmButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				// all set in result will be returned to the caller
				setResult(RESULT_OK);
				// signal that the Activity is done
				finish();									
			}
		});
		
	}
	
	@SuppressWarnings("deprecation")
	private void populateFields() {
		if (mRowId != null) {			// edit mode
			// find the right task to edit
			Cursor task = mDbHelper.fetchTask(mRowId);
			// get cursor life-cycle (release and re-create resources depend on life-cycle)
			startManagingCursor(task);
			// get title and body values from Cursor
			mTitleText.setText(task.getString(
					task.getColumnIndexOrThrow(TasksDbAdapter.KEY_TITLE)));
			mBodyText.setText(task.getString(
					task.getColumnIndexOrThrow(TasksDbAdapter.KEY_BODY)));								
		}
	}
	
	/**
	 * Life-cycle methods
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveState();
		outState.putSerializable(TasksDbAdapter.KEY_ROWID, mRowId);
	}
	
	@Override
	protected void onPause() {		// saveState on paused
		super.onPause();
		saveState();
	}

	@Override 					
	protected void onResume() {		// fill files on resumed 
		super.onResume();
		populateFields();
	}
	
	private void saveState() {
		String title = mTitleText.getText().toString();
		String body = mBodyText.getText().toString();								
						
		if (!title.matches("")) {		// modify database only if TextEdit isn't empty 									
			if (mRowId == null)	{ 		// new task
				long id = mDbHelper.createTask(title, body, 0);
				if (id > 0) 
					mRowId = id;
				Toast.makeText(this, R.string.toast_add_task, Toast.LENGTH_SHORT).show();
			} else {					// edit task		
				mDbHelper.updateTask(mRowId, title, body);				
			}			
		}
	}	
}
