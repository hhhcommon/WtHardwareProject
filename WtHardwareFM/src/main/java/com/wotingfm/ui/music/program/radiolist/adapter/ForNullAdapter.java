package com.wotingfm.ui.music.program.radiolist.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ForNullAdapter extends BaseAdapter  {
	private Context context;

	public ForNullAdapter(Context context) {
		this.context = context;
	}

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
		return position;
	}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return convertView;
    }


}
