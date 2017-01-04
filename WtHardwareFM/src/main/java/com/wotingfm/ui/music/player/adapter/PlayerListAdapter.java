package com.wotingfm.ui.music.player.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.ui.music.player.model.LanguageSearchInside;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

public class PlayerListAdapter extends BaseAdapter {
	private List<LanguageSearchInside> list;
	private Context context;
	private Bitmap bmp;

	public PlayerListAdapter(Context context, List<LanguageSearchInside> list) {
		this.context = context;
		this.list = list;
		bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
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
			convertView = LayoutInflater.from(context).inflate(R.layout.adapter_fragment_player, null);
			holder = new ViewHolder();
			holder.textview_ranktitle = (TextView) convertView.findViewById(R.id.RankTitle);// 台名
			holder.RankContent = (TextView) convertView.findViewById(R.id.RankContent);// 台名
			holder.imageview_rankimage = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 电台图标
			holder.mTv_number = (TextView) convertView.findViewById(R.id.tv_num);
			holder.imageView_playering = (ImageView) convertView.findViewById(R.id.imageView_playering);
			holder.textPlayTime = (TextView) convertView.findViewById(R.id.tv_last);
			holder.imageView_playering.setBackgroundResource(R.drawable.playering_show);
			holder.img_zhezhao = (ImageView) convertView.findViewById(R.id.img_zhezhao);
			Bitmap bmp_zhezhao = BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b);
			holder.img_zhezhao.setImageBitmap(bmp_zhezhao);
			holder.draw = (AnimationDrawable) holder.imageView_playering.getBackground();
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		LanguageSearchInside searchlist = list.get(position);
		if (searchlist != null) {
			if (searchlist.getType().equals("2")) {
				//播放状态，按钮显示
				holder.imageView_playering.setVisibility(View.VISIBLE);
				holder.textview_ranktitle.setTextColor(context.getResources().getColor(R.color.dinglan_orange_z));
				if (holder.draw.isRunning()) {
				} else {
					holder.draw.start();
				}
			} else if (searchlist.getType().equals("0")) {
				holder.draw.stop();
				holder.draw.selectDrawable(0);
				//播放状态，按钮显示
				holder.imageView_playering.setVisibility(View.VISIBLE);
				holder.textview_ranktitle.setTextColor(context.getResources().getColor(R.color.dinglan_orange_z));

			} else if (searchlist.getType().equals("1")) {
				holder.imageView_playering.setVisibility(View.INVISIBLE);
				holder.textview_ranktitle.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
				if (holder.draw.isRunning()) {
					holder.draw.stop();
				}
			}

			if (searchlist.getPlayCount() == null || searchlist.getPlayCount().equals("")) {
				holder.mTv_number.setText("0");
			} else {
				holder.mTv_number.setText(searchlist.getPlayCount());
			}

			if (searchlist.getContentImg() != null && !searchlist.getContentImg().equals("")) {
				String url;
				if (searchlist.getContentImg().startsWith("http")) {
					//url = allList.get(number).getContentImg();
					url= AssembleImageUrlUtils.assembleImageUrl180(searchlist.getContentImg());
				} else {
					// url = GlobalConfig.imageurl + allList.get(number).getContentImg();
					url=AssembleImageUrlUtils.assembleImageUrl180(GlobalConfig.imageurl+searchlist.getContentImg());
				}
				Picasso.with(context).load(url.replace("\\/", "/")).into(holder.imageview_rankimage);

			} else {
				holder.imageview_rankimage.setImageBitmap(bmp);
			}
			if (searchlist.getContentPub() != null && searchlist.getContentPub() != null && !searchlist.getContentPub().equals("")) {
				holder.RankContent.setText(searchlist.getContentPub());
			} else {
				holder.RankContent.setText("我听科技");
			}
			if (searchlist.getContentName() != null && !searchlist.getContentName().equals("")) {
				holder.textview_ranktitle.setText(searchlist.getContentName());
			} else {
				holder.textview_ranktitle.setText("woting_music");
			}

			if (searchlist.getPlayerAllTime() != null && !searchlist.getPlayerAllTime().equals("")) {
				try {
					int minute = Integer.valueOf(searchlist.getPlayerAllTime()) / (1000 * 60);
					int second = (Integer.valueOf(searchlist.getPlayerAllTime()) / 1000) % 60;
					if (second < 10) {
						holder.textPlayTime.setText(minute + "\'" + " " + "0" + second + "\"");
					} else {
						holder.textPlayTime.setText(minute + "\'" + " " + second + "\"");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				//节目时长

				if (searchlist.getContentTimes() == null
						|| searchlist.getContentTimes().equals("")
						|| searchlist.getContentTimes().equals("null")) {
					holder.textPlayTime.setText(context.getString(R.string.play_time));
				} else {
					try {
						if (searchlist.getContentTimes().contains(":")) {
							holder.textPlayTime.setText(searchlist.getContentTimes());
						} else {
							int minute = Integer.valueOf(searchlist.getContentTimes()) / (1000 * 60);
							int second = (Integer.valueOf(searchlist.getContentTimes()) / 1000) % 60;
							if (second < 10) {
								holder.textPlayTime.setText(minute + "\'" + " " + "0" + second + "\"");
							} else {
								holder.textPlayTime.setText(minute + "\'" + " " + second + "\"");
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return convertView;
	}

	static class ViewHolder {
		public AnimationDrawable draw;
		public ImageView imageView_playering;
		public TextView RankContent;
		public ImageView imageview_rankimage;
		public TextView textview_ranktitle;
		public TextView mTv_number;
		public TextView textPlayTime;
		public ImageView img_zhezhao;
	}
}
