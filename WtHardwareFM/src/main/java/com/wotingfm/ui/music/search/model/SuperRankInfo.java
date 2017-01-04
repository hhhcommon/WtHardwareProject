package com.wotingfm.ui.music.search.model;

import com.wotingfm.ui.music.player.model.PlayerHistory;
import com.wotingfm.ui.music.program.fmlist.model.RankInfo;

import java.util.List;


public class SuperRankInfo {
	private String Key;
	private List<RankInfo> list;
	private List<PlayerHistory> historyList;
	
	public List<PlayerHistory> getHistoryList() {
		return historyList;
	}
	public void setHistoryList(List<PlayerHistory> historyList) {
		this.historyList = historyList;
	}
	public String getKey() {
		return Key;
	}
	public void setKey(String key) {
		Key = key;
	}
	public List<RankInfo> getList() {
		return list;
	}
	public void setList(List<RankInfo> list) {
		this.list = list;
	}
}
