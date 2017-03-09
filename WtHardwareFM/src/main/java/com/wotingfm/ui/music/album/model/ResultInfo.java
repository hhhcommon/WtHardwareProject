package com.wotingfm.ui.music.album.model;

import java.util.List;

/**
 * 专辑信息
 * Created by Administrator on 2017/1/11.
 */
public class ResultInfo {

    private String ContentName;// 专辑名

    private long ContentPubTime;

    private String ContentDescn;// 专辑介绍

    private String ContentPub;// 来源

    private String PlayCount;// 播放次数

    private String MediaType;// 媒体类型

    private String PageSize;// 数量

    private String ContentSubCount;

    private String ContentId;// ID

    private String ContentFavorite;// 是否喜欢  == "1" 喜欢   == "0" 没有喜欢

    private String ContentSubscribe;// 是否订阅  == "1" 订阅  == "0" 没有订阅

    private List<ContentInfo> SubList;// 专辑中的列表信息

    private String ContentURI;// ContentURI

    private List<ContentCatalogs> ContentCatalogs;

    private long CTime;

    private String ContentKeyWord;

    private int Page;

    private String ContentSubjectWord;

    private String ContentStatus;

    private String ContentShareURL;// 分享 URL

    private String ContentImg;// 专辑封面图片

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

    public String getContentName() {
        return ContentName;
    }

    public void setContentName(String contentName) {
        ContentName = contentName;
    }

    public long getContentPubTime() {
        return ContentPubTime;
    }

    public void setContentPubTime(long contentPubTime) {
        ContentPubTime = contentPubTime;
    }

    public String getContentDescn() {
        return ContentDescn;
    }

    public void setContentDescn(String contentDescn) {
        ContentDescn = contentDescn;
    }

    public String getContentURI() {
        return ContentURI;
    }

    public void setContentURI(String contentURI) {
        ContentURI = contentURI;
    }

    public List<com.wotingfm.ui.music.album.model.ContentCatalogs> getContentCatalogs() {
        return ContentCatalogs;
    }

    public void setContentCatalogs(List<com.wotingfm.ui.music.album.model.ContentCatalogs> contentCatalogs) {
        ContentCatalogs = contentCatalogs;
    }

    public long getCTime() {
        return CTime;
    }

    public void setCTime(long CTime) {
        this.CTime = CTime;
    }

    public String getContentKeyWord() {
        return ContentKeyWord;
    }

    public void setContentKeyWord(String contentKeyWord) {
        ContentKeyWord = contentKeyWord;
    }

    public int getPage() {
        return Page;
    }

    public void setPage(int page) {
        Page = page;
    }

    public String getContentSubjectWord() {
        return ContentSubjectWord;
    }

    public void setContentSubjectWord(String contentSubjectWord) {
        ContentSubjectWord = contentSubjectWord;
    }

    public String getContentStatus() {
        return ContentStatus;
    }

    public void setContentStatus(String contentStatus) {
        ContentStatus = contentStatus;
    }

    public String getContentShareURL() {
        return ContentShareURL;
    }

    public void setContentShareURL(String contentShareURL) {
        ContentShareURL = contentShareURL;
    }

    public String getContentImg() {
        return ContentImg;
    }

    public void setContentImg(String contentImg) {
        ContentImg = contentImg;
    }

    public String getContentSubscribe() {
        return ContentSubscribe;
    }

    public void setContentSubscribe(String contentSubscribe) {
        ContentSubscribe = contentSubscribe;
    }

    @Override
    public String toString() {
        return "ResultInfo{" +
                "ContentName='" + ContentName + '\'' +
                ", ContentPubTime=" + ContentPubTime +
                ", ContentDescn='" + ContentDescn + '\'' +
                ", ContentPub='" + ContentPub + '\'' +
                ", PlayCount='" + PlayCount + '\'' +
                ", MediaType='" + MediaType + '\'' +
                ", PageSize='" + PageSize + '\'' +
                ", ContentSubCount='" + ContentSubCount + '\'' +
                ", ContentId='" + ContentId + '\'' +
                ", ContentFavorite='" + ContentFavorite + '\'' +
                ", SubList=" + SubList +
                ", ContentURI='" + ContentURI + '\'' +
                ", ContentCatalogs=" + ContentCatalogs +
                ", CTime=" + CTime +
                ", ContentKeyWord='" + ContentKeyWord + '\'' +
                ", Page=" + Page +
                ", ContentSubjectWord='" + ContentSubjectWord + '\'' +
                ", ContentStatus='" + ContentStatus + '\'' +
                ", ContentShareURL='" + ContentShareURL + '\'' +
                ", ContentImg='" + ContentImg + '\'' +
                '}';
    }
}
