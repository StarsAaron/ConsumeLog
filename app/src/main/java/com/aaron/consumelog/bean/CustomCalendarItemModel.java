package com.aaron.consumelog.bean;

import com.kelin.calendarlistview.library.BaseCalendarItemModel;

/**
 * Created by kelin on 16-7-20.
 */
public class CustomCalendarItemModel extends BaseCalendarItemModel {
    private int newsCount;

    public int getNewsCount() {
        return newsCount;
    }

    public void setNewsCount(int newsCount) {
        this.newsCount = newsCount;
    }

}
