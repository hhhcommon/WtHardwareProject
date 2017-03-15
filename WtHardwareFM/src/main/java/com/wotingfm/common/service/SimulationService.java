package com.wotingfm.common.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.mediatek.engineermode.io.EmGpio;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.constant.FrequenceConstant;
import com.wotingfm.util.ToastUtils;

import java.io.IOException;
import java.security.InvalidParameterException;

import android_serialport_api.ComBean;
import android_serialport_api.SerialHelper;

/**
 * Created by Administrator on 2016/10/13 0013.
 */
public class SimulationService extends Service  {
    final static int GPIO_DEVICE_OPEN = 12;
    final static int GPIO_AUDIO_EN = 58;
    final static int GPIO_PTT = 59;
    final String STATE_OPEN = "开机";
    final String STATE_CLOSE = "关机";
    private static SerialControl serialControl;
    private static String frequence;
    private static SimulationService context;
    private static boolean IsComPort;
    private static boolean CanMONI;

    @Override
    public void onCreate() {
        super.onCreate();
        context=this;
        initEmp();
    }

    // 初始化模拟对讲
    private void initEmp() {
        frequence = BSApplication.SharedPreferences.getString(FrequenceConstant.FREQUENCE, "");
        EmGpio.gpioInit();
        EmGpio.setGpioOutput(GPIO_AUDIO_EN);
        EmGpio.setGpioOutput(GPIO_DEVICE_OPEN);
        EmGpio.setGpioOutput(GPIO_PTT);
        serialControl = new SerialControl();
        serialControl.setPort("/dev/ttyMT2");
        serialControl.setBaudRate("9600");
        OpenComPort(serialControl);// 打开串口
        if(frequence!=null){
            String deviceNeedFreq=frequence.substring(frequence.indexOf("-")+1,frequence.length());
            String s="AT+DMOSETGROUP=1,"+deviceNeedFreq+","+deviceNeedFreq+",00,3,1\r\n";
            serialControl.sendTxt("AT+DMOSETGROUP=1,"+deviceNeedFreq+","+deviceNeedFreq+",00,3,1\r\n");
            Log.e("串口数据",s);
            //serialControl.sendTxt(s);
        }
        // 声控命令
        // 1-8：声控等级参数，等级越小越灵敏
        // 0：关闭声控功能（不为零则表示开启）
        serialControl.sendTxt("AT+DMOSETVOX=0\r\n");

        // 音量命令
        // 1-8：等级越高输出音量越大（最大320mv）
        serialControl.sendTxt("AT+DMOSETVOLUME=5\r\n");
    }

    public static void setFrequence(String Frequ){
        if(!TextUtils.isEmpty(Frequ)){
      /*      ToastUtils.show_always(context,"对讲频率已经变更到"+Frequ);*/
            String deviceNeedFreq=Frequ.substring(5,Frequ.length()).trim();
      /*      ToastUtils.show_always(context,deviceNeedFreq);*/
              Log.e("要设置的Freq","a"+deviceNeedFreq+"b");
            serialControl.sendTxt("AT+DMOSETGROUP=1,"+deviceNeedFreq+","+deviceNeedFreq+",00,3,1\n");
        }
    }

    public static void setFrequenceFromOut(String Frequ){
        if(!TextUtils.isEmpty(Frequ)){
        /*    ToastUtils.show_always(context,"对讲频率已经变更到"+Frequ);
            String deviceNeedFreq=frequence.substring(frequence.indexOf("-")+1,frequence.length());*/
            serialControl.sendTxt("AT+DMOSETGROUP=1,"+Frequ+","+Frequ+",00,3,1\r\n");
        }else{
            ToastUtils.show_always(context,"传入的频率值不合法");
        }
    }
   /* //
    public static boolean initDuijiang(String Frequ){
        if(IsComPort){
            if(!TextUtils.isEmpty(Frequ)){
            // 声控命令
            // 1-8：声控等级参数，等级越小越灵敏
            // 0：关闭声控功能（不为零则表示开启）
            serialControl.sendTxt("AT+DMOSETVOX=0\r\n");

            // 音量命令
            // 1-8：等级越高输出音量越大（最大320mv）
            serialControl.sendTxt("AT+DMOSETVOLUME=5\r\n");

            // 设置频率 // 此处要对数据库进行拼接

                String deviceNeedFreq=Frequ.substring(Frequ.indexOf("-")+1,Frequ.length());
                serialControl.sendTxt("AT+DMOSETGROUP=1,"+deviceNeedFreq+","+deviceNeedFreq+",4,1\n");
                CanMONI=true;

            }else{
                // 频率列表为空
                CanMONI=false;

            }
        }else{
            // ToastUtils.show_always(context,"串口通信失败");
            CanMONI=false;
        }
          return  CanMONI;
    }*/

    //----------------------------------------------------打开串口
    private void OpenComPort(SerialHelper ComPort) {

        try {
            ComPort.open();
            IsComPort=true;
        } catch (SecurityException e) {
            //ShowMessage("打开串口失败:没有串口读/写权限!");
            Log.i("7889", "打开串口失败:没有串口读/写权限!");
            IsComPort=false;
        } catch (IOException e) {
            //ShowMessage("打开串口失败:未知错误!");
            Log.i("7889", "打开串口失败:未知错误!");
            IsComPort=false;
        } catch (InvalidParameterException e) {
            //ShowMessage("打开串口失败:参数错误!");
            Log.i("7889", "打开串口失败:参数错误!");
            IsComPort=false;
        }
    }

    //----------------------------------------------------关闭串口
    private static void CloseComPort(SerialHelper ComPort) {
        if (ComPort != null) {
            ComPort.stopSend();
            ComPort.close();
        }
    }

    //----------------------------------------------------对话
    public static void talk() {
        EmGpio.setGpioDataHigh(GPIO_DEVICE_OPEN);
        EmGpio.setGpioDataLow(GPIO_AUDIO_EN);
        EmGpio.setGpioDataLow(GPIO_PTT);
    }

    //====================================================关闭
    public static void closeDevice() {
        EmGpio.setGpioDataLow(GPIO_DEVICE_OPEN);                   // 关机
        EmGpio.setGpioDataLow(GPIO_AUDIO_EN);
        EmGpio.setGpioDataHigh(GPIO_PTT);
    }

    //=====================================================开机
    public static void openDevice() {
        EmGpio.setGpioDataHigh(GPIO_DEVICE_OPEN);                  // 开机
        EmGpio.setGpioDataHigh(GPIO_AUDIO_EN);
        EmGpio.setGpioDataHigh(GPIO_PTT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeDevice();
        CloseComPort(serialControl);
    }

    private class SerialControl extends SerialHelper {

        public SerialControl() {
        }

        @Override
        protected void onDataReceived(ComBean ComRecData) {
            Log.e("0000===7889", "=======123============" + new String(ComRecData.bRec));
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
