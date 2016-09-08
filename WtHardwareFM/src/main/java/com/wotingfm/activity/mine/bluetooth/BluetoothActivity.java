package com.wotingfm.activity.mine.bluetooth;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.activity.mine.bluetooth.adapter.UserBluetoothAdapter;
import com.wotingfm.activity.mine.main.MineActivity;
import com.wotingfm.common.constant.StringConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 蓝牙设置界面
 */
public class BluetoothActivity extends AppBaseActivity implements View.OnClickListener {
    private boolean hasRegister = false;// 标识 是否已注册蓝牙监听广播
    private SharedPreferences sharedPreferences;
    private boolean openDetectionState;// 标识 是否为开放检测
    private String newName;// 保存用户对设备设置的心名字
    private Dialog setBluetoothNameDialog;// 用户重命名蓝牙名字

    private DeviceReceiver mDevice = new DeviceReceiver();// 蓝牙广播
    private ListView userBluetoothList;// 可用蓝牙设备列表
    private ListView pairBluetoothList;// 已配对蓝牙设备列表
    private List<String> pairList;
    private List<String> userList;

    private LinearLayout linearBluetoothSearch;// 搜索蓝牙设备
    private RelativeLayout openDetection;// 开放检测设置  打开 OR 关闭
    private ImageView imageBluetoothSet;// 蓝牙设置开关
    private ImageView imageOpenDetection;// 蓝牙开放检测开关
    private TextView textOpenDetectionInfo;// 蓝牙开放检测提示信息
    private TextView textOpenDetectionTime;// 蓝牙开放检测倒计时时间
    private TextView textUserDevice;// 提示文字 可用设备
    private TextView textPairDevice;// 提示文字 已配对设备
    private TextView textBluetoothName;// 蓝牙名字
    private TextView textOpenDetection;// "开放检测"

    @Override
    protected int setViewId() {
        return R.layout.activity_bluetooth;
    }

