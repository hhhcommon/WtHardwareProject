package com.wotingfm.common.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.mediatek.engineermode.io.EmGpio;
import com.wotingfm.util.ToastUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;

import android_serialport_api.ComBean;
import android_serialport_api.SerialHelper;

/**
 * Created by Administrator on 2016/10/13 0013.
 */
public class SimulationService extends Service {
    final static int GPIO_DEVICE_OPEN = 12;
    final static int GPIO_AUDIO_EN = 58;
    final static int GPIO_PTT = 59;
    private SerialControl serialControl;
    private static SimulationService context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        initEmp();
    }

    // 初始化模拟对讲
    private void initEmp() {
        EmGpio.gpioInit();
        EmGpio.setGpioOutput(GPIO_AUDIO_EN);
        EmGpio.setGpioOutput(GPIO_DEVICE_OPEN);
        EmGpio.setGpioOutput(GPIO_PTT);

        serialControl = new SerialControl();
        serialControl.setPort("/dev/ttyMT2");
        serialControl.setBaudRate("9600");
//
//        // 声控命令
//        // 1-8：声控等级参数，等级越小越灵敏
//        // 0：关闭声控功能（不为零则表示开启）
//        serialControl.sendTxt("AT+DMOSETVOX=0\r\n");
//
//        // 音量命令
//        // 1-8：等级越高输出音量越大（最大320mv）
//        serialControl.sendTxt("AT+DMOSETVOLUME=5\r\n");
    }

    /**
     * 设置对讲频率
     * @param Frequ
     */
    public static void setFrequence(String Frequ) {
        if (!TextUtils.isEmpty(Frequ)) {
            String _deviceNeedFreq = Frequ.substring(5, Frequ.length()).trim();
            String deviceNeedFreq = _deviceNeedFreq.replaceAll(" ", "");
            //Log.e("要设置的Freq","a"+deviceNeedFreq+"b");
            context.serialControl.sendTxt("AT+DMOSETGROUP=1," + deviceNeedFreq + "," + deviceNeedFreq + ",00,4,1\r\n");
        } else {
            ToastUtils.show_always(context, "当前无对讲频率，赶紧设置一个开始聊天吧");
        }
    }

    /**
     * 对讲模块开关
     * true   打开硬件对讲模块
     * false  关闭硬件对讲模块
     *
     * @param type
     */
    public static void onOpenDevice(boolean type) {
        if (type) {
            OpenComPort(context.serialControl);
            openDevice();
        } else {
            CloseComPort(context.serialControl);
            closeDevice();
        }
    }

    /**
     * 通话
     */
    public static void talk() {
        EmGpio.setGpioDataHigh(GPIO_DEVICE_OPEN);
        EmGpio.setGpioDataLow(GPIO_AUDIO_EN);
        EmGpio.setGpioDataLow(GPIO_PTT);
    }

    /**
     * 打开串口
     *
     * @param ComPort
     */
    private static void OpenComPort(SerialHelper ComPort) {
        try {
            ComPort.open();
        } catch (SecurityException e) {
            Log.i("硬件反馈消息", "打开串口失败:没有串口读/写权限!");
        } catch (IOException e) {
            Log.i("硬件反馈消息", "打开串口失败:未知错误!");
        } catch (InvalidParameterException e) {
            Log.i("硬件反馈消息", "打开串口失败:参数错误!");
        }
    }

    /**
     * 关闭串口
     *
     * @param ComPort
     */
    private static void CloseComPort(SerialHelper ComPort) {
        if (ComPort != null) {
            ComPort.stopSend();
            ComPort.close();
        }
    }

    /**
     * 关机
     */
    private static void closeDevice() {
        EmGpio.setGpioDataLow(GPIO_DEVICE_OPEN);                   //
        EmGpio.setGpioDataLow(GPIO_AUDIO_EN);
        EmGpio.setGpioDataHigh(GPIO_PTT);
    }

    /**
     * 开机
     */
    public static void openDevice() {
        EmGpio.setGpioDataHigh(GPIO_DEVICE_OPEN);
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
        protected void onDataReceived(ComBean ComRecData)  {
            try {
                Log.e("硬件反馈消息", "============" + new String(ComRecData.bRec).getBytes("utf-8"));
                byte[] b = ComRecData.bRec;
                for (int i = 0; i < b.length; i++) {
                    Log.e("硬件反馈消息===","["+i+"]==="+ b[i]);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
