package com.wotingfm.ui.im.interphone.linkman.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.ui.im.interphone.linkman.model.TalkPersonInside;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

public class SortGroupMemberAdapter extends BaseAdapter implements SectionIndexer {
	private List<TalkPersonInside> list = null;
	private Context mContext;
	private TalkPersonInside lists;
	private OnListeners onListeners;
	private String url;

	public SortGroupMemberAdapter(Context mContext, List<TalkPersonInside> list) {
		this.mContext = mContext;
		this.list = list;
	}
	
	public void setOnListeners(OnListeners onListener) {
		this.onListeners = onListener;
	}

	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView
	 */
	public void updateListView(List<TalkPersonInside> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	public int getCount() {
		return this.list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup arg2) {
		ViewHolder holder ;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView=LayoutInflater.from(mContext).inflate(R.layout.adapter_talk_person, null);
			holder.tv_name = (TextView)convertView.findViewById(R.id.tv_name);//名
			holder.tv_b_name = (TextView)convertView.findViewById(R.id.tv_b_name);//名
			holder.imageView_touxiang=(ImageView)convertView.findViewById(R.id.image);
			holder.indexLayut=(LinearLayout)convertView.findViewById(R.id.index);
			holder.lin_add=(LinearLayout)convertView.findViewById(R.id.lin_add);
			holder.contactLayut=(LinearLayout)convertView.findViewById(R.id.contactLayut);
			holder.indexTv = (TextView) convertView.findViewById(R.id.indexTv); 
			convertView.setTag(holder);
		}else{
			holder=(ViewHolder) convertView.getTag();
		}
		lists=list.get(position);
		// 根据position获取分类的首字母的Char ascii值
		int section = getSectionForPosition(position);
		// 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
		// 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
		if (position == getPositionForSection(section)) {
			holder.indexLayut.setVisibility(View.VISIBLE);
			holder.indexTv.setText(list.get(position).getSortLetters());  
		} else {
			holder.indexLayut.setVisibility(View.GONE);
		}
			
			if(lists.getUserName()==null||lists.getUserName().equals("")){
				holder.tv_name.setText("未知");//名
			}else{
				holder.tv_name.setText(lists.getUserName());//名
			}
			if(lists.getUserAliasName()==null||lists.getUserAliasName().equals("")){
				holder.tv_b_name.setVisibility(View.GONE);
			}else{
				holder.tv_b_name.setVisibility(View.VISIBLE);
				holder.tv_b_name.setText(lists.getUserAliasName());//名
			}
			
			if(lists.getPortraitMini()==null||lists.getPortraitMini().equals("")||lists.getPortraitMini().equals("null")||lists.getPortraitMini().trim().equals("")){
				Bitmap bmp = BitmapUtils.readBitMap(mContext, R.mipmap.wt_image_tx_hy);
				holder.imageView_touxiang.setImageBitmap(bmp);
			}else{
//				Bitmap bmp = BitmapUtils.readBitMap(mContext, R.mipmap.wt_image_tx_hy);
//				holder.imageView_touxiang.setImageBitmap(bmp);
				//将图片切成长宽都是100 加载到控件上，适合确定控件长宽
				if (lists.getPortraitMini().startsWith("http:")) {
					url = lists.getPortraitMini();
				} else {
					url = GlobalConfig.imageurl + lists.getPortraitMini();
				}
				Picasso.with(mContext).load(url.replace( "\\/", "/")).resize(100, 100).centerCrop().into(holder.imageView_touxiang);
			}
		
		holder.lin_add.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onListeners.add(position);
			}
		});	
		return convertView;
	}
	
	public interface OnListeners {
		void add(int position);
	}

	final static class ViewHolder {
		public TextView tv_b_name;
		public LinearLayout lin_add;
		public LinearLayout contactLayut;
		public TextView indexTv;
		public LinearLayout indexLayut;
		public ImageView imageView_touxiang;
		public TextView tv_name;
	}

	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	public int getSectionForPosition(int position) {
		return list.get(position).getSortLetters().charAt(0);
	}

	/**
	 * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
	 */
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * 提取英文的首字母，非英文字母用#代替。
	 * 
	 * @param str
	 * @return
	 */
	private String getAlpha(String str) {
		String sortStr = str.trim().substring(0, 1).toUpperCase();
		// 正则表达式，判断首字母是否是英文字母
		if (sortStr.matches("[A-Z]")) {
			return sortStr;
		} else {
			return "#";
		}
	}

	@Override
	public Object[] getSections() {
		return null;
	}

}