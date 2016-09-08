package com.wotingfm.activity.music.search.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wotingfm.R;

import java.util.List;


public class SearchLikeAdapter extends BaseAdapter {
	private Context context;
	private List<String> list;
	private View view;
	private TextView tv;


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
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		view= LayoutInflater.from(context).inflate(R.layout.adapter_searchlike, null);

		tv=(TextView)view.findViewById(R.id.tv_search_like);
		tv.setText(list.get(position));
		return view;
	}

	/*@Override
	public View getView(FlowLayout parent, int position, Object o) {
		view=LayoutInflater.from(context).inflate(R.layout.adapter_searchlike, null);

		tv=(TextView)view.findViewById(R.id.tv_search_like);
		tv.setText(list.get(position));
		return view;
	}*/
	private class ViewHolder {
		public TextView tv;
	}
}
