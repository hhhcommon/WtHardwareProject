package com.wotingfm.activity.mine.bluetooth.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wotingfm.R;

import java.util.List;

/**
 * 可用蓝牙列表
 * Created by Administrator on 9/7/2016.
 */
public class UserBluetoothAdapter extends BaseAdapter {
    private Context context;
    private List<String> list;

    public UserBluetoothAdapter(Context context, List<String> list){
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
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_user_bluebooth, parent, false);
            holder.textBlueboothName = (TextView) convertView.findViewById(R.id.text_bluebooth_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textBlueboothName.setText(list.get(position));

        return convertView;
    }

    class ViewHolder {
        TextView textBlueboothName;
    }
}
