package com.wotingfm.activity.im.interphone.message.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.im.interphone.message.model.MessageInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.helper.ImageLoader;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public class NewsAdapter extends BaseAdapter {
    private List<MessageInfo> list;
    private Context context;
    private ImageLoader imageLoader;
    private MessageInfo lists;
    private SimpleDateFormat format;
    protected OnListener onListener;

    public NewsAdapter(Context context, List<MessageInfo> list) {
        super();
        this.list = list;
        this.context = context;
        imageLoader = new ImageLoader(context);
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }

    public void setOnListener(OnListener onListener) {
        this.onListener = onListener;
    }

    public void changeDate(List<MessageInfo> list) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_message_list, parent, false);
            holder.Image = (ImageView) convertView.findViewById(R.id.image);
            holder.tv_news = (TextView) convertView.findViewById(R.id.tv_news);
            holder.tv_jieshao = (TextView) convertView.findViewById(R.id.tv_jieshao);
            holder.time = (TextView) convertView.findViewById(R.id.tv_time);
            holder.tv_res = (TextView) convertView.findViewById(R.id.tv_res);
            holder.tv_acc = (TextView) convertView.findViewById(R.id.tv_acc);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        lists = list.get(position);
        if (lists != null && lists.getMSType() != null && !lists.getMSType().equals("")) {
            if (lists.getMSType().equals("person")) {
                if (lists.getUserName() == null || lists.getUserName().equals("")) {
                    holder.tv_news.setText("未知");
                } else {
                    holder.tv_news.setText(lists.getUserName() + "添加您为好友");
                }

                if (lists.getInviteMesage() == null || lists.getInviteMesage().equals("")) {
                    holder.tv_jieshao.setText("无邀请信息");
                } else {
                    holder.tv_jieshao.setText("" + lists.getInviteMesage());
                }
                if (lists.getInviteTime() == null || lists.getInviteTime().equals("") || lists.getInviteTime().equals("null")) {
                    holder.time.setText("0000-00-00  00:00");
                } else {
                    long time = Long.parseLong(lists.getInviteTime()) / 1000;
                    holder.time.setText(format.format(new Date(time)));
                }
                if (lists.getPortrait() != null && lists.getPortrait().equals("") && !lists.getPortrait().equals("null")) {
                    String url;
                    if (lists.getPortrait().startsWith("http:")) {
                        url = lists.getPortrait();
                    } else {
                        url = GlobalConfig.imageurl + lists.getPortrait();
                    }
                    imageLoader.DisplayImage(url.replace("\\/", "/"), holder.Image, false, false, null, null);
//					holder.Image.setImageResource(R.drawable.wt_image_tx_hy);
                }
            } else {
                if (lists.getGroupName() == null || lists.getGroupName().equals("")) {
                    holder.tv_news.setText("未知");
                } else {
                    holder.tv_news.setText("组名: " + lists.getGroupName());
                }
                if (lists.getUserName() == null || lists.getUserName().equals("")) {
                    holder.tv_jieshao.setText("无邀请信息");
                } else {
                    holder.tv_jieshao.setText("" + lists.getUserName() + "邀请您加入该群");
                }
                if (lists.getInviteTime() == null || lists.getInviteTime().equals("") || lists.getInviteTime().equals("null")) {
                    holder.time.setText("0000-00-00  00:00");
                } else {
                    long time = Long.parseLong(lists.getInviteTime());
                    holder.time.setText(format.format(new Date(time)));
                }
                if (lists.getProtraitMini() != null && !lists.getProtraitMini().equals("")
                        && !lists.getProtraitMini().equals("null")) {
//                    holder.Image.setImageResource(R.drawable.wt_image_tx_qz);
                    String url;
                    if (lists.getProtraitMini().startsWith("http:")) {
                        url = lists.getProtraitMini();
                    } else {
                        url = GlobalConfig.imageurl + lists.getProtraitMini();
                    }
                    imageLoader.DisplayImage(url.replace("\\/", "/"), holder.Image, false, false, null, null);
                }
            }
        }
        holder.tv_acc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onListener.agree(position);
            }
        });
        holder.tv_res.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onListener.refused(position);
            }
        });
        return convertView;
    }

    public interface OnListener {
        void agree(int position);

        void refused(int position);
    }

    class ViewHolder {
        public TextView tv_jieshao;
        public TextView tv_acc;
        public TextView tv_res;
        public TextView time;
        public TextView tv_news;
        public ImageView Image;
    }
}
