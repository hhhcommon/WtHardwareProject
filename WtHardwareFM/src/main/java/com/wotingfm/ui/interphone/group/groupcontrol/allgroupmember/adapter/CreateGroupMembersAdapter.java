package com.wotingfm.ui.interphone.group.groupcontrol.allgroupmember.adapter;

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

import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.IntegerConstant;
import com.wotingfm.ui.common.model.UserInfo;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

public class CreateGroupMembersAdapter extends BaseAdapter implements SectionIndexer {
    private List<UserInfo> list;
    private Context context;
    private UserInfo lists;
    private String url;

    public CreateGroupMembersAdapter(Context context, List<UserInfo> list) {
        this.list = list;
        this.context = context;
    }

    public void ChangeDate(List<UserInfo> list) {
        this.list = list;
        this.notifyDataSetChanged();
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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder ;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_group_members, null);
            holder = new ViewHolder();
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);//名
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            holder.indexLayut = (LinearLayout) convertView.findViewById(R.id.index);
            holder.contactLayut = (LinearLayout) convertView.findViewById(R.id.contactLayut);
            holder.indexTv = (TextView) convertView.findViewById(R.id.indexTv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        lists = list.get(position);
        // 根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);
        // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section)) {
            holder.indexLayut.setVisibility(View.VISIBLE);
            holder.indexTv.setText(list.get(position).getSortLetters());
        } else {
            holder.indexLayut.setVisibility(View.GONE);
        }

        if (lists.getUserAliasName() != null) {
            holder.tv_name.setText(lists.getUserAliasName());
        } else {
            if (lists.getUserName() == null || lists.getUserName().equals("")) {
                holder.tv_name.setText("未知");//名
            } else {
                if (lists.getUserName() == null || lists.getUserName().equals("")) {
                    holder.tv_name.setText("未知");//名
                } else {
                    holder.tv_name.setText(lists.getUserName());//名
                }
            }
            holder.tv_name.setText(lists.getUserName());//名
        }

        if (lists.getPortraitMini() == null || lists.getPortraitMini().equals("") || lists.getPortraitMini().equals("null") || lists.getPortraitMini().trim().equals("")) {
            Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_tx_hy);
            holder.image.setImageBitmap(bmp);
        } else {
            if (lists.getPortraitMini().startsWith("http:")) {
                url = lists.getPortraitMini();
            } else {
                url = GlobalConfig.imageurl + lists.getPortraitMini();
            }
            String _url = AssembleImageUrlUtils.assembleImageUrl150(url);

            // 加载图片
            AssembleImageUrlUtils.loadImage(_url, url, holder.image, IntegerConstant.TYPE_PERSON);
        }
        return convertView;
    }

    class ViewHolder {
        public ImageView image;
        public LinearLayout contactLayut;
        public TextView indexTv;
        public LinearLayout indexLayut;
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
