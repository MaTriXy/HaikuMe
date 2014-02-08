/*
Copyright 2014 Brian Romanowski

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see `<http://www.gnu.org/licenses/>`.
*/

package com.pwnetics.glass.haiku;

import java.util.Collection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * DB holds r/haiku permalinks and whether they've been visited.
 * Queries allow choosing from the unvisited haikus at random.
 * 
 * @author romanows
 */
public class HaikuDbAdapter {
    private static final String LOG_TAG = HaikuDbAdapter.class.getSimpleName();

    private final Context context;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    private static final String PERMALINK_TABLE_NAME = "haiku_permalinks";
    private static final String PERMALINK_KEY = "permalink";
    private static final String PERMALINK_IS_VISITED = "is_visited";


    private class DatabaseHelper extends SQLiteOpenHelper {
        private final String LOG_TAG = DatabaseHelper.class.getSimpleName();
    
        private static final int DATABASE_VERSION = 1;
        private static final String CREATE_PERMALINK_TABLE = "CREATE TABLE " + PERMALINK_TABLE_NAME + " (" +
                    PERMALINK_KEY + " TEXT PRIMARY KEY NOT NULL, " +
                    PERMALINK_IS_VISITED + " INTEGER NOT NULL DEFAULT 0 CHECK (" + PERMALINK_IS_VISITED + " = 0 OR " + PERMALINK_IS_VISITED + " = 1) );";
        
        private final Context context;
    
        public DatabaseHelper(Context context) {
            super(context, PERMALINK_TABLE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }
    
        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "Creating database");
            db.execSQL(CREATE_PERMALINK_TABLE);

            Log.d(LOG_TAG, "Importing permalinks");
            String[] permalinks = context.getResources().getStringArray(R.array.permalink);
            addPermalinks(db, permalinks);
        }
    

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Do nothing
        }
        
        
        public void addPermalink(SQLiteDatabase db, String permalink) {
            Log.d(LOG_TAG, "Adding permalink '" + permalink + "'");
            ContentValues cv = new ContentValues();
            cv.put(PERMALINK_KEY, permalink);
            cv.put(PERMALINK_IS_VISITED, 0);
            db.insert(PERMALINK_TABLE_NAME, null, cv);
        }
    
    
        public void addPermalinks(SQLiteDatabase db, Collection<String> permalinks) {
            Log.d(LOG_TAG, "Adding " + permalinks.size()  + " permalinks");
            for(String permalink : permalinks) {
                addPermalink(db, permalink);
            }
        }
        
        
        public void addPermalinks(SQLiteDatabase db, String[] permalinks) {
            Log.d(LOG_TAG, "Adding " + permalinks.length  + " permalinks");
            for(String permalink : permalinks) {
                addPermalink(db, permalink);
            }
        }
    }
    

    /**
     * Constructor
     * @param context
     */
    public HaikuDbAdapter(Context context) {
        this.context = context;
    }

    
    /**
     * Get a writable database adapter.
     *
     * @return this, to allow chaining
     * @throws SQLException if the database could be neither opened or created
     */
    public HaikuDbAdapter open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return this;
    }


    public void close() {
        dbHelper.close();
    }


    public SQLiteDatabase getDb() {
        return db;
    }
    

    public void addPermalink(String permalink) {
        dbHelper.addPermalink(this.db, permalink);
    }


    public void addPermalinks(Collection<String> permalinks) {
        dbHelper.addPermalinks(this.db, permalinks);
    }
    
    
    public void addPermalinks(String[] permalinks) {
        dbHelper.addPermalinks(this.db, permalinks);
    }
    
    
    /** 
     * Clears the visited flag from all permalinks.
     * Used to reset the table after all permalinks have been visited once.
     */
    private void clearAllVisitedFlags() {
        Log.d(LOG_TAG, "Clearing visited flags");

        ContentValues cv = new ContentValues();
        cv.put(PERMALINK_IS_VISITED, 0);
        int numRows = db.update(PERMALINK_TABLE_NAME, cv, null, null);

        Log.d(LOG_TAG, numRows + " visited flags cleared");
    }


    /** 
     * Query the database for a random permalink that hasn't been visited.
     * If all permalinks have been visited, will clear the visited flags and return a random permalink.
     * @return permalink string or null if the table contains no permalinks
     */
    public String fetchRandomPermalink() {
        Log.d(LOG_TAG, "Fetching random unvisited permalink");
        String query = "SELECT " + PERMALINK_KEY + " FROM " + PERMALINK_TABLE_NAME + " WHERE " + PERMALINK_IS_VISITED + "=0 ORDER BY RANDOM() LIMIT 1";

        for(int i=0; i<2; i++) {
            Cursor c = db.rawQuery(query, null);
            if(c.moveToFirst()) {
                String permalink = c.getString(c.getColumnIndex(PERMALINK_KEY));
                c.close();
                return permalink;
            } 
            c.close();
            clearAllVisitedFlags();  // Try one more time after clearing all visited flags; maybe we visited everything?
        }
        return null;
    }

    
    public void markPermalinkAsVisited(String permalink) {
        Log.d(LOG_TAG, "Marking the permalink '" + permalink + "' as visited");
        ContentValues cv = new ContentValues();
        cv.put(PERMALINK_IS_VISITED, 1);
        int numRows = db.update(PERMALINK_TABLE_NAME, cv, PERMALINK_KEY + "=?", new String[] {permalink});
        if(numRows != 1) {
        	Log.w(LOG_TAG, "Marked " + numRows + " as visited when attempting to mark permalink '" + permalink + "'");
        }
    }
}
