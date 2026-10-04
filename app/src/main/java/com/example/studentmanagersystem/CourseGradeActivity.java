package com.example.studentmanagersystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// 老师端：某门课的课程信息 + 全班学生成绩录入
public class CourseGradeActivity extends BaseActivity {

    private ScDao scDao;
    private Course course;
    private List<GradeItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_grade);
        setTitle("成绩录入");

        String cno = getIntent().getStringExtra("cno");
        String tno = getIntent().getStringExtra("tno");
        CourseDao courseDao = new CourseDao(this);
        scDao = new ScDao(this);
        course = courseDao.getByCno(cno);

        // 归属校验：课程不存在，或不是当前登录教师名下的课，一律拒绝进入
        if (course == null || (tno != null && !tno.equals(course.getTno()))) {
            Toast.makeText(this, "只能查看和录入本人名下课程的成绩", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvInfo = findViewById(R.id.tvCourseInfo);
        tvInfo.setText("课程：" + course.getCno() + " " + course.getCname()
                + "\n学分：" + course.getCcredit() + "　学时：" + course.getChours()
                + "　学期：" + course.getSemester());

        items = scDao.listCourseGrades(cno);
        RecyclerView rv = findViewById(R.id.rvGrades);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new GradeAdapter(items, this::saveOne));
        TextView tvEmpty = findViewById(R.id.tvEmpty);
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        Button btnStats = findViewById(R.id.btnStats);
        btnStats.setOnClickListener(v -> {
            Intent it = new Intent(this, CourseStatsActivity.class);
            it.putExtra("cno", cno);
            it.putExtra("tno", tno);
            startActivity(it);
        });
    }

    private void saveOne(GradeItem item, String text) {
        Double grade = null;
        if (!TextUtils.isEmpty(text)) {
            try {
                grade = Double.parseDouble(text);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "成绩必须是数字", Toast.LENGTH_SHORT).show();
                return;
            }
            if (grade < 0 || grade > 100) {
                Toast.makeText(this, "成绩应在 0~100 之间", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        scDao.saveGrade(item.getSno(), course.getCno(), grade, course.getSemester());
        item.setGrade(grade);
        Toast.makeText(this,
                item.getSname() + " 的成绩已" + (grade == null ? "清空（未考）" : "保存为 " + grade),
                Toast.LENGTH_SHORT).show();
    }
}
