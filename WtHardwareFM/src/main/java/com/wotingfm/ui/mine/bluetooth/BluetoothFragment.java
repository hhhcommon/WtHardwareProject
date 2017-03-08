package com.wotingfm.ui.mine.bluetooth;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wotingfm.R;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.ui.mine.main.MineActivity;
import com.wotingfm.ui.mine.main.MineFragment;
import com.wotingfm.ui.mine.bluetooth.adapter.UserBluetoothAdapter;
import com.wotingfm.ui.mine.bluetooth.model.BluetoothInfo;
import com.wotingfm.util.L;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 蓝牙设置界面
 */
public class BluetoothFragment extends Fragment implements View.OnClickListener {
    private UserBluetoothAdapter userAdapter;
    private DeviceReceiver mDevice = new DeviceReceiver();  // 蓝牙广播

    private ListView userBluetoothList;                     // 可用蓝牙设备列表
    private List<BluetoothInfo> pairList;                   // 已经配对的蓝牙列表
    private List<BluetoothInfo> userList;                   // 附近可以配对的蓝牙列表
    private List<BluetoothInfo> list = new ArrayList<>();   // 蓝牙列表 包含已配对和可以配对的设备
    private Set<BluetoothDevice> device;                    // 搜索到新的蓝牙设备列表

    private LinearLayout linearBluetoothSearch;             // 搜索蓝牙设备
    private RelativeLayout openDetection;                   // 开放检测设置  打开 OR 关闭
    private ImageView imageBluetoothSet;                    // 蓝牙设置开关
    private ImageView imageOpenDetection;                   // 蓝牙开放检测开关
    private TextView textOpenDetectionInfo;                 // 蓝牙开放检测提示信息
    private TextView textOpenDetectionTime;                 // 蓝牙开放检测倒计时时间
    private TextView textBluetoothName;                     // 蓝牙名字
    private TextView textOpenDetection;                     // "开放检测"
    private Button btnSearchDevice;                         // 扫描蓝牙设备
    private Dialog setBluetoothNameDialog;                  // 用户重命名蓝牙名字

