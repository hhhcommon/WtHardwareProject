package com.wotingfm.activity.mine.wifi.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.util.L;

import java.util.List;

/**
 * WiFi列表
 * Created by Administrator on 9/9/2016.
 */
public class WiFiListAdapter extends BaseAdapter {
    private Context context;
    private List<ScanResult> list;
    private WifiManager wifiManager;

    public WiFiListAdapter(Context context, List<ScanResult> list) {
        this.context = context;
        this.list = list;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public void setList(List<ScanResult> list) {
        this.list = list;
        notifyDataSetChanged();
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
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_wifi_list, parent, false);
            holder.textWiFiName = (TextView) convertView.findViewById(R.id.text_wifi_name);
            holder.textWiFiState = (TextView) convertView.findViewById(R.id.text_wifi_state);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ScanResult result = list.get(position);
        holder.textWiFiName.setText(result.SSID);
        String connWiFiName = wifiManager.getConnectionInfo().getSSID();
        if (connWiFiName.startsWith("\"")) {
            connWiFiName = connWiFiName.substring(1, connWiFiName.length() - 1);
        }
        if (result.SSID.equals(connWiFiName)) {
            holder.textWiFiState.setText("已连接");
            holder.textWiFiName.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
        } else {
            holder.textWiFiState.setText("通过WPA/WPA2进行保护");
            holder.textWiFiName.setTextColor(context.getResources().getColor(R.color.BLACK));
        }
        L.i("WiFi信息 -- > " + result.toString());
        return convertView;
    }

    class ViewHolder {
        TextView textWiFiName;// WiFi Name
        TextView textWiFiState;// WiFi 状态
    }
}
