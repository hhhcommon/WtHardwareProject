package com.wotingfm.activity.im.interphone.groupmanage.model;

import java.io.Serializable;

/**
 * 用户信息实体类
 */
public class UserInfo implements Serializable {
	private String name;   //显示的数据
	private String sortLetters;  //显示数据拼音的首字母
	private String InnerPhoneNum;
	private String UserName;
	private String UserId;
    private int Type=1;//标记是否为最后一项的，新加入尾部的数据会设置type为2，在adapter里针对此属性设置对应的gridview
    private int CheckType=1;//标记item是否被选中，1为未选中，2为选中
	private String RealName;//实名
	private String UserNum;//用户码
	private String PhoneNum;//用户主手机号
	private String Email;//
	private String Descn;//
	private String PortraitBig;//
	private String PortraitMini;//
	private String truename;
	private String pinYinName;
	private String UserAliasName;
	private String InvitedUserName;
	private String InviteUserId;
	private String BeInviteUserId;
    private String InviteCount;
	private String GroupCreator;
	private String GroupImg;
	private String GroupName;
	private String GroupId;
	private String GroupNum;
	private String InviteTime;
	private String GroupMyAlias;
	private String LoginName;
	private String GroupSignature;
	private String GroupType;
	private String GroupCount;
	private String GroupManager;

	public void setGroupCount(String groupCount) {
		GroupCount = groupCount;
	}

	public void setGroupManager(String groupManager) {
		GroupManager = groupManager;
	}

	public void setGroupSignature(String groupSignature) {
		GroupSignature = groupSignature;
	}

	public String getGroupCount() {
		return GroupCount;
	}

	public String getGroupManager() {
		return GroupManager;
	}

	public String getGroupSignature() {
		return GroupSignature;
	}

	public String getLoginName() {
		return LoginName;
	}

	public void setLoginName(String loginName) {
		LoginName = loginName;
	}

	public String getGroupMyAlias() {
		return GroupMyAlias;
	}

	public void setGroupMyAlias(String groupMyAlias) {
		GroupMyAlias = groupMyAlias;
	}

	public String getInnerPhoneNum() {
		return InnerPhoneNum;
	}

	public void setInnerPhoneNum(String innerPhoneNum) {
		InnerPhoneNum = innerPhoneNum;
	}

	public String getUserName() {
		return UserName;
	}

	public void setUserName(String userName) {
		UserName = userName;
	}

	public String getUserId() {
		return UserId;
	}

	public void setUserId(String userId) {
		UserId = userId;
	}

	public String getEmail() {
		return Email;
	}

	public void setEmail(String email) {
		Email = email;
	}


	public String getPhoneNum() {
		return PhoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		PhoneNum = phoneNum;
	}
	public String getGroupName() {
		return GroupName;
	}

	public void setGroupName(String groupName) {
		GroupName = groupName;
	}

	public String getGroupCreator() {
		return GroupCreator;
	}

	public void setGroupCreator(String groupCreator) {
		GroupCreator = groupCreator;
	}

	public String getGroupId() {
		return GroupId;
	}

	public void setGroupId(String groupId) {
		GroupId = groupId;
	}

	public String getGroupType() {
		return GroupType;
	}

	public void setGroupType(String groupType) {
		GroupType = groupType;
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

	public String getInviteCount() {
		return InviteCount;
	}

	public void setInviteCount(String inviteCount) {
		InviteCount = inviteCount;
	}

	public String getInviteTime() {
		return InviteTime;
	}

	public void setInviteTime(String inviteTime) {
		InviteTime = inviteTime;
	}

	public String getBeInviteUserId() {
		return BeInviteUserId;
	}

	public void setBeInviteUserId(String beInviteUserId) {
		BeInviteUserId = beInviteUserId;
	}

	public String getInviteUserId() {
		return InviteUserId;
	}

	public void setInviteUserId(String inviteUserId) {
		InviteUserId = inviteUserId;
	}

	public String getInvitedUserName() {
		return InvitedUserName;
	}

	public void setInvitedUserName(String invitedUserName) {
		InvitedUserName = invitedUserName;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSortLetters() {
		return sortLetters;
	}
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}
	public String getUserAliasName() {
		return UserAliasName;
	}
	public void setUserAliasName(String userAliasName) {
		UserAliasName = userAliasName;
	}
	public String getTruename() {
		return truename;
	}
	public void setTruename(String truename) {
		this.truename = truename;
	}
	public String getPinYinName() {
		return pinYinName;
	}
	public void setPinYinName(String pinYinName) {
		this.pinYinName = pinYinName;
	}
	public String getRealName() {
		return RealName;
	}
	public void setRealName(String realName) {
		RealName = realName;
	}
	public String getUserNum() {
		return UserNum;
	}
	public void setUserNum(String userNum) {
		UserNum = userNum;
	}
	public String getDescn() {
		return Descn;
	}
	public void setDescn(String descn) {
		Descn = descn;
	}
	public String getPortraitBig() {
		return PortraitBig;
	}
	public void setPortraitBig(String portraitBig) {
		PortraitBig = portraitBig;
	}
	public String getPortraitMini() {
		return PortraitMini;
	}
	public void setPortraitMini(String portraitMini) {
		PortraitMini = portraitMini;
	}
	public int getCheckType() {
		return CheckType;
	}
	public void setCheckType(int checkType) {
		CheckType = checkType;
	}
	public int getType() {
		return Type;
	}
	public void setType(int type) {
		Type = type;
	}
}
