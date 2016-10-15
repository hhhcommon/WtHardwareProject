package com.wotingfm.activity.im.interphone.creategroup.model;

import java.io.Serializable;

/**
 * 创建群组服务器返回信息
 */
public class GroupRation implements Serializable {
    private static final long serialVersionUID = -2444225043712411920L;
    public String GroupId;
    public String GroupName;
    public String GroupCount;
    public String GroupImg;
    public String GroupNum;
    public String GroupDescn;
    public String CreateTime;
    public String InnerPhoneNum;
    public String GroupType;
    public String GroupSignature;

    private String alternateChannel1;// 备用频道 1
    private String alternateChannel2;// 备用频道 2

    public String getAlternateChannel1() {
        return alternateChannel1;
    }

    public void setAlternateChannel1(String alternateChannel1) {
        this.alternateChannel1 = alternateChannel1;
    }

    public String getAlternateChannel2() {
        return alternateChannel2;
    }

    public void setAlternateChannel2(String alternateChannel2) {
        this.alternateChannel2 = alternateChannel2;
    }

    public String getGroupSignature() {
        return GroupSignature;
    }

    public void setGroupSignature(String groupSignature) {
        GroupSignature = groupSignature;
    }

    public String getGroupType() {
        return GroupType;
    }

    public void setGroupType(String groupType) {
        GroupType = groupType;
    }

    public String getGroupDescn() {
        return GroupDescn;
    }

    public void setGroupDescn(String groupDescn) {
        GroupDescn = groupDescn;
    }

    public String getInnerPhoneNum() {
        return InnerPhoneNum;
    }

    public void setInnerPhoneNum(String innerPhoneNum) {
        InnerPhoneNum = innerPhoneNum;
    }

    public String getGroupId() {
        return GroupId;
    }

    public void setGroupId(String groupId) {
        GroupId = groupId;
    }

    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String groupName) {
        GroupName = groupName;
    }

    public String getGroupCount() {
        return GroupCount;
    }

    public void setGroupCount(String groupCount) {
        GroupCount = groupCount;
    }

    public String getGroupImg() {
        return GroupImg;
    }

    public void setGroupImg(String groupImg) {
        GroupImg = groupImg;
    }

    public String getGroupNum() {
        return GroupNum;
    }

    public void setGroupNum(String groupNum) {
        GroupNum = groupNum;
    }

    public String getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(String createTime) {
        CreateTime = createTime;
    }
}
