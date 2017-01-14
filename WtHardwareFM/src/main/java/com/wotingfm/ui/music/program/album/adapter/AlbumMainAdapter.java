package com.wotingfm.ui.music.program.album.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.ui.music.program.album.model.ContentInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 专辑列表界面数据适配
 */
public class AlbumMainAdapter extends BaseAdapter {
	private List<ContentInfo> list;
	private Context context;
	private SimpleDateFormat format;

	public AlbumMainAdapter(Context context, List<ContentInfo> subList) {
		this.list = subList;
		this.context = context;
        format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
	}

	public void ChangeDate(List<ContentInfo> list) {
		this.list = list;
		this.notifyDataSetChanged();
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
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.adapter_album_main, null);
			holder.textName = (TextView) convertView.findViewById(R.id.tv_name);// 节目名
			holder.textPlayNum = (TextView) convertView.findViewById(R.id.tv_playnum);// 播放次数
            holder.textTime = (TextView) convertView.findViewById(R.id.text_time);// 节目时长
			holder.textCTime = (TextView) convertView.findViewById(R.id.tv_time);// CTime
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
        ContentInfo lists = list.get(position);

        // 节目名
        String contentName = lists.getContentName();
		if (contentName == null || contentName.equals("")) {
            contentName = "未知";
		}
        holder.textName.setText(contentName);

        // 播放次数
        String playCount = lists.getPlayCount();
		if (playCount == null || playCount.equals("")) {
            playCount = "1234";
		}
        Float count = Float.valueOf(playCount);
        if(count > 10000) {
            count = count / 10000;
            playCount = count + "万";
            if(count > 10000) {
                count = count / 10000;
                playCount = count + "亿";
            }
        }
        holder.textPlayNum.setText(playCount);

        // 节目时长
        String contentTime = lists.getContentTimes();
        if (contentTime == null || contentTime.equals("") || contentTime.equals("null")) {
            holder.textTime.setText(context.getString(R.string.play_time));
        } else {
            try {
                int time = Integer.valueOf(contentTime);
                int minute = time / (1000 * 60);
                int second = (time / 1000) % 60;
                if(second < 10){
                    contentTime = minute + "\'" + " " + "0" + second + "\"";
                }else{
                    contentTime = minute + "\'" + " " + second + "\"";
                }
                holder.textTime.setText(contentTime);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                holder.textTime.setText(context.getString(R.string.play_time));
            }
        }

        // CTime
        String cTime = lists.getCTime();
		if (cTime == null || cTime.equals("")) {
            cTime = "1970-01-18";
		} else {
            cTime = format.format(new Date(Long.parseLong(cTime)));
		}
        holder.textCTime.setText(cTime);
		
		return convertView;
	}

	class ViewHolder {
        public TextView textName;// 节目名
        public TextView textPlayNum;// 播放次数
        public TextView textTime;// 节目时长
		public TextView textCTime;// CTime
	}
}
