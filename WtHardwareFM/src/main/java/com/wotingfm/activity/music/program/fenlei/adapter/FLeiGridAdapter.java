package com.wotingfm.activity.music.program.fenlei.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.music.program.fenlei.model.fenLeiName;

import java.util.List;

/**
 * 分类 GridView 的数据适配
 */
public class FLeiGridAdapter extends BaseAdapter {
	private List<fenLeiName> list;
	private Context context;

	public FLeiGridAdapter(Context context, List<fenLeiName> list) {
		super();
		this.list = list;
		this.context = context;
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
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.adapter_fenlei_child_grid, parent, false);
			holder.textName = (TextView) convertView.findViewById(R.id.tv_name);// 台名
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.textName.setText(list.get(position).getCatalogName());
		return convertView;
	}

	class ViewHolder {
		public TextView textName;
	}
}
