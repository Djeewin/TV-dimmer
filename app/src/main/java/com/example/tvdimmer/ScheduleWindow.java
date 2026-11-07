package com.example.tvdimmer;
import java.util.Calendar;
final class ScheduleWindow {
    static Calendar at(Calendar day,int minute){Calendar t=(Calendar)day.clone();t.set(Calendar.HOUR_OF_DAY,minute/60);t.set(Calendar.MINUTE,minute%60);t.set(Calendar.SECOND,0);t.set(Calendar.MILLISECOND,0);return t;}
    static boolean selected(int mask,Calendar day){return (mask&(1<<(day.get(Calendar.DAY_OF_WEEK)-1)))!=0;}
    static boolean active(Calendar now,int mask,int startMinute,int endMinute,boolean endEnabled){for(int i=0;i<8;i++){Calendar day=(Calendar)now.clone();day.add(Calendar.DATE,-i);if(!selected(mask,day))continue;Calendar start=at(day,startMinute);if(start.after(now))continue;if(!endEnabled)return true;Calendar end=at(day,endMinute);if(!end.after(start))end.add(Calendar.DATE,1);return now.before(end);}return false;}
    static long next(Calendar now,int mask,int startMinute,int endMinute,boolean endEnabled){long next=Long.MAX_VALUE;for(int i=-1;i<=8;i++){Calendar day=(Calendar)now.clone();day.add(Calendar.DATE,i);if(!selected(mask,day))continue;Calendar start=at(day,startMinute);if(start.after(now))next=Math.min(next,start.getTimeInMillis());if(endEnabled){Calendar end=at(day,endMinute);if(!end.after(start))end.add(Calendar.DATE,1);if(end.after(now))next=Math.min(next,end.getTimeInMillis());}}return next;}
    static boolean effective(int override,boolean scheduled,boolean manual,boolean window){return override==-1?(scheduled?window:manual):override==1;}
}
