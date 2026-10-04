package com.example.studentmanagersystem;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Set;

// 学生端：课程查询 + 选课/退课（顶部选课概览实时更新）
public class CourseSelectActivity extends BaseActivity {

    private CourseDao courseDao;
    private ScDao scDao;
    private CourseSelectAdapter adapter;
    private TextView tvSummary;
    private List<Course> allCourses;
    private String sno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_select);
        setTitle("课程查询与选课");

        sno = getIntent().getStringExtra("sno");
        courseDao = new CourseDao(this);
        scDao = new ScDao(this);

        allCourses = courseDao.getAll();
        RecyclerView rv = findViewById(R.id.rvSelectCourses);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CourseSelectAdapter(allCourses, scDao.enrolledCnoSet(sno),
                new CourseSelectAdapter.OnToggle() {
                    @Override public void onEnroll(Course c) { doEnroll(c); }
                    @Override public void onDrop(Course c) { doDrop(c); }
                });
        rv.setAdapter(adapter);
        tvSummary = findViewById(R.id.tvSummary);
        TextView tvEmpty = findViewById(R.id.tvEmpty);
        tvEmpty.setVisibility(allCourses.isEmpty() ? View.VISIBLE : View.GONE);
        refreshSummary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            allCourses = courseDao.getAll();
            adapter.setEnrolled(scDao.enrolledCnoSet(sno));
            refreshSummary();
        }
    }

    // 概览：可选门数 / 已选门数 / 选课学分合计
    private void refreshSummary() {
        Set<String> en = scDao.enrolledCnoSet(sno);
        double credits = 0;
        for (Course c : allCourses) {
            if (en.contains(c.getCno())) credits += c.getCcredit();
        }
        tvSummary.setText("本学期可选 " + allCourses.size() + " 门，你已选 " + en.size()
                + " 门 · 选课学分合计 " + String.format("%.1f", credits)
                + "\n已选课程可随时退课，未出成绩显示“未知”。");
    }

    private void doEnroll(Course c) {
        long r = scDao.enroll(sno, c);
        if (r == -1) {
            Toast.makeText(this, "你已经选过这门课了", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "选课成功：" + c.getCname(), Toast.LENGTH_SHORT).show();
        }
        adapter.setEnrolled(scDao.enrolledCnoSet(sno));
        refreshSummary();
    }

    private void doDrop(Course c) {
        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("退课")
                .setMessage("确定退选 " + c.getCname() + " 吗？")
                .setPositiveButton("退课", (d, w) -> {
                    scDao.drop(sno, c.getCno());
                    Toast.makeText(this, "已退课", Toast.LENGTH_SHORT).show();
                    adapter.setEnrolled(scDao.enrolledCnoSet(sno));
                    refreshSummary();
                })
                .setNegativeButton("取消", null)
                .show();
        dlg.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                androidx.core.content.ContextCompat.getColor(this, R.color.accent_orange));
    }
}
