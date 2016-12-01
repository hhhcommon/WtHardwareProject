package com.wotingfm.activity.mine.set.preference.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.music.program.fenlei.model.FenLeiName;

import java.util.List;

public class PreferGridAdapter extends BaseAdapter {
    private List<FenLeiName> list;
    private Context context;

    public PreferGridAdapter(Context context, List<FenLeiName> list) {
        super();
        this.list = list;
        this.context = context;
    }

    public void ChangeData(List<FenLeiName> list) {
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
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.img_prefer = (ImageView) convertView.findViewById(R.id.img_prefer);
            holder.rv_all = (RelativeLayout) convertView.findViewById(R.id.relativeLayout2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv_name.setText(list.get(position).getName());
//        String s1 = list.get(position).getName();
//        String s = list.get(position).getchecked();
        if (list.get(position).getchecked().equals("false")) {
            holder.img_prefer.setImageResource(R.mipmap.wt_img_unprefer);
            holder.rv_all.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_edittext_stroke_gray));
        } else {
            holder.img_prefer.setImageResource(R.mipmap.wt_img_prefer);
            holder.rv_all.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_edittext_stroke_dinglan));
        }
        return convertView;
    }

    class ViewHolder {
        public TextView tv_name;
        public ImageView img_prefer;
        public RelativeLayout rv_all;
    }
}
