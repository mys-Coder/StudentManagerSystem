package com.example.studentmanagersystem;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studentmanagersystem.db.DbHelper;

import java.util.ArrayList;
import java.util.List;

// 教师数据访问类：负责 teacher 表的查询与删除（教师账号通过 CSV 导入新增）
public class TeacherDao {
    private final DbHelper helper;

    public TeacherDao(Context context) {
        helper = new DbHelper(context);
    }

    // 查询全部教师，按工号排序
    public List<Teacher> getAll() {
        List<Teacher> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM teacher ORDER BY tno", null);
        while (c.moveToNext()) list.add(cursorToTeacher(c));
        c.close();
        db.close();
        return list;
    }

    // 模糊搜索：按工号/姓名/职称/手机号匹配
    public List<Teacher> search(String kw) {
        List<Teacher> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String k = "%" + kw + "%";
        Cursor c = db.rawQuery(
                "SELECT * FROM teacher WHERE tno LIKE ? OR tname LIKE ? OR title LIKE ? OR phone LIKE ? ORDER BY tno",
                new String[]{k, k, k, k});
        while (c.moveToNext()) list.add(cursorToTeacher(c));
        c.close();
        db.close();
        return list;
    }

    // 按工号查一个教师，找不到返回 null
    public Teacher getByTno(String tno) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM teacher WHERE tno=?", new String[]{tno});
        Teacher t = null;
        if (c.moveToFirst()) t = cursorToTeacher(c);
        c.close();
        db.close();
        return t;
    }

    // 教师总数
    public int count() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM teacher", null);
        int n = 0;
        if (c.moveToFirst()) n = c.getInt(0);
        c.close();
        db.close();
        return n;
    }

    // 该教师名下的课程门数（删除前校验用）
    public int countCoursesOf(String tno) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM course WHERE tno=?", new String[]{tno});
        int n = 0;
        if (c.moveToFirst()) n = c.getInt(0);
        c.close();
        db.close();
        return n;
    }

    // 删除，按工号，返回删除行数
    public int delete(String tno) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int rows = db.delete("teacher", "tno=?", new String[]{tno});
        db.close();
        return rows;
    }

    private Teacher cursorToTeacher(Cursor c) {
        return new Teacher(
                c.getString(c.getColumnIndexOrThrow("tno")),
                c.getString(c.getColumnIndexOrThrow("tname")),
                c.getString(c.getColumnIndexOrThrow("tsex")),
                c.getString(c.getColumnIndexOrThrow("title")),
                c.getString(c.getColumnIndexOrThrow("pwd")),
                c.getString(c.getColumnIndexOrThrow("phone"))
        );
    }
}
