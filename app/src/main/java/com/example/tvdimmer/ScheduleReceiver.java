package com.example.tvdimmer;
import android.content.*;
public class ScheduleReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        String action = i.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Plan.TICK.equals(action)) {
            Plan.clearOverride();
        }
        if(Intent.ACTION_BOOT_COMPLETED.equals(i.getAction()) && !Plan.prefs(c).getBoolean("boot",true)) { Plan.arm(c); return; }
        Plan.sync(c);
    }
}
