package com.wotingfm.activity.music.program.fenlei.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.activity.music.program.fenlei.model.FLeiName;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

/**
 * 分类 GridView 的数据适配
 */
public class FLeiGridAdapter extends BaseAdapter {
	private List<FLeiName> list;
	private Context context;

	public FLeiGridAdapter(Context context, List<FLeiName> list) {
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
			holder.image = (ImageView) convertView.findViewById(R.id.image);// 台名
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		FLeiName lists = list.get(position);
		holder.textName.setText(lists.getCatalogName());

		if(lists.getPortraitMini()==null||lists.getPortraitMini().equals("")||lists.getPortraitMini().equals("null")||lists.getPortraitMini().trim().equals("")){
			Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_img_catalog);
			holder.image.setImageBitmap(bmp);
		}else{
			String url;
			if (lists.getPortraitMini().startsWith("http:")) {
				url = lists.getPortraitMini();
			} else {
				url = GlobalConfig.imageurl + lists.getPortraitMini();
			}
			Picasso.with(context).load(url.replace( "\\/", "/")).resize(40, 40).centerCrop().into(holder.image);
		}
		return convertView;
	}

	class ViewHolder {
		public TextView textName;
		public ImageView image;
	}
}
