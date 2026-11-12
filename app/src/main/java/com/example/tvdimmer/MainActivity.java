package com.example.tvdimmer;

import android.app.*;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int BG=0xff0f0f0f, SURFACE=0xff292929, ORANGE=0xffff9500, TEXT=0xffeeeeee, GRAY=0xff666666;
    private LinearLayout body;
    private Button power,startButton,endButton;
    private TextView levelText;
    private SeekBar slider;
    private Switch boot,schedule,daily,permissions;
    private final CheckBox[] days=new CheckBox[7];
    private int startMinute,endMinute;
    private boolean loading,permissionUpdate;
    private boolean foreground,initialFocusPending=true,powerIsOn;
    private float powerStrokeOpacity=1f;
    private GradientDrawable powerDrawable;
    private ValueAnimator powerPulse;
    private int dp(float x){return Math.round(x*getResources().getDisplayMetrics().density);}
    private GradientDrawable shape(int fill,int radius,boolean focused){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));if(focused)d.setStroke(dp(2),ORANGE);return d;}
    private void focus(View v,int color,int radius){v.setBackground(shape(color,radius,v.hasFocus()));v.setOnFocusChangeListener((a,b)->a.setBackground(shape(color,radius,b)));}
    private void actionFocus(Button b,int normal,int selected){
        b.setDefaultFocusHighlightEnabled(false);
        View.OnFocusChangeListener listener=(v,focused)->{GradientDrawable d=shape(focused?selected:normal,8,false);if(focused)d.setStroke(dp(2),Color.WHITE);v.setBackground(d);};
        b.setOnFocusChangeListener(listener);listener.onFocusChange(b,b.hasFocus());
    }
    private void center(View v){body.addView(v,new LinearLayout.LayoutParams(-2,-2));}
    private void space(int height){Space s=new Space(this);body.addView(s,new LinearLayout.LayoutParams(1,dp(height)));}
    private void sectionDivider(){space(20);View line=new View(this);line.setTag("section-divider");line.setBackgroundColor(0xff444444);body.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));space(20);}
    private LinearLayout row(){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);center(r);return r;}
    private TextView text(String value,float size,int color){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER);return t;}
    private Button button(String value,boolean primary,Runnable callback){Button b=new Button(this);b.setText(value);b.setTextSize(15);b.setTextColor(primary?BG:TEXT);b.setTypeface(null,Typeface.BOLD);b.setAllCaps(false);b.setPadding(dp(14),dp(6),dp(14),dp(6));b.setMinWidth(0);b.setMinimumWidth(0);b.setMinHeight(0);b.setMinimumHeight(0);b.setBackgroundTintList(null);focus(b,primary?ORANGE:SURFACE,8);b.setOnClickListener(v->callback.run());return b;}
    private ColorStateList stateColors(int on,int off){return new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{on,off});}
    private Switch toggle(String label,boolean checked){Switch s=new Switch(this);s.setText(label);s.setTextSize(15);s.setTextColor(TEXT);s.setChecked(checked);s.setShowText(false);s.setSwitchPadding(dp(12));s.setPadding(0,0,dp(8),0);s.setMinHeight(dp(30));s.setMinimumHeight(dp(30));GradientDrawable thumb=new GradientDrawable();thumb.setShape(GradientDrawable.OVAL);thumb.setColor(Color.WHITE);thumb.setSize(dp(24),dp(24));s.setThumbDrawable(thumb);GradientDrawable track=shape(GRAY,14,false);track.setSize(dp(46),dp(26));s.setTrackDrawable(track);s.setSwitchMinWidth(dp(46));s.setTrackTintList(stateColors(ORANGE,GRAY));s.setThumbTintList(stateColors(Color.WHITE,0xffbbbbbb));focus(s,Color.TRANSPARENT,6);return s;}
    private String time(int value){return String.format(Locale.ROOT,"%02d:%02d",value/60,value%60);}
    private void timeLabels(){startButton.setText(time(startMinute));endButton.setText(time(endMinute));}

    @Override public void onCreate(Bundle state){
        super.onCreate(state);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
        FitSurface surface=new FitSurface(this);
        FrameLayout canvas=new FrameLayout(this);canvas.setBackgroundColor(BG);surface.addView(canvas);setContentView(surface);
        LinearLayout panels=new LinearLayout(this);panels.setOrientation(LinearLayout.HORIZONTAL);panels.setPadding(dp(1),dp(1),dp(1),dp(1));panels.setBackgroundColor(BG);panels.setTag("main-panels");
        FrameLayout.LayoutParams frameParams=new FrameLayout.LayoutParams(-1,-1);frameParams.setMargins(dp(24),dp(20),dp(24),dp(20));canvas.addView(panels,frameParams);
        BrandingPane branding=new BrandingPane(this);panels.addView(branding,new LinearLayout.LayoutParams(0,-1,0.36f));
        View divider=new View(this);divider.setBackgroundColor(0xff363636);LinearLayout.LayoutParams dividerParams=new LinearLayout.LayoutParams(dp(1),-1);dividerParams.setMargins(0,dp(12),0,dp(12));panels.addView(divider,dividerParams);
        FrameLayout settings=new FrameLayout(this);panels.addView(settings,new LinearLayout.LayoutParams(0,-1,0.64f));
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setGravity(Gravity.START);
        FrameLayout.LayoutParams settingsParams=new FrameLayout.LayoutParams(-1,-2,Gravity.CENTER);settingsParams.setMargins(dp(24),dp(24),dp(24),dp(24));settings.addView(body,settingsParams);
        LinearLayout brandText=new LinearLayout(this);brandText.setOrientation(LinearLayout.VERTICAL);brandText.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView title=text("TV Dimmer",20,TEXT);title.setTypeface(null,Typeface.BOLD);title.setIncludeFontPadding(false);brandText.addView(title);
        TextView subtitle=text("Reduce the brightness of your TV for a more comfy view at night",15,TEXT);subtitle.setIncludeFontPadding(false);subtitle.setMaxWidth(dp(250));LinearLayout.LayoutParams subtitleParams=new LinearLayout.LayoutParams(-2,-2);subtitleParams.topMargin=dp(10);brandText.addView(subtitle,subtitleParams);
        FrameLayout.LayoutParams brandParams=new FrameLayout.LayoutParams(-1,-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL);brandParams.setMargins(dp(24),dp(60),dp(24),0);branding.addView(brandText,brandParams);branding.header=brandText;
        // Equal dimensions, no default button insets or minimum sizes, and an oval drawable.
        power=button("OFF",false,this::toggleNow);power.setTextSize(30);power.setSingleLine(true);power.setGravity(Gravity.CENTER);power.setPadding(0,0,0,0);power.setDefaultFocusHighlightEnabled(false);power.setStateListAnimator(null);power.setElevation(0);power.setFocusableInTouchMode(true);power.setId(View.generateViewId());
        branding.addView(power,new FrameLayout.LayoutParams(dp(112),dp(112),Gravity.CENTER));power.setOnFocusChangeListener((v,focused)->paintPower());
        TextView credit=text("Created by Godwin Ikape",10.8f,Color.GRAY);credit.setIncludeFontPadding(false);credit.setAlpha(0.45f);FrameLayout.LayoutParams creditParams=new FrameLayout.LayoutParams(-2,-2,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);creditParams.bottomMargin=dp(28);branding.addView(credit,creditParams);branding.footer=credit;
        Button support = button("Buy me a coffee", false, this::showSupport);
        support.setTextSize(11);
        support.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Cookie-Regular.ttf"));
        support.setSingleLine(true);
        support.setGravity(Gravity.CENTER);
        support.setCompoundDrawablePadding(dp(8));
        android.graphics.drawable.Drawable cup = getDrawable(R.drawable.support_cup);
        cup.setBounds(0, 0, dp(12), dp(12));
        support.setCompoundDrawablesRelative(cup, null, null, null);
        focus(support, SURFACE, 5);
        support.setIncludeFontPadding(false);
        support.setPadding(dp(6), 0, dp(6), 0);
        support.setId(View.generateViewId());
        support.setNextFocusUpId(power.getId());
        power.setNextFocusDownId(support.getId());
        FrameLayout.LayoutParams supportParams = new FrameLayout.LayoutParams(
                dp(99.2f), dp(19.2f), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        supportParams.bottomMargin = dp(2.4f);
        branding.addView(support, supportParams);
        levelText=text("",16,TEXT);center(levelText);
        int trackWidth=dp(520);
        slider=new SeekBar(this);slider.setMax(9);slider.setKeyProgressIncrement(1);slider.setMinimumHeight(dp(32));slider.setProgressTintList(ColorStateList.valueOf(ORANGE));slider.setThumbTintList(ColorStateList.valueOf(ORANGE));slider.setProgressBackgroundTintList(ColorStateList.valueOf(GRAY));slider.setPadding(dp(10),0,dp(10),0);focus(slider,Color.TRANSPARENT,6);
        slider.setOnKeyListener((view,key,event)->key==KeyEvent.KEYCODE_DPAD_RIGHT&&slider.getProgress()==slider.getMax());
        slider.setId(View.generateViewId());power.setNextFocusRightId(slider.getId());
        power.setOnKeyListener((view,key,event)->{if(key!=KeyEvent.KEYCODE_DPAD_RIGHT)return false;if(event.getAction()==KeyEvent.ACTION_DOWN)slider.requestFocusFromTouch();return true;});
        body.addView(slider,new LinearLayout.LayoutParams(trackWidth,dp(32)));
        LinearLayout scale=new LinearLayout(this);scale.setGravity(Gravity.CENTER);scale.setPadding(dp(10),0,dp(10),0);body.addView(scale,new LinearLayout.LayoutParams(trackWidth,-2));TextView low=text("1",12,GRAY),high=text("10",12,GRAY);low.setGravity(Gravity.LEFT);high.setGravity(Gravity.RIGHT);scale.addView(low,new LinearLayout.LayoutParams(0,-2,1));scale.addView(high,new LinearLayout.LayoutParams(0,-2,1));space(12);

        boot=toggle("Start automatically after boot",true);center(boot);
        sectionDivider();
        schedule=toggle("Enable schedule",false);center(schedule);space(8);
        LinearLayout times=row();times.addView(text("Dimmer starts at  ",15,TEXT));startButton=button("23:00",false,()->editTime(true));times.addView(startButton,new LinearLayout.LayoutParams(dp(76),dp(32)));times.addView(text("  and ends at  ",15,TEXT));endButton=button("07:00",false,()->editTime(false));times.addView(endButton,new LinearLayout.LayoutParams(dp(76),dp(32)));space(8);
        daily=toggle("Daily",true);center(daily);space(4);
        LinearLayout weekly=row();weekly.addView(text("Weekly:  ",14,TEXT));String[] names={"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
        for(int i=0;i<7;i++){CheckBox d=new CheckBox(this);d.setText(names[i]);d.setTextColor(TEXT);d.setTextSize(14);d.setButtonTintList(stateColors(ORANGE,GRAY));d.setMinHeight(0);d.setMinimumHeight(0);d.setMinWidth(0);d.setMinimumWidth(0);d.setPadding(dp(2),0,dp(4),0);focus(d,Color.TRANSPARENT,4);days[i]=d;weekly.addView(d,new LinearLayout.LayoutParams(-2,dp(32)));}
        sectionDivider();
        LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);body.addView(actions,new LinearLayout.LayoutParams(-1,dp(38)));
        Button save=button("SAVE SETTINGS",true,this::save);actionFocus(save,ORANGE,ORANGE);actions.addView(save,new LinearLayout.LayoutParams(0,-1,1));
        Space actionGap=new Space(this);actions.addView(actionGap,new LinearLayout.LayoutParams(dp(16),1));
        Button reset=button("RESET ALL TO DEFAULT",false,this::confirmReset);actionFocus(reset,SURFACE,0xff9e180e);actions.addView(reset,new LinearLayout.LayoutParams(0,-1,1));space(16);
        permissions=toggle("Allow display and schedule permissions",false);center(permissions);
        permissions.setOnCheckedChangeListener((v,checked)->{if(!permissionUpdate){refreshPermissions();permissionDialog();}});
        schedule.setOnCheckedChangeListener((v,on)->updateDays());daily.setOnCheckedChangeListener((v,on)->updateDays());
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int n,boolean user){levelText.setText("Choose brightness level ("+(n+1)+"/10)");if(user){Plan.prefs(MainActivity.this).edit().putInt("level",n+1).apply();Plan.refresh(MainActivity.this);}}
            public void onStartTrackingTouch(SeekBar s){}
            public void onStopTrackingTouch(SeekBar s){}
        });
        slider.setOnFocusChangeListener((v,focused)->{v.setBackground(shape(Color.TRANSPARENT,6,focused));});
        load();power.requestFocus();
    }
    private void showSupport() {
        ImageView qr = new ImageView(this);
        qr.setImageResource(R.drawable.support_checkout);
        qr.setScaleType(ImageView.ScaleType.FIT_CENTER);
        qr.setContentDescription("Scan with your phone to open Stripe Checkout");
        FrameLayout content = new FrameLayout(this);
        content.setPadding(dp(24), dp(16), dp(24), dp(16));
        content.addView(qr, new FrameLayout.LayoutParams(dp(200), dp(200), Gravity.CENTER));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Buy me a coffee")
                .setView(content)
                .setPositiveButton("Close", null)
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).requestFocus();
    }

    private final class BrandingPane extends FrameLayout {
        View header,footer;
        BrandingPane(Context context){super(context);}
        @Override protected void onLayout(boolean changed,int l,int t,int r,int b){
            super.onLayout(changed,l,t,r,b);
            if(header==null||footer==null||power==null)return;
            int width=power.getMeasuredWidth(),height=power.getMeasuredHeight();
            int x=(getWidth()-width)/2,y=(header.getBottom()+footer.getTop()-height)/2;
            power.layout(x,y,x+width,y+height);
        }
    }
    private final class FitSurface extends FrameLayout {
        FitSurface(Context context){super(context);setBackgroundColor(BG);}
        @Override protected void onMeasure(int widthSpec,int heightSpec){
            int width=MeasureSpec.getSize(widthSpec),height=MeasureSpec.getSize(heightSpec);setMeasuredDimension(width,height);
            if(getChildCount()>0)getChildAt(0).measure(MeasureSpec.makeMeasureSpec(dp(960),MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(dp(540),MeasureSpec.EXACTLY));
        }
        @Override protected void onLayout(boolean changed,int l,int t,int r,int b){
            if(getChildCount()==0)return;View canvas=getChildAt(0);int width=dp(960),height=dp(540);canvas.layout(0,0,width,height);
            float scale=Math.min((r-l)/(float)width,(b-t)/(float)height);canvas.setPivotX(0);canvas.setPivotY(0);canvas.setScaleX(scale);canvas.setScaleY(scale);canvas.setTranslationX(((r-l)-width*scale)/2f);canvas.setTranslationY(((b-t)-height*scale)/2f);
        }
    }
    private void paintPower(){
        powerIsOn=Plan.active(this,System.currentTimeMillis());power.setText(powerIsOn?"ON":"OFF");power.setTextColor(powerIsOn?BG:TEXT);
        if(powerDrawable==null){powerDrawable=new GradientDrawable();powerDrawable.setShape(GradientDrawable.OVAL);power.setBackground(powerDrawable);}
        powerDrawable.setColor(powerIsOn?ORANGE:SURFACE);paintPowerStroke();power.setContentDescription("Dimmer "+(powerIsOn?"on":"off"));
        if(foreground&&!powerIsOn)startPowerPulse();else stopPowerPulse();
    }
    private void paintPowerStroke(){
        if(powerDrawable==null)return;
        int color=powerIsOn?(power.hasFocus()?TEXT:ORANGE):Color.argb(Math.round(255*powerStrokeOpacity),255,255,255);
        powerDrawable.setStroke(dp(power.hasFocus()?3:1),color);
    }
    private void startPowerPulse(){
        if(powerPulse==null){powerPulse=ValueAnimator.ofFloat(0.35f,1f);powerPulse.setDuration(1500);powerPulse.setRepeatCount(ValueAnimator.INFINITE);powerPulse.setRepeatMode(ValueAnimator.REVERSE);powerPulse.setInterpolator(new AccelerateDecelerateInterpolator());powerPulse.addUpdateListener(animation->{powerStrokeOpacity=(Float)animation.getAnimatedValue();paintPowerStroke();});}
        if(!powerPulse.isStarted())powerPulse.start();
    }
    private void stopPowerPulse(){if(powerPulse!=null)powerPulse.cancel();powerStrokeOpacity=1f;paintPowerStroke();}
    private void load(){loading=true;android.content.SharedPreferences p=Plan.prefs(this);slider.setProgress(Math.max(0,Math.min(9,p.getInt("level",6)-1)));levelText.setText("Choose brightness level ("+(slider.getProgress()+1)+"/10)");boot.setChecked(p.getBoolean("boot",true));schedule.setChecked(p.getBoolean("schedule",false));int mask=p.getInt("days",127);daily.setChecked(p.getBoolean("daily",mask==127));int custom=p.getInt("customDays",mask==127?0:mask);for(int i=0;i<7;i++)days[i].setChecked((custom&(1<<i))!=0);startMinute=p.getInt("start",1380);endMinute=p.getInt("end",420);timeLabels();loading=false;updateDays();refreshPermissions();paintPower();}
    private void updateDays(){if(loading||daily==null||schedule==null)return;daily.setEnabled(schedule.isChecked());for(CheckBox d:days)if(d!=null){boolean enabled=schedule.isChecked()&&!daily.isChecked();d.setEnabled(enabled);d.setAlpha(enabled?1f:0.45f);}}
    private void toggleNow(){boolean on=!Plan.active(this,System.currentTimeMillis());if(on&&!Settings.canDrawOverlays(this)){Toast.makeText(this,"Allow display access first",Toast.LENGTH_SHORT).show();openAccess(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);return;}Plan.override(on);Plan.prefs(this).edit().putInt("level",slider.getProgress()+1).remove("error").apply();Plan.refresh(this);paintPower();}
    private void save(){int mask=0;for(int i=0;i<7;i++)if(days[i].isChecked())mask|=1<<i;if(schedule.isChecked()&&!daily.isChecked()&&mask==0){Toast.makeText(this,"Choose at least one weekday",Toast.LENGTH_SHORT).show();return;}if(schedule.isChecked()&&(!Plan.exact(this)||!Settings.canDrawOverlays(this))){Toast.makeText(this,"Allow display and schedule access first",Toast.LENGTH_SHORT).show();return;}
        android.content.SharedPreferences.Editor e=Plan.prefs(this).edit().putInt("level",slider.getProgress()+1).putBoolean("boot",boot.isChecked()).putBoolean("schedule",schedule.isChecked()).putBoolean("daily",daily.isChecked()).putInt("customDays",mask).putInt("days",daily.isChecked()?127:mask).putInt("start",startMinute).putInt("end",endMinute).putBoolean("endEnabled",true).remove("error");if(schedule.isChecked())Plan.clearOverride();e.apply();Plan.sync(this);paintPower();Toast.makeText(this,"Preferences saved",Toast.LENGTH_SHORT).show();}
    private void editTime(boolean isStart){
        int value=isStart?startMinute:endMinute;LinearLayout fields=new LinearLayout(this);fields.setGravity(Gravity.CENTER);fields.setPadding(dp(24),dp(16),dp(24),dp(16));
        EditText hour=new EditText(this),minute=new EditText(this);for(EditText f:new EditText[]{hour,minute}){f.setTextSize(25);f.setTextColor(TEXT);f.setGravity(Gravity.CENTER);f.setSingleLine();f.setInputType(InputType.TYPE_CLASS_NUMBER);f.setSelectAllOnFocus(true);f.setMinWidth(dp(90));}
        hour.setContentDescription("Hours, zero to 23");minute.setContentDescription("Minutes, zero to 59");hour.setText(String.format(Locale.ROOT,"%02d",value/60));minute.setText(String.format(Locale.ROOT,"%02d",value%60));fields.addView(hour);fields.addView(text(":",25,TEXT));fields.addView(minute);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(isStart?"Start time":"End time").setView(fields).setNegativeButton("Cancel",null).setPositiveButton("Set",null).create();
        View.OnKeyListener keys=(v,key,event)->{if(event.getAction()!=KeyEvent.ACTION_DOWN)return key==KeyEvent.KEYCODE_DPAD_UP||key==KeyEvent.KEYCODE_DPAD_DOWN;if(key==KeyEvent.KEYCODE_DPAD_RIGHT){if(v==hour)minute.requestFocus();else dialog.getButton(AlertDialog.BUTTON_POSITIVE).requestFocus();return true;}if(key==KeyEvent.KEYCODE_DPAD_LEFT){if(v==minute)hour.requestFocus();else dialog.getButton(AlertDialog.BUTTON_NEGATIVE).requestFocus();return true;}if(key==KeyEvent.KEYCODE_DPAD_UP||key==KeyEvent.KEYCODE_DPAD_DOWN){EditText f=(EditText)v;int n;try{n=Integer.parseInt(f.getText().toString());}catch(NumberFormatException e){n=0;}n=Math.floorMod(n+(key==KeyEvent.KEYCODE_DPAD_UP?1:-1),f==hour?24:60);f.setText(String.format(Locale.ROOT,"%02d",n));f.selectAll();return true;}return false;};hour.setOnKeyListener(keys);minute.setOnKeyListener(keys);
        dialog.setOnShowListener(v->{dialog.getWindow().setBackgroundDrawable(shape(SURFACE,12,false));dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ORANGE);dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(a->{int result=ClockInput.parse(hour.getText().toString(),minute.getText().toString());if(result<0){hour.setError("Use 00–23 hours and 00–59 minutes");hour.requestFocus();return;}if(result==(isStart?endMinute:startMinute)){minute.setError("Start and end must differ");minute.requestFocus();return;}if(isStart)startMinute=result;else endMinute=result;timeLabels();dialog.dismiss();});hour.requestFocus();hour.selectAll();});dialog.show();
    }
    private void openAccess(String action){try{startActivity(new Intent(action,Uri.parse("package:"+getPackageName())));}catch(RuntimeException e){try{startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));}catch(RuntimeException ignored){Toast.makeText(this,"Open TV Settings > Apps > Special app access",Toast.LENGTH_LONG).show();}}}
    private void refreshPermissions(){permissionUpdate=true;permissions.setChecked(Settings.canDrawOverlays(this)&&Plan.exact(this));permissionUpdate=false;}
    private void permissionDialog(){String[] entries=Build.VERSION.SDK_INT>=31?new String[]{"Display over other apps","Precise schedules"}:new String[]{"Display over other apps"};new AlertDialog.Builder(this).setTitle("Permissions").setItems(entries,(d,which)->openAccess(which==0?Settings.ACTION_MANAGE_OVERLAY_PERMISSION:Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)).setNegativeButton("Done",null).show();}
    private void confirmReset(){new AlertDialog.Builder(this).setTitle("Reset all to default?").setMessage("Stops dimming and clears preferences. Permissions must be turned off in Android Settings.").setNegativeButton("Cancel",null).setPositiveButton("Reset",(d,w)->{Plan.clearOverride();Plan.prefs(this).edit().clear().apply();Plan.sync(this);load();Toast.makeText(this,"Defaults restored",Toast.LENGTH_SHORT).show();permissionDialog();}).show();}
    private final Handler handler=new Handler(Looper.getMainLooper());private final Runnable refresh=new Runnable(){public void run(){paintPower();refreshPermissions();handler.postDelayed(this,1000);}};
    @Override protected void onResume(){super.onResume();foreground=true;if(power!=null){paintPower();refreshPermissions();handler.post(refresh);}}
    @Override protected void onPause(){foreground=false;stopPowerPulse();handler.removeCallbacks(refresh);super.onPause();}
    @Override public void onWindowFocusChanged(boolean hasFocus){super.onWindowFocusChanged(hasFocus);if(hasFocus&&initialFocusPending&&power!=null){power.requestFocusFromTouch();initialFocusPending=false;}}
}
