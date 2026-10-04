package com.example.studentmanagersystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// 老师端：课程列表（总数/学分/学时概览、搜索、单击录入成绩、长按修改/删除）
public class CourseListActivity extends BaseActivity {

    private CourseDao dao;
    private CourseAdapter adapter;
    private EditText etSearch;
    private TextView tvEmpty;
    private String tno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);
        setTitle("课程管理");

        dao = new CourseDao(this);
        tno = getIntent().getStringExtra("tno");
        etSearch = findViewById(R.id.etSearchCourse);

        RecyclerView rv = findViewById(R.id.rvCourses);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CourseAdapter(
                dao.getByTeacher(tno),
                c -> {
                    Intent it = new Intent(this, CourseGradeActivity.class);
                    it.putExtra("cno", c.getCno());
                    it.putExtra("tno", tno);
                    startActivity(it);
                },
                c -> { showCourseMenu(c); return true; });
        rv.setAdapter(adapter);
        tvEmpty = findViewById(R.id.tvEmpty);

        Button btnAdd = findViewById(R.id.btnAddCourse);
        btnAdd.setOnClickListener(v -> {
            Intent it = new Intent(this, CourseEditActivity.class);
            it.putExtra("tno", tno);
            startActivity(it);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { loadData(); }
        });

        refreshTotals();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        refreshTotals();
    }

    private void loadData() {
        String kw = etSearch.getText().toString().trim();
        adapter.setData(kw.isEmpty() ? dao.getByTeacher(tno) : dao.searchByTeacher(tno, kw));
        tvEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    // 统计本人名下课程的门数 / 总学分 / 总学时（不受搜索词影响）
    private void refreshTotals() {
        List<Course> all = dao.getByTeacher(tno);
        double credits = 0;
        int hours = 0;
        for (Course c : all) {
            credits += c.getCcredit();
            hours += c.getChours();
        }
        ((TextView) findViewById(R.id.tvMTotal)).setText(String.valueOf(all.size()));
        ((TextView) findViewById(R.id.tvMCredit)).setText(String.format("%.1f", credits));
        ((TextView) findViewById(R.id.tvMHours)).setText(String.valueOf(hours));
    }

    private void showCourseMenu(Course c) {
        String[] items = {"修改课程信息", "删除该课程", "成绩统计图表"};
        new AlertDialog.Builder(this)
                .setTitle(c.getCno() + " " + c.getCname())
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        Intent it = new Intent(this, CourseEditActivity.class);
                        it.putExtra("cno", c.getCno());
                        startActivity(it);
                    } else if (which == 1) {
                        AlertDialog del = new AlertDialog.Builder(this)
                                .setTitle("删除课程")
                                .setMessage("确定删除 " + c.getCname() + " 吗？\n该课的选课成绩记录也会一起删除。")
                                .setPositiveButton("删除", (d, w) -> {
                                    dao.delete(c.getCno());
                                    loadData();
                                    refreshTotals();
                                })
                                .setNegativeButton("取消", null)
                                .show();
                        del.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                                androidx.core.content.ContextCompat.getColor(this, R.color.accent_red));
                    } else {
                        Intent it = new Intent(this, CourseStatsActivity.class);
                        it.putExtra("cno", c.getCno());
                        it.putExtra("tno", tno);
                        startActivity(it);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
