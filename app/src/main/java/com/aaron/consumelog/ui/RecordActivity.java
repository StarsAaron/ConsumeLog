package com.aaron.consumelog.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.aaron.consumelog.R;
import com.aaron.consumelog.adapter.CalendarItemAdapter;
import com.aaron.consumelog.adapter.DayNewsListAdapter;
import com.aaron.consumelog.bean.CustomCalendarItemModel;
import com.aaron.consumelog.bean.RecordBean;
import com.aaron.consumelog.db.dao.RecordDao;
import com.kelin.calendarlistview.library.CalendarHelper;
import com.kelin.calendarlistview.library.CalendarListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class RecordActivity extends AppCompatActivity implements DayNewsListAdapter.NewsOnClickListener {
    public static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat YEAR_MONTH_FORMAT = new SimpleDateFormat("yyyy年MM月");

    @InjectView(R.id.fab)
    FloatingActionButton fab;
    @InjectView(R.id.calendar_listview)
    CalendarListView calendarListView;

    private DayNewsListAdapter dayNewsListAdapter;
    private CalendarItemAdapter calendarItemAdapter;
    private Handler handler = new Handler();
    private RecordDao recordDao;
    private ActionBar actionBar;

    //key:date "yyyy-mm-dd" format.
    private TreeMap<String, List<RecordBean>> listTreeMap = new TreeMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        ButterKnife.inject(this);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //添加记录
                AddRecordActivity.ToAddRecordActivity(RecordActivity.this, AddRecordActivity.TYPE.T_ADD);
            }
        });

        //设置标题
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        dayNewsListAdapter = new DayNewsListAdapter(this, this);
        calendarItemAdapter = new CalendarItemAdapter(this);
        calendarListView.setCalendarListViewAdapter(calendarItemAdapter, dayNewsListAdapter);

        Calendar calendar = Calendar.getInstance();
        actionBar.setTitle(YEAR_MONTH_FORMAT.format(calendar.getTime()));
        actionBar.setElevation(0);
        //加载记录列表数据
        loadNewsList(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
        loadCalendarData(YEAR_MONTH_FORMAT.format(calendar.getTime()));

        // 日历的上拉下拉刷新
        calendarListView.setOnListPullListener(new CalendarListView.onListPullListener() {
            @Override
            public void onRefresh() {//下拉
                if (listTreeMap.size() != 0) {
                    String date = listTreeMap.firstKey();
                    Calendar calendar = CalendarHelper.getCalendarByYearMonthDay(date);
                    calendar.add(Calendar.MONTH, -1);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    loadNewsList(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
                }
            }

            @Override
            public void onLoadMore() {//上拉
                if (listTreeMap.size() != 0) {
                    String date = listTreeMap.lastKey();
                    Calendar calendar = CalendarHelper.getCalendarByYearMonthDay(date);
                    calendar.add(Calendar.MONTH, 1);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    loadNewsList(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
                }
            }
        });

        //日历对应的记录列表刷新
        calendarListView.setOnMonthChangedListener(new CalendarListView.OnMonthChangedListener() {
            @Override
            public void onMonthChanged(String yearMonth) {
                Calendar calendar = CalendarHelper.getCalendarByYearMonth(yearMonth);
                actionBar.setTitle(YEAR_MONTH_FORMAT.format(calendar.getTime()));
                loadCalendarData(yearMonth);
//                Toast.makeText(RecordActivity.this, YEAR_MONTH_FORMAT.format(calendar.getTime()), Toast.LENGTH_SHORT).show();
            }
        });

        calendarListView.setOnCalendarViewItemClickListener(new CalendarListView.OnCalendarViewItemClickListener() {
            @Override
            public void onDateSelected(View View, String selectedDate, int listSection, SelectedDateRegion selectedDateRegion) {

            }
        });
    }

    /**
     * 加载列表数据
     */
    private void loadNewsList(int mYear, int mMonth) {
        if (recordDao == null) {
            recordDao = new RecordDao(RecordActivity.this);
        }
        List<RecordBean> recordBeenList = recordDao.selectRecordByYearAndMonth(mYear, mMonth);
        for (RecordBean recordBean : recordBeenList) {
            if (listTreeMap.get(recordBean._date) != null) {
                List<RecordBean> list = listTreeMap.get(recordBean._date);
                list.add(recordBean);
            } else {
                List<RecordBean> list = new ArrayList<>();
                list.add(recordBean);
                listTreeMap.put(recordBean._date, list);
            }
        }
        if (recordBeenList.size() != 0 && listTreeMap.size() != 0) {
            dayNewsListAdapter.setDateDataMap(listTreeMap);
            dayNewsListAdapter.notifyDataSetChanged();
            calendarItemAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 加载日历数据
     *
     * @param date
     */
    private void loadCalendarData(final String date) {
        handler.removeCallbacks(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String selectDdate = calendarListView.getCurrentSelectedDate();
                if (!TextUtils.isEmpty(selectDdate) && date.equals(selectDdate.substring(0, 7))) {
                    for (String d : listTreeMap.keySet()) {
                        if (date.equals(d.substring(0, 7))) {
                            CustomCalendarItemModel customCalendarItemModel = calendarItemAdapter.getDayModelList().get(d);
                            if (customCalendarItemModel != null) {
                                customCalendarItemModel.setNewsCount(listTreeMap.get(d).size());
                            }

                        }
                    }
                    calendarItemAdapter.notifyDataSetChanged();
                }
            }
        }, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(null);
    }

    @Override
    public void onClick(RecordBean model) {
        AddRecordActivity.ToAddRecordActivity(this, AddRecordActivity.TYPE.T_EDIT, model);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}