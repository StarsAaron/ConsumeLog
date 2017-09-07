package com.aaron.consumelog.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aaron.consumelog.R;
import com.aaron.consumelog.adapter.RecentRecordAdapter;
import com.aaron.consumelog.bean.BarBean;
import com.aaron.consumelog.bean.RecordBean;
import com.aaron.consumelog.bean.SumBean;
import com.aaron.consumelog.db.dao.RecordDao;
import com.aaron.consumelog.util.DateUtils;
import com.aaron.consumelog.util.FileUtil;
import com.leon.lfilepickerlibrary.LFilePicker;
import com.leon.lfilepickerlibrary.utils.Constant;
import com.othershe.nicedialog.NiceDialog;
import com.othershe.nicedialog.ViewConvertListener;
import com.othershe.nicedialog.ViewHolder;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.PieChartView;

import static android.R.attr.format;


public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.fab)
    FloatingActionButton fab;
    @InjectView(R.id.columnChartView)
    ColumnChartView columnChartView;
    @InjectView(R.id.btn_pre_week)
    ImageView btnPreWeek;
    @InjectView(R.id.btn_next_week)
    ImageView btnNextWeek;
    @InjectView(R.id.pieChartView)
    PieChartView pieChartView;
    @InjectView(R.id.tv_null)
    TextView tv_null;
    @InjectView(R.id.ll_recent)
    LinearLayout llRecent;
    @InjectView(R.id.lv_recent_list)
    RecyclerView recent_list;
    @InjectView(R.id.tv_title)
    TextView tvTitle;

    private boolean hasLabels = true; //柱形图是否显示柱标
    private boolean hasLabelForSelected = false;//是否在选中的时候显示柱标
    private ColumnChartData columnChartData;
    private PieChartData pieChartData;
    private RecordDao recordDao;
    private int offset = 0;
    private static final int REQUEST_FILE_PATH = 1000;//请求文件路径
    private static final int REQUEST_DIR_PATH = 2000;//请求文件夹路径

    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x100://清空记录
                    if (recordDao == null) {
                        recordDao = new RecordDao(MainActivity.this);
                    }
                    if (recordDao.deleteAll()) {
                        Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        initView(offset);
                    } else {
                        Toast.makeText(MainActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 0x101://恢复记录
                    Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
                    initView(offset);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //添加记录
                AddRecordActivity.ToAddRecordActivity(MainActivity.this, AddRecordActivity.TYPE.T_ADD);
            }
        });

        columnChartView.setOnValueTouchListener(new ColumnChartValueTouchListener());
        pieChartView.setOnValueTouchListener(new PieChartValueTouchListener());
        if (savedInstanceState != null) {
            offset = savedInstanceState.getInt("offset");
        }

        initView(offset);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView(offset);
    }

    /**
     * 初始化视图
     * @param offset
     */
    private void initView(int offset) {
        getRecentRecord();
        drawColumnView(offset);
        drawPieChartView(offset);
    }

    /**
     * 获取最近记录
     */
    private void getRecentRecord() {
        if (recordDao == null) {
            recordDao = new RecordDao(MainActivity.this);
        }

        List<RecordBean> recordBeanList = recordDao.selectTopRecord(5);
//        Collections.reverse(recordBeanList);    // 实现list集合逆序排列
        if (recordBeanList != null && recordBeanList.size() != 0) {
            recent_list.setVisibility(View.VISIBLE);
            recent_list.setAdapter(new RecentRecordAdapter(this, recordBeanList));
            recent_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            //设置Item增加、移除动画
            recent_list.setItemAnimator(new DefaultItemAnimator());
        }else{
            recent_list.setVisibility(View.GONE);
        }

        //让处于ScrollView或者RecyclerView1 顶端的某个控件获得焦点,防止下面的listView自动滑动到顶部
        pieChartView.setFocusableInTouchMode(true);
        pieChartView.requestFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myHandler != null) {
            myHandler.removeCallbacks(null);
        }
    }

    /**
     * 柱形图动画
     */
    private void columnChartViewAnimation() {
        for (Column column : columnChartData.getColumns()) {
            for (SubcolumnValue value : column.getValues()) {
                value.setTarget(value.getValue() + 10);
            }
        }
        columnChartView.startDataAnimation();
    }

    private void pieChartViewAnimation() {
        for (SliceValue value : pieChartData.getValues()) {
            value.setTarget(value.getValue() + 10);
        }
        pieChartView.startDataAnimation();
    }

    /**
     * 初始化柱形视图
     *
     * @param offset 剩一个星期传入-1，这个星期传入0，下一个星期传入1，以此类推
     */
    private void drawColumnView(int offset) {
        if (recordDao == null) {
            recordDao = new RecordDao(MainActivity.this);
        }
        List<BarBean> barBeenList = getCurrentWeek(offset);
        tvTitle.setText(getWeekOfMonthString(barBeenList.get(3).mDate));

        recordDao.selectRecordBetween(barBeenList.get(0).mDate
                , barBeenList.get(barBeenList.size() - 1).mDate, barBeenList);//柱形图数据
        if (checkListIsNull(barBeenList)) {
            columnChartView.setVisibility(View.INVISIBLE);
            tv_null.setVisibility(View.VISIBLE);
            return;
        } else {
            columnChartView.setVisibility(View.VISIBLE);
            tv_null.setVisibility(View.GONE);
        }
        List<Column> columnList = new ArrayList<>();
        List<AxisValue> axisValueList = new ArrayList<>();//Y轴坐标数据
        int i = 0;
        for (BarBean barBean : barBeenList) {
            List<SubcolumnValue> values = new ArrayList<>();
            values.add(new SubcolumnValue(barBean.mTotalMoney, ChartUtils.nextColor()));
//            values.add(new SubcolumnValue(barBean.mTotalMoney, Color.parseColor("#ffffff")));
            Column column = new Column(values);
            column.setHasLabels(hasLabels);
            column.setHasLabelsOnlyForSelected(hasLabelForSelected);
            columnList.add(column);

            //X轴
            AxisValue axisValue = new AxisValue(i).setLabel(barBean.getDayOfWeekAndDate());
            axisValueList.add(axisValue);
            i++;
        }
        // --------- 测试数据 START----------------
//        for(int j=0;j<7;j++){
//            List<SubcolumnValue> values = new ArrayList<>();
//            values.add(new SubcolumnValue((float)Math.random(), ChartUtils.pickColor()));
//            Column column = new Column(values);
//            column.setHasLabels(hasLabels);
//            columnList.add(column);
//        }
        // --------- 测试数据 END----------------
        columnChartData = new ColumnChartData(columnList);
        Axis axisX = new Axis();
        axisX.setHasLines(false);
        axisX.setName("星期(单位：元)");
        axisX.setInside(false);
        axisX.setLineColor(Color.parseColor("#ffffff"));
        axisX.setTextColor(Color.parseColor("#ffffff"));
        axisX.setValues(axisValueList);
        columnChartData.setAxisXBottom(axisX);
        columnChartView.setColumnChartData(columnChartData);
        columnChartView.setHorizontalScrollBarEnabled(true);
        columnChartView.setZoomEnabled(false);
    }

    /**
     * 判断柱形数据列表是否为0
     *
     * @param barBeenList
     * @return
     */
    private boolean checkListIsNull(List<BarBean> barBeenList) {
        for (BarBean bb : barBeenList) {
            if ((int) (bb.mTotalMoney * 100) != 0) {
//                oldOffset = offset;
                return false;
            }
        }

        Toast.makeText(this, "没有数据", Toast.LENGTH_SHORT).show();
//        offset = oldOffset;//恢复当前日期偏移量
        return true;
    }


    /**
     * 绘制饼图
     */
    private void drawPieChartView(int offset) {
        if (recordDao == null) {
            recordDao = new RecordDao(MainActivity.this);
        }
        List<BarBean> barBeenList = getCurrentWeek(offset);
        List<SumBean> sumBeanList = recordDao.selectRecordBetweenDateGroupByConsumeType(
                barBeenList.get(0).mDate
                , barBeenList.get(barBeenList.size() - 1).mDate);//饼图数据
        List<SliceValue> values = new ArrayList<>();
        if (sumBeanList.size() == 0) {
            pieChartView.setVisibility(View.GONE);
        } else {
            pieChartView.setVisibility(View.VISIBLE);
        }
        for (SumBean sb : sumBeanList) {
            SliceValue sliceValue = new SliceValue(sb.total, ChartUtils.nextColor());
            if (!TextUtils.isEmpty(sb.consumeType)) {
                sliceValue.setLabel(sb.consumeType + ":" + sb.total + "元");
            }
            values.add(sliceValue);
        }
        pieChartData = new PieChartData(values);
        pieChartData.setHasLabels(hasLabels);
        pieChartData.setHasLabelsOutside(false);
        pieChartData.setHasCenterCircle(true);

        pieChartData.setCenterText1FontSize(ChartUtils.px2sp(getResources().getDisplayMetrics().scaledDensity,
                (int) getResources().getDimension(R.dimen.pie_centertext_size)));
        pieChartData.setCenterText1("本星期统计");
        pieChartView.setPieChartData(pieChartData);
    }

    /**
     * 获取当前星期周一和周日是多少号，星期一为第一天
     *
     * @param weekOffset 剩一个星期传入-1，这个星期传入0，下一个星期传入1，以此类推
     * @return
     */
    private static List<BarBean> getCurrentWeek(int weekOffset) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 设置时间格式
        Calendar cal = Calendar.getInstance();
        int n = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (n == 0) {
            n = 7;
        }

        List<BarBean> barBeenList = new ArrayList<>();
        BarBean barBean = new BarBean();
        cal.add(Calendar.DATE, -(n - 1) + weekOffset * 7);// 本周一的日期
        barBean.mDate = sdf.format(cal.getTime());
        barBean.mDay = cal.get(Calendar.DATE);
        barBean.dayOfWeek = 1;
        barBeenList.add(barBean);

        for (int j = 1; j <= 6; j++) {
            cal.add(Calendar.DATE, 1);
            barBean = new BarBean();
            barBean.mDate = sdf.format(cal.getTime());
            barBean.mDay = cal.get(Calendar.DATE);
            barBean.dayOfWeek = j + 1;
            barBeenList.add(barBean);
        }
        return barBeenList;
    }

    /**
     * 获取月份中的第几周
     *
     * @param time 时间
     * @return 8 月 2 周
     */
    public static String getWeekOfMonthString(String time) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        long time2 = new Date().getTime();
        try {
            time2 = format.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        StringBuilder builder = new StringBuilder();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(time2));
        builder.append(cal.get(Calendar.MONTH) + 1);
        builder.append(" 月 ");
        builder.append(cal.get(Calendar.WEEK_OF_MONTH) + " 周");
        return builder.toString();
    }

    @OnClick({R.id.btn_pre_week, R.id.btn_next_week, R.id.iv_more})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_pre_week: // 前一个星期
                offset--;
                initView(offset);
                break;
            case R.id.btn_next_week: //下一个星期
                offset++;
                initView(offset);
                break;
            case R.id.iv_more://菜单
                NiceDialog.init()
                        .setLayoutId(R.layout.layout_main_menu)
                        .setConvertListener(new ViewConvertListener() {
                            @Override
                            public void convertView(ViewHolder holder, final NiceDialog dialog) {
                                holder.setOnClickListener(R.id.tv_export, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();

                                        new LFilePicker()
                                                .withActivity(MainActivity.this)
                                                .withRequestCode(REQUEST_DIR_PATH)
                                                .withTitle("请选择导出文件夹")
                                                .withIconStyle(Constant.ICON_STYLE_BLUE)
                                                .withChooseType(Constant.CHOOSE_DIR)
                                                .withMutilyMode(false)
                                                .start();
                                    }
                                });

                                holder.setOnClickListener(R.id.tv_import, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();

                                        new LFilePicker()
                                                .withActivity(MainActivity.this)
                                                .withRequestCode(REQUEST_FILE_PATH)
                                                .withTitle("请选择导入的文件")
                                                .withIconStyle(Constant.ICON_STYLE_BLUE)
                                                .withChooseType(Constant.CHOOSE_FILE)
                                                .withMutilyMode(false)
                                                .start();
                                    }
                                });
                                holder.setOnClickListener(R.id.tv_clear, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();

                                        NiceDialog.init()
                                                .setLayoutId(R.layout.confirm_layout)
                                                .setDimAmount(0.3f)
                                                .setMargin(60)
                                                .setOutCancel(false)
                                                .setConvertListener(new ViewConvertListener() {
                                                    @Override
                                                    public void convertView(ViewHolder holder, final NiceDialog dialog) {
                                                        holder.setText(R.id.title, "提示");
                                                        holder.setText(R.id.message, "确定要清空数据记录吗?");
                                                        holder.setOnClickListener(R.id.cancel, new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                dialog.dismiss();
                                                            }
                                                        });
                                                        holder.setOnClickListener(R.id.ok, new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                myHandler.sendEmptyMessage(0x100);
                                                                dialog.dismiss();
                                                            }
                                                        });
                                                    }
                                                })
                                                .show(getSupportFragmentManager());
                                    }
                                });
                                holder.setOnClickListener(R.id.tv_about, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                        final StringBuffer message = new StringBuffer();
                                        message.append("作者：Aaron\n邮箱：103514303@qq.com\n版本：V");
                                        try {
                                            String version = getPackageManager().getPackageInfo(MainActivity.this.getPackageName(), 0).versionName;
                                            message.append(version + "\n\n");
                                        } catch (PackageManager.NameNotFoundException e) {
                                            e.printStackTrace();
                                        }

                                        NiceDialog.init()
                                                .setLayoutId(R.layout.confirm_layout)
                                                .setDimAmount(0.3f)
                                                .setMargin(60)
                                                .setOutCancel(true)
                                                .setConvertListener(new ViewConvertListener() {
                                                    @Override
                                                    public void convertView(ViewHolder holder, final NiceDialog dialog) {
                                                        holder.setText(R.id.title, "关于");
                                                        holder.setText(R.id.message, message.toString());
                                                        holder.getView(R.id.line).setVisibility(View.GONE);
                                                        holder.getView(R.id.ll_bar).setVisibility(View.GONE);
                                                    }
                                                })
                                                .show(getSupportFragmentManager());
                                    }
                                });
                            }
                        })
                        .setShowBottom(true)
                        .show(getSupportFragmentManager());
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { //备份
            List<String> list = data.getStringArrayListExtra("paths");
            final String path = list.get(0);
            if (requestCode == REQUEST_DIR_PATH) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String fileName = "支出统计_" + DateUtils.getDateDayTime2(System.currentTimeMillis());
                        FileUtil fileUtil = new FileUtil(getApplicationContext());
                        Message message = myHandler.obtainMessage();
                        if (fileUtil.backupDatabase(RecordDao.db_name, path + File.separator + fileName)) {
                            message.obj = "备份成功！";
                        } else {
                            message.obj = "备份失败！";
                        }
                        message.what = 0x101;
                        myHandler.sendMessage(message);
                    }
                }).start();
            }

            if (requestCode == REQUEST_FILE_PATH) {//恢复
                NiceDialog.init()
                        .setLayoutId(R.layout.confirm_layout)
                        .setDimAmount(0.3f)
                        .setMargin(60)
                        .setOutCancel(true)
                        .setConvertListener(new ViewConvertListener() {
                            @Override
                            public void convertView(ViewHolder holder, final NiceDialog dialog) {
                                holder.setText(R.id.title, "提示");
                                holder.setText(R.id.message, "恢复的数据会把当前应用的数据都覆盖掉，确定要恢复数据吗？");
                                holder.setOnClickListener(R.id.ok, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                File file = new File(path);
                                                FileUtil fileUtil = new FileUtil(getApplicationContext());
                                                Message message = myHandler.obtainMessage();
                                                if (file.exists()) {
                                                    if (fileUtil.restoteDatabase(RecordDao.db_name, path)) {
                                                        message.obj = "恢复成功！";
                                                    } else {
                                                        message.obj = "恢复失败！";
                                                    }
                                                } else {
                                                    message.obj = "文件不存在！";
                                                }
                                                message.what = 0x101;
                                                myHandler.sendMessage(message);
                                            }
                                        }).start();
                                    }
                                });
                            }
                        })
                        .show(getSupportFragmentManager());
            }
        }
    }

    /**
     * 柱形图点击监听
     */
    private class ColumnChartValueTouchListener implements ColumnChartOnValueSelectListener {

        @Override
        public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
            Intent intent = new Intent(MainActivity.this, RecordActivity.class);
            startActivity(intent);
        }

        @Override
        public void onValueDeselected() {

        }

    }

    /**
     * 饼图点击监听
     */
    private class PieChartValueTouchListener implements PieChartOnValueSelectListener {

        @Override
        public void onValueSelected(int arcIndex, SliceValue value) {
//            Toast.makeText(MainActivity.this, "Selected: " + value, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, RecordActivity.class);
            startActivity(intent);
        }

        @Override
        public void onValueDeselected() {

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("offset", offset);
    }
}