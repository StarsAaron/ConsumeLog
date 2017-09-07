package com.aaron.consumelog.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aaron.consumelog.R;
import com.aaron.consumelog.bean.RecordBean;
import com.aaron.consumelog.db.dao.RecordDao;
import com.othershe.nicedialog.NiceDialog;
import com.othershe.nicedialog.ViewConvertListener;
import com.othershe.nicedialog.ViewHolder;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.qqtheme.framework.picker.DatePicker;
import cn.qqtheme.framework.picker.OptionPicker;
import cn.qqtheme.framework.util.ConvertUtils;
import cn.qqtheme.framework.widget.WheelView;


public class AddRecordActivity extends AppCompatActivity {

    @InjectView(R.id.et_time)
    TextView etTime;
    @InjectView(R.id.et_money)
    EditText etMoney;
    @InjectView(R.id.et_description)
    EditText etDescription;
    @InjectView(R.id.btn_save)
    Button btnSave;
    @InjectView(R.id.btn_delete)
    Button btnDelete;
    @InjectView(R.id.tv_type)
    TextView etType;


    private String chooseConsume = "";
    private boolean isFromEdit = false;//是否来自编辑页面
    private RecordBean recordBean = null;//来自编辑请求的RecordBean

    private int mYear, mMonth, mDay;

    public enum TYPE {
        T_EDIT, T_ADD
    }

    public static void ToAddRecordActivity(Context context, TYPE type, RecordBean model) {
        Intent intent = new Intent(context, AddRecordActivity.class);

        switch (type) {
            case T_ADD:
                intent.putExtra("Type", "add");
                context.startActivity(intent);
                break;
            case T_EDIT:
                if (model != null) {
                    intent.putExtra("RecordBean", model);
                    intent.putExtra("Type", "edit");
                    context.startActivity(intent);
                }
                break;
        }
    }

    public static void ToAddRecordActivity(Context context, TYPE type) {
        ToAddRecordActivity(context, type, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);
        ButterKnife.inject(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 设置时间格式
        Calendar calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH) + 1;
        mDay = calendar.get(Calendar.DATE);

        etTime.setText(sdf.format(calendar.getTime()));
        recordBean = new RecordBean();//放在前面

        Intent intent = getIntent();
        switch (intent.getStringExtra("Type")) {
            case "add":
                isFromEdit = false;
                getSupportActionBar().setTitle("添加记录");
                break;
            case "edit":
                isFromEdit = true;
                getSupportActionBar().setTitle("编辑记录");
                recordBean = (RecordBean) intent.getSerializableExtra("RecordBean");
                initEditDate(recordBean);
                btnDelete.setVisibility(View.VISIBLE);
                break;
        }
    }

