package com.al.k2screenoff;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

/**
 * Turns the TV panel off (same mechanism as Settings -> System -> Power
 * -> "Turn off display"): writes Settings.Global "screen_off_control".
 * 0 = picture off, 1 = picture on. Observed by mitv.service ->
 * ScreenSaverManagerImpl.setPictureOff -> RenderService SetPanelPowerStatus.
 */
public class OffActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        try {
            Settings.Global.putInt(getContentResolver(), "screen_off_control", 0);
            Log.i("K2ScreenOff", "picture off");
        } catch (Throwable t) {
            Log.e("K2ScreenOff", "fail: " + t);
        }
        finish();
    }
}
