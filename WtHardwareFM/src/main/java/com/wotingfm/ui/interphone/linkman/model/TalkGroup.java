package com.wotingfm.ui.interphone.linkman.model;


import com.wotingfm.ui.common.model.GroupInfo;

import java.io.Serializable;
import java.util.List;

public class TalkGroup implements Serializable{
	private String Type;
	private String PageSize;
	private String AllSize;
	private String Name;
	private List<GroupInfo> Groups;
	
	public String getType() {
		return Type;
	}
	public void setType(String type) {
		Type = type;
	}
	public String getPageSize() {
		return PageSize;
	}
	public void setPageSize(String pageSize) {
		PageSize = pageSize;
	}
	public String getAllSize() {
		return AllSize;
	}
	public void setAllSize(String allSize) {
		AllSize = allSize;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public List<GroupInfo> getGroups() {
		return Groups;
	}
	public void setGroups(List<GroupInfo> groups) {
		Groups = groups;
	}
}
