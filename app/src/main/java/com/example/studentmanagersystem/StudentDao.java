package com.example.studentmanagersystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studentmanagersystem.db.DbHelper;

import java.util.ArrayList;
import java.util.List;

// 学生数据访问类：专门负责 student 表的增删改查
public class StudentDao {
    private final DbHelper helper;

    public StudentDao(Context context) {
        helper = new DbHelper(context);
    }

    // 查询全部学生，按学号排序
    public List<Student> getAll() {
        List<Student> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM student ORDER BY sno", null);
        while (c.moveToNext()) {
            list.add(cursorToStudent(c));
        }
        c.close();
        db.close();
        return list;
    }
    // 模糊搜索：按学号/姓名/班级/手机匹配
    public List<Student> search(String kw) {
        List<Student> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String k = "%" + kw + "%";
        Cursor c = db.rawQuery(
                "SELECT * FROM student WHERE sno LIKE ? OR sname LIKE ? OR sdept LIKE ? OR phone LIKE ? ORDER BY sno",
                new String[]{k, k, k, k});
        while (c.moveToNext()) list.add(cursorToStudent(c));
        c.close();
        db.close();
        return list;
    }


    // 按学号查一个学生（修改时回显用），找不到返回 null
    public Student getBySno(String sno) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM student WHERE sno=?", new String[]{sno});
        Student s = null;
        if (c.moveToFirst()) {
            s = cursorToStudent(c);
        }
        c.close();
        db.close();
        return s;
    }

    // 新增，返回新行 id（-1 表示失败，通常是学号重复）
    public long insert(Student s) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long id = db.insert("student", null, toValues(s));
        db.close();
        return id;
    }

    // 修改：oldSno 是修改前的学号（用来定位），s 是改后的数据（学号可能已被改）
    public int update(String oldSno, Student s) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int rows = 0;
        db.beginTransaction(); // 开启事务：两张表要么一起成功，要么一起不生效
        try {
            // 1. 更新学生本人（用旧学号定位）
            rows = db.update("student", toValues(s), "sno=?", new String[]{oldSno});
            // 2. 同步把选课成绩表里该生的学号改成新学号，避免成绩对不上人
            ContentValues scV = new ContentValues();
            scV.put("sno", s.getSno());
            db.update("sc", scV, "sno=?", new String[]{oldSno});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        return rows;
    }


    // 删除，按学号，返回删除行数
    public int delete(String sno) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int rows = db.delete("student", "sno=?", new String[]{sno});
        db.close();
        return rows;
    }

    // 把游标当前行转成 Student 对象
    private Student cursorToStudent(Cursor c) {
        return new Student(
                c.getString(c.getColumnIndexOrThrow("sno")),
                c.getString(c.getColumnIndexOrThrow("sname")),
                c.getString(c.getColumnIndexOrThrow("ssex")),
                c.getInt(c.getColumnIndexOrThrow("sage")),
                c.getString(c.getColumnIndexOrThrow("sdept")),
                c.getString(c.getColumnIndexOrThrow("pwd")),
                c.getString(c.getColumnIndexOrThrow("phone"))
        );
    }

    // 把 Student 对象转成 ContentValues
    private ContentValues toValues(Student s) {
        ContentValues v = new ContentValues();
        v.put("sno", s.getSno());
        v.put("sname", s.getSname());
        v.put("ssex", s.getSsex());
        v.put("sage", s.getSage());
        v.put("sdept", s.getSdept());
        v.put("pwd", s.getPwd());
        v.put("phone", s.getPhone());
        return v;
    }
}
