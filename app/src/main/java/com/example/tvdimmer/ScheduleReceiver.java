package com.example.tvdimmer;
import android.content.*;
public class ScheduleReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        if(Intent.ACTION_BOOT_COMPLETED.equals(i.getAction()) && !Plan.prefs(c).getBoolean("boot",true)) { Plan.arm(c); return; }
        Plan.sync(c);
    }
}
