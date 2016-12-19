package com.wotingfm.activity.im.interphone.creategroup.model;

import java.io.Serializable;

/**
 * 创建群组服务器返回信息
 */
public class GroupRation implements Serializable {
    private static final long serialVersionUID = -2444225043712411920L;
    private String GroupId;
    private String GroupName;
    private String GroupCount;
    private String GroupImg;
    private String GroupNum;
    private String GroupDescn;
    private String CreateTime;
    private String InnerPhoneNum;
    private String GroupType;
    private String GroupSignature;
    private String GroupCreator;
    private String GroupManager;
    private String GroupMyAlias;

    public String getGroupMyAlias() {
        return GroupMyAlias;
    }

    public void setGroupMyAlias(String groupMyAlias) {
        GroupMyAlias = groupMyAlias;
    }

    public String getGroupCreator() {
        return GroupCreator;
    }

    public void setGroupCreator(String groupCreator) {
        GroupCreator = groupCreator;
    }

    public String getGroupManager() {
        return GroupManager;
    }

    public void setGroupManager(String groupManager) {
        GroupManager = groupManager;
    }

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
