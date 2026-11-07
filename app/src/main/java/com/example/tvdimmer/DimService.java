package com.example.tvdimmer;
import android.app.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.os.*;
import android.provider.Settings;
import android.view.*;
public class DimService extends Service {
    static DimService instance;
    private WindowManager wm;private View shade;private WindowManager.LayoutParams layout;
    @Override public void onCreate(){super.onCreate();instance=this;wm=(WindowManager)getSystemService(WINDOW_SERVICE);NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);nm.createNotificationChannel(new NotificationChannel("dimmer","TV dimming",NotificationManager.IMPORTANCE_LOW));PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE);PendingIntent restore=PendingIntent.getService(this,2,new Intent(this,DimService.class).setAction("RESTORE"),PendingIntent.FLAG_IMMUTABLE);startForeground(1,new Notification.Builder(this,"dimmer").setSmallIcon(android.R.drawable.ic_menu_view).setContentTitle("TV Dimmer").setContentIntent(open).addAction(new Notification.Action.Builder(null,"Restore normal",restore).build()).setOngoing(true).build());}
    private void clear(){View old=shade;shade=null;layout=null;if(old!=null)try{wm.removeView(old);}catch(IllegalArgumentException ignored){}}
    void render(){
        boolean active=Plan.active(this,System.currentTimeMillis());
        int level=Plan.prefs(this).getInt("level",6);level=Math.max(1,Math.min(10,level));
        if(!active||!Settings.canDrawOverlays(this)){clear();if(instance==this)instance=null;stopSelf();return;}
        float alpha=DimLevel.alpha(level);
        if(alpha==0){clear();return;}
        try{
            if(shade==null){shade=new View(this);shade.setBackgroundColor(0xff000000);layout=new WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);layout.gravity=Gravity.TOP|Gravity.START;layout.alpha=alpha;wm.addView(shade,layout);}
            else{layout.alpha=alpha;wm.updateViewLayout(shade,layout);}
        }catch(RuntimeException e){clear();Plan.prefs(this).edit().putString("error","Allow display access in Android Settings").apply();stopSelf();}
    }
    @Override public int onStartCommand(Intent i,int flags,int id){instance=this;if(i!=null&&"RESTORE".equals(i.getAction())){Plan.prefs(this).edit().putBoolean("manual",false).putInt("override",0).apply();}render();return START_STICKY;}
    @Override public void onDestroy(){clear();if(instance==this)instance=null;super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}
