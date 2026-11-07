package com.example.tvdimmer;
import org.junit.Test;
import static org.junit.Assert.*;
public class ClockInputTest {
 @Test public void midnightAndLastMinute(){assertEquals(0,ClockInput.parse("00","00"));assertEquals(1439,ClockInput.parse("23","59"));}
 @Test public void singleDigits(){assertEquals(427,ClockInput.parse("7","7"));}
 @Test public void outsideRange(){assertEquals(-1,ClockInput.parse("24","00"));assertEquals(-1,ClockInput.parse("23","60"));}
 @Test public void malformedInput(){for(String s:new String[]{"","-1","000"," 7","7 ","2a","+2"})assertEquals(-1,ClockInput.parse(s,"00"));assertEquals(-1,ClockInput.parse(null,"12"));assertEquals(-1,ClockInput.parse("12",null));}
}
