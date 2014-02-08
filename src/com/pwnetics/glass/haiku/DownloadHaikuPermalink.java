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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

public class DownloadHaikuPermalink extends AsyncTask<String, Void, HaikuAuthorTuple> {
    private static final String LOG_TAG = DownloadHaikuPermalink.class.getSimpleName();

    private static final String HAIKU_URL_PREFIX = "http://reddit.com/r/haiku/";
    private static final String USER_AGENT = "google glass haiku reader v1.0";
    private static final int READ_TIMEOUT_MS = 10000;
    private static final int CONNECT_TIMEOUT_MS = 10000;

    private static final int MAX_LINE_LENGTH = 45;
    
    private final Activity activity;
    private final String errorMessage;
    

    public DownloadHaikuPermalink(Activity activity, String errorMessage) {
        this.activity = activity;
        this.errorMessage = errorMessage;
    }
    

    private String getPermalinkUrl(String permalink) {
        return HAIKU_URL_PREFIX + permalink + ".json";
    }
    

    @Override
    protected HaikuAuthorTuple doInBackground(String... permalinks) {
        String message = null;
        try {
            URL permalinkUrl = new URL(getPermalinkUrl(permalinks[0]));
            Log.d(LOG_TAG, "Fetching haiku url '" + permalinkUrl + "'");

            HttpURLConnection conn = (HttpURLConnection) permalinkUrl.openConnection();
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();

            int response = conn.getResponseCode();
            if(response == HttpURLConnection.HTTP_OK) {
                HaikuDbAdapter dbHelper = new HaikuDbAdapter(activity);
                dbHelper.open();
                dbHelper.markPermalinkAsVisited(permalinks[0]);
                dbHelper.close();

                // Trick to read in contents of the connection response InputStream as a string
                InputStream is = null;
                try {
                    is = conn.getInputStream();
                    Scanner foo = new java.util.Scanner(is, "utf-8").useDelimiter("\\A");
                    message = foo.hasNext() ? foo.next() : "";

                    Log.d(LOG_TAG, "Haiku response code status " + response + " and message body '" + message + "'");
                } finally {
                    if (is != null) {
                        is.close();
                    } 
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException while attempting to download r/haiku JSON");
        } 
        
        if(message == null) {
            return null;
        }
        Matcher titleMatcher = Pattern.compile(".*\"title\":\\s*\"([^\"]+)\".*").matcher(message);
        if(!titleMatcher.matches()) {
            Log.e(LOG_TAG, "Could not extract title from downloaded content");
            return null;
        }
        String haiku = titleMatcher.group(1);
        String[] haikuLines = haiku.split("/");
        if(haikuLines.length != 3) {
            Log.e(LOG_TAG, "Haiku text is not 3 lines");
            return null;  // FIXME: not quite fair, but we'll stick with the 3-line hard-constraint on the haiku
        }
        StringBuilder sb = new StringBuilder();
        for(String x : haikuLines) {
            x = x.trim();
            if(x.length() > MAX_LINE_LENGTH) {
                Log.e(LOG_TAG, "Haiku line exceeded " + MAX_LINE_LENGTH + " characters");
                return null;
            }
            sb.append(x);
            sb.append("\n");
        }
        haiku = sb.substring(0, sb.length() - 1);  // everything but the last newline
                    
        String noAuthor = "n/a";
        String author = "";
        Matcher authorMatcher = Pattern.compile(".*\"author\": \"([^\"]*)\".*").matcher(message);
        if(!authorMatcher.matches()) {
            Log.e(LOG_TAG, "Could not extract author from downloaded content");
            return new HaikuAuthorTuple(haiku, noAuthor);
        }
        author = authorMatcher.group(1).trim();
        if(author.length() > MAX_LINE_LENGTH) {
            Log.e(LOG_TAG, "Author line exceeded " + MAX_LINE_LENGTH + " characters");
            return new HaikuAuthorTuple(haiku, noAuthor);
        }
        return new HaikuAuthorTuple(haiku, author);
    }

    
    @Override
    protected void onPostExecute(HaikuAuthorTuple result) {
        activity.setContentView(R.layout.activity_haiku);
        TextView haikuView = (TextView) activity.findViewById(R.id.haiku);
        TextView authorView = (TextView) activity.findViewById(R.id.author);
        if(result != null) {
            haikuView.setText(result.haiku);
            authorView.setText(result.author);
        } else {
            haikuView.setText(errorMessage);
            authorView.setText("");
        }
    }
}