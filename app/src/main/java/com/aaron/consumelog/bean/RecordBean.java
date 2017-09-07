package com.aaron.consumelog.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by stars on 2015/7/26.
 */
public class RecordBean implements Serializable {
    public int _id; //ID
    public int _year;//年
    public int _month;//月
    public int _day;//日
    public String _date; // 时间
    public String _type; //类型（支出/收入）
    public float _consume; //金额
    public String _consumeType;//消费类型（购物，电费，水费，车费，汽油费，医疗，送礼，其他。。。）
    public String _description; //描述
    public Date date;//记录时间
}
