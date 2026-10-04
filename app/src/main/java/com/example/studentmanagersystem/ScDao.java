package com.example.studentmanagersystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studentmanagersystem.db.DbHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 选课成绩数据访问类：sc 表相关操作
public class ScDao {

    public static final String CURRENT_SEMESTER = "2026-1"; // 本学期

    private final DbHelper helper;

    public ScDao(Context context) {
        helper = new DbHelper(context);
    }

    // 查某门课"全部学生"及该课成绩；没选课/没成绩的学生也列出，grade=null
    public List<GradeItem> listCourseGrades(String cno) {
        List<GradeItem> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT s.sno, s.sname, sc.grade " +
                "FROM student s LEFT JOIN sc ON s.sno = sc.sno AND sc.cno = ? " +
                "ORDER BY s.sno";
        Cursor c = db.rawQuery(sql, new String[]{cno});
        while (c.moveToNext()) {
            int gIdx = c.getColumnIndexOrThrow("grade");
            Double grade = c.isNull(gIdx) ? null : c.getDouble(gIdx);
            list.add(new GradeItem(
                    c.getString(c.getColumnIndexOrThrow("sno")),
                    c.getString(c.getColumnIndexOrThrow("sname")),
                    grade));
        }
        c.close();
        db.close();
        return list;
    }

    // 录入/修改某学生某门课成绩；grade 传 null=未考。没选课会顺带建选课记录
    public void saveGrade(String sno, String cno, Double grade, String semester) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        if (grade == null) v.putNull("grade"); else v.put("grade", grade);

        int rows = db.update("sc", v, "sno=? AND cno=?", new String[]{sno, cno});
        if (rows == 0) {
            ContentValues ins = new ContentValues();
            ins.put("sno", sno);
            ins.put("cno", cno);
            if (grade == null) ins.putNull("grade"); else ins.put("grade", grade);
            ins.put("semester", semester);
            ins.put("select_time", (int) (System.currentTimeMillis() / 1000));
            db.insert("sc", null, ins);
        }
        db.close();
    }

    // 成绩单：某学生本学期所选课程及成绩（连表查课程信息）
    public List<ScItem> listTranscript(String sno) {
        List<ScItem> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT c.cno, c.cname, c.ccredit, c.chours, c.semester, sc.grade " +
                "FROM sc INNER JOIN course c ON sc.cno = c.cno " +
                "WHERE sc.sno = ? AND c.semester = ? ORDER BY c.cno";
        Cursor cur = db.rawQuery(sql, new String[]{sno, CURRENT_SEMESTER});
        while (cur.moveToNext()) {
            int gi = cur.getColumnIndexOrThrow("grade");
            Double g = cur.isNull(gi) ? null : cur.getDouble(gi);
            list.add(new ScItem(
                    cur.getString(cur.getColumnIndexOrThrow("cno")),
                    cur.getString(cur.getColumnIndexOrThrow("cname")),
                    cur.getDouble(cur.getColumnIndexOrThrow("ccredit")),
                    cur.getInt(cur.getColumnIndexOrThrow("chours")),
                    cur.getString(cur.getColumnIndexOrThrow("semester")),
                    g));
        }
        cur.close();
        db.close();
        return list;
    }

    // 某学生已选的全部课程号
    public Set<String> enrolledCnoSet(String sno) {
        Set<String> set = new HashSet<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT cno FROM sc WHERE sno=?", new String[]{sno});
        while (c.moveToNext()) set.add(c.getString(0));
        c.close();
        db.close();
        return set;
    }

    // 选课：插入一条无成绩记录；返回 -1 表示已选过
    public long enroll(String sno, Course course) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("sno", sno);
        v.put("cno", course.getCno());
        v.putNull("grade");
        v.put("semester", course.getSemester());
        v.put("select_time", (int) (System.currentTimeMillis() / 1000));
        long r = db.insert("sc", null, v);
        db.close();
        return r;
    }

    // 退课：删除该选课记录
    public int drop(String sno, String cno) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int n = db.delete("sc", "sno=? AND cno=?", new String[]{sno, cno});
        db.close();
        return n;
    }
}
