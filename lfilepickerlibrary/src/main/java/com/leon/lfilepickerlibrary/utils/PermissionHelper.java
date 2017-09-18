package com.leon.lfilepickerlibrary.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Aaron on 2017/9/11.
 * <p>
 * permissionHelper2 =new PermissionHelper(this);
 * permissionHelper2
 * .requestCodes(WRITE_EXTERNAL_STORAGE_CODE)
 * .requestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
 * .requestRationaleDialog(true)
 * .requestListener(new PermissionHelper.OnPermissionListener() {
 *
 * @Override public void onPermissionGranted() {
 * initView();
 * initListener();
 * }
 * @Override public void onPermissionDenied(String[] deniedPermissions) {
 * Toast.makeText(LFilePickerActivity.this,"功能受限，请开启读取存储权限！"
 * ,Toast.LENGTH_SHORT).show();
 * }
 * })
 * .require();
 *
 * <p>
 * 处理申请的结果：
 * 直接把参数交给mHelper就行了
 * @Override public void onRequestPermissionsResult(int requestCode
 * , @NonNull String[] permissions, @NonNull int[] grantResults) {
 * if(permissionHelper2 != null){
 * permissionHelper2.onRequestPermissionsResult(requestCode,permissions, grantResults);
 * }
 * super.onRequestPermissionsResult(requestCode, permissions, grantResults);
 * }
 */
public class PermissionHelper {
    private OnPermissionListener mOnPermissionListener;
    private int REQUEST_PERMISSION_CODE = 0x111;
    private String[] mPermissionArray;//权限列表
    private Object mContext;
    private boolean isShow = true;//是否继续显示权限申请提示框（针对权限被拒绝的情况）

    /**
     * 权限申请监听
     */
    public interface OnPermissionListener {
        void onPermissionGranted();
        void onPermissionDenied(String[] deniedPermissions);
    }

    /**
     * 私有化构造方法
     *
     * @param object
     */
    public PermissionHelper(@NonNull Object object) {
        checkCallingObjectSuitability(object);
        this.mContext = object;
    }

    public static PermissionHelper getDefault(@NonNull Object object){
        return new PermissionHelper(object);
    }

    /**
     * 设置请求的权限列表
     *
     * @param mPermissionArray
     * @return
     */
    public PermissionHelper requestPermissions(String... mPermissionArray) {
        this.mPermissionArray = mPermissionArray;
        return this;
    }

    /**
     * 设置请求码
     *
     * @param code
     * @return
     */
    public PermissionHelper requestCodes(int code) {
        this.REQUEST_PERMISSION_CODE = code;
        return this;
    }

    /**
     * 设置请求监听器
     *
     * @param listener
     * @return
     */
    public PermissionHelper requestListener(OnPermissionListener listener) {
        this.mOnPermissionListener = listener;
        return this;
    }

    /**
     * 是否继续显示权限申请提示框（针对权限被拒绝的情况）
     *
     * @param isShow
     * @return
     */
    public PermissionHelper requestRationaleDialog(boolean isShow) {
        this.isShow = isShow;
        return this;
    }

