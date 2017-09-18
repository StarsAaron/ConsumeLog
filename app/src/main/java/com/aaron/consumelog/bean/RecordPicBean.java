package com.aaron.consumelog.bean;

import java.io.Serializable;

/**
 * Created by Aaron on 2017/9/17.
 */

public class RecordPicBean  implements Serializable {
    public int _picId;//图片记录ID
    public int _recordId;//记录ID（外键）
    public String _picPath;//图片存放路径

}
