package com.wotingfm.activity.im.interphone.notify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.im.interphone.linkman.model.DBNotifyHistorary;

import java.util.List;

/**
 * 消息列表
 * Created by Administrator on 2016/8/26 0026.
 */
public class NotifyListAdapter extends BaseAdapter {
    private Context context;
    private List<DBNotifyHistorary> list;

    public NotifyListAdapter(Context context, List<DBNotifyHistorary> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
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
        ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_notify_list, parent, false);
            viewHolder.textTitle = (TextView) convertView.findViewById(R.id.text_notify_title);
            viewHolder.textContent = (TextView) convertView.findViewById(R.id.text_notify_content);
            viewHolder.textTime = (TextView) convertView.findViewById(R.id.text_notify_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        DBNotifyHistorary notifyNewData = list.get(position);
        viewHolder.textTitle.setText(notifyNewData.getTitle());
        viewHolder.textContent.setText(notifyNewData.getContent());
        viewHolder.textTime.setText(notifyNewData.getAddTime());
        return convertView;
    }

    class ViewHolder{
        TextView textTitle;     // 消息标题
        TextView textContent;   // 消息内容 只能显示开头部分内容
        TextView textTime;      // 接收消息的时间 时间格式 MM月dd日 HH:mm  如果时间就是今天则不显示日期
    }
}
