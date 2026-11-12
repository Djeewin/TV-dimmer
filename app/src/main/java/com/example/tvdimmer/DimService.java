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
    private final BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            Plan.clearOverride();
            Plan.sync(context);
        }
    };

    @Override public void onCreate(){super.onCreate();instance=this;
        IntentFilter powerEvents = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        powerEvents.addAction(Intent.ACTION_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(powerReceiver, powerEvents, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(powerReceiver, powerEvents);
        }
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);nm.createNotificationChannel(new NotificationChannel("dimmer","TV dimming",NotificationManager.IMPORTANCE_LOW));PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE);PendingIntent restore=PendingIntent.getService(this,2,new Intent(this,DimService.class).setAction("RESTORE"),PendingIntent.FLAG_IMMUTABLE);startForeground(1,new Notification.Builder(this,"dimmer").setSmallIcon(android.R.drawable.ic_menu_view).setContentTitle("TV Dimmer").setContentIntent(open).addAction(new Notification.Action.Builder(null,"Restore normal",restore).build()).setOngoing(true).build());}
    private void clear(){View old=shade;shade=null;layout=null;if(old!=null)try{wm.removeView(old);}catch(IllegalArgumentException ignored){}}
    void render(){
        long now = System.currentTimeMillis();
        int level=Plan.prefs(this).getInt("level",6);level=Math.max(1,Math.min(10,level));
        if(!Plan.needsService(this, now)||!Settings.canDrawOverlays(this)){clear();if(instance==this)instance=null;stopSelf();return;}
        PowerManager power = (PowerManager) getSystemService(POWER_SERVICE);
        if (!power.isInteractive() || !Plan.active(this, now)) {
            clear();
            return;
        }
        float alpha=DimLevel.alpha(level);
        if(alpha==0){clear();return;}
        try{
            if(shade==null){shade=new View(this);shade.setBackgroundColor(0xff000000);layout=new WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);layout.gravity=Gravity.TOP|Gravity.START;layout.alpha=alpha;wm.addView(shade,layout);}
            else{layout.alpha=alpha;wm.updateViewLayout(shade,layout);}
        }catch(RuntimeException e){clear();Plan.prefs(this).edit().putString("error","Allow display access in Android Settings").apply();stopSelf();}
    }
    @Override public int onStartCommand(Intent i,int flags,int id){instance=this;if(i!=null&&"RESTORE".equals(i.getAction())){Plan.override(false);}render();return START_STICKY;}
    @Override public void onDestroy(){unregisterReceiver(powerReceiver);clear();if(instance==this)instance=null;super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}
