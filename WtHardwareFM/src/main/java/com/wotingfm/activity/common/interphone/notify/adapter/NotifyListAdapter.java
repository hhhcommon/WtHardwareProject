package com.wotingfm.activity.common.interphone.notify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.common.interphone.notify.model.NotifyNewData;

import java.util.List;

/**
 * 消息列表
 * Created by Administrator on 2016/8/26 0026.
 */
public class NotifyListAdapter extends BaseAdapter {
    private Context context;
    private List<NotifyNewData> list;

    public NotifyListAdapter(Context context, List<NotifyNewData> list){
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_notify_list, parent, false);
            viewHolder.textTitle = (TextView) convertView.findViewById(R.id.text_notify_title);
            viewHolder.textNumber = (TextView) convertView.findViewById(R.id.text_notify_number);
            viewHolder.textContent = (TextView) convertView.findViewById(R.id.text_notify_content);
            viewHolder.textTime = (TextView) convertView.findViewById(R.id.text_notify_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        NotifyNewData notifyNewData = list.get(position);
        viewHolder.textTitle.setText(notifyNewData.getTitle());
        if(notifyNewData.getNumber().equals("0")){
            viewHolder.textNumber.setVisibility(View.GONE);
        } else {
            viewHolder.textNumber.setText(notifyNewData.getNumber());
        }
        viewHolder.textContent.setText(notifyNewData.getContent());
        viewHolder.textTime.setText(notifyNewData.getTime());
        return convertView;
    }

    class ViewHolder{
        TextView textTitle;     // 消息标题
        TextView textNumber;    // 消息数量
        TextView textContent;   // 消息内容 只能显示开头部分内容
        TextView textTime;      // 接收消息的时间 时间格式 MM月dd日 HH:mm  如果时间就是今天则不显示日期
    }
}
