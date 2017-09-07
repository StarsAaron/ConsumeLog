package com.kelin.calendarlistview.library;

/**
 * 日历子数据Bean
 */
public class BaseCalendarItemModel {
    private boolean isCurrentMonth; //今天是否当前月份
    private String dayNumber; //几号
    private long timeMill; //日期秒数
    private boolean isToday; //是否是今天
    private boolean isHoliday; //是否星期六日
    private Status status; //状态

    public enum Status {
        NONE,
        DISABLE,
        SELECTED
    }

    public boolean isCurrentMonth() {
        return isCurrentMonth;
    }

    public void setCurrentMonth(boolean currentMonth) {
        isCurrentMonth = currentMonth;
    }

    public String getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(String dayNumber) {
        this.dayNumber = dayNumber;
    }

    public long getTimeMill() {
        return timeMill;
    }

    public void setTimeMill(long timeMill) {
        this.timeMill = timeMill;
    }

    public boolean isToday() {
        return isToday;
    }

    public void setToday(boolean today) {
        isToday = today;
    }

    public boolean isHoliday() {
        return isHoliday;
    }

    public void setHoliday(boolean holiday) {
        isHoliday = holiday;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
