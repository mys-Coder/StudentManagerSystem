package com.example.studentmanagersystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studentmanagersystem.db.DbHelper;

import java.util.ArrayList;
import java.util.List;

// 课程数据访问类：course 表的增删改查
public class CourseDao {
    private final DbHelper helper;

    public CourseDao(Context context) {
        helper = new DbHelper(context);
    }

    public List<Course> getAll() {
        List<Course> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM course ORDER BY cno", null);
        while (c.moveToNext()) list.add(cursorToCourse(c));
        c.close();
        db.close();
        return list;
    }
    // 模糊搜索：按课程号/课程名/学期匹配
    public List<Course> search(String kw) {
        List<Course> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String k = "%" + kw + "%";
        Cursor c = db.rawQuery(
                "SELECT * FROM course WHERE cno LIKE ? OR cname LIKE ? OR semester LIKE ? ORDER BY cno",
                new String[]{k, k, k});
        while (c.moveToNext()) list.add(cursorToCourse(c));
        c.close();
        db.close();
        return list;
    }

    // 按授课教师工号查询其名下全部课程（教师数据隔离：每位教师只能看到自己的课）
    public List<Course> getByTeacher(String tno) {
        List<Course> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM course WHERE tno=? ORDER BY cno",
                new String[]{tno});
        while (c.moveToNext()) list.add(cursorToCourse(c));
        c.close();
        db.close();
        return list;
    }

    // 按授课教师工号 + 关键字模糊搜索（课程号/课程名/学期），搜索范围仅限本人名下课程
    public List<Course> searchByTeacher(String tno, String kw) {
        List<Course> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String k = "%" + kw + "%";
        Cursor c = db.rawQuery(
                "SELECT * FROM course WHERE tno=? AND (cno LIKE ? OR cname LIKE ? OR semester LIKE ?) ORDER BY cno",
                new String[]{tno, k, k, k});
        while (c.moveToNext()) list.add(cursorToCourse(c));
        c.close();
        db.close();
        return list;
    }


    public Course getByCno(String cno) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM course WHERE cno=?", new String[]{cno});
        Course course = null;
        if (c.moveToFirst()) course = cursorToCourse(c);
        c.close();
        db.close();
        return course;
    }

    public long insert(Course course) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long id = db.insert("course", null, toValues(course));
        db.close();
        return id;
    }

    // 修改：oldCno 是改前课程号（定位用），course 是改后数据（课程号可能已改）
    public int update(String oldCno, Course course) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int rows = 0;
        db.beginTransaction();
        try {
            rows = db.update("course", toValues(course), "cno=?", new String[]{oldCno});
            ContentValues scV = new ContentValues();
            scV.put("cno", course.getCno());
            db.update("sc", scV, "cno=?", new String[]{oldCno}); // 同步选课表课程号
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        return rows;
    }

    // 删除课程：同时删掉该课的选课成绩记录
    public int delete(String cno) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int rows = 0;
        db.beginTransaction();
        try {
            db.delete("sc", "cno=?", new String[]{cno});
            rows = db.delete("course", "cno=?", new String[]{cno});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        return rows;
    }

    private Course cursorToCourse(Cursor c) {
        return new Course(
                c.getString(c.getColumnIndexOrThrow("cno")),
                c.getString(c.getColumnIndexOrThrow("cname")),
                c.getDouble(c.getColumnIndexOrThrow("ccredit")),
                c.getInt(c.getColumnIndexOrThrow("chours")),
                c.getString(c.getColumnIndexOrThrow("tno")),
                c.getString(c.getColumnIndexOrThrow("semester"))
        );
    }

    private ContentValues toValues(Course course) {
        ContentValues v = new ContentValues();
        v.put("cno", course.getCno());
        v.put("cname", course.getCname());
        v.put("ccredit", course.getCcredit());
        v.put("chours", course.getChours());
        v.put("tno", course.getTno());
        v.put("semester", course.getSemester());
        return v;
    }
}
