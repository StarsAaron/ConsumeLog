package com.aaron.consumelog.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aaron.consumelog.R;
import com.aaron.consumelog.bean.RecordBean;

import java.util.List;

/**
 * Created by Aaron on 2017/9/5.
 */

public class RecentRecordAdapter extends RecyclerView.Adapter<RecentRecordAdapter.MyViewHolder> {
    private Context context;
    private List<RecordBean> recordBeenList;

    public RecentRecordAdapter(Context context,List<RecordBean> recordBeenList) {
        this.context = context;
        this.recordBeenList = recordBeenList;
    }

    @Override
    public RecentRecordAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recentrecord,parent,false));
    }

    @Override
    public void onBindViewHolder(RecentRecordAdapter.MyViewHolder holder, int position) {
        RecordBean recordBean = recordBeenList.get(position);
        holder.recent_date.setText(recordBean._date);
        holder.recent_money.setText("￥:"+recordBean._consume);
        holder.recent_type.setText("分类:"+recordBean._consumeType);
    }

    @Override
    public int getItemCount() {
        return recordBeenList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView recent_date;
        TextView recent_money;
        TextView recent_type;

        public MyViewHolder(View view) {
            super(view);
            recent_date = (TextView) view.findViewById(R.id.tv_recent_date);
            recent_money = (TextView) view.findViewById(R.id.tv_recent_money);
            recent_type = (TextView) view.findViewById(R.id.tv_recent_type);
        }
    }
}
