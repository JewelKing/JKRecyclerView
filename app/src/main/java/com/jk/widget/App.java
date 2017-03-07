package com.jk.widget;

import android.app.Application;

/**
 * Title
 *
 * @author JewelKing
 * @date 2017-03-07
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCrashHandler handler = AppCrashHandler.getInstance();
        handler.init(this);
    }
}
