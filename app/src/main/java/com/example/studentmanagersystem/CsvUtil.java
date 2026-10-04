package com.example.studentmanagersystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studentmanagersystem.db.DbHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// CSV 导入导出工具（配合系统文件选择器 SAF，无需存储权限）
public class CsvUtil {

    private final DbHelper helper;

    public CsvUtil(Context context) {
        helper = new DbHelper(context);
    }

    // ---------- 导入：返回成功条数 ----------

    // 学生 CSV 列：学号,姓名,性别,年龄,班级,密码,手机
    public int importStudents(InputStream in) throws IOException {
        List<String[]> rows = readRows(in, 7);
        SQLiteDatabase db = helper.getWritableDatabase();
        int n = 0;
        for (String[] r : rows) {
            try {
                ContentValues v = new ContentValues();
                v.put("sno", r[0]);
                v.put("sname", r[1]);
                v.put("ssex", r[2]);
                v.put("sage", Integer.parseInt(r[3]));
                v.put("sdept", r[4]);
                String stuPwd = r[5];
                // CSV 里是明文就加密；若已是 32 位 MD5（如本系统导出的文件）则保持不变
                v.put("pwd", Md5Util.isMd5(stuPwd) ? stuPwd : Md5Util.md5(stuPwd));
                v.put("phone", r[6]);
                db.insertWithOnConflict("student", null, v, SQLiteDatabase.CONFLICT_REPLACE);
                n++;
            } catch (Exception ignore) { } // 坏行跳过
        }
        db.close();
        return n;
    }

    // 课程 CSV 支持两种格式：
    //  6 列（推荐，与课程导出文件完全一致）：课程号,课程名,学分,学时,授课教师工号,学期
    //  5 列（旧模板兼容）：课程号,课程名,学分,学时,学期 —— 授课教师默认 t001
    public int importCourses(InputStream in) throws IOException {
        List<String[]> rows = readRows(in, 5);
        SQLiteDatabase db = helper.getWritableDatabase();
        int n = 0;
        for (String[] r : rows) {
            try {
                String tno;
                String semester;
                if (r.length >= 6) {
                    tno = r[4];
                    semester = r[5];
                } else { // 旧 5 列模板：没有教师工号列
                    tno = "t001";
                    semester = r[4];
                }
                // 工号留空、或该教师在库中不存在时，归到默认教师 t001，避免产生无人可见的"孤儿课程"
                if (tno == null || tno.isEmpty() || !teacherExists(db, tno)) {
                    tno = "t001";
                }
                ContentValues v = new ContentValues();
                v.put("cno", r[0]);
                v.put("cname", r[1]);
                v.put("ccredit", Double.parseDouble(r[2]));
                v.put("chours", Integer.parseInt(r[3]));
                v.put("tno", tno);
                v.put("semester", semester);
                db.insertWithOnConflict("course", null, v, SQLiteDatabase.CONFLICT_REPLACE);
                n++;
            } catch (Exception ignore) { } // 坏行跳过
        }
        db.close();
        return n;
    }

    // 判断教师工号是否已存在
    private boolean teacherExists(SQLiteDatabase db, String tno) {
        Cursor c = db.rawQuery("SELECT 1 FROM teacher WHERE tno=? LIMIT 1", new String[]{tno});
        boolean ok = c.moveToFirst();
        c.close();
        return ok;
    }

    // 老师 CSV 列：工号,姓名,性别,职称,密码,手机
    public int importTeachers(InputStream in) throws IOException {
        List<String[]> rows = readRows(in, 6);
        SQLiteDatabase db = helper.getWritableDatabase();
        int n = 0;
        for (String[] r : rows) {
            try {
                ContentValues v = new ContentValues();
                v.put("tno", r[0]);
                v.put("tname", r[1]);
                v.put("tsex", r[2]);
                v.put("title", r[3]);
                String teaPwd = r[4];
                v.put("pwd", Md5Util.isMd5(teaPwd) ? teaPwd : Md5Util.md5(teaPwd));
                v.put("phone", r[5]);
                db.insertWithOnConflict("teacher", null, v, SQLiteDatabase.CONFLICT_REPLACE);
                n++;
            } catch (Exception ignore) { }
        }
        db.close();
        return n;
    }

