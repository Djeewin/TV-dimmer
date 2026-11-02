package com.example.tvdimmer;
import android.app.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.os.*;
import android.provider.Settings;
import android.view.*;

public class DimService extends Service {
    WindowManager wm; View shade;
    @Override public void onCreate() {
        super.onCreate(); wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel("dimmer","TV dimming",NotificationManager.IMPORTANCE_LOW));
        PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE);
        PendingIntent stop=PendingIntent.getService(this,2,new Intent(this,DimService.class).setAction("RESTORE"),PendingIntent.FLAG_IMMUTABLE);
        startForeground(1,new Notification.Builder(this,"dimmer").setSmallIcon(android.R.drawable.ic_menu_view).setContentTitle("TV Dimmer is ready").setContentText("Open to adjust brightness or restore the screen").setContentIntent(open).addAction(new Notification.Action.Builder(null,"Restore normal",stop).build()).setOngoing(true).build());
    }
    void clear() { if(shade!=null) { wm.removeView(shade); shade=null; } }
    @Override public int onStartCommand(Intent intent,int flags,int id) {
        if(intent!=null && "RESTORE".equals(intent.getAction())) { Plan.prefs(this).edit().putBoolean("schedule",false).putBoolean("manual",false).apply(); Plan.arm(this); clear(); stopSelf(); return START_NOT_STICKY; }
        if(!Settings.canDrawOverlays(this)) { clear(); stopSelf(); return START_NOT_STICKY; }
        int level=Plan.prefs(this).getInt("level",6);
        boolean active=Plan.active(this,System.currentTimeMillis());
        clear();
        if(active && level<10) {
            shade=new View(this); shade.setBackgroundColor(0xff000000);
            WindowManager.LayoutParams lp=new WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
            // 95% black at 1, transparent at 10. Never completely black.
            lp.alpha=(10-level)/9f*0.95f; lp.gravity=Gravity.TOP|Gravity.START;
            try { wm.addView(shade,lp); }
            catch(RuntimeException e) { shade=null; Plan.prefs(this).edit().putString("error","Overlay unavailable. Check display over other apps permission.").apply(); stopSelf(); }
        }
        if(!active && !Plan.prefs(this).getBoolean("schedule",false)) stopSelf();
        return START_STICKY;
    }
    @Override public void onDestroy() { clear(); super.onDestroy(); }
    @Override public IBinder onBind(Intent i) { return null; }
}
