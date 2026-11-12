package com.example.tvdimmer;
import android.app.*;
import android.content.*;
import android.os.Build;
import android.provider.Settings;
import java.util.Calendar;
final class Plan {
    static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences("settings",0);}
    static boolean exact(Context c){return Build.VERSION.SDK_INT<31||((AlarmManager)c.getSystemService(Context.ALARM_SERVICE)).canScheduleExactAlarms();}
    static final String TICK = "com.example.tvdimmer.TICK";
    private static int manualOverride = -1;

    static void override(boolean enabled) {
        manualOverride = enabled ? 1 : 0;
    }

    static void clearOverride() {
        manualOverride = -1;
    }

    static boolean scheduledNow(Context context, long now) {
        SharedPreferences settings = prefs(context);
        if (!settings.getBoolean("schedule", false)) return false;
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(now);
        return ScheduleWindow.active(time, settings.getInt("days", 127),
                settings.getInt("start", 1380), settings.getInt("end", 420),
                settings.getBoolean("endEnabled", true));
    }

    static boolean active(Context context, long now) {
        return manualOverride == -1 ? scheduledNow(context, now) : manualOverride == 1;
    }

    static boolean needsService(Context context, long now) {
        // Keep listening for wake while the user has overridden an active schedule.
        return active(context, now) || scheduledNow(context, now);
    }
    static void arm(Context c){AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);PendingIntent pi=PendingIntent.getBroadcast(c,1,new Intent(c,ScheduleReceiver.class).setAction(TICK),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);am.cancel(pi);android.content.SharedPreferences p=prefs(c);if(!p.getBoolean("schedule",false)||!exact(c))return;long next=ScheduleWindow.next(Calendar.getInstance(),p.getInt("days",127),p.getInt("start",1380),p.getInt("end",420),p.getBoolean("endEnabled",true));if(next!=Long.MAX_VALUE)try{am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next,pi);}catch(SecurityException e){p.edit().putString("error","Allow schedule access in Android Settings").apply();}}
    static void sync(Context c){arm(c);refresh(c);}
    static void refresh(Context c){
        if(DimService.instance!=null){DimService.instance.render();return;}
        if(needsService(c,System.currentTimeMillis())&&Settings.canDrawOverlays(c))try{c.startForegroundService(new Intent(c,DimService.class));}catch(RuntimeException e){prefs(c).edit().putString("error","Open TV Dimmer and save preferences to start").apply();}
    }
}
