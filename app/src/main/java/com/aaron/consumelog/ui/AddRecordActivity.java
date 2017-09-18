package com.aaron.consumelog.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.aaron.consumelog.R;
import com.aaron.consumelog.adapter.ImgGridViewAdapter;
import com.aaron.consumelog.bean.RecordBean;
import com.aaron.consumelog.db.dao.RecordDao;
import com.aaron.consumelog.util.GetViewSizeUtils;
import com.aaron.consumelog.util.ImageUtils;
import com.customdialoglibrary.CustomDialog;
import com.customdialoglibrary.ViewHolder;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

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
    @InjectView(R.id.gv_image)
    GridView mGridView;

    private static final int PHOTO_CAPTURE = 0x11;// 拍照
    private static final int PHOTO_RESULT = 0x12;// 结果
    private static final int PHOTO_REQUEST_GALLERY = 0x13;// 相册
    private static final int SHOWRESULT = 0x101;
    private static final int SHOWTREATEDIMG = 0x102;

    private String chooseConsume = "";
    private boolean isFromEdit = false;//是否来自编辑页面
    private RecordBean recordBean = null;//来自编辑请求的RecordBean

    private int mYear, mMonth, mDay;
    private static final String imgFileName = "ConsumeLogImg";//存放图片的文件夹
    private static String imgFilePath = getSDPath() + File.separator+imgFileName;//存放图片的文件夹路径
    private String imgFilePathNow;//当前日期的图片文件夹
    private String cropImgName;//需要裁剪的Img路径
    private String afterCropImgName;//裁剪后的Img路径
    
    public enum TYPE {
        T_EDIT, T_ADD
    }

    private List<Bitmap> mDatas;
    private ImgGridViewAdapter adapter;
    private File[] fileList;

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
//        recordBean._random_tip = UUID.randomUUID().getLeastSignificantBits();

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
        mDatas=new ArrayList<>();
        imgFilePathNow = imgFilePath+File.separator+etTime.getText().toString();
        
//        loadImg();
    }

    /**
     * 获取sd卡的路径
     *
     * @return 路径的字符串
     */
    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取外存目录
        }
        return sdDir.toString();
    }

    /**
     * dp转px
     *
     * @param dpValue dp值
     * @return px值
     */
    public int dp2px(final float dpValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 加载图片
     */
    private void loadImg(){
        File[] fileList = null;
        File path = new File(imgFilePathNow);
        if (!path.exists()) {// 若文件夹不存在 首先创建文件夹
            path.mkdirs();
        }else{
            fileList = path.listFiles();
        }
        if(fileList != null && fileList.length!=0){
            for (File file:fileList) {
                mDatas.add(ImageUtils.getBitmap(file,dp2px(48),dp2px(48)));
            }
        }
        adapter=new ImgGridViewAdapter(AddRecordActivity.this,mDatas);
        mGridView.setAdapter(adapter);
        GetViewSizeUtils.setGridViewHeightBasedOnChildren(mGridView);
        adapter.notifyDataSetChanged();
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(position==parent.getChildCount()-1){
                    Toast.makeText(AddRecordActivity.this, "您点击了添加", Toast.LENGTH_SHORT).show();
                    CustomDialog.init(CustomDialog.DialogType.CUSTOM_VIEW_TYPE)
                            .setLayoutId(R.layout.layout_addpic_menu)
                            .setConvertListener(new CustomDialog.ViewConvertListener() {
                                @Override
                                public void convertView(ViewHolder holder, CustomDialog customDialog) {
                                    holder.setOnClickListener(R.id.ll_pic_lib, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(Intent.ACTION_PICK, null);
                                            intent.setType("image/*");
//                                            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                            startActivityForResult(intent,PHOTO_REQUEST_GALLERY);
                                        }
                                    });
                                    holder.setOnClickListener(R.id.ll_capture, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            String fileName = createFileName();
                                            File file = new File(imgFilePath+File.separator+"temp",fileName);
                                            cropImgName = file.getAbsolutePath();
                                            afterCropImgName = imgFilePathNow+File.separator+fileName;
                                            Intent mIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                            mIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                                            startActivityForResult(mIntent,PHOTO_CAPTURE);
                                        }
                                    });
                                }
                            })
                            .setShowBottom(true)
                            .setAnimStyle(R.style.MyAnimation)
                            .show(getSupportFragmentManager());
                }
            }
        });
    }

    /**
     * 生成唯一图片文件名
     * @return
     */
    private String createFileName(){
        StringBuilder builder = new StringBuilder();
//        builder.append(etTime.getText().toString());
//        builder.append(recordBean._random_tip);
        builder.append("-"+UUID.randomUUID().toString());
        builder.append(".jpg");
        return builder.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED)
            return;

        if (requestCode == PHOTO_CAPTURE) {
            startPhotoCrop(Uri.fromFile(new File(cropImgName)),Uri.fromFile(new File(afterCropImgName)));
        }

        if (requestCode == PHOTO_REQUEST_GALLERY) {
            startPhotoCrop(data.getData(),Uri.fromFile(new File(afterCropImgName)));
        }
        loadImg();
    }

    /**
     * 调用系统图片编辑进行裁剪
     */
    public void startPhotoCrop(Uri in,Uri out) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(in, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,out);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, PHOTO_RESULT);
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
                CustomDialog.init()
                        .setTitleText("删除")
                        .setContentText("确定要删除吗?")
                        .setDimAmount(0.3f)
                        .setOutCancel(true)
                        .setCancelText("取消")
                        .setCancelClickListener(new CustomDialog.OnDefaultDialogButtonClickListener() {
                            @Override
                            public void onClick(CustomDialog customDialog) {
                                customDialog.dismiss();
                            }
                        })
                        .setConfirmText("删除")
                        .setConfirmClickListener(new CustomDialog.OnDefaultDialogButtonClickListener() {
                            @Override
                            public void onClick(CustomDialog customDialog) {
                                customDialog.dismiss();
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
