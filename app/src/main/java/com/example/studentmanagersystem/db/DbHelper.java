package com.example.studentmanagersystem.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.studentmanagersystem.Md5Util;

// 数据库帮助类：负责建表、升级、写入初始测试数据
public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "student_mis.db"; // 数据库文件名
    private static final int DB_VERSION = 1;                // 改了表结构就把它加1

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 老师表
        db.execSQL("CREATE TABLE teacher(" +
                "tno TEXT PRIMARY KEY," +
                "tname TEXT NOT NULL," +
                "tsex TEXT," +
                "title TEXT," +
                "pwd TEXT NOT NULL," +
                "phone TEXT)");

        // 学生表
        db.execSQL("CREATE TABLE student(" +
                "sno TEXT PRIMARY KEY," +
                "sname TEXT NOT NULL," +
                "ssex TEXT," +
                "sage INTEGER," +
                "sdept TEXT," +
                "pwd TEXT NOT NULL," +
                "phone TEXT)");

        // 课程表
        db.execSQL("CREATE TABLE course(" +
                "cno TEXT PRIMARY KEY," +
                "cname TEXT NOT NULL," +
                "ccredit REAL," +
                "chours INTEGER," +
                "tno TEXT," +
                "semester TEXT)");

        // 选课成绩表（学生和课程是多对多，靠它连接）
        db.execSQL("CREATE TABLE sc(" +
                "sno TEXT," +
                "cno TEXT," +
                "grade REAL," +
                "semester TEXT," +
                "select_time INTEGER," +
                "PRIMARY KEY(sno,cno))");

        seedData(db); // 写入初始测试数据
    }

    // 写入初始测试数据
    private void seedData(SQLiteDatabase db) {
        // 一名老师
        ContentValues t = new ContentValues();
        t.put("tno", "t001");
        t.put("tname", "易云山");
        t.put("tsex", "男");
        t.put("title", "教授");
        t.put("pwd", Md5Util.md5("123456"));
        t.put("phone", "");
        db.insert("teacher", null, t);

        // 学生：学号、姓名、性别、年龄、班级、密码、手机
        String[][] students = {
                {"B24040201", "马悦珊", "女", "20", "计软网安学院B240402", "123456", "13800000001"},
                {"B24040403", "陈一", "女", "20", "计软网安B240403", "123456", "13800000002"},
                {"B24040406", "孙八", "女", "20", "计软网安B240406", "123456", "13800000003"}
        };
        for (String[] s : students) {
            ContentValues v = new ContentValues();
            v.put("sno", s[0]);
            v.put("sname", s[1]);
            v.put("ssex", s[2]);
            v.put("sage", Integer.parseInt(s[3]));
            v.put("sdept", s[4]);
            v.put("pwd", Md5Util.md5(s[5]));
            v.put("phone", s[6]);
            db.insert("student", null, v);
        }

        // 课程：课程号、课程名、学分、学时、学期（老师都是 t001）
        String[][] courses = {
                {"C001", "计算机通信与网络", "3.5", "48", "2026-1"},
                {"C002", "计算机组成原理", "3.5", "48", "2026-1"},
                {"C003", "操作系统", "3.0", "48", "2026-1"},
                {"C004", "嵌入式手机终端开发", "2.0", "32", "2026-1"}
        };
        for (String[] c : courses) {
            ContentValues v = new ContentValues();
            v.put("cno", c[0]);
            v.put("cname", c[1]);
            v.put("ccredit", Double.parseDouble(c[2]));
            v.put("chours", Integer.parseInt(c[3]));
            v.put("tno", "t001");
            v.put("semester", c[4]);
            db.insert("course", null, v);
        }

        // 选课+成绩：含优秀/及格/不及格/未考四种情况，方便演示统计图表
        Object[][] scRows = {
                // 马悦珊：三门有成绩，C004 未考
                {"B24040201", "C001", 88.5, "2026-1"},
                {"B24040201", "C002", 92.0, "2026-1"},
                {"B24040201", "C003", 76.0, "2026-1"},
                {"B24040201", "C004", null, "2026-1"},
                // 陈一：C002 不及格（58），其余有成绩
                {"B24040403", "C001", 75.0, "2026-1"},
                {"B24040403", "C002", 58.0, "2026-1"},
                {"B24040403", "C003", 81.0, "2026-1"},
                {"B24040403", "C004", 85.0, "2026-1"},
                // 孙八：已选两门，均未考
                {"B24040406", "C001", null, "2026-1"},
                {"B24040406", "C002", null, "2026-1"}
        };
        for (Object[] r : scRows) {
            ContentValues v = new ContentValues();
            v.put("sno", (String) r[0]);
            v.put("cno", (String) r[1]);
            if (r[2] != null) {
                v.put("grade", (Double) r[2]); // 有成绩才写，没成绩留空=未考
            }
            v.put("semester", (String) r[3]);
            db.insert("sc", null, v);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 开发阶段改表结构：全删重建（正式版不能这么干）
        db.execSQL("DROP TABLE IF EXISTS sc");
        db.execSQL("DROP TABLE IF EXISTS course");
        db.execSQL("DROP TABLE IF EXISTS student");
        db.execSQL("DROP TABLE IF EXISTS teacher");
        onCreate(db);
    }
}
