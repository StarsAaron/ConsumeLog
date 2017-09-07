package com.aaron.consumelog.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aaron.consumelog.R;
import com.aaron.consumelog.bean.RecordBean;
import com.aaron.consumelog.ui.AddRecordActivity;
import com.aaron.consumelog.ui.RecordActivity;
import com.kelin.calendarlistview.library.BaseCalendarListAdapter;
import com.kelin.calendarlistview.library.CalendarHelper;
import java.util.Calendar;
import java.util.List;

/**
 * 历史记录列表
 */
public class DayNewsListAdapter extends BaseCalendarListAdapter<RecordBean> {
    private Context context;
    private NewsOnClickListener newsOnClickListener;

    public DayNewsListAdapter(Context context,NewsOnClickListener newsOnClickListener) {
        super(context);
        this.context = context;
        this.newsOnClickListener = newsOnClickListener;
    }

    @Override
    public View getSectionHeaderView(String date, View convertView, ViewGroup parent) {
        HeaderViewHolder headerViewHolder;
        if (convertView != null) {
            headerViewHolder = (HeaderViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(R.layout.listitem_calendar_header, null);
            headerViewHolder = new HeaderViewHolder();
            headerViewHolder.dayText = (TextView) convertView.findViewById(R.id.header_day);
            headerViewHolder.yearMonthText = (TextView) convertView.findViewById(R.id.header_year_month);
            convertView.setTag(headerViewHolder);
        }

        Calendar calendar = CalendarHelper.getCalendarByYearMonthDay(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String dayStr = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        if (day < 10) {
            dayStr = "0" + dayStr;
        }
        headerViewHolder.dayText.setText(dayStr);
        headerViewHolder.yearMonthText.setText(RecordActivity.YEAR_MONTH_FORMAT.format(calendar.getTime()));
        return convertView;
    }

    @Override
    public View getItemView(final RecordBean model, String date, int pos, View convertView, ViewGroup parent) {
        ContentViewHolder contentViewHolder;
        if (convertView != null) {
            contentViewHolder = (ContentViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(R.layout.listitem_calendar_content, null);
            contentViewHolder = new ContentViewHolder();
            contentViewHolder.titleTextView = (TextView) convertView.findViewById(R.id.tv_desc);
            contentViewHolder.timeTextView = (TextView) convertView.findViewById(R.id.tv_time);
            contentViewHolder.newsImageView = (ImageView) convertView.findViewById(R.id.image);
            contentViewHolder.moneyTextView = (TextView) convertView.findViewById(R.id.tv_money);
            contentViewHolder.consumeType = (TextView) convertView.findViewById(R.id.tv_consumeType);
            contentViewHolder.newsItem = (LinearLayout) convertView.findViewById(R.id.ll_news_item);

            convertView.setTag(contentViewHolder);
        }

        contentViewHolder.titleTextView.setText(model._description);
        contentViewHolder.timeTextView.setText(date);
        contentViewHolder.moneyTextView.setText("￥:"+model._consume);
        contentViewHolder.consumeType.setText("分类："+model._consumeType);
        switch(model._consumeType){
            case "衣":
                contentViewHolder.newsImageView.setBackgroundResource(R.mipmap.ic_clothes);
                break;
            case "食":
                contentViewHolder.newsImageView.setBackgroundResource(R.mipmap.ic_food);
                break;
            case "住":
                contentViewHolder.newsImageView.setBackgroundResource(R.mipmap.ic_live);
                break;
            case "行":
                contentViewHolder.newsImageView.setBackgroundResource(R.mipmap.ic_travel);
                break;
        }

        contentViewHolder.newsItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(newsOnClickListener != null){
                    newsOnClickListener.onClick(model);
                }
            }
        });
        return convertView;
    }

    private static class HeaderViewHolder {
        TextView dayText;
        TextView yearMonthText;
    }

    private static class ContentViewHolder {
        LinearLayout newsItem;
        TextView titleTextView;
        TextView timeTextView;
        TextView moneyTextView;
        TextView consumeType;
        ImageView newsImageView;
    }

    /**
     * 列表项点击事件
     */
    public interface NewsOnClickListener{
        void onClick(RecordBean model);
    }
}
