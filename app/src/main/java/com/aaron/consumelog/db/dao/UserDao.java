package com.aaron.consumelog.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.aaron.consumelog.bean.UserBean;
import com.aaron.consumelog.db.UserDBOpenHelper;
import com.aaron.consumelog.util.Md5Util;


/**
 * Created by stars on 2015/7/26.
 */
public class UserDao  {
    private Context context;
    private UserDBOpenHelper userDBOpenHelper = null;

    public UserDao(Context context) {
        this.context = context;
        userDBOpenHelper = new UserDBOpenHelper(context,"user.db",null,1);
    }

    public int addUser(UserBean userBean){
        SQLiteDatabase db = userDBOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", Md5Util.md5Arithmetic(userBean.password));
        long i = db.insert("user", null, values);
        db.close();
        if(i!=-1){
            Toast.makeText(context, "注册成功", Toast.LENGTH_SHORT).show();
            return 1;
        }else{
            Toast.makeText(context, "注册失败", Toast.LENGTH_SHORT).show();
            return 0;
        }
    }

    public boolean checkUser(String password){
        SQLiteDatabase db = userDBOpenHelper.getReadableDatabase();
        Cursor cursor = db.query("user", null, "password=?", new String[]{password}, null, null, null);
        if(cursor.moveToNext()){
            cursor.close();
            db.close();
            return true;
        }else{
            cursor.close();
            db.close();
            return false;
        }
    }

    public boolean anyoneExit(){
        SQLiteDatabase db = userDBOpenHelper.getReadableDatabase();
        Cursor cursor = db.query("user", null, null, null, null, null, null);
        if(cursor.moveToNext()){
            cursor.close();
            db.close();
            return true;
        }else{
            cursor.close();
            db.close();
            return false;
        }
    }

    public String getPassword(){
        SQLiteDatabase db = userDBOpenHelper.getReadableDatabase();
        Cursor cursor = db.query("user", new String[]{"password"}, null, null, null, null, null);
        if(cursor.moveToNext()){
            String password = cursor.getString(0);
            cursor.close();
            db.close();
            return password;
        }else{
            cursor.close();
            db.close();
            return null;
        }
    }

    /**
     * 删除所有记录
     */
    public boolean delete(){
        SQLiteDatabase db = userDBOpenHelper.getWritableDatabase();
        if(db.delete("user", "1", null)!=-1){
            db.close();
            return true;
        }else{
            db.close();
            return false;
        }

    }
}
