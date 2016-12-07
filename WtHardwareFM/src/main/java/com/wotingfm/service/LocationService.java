package com.wotingfm.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.amap.api.location.AMapLocation;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.location.GDLocation;

import java.util.Locale;

/**
 * Created by Administrator on 2016/10/13 0013.
 */
public class LocationService extends Service implements GDLocation.Location {

    private GDLocation mGDLocation;

    @Override
    public void onCreate() {
        super.onCreate();
        mGDLocation= GDLocation.getInstance(getApplicationContext(),this);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String p = pref.getString("set_locale", "");
        if (p != null && !p.equals("")) {
            Locale locale;
            if(p.startsWith("zh")) {
                locale = Locale.CHINA;
            } else {
                locale = new Locale(p);
            }
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
        }
        mGDLocation.startLocation();


    }
    //设置定位 并给整个程序提供数据支持
    @Override
    public void locationSuccess(AMapLocation amapLocation) {
        String City=amapLocation.getCity();
        String Address=amapLocation.getAddress();
        String AdCode=amapLocation.getAdCode();//地区编码
        String Latitude=String.valueOf(amapLocation.getLatitude());
        String Longitude=String.valueOf(amapLocation.getLongitude());
        if(GlobalConfig.latitude==null){
            GlobalConfig.latitude = Latitude;
        }else{
            if(!GlobalConfig.latitude.equals(Latitude)){
                GlobalConfig.latitude = Latitude;
            }
        }
        if(GlobalConfig.longitude==null){
            GlobalConfig.longitude = Longitude;
        }else{
            if(!GlobalConfig.longitude.equals(Latitude)){
                GlobalConfig.longitude = Latitude;
            }
        }

        if(GlobalConfig.CityName==null){
            GlobalConfig.CityName=City;
        }else{
            if(!GlobalConfig.CityName.equals(City)){
                GlobalConfig.CityName=City;
                //此时应该调用重新适配CityName方法
            }
        }
        SharedPreferences sp = getSharedPreferences("wotingfm", Context.MODE_PRIVATE);
        SharedPreferences.Editor et = sp.edit();
        et.putString(StringConstant.CITYNAME, City);
        et.putString(StringConstant.CITYID, GlobalConfig.AdCode);
        et.putString(StringConstant.LATITUDE,String.valueOf(Latitude));
        et.putString(StringConstant.LONGITUDE,String.valueOf(Longitude));
        et.commit();
    }

    @Override
    public void locationFail(AMapLocation amapLocation) {

    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
