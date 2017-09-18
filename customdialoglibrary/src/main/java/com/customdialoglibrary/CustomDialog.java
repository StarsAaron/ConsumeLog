package com.customdialoglibrary;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import com.customdialoglibrary.view.ProgressWheel;
import com.customdialoglibrary.view.SuccessTickView;

import java.io.Serializable;
import java.util.List;


/**
 * 自定义Dialog 带Fragment生命周期，旋转屏幕内容恢复
 * Created by Aaron on 2017/9/13.
 */
public class CustomDialog extends DialogFragment {
    private static final String MARGIN = "margin";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String DIM = "dim_amount";
    private static final String BOTTOM = "show_bottom";
    private static final String CANCEL = "out_cancel";
    private static final String ANIM = "anim_style";
    private static final String LAYOUT = "layout_id";
    private static final String COVER_LISTENER = "cover_listener";
    private static final String DIALOG_TYPE = "dialog_type";
    private static final String TITLE_TEXT = "mTitleText";
    private static final String CONTENT_TEXT = "mContentText";
    private static final String CANCEL_TEXT = "mCancelText";
    private static final String CONFIRM_TEXT = "mConfirmText";
    private static final String CANCEL_LISTENER = "mCancelClickListener";
    private static final String CONFIRM_LISTENER = "mConfirmClickListener";
    private static final String CUSTOM_IMAGE = "customimage";

    private AnimationSet mErrorXInAnim; // 错误图案动画
    private AnimationSet mSuccessLayoutAnimSet; // 成功图案动画
    private Animation mSuccessBowAnim; // 成功图案动画

    private TextView mTitleTextView;
    private TextView mContentTextView;
    private FrameLayout mErrorFrame;
    private FrameLayout mSuccessFrame;
    private FrameLayout mProgressFrame;
    private SuccessTickView mSuccessTick;
    private ImageView mErrorX;
    private ImageView iv_warning;
    private View mSuccessLeftMask;
    private View mSuccessRightMask;
    private ImageView mCustomImage;
    private Button mConfirmButton;
    private Button mCancelButton;
    private FrameLayout mWarningFrame;
    private ProgressWheel mProgressWheel;

    private String mTitleText;// 标题
    private String mContentText;// 内容
    private String mCancelText;// 取消按钮文字
    private String mConfirmText; //确定按钮文字

    private boolean mShowCancel; //是否显示取消按钮
    private boolean mShowConfirm;//是否显示确定按钮
    private boolean mShowContent;// 是否显示内容
    private int mCustomImgDrawableRec;// 自定义图片资源ID
    private int margin;//左右边距
    private int width;//宽度
    private int height;//高度
    private float dimAmount = 0.5f;//灰度深浅
    private boolean showBottom;//是否底部显示
    private boolean outCancel = true;//是否点击外部取消
    @StyleRes
    private int animStyle; //动画
    @LayoutRes
    protected int layoutId = -1;//布局

    private ViewConvertListener convertListener;//视图初始化监听
    private DialogType dialogType = DialogType.NORMAL_TYPE;//Dialog显示类型
    private OnDefaultDialogButtonClickListener mCancelClickListener;//取消按钮点击回调
    private OnDefaultDialogButtonClickListener mConfirmClickListener;//确定按钮点击回调

    /**
     * 显示的对话框类型
     */
    public enum DialogType {
        NORMAL_TYPE // 默认
        , ERROR_TYPE // 错误提示框
        , SUCCESS_TYPE // 成功提示框
        , WARNING_TYPE // 警告提示框
        , PROGRESS_TYPE // 进度提示框
        , CUSTOM_IMAGE_TYPE //自定义图片提示框
        , CUSTOM_VIEW_TYPE // 自定义视图布局提示框
    }