    //来自编辑请求，进行空间数据初始化
    private void initEditDate(RecordBean recordBean) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setGroupingUsed(false);//不加，分割
        etTime.setText(recordBean._date);
        etMoney.setText(nf.format(recordBean._consume));
        etType.setText(recordBean._consumeType);
        etDescription.setText(recordBean._description);
    }

    @OnClick({R.id.et_time, R.id.btn_save, R.id.btn_delete, R.id.tv_type})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.et_time:
                final DatePicker picker = new DatePicker(this);
                picker.setCanceledOnTouchOutside(true);
                picker.setUseWeight(true);
                picker.setTopPadding(ConvertUtils.toPx(this, 10));
                picker.setTopBackgroundColor(0xFFEEEEEE);
                picker.setTopLineColor(getResources().getColor(R.color.colorPrimary));
                picker.setTitleTextColor(getResources().getColor(R.color.colorPrimary));
                picker.setRangeEnd(2099, 1, 11);
                picker.setRangeStart(2015, 1, 1);
                picker.setSelectedItem(mYear, mMonth, mDay);
                picker.setResetWhileWheel(false);
                picker.setCancelTextColor(getResources().getColor(R.color.colorPrimary));
                picker.setCancelTextSize(16);
                picker.setDividerColor(getResources().getColor(R.color.colorPrimary));
                picker.setSubmitTextSize(16);
                picker.setSubmitTextColor(getResources().getColor(R.color.colorPrimary));
                picker.setTextColor(getResources().getColor(R.color.colorPrimary));
                picker.setTextSize(16);
                picker.setBackgroundColor(0xFFE1E1E1);
                picker.setOnDatePickListener(new DatePicker.OnYearMonthDayPickListener() {
                    @Override
                    public void onDatePicked(String year, String month, String day) {
//                        showMsg(year + "-" + month + "-" + day);
                        etTime.setText(year + "-" + month + "-" + day);
                    }
                });
                picker.setOnWheelListener(new DatePicker.OnWheelListener() {
                    @Override
                    public void onYearWheeled(int index, String year) {
                        picker.setTitleText(year + "-" + picker.getSelectedMonth() + "-" + picker.getSelectedDay());
                    }

                    @Override
                    public void onMonthWheeled(int index, String month) {
                        picker.setTitleText(picker.getSelectedYear() + "-" + month + "-" + picker.getSelectedDay());
                    }

                    @Override
                    public void onDayWheeled(int index, String day) {
                        picker.setTitleText(picker.getSelectedYear() + "-" + picker.getSelectedMonth() + "-" + day);
                    }
                });
                picker.show();
                break;
            case R.id.btn_save:
                String time = etTime.getText().toString().trim();
                String money = etMoney.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                String consumeType = etType.getText().toString().trim();
                if (saveBefore(time, money, consumeType)) {
                    String[] time2 = time.split("-");
                    recordBean._year = Integer.valueOf(time2[0]);
                    recordBean._month = Integer.valueOf(time2[1]);
                    recordBean._day = Integer.valueOf(time2[2]);
                    recordBean._date = time;
                    recordBean._consume = Float.parseFloat(money);
                    recordBean._description = description;
                    recordBean._type = "";
                    recordBean._consumeType = consumeType;
                    RecordDao recordDao = new RecordDao(AddRecordActivity.this);
                    if (isFromEdit) {//是否来自编辑请求
                        if (recordDao.updateRecord(recordBean)) {
                            showMsg("修改成功！");
                            finish();
                        } else {
                            showMsg("修改失败！");
                        }
                    } else {
                        if (recordDao.addRecord(recordBean)) {
                            showMsg("保存成功！");
                        } else {
                            showMsg("保存失败！");
                        }
                    }
                }
                break;
            case R.id.btn_delete:
                NiceDialog.init()
                        .setLayoutId(R.layout.confirm_layout)
                        .setDimAmount(0.3f)
                        .setMargin(60)
                        .setOutCancel(false)
                        .setConvertListener(new ViewConvertListener() {
                            @Override
                            public void convertView(ViewHolder holder, final NiceDialog dialog) {
                                holder.setText(R.id.title, "删除");
                                holder.setText(R.id.message, "确定要删除吗？");
                                holder.setOnClickListener(R.id.cancel, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });

                                holder.setOnClickListener(R.id.ok, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Looper.prepare();
                                                RecordDao recordDao = new RecordDao(AddRecordActivity.this);
                                                if (recordDao.delete(recordBean._id)) {
                                                    showMsg("删除成功！");
                                                    finish();
                                                } else {
                                                    showMsg("删除失败！");
                                                }
                                                Looper.loop();
                                            }
                                        }).start();
                                    }
                                });
                            }
                        })
                        .show(getSupportFragmentManager());
                break;
            case R.id.tv_type:
                OptionPicker optionPicker = new OptionPicker(this
                        , getResources().getStringArray(R.array.consume_type));
                optionPicker.setCycleDisable(true);//不禁用循环
                optionPicker.setTopBackgroundColor(0xFFEEEEEE);
                optionPicker.setTopHeight(38);
                optionPicker.setTopLineColor(getResources().getColor(R.color.colorPrimary));
                optionPicker.setTopLineHeight(1);
                optionPicker.setTitleText("请选择类型");
                optionPicker.setTitleTextColor(getResources().getColor(R.color.colorPrimary));
                optionPicker.setTitleTextSize(16);
                optionPicker.setCancelTextColor(getResources().getColor(R.color.colorPrimary));
                optionPicker.setCancelTextSize(16);
                optionPicker.setSubmitTextColor(getResources().getColor(R.color.colorPrimary));
                optionPicker.setSubmitTextSize(16);
                optionPicker.setTextColor(getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.color_text));

                WheelView.DividerConfig config = new WheelView.DividerConfig();
                config.setColor(getResources().getColor(R.color.colorPrimary));//线颜色
                config.setAlpha(140);//线透明度
                config.setRatio((float) (1.0 / 8.0));//线比率
                optionPicker.setDividerConfig(config);
                optionPicker.setBackgroundColor(0xFFE1E1E1);
                optionPicker.setSelectedIndex(0);
                optionPicker.setCanceledOnTouchOutside(true);
                optionPicker.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
                    @Override
                    public void onOptionPicked(int index, String item) {
//                        showMsg("index=" + index + ", item=" + item);
                        etType.setText(item);
                    }
                });
                optionPicker.show();
                break;
        }
    }

    /**
     * 保存记录前的检查
     */
    private boolean saveBefore(String time, String money, String consumeType) {
        String money_regu = "^\\d+\\.\\d{1,2}|\\d+$";
        if (TextUtils.isEmpty(time)) {
            showMsg("时间不能为空！");
            return false;
        }
        if (TextUtils.isEmpty(money)) {
            showMsg("金额不能为空！");
            return false;
        }
        if (!money.matches(money_regu)) {
            showMsg("金额格式不正确！");
            return false;
        }
        if (TextUtils.isEmpty(consumeType)) {
            showMsg("请选择类型！");
            return false;
        }
        return true;
    }

    /**
     * 显示Toast提示
     *
     * @param str
     */
    private void showMsg(String str) {
        Toast.makeText(AddRecordActivity.this, str, Toast.LENGTH_SHORT).show();
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
