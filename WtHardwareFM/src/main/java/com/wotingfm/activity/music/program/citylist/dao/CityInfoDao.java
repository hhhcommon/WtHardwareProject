package com.wotingfm.activity.music.program.citylist.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wotingfm.activity.music.program.fenlei.model.fenLeiName;
import com.wotingfm.helper.SqliteHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 城市库表
 * 作者：xinlong on 2016/8/29 11:30
 * 邮箱：645700751@qq.com
 */
public class CityInfoDao {
    private SqliteHelper helper;
    private Context context;

    public CityInfoDao(Context context) {
        helper = new SqliteHelper(context);
        this.context = context;
    }

    //查
    public List<fenLeiName> queryCityInfo() {
        List<fenLeiName> myList = new ArrayList<fenLeiName>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = null;
    /*	String url = cursor.getString(cursor.getColumnIndex("url"));
		String author = cursor.getColumnName(cursor.getColumnIndex("author"));*/

        try {
            cursor = db.rawQuery("Select * from cityinfo", new String[]{});
            while (cursor.moveToNext()) {
                String Adcode = cursor.getString(cursor.getColumnIndex("adcode"));
                String CityName = cursor.getString(cursor.getColumnIndex("cityname"));
                fenLeiName mFenLeiName = new fenLeiName();
                mFenLeiName.setCatalogId(Adcode);
                mFenLeiName.setCatalogName(CityName);
                myList.add(mFenLeiName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return myList;
    }

    //增
    public void InsertCityInfo(List<fenLeiName> list) {
        SQLiteDatabase db = helper.getWritableDatabase();
        for (int i = 0; i < list.size(); i++) {
            String adcode = list.get(i).getCatalogId();
            String cityName = list.get(i).getCatalogName();
            db.execSQL("insert into cityinfo(adcode,cityname)values(?,?)", new Object[]{adcode, cityName});
        }
        if (db != null) {
            db.close();
        }
    }

    //
    public void DelCityInfo() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("delete  from cityinfo");
        db.close();
    }
}
