package com.wotingfm.activity.mine.set.preference.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.mine.set.preference.activity.PreferenceActivity;
import com.wotingfm.activity.music.program.fenlei.model.FenLei;
import com.wotingfm.widget.MyGridView;

import java.util.List;

/**
 * 偏好设置的适配器
 * 作者：xinlong on 2016/10/20
 * 邮箱：645700751@qq.com
 */
public class PianHaoAdapter extends BaseAdapter {
    private List<FenLei> list;
    private Context context;
    private PreferGridAdapter adapters;
    private preferCheck preferCheck;

    public PianHaoAdapter(Context context, List<FenLei> list) {
        super();
        this.list = list;
        this.context = context;
    }

    public void setOnListener(preferCheck friendCheck) {
        this.preferCheck = friendCheck;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_prefer_group, parent, false);
            holder = new ViewHolder();
            holder.textName = (TextView) convertView.findViewById(R.id.tv_name);
            holder.textAllCheck = (TextView) convertView.findViewById(R.id.tv_Quan_Xuan);
            holder.gridView = (MyGridView) convertView.findViewById(R.id.gridView);
            holder.gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textName.setText(list.get(position).getName());
        adapters = new PreferGridAdapter(context, list.get(position).getChildren());
        holder.gridView.setAdapter(adapters);

        holder.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int positions, long id) {
                String checkString = list.get(position).getChildren().get(positions).getchecked();
                if (checkString.equals("false")) {
                    list.get(position).getChildren().get(positions).setchecked("true");
                } else {
                    list.get(position).getChildren().get(positions).setchecked("false");
                }
                PreferenceActivity.RefreshView(list);
            }
        });
        if (list.get(position).getTag() == position && list.get(position).getTagType() == 1) {
            holder.textAllCheck.setText("取消全选");
            holder.textAllCheck.setTextColor(context.getResources().getColor(R.color.gray));
            holder.textName.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
        } else {
            holder.textAllCheck.setText("全选");
            holder.textAllCheck.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            holder.textName.setTextColor(context.getResources().getColor(R.color.black));
        }
        holder.textAllCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferCheck.clickPosition(position);
            }
        });
        return convertView;
    }

    public interface preferCheck {
        void clickPosition(int position);
    }

    class ViewHolder {
        public TextView textName;
        public MyGridView gridView;
        public TextView textAllCheck;
    }
}