    // 读取 CSV：自动去 BOM、跳过第一行表头、要求每行至少 minCols 列
    private List<String[]> readRows(InputStream in, int minCols) throws IOException {
        List<String[]> list = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line;
        boolean first = true;
        while ((line = br.readLine()) != null) {
            if (first) {
                first = false;
                if (line.startsWith("\uFEFF")) line = line.substring(1);
                continue; // 跳过表头
            }
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] cols = line.split(",", -1);
            if (cols.length < minCols) continue;
            for (int i = 0; i < cols.length; i++) cols[i] = cols[i].trim();
            list.add(cols);
        }
        br.close();
        return list;
    }

    // ---------- 导出：返回 CSV 文本（开头加 BOM，Excel 打开中文不乱码） ----------

    public String exportStudents() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM student ORDER BY sno", null);
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF').append("学号,姓名,性别,年龄,班级,密码,手机\n");
        while (c.moveToNext()) {
            sb.append(c.getString(c.getColumnIndexOrThrow("sno"))).append(',')
                    .append(c.getString(c.getColumnIndexOrThrow("sname"))).append(',')
                    .append(c.getString(c.getColumnIndexOrThrow("ssex"))).append(',')
                    .append(c.getInt(c.getColumnIndexOrThrow("sage"))).append(',')
                    .append(c.getString(c.getColumnIndexOrThrow("sdept"))).append(',')
                    .append(c.getString(c.getColumnIndexOrThrow("pwd"))).append(',')
                    .append(c.getString(c.getColumnIndexOrThrow("phone"))).append('\n');
        }
        c.close();
        db.close();
        return sb.toString();
    }

    public String exportCourses() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM course ORDER BY cno", null);
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF').append("课程号,课程名,学分,学时,授课教师工号,学期\n");
        while (c.moveToNext()) {
            sb.append(c.getString(c.getColumnIndexOrThrow("cno"))).append(',')
                    .append(c.getString(c.getColumnIndexOrThrow("cname"))).append(',')
                    .append(c.getDouble(c.getColumnIndexOrThrow("ccredit"))).append(',')
                    .append(c.getInt(c.getColumnIndexOrThrow("chours"))).append(',')
                    .append(c.getString(c.getColumnIndexOrThrow("tno"))).append(',')
                    .append(c.getString(c.getColumnIndexOrThrow("semester"))).append('\n');
        }
        c.close();
        db.close();
        return sb.toString();
    }

    public String exportTeachers() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM teacher ORDER BY tno", null);
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF').append("工号,姓名,性别,职称,密码,手机\n");
        while (c.moveToNext()) {
            sb.append(c.getString(c.getColumnIndexOrThrow("tno"))).append(',')
                    .append(c.getString(c.getColumnIndexOrThrow("tname"))).append(',')
                    .append(c.getString(c.getColumnIndexOrThrow("tsex"))).append(',')
                    .append(c.getString(c.getColumnIndexOrThrow("title"))).append(',')
                    .append(c.getString(c.getColumnIndexOrThrow("pwd"))).append(',')
                    .append(c.getString(c.getColumnIndexOrThrow("phone"))).append('\n');
        }
        c.close();
        db.close();
        return sb.toString();
    }

    public String exportTranscript(String sno) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT c.cno,c.cname,c.ccredit,c.chours,c.semester,sc.grade " +
                "FROM sc INNER JOIN course c ON sc.cno=c.cno " +
                "WHERE sc.sno=? AND c.semester=? ORDER BY c.cno";
        Cursor c = db.rawQuery(sql, new String[]{sno, ScDao.CURRENT_SEMESTER});
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF').append("课程号,课程名,学分,学时,学期,成绩\n");
        while (c.moveToNext()) {
            int gi = c.getColumnIndexOrThrow("grade");
            String grade = c.isNull(gi) ? "未知" : String.valueOf(c.getDouble(gi));
            sb.append(c.getString(c.getColumnIndexOrThrow("cno"))).append(',')
                    .append(c.getString(c.getColumnIndexOrThrow("cname"))).append(',')
                    .append(c.getDouble(c.getColumnIndexOrThrow("ccredit"))).append(',')
                    .append(c.getInt(c.getColumnIndexOrThrow("chours"))).append(',')
                    .append(c.getString(c.getColumnIndexOrThrow("semester"))).append(',')
                    .append(grade).append('\n');
        }
        c.close();
        db.close();
        return sb.toString();
    }
}
