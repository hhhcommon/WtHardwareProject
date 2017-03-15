package com.wotingfm.ui.mine.bluetooth.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.ui.mine.bluetooth.model.BluetoothInfo;
import com.wotingfm.util.L;

import java.util.List;

/**
 * 可用蓝牙列表
 * Created by Administrator on 9/7/2016.
 */
public class PairBluetoothAdapter extends BaseAdapter {
    private Context context;
    private List<BluetoothInfo> list;
    private PairInfoListener pairInfoListener;

    public PairBluetoothAdapter(Context context, List<BluetoothInfo> list) {
        this.context = context;
        this.list = list;
    }

    public void setListener(PairInfoListener pairInfoListener) {
        this.pairInfoListener = pairInfoListener;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public BluetoothInfo getItem(int position) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_user_bluebooth_pair, parent, false);
            holder.textBluetoothName = (TextView) convertView.findViewById(R.id.text_bluebooth_name);// 设备名字
//            holder.imageConnInfo = (ImageView) convertView.findViewById(R.id.image_conn_info);// 查看已经配对过的设备信息
            holder.viewConn = convertView.findViewById(R.id.view_conn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        BluetoothInfo bName = list.get(position);
        if (bName.getBluetoothName() == null) {
            holder.textBluetoothName.setText(bName.getBluetoothAddress());
        } else {
            holder.textBluetoothName.setText(bName.getBluetoothName());
        }
        L.w("bName" + bName + "position == " + position);
        holder.viewConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairInfoListener.pairInfo(position);
            }
        });

        return convertView;
    }

    public interface PairInfoListener {
        void pairInfo(int p);
    }

    class ViewHolder {
        TextView textBluetoothName;
        View viewConn;
//        ImageView imageConnInfo;// 配对过的设备信息
    }
}
