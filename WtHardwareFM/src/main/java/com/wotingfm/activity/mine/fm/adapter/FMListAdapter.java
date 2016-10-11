package com.wotingfm.activity.mine.fm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.mine.fm.model.FMInfo;

import java.util.List;

/**
 * FM列表
 * Created by Administrator on 9/10/2016.
 */
public class FMListAdapter extends BaseAdapter {
    private Context context;
    private List<FMInfo> list;

    public FMListAdapter(Context context, List<FMInfo> list){
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
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_fm_list, parent, false);
            holder.textFmFrequency = (TextView) convertView.findViewById(R.id.text_fm_frequency);
            holder.textFmInfo = (TextView) convertView.findViewById(R.id.text_fm_info);
            holder.linearView = (LinearLayout) convertView.findViewById(R.id.linear_view);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        FMInfo fmInfo = list.get(position);
        holder.textFmFrequency.setText(fmInfo.getFmName());// 频率
        holder.textFmInfo.setText(fmInfo.getFmIntroduce());
        if(fmInfo.getType() == 0) {
            holder.textFmInfo.setVisibility(View.GONE);
            holder.textFmFrequency.setTextColor(context.getResources().getColor(R.color.wt_login_third));
            holder.linearView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.person_color));
        } else {
            holder.textFmInfo.setVisibility(View.VISIBLE);
            holder.textFmFrequency.setTextColor(context.getResources().getColor(R.color.green));
            holder.linearView.setBackgroundDrawable(context.getResources().getDrawable(R.color.linkman_bt));
        }
        return convertView;
    }

    class ViewHolder {
        LinearLayout linearView;
        TextView textFmFrequency;
        TextView textFmInfo;
    }
}