    @Override
    protected void init() {
        setTitle("蓝牙");
        sharedPreferences = getSharedPreferences("wotingfm", Context.MODE_PRIVATE);

        Button btnSearchDevice = (Button) findViewById(R.id.bluetooth_search);// 搜索蓝牙设备
        btnSearchDevice.setOnClickListener(this);

        linearBluetoothSearch = (LinearLayout) findViewById(R.id.linear_bluetooth_search);
        userBluetoothList = (ListView) findViewById(R.id.list_user_bluetooth);
        View headView = LayoutInflater.from(context).inflate(R.layout.head_view_bluetooth, null);
        userBluetoothList.addHeaderView(headView);
        pairBluetoothList = (ListView) headView.findViewById(R.id.list_pair_bluetooth);

        openDetection = (RelativeLayout) headView.findViewById(R.id.open_detection);// 开放检测
        openDetection.setOnClickListener(this);
        imageOpenDetection = (ImageView) headView.findViewById(R.id.image_open_detection);// 开放检测开关
        textOpenDetection = (TextView) headView.findViewById(R.id.text_open_detection);
        textOpenDetectionInfo = (TextView) headView.findViewById(R.id.text_open_detection_info);
        textOpenDetectionTime = (TextView) headView.findViewById(R.id.text_open_detection_time);// 开放检测时间
        textUserDevice = (TextView) headView.findViewById(R.id.text_user_device);
        textPairDevice = (TextView) headView.findViewById(R.id.text_pair_device);

        textBluetoothName = (TextView) findViewById(R.id.text_device_name);
        textBluetoothName.setText(MineActivity.blueAdapter.getName());

        headView.findViewById(R.id.device_name_set).setOnClickListener(this);
        headView.findViewById(R.id.bluetooth_set).setOnClickListener(this);// 开启蓝牙
        imageBluetoothSet = (ImageView) findViewById(R.id.image_bluetooth_set);
        if (MineActivity.blueAdapter.isEnabled()) {
            imageBluetoothSet.setImageResource(R.mipmap.wt_person_on);
            openDetection.setClickable(true);
            openDetectionState = sharedPreferences.getBoolean(StringConstant.BLUETOOTH_OPEN_DETECTION_SET, true);// 开放蓝牙检测开关
            if(openDetectionState){
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
            textUserDevice.setVisibility(View.VISIBLE);
            textPairDevice.setVisibility(View.VISIBLE);
            pairBluetoothList.setVisibility(View.VISIBLE);
            linearBluetoothSearch.setVisibility(View.VISIBLE);
            userBluetoothList.setDividerHeight(1);
            List list = new ArrayList();
            list.add("小米");
            userBluetoothList.setAdapter(new UserBluetoothAdapter(context, list));
        } else {
            imageBluetoothSet.setImageResource(R.mipmap.wt_person_close);
            imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
            openDetection.setClickable(false);
            textOpenDetection.setTextColor(getResources().getColor(R.color.textshang2));
            openDetection.setBackgroundDrawable(null);
            textUserDevice.setVisibility(View.GONE);
            textPairDevice.setVisibility(View.GONE);
            pairBluetoothList.setVisibility(View.GONE);
            linearBluetoothSearch.setVisibility(View.GONE);
            textOpenDetectionTime.setVisibility(View.GONE);
            textOpenDetectionInfo.setText("仅让已配对的蓝牙设备可见");
            userBluetoothList.setDividerHeight(0);
            List list = new ArrayList();
            list.add("");
            userBluetoothList.setAdapter(new UserBluetoothAdapter(context, list));
        }
    }

    // 注册蓝牙接收广播
    @Override
    protected void onStart() {
        if(!hasRegister){
            hasRegister = true;
            IntentFilter filterStart = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            IntentFilter filterEnd = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mDevice, filter);
            registerReceiver(mDevice, filterStart);
            registerReceiver(mDevice, filterEnd);
        }
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bluetooth_search:// 搜索蓝牙设备

                break;
            case R.id.bluetooth_set:// 蓝牙开关
                if (MineActivity.blueAdapter.isEnabled()) {
                    MineActivity.blueAdapter.cancelDiscovery();// 关闭蓝牙
                    MineActivity.blueAdapter.disable();
                    SharedPreferences.Editor et = sharedPreferences.edit();
                    imageBluetoothSet.setImageResource(R.mipmap.wt_person_close);
                    et.putBoolean(StringConstant.BLUETOOTH_SET, false);
                    et.commit();
                    openDetection.setClickable(false);
                    imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
                } else {
                    setBluetooth();// 打开蓝牙
                }
                break;
            case R.id.open_detection:// 开放检测
                SharedPreferences.Editor et = sharedPreferences.edit();
                if(openDetectionState){
                    imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
                    et.putBoolean(StringConstant.BLUETOOTH_OPEN_DETECTION_SET, false);
                    et.commit();
                } else {
                    Intent intentOpen = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);// 使蓝牙设备可见，方便配对
                    intentOpen.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 200);
                    startActivity(intentOpen);
                    imageOpenDetection.setImageResource(R.mipmap.wt_person_on);
                    et.putBoolean(StringConstant.BLUETOOTH_OPEN_DETECTION_SET, true);
                    et.commit();
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
        View dialog = LayoutInflater.from(this).inflate(R.layout.dialog_set_bluetooth_name, null);
        final EditText editNewName = (EditText) dialog.findViewById(R.id.edit_new_name);
        setBluetoothNameDialog = new Dialog(this, R.style.MyDialog);
        setBluetoothNameDialog.setContentView(dialog);
        setBluetoothNameDialog.setCanceledOnTouchOutside(false);
        setBluetoothNameDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        dialog.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newName = editNewName.getText().toString();
                if(!TextUtils.isEmpty(newName.trim())) {
                    MineActivity.blueAdapter.setName(newName);
                    textBluetoothName.setText(newName);
                    setBluetoothNameDialog.dismiss();
                } else {
                    Toast.makeText(BluetoothActivity.this, "设备名字不能为空，请重新输入!", Toast.LENGTH_SHORT).show();
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
    private void setBluetooth(){
        if(MineActivity.blueAdapter != null){  // 设备支持蓝牙
            // 确认开启蓝牙
            MineActivity.blueAdapter.enable(); // 直接开启，不经过提示
//                Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); // 请求用户开启
//                startActivityForResult(intent, RESULT_FIRST_USER);
            SharedPreferences.Editor et = sharedPreferences.edit();
            imageBluetoothSet.setImageResource(R.mipmap.wt_person_on);
            et.putBoolean(StringConstant.BLUETOOTH_SET, true);
            openDetection.setClickable(true);
            openDetectionState = sharedPreferences.getBoolean(StringConstant.BLUETOOTH_OPEN_DETECTION_SET, true);
            if(openDetectionState){
                imageOpenDetection.setImageResource(R.mipmap.wt_person_on);
                Intent intentOpen = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);// 使蓝牙设备可见，方便配对
                intentOpen.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 200);
                startActivity(intentOpen);
            } else {
                imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
            }
        } else{   // 设备不支持蓝牙
            Toast.makeText(BluetoothActivity.this, "设备不支持蓝牙!", Toast.LENGTH_SHORT).show();
        }
    }

    // 蓝牙搜索状态广播监听
    private class DeviceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){    // 搜索到新设备
                if(MineActivity.blueAdapter.getState() == BluetoothAdapter.STATE_OFF){
                    imageBluetoothSet.setImageResource(R.mipmap.wt_person_close);
                    imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
                    openDetection.setClickable(false);
                    textOpenDetection.setTextColor(getResources().getColor(R.color.textshang2));
                    openDetection.setBackgroundDrawable(null);
                    textUserDevice.setVisibility(View.GONE);
                    textPairDevice.setVisibility(View.GONE);
                    pairBluetoothList.setVisibility(View.GONE);
                    linearBluetoothSearch.setVisibility(View.GONE);
                    textOpenDetectionTime.setVisibility(View.GONE);
                    textOpenDetectionInfo.setText("仅让已配对的蓝牙设备可见");
                    userBluetoothList.setDividerHeight(0);
                    List list = new ArrayList();
                    list.add("");
                    userBluetoothList.setAdapter(new UserBluetoothAdapter(context, list));
                } else if(MineActivity.blueAdapter.getState() == BluetoothAdapter.STATE_ON){
                    imageBluetoothSet.setImageResource(R.mipmap.wt_person_on);
                    openDetection.setClickable(true);
                    openDetectionState = sharedPreferences.getBoolean(StringConstant.BLUETOOTH_OPEN_DETECTION_SET, true);// 开放蓝牙检测开关
                    if(openDetectionState){
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
                    textUserDevice.setVisibility(View.VISIBLE);
                    textPairDevice.setVisibility(View.VISIBLE);
                    pairBluetoothList.setVisibility(View.VISIBLE);
                    linearBluetoothSearch.setVisibility(View.VISIBLE);
                    userBluetoothList.setDividerHeight(1);
                    List list = new ArrayList();
                    list.add("小米");
                    userBluetoothList.setAdapter(new UserBluetoothAdapter(context, list));
                }
            }
//            if(BluetoothDevice.ACTION_FOUND.equals(action)){    // 搜索到新设备
//                BluetoothDevice btd=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                if (btd.getBondState() != BluetoothDevice.BOND_BONDED) {// 搜索没有配过对的蓝牙设备
//                    deviceList.add(btd.getName()+'\n'+btd.getAddress());
//                    adapter.notifyDataSetChanged();
//                }
//            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){// 搜索结束
//                if (deviceListview.getCount() == 0) {
//                    deviceList.add("No can be matched to use bluetooth");
//                    adapter.notifyDataSetChanged();
//                }
//                btserch.setText("repeat search");
//            }
        }
    }

    /**
     * 获取已经配对的蓝牙设备
     */
    private void findAvalibleDevice(){
        //获取可配对蓝牙设备
        Set<BluetoothDevice> device = MineActivity.blueAdapter.getBondedDevices();

//        if(MineActivity.blueAdapter != null && MineActivity.blueAdapter.isDiscovering()){
//            deviceList.clear();
//            adapter.notifyDataSetChanged();
//        }
//        if(device.size()>0){ //存在已经配对过的蓝牙设备
//            for(Iterator<BluetoothDevice> it = device.iterator(); it.hasNext();){
//                BluetoothDevice btd=it.next();
//                deviceList.add(btd.getName()+'\n'+btd.getAddress());
//                adapter.notifyDataSetChanged();
//            }
//        }else{  //不存在已经配对过的蓝牙设备
//            deviceList.add("No can be matched to use bluetooth");
//            adapter.notifyDataSetChanged();
//        }
    }

    @Override
    protected void onDestroy() {
        if(hasRegister){
            hasRegister = false;
            unregisterReceiver(mDevice);
        }
        super.onDestroy();
    }
}
