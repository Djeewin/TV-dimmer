package com.example.tvdimmer;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
public class ScheduleWindowTest {
 Calendar at(int day,int hour,int minute){Calendar t=Calendar.getInstance(TimeZone.getTimeZone("Europe/London"));t.clear();t.set(2026,Calendar.NOVEMBER,day,hour,minute);return t;}
 boolean active(int day,int hour,int minute,int mask){return ScheduleWindow.active(at(day,hour,minute),mask,1380,420,true);}
 @Test public void overnightBoundaries(){assertFalse(active(7,22,59,127));assertTrue(active(7,23,0,127));assertTrue(active(8,0,0,127));assertTrue(active(8,6,59,127));assertFalse(active(8,7,0,127));}
 @Test public void weekdayOwnsMorning(){int friday=1<<(Calendar.FRIDAY-1);assertTrue(active(7,6,59,friday));assertFalse(active(7,23,0,friday));assertFalse(active(8,6,0,friday));}
 @Test public void previousStartNextEnd(){int friday=1<<(Calendar.FRIDAY-1);assertEquals(at(7,7,0).getTimeInMillis(),ScheduleWindow.next(at(7,6,0),friday,1380,420,true));}
 @Test public void nextStartAfterEnd(){assertEquals(at(7,23,0).getTimeInMillis(),ScheduleWindow.next(at(7,7,0),127,1380,420,true));}
 @Test public void emptyMask(){assertFalse(active(7,23,30,0));assertEquals(Long.MAX_VALUE,ScheduleWindow.next(at(7,12,0),0,1380,420,true));}
 @Test public void manualOverride(){assertTrue(ScheduleWindow.effective(1,true,false,false));assertFalse(ScheduleWindow.effective(0,true,true,true));assertTrue(ScheduleWindow.effective(-1,true,false,true));assertFalse(ScheduleWindow.effective(-1,true,true,false));}
 @Test public void daytimeWindow(){assertTrue(ScheduleWindow.active(at(7,12,0),127,600,840,true));assertFalse(ScheduleWindow.active(at(7,14,0),127,600,840,true));}
 @Test public void daylightSavingEnd(){Calendar t=at(25,6,59);t.set(Calendar.MONTH,Calendar.OCTOBER);assertTrue(ScheduleWindow.active(t,127,1380,420,true));t.set(Calendar.HOUR_OF_DAY,7);assertFalse(ScheduleWindow.active(t,127,1380,420,true));}
}
