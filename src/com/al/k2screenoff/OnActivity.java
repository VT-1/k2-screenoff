package com.al.k2screenoff;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

/** Turns the TV panel back on: Settings.Global "screen_off_control" = 1. */
public class OnActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        try {
            Settings.Global.putInt(getContentResolver(), "screen_off_control", 1);
            Log.i("K2ScreenOn", "picture on");
        } catch (Throwable t) {
            Log.e("K2ScreenOn", "fail: " + t);
        }
        finish();
    }
}
