package com.wotingfm.ui.mine.fm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.ui.mine.fm.model.FMInfo;
import com.wotingfm.util.BitmapUtils;

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
            holder.imageSignal = (ImageView) convertView.findViewById(R.id.image_signal);
            holder.textFmFrequency = (TextView) convertView.findViewById(R.id.text_fm_frequency);
            holder.imageCheck = (ImageView) convertView.findViewById(R.id.image_check);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        FMInfo fmInfo = list.get(position);
        holder.textFmFrequency.setText(fmInfo.getFmName());// 频率
        if(fmInfo.getType() == 0) {
            holder.imageSignal.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_image_fm_signal));
            holder.textFmFrequency.setTextColor(context.getResources().getColor(R.color.wt_login_third));
            holder.imageCheck.setVisibility(View.GONE);
        } else {
            holder.imageSignal.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_image_fm_check_signal));
            holder.textFmFrequency.setTextColor(context.getResources().getColor(R.color.dinglan_orange_z));
            holder.imageCheck.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    class ViewHolder {
        ImageView imageSignal;// 信号图标
        TextView textFmFrequency;// FM
        ImageView imageCheck;// 选中图标
    }
}
