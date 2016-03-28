package com.admin.xfnulled;

import android.app.Application; 
import com.parse.Parse; 
import com.parse.ParsePush;

public class App extends Application { 

    @Override public void onCreate() { 
        super.onCreate();

		Parse.initialize(this, "", ""); 
        ParsePush.subscribeInBackground("");
    }
} 