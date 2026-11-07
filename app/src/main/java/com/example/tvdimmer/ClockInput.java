package com.example.tvdimmer;
final class ClockInput {
    static int parse(String hour,String minute){if(hour==null||minute==null||!hour.matches("[0-9]{1,2}")||!minute.matches("[0-9]{1,2}"))return -1;int h=Integer.parseInt(hour),m=Integer.parseInt(minute);return h<24&&m<60?h*60+m:-1;}
}
