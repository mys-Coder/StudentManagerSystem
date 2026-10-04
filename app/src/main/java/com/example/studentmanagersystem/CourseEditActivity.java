package com.example.studentmanagersystem;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// 课程 新增 / 修改
public class CourseEditActivity extends BaseActivity {

    private EditText etCno, etCname, etCredit, etHours, etSemester;
    private CourseDao dao;
    private String oldCno; // 修改前课程号；null=新增
    private String tno;    // 授课老师工号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_edit);

        etCno = findViewById(R.id.etCno);
        etCname = findViewById(R.id.etCname);
        etCredit = findViewById(R.id.etCredit);
        etHours = findViewById(R.id.etHours);
        etSemester = findViewById(R.id.etSemester);
        Button btnSave = findViewById(R.id.btnSaveCourse);
        dao = new CourseDao(this);

        oldCno = getIntent().getStringExtra("cno");
        tno = getIntent().getStringExtra("tno");
        if (oldCno != null) {
            setTitle("修改课程");
            Course c = dao.getByCno(oldCno);
            if (c != null) {
                etCno.setText(c.getCno());
                etCname.setText(c.getCname());
                etCredit.setText(String.valueOf(c.getCcredit()));
                etHours.setText(String.valueOf(c.getChours()));
                etSemester.setText(c.getSemester());
                tno = c.getTno();
            }
        } else {
            setTitle("新增课程");
            etSemester.setText("2026-1"); // 默认学期，方便录入
        }

        btnSave.setOnClickListener(v -> save());
    }

    private void save() {
        String cno = etCno.getText().toString().trim();
        String cname = etCname.getText().toString().trim();
        String creditStr = etCredit.getText().toString().trim();
        String hoursStr = etHours.getText().toString().trim();
        String semester = etSemester.getText().toString().trim();

        if (TextUtils.isEmpty(cno) || TextUtils.isEmpty(cname)
                || TextUtils.isEmpty(creditStr) || TextUtils.isEmpty(hoursStr)
                || TextUtils.isEmpty(semester)) {
            Toast.makeText(this, "课程号、课程名、学分、学时、学期都要填", Toast.LENGTH_SHORT).show();
            return;
        }
        double credit;
        int hours;
        try {
            credit = Double.parseDouble(creditStr);
            hours = Integer.parseInt(hoursStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "学分是小数、学时是整数", Toast.LENGTH_SHORT).show();
            return;
        }

        // 改了课程号要查重
        if (oldCno != null && !cno.equals(oldCno) && dao.getByCno(cno) != null) {
            Toast.makeText(this, "该课程号已存在", Toast.LENGTH_SHORT).show();
            return;
        }

        Course c = new Course(cno, cname, credit, hours, tno, semester);
        if (oldCno == null) {
            long r = dao.insert(c);
            if (r == -1) {
                Toast.makeText(this, "新增失败：课程号可能已存在", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "新增成功", Toast.LENGTH_SHORT).show();
        } else {
            dao.update(oldCno, c);
            Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
