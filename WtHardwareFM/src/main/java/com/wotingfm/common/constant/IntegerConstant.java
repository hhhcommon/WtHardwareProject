package com.wotingfm.common.constant;

/**
 * 保存Int数据类型常量
 * Created by Administrator on 2016/8/24 0024.
 */
public class IntegerConstant {

    // 标记  创建公开群
    public static final int CREATE_GROUP_PUBLIC = 1;

    // 标记  创建密码群
    public static final int CREATE_GROUP_PRIVATE = 2;

    // 标记  创建验证群
    public static final int CREATE_GROUP_VERIFICATION = 0;

    // 打开系统图库
    public static final int TO_GALLERY = 0x00D;

    // 打开系统照相机
    public static final int TO_CAMARA = 0x00E;

    // 跳转至图片剪裁页面
    public static final int PHOTO_REQUEST_CUT = 0x00F;

    /**
     * PlayerFragment 相关
     */
    // 更新列表
    public static final int PLAY_UPDATE_LIST = 1001;

    // 更新列表界面
    public static final int PLAY_UPDATE_LIST_VIEW = 1002;

    // 默认图片
    public static final int TYPE_DEFAULT = 0x000;

    // 列表默认图片
    public static final int TYPE_LIST = 0x001;

    // 群组默认头像
    public static final int TYPE_GROUP = 0x002;

    // 用户默认头像
    public static final int TYPE_MINE = 0x003;

    // 好友默认头像
    public static final int TYPE_PERSON = 0x004;

    // 数据收集类型 播放事件
    public static final int DATA_TYPE_PLAY = 0x006;

    // 数据收集类型 打开界面
    public static final int DATA_TYPE_OPEN = 0x007;

    // 数据上传方式 即时上传
    public static final int DATA_UPLOAD_TYPE_IMM = 0;

    // 数据上传方式 定制时间上传
    public static final int DATA_UPLOAD_TYPE_GIVEN = -1;

    public static final int DATA_UPLOAD_COUNT = 10;
}
