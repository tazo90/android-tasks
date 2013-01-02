package com.example.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple tasks database access helper class. Defines the basic CRUD operations
 * for the task list example, and gives the ability to list all tasks as well as
 * retrieve or modify a specific task.
 */
public class TasksDbAdapter {

    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_SELECTED = "selected";
    public static final String KEY_ROWID = "_id";    

    private static final String TAG = "TasksDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table tasks (_id integer primary key autoincrement, "
        + "title text not null, body text not null, selected integer default 0);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "tasks";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public TasksDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public TasksDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();        
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new task using the title and body provided. If the task is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the task
     * @param body the body of the task
     * @return rowId or -1 if failed
     */
    public long createTask(String title, String body, int selected) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_BODY, body);
        initialValues.put(KEY_SELECTED, selected);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the task with the given rowId
     * 
     * @param rowId id of task to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteTask(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all tasks in the database
     * 
     * @return Cursor over all tasks
     */
    public Cursor fetchAllTasks() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_BODY, KEY_SELECTED}, null, null, null, null, null);
    }
    
    
    /**
     * Return a Cursor over the list of all tasks names in the database
     * 
     * @return Cursor over all tasks name
     */
    public Cursor fetchAllTasksByName(String inputText) throws SQLException {
    	//Log.w(TAG, inputText);
    	Cursor cur = null;
    	
    	if (inputText == null || inputText.length() == 0) {
    		cur = mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, 
    				KEY_TITLE, KEY_BODY, KEY_SELECTED}, 
    				null, null, null, null, null);
    	} else {
    		cur = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
    				KEY_TITLE, KEY_BODY, KEY_SELECTED},
    				KEY_TITLE + " like '%" + inputText + "%'", null,
    				null, null, null, null);
    	}
    	if (cur != null) 
    		cur.moveToFirst();
    	
    	return cur;
    }

    /**
     * Return a Cursor positioned at the task that matches the given rowId
     * 
     * @param rowId id of task to retrieve
     * @return Cursor positioned to matching task, if found
     * @throws SQLException if task could not be found/retrieved
     */
    public Cursor fetchTask(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_TITLE, KEY_BODY, KEY_SELECTED}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) 
            mCursor.moveToFirst();
        
        return mCursor;

    }

    /**
     * Update the task using the details provided. The task to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of task to update
     * @param title value to set task title to
     * @param body value to set task body to
     * @return true if the task was successfully updated, false otherwise
     */
    public boolean updateTask(long rowId, String title, String body) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body);        

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    /**
     * update checkbox field of task  
     * @param rowId
     * @param selected
     * @return
     */
    public boolean selectedTask(long rowId, int selected) {
    	ContentValues args = new ContentValues();
    	args.put(KEY_SELECTED, selected);
    	
    	return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }     
    
}
