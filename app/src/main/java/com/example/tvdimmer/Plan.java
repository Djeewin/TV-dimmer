package com.example.tvdimmer;
import android.app.*;
import android.content.*;
import android.os.Build;
import android.provider.Settings;
import java.util.Calendar;
final class Plan {
    static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences("settings",0);}
    static boolean exact(Context c){return Build.VERSION.SDK_INT<31||((AlarmManager)c.getSystemService(Context.ALARM_SERVICE)).canScheduleExactAlarms();}
    static boolean active(Context c,long now){android.content.SharedPreferences p=prefs(c);Calendar t=Calendar.getInstance();t.setTimeInMillis(now);return ScheduleWindow.effective(p.getInt("override",-1),p.getBoolean("schedule",false),p.getBoolean("manual",false),ScheduleWindow.active(t,p.getInt("days",127),p.getInt("start",1380),p.getInt("end",420),p.getBoolean("endEnabled",true)));}
    static void arm(Context c){AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);PendingIntent pi=PendingIntent.getBroadcast(c,1,new Intent(c,ScheduleReceiver.class).setAction("com.example.tvdimmer.TICK"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);am.cancel(pi);android.content.SharedPreferences p=prefs(c);if(!p.getBoolean("schedule",false)||!exact(c))return;long next=ScheduleWindow.next(Calendar.getInstance(),p.getInt("days",127),p.getInt("start",1380),p.getInt("end",420),p.getBoolean("endEnabled",true));if(next!=Long.MAX_VALUE)try{am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next,pi);}catch(SecurityException e){p.edit().putString("error","Allow schedule access in Android Settings").apply();}}
    static void sync(Context c){arm(c);refresh(c);}
    static void refresh(Context c){
        if(DimService.instance!=null){DimService.instance.render();return;}
        if(active(c,System.currentTimeMillis())&&Settings.canDrawOverlays(c))try{c.startForegroundService(new Intent(c,DimService.class));}catch(RuntimeException e){prefs(c).edit().putString("error","Open TV Dimmer and save preferences to start").apply();}
    }
}
