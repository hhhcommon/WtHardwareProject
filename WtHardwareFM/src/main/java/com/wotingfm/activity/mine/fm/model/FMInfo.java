package com.wotingfm.activity.mine.fm.model;

/**
 * FM
 * Created by Administrator on 9/10/2016.
 */
public class FMInfo {
    private String fmName;

    private String fmIntroduce;

    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFmName() {
        return fmName;
    }

    public void setFmName(String fmName) {
        this.fmName = fmName;
    }

    public String getFmIntroduce() {
        return fmIntroduce;
    }

    public void setFmIntroduce(String fmIntroduce) {
        this.fmIntroduce = fmIntroduce;
    }
}
