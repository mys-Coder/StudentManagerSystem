package com.example.studentmanagersystem;

// 课程图标：优先按课程名关键字匹配 emoji，未命中则按位置轮换书本图标
public class CourseIcons {

    private static final String[] FALLBACK = {"📘", "📗", "📙", "📓"};

    public static String iconFor(String name, int position) {
        if (name == null || name.isEmpty()) {
            return FALLBACK[Math.floorMod(position, FALLBACK.length)];
        }
        if (name.contains("网络") || name.contains("通信")) return "🌐";
        if (name.contains("组成") || name.contains("硬件") || name.contains("结构")) return "💾";
        if (name.contains("操作系统")) return "⚙️";
        if (name.contains("嵌入") || name.contains("移动") || name.contains("手机")
                || name.contains("安卓") || name.contains("Android")) return "📱";
        if (name.contains("数据")) return "🗄️";
        if (name.contains("数学")) return "📐";
        if (name.contains("英语") || name.contains("英文")) return "🔤";
        if (name.contains("安全")) return "🛡️";
        return FALLBACK[Math.floorMod(position, FALLBACK.length)];
    }
}