    public CustomDialog() {
        try {
            throw new IllegalAccessException("使用静态init()方法新建实例");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置对话框样式
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.alert_dialog);
        //恢复保存的数据
        if (savedInstanceState != null) {
            margin = savedInstanceState.getInt(MARGIN);
            width = savedInstanceState.getInt(WIDTH);
            height = savedInstanceState.getInt(HEIGHT);
            dimAmount = savedInstanceState.getFloat(DIM);
            showBottom = savedInstanceState.getBoolean(BOTTOM);
            outCancel = savedInstanceState.getBoolean(CANCEL);
            animStyle = savedInstanceState.getInt(ANIM);
            layoutId = savedInstanceState.getInt(LAYOUT);
            mTitleText = savedInstanceState.getString(TITLE_TEXT);
            mContentText = savedInstanceState.getString(CONTENT_TEXT);
            mCancelText = savedInstanceState.getString(CANCEL_TEXT);
            mConfirmText = savedInstanceState.getString(CONFIRM_TEXT);
            mCustomImgDrawableRec = savedInstanceState.getInt(CUSTOM_IMAGE);
            convertListener = (ViewConvertListener) savedInstanceState.getSerializable(COVER_LISTENER);
            mCancelClickListener = (OnDefaultDialogButtonClickListener) savedInstanceState.getSerializable(CANCEL_LISTENER);
            mConfirmClickListener = (OnDefaultDialogButtonClickListener) savedInstanceState.getSerializable(CONFIRM_LISTENER);
            dialogType = (DialogType) savedInstanceState.getSerializable(DIALOG_TYPE);
        }

        // 初始化动画
        mErrorXInAnim = (AnimationSet) AnimationUtils.loadAnimation(getContext(), R.anim.error_anim);
        // 2.3.x system don't support alpha-animation on layer-list drawable
        // remove it from animation set
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            List<Animation> childAnims = mErrorXInAnim.getAnimations();
            int idx = 0;
            for (; idx < childAnims.size(); idx++) {
                if (childAnims.get(idx) instanceof AlphaAnimation) {
                    break;
                }
            }
            if (idx < childAnims.size()) {
                childAnims.remove(idx);
            }
        }
        mSuccessBowAnim = AnimationUtils.loadAnimation(getContext(), R.anim.success_bow_roate_anim);
        mSuccessLayoutAnimSet = (AnimationSet) AnimationUtils.loadAnimation(getContext(), R.anim.success_mask_layout_anim);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 初始化对话框背景显示
        Window window = getDialog().getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            //调节灰色背景透明度[0-1]，默认0.5f
            lp.dimAmount = dimAmount;
            //是否在底部显示
            if (showBottom) {
                lp.gravity = Gravity.BOTTOM;
                if (animStyle == 0) {
                    animStyle = R.style.DefaultAnimation;
                }
            }

            //设置dialog宽度
            if (width == 0) {//默认跟屏幕等宽
                lp.width = getScreenWidth(getContext()) - 2 * dp2px(margin);
            } else if (width == WindowManager.LayoutParams.WRAP_CONTENT) { // 包裹内容
                lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
            } else { // 宽度
                int ww = getScreenWidth(getContext());
                if (width > (ww - 2 * margin)) { //如果设置的宽度大于屏幕宽度减去左右边距，显示最大宽
                    lp.width = ww - 2 * dp2px(margin);
                } else {
                    lp.width = dp2px(width);
                }
            }
            //设置dialog高度
            if (height <= 0) {//默认包裹内容
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            } else {// 如果设置了高度值，显示高度减去边距
                lp.height = dp2px(height);
            }

