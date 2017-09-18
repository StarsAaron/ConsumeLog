package com.aaron.consumelog.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.aaron.consumelog.R;

import java.util.List;

/**
 * Created by Aaron on 2017/9/15.
 */
public class ImgGridViewAdapter extends BaseAdapter {
    private Context context;
    private List<Bitmap> list;
    private LayoutInflater layoutInflater;

    public ImgGridViewAdapter(Context context, List<Bitmap> list) {
        this.context = context;
        this.list = list;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size()+1;//注意此处
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = layoutInflater.inflate(R.layout.item_grid_pic, null);
        ImageView imageView = convertView.findViewById(R.id.iv_img);
        if (position < list.size()) {
            imageView.setImageBitmap(list.get(position));
        }else{
            imageView.setBackgroundResource(R.mipmap.ic_add_pic);//最后一个显示加号图片
        }
        return convertView;
    }

}
