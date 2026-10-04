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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// 老师端：学生信息列表（增、删、改、查 + 搜索）
public class StudentListActivity extends BaseActivity {

    private StudentDao dao;
    private StudentAdapter adapter;
    private EditText etSearch;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);
        setTitle("学生信息管理");

        dao = new StudentDao(this);
        etSearch = findViewById(R.id.etSearch);

        RecyclerView rv = findViewById(R.id.rvStudents);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(
                dao.getAll(),
                s -> {
                    Intent it = new Intent(this, StudentEditActivity.class);
                    it.putExtra("sno", s.getSno());
                    startActivity(it);
                },
                s -> { showDeleteDialog(s); return true; });
        rv.setAdapter(adapter);
        tvEmpty = findViewById(R.id.tvEmpty);

        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, StudentEditActivity.class)));

        // 输入内容变化时实时过滤
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { loadData(); }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // 从新增/修改页返回也会刷新
    }

    // 根据搜索框内容决定显示全部还是搜索结果
    private void loadData() {
        String kw = etSearch.getText().toString().trim();
        adapter.setData(kw.isEmpty() ? dao.getAll() : dao.search(kw));
        tvEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void showDeleteDialog(Student s) {
        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("删除学生")
                .setMessage("确定删除 " + s.getSname() + "（" + s.getSno() + "）吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    dao.delete(s.getSno());
                    loadData();
                })
                .setNegativeButton("取消", null)
                .show();
        dlg.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                androidx.core.content.ContextCompat.getColor(this, R.color.accent_red));
    }
}
