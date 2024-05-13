package com.example.cute_pet.util;

public class NicknameValidator {
    public static boolean validateNickname(String nickname) {
        // 计算中文字符的数量
        int chineseCount = 0;
        for (int i = 0; i < nickname.length(); i++) {
            if (isChinese(nickname.charAt(i))) {
                chineseCount++;
            }
        }


        // 计算英文字母和其他字符的数量
        int otherCount = nickname.length() - chineseCount;

        // 计算总字节长度
        int byteLength = chineseCount * 2 + otherCount;


        // 判断是否符合要求
        if ((chineseCount <= 6 && otherCount == 0) || (byteLength <= 12)) {
            return true;
        } else {
            return false;
        }
    }

    public static String validateNicknameResult(String nickname) {
        // 计算中文字符的数量
        int chineseCount = 0;
        for (int i = 0; i < nickname.length(); i++) {
            if (isChinese(nickname.charAt(i))) {
                chineseCount++;
            }
        }


        // 计算英文字母和其他字符的数量
        int otherCount = nickname.length() - chineseCount;

        // 计算总字节长度
        int byteLength = chineseCount * 2 + otherCount;
        // 判断是否符合要求
        if (chineseCount >= 6){
            return "中文长度最大6";
        }
        return "昵称长度最大12";
    }
    // 判断字符是否为中文
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A;
    }
}