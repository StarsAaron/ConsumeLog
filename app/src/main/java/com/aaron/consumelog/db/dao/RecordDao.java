package com.aaron.consumelog.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.aaron.consumelog.bean.BarBean;
import com.aaron.consumelog.bean.DateBean;
import com.aaron.consumelog.bean.RecordBean;
import com.aaron.consumelog.bean.SumBean;
import com.aaron.consumelog.db.RecordDBOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.R.id.list;

public class RecordDao {
    public static final String db_name = "recordDB.db";
    private Context context;
    private RecordDBOpenHelper recordDBOpenHelper = null;

    public RecordDao(Context context) {
        this.context = context;
        recordDBOpenHelper = new RecordDBOpenHelper(context,db_name,null,1);
    }

    /**
     * 添加记录
     * @param recordrBean
     * @return
     */
    public boolean addRecord(RecordBean recordrBean){
        SQLiteDatabase db = recordDBOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("r_year", recordrBean._year);
        values.put("r_month", recordrBean._month);
        values.put("r_day", recordrBean._day);
        values.put("r_date",recordrBean._date);
        values.put("r_type", recordrBean._type);
        values.put("r_consume", recordrBean._consume);
        values.put("r_description", recordrBean._description);
        values.put("r_consumeType", recordrBean._consumeType);
        long i = db.insert("Record", null, values);
        db.close();
        if(i!=-1){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 查找最新num条记录
     * @param num
     * @return
     */
    public List<RecordBean> selectTopRecord(int num){
        List<RecordBean> list = new ArrayList<>();
        SQLiteDatabase db = recordDBOpenHelper.getReadableDatabase();
        String str = "Select * from Record order by r_record_date desc limit ?";
        Cursor cursor = db.rawQuery(str,new String[]{String.valueOf(num)});
        while(cursor.moveToNext()){
            RecordBean recordBean = new RecordBean();
            recordBean._id = cursor.getInt(0);
            recordBean._year = cursor.getInt(1);
            recordBean._month = cursor.getInt(2);
            recordBean._day = cursor.getInt(3);
            recordBean._date = cursor.getString(4);
            recordBean._type = cursor.getString(5);
            recordBean._consume = cursor.getFloat(6);
            recordBean._description = cursor.getString(7);
            recordBean._consumeType = cursor.getString(8);
            list.add(recordBean);
        }
        cursor.close();
        db.close();
        return list;
    }

    /**
     * 按年份和月份查找记录
     * @return
     */
    public List<RecordBean> selectRecordByYearAndMonth(int year,int month){
        List<RecordBean> list = new ArrayList<>();
        SQLiteDatabase db = recordDBOpenHelper.getReadableDatabase();
        String str = "Select * from Record where r_year=? And r_month=? order by r_day asc";
        Cursor cursor = db.rawQuery(str,new String[]{String.valueOf(year),String.valueOf(month)});
//        Cursor cursor = db.query("Record", null, "WHERE r_year=?", new String[]{String.valueOf(year)}, null, null, null);
        while(cursor.moveToNext()){
            RecordBean recordBean = new RecordBean();
            recordBean._id = cursor.getInt(0);
            recordBean._year = cursor.getInt(1);
            recordBean._month = cursor.getInt(2);
            recordBean._day = cursor.getInt(3);
            recordBean._date = cursor.getString(4);
            recordBean._type = cursor.getString(5);
            recordBean._consume = cursor.getFloat(6);
            recordBean._description = cursor.getString(7);
            recordBean._consumeType = cursor.getString(8);
            list.add(recordBean);
        }
        cursor.close();
        db.close();
        return list;
    }

    /**
     * 查找时间范围记录
     * @return
     */
    public List<BarBean> selectRecordBetween(String startTime, String endTime,List<BarBean> barBeanList){
        SQLiteDatabase db = recordDBOpenHelper.getReadableDatabase();
        String str = "Select r_date,sum(r_consume) from Record where r_date BETWEEN ? And ? group by r_date order by r_date asc";
        Cursor cursor = db.rawQuery(str,new String[]{startTime,endTime});
        while(cursor.moveToNext()){
            for(BarBean barBean1:barBeanList){
                if(barBean1.mDate.equals(cursor.getString(0))){//判断日期是否同一天，相同就赋值金额
                    barBean1.mTotalMoney = cursor.getFloat(1);
                }
            }
        }
        cursor.close();
        db.close();
        return barBeanList;
    }

    /**
     * 根据类型统计总金额
     * @param startTime
     * @param endTime
     * @return
     */
    public List<SumBean> selectRecordBetweenDateGroupByConsumeType(String startTime, String endTime){
        String str = "select sum(r_consume),r_consumeType from Record where r_date BETWEEN ? And ? group by r_consumeType";
        SQLiteDatabase db = recordDBOpenHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(str,new String[]{startTime,endTime});
        List<SumBean> sumBeanList = new ArrayList<>();
        while(cursor.moveToNext()){
            SumBean sumBean = new SumBean();
            sumBean.total = cursor.getFloat(0);
            sumBean.consumeType = cursor.getString(1);
            sumBeanList.add(sumBean);
        }
        cursor.close();
        db.close();
        return sumBeanList;
    }

    /**
     * 更新记录
     * @return
     */
    public boolean updateRecord(RecordBean recordrBean){
        SQLiteDatabase db = recordDBOpenHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("r_year", recordrBean._year);
        values.put("r_month", recordrBean._month);
        values.put("r_day", recordrBean._day);
        values.put("r_date", recordrBean._date);
        values.put("r_type", recordrBean._type);
        values.put("r_consume", recordrBean._consume);
        values.put("r_description", recordrBean._description);
        values.put("r_consumeType", recordrBean._consumeType);
        int i = db.update("Record", values, "_id=?", new String[]{String.valueOf(recordrBean._id)});
        if(i != 0){
            db.close();
            Intent intent = new Intent();
            intent.setAction("com.rujian.consumemanager.mydataChangeReceiver.datechange");
            context.sendBroadcast(intent);
            return true;
        } else {
            db.close();
            return false;
        }
    }

    /**
     * 删除某条记录
     */
    public boolean delete(int id){
        SQLiteDatabase db = recordDBOpenHelper.getWritableDatabase();
        if(db.delete("Record", "_id=?", new String[]{String.valueOf(id)})!=-1){
            db.close();
            Intent intent = new Intent();
            intent.setAction("com.rujian.consumemanager.mydataChangeReceiver.datechange");
            context.sendBroadcast(intent);
            return true;
        }else{
            db.close();
            return false;
        }

    }

    /**
     * 删除所有记录
     */
    public boolean deleteAll(){
        SQLiteDatabase db = recordDBOpenHelper.getWritableDatabase();
        if(db.delete("Record", "1", null)!=-1){
            db.close();
            Intent intent = new Intent();
            intent.setAction("com.rujian.consumemanager.mydataChangeReceiver.datechange");
            context.sendBroadcast(intent);
            return true;
        }else{
            db.close();
            return false;
        }

    }

}
