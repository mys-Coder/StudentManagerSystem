package com.example.studentmanagersystem;

import java.security.MessageDigest;

// MD5 加密工具：用户密码以 MD5 摘要形式存库，不保存明文
public class Md5Util {

    // 计算字符串的 32 位小写 MD5
    public static String md5(String src) {
        if (src == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] data = md.digest(src.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : data) {
                String hex = Integer.toHexString(b & 0xff);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            return src; // Android 自带 MD5 算法，正常不会走到这里
        }
    }

    // 判断字符串是否已经是 32 位 MD5（CSV 导入时用，避免把已加密的密码再加密一次）
    public static boolean isMd5(String s) {
        return s != null && s.matches("(?i)^[0-9a-f]{32}$");
    }

    // 校验输入密码：库里可能是 MD5（新数据）或历史明文（旧数据），两者都兼容
    public static boolean check(String input, String stored) {
        if (input == null || stored == null) return false;
        return stored.equalsIgnoreCase(md5(input)) || stored.equals(input);
    }
}
