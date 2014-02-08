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

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class HaikuActivity extends Activity {
    private static final String LOG_TAG = HaikuActivity.class.getSimpleName();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_loading);

        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.d(LOG_TAG, "retreiving random permalink from database");
            HaikuDbAdapter dbHelper = new HaikuDbAdapter(this);
            dbHelper.open();
            String permalink = dbHelper.fetchRandomPermalink();
            new DownloadHaikuPermalink(this, getString(R.string.problems_parsing_submission)).execute(permalink);
            dbHelper.close();
        } else {
            Log.d(LOG_TAG, "no network connection available");
            setContentView(R.layout.activity_haiku);
            TextView haikuView = (TextView) findViewById(R.id.haiku);
            haikuView.setText(R.string.no_network_connectivity);
        }
    }
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            openOptionsMenu();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.example:
            setContentView(R.layout.activity_haiku);
            TextView haikuView = (TextView) findViewById(R.id.haiku);
            TextView authorView = (TextView) findViewById(R.id.author);
            haikuView.setText(R.string.example_haiku);
            authorView.setText(R.string.example_author);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
