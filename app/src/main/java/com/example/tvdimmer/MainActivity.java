package com.example.tvdimmer;
import android.app.*;
import android.os.*;
import android.content.*;
import android.net.Uri;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout body; TextView status, levelText; SeekBar slider; Switch manual, schedule, boot, endEnabled;
    Button startButton,endButton; CheckBox[] days=new CheckBox[7]; int start,end;
    final String[] names={"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
    TextView label(String s,int size) { TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setPadding(0,10,0,8); body.addView(t); return t; }
    Button button(String s,Runnable r) { Button b=new Button(this); b.setText(s); body.addView(b); b.setOnClickListener(v->r.run()); return b; }
    Switch toggle(String s,boolean value) { Switch t=new Switch(this); t.setText(s); t.setTextSize(18); t.setPadding(8,12,8,12); t.setChecked(value); body.addView(t); return t; }
    String time(int m) { return String.format(Locale.getDefault(),"%02d:%02d",m/60,m%60); }
    void pick(boolean isStart) { int m=isStart?start:end; new TimePickerDialog(this,(v,h,n)->{if(isStart)start=h*60+n;else end=h*60+n; startButton.setText("Start: "+time(start));endButton.setText("Restore: "+time(end));},m/60,m%60,true).show(); }
    void access(String action) { try { startActivity(new Intent(action,Uri.parse("package:"+getPackageName()))); } catch(RuntimeException e) { Toast.makeText(this,"Use Settings > Apps > Special app access, or see README for ADB setup.",Toast.LENGTH_LONG).show(); } }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state); android.content.SharedPreferences p=Plan.prefs(this);
        ScrollView scroll=new ScrollView(this); body=new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(48,24,48,48); scroll.addView(body); setContentView(scroll);
        label("TV Dimmer",30); label("Use the remote arrows to adjust. Level 10 restores normal brightness.",18);
        status=label("",16);
        button("1. Allow display over other apps",()->access(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
        button("2. Allow precise schedules (Android 12+)",()->{if(Build.VERSION.SDK_INT>=31)access(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);});
        levelText=label("Brightness: "+p.getInt("level",6)+" / 10",22);
        slider=new SeekBar(this); slider.setMax(9); slider.setProgress(p.getInt("level",6)-1); slider.setKeyProgressIncrement(1); body.addView(slider);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int n,boolean user){levelText.setText("Brightness: "+(n+1)+" / 10"); if(user){p.edit().putInt("level",n+1).apply(); if(Settings.canDrawOverlays(MainActivity.this))Plan.sync(MainActivity.this);}} public void onStartTrackingTouch(SeekBar s){} public void onStopTrackingTouch(SeekBar s){} });
        manual=toggle("Dim now (when schedule is off)",p.getBoolean("manual",false));
        boot=toggle("Start automatically after boot",p.getBoolean("boot",true));
        schedule=toggle("Enable schedule",p.getBoolean("schedule",false));
        label("Repeat",22);
        button("Daily",()->{for(CheckBox d:days)d.setChecked(true);});
        button("Weekly: choose one weekday",()->{for(int i=0;i<7;i++)days[i].setChecked(i==Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1);});
        label("Custom: select any combination of weekdays",16);
        int mask=p.getInt("days",127);
        LinearLayout row=new LinearLayout(this); body.addView(row);
        for(int i=0;i<7;i++){days[i]=new CheckBox(this); days[i].setText(names[i]); days[i].setChecked((mask&(1<<i))!=0);row.addView(days[i]);}
        start=p.getInt("start",1260);end=p.getInt("end",420);
        startButton=button("Start: "+time(start),()->pick(true));
        endEnabled=toggle("Restore normal brightness at end time",p.getBoolean("endEnabled",true));
        endButton=button("Restore: "+time(end),()->pick(false));
        label("An earlier end time means the following morning. Times use the TV's local time zone. Without an end time, dimming stays on until you restore it.",16);
        button("Apply settings",()->{
            int selected=0;for(int i=0;i<7;i++)if(days[i].isChecked())selected|=1<<i;
            if(schedule.isChecked()&&(selected==0||!Plan.exact(this))){Toast.makeText(this,"Choose at least one day and allow precise schedules first.",Toast.LENGTH_LONG).show();return;}
            if(!Settings.canDrawOverlays(this)){Toast.makeText(this,"Allow display over other apps first.",Toast.LENGTH_LONG).show();return;}
            p.edit().putInt("days",selected).putInt("start",start).putInt("end",end).putBoolean("endEnabled",endEnabled.isChecked()).putBoolean("schedule",schedule.isChecked()).putBoolean("manual",manual.isChecked()).putBoolean("boot",boot.isChecked()).remove("error").apply(); Plan.sync(this); refresh();Toast.makeText(this,"Settings saved",Toast.LENGTH_SHORT).show();
        });
        button("RESTORE NORMAL NOW (also disables schedule)",()->{schedule.setChecked(false);manual.setChecked(false);slider.setProgress(9);p.edit().putBoolean("schedule",false).putBoolean("manual",false).putInt("level",10).apply();Plan.arm(this);stopService(new Intent(this,DimService.class));refresh();});
        label("No internet access. No accounts. Open this app once and grant permissions before testing automatic startup.",16);
    }
    void refresh(){status.setText("Overlay: "+(Settings.canDrawOverlays(this)?"allowed":"needs permission")+"   Schedule access: "+(Plan.exact(this)?"allowed":"needs permission")+"\n"+Plan.prefs(this).getString("error",""));}
    @Override public void onResume(){super.onResume();if(status!=null)refresh();}
}
