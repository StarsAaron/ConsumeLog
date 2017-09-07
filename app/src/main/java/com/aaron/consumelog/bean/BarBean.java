package com.aaron.consumelog.bean;

/**
 * Created by Aaron on 2017/8/30.
 */

public class BarBean {
    public int mDay;//几号
    public String mDate;//日期字符串
    public int dayOfWeek;//星期几
    public float mTotalMoney=0f;//总金额

    /**
     * 获取星期号数字符串
     * @return
     */
    public String getDayOfWeekAndDate(){
        String msg = "";
        switch(dayOfWeek){
            case 1:
                msg = "一("+mDay+")";
                break;
            case 2:
                msg = "二("+mDay+")";
                break;
            case 3:
                msg = "三("+mDay+")";
                break;
            case 4:
                msg = "四("+mDay+")";
                break;
            case 5:
                msg = "五("+mDay+")";
                break;
            case 6:
                msg = "六("+mDay+")";
                break;
            case 7:
                msg = "日("+mDay+")";
                break;
        }
        return msg;
    }
}
