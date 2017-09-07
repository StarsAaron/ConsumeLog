package com.aaron.consumelog.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aaron on 2017/8/30.
 */

public class WeekUtils {


    public static void main(String[] args){
        getWeekByDate(new Date());

        getLastWeek();

        getCurrentWeek();
    }

    private static String[] getWeekByDate(Date time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 设置时间格式
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        // 判断要计算的日期是否是周日，如果是则减一天计算周六的，否则会出问题，计算到下一周去了
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);// 获得当前日期是一个星期的第几天
        if (1 == dayWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        System.out.println("要计算日期为:" + sdf.format(cal.getTime())); // 输出要计算日期
        cal.setFirstDayOfWeek(Calendar.MONDAY);// 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        int day = cal.get(Calendar.DAY_OF_WEEK);// 获得当前日期是一个星期的第几天
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);// 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        String imptimeBegin = sdf.format(cal.getTime());
        System.out.println("所在周星期一的日期：" + imptimeBegin);
        cal.add(Calendar.DATE, 2);
        String imptimeMi = sdf.format(cal.getTime());
        System.out.println("所在周星期三的日期：" + imptimeMi);
        cal.add(Calendar.DATE, 4);
        String imptimeEnd = sdf.format(cal.getTime());
        System.out.println("所在周星期五的日期：" + imptimeEnd);

        return new String[]{imptimeBegin,imptimeEnd};
    }

    /**
     * 获取上个星期每天对应多少号，星期一为第一天
     * @return
     */
    public static Map<String, String> getLastWeek() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 设置时间格式
        Map<String, String> map = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        int n = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (n == 0) {
            n = 7;
        }
        cal.add(Calendar.DATE, -(7 + (n - 1)));// 上周一的日期
        Date monday = cal.getTime();

        map.put("monday", sdf.format(cal.getTime()));

        cal.add(Calendar.DATE, 1);
        Date tuesday = cal.getTime();
        map.put("tuesday", sdf.format(cal.getTime()));

        cal.add(Calendar.DATE, 1);
        Date wednesday = cal.getTime();
        map.put("wednesday", sdf.format(cal.getTime()));

        cal.add(Calendar.DATE, 1);
        Date thursday = cal.getTime();
        map.put("thursday", sdf.format(cal.getTime()));

        cal.add(Calendar.DATE, 1);
        Date friday = cal.getTime();
        map.put("friday", sdf.format(cal.getTime()));

        cal.add(Calendar.DATE, 1);
        Date saturday = cal.getTime();
        map.put("saturday", sdf.format(cal.getTime()));

        cal.add(Calendar.DATE, 1);
        Date sunday = cal.getTime();
        map.put("sunday", sdf.format(cal.getTime()));
        return map;
    }

    /**
     * 获取当前星期周一和周日是多少号，星期一为第一天
     * @return
     */
    public static String[] getCurrentWeek(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 设置时间格式
        Calendar cal = Calendar.getInstance();
        int n = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (n == 0) {
            n = 7;
        }
        String[] dateStr = new String[2];
        cal.add(Calendar.DATE, -(n - 1));// 本周一的日期
        dateStr[0] = sdf.format(cal.getTime());

        cal.add(Calendar.DATE, 6);
        dateStr[1] = sdf.format(cal.getTime());

        return dateStr;
    }
}
