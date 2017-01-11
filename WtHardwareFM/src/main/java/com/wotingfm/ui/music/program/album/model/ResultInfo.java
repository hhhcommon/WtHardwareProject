package com.wotingfm.ui.music.program.album.model;

import java.util.List;

/**
 * 专辑信息
 * Created by Administrator on 2017/1/11.
 */
public class ResultInfo {

    private String ContentPub;// 来源

    private String PlayCount;// 播放次数

    private String MediaType;// 媒体类型

    private String PageSize;// 数量

    private String ContentSubCount;

    private String ContentId;// ID

    private String ContentFavorite;// 是否喜欢  == "1" 喜欢   =="0" 没有喜欢

    private List<ContentInfo> SubList;// 专辑中的列表信息

    public String getContentPub() {
        return ContentPub;
    }

    public void setContentPub(String contentPub) {
        ContentPub = contentPub;
    }

    public String getPlayCount() {
        return PlayCount;
    }

    public void setPlayCount(String playCount) {
        PlayCount = playCount;
    }

    public String getMediaType() {
        return MediaType;
    }

    public void setMediaType(String mediaType) {
        MediaType = mediaType;
    }

    public String getPageSize() {
        return PageSize;
    }

    public void setPageSize(String pageSize) {
        PageSize = pageSize;
    }

    public String getContentSubCount() {
        return ContentSubCount;
    }

    public void setContentSubCount(String contentSubCount) {
        ContentSubCount = contentSubCount;
    }

    public String getContentId() {
        return ContentId;
    }

    public void setContentId(String contentId) {
        ContentId = contentId;
    }

    public String getContentFavorite() {
        return ContentFavorite;
    }

    public void setContentFavorite(String contentFavorite) {
        ContentFavorite = contentFavorite;
    }

    public List<ContentInfo> getSubList() {
        return SubList;
    }

    public void setSubList(List<ContentInfo> subList) {
        SubList = subList;
    }

    @Override
    public String toString() {
        return "ResultInfo{" +
                "ContentPub='" + ContentPub + '\'' +
                ", PlayCount='" + PlayCount + '\'' +
                ", MediaType='" + MediaType + '\'' +
                ", PageSize='" + PageSize + '\'' +
                ", ContentSubCount='" + ContentSubCount + '\'' +
                ", ContentId='" + ContentId + '\'' +
                ", ContentFavorite='" + ContentFavorite + '\'' +
                ", SubList=" + SubList +
                '}';
    }
}
