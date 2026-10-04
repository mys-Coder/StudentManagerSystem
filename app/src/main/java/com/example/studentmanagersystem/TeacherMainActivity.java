package com.example.studentmanagersystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.List;

// 老师端主页：学生/课程概览四格 + 四个教学管理入口
public class TeacherMainActivity extends BaseActivity {

    private String tno;
    private StudentDao studentDao;
    private CourseDao courseDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_main);

        String name = getIntent().getStringExtra("name");
        tno = getIntent().getStringExtra("tno");
        studentDao = new StudentDao(this);
        courseDao = new CourseDao(this);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        String title = (name != null && name.endsWith("老师")) ? name : name + "老师";
        tvWelcome.setText("欢迎您，" + title + "！");
        TextView tvAvatar = findViewById(R.id.tvAvatar);
        tvAvatar.setText((name == null || name.isEmpty()) ? "师" : name.substring(0, 1));

        findViewById(R.id.btnStudentManage).setOnClickListener(v ->
                startActivity(new Intent(this, StudentListActivity.class)));

        findViewById(R.id.btnCourseManage).setOnClickListener(v -> {
            Intent it = new Intent(this, CourseListActivity.class);
            it.putExtra("tno", tno);
            startActivity(it);
        });

        findViewById(R.id.btnDataIo).setOnClickListener(v ->
                startActivity(new Intent(this, DataIoActivity.class)));

        findViewById(R.id.btnTeacherManage).setOnClickListener(v -> {
            Intent it = new Intent(this, TeacherListActivity.class);
            it.putExtra("tno", tno);
            startActivity(it);
        });

        findViewById(R.id.btnChangePwd).setOnClickListener(v -> {
            Intent it = new Intent(this, ChangePasswordActivity.class);
            it.putExtra("role", "teacher");
            it.putExtra("id", tno);
            startActivity(it);
        });

        loadOverview();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOverview();
    }

    // 概览：学生总数、本人课程总数、本人课程总学分、本人课程总学时（实时统计）
    private void loadOverview() {
        int studentN = studentDao.getAll().size();

        List<Course> courses = courseDao.getByTeacher(tno);
        double credits = 0;
        int hours = 0;
        for (Course c : courses) {
            credits += c.getCcredit();
            hours += c.getChours();
        }

        ((TextView) findViewById(R.id.tvOvStudents)).setText(String.valueOf(studentN));
        ((TextView) findViewById(R.id.tvOvCourses)).setText(String.valueOf(courses.size()));
        ((TextView) findViewById(R.id.tvOvCredits)).setText(String.format("%.1f", credits));
        ((TextView) findViewById(R.id.tvOvHours)).setText(String.valueOf(hours));
    }
}