            //设置dialog进入、退出的动画
            window.setWindowAnimations(animStyle);
            window.setAttributes(lp);
        }
        setCancelable(outCancel);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        if (dialogType == DialogType.CUSTOM_VIEW_TYPE && layoutId != -1) {
            dialogType = DialogType.CUSTOM_VIEW_TYPE;//防止只设置layoutId不设置CUSTOM_VIEW_TYPE类型
            // 自定义布局，设置 setConvertListener 绑定视图控件
            view = inflater.inflate(layoutId, container, false);
        } else {
            // 默认布局
            view = inflater.inflate(R.layout.dialog_custom, container, false);
            mTitleTextView = view.findViewById(R.id.title_text);
            mContentTextView = view.findViewById(R.id.content_text);
            mErrorFrame = view.findViewById(R.id.error_frame);
            mErrorX = mErrorFrame.findViewById(R.id.error_x);
            mSuccessFrame = view.findViewById(R.id.success_frame);
            mProgressFrame = view.findViewById(R.id.progress_dialog);
            mSuccessTick = mSuccessFrame.findViewById(R.id.success_tick);
            mSuccessLeftMask = mSuccessFrame.findViewById(R.id.mask_left);
            mSuccessRightMask = mSuccessFrame.findViewById(R.id.mask_right);
            mCustomImage = view.findViewById(R.id.custom_image);
            mWarningFrame = view.findViewById(R.id.warning_frame);
            mConfirmButton = view.findViewById(R.id.confirm_button);
            mCancelButton = view.findViewById(R.id.cancel_button);
            iv_warning = view.findViewById(R.id.iv_warning);
            mProgressWheel = view.findViewById(R.id.progressWheel);
            mConfirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mConfirmClickListener != null) {
                        mConfirmClickListener.onClick(CustomDialog.this);
                    } else {
                        CustomDialog.this.dismiss();
                    }
                }
            });
            mCancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mCancelClickListener != null) {
                        mCancelClickListener.onClick(CustomDialog.this);
                    } else {
                        CustomDialog.this.dismiss();
                    }
                }
            });
            setTitleText(mTitleText);
            setContentText(mContentText);
            setCancelText(mCancelText);
            setConfirmText(mConfirmText);
            changeAlertType(dialogType, true);
        }
        if (convertListener != null) {
            convertListener.convertView(ViewHolder.create(view), CustomDialog.this);
        }
        return view;
    }

    /**
     * 屏幕旋转等导致DialogFragment销毁后重建时保存数据
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MARGIN, margin);
        outState.putInt(WIDTH, width);
        outState.putInt(HEIGHT, height);
        outState.putFloat(DIM, dimAmount);
        outState.putBoolean(BOTTOM, showBottom);
        outState.putBoolean(CANCEL, outCancel);
        outState.putInt(ANIM, animStyle);
        outState.putInt(LAYOUT, layoutId);
        outState.putString(TITLE_TEXT, mTitleText);
        outState.putString(CONTENT_TEXT, mContentText);
        outState.putString(CANCEL_TEXT, mCancelText);
        outState.putString(CONFIRM_TEXT, mConfirmText);

        outState.putSerializable(COVER_LISTENER, convertListener);
        outState.putSerializable(CANCEL_LISTENER, mCancelClickListener);
        outState.putSerializable(CONFIRM_LISTENER, mConfirmClickListener);
        outState.putSerializable(CUSTOM_IMAGE, mCustomImgDrawableRec);
        outState.putSerializable(DIALOG_TYPE, dialogType);
    }

    /**
     * 新建实例
     *
     * @return
     */
    public static CustomDialog init() {
        CustomDialog customDialog = new CustomDialog();
        customDialog.dialogType = DialogType.NORMAL_TYPE;
        return customDialog;
    }

    /**
     * 新建实例
     *
     * @return
     */
    public static CustomDialog init(DialogType dialogType) {
        CustomDialog customDialog = new CustomDialog();
        customDialog.dialogType = dialogType;
        return customDialog;
    }

    /**
     * 重置默认布局对话框视图
     */
    private void restore() {
        mCustomImage.setVisibility(View.GONE);
        mErrorFrame.setVisibility(View.GONE);
        mSuccessFrame.setVisibility(View.GONE);
        mWarningFrame.setVisibility(View.GONE);
        mProgressFrame.setVisibility(View.GONE);
        mConfirmButton.setVisibility(View.VISIBLE);

        mConfirmButton.setBackgroundResource(R.drawable.blue_button_background);
        mErrorFrame.clearAnimation();
        mErrorX.clearAnimation();
        mSuccessTick.clearAnimation();
        mSuccessLeftMask.clearAnimation();
        mSuccessRightMask.clearAnimation();
    }

    /**
     * 切换默认对话框显示类型
     *
     * @param alertType
     */
    public void changeAlertType(DialogType alertType) {
        changeAlertType(alertType, false);
    }

    /**
     * 切换默认对话框显示类型
     *
     * @param alertType
     * @param fromCreate 是否在onCreate中调用该方法
     */
    private void changeAlertType(DialogType alertType, boolean fromCreate) {
        dialogType = alertType;
        // call after created views
        if (!fromCreate) {
            // restore all of views state before switching alert type
            restore();
        }
        switch (dialogType) {
            case ERROR_TYPE:
                mErrorFrame.setVisibility(View.VISIBLE);
                break;
            case SUCCESS_TYPE:
                mSuccessFrame.setVisibility(View.VISIBLE);
                // initial rotate layout of success mask
                mSuccessLeftMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(0));
                mSuccessRightMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(1));
                break;
            case WARNING_TYPE:
                mConfirmButton.setBackgroundResource(R.drawable.red_button_background);
                mWarningFrame.setVisibility(View.VISIBLE);
                break;
            case CUSTOM_IMAGE_TYPE:
                setCustomImage(mCustomImgDrawableRec);
                break;
            case PROGRESS_TYPE:
                mProgressFrame.setVisibility(View.VISIBLE);
                mConfirmButton.setVisibility(View.GONE);
                break;
        }
        if (!fromCreate) {
            playAnimation();
        }
    }

    /**
     * 播放默认布局对话框内容动画
     */
    private void playAnimation() {
        if (dialogType == DialogType.ERROR_TYPE) {
            mErrorX.startAnimation(mErrorXInAnim);
        } else if (dialogType == DialogType.SUCCESS_TYPE) {
            mSuccessTick.startTickAnim();
            mSuccessRightMask.startAnimation(mSuccessBowAnim);
        } else if (dialogType == DialogType.WARNING_TYPE) {
            iv_warning.startAnimation(mErrorXInAnim);
        }
    }

    /**
     * 设置布局文件
     *
     * @param layoutId
     * @return
     */
    public CustomDialog setLayoutId(@LayoutRes int layoutId) {
        this.layoutId = layoutId;
        return this;
    }

    /**
     * 设置视图监听器
     *
     * @param convertListener
     * @return
     */
    public CustomDialog setConvertListener(ViewConvertListener convertListener) {
        this.convertListener = convertListener;
        return this;
    }

    /**
     * 设置距离
     *
     * @param margin
     * @return
     */
    public CustomDialog setMargin(int margin) {
        this.margin = margin;
        return this;
    }

    /**
     * 设置宽度
     *
     * @param width
     * @return
     */
    public CustomDialog setWidth(int width) {
        this.width = width;
        return this;
    }

    /**
     * 设置高度
     *
     * @param height
     * @return
     */
    public CustomDialog setHeight(int height) {
        this.height = height;
        return this;
    }

    /**
     * 设置背景灰度
     *
     * @param dimAmount
     * @return
     */
    public CustomDialog setDimAmount(float dimAmount) {
        this.dimAmount = dimAmount;
        return this;
    }

    /**
     * 设置是否显示在底部
     *
     * @param showBottom
     * @return
     */
    public CustomDialog setShowBottom(boolean showBottom) {
        this.showBottom = showBottom;
        return this;
    }

    /**
     * 设置是否点击外区域消失
     *
     * @param outCancel
     * @return
     */
    public CustomDialog setOutCancel(boolean outCancel) {
        this.outCancel = outCancel;
        return this;
    }

    /**
     * 设置动画
     *
     * @param animStyle
     * @return
     */
    public CustomDialog setAnimStyle(@StyleRes int animStyle) {
        this.animStyle = animStyle;
        return this;
    }

    /**
     * 最后调用，显示对话框
     *
     * @param manager
     * @return
     */
    public CustomDialog show(FragmentManager manager) {
        super.show(manager, String.valueOf(System.currentTimeMillis()));
        return this;
    }

    /**
     * 获取默认布局对话框标题
     *
     * @return
     */
    public String getTitleText() {
        return mTitleText;
    }

    /**
     * 设置默认布局对话框标题
     *
     * @param text
     * @return
     */
    public CustomDialog setTitleText(String text) {
        mTitleText = text;
        if (mTitleTextView != null && mTitleText != null) {
            mTitleTextView.setText(mTitleText);
        }
        return this;
    }

    /**
     * 设置默认布局对话框自定义图片
     *
     * @param resourceId
     * @return
     */
    public CustomDialog setCustomImage(int resourceId) {
        mCustomImgDrawableRec = resourceId;
        if (mCustomImage != null && mCustomImgDrawableRec != -1) {
            mCustomImage.setVisibility(View.VISIBLE);
            mCustomImage.setImageResource(resourceId);
        }
        return this;
    }

    /**
     * 获取默认布局对话框内容
     *
     * @return
     */
    public String getContentText() {
        return mContentText;
    }

    /**
     * 设置默认布局对话框内容
     *
     * @param text
     * @return
     */
    public CustomDialog setContentText(String text) {
        mContentText = text;
        if (mContentTextView != null && mContentText != null) {
            showContentText(true);
            mContentTextView.setText(mContentText);
        }
        return this;
    }

    /**
     * 获取默认布局对话框是否显示取消按钮
     *
     * @return
     */
    public boolean isShowCancelButton() {
        return mShowCancel;
    }

    /**
     * 默认布局对话框是否显示取消按钮
     *
     * @param isShow
     * @return
     */
    public CustomDialog showCancelButton(boolean isShow) {
        mShowCancel = isShow;
        if (mCancelButton != null) {
            mCancelButton.setVisibility(mShowCancel ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    /**
     * 默认布局对话框是否显示确定按钮
     *
     * @param isShow
     * @return
     */
    public CustomDialog showConfirmButton(boolean isShow) {
        mShowConfirm = isShow;
        if (mConfirmButton != null) {
            mConfirmButton.setVisibility(mShowConfirm ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    /**
     * 判断默认布局对话框是否显示对话框内容
     *
     * @return
     */
    public boolean isShowContentText() {
        return mShowContent;
    }

    /**
     * 设置默认布局对话框是否显示对话框内容
     *
     * @param isShow
     * @return
     */
    public CustomDialog showContentText(boolean isShow) {
        mShowContent = isShow;
        if (mContentTextView != null) {
            mContentTextView.setVisibility(mShowContent ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    /**
     * 获取默认布局对话框的取消按钮文字
     *
     * @return
     */
    public String getCancelText() {
        return mCancelText;
    }

    /**
     * 设置默认布局对话框的取消按钮文字
     *
     * @param text
     * @return
     */
    public CustomDialog setCancelText(String text) {
        mCancelText = text;
        if (mCancelButton != null && mCancelText != null) {
            showCancelButton(true);
            mCancelButton.setText(mCancelText);
        }
        return this;
    }

    /**
     * 获取默认布局对话框的确定按钮文字
     *
     * @return
     */
    public String getConfirmText() {
        return mConfirmText;
    }

    /**
     * 设置默认布局对话框的确定按钮文字
     *
     * @param text
     * @return
     */
    public CustomDialog setConfirmText(String text) {
        mConfirmText = text;
        if (mConfirmButton != null && mConfirmText != null) {
            showConfirmButton(true);
            mConfirmButton.setText(mConfirmText);
        }
        return this;
    }

    /**
     * 设置默认布局对话框的取消按钮点击监听
     *
     * @param listener
     * @return
     */
    public CustomDialog setCancelClickListener(OnDefaultDialogButtonClickListener listener) {
        mCancelClickListener = listener;
        return this;
    }

    /**
     * 设置默认布局对话框的确定按钮点击监听
     *
     * @param listener
     * @return
     */
    public CustomDialog setConfirmClickListener(OnDefaultDialogButtonClickListener listener) {
        mConfirmClickListener = listener;
        return this;
    }

    /**
     * 设置进度条颜色
     *
     * @param recId
     * @return
     */
    public CustomDialog setProgressColor(int recId) {
        mProgressWheel.setBarColor(getResources().getColor(recId));
        return this;
    }

    /**
     * 视图内容绑定监听
     */
    public interface ViewConvertListener extends Serializable {
        long serialVersionUID = System.currentTimeMillis();

        void convertView(ViewHolder holder, CustomDialog customDialog);
    }

    /**
     * 对话框点击事件
     */
    public interface OnDefaultDialogButtonClickListener extends Serializable {
        long serialVersionUID = System.currentTimeMillis();

        void onClick(CustomDialog customDialog);
    }

    /**
     * dp2px
     *
     * @param dipValue
     * @return
     */
    public int dp2px(float dipValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }
}