    /**
     * 发出请求
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void require() {
        if (mPermissionArray == null || mPermissionArray.length == 0) {
            throw new IllegalArgumentException("先使用requestPermissions方法设置申请权限");
        }
        final String[] deniedPermissions = getDeniedPermissions(getActivity(mContext), mPermissionArray);
        if (deniedPermissions.length > 0) {
            boolean rationale = shouldShowRequestPermissionRationale(getActivity(mContext), deniedPermissions);
            if (rationale && isShow) {
                showMessageOKCancel("权限", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        executePermissionsRequest(mContext, deniedPermissions,
                                REQUEST_PERMISSION_CODE);

                    }
                });
            } else {
                executePermissionsRequest(mContext, deniedPermissions,
                        REQUEST_PERMISSION_CODE);
            }
        } else {
            if (mOnPermissionListener != null)
                mOnPermissionListener.onPermissionGranted();
        }
    }

    /**
     * 执行申请,兼容fragment
     *
     * @param object
     * @param perms
     * @param requestCode
     */
    @TargetApi(23)
    private void executePermissionsRequest(@NonNull Object object, @NonNull String[] perms, int requestCode) {
        if (object instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) object, perms, requestCode);
        } else if (object instanceof Fragment) {
            ((Fragment) object).requestPermissions(perms, requestCode);
        } else if (object instanceof android.app.Fragment) {
            ((android.app.Fragment) object).requestPermissions(perms, requestCode);
        }
    }

    /**
     * 获取跳转到软件设置界面的Intent
     *
     * @return
     */
    public Intent getSettingIntent() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getActivity(mContext).getPackageName(), null);
        intent.setData(uri);
        return intent;
    }

    /**
     * 不再提示之后授权弹框
     *
     * @param message
     * @param okListener
     */
    public void showMessageOKCancel(CharSequence message, DialogInterface.OnClickListener okListener) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(mContext))
                .setMessage(message)
                .setPositiveButton("确定", okListener)
                .setNegativeButton("取消", null)
                .create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }


    /**
     * 拒绝授权显示提示框
     */
    public void showRationaleDialog(String[] denyPermissionList) {
        StringBuilder builder = new StringBuilder();
        if (denyPermissionList != null) {
            for (String str : denyPermissionList) {
                builder.append(str + "\n");
            }
        }
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(mContext))
                .setTitle("权限限制")
                .setMessage("功能受限了，我们需要开启权限\n" + builder.toString())
                .setPositiveButton("去开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity(mContext).startActivity(getSettingIntent());
                        getActivity(mContext).finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        getActivity(mContext).finish();
                    }
                }).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    /**
     * 请求权限结果，对应Activity中onRequestPermissionsResult()方法。
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (mOnPermissionListener != null) {
                String[] deniedPermissions = getDeniedPermissions(getActivity(mContext), permissions);
                if (deniedPermissions.length > 0) {
                    //这里判断grantResults.length==0是为了有些定制的系统当用户点击不在提示后
                    // ，shouldShowRequestPermissionRationale总是返回false，grantResults为无内容的情况
                    mOnPermissionListener.onPermissionDenied(deniedPermissions);
                    if (isShow) {
                        showRationaleDialog(deniedPermissions);
                    }
                } else {
                    mOnPermissionListener.onPermissionGranted();
                }
            }
        }
    }

    /**
     * 获取请求权限中需要授权的权限
     */
    private static String[] getDeniedPermissions(final Context context, final String[] permissions) {
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions.toArray(new String[deniedPermissions.size()]);
    }

    /**
     * 是否有权限需要说明提示
     */
    @TargetApi(Build.VERSION_CODES.M)
    private static boolean shouldShowRequestPermissionRationale(Object object
            , String... deniedPermissions) {
        boolean shouldShowRationale;
        if (object instanceof Activity) {
            for (String permission : deniedPermissions) {
                shouldShowRationale = ActivityCompat
                        .shouldShowRequestPermissionRationale((Activity) object, permission);
                if (shouldShowRationale) return true;
            }
            return false;
        } else if (object instanceof Fragment) {
            for (String permission : deniedPermissions) {
                shouldShowRationale = ((Fragment) object)
                        .shouldShowRequestPermissionRationale(permission);
                if (shouldShowRationale) return true;
            }
            return false;
        } else if (object instanceof android.app.Fragment) {
            for (String permission : deniedPermissions) {
                shouldShowRationale = ((android.app.Fragment) object)
                        .shouldShowRequestPermissionRationale(permission);
                if (shouldShowRationale) return true;
            }
            return false;
        } else {
            throw new IllegalArgumentException("Caller must be an Activity or a Fragment.");
        }
    }

    /**
     * 检查传递Context是否合法
     *
     * @param object
     */
    private void checkCallingObjectSuitability(@Nullable Object object) {
        if (object == null) {
            throw new NullPointerException("Activity or Fragment should not be null");
        }

        boolean isActivity = object instanceof Activity;
        boolean isSupportFragment = object instanceof Fragment;
        boolean isAppFragment = object instanceof android.app.Fragment;
        if (!(isSupportFragment || isActivity || (isAppFragment && isNeedRequest()))) {
            if (isAppFragment) {
                throw new IllegalArgumentException(
                        "Target SDK needs to be greater than 23 if caller is android.app.Fragment");
            } else {
                throw new IllegalArgumentException("Caller must be an Activity or a Fragment.");
            }
        }
    }

    /**
     * 判断是否需要动态申请权限
     *
     * @return
     */
    public static boolean isNeedRequest() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }


    /**
     * 获取Activity
     *
     * @param object
     * @return
     */
    private static Activity getActivity(@NonNull Object object) {
        if (object instanceof Activity) {
            return ((Activity) object);
        } else if (object instanceof Fragment) {
            return ((Fragment) object).getActivity();
        } else if (object instanceof android.app.Fragment) {
            return ((android.app.Fragment) object).getActivity();
        } else {
            return null;
        }
    }
}
