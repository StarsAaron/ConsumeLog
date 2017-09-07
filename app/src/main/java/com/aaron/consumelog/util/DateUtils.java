package com.aaron.consumelog.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间格式化工具
 * SimpleDateFormat 不是线程安全的，方法里面的局部变量可以忽略多线程的不安全，但每次需要创建对象效率不高
 */
public class DateUtils {
    public static SimpleDateFormat getFormat(String partten) {
        //return new SimpleDateFormat(partten, Locale.getDefault());
        //推荐下面这种写法
        SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateInstance();
        sdf.applyPattern(partten);
        return sdf;
    }

    /**
     * 获取 HH:mm 格式时间
     *
     * @param time 例如："2013-01-21 15:10:20"
     * @return
     */
    public static String formatTime(String time) {
//        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        //推荐下面这种写法
        SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateInstance();
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(time));
            return c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取时间 yyyyMM 格式
     * @param time milliseconds since January 1, 1970 00:00:00.0 UTC.(获取当前时间System.currentTimeMillis())
     * @return
     */
    public static String getDate(long time) {
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM");
        //推荐下面这种写法
        SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateInstance();
        formatter.applyPattern("yyyyMM");
        Date curDate = new Date(time);
        String str = formatter.format(curDate);
        return str;
    }

    /**
     * 获取时间 yyyyMMdd 格式
     * @param time milliseconds since January 1, 1970 00:00:00.0 UTC.(获取当前时间System.currentTimeMillis())
     * @return
     */
    public static String getDateDay(long time) {
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

        //推荐下面这种写法
        SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateInstance();
        formatter.applyPattern("yyyyMMdd");
        Date curDate = new Date(time);
        String str = formatter.format(curDate);

        return str;
    }

    /**
     * 获取时间 yyyy-MM-dd HH:mm:ss 格式
     * @param time milliseconds since January 1, 1970 00:00:00.0 UTC.(获取当前时间System.currentTimeMillis())
     * @return
     */
    public static String getDateDayTime(long time) {
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //推荐下面这种写法
        SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateInstance();
        formatter.applyPattern("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(time);
        String str = formatter.format(curDate);
        return str;
    }

    /**
     * 获取时间 yyyy-MM-dd HH:mm:ss 格式
     * @param time milliseconds since January 1, 1970 00:00:00.0 UTC.(获取当前时间System.currentTimeMillis())
     * @return
     */
    public static String getDateDayTime2(long time) {
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //推荐下面这种写法
        SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateInstance();
        formatter.applyPattern("yyyy年MM月dd日HH时mm分ss秒");
        Date curDate = new Date(time);
        String str = formatter.format(curDate);
        return str;
    }


    /**
     * 比较时间前后
     *
     * @param stime
     * @param etime
     * @return
     */
    public static boolean compareTime(String stime, String etime) {
//        DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        //推荐下面这种写法
        SimpleDateFormat dateformat = (SimpleDateFormat) DateFormat.getDateInstance();
        dateformat.applyPattern("yyyy-MM-dd");
        try {
            Date d1 = dateformat.parse(stime);
            Date d2 = dateformat.parse(etime);

            return d1.after(d2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Java.util.Date 转 Java.sql.Date
     * @param uDate
     * @return
     */
    public static java.sql.Date uDate2SDate(Date uDate) {
        return new java.sql.Date(uDate.getTime());
    }

    /**
     * 将“yyyy-MM-dd”格式字符串转换成 Java.sql.Date
     * @param str
     * @return
     */
    public static java.sql.Date getSDate(String str) {
        Date date = null;
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) DateFormat.getDateInstance();
        simpleDateFormat.applyPattern("yyyy-MM-dd");
        try {
            date = simpleDateFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new java.sql.Date(date.getTime());
    }
}
