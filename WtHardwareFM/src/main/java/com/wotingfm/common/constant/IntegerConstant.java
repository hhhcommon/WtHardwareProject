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
}
