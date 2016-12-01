package com.wotingfm.activity.mine.set.preference.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.music.program.fenlei.model.FenLeiName;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

public class PreferGridAdapter extends BaseAdapter {
    private List<FenLeiName> list;
    private Context context;

    public PreferGridAdapter(Context context, List<FenLeiName> list) {
        this.list = list;
        this.context = context;
    }

    public void changeData(List<FenLeiName> list) {
        this.list = list;
        notifyDataSetInvalidated();
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
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_prefer_child_grid, null);
            holder = new ViewHolder();
            holder.textName = (TextView) convertView.findViewById(R.id.tv_name);
            holder.imgPrefer = (ImageView) convertView.findViewById(R.id.img_prefer);
            holder.viewAll = convertView.findViewById(R.id.relativeLayout2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textName.setText(list.get(position).getName());
        if (list.get(position).getchecked().equals("false")) {
            holder.imgPrefer.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_img_unprefer));
            holder.viewAll.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_edittext_stroke_gray));
        } else {
            holder.imgPrefer.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_img_prefer));
            holder.viewAll.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_edittext_stroke_dinglan));
        }
        return convertView;
    }

    class ViewHolder {
        public TextView textName;
        public ImageView imgPrefer;
        public View viewAll;
    }
}
