package com.wotingfm.util;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具类
 * @author 辛龙
 */
@SuppressLint("SimpleDateFormat")
public class TimeUtils {
    public static String converTime(long time) {
        //今天已经度过的时间
        int todaytime = Integer.parseInt(getHour(System.currentTimeMillis()))*60*60
                +Integer.parseInt(getMinute(System.currentTimeMillis()))*60;

        long currentSeconds = System.currentTimeMillis() / 1000;// 当前系统时间
        long timeGap = currentSeconds - time / 1000;			// 与现在时间相差秒数
        String timeStr = null;
        if (timeGap > (2 * 24 * 60 * 60 + todaytime)) {			// 大于2天以上就返回标准时间
            timeStr = getDayTime(time) + " " + getMinTime(time);
        } else if (timeGap >( 24 * 60 * 60 + todaytime)) {		// 大于1天以上
            timeStr = "前天 " + getMinTime(time);
        } else if (timeGap > todaytime) {						// 大于0秒以上
            timeStr =  "昨天 " + getMinTime(time);
        } else {
            timeStr = "今天 " + getMinTime(time);
        }
        return timeStr;
    }

    public static String getChatTime(long time) {
        return getMinTime(time);
    }

//	public static String getPrefix(long time) {
//		long currentSeconds = System.currentTimeMillis();
//		long timeGap = currentSeconds - time;// 与现在时间差
//		String timeStr = null;
//		if (timeGap > 24 * 3 * 60 * 60 * 1000) {
//			timeStr = getDayTime(time) + " " + getMinTime(time);
//		} else if (timeGap > 24 * 2 * 60 * 60 * 1000) {
//			timeStr = "前天 " + getMinTime(time);
//		} else if (timeGap > 24 * 60 * 60 * 1000) {
//			timeStr = "昨天 " + getMinTime(time);
//		} else {
//			timeStr = "今天 " + getMinTime(time);
//		}
//		return timeStr;
//	}

    public static String getDayTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("MM-dd");
        return format.format(new Date(time));
    }

    public static String getHour(long time) {
        SimpleDateFormat format = new SimpleDateFormat("HH");
        return format.format(new Date(time));
    }

    public static String getMinute(long time) {
        SimpleDateFormat format = new SimpleDateFormat("mm");
        return format.format(new Date(time));
    }

    public static String getMinTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(new Date(time));
    }

    public static int getTime(long time) {
        int h = Integer.parseInt(new SimpleDateFormat("HH").format(new Date(time)));
        int m = Integer.parseInt(new SimpleDateFormat("mm").format(new Date(time)));
        int s = Integer.parseInt(new SimpleDateFormat("ss").format(new Date(time)));
        int _time = h*60*60+m*60+s;
//        Log.e("时分秒", h+"-"+m+"-"+s+"");
        return _time;
    }

    /**
     * 获取当前时间
     * @param time  "yyyy-MM-dd    HH:mm:ss "
     * @return
     */
    public static  String getTime(String type,long time){
        SimpleDateFormat    formatter    =   new    SimpleDateFormat    (type);
        Date    curDate    =   new    Date(time);
        String    addtime    =    formatter.format(curDate);
        return addtime;
    }


    /**
     * 戳转时间
     */
    public static String date2TimeStamp(String year){

        String date_str=year.replaceAll("年","-").replaceAll("月","-").replaceAll(" ","").replaceAll("日","");

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return String.valueOf(sdf.parse(date_str).getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 时间转戳
     */
    public static String timeStamp2Date(String seconds) {
        String format="yyyy-MM-dd";
        if(seconds == null || seconds.isEmpty() || seconds.equals("null")){
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String s=sdf.format(new Date(Long.valueOf(seconds)));
        return s.replaceFirst("-","年").replaceFirst("-","月")+"日";
    }

    /**
     * 得到现在小时
     */
    public static int getHour() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        String hour;
        hour = dateString.substring(11, 13);
        return Integer.parseInt(hour);
    }

    /**
     * 得到现在分钟
     */
    public static int getMinute() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        String min;
        min = dateString.substring(14, 16);
        return Integer.parseInt(min);
    }

    public static int getWeek(long timeStamp) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(new Date(timeStamp));
        int d = cd.get(Calendar.DAY_OF_WEEK);
        // 获取指定日期转换成星期几
//        if (mydate == 1) {/
//            week = "周日";
//        } else if (mydate == 2) {
//            week = "周一";
//        } else if (mydate == 3) {
//            week = "周二";
//        } else if (mydate == 4) {
//            week = "周三";
//        } else if (mydate == 5) {
//            week = "周四";
//        } else if (mydate == 6) {
//            week = "周五";
//        } else if (mydate == 7) {
//            week = "周六";
//        }
        return d;
    }
}
