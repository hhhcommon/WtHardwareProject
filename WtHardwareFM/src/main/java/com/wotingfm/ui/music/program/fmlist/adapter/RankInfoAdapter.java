package com.wotingfm.ui.music.program.fmlist.adapter;

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
import com.wotingfm.ui.music.program.fmlist.model.RankInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

// 这个代码写完了 然后要求对应着代码加载看看
public class RankInfoAdapter extends BaseAdapter   {
	private List<RankInfo> list;
	private Context context;
//	private RankInfo rank;
//	private String url;

	public RankInfoAdapter(Context context, List<RankInfo> list) {
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder ;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.adapter_rankinfo, null);
			holder.textview_ranktitle = (TextView) convertView.findViewById(R.id.RankTitle);// 台名
			holder.imageview_rankimage = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 电台图标
			holder.textview_rankplaying=(TextView)convertView.findViewById(R.id.RankPlaying);

			holder.img_zhezhao = (ImageView) convertView.findViewById(R.id.img_zhezhao);
			Bitmap bmp_zhezhao = BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b);
			holder.img_zhezhao.setImageBitmap(bmp_zhezhao);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		RankInfo lists = list.get(position);

		if (lists.getContentName() == null || lists.getContentName().equals("")) {
			holder.textview_ranktitle.setText("未知");
		} else {
			holder.textview_ranktitle.setText(lists.getContentName());
		}

        // 正在直播的节目
        String contentPlaying = lists.getIsPlaying();
        if (contentPlaying == null || contentPlaying.equals("")) {
            contentPlaying = "暂无节目单";
        }
		holder.textview_rankplaying.setText(contentPlaying);

		if (lists.getContentImg() == null || lists.getContentImg().equals("")
				|| lists.getContentImg().equals("null") || lists.getContentImg().trim().equals("")) {
			Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx_d);
			holder.imageview_rankimage.setImageBitmap(bmp);
		} else {
			String url;
			if(lists.getContentImg().startsWith("http")){
				url =  lists.getContentImg();
			}else{
				url = GlobalConfig.imageurl + lists.getContentImg();
			}
			url= AssembleImageUrlUtils.assembleImageUrl150(url);
			Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageview_rankimage);
		}
		return convertView;
	}

	class ViewHolder {
		public ImageView imageview_rankimage;
		public TextView textview_ranktitle;
		public TextView textview_rankplaying;
		public ImageView img_zhezhao;
	}
}