    //    private String newName;                                 // 保存用户对设备设置的心名字
    private boolean hasRegister = false;                    // 标识 是否已注册蓝牙监听广播
    private boolean openDetectionState;                     // 标识 是否为开放检测
    private boolean isScan = true;                          // 是否正在扫描蓝牙
    private int index;                                      // 配对的蓝牙在列表中的位置
    private FragmentActivity context;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_bluetooth, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            init();
        }
        return rootView;
    }

    private void init() {
        TextView textTitle = (TextView) rootView.findViewById(R.id.text_title);
        textTitle.setText("蓝牙");// 设置标题

        ImageView leftImage = (ImageView) rootView.findViewById(R.id.left_image);
        leftImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MineActivity.close();
            }
        });

        btnSearchDevice = (Button) rootView.findViewById(R.id.bluetooth_search);// 搜索蓝牙设备
        btnSearchDevice.setOnClickListener(this);
        btnSearchDevice.setClickable(false);

        linearBluetoothSearch = (LinearLayout) rootView.findViewById(R.id.linear_bluetooth_search);
        userBluetoothList = (ListView) rootView.findViewById(R.id.list_user_bluetooth);
        userBluetoothList.setSelector(new ColorDrawable(Color.TRANSPARENT));
        View headView = LayoutInflater.from(context).inflate(R.layout.head_view_bluetooth, null);
        userBluetoothList.addHeaderView(headView);

        openDetection = (RelativeLayout) headView.findViewById(R.id.open_detection);// 开放检测
        openDetection.setOnClickListener(this);

        imageOpenDetection = (ImageView) headView.findViewById(R.id.image_open_detection);// 开放检测开关
        textOpenDetection = (TextView) headView.findViewById(R.id.text_open_detection);
        textOpenDetectionInfo = (TextView) headView.findViewById(R.id.text_open_detection_info);
        textOpenDetectionTime = (TextView) headView.findViewById(R.id.text_open_detection_time);// 开放检测时间

        textBluetoothName = (TextView) rootView.findViewById(R.id.text_device_name);
        textBluetoothName.setText(MineFragment.blueAdapter.getName());

        headView.findViewById(R.id.device_name_set).setOnClickListener(this);
        headView.findViewById(R.id.bluetooth_set).setOnClickListener(this);// 开启蓝牙

        imageBluetoothSet = (ImageView) rootView.findViewById(R.id.image_bluetooth_set);
        if (MineFragment.blueAdapter.isEnabled()) {
            imageBluetoothSet.setImageResource(R.mipmap.wt_person_on);
            openDetection.setClickable(true);
            openDetectionState = BSApplication.SharedPreferences.getBoolean(StringConstant.BLUETOOTH_OPEN_DETECTION_SET, true);// 开放蓝牙检测开关
            if (openDetectionState) {
                imageOpenDetection.setImageResource(R.mipmap.wt_person_on);
                textOpenDetectionInfo.setText("对附近所有的蓝牙设备可见");
                textOpenDetectionTime.setVisibility(View.VISIBLE);
            } else {
                imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
                textOpenDetectionInfo.setText("仅让已配对的蓝牙设备可见");
                textOpenDetectionTime.setVisibility(View.GONE);
            }
            textOpenDetection.setTextColor(getResources().getColor(R.color.wt_login_third));
            openDetection.setBackgroundDrawable(getResources().getDrawable(R.drawable.person_color));
            linearBluetoothSearch.setVisibility(View.VISIBLE);
            userBluetoothList.setDividerHeight(1);
            MineFragment.blueAdapter.startDiscovery();
            list.addAll(pairList = findAvalibleDevice());
            userBluetoothList.setAdapter(userAdapter = new UserBluetoothAdapter(context, list));
        } else {
            imageBluetoothSet.setImageResource(R.mipmap.wt_person_close);
            imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
            openDetection.setClickable(false);
            textOpenDetection.setTextColor(getResources().getColor(R.color.textshang2));
            openDetection.setBackgroundDrawable(null);
            linearBluetoothSearch.setVisibility(View.GONE);
            textOpenDetectionTime.setVisibility(View.GONE);
            textOpenDetectionInfo.setText("仅让已配对的蓝牙设备可见");
            userBluetoothList.setDividerHeight(0);
            userBluetoothList.setAdapter(userAdapter = new UserBluetoothAdapter(context, list));
        }
        setItemLis();
    }

    // 注册蓝牙接收广播
    @Override
    public void onStart() {
        if (!hasRegister) {
            hasRegister = true;
            IntentFilter filterStart = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            IntentFilter filterEnd = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            IntentFilter filterBond = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            context.registerReceiver(mDevice, filter);
            context.registerReceiver(mDevice, filterStart);
            context.registerReceiver(mDevice, filterEnd);
            context.registerReceiver(mDevice, filterBond);
        }
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bluetooth_search:// 搜索蓝牙设备
                isScan = true;
                MineFragment.blueAdapter.startDiscovery();
                if (userList != null) {
                    userList.clear();
                }
                btnSearchDevice.setText("正在扫描附近蓝牙设备...");
                btnSearchDevice.setClickable(false);
                break;
            case R.id.bluetooth_set:// 蓝牙开关
                if (MineFragment.blueAdapter.isEnabled()) {
                    MineFragment.blueAdapter.cancelDiscovery();// 停止蓝牙搜索
                    MineFragment.blueAdapter.disable();// 关闭蓝牙
//                    SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
//                    imageBluetoothSet.setImageResource(R.mipmap.wt_person_close);
//                    et.putBoolean(StringConstant.BLUETOOTH_SET, false);
//                    if(!et.commit()) {
//                        L.w("数据 commit 失败!");
//                    }
//                    openDetection.setClickable(false);
//                    imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
                } else {
                    setBluetooth();// 打开蓝牙
                }
                break;
            case R.id.open_detection:// 开放检测
                SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
                if (openDetectionState) {
                    imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
                    et.putBoolean(StringConstant.BLUETOOTH_OPEN_DETECTION_SET, false);
                    if (!et.commit()) {
                        L.w("数据 commit 失败!");
                    }
                    textOpenDetectionInfo.setText("仅让已配对的蓝牙设备可见");
                    textOpenDetectionTime.setVisibility(View.GONE);
                } else {
                    Intent intentOpen = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);// 使蓝牙设备可见，方便配对
                    intentOpen.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
                    startActivity(intentOpen);
                    imageOpenDetection.setImageResource(R.mipmap.wt_person_on);
                    et.putBoolean(StringConstant.BLUETOOTH_OPEN_DETECTION_SET, true);
                    if (!et.commit()) {
                        L.w("数据 commit 失败!");
                    }
                    textOpenDetectionInfo.setText("对附近所有的蓝牙设备可见");
                    textOpenDetectionTime.setVisibility(View.VISIBLE);
                }
                openDetectionState = !openDetectionState;
                break;
            case R.id.device_name_set:
                setBluetoothNameDialog();
                setBluetoothNameDialog.show();
                break;
        }
    }

    // 重新命名本设备蓝牙名称
    private void setBluetoothNameDialog() {
        View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_set_bluetooth_name, null);
        final EditText editNewName = (EditText) dialog.findViewById(R.id.edit_new_name);
        setBluetoothNameDialog = new Dialog(context, R.style.MyDialog);
        setBluetoothNameDialog.setContentView(dialog);
        setBluetoothNameDialog.setCanceledOnTouchOutside(false);
        setBluetoothNameDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        dialog.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = editNewName.getText().toString();
                if (!TextUtils.isEmpty(newName.trim())) {
                    MineFragment.blueAdapter.setName(newName);
                    textBluetoothName.setText(newName);
                    setBluetoothNameDialog.dismiss();
                } else {
                    Toast.makeText(context, "设备名字不能为空，请重新输入!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBluetoothNameDialog.dismiss();
            }
        });
    }

    // 蓝牙设置
    private void setBluetooth() {
        if (MineFragment.blueAdapter != null) {  // 设备支持蓝牙
            MineFragment.blueAdapter.enable(); // 直接开启，不经过提示
            imageBluetoothSet.setImageResource(R.mipmap.wt_person_on);
            openDetection.setClickable(true);
            openDetectionState = BSApplication.SharedPreferences.getBoolean(StringConstant.BLUETOOTH_OPEN_DETECTION_SET, true);
            if (openDetectionState) {
                imageOpenDetection.setImageResource(R.mipmap.wt_person_on);
                Intent intentOpen = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);// 使蓝牙设备可见，方便配对
                intentOpen.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);// 默认两分钟
                startActivity(intentOpen);
            } else {
                imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
            }
        } else {   // 设备不支持蓝牙
            Toast.makeText(context, "设备不支持蓝牙!", Toast.LENGTH_SHORT).show();
        }
    }

    // 蓝牙搜索状态广播监听
    private class DeviceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (MineFragment.blueAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    imageBluetoothSet.setImageResource(R.mipmap.wt_person_close);
                    imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
                    openDetection.setClickable(false);
                    textOpenDetection.setTextColor(getResources().getColor(R.color.textshang2));
                    openDetection.setBackgroundDrawable(null);
                    linearBluetoothSearch.setVisibility(View.GONE);
                    textOpenDetectionTime.setVisibility(View.GONE);
                    textOpenDetectionInfo.setText("仅让已配对的蓝牙设备可见");
                    userBluetoothList.setDividerHeight(0);
                    if (userList != null) {
                        userList.clear();
                    }
                    list.clear();
                    userAdapter.notifyDataSetChanged();
                } else if (MineFragment.blueAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    MineFragment.blueAdapter.startDiscovery();
                    imageBluetoothSet.setImageResource(R.mipmap.wt_person_on);
                    openDetection.setClickable(true);
                    // 开放蓝牙检测开关
                    openDetectionState = BSApplication.SharedPreferences.getBoolean(StringConstant.BLUETOOTH_OPEN_DETECTION_SET, true);
                    if (openDetectionState) {
                        imageOpenDetection.setImageResource(R.mipmap.wt_person_on);
                        textOpenDetectionInfo.setText("对附近所有的蓝牙设备可见");
                        textOpenDetectionTime.setVisibility(View.VISIBLE);
                    } else {
                        imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
                        textOpenDetectionInfo.setText("仅让已配对的蓝牙设备可见");
                        textOpenDetectionTime.setVisibility(View.GONE);
                    }
                    textOpenDetection.setTextColor(getResources().getColor(R.color.wt_login_third));
                    openDetection.setBackgroundDrawable(getResources().getDrawable(R.drawable.person_color));
                    linearBluetoothSearch.setVisibility(View.VISIBLE);
                    userBluetoothList.setDividerHeight(1);
                    list.addAll(pairList = findAvalibleDevice());
                    userAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {// 搜索到新设备
                BluetoothDevice btd = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (btd.getBondState() != BluetoothDevice.BOND_BONDED) {// 搜索没有配过对的蓝牙设备
                    if (device == null) {
                        device = new HashSet<>();
                    }
                    L.w("搜索到蓝牙设备" + btd.getName());
                    device.add(btd);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {// 搜索结束
                if (MineFragment.blueAdapter.isEnabled()) {
                    L.w("搜索结束" + list.size());
                    list.clear();
                    list.addAll(pairList = findAvalibleDevice());

                    isScan = false;
                    btnSearchDevice.setClickable(true);
                    btnSearchDevice.setText("搜索设备");
                    if (device != null && device.size() > 0) { // 存在已经配对过的蓝牙设备
                        Iterator<BluetoothDevice> it = device.iterator();
                        while (it.hasNext()) {
                            BluetoothDevice btd = it.next();
                            BluetoothInfo bName = new BluetoothInfo();
                            bName.setBluetoothName(btd.getName());
                            bName.setBluetoothAddress(btd.getAddress());
                            bName.setBluetoothType(0);
                            if (userList == null) {
                                userList = new ArrayList<>();
                            }
                            userList.add(bName);
                        }
                        list.addAll(userList);
                        userAdapter.setList(list);
                    }
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        L.d("BlueToothActivity", "正在配对......");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Toast.makeText(context, "配对成功!", Toast.LENGTH_SHORT).show();
                        pairList.add(list.get(index));
                        userList.remove(list.get(index));
                        list.clear();
                        list.addAll(pairList);
                        list.addAll(userList);
                        userAdapter.setList(list);
                        isScan = true;
                        connect(device);// 连接设备
                        break;
                    case BluetoothDevice.BOND_NONE:
                        L.d("BlueToothActivity", "取消配对");
                        break;
                }
            }
        }
    }

    // 获取已经配对的蓝牙设备
    private List<BluetoothInfo> findAvalibleDevice() {
        List<BluetoothInfo> pairList = new ArrayList<>();
        // 获取可配对蓝牙设备
        Set<BluetoothDevice> device = MineFragment.blueAdapter.getBondedDevices();
        if (device.size() > 0) { // 存在已经配对过的蓝牙设备
            Iterator<BluetoothDevice> it = device.iterator();
            while (it.hasNext()) {
                BluetoothDevice btd = it.next();
                BluetoothInfo bName = new BluetoothInfo();
                bName.setBluetoothName(btd.getName());
                bName.setBluetoothAddress(btd.getAddress());
                bName.setBluetoothType(1);
                pairList.add(bName);
            }
            L.w("--- > > " + pairList.size());
            for (int i = 0; i < pairList.size(); i++) {
                L.i(pairList.get(i).getBluetoothName());
            }
        }
        return pairList;
    }

    // 子条目点击事件  发送配对请求
    private void setItemLis() {
        userBluetoothList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!MineFragment.blueAdapter.isEnabled() || isScan || list.size() == 0) {
                    return;
                }
                if (position - 1 >= 0) {
                    String address = list.get(position - 1).getBluetoothAddress();
                    if (address != null && !address.equals("No can be matched to use bluetooth")) {
                        MineFragment.blueAdapter.cancelDiscovery();
                        BluetoothDevice mBluetoothDevice = MineFragment.blueAdapter.getRemoteDevice(address);
                        try {
                            // 连接建立之前的先配对
                            Method createBond = BluetoothDevice.class.getMethod("createBond");
                            L.e("TAG", "开始配对");
                            Boolean returnValue = (Boolean) createBond.invoke(mBluetoothDevice);
                            if (returnValue) {
                                index = position - 1;
                                list.get(index).setBluetoothType(1);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        // 点击取消已配对的蓝牙设备
        userAdapter.setListener(new UserBluetoothAdapter.CancelListener() {
            @Override
            public void cancelPair(int p) {
                if (!MineFragment.blueAdapter.isEnabled() || isScan) {
                    return;
                }
                try {
                    BluetoothDevice btDevice = MineFragment.blueAdapter.getRemoteDevice(list.get(p).getBluetoothAddress());
                    Method removeBondMethod = BluetoothDevice.class.getMethod("removeBond");
                    Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
                    if (returnValue) {
                        Toast.makeText(context, p + "--- > 取消配对", Toast.LENGTH_SHORT).show();
                        list.remove(p);
                        userAdapter.setList(list);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 蓝牙连接
    private void connect(BluetoothDevice btDev) {
        UUID uuid = UUID.fromString(StringConstant.SPP_UUID);
        try {
            BluetoothSocket btSocket = btDev.createRfcommSocketToServiceRecord(uuid);
            L.d("BlueToothTestActivity", "开始连接...");
            btSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        MineFragment.blueAdapter.cancelDiscovery();// 停止蓝牙搜索
        if (hasRegister) {
            hasRegister = false;
            context.unregisterReceiver(mDevice);
        }
        super.onDestroy();
    }
}