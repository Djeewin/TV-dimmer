package com.example.tvdimmer;

import android.app.*;
import android.content.*;
import android.os.Build;
import android.provider.Settings;
import java.util.Calendar;

final class Plan {
    static android.content.SharedPreferences prefs(Context c) { return c.getSharedPreferences("settings",0); }
    static boolean exact(Context c) { return Build.VERSION.SDK_INT < 31 || ((AlarmManager)c.getSystemService(Context.ALARM_SERVICE)).canScheduleExactAlarms(); }
    static boolean selected(Context c, Calendar day) { return (prefs(c).getInt("days",127) & (1 << (day.get(Calendar.DAY_OF_WEEK)-1))) != 0; }
    static Calendar at(Calendar day, int minute) { Calendar t=(Calendar)day.clone(); t.set(Calendar.HOUR_OF_DAY,minute/60); t.set(Calendar.MINUTE,minute%60); t.set(Calendar.SECOND,0); t.set(Calendar.MILLISECOND,0); return t; }
    // Find the latest start. Each selected day owns its following overnight end.
    static boolean active(Context c, long now) {
        if (!prefs(c).getBoolean("schedule",false)) return prefs(c).getBoolean("manual",false);
        Calendar today=Calendar.getInstance(); today.setTimeInMillis(now);
        for(int i=0;i<8;i++) {
            Calendar d=(Calendar)today.clone(); d.add(Calendar.DATE,-i);
            if(!selected(c,d)) continue;
            Calendar start=at(d,prefs(c).getInt("start",1260));
            if(start.getTimeInMillis()>now) continue;
            if(!prefs(c).getBoolean("endEnabled",true)) return true;
            Calendar end=at(d,prefs(c).getInt("end",420));
            if(!end.after(start)) end.add(Calendar.DATE,1);
            return now<end.getTimeInMillis();
        }
        return false;
    }
    static void arm(Context c) {
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi=PendingIntent.getBroadcast(c,1,new Intent(c,ScheduleReceiver.class).setAction("com.example.tvdimmer.TICK"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        am.cancel(pi);
        if(!prefs(c).getBoolean("schedule",false) || !exact(c)) return;
        long now=System.currentTimeMillis(), next=Long.MAX_VALUE;
        Calendar today=Calendar.getInstance();
        for(int i=-1;i<=8;i++) {
            Calendar d=(Calendar)today.clone(); d.add(Calendar.DATE,i);
            if(!selected(c,d)) continue;
            Calendar s=at(d,prefs(c).getInt("start",1260));
            if(s.getTimeInMillis()>now) next=Math.min(next,s.getTimeInMillis());
            if(prefs(c).getBoolean("endEnabled",true)) {
                Calendar e=at(d,prefs(c).getInt("end",420)); if(!e.after(s)) e.add(Calendar.DATE,1);
                if(e.getTimeInMillis()>now) next=Math.min(next,e.getTimeInMillis());
            }
        }
        if(next!=Long.MAX_VALUE) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next,pi);
    }
    static void sync(Context c) {
        arm(c);
        if(Settings.canDrawOverlays(c)) {
            try { c.startForegroundService(new Intent(c,DimService.class)); }
            catch (RuntimeException e) { prefs(c).edit().putString("error","Automatic start was blocked. Open TV Dimmer and press Apply.").apply(); }
        }
    }
}
