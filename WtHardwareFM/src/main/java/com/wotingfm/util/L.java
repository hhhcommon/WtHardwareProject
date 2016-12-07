package com.wotingfm.util;

import android.util.Log;

/**
 * 日志工具类
 */
public class L {
    private static final String TAG = "main";       // 标签
    private static String className;                // Log 输出所在类
    private static String methodName;               // Log 输出所在方法
    private static int lineNumber;                  // Log 输出所行号
    private static boolean IS_DEBUG = true;         // Debug 模式

    // 获取输出所在位置的信息 className methodName lineNumber
    private static void getDetail(StackTraceElement[] elements) {
        className = elements[1].getFileName().split("\\.")[0];
        methodName = elements[1].getMethodName();
        lineNumber = elements[1].getLineNumber();
    }

    // 创建 Log 输出的基本信息
    private static String createLog(String log) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        buffer.append(className);
        buffer.append(".java ");
        buffer.append(methodName);
        buffer.append("()");
        buffer.append(" line:");
        buffer.append(lineNumber);
        buffer.append("] ");
        buffer.append(log);
        return buffer.toString();
    }

    public static void v(String message) {
        if (IS_DEBUG) {
            getDetail(new Throwable().getStackTrace());
            Log.v(TAG, createLog(message));
        }
    }

    public static void i(String message) {
        if (IS_DEBUG) {
            getDetail(new Throwable().getStackTrace());
            Log.i(TAG, createLog(message));
        }
    }

    public static void d(String message) {
        if (IS_DEBUG) {
            getDetail(new Throwable().getStackTrace());
            Log.d(TAG, createLog(message));
        }
    }

    public static void w(String message) {
        if (IS_DEBUG) {
            getDetail(new Throwable().getStackTrace());
            Log.w(TAG, createLog(message));
        }
    }

    public static void e(String message) {
        if (IS_DEBUG) {
            getDetail(new Throwable().getStackTrace());
            Log.e(TAG, createLog(message));
        }
    }

    // 自定义 TAG
    public static void v(String tag, String message) {
        if (IS_DEBUG) {
            getDetail(new Throwable().getStackTrace());
            Log.v(tag, createLog(message));
        }
    }

    public static void i(String tag, String message) {
        if (IS_DEBUG) {
            getDetail(new Throwable().getStackTrace());
            Log.i(tag, createLog(message));
        }
    }

    public static void d(String tag, String message) {
        if (IS_DEBUG) {
            getDetail(new Throwable().getStackTrace());
            Log.d(tag, createLog(message));
        }
    }

    public static void w(String tag, String message) {
        if (IS_DEBUG) {
            getDetail(new Throwable().getStackTrace());
            Log.w(tag, createLog(message));
        }
    }

    public static void e(String tag, String message) {
        if (IS_DEBUG) {
            getDetail(new Throwable().getStackTrace());
            Log.e(tag, createLog(message));
        }
    }
}
