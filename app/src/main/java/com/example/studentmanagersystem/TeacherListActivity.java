package com.example.studentmanagersystem;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// 老师端：教师信息列表（搜索 + 长按删除，含自身/末位/名下有课程三重保护）
public class TeacherListActivity extends BaseActivity {

    private TeacherDao dao;
    private TeacherAdapter adapter;
    private EditText etSearch;
    private TextView tvEmpty;
    private String currentTno; // 当前登录教师工号，不允许删除自己

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_list);
        setTitle("教师信息管理");

        currentTno = getIntent().getStringExtra("tno");
        dao = new TeacherDao(this);
        etSearch = findViewById(R.id.etSearch);

        RecyclerView rv = findViewById(R.id.rvTeachers);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TeacherAdapter(dao.getAll(), t -> { showDeleteDialog(t); return true; });
        rv.setAdapter(adapter);
        tvEmpty = findViewById(R.id.tvEmpty);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { loadData(); }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        String kw = etSearch.getText().toString().trim();
        adapter.setData(kw.isEmpty() ? dao.getAll() : dao.search(kw));
        tvEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void showDeleteDialog(Teacher t) {
        // 保护 1：不能删除当前登录的自己
        if (t.getTno() != null && t.getTno().equals(currentTno)) {
            Toast.makeText(this, "不能删除当前登录的教师账号", Toast.LENGTH_SHORT).show();
            return;
        }
        // 保护 2：至少保留一个教师账号
        if (dao.count() <= 1) {
            Toast.makeText(this, "至少保留一个教师账号，无法删除", Toast.LENGTH_SHORT).show();
            return;
        }
        // 保护 3：名下还有课程的教师不能直接删除
        int courseN = dao.countCoursesOf(t.getTno());
        if (courseN > 0) {
            Toast.makeText(this,
                    "该教师名下还有 " + courseN + " 门课程，请先在课程管理中删除或调整后再删除",
                    Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("删除教师")
                .setMessage("确定删除 " + t.getTname() + "（" + t.getTno() + "）吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    dao.delete(t.getTno());
                    loadData();
                })
                .setNegativeButton("取消", null)
                .show();
        dlg.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                androidx.core.content.ContextCompat.getColor(this, R.color.accent_red));
    }
}
