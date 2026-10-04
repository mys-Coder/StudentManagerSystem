package com.example.studentmanagersystem;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// 学生 新增 / 修改 页面（学号允许修改，会同步更新选课成绩表）
public class StudentEditActivity extends BaseActivity {

    private EditText etSno, etName, etAge, etDept, etPwd, etPhone;
    private Spinner spSex;
    private StudentDao dao;
    private String editSno; // 修改前的学号；null=新增模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_edit);

        etSno = findViewById(R.id.etSno);
        etName = findViewById(R.id.etName);
        spSex = findViewById(R.id.spSex);
        etAge = findViewById(R.id.etAge);
        etDept = findViewById(R.id.etDept);
        etPwd = findViewById(R.id.etPwd);
        etPhone = findViewById(R.id.etPhone);
        Button btnSave = findViewById(R.id.btnSave);
        dao = new StudentDao(this);

        // 性别下拉框
        ArrayAdapter<String> sexAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"男", "女"});
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSex.setAdapter(sexAdapter);

        // 判断新增还是修改
        editSno = getIntent().getStringExtra("sno");
        if (editSno != null) {
            setTitle("修改学生");
            Student s = dao.getBySno(editSno);
            if (s != null) {
                etSno.setText(s.getSno());
                etName.setText(s.getSname());
                spSex.setSelection("女".equals(s.getSsex()) ? 1 : 0);
                etAge.setText(String.valueOf(s.getSage()));
                etDept.setText(s.getSdept());
                // 密码框不回填密文：留空表示不修改密码，填写了才更新
                etPwd.setText("");
                etPwd.setHint("留空则不修改密码");
                etPhone.setText(s.getPhone());
            }
            // 学号现在允许修改，不再禁用
        } else {
            setTitle("新增学生");
        }

        btnSave.setOnClickListener(v -> save());
    }

    private void save() {
        String sno = etSno.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String dept = etDept.getText().toString().trim();
        String pwd = etPwd.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String sex = (String) spSex.getSelectedItem();

        // 非空校验（修改模式下密码可留空，表示不修改原密码）
        if (TextUtils.isEmpty(sno) || TextUtils.isEmpty(name)
                || TextUtils.isEmpty(ageStr)
                || (editSno == null && TextUtils.isEmpty(pwd))) {
            Toast.makeText(this, "学号、姓名、年龄不能为空，新增时密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "年龄必须是数字", Toast.LENGTH_SHORT).show();
            return;
        }

        // 如果改了学号，新学号不能和已有的别人重复
        if (editSno != null && !sno.equals(editSno) && dao.getBySno(sno) != null) {
            Toast.makeText(this, "该学号已存在，不能重复", Toast.LENGTH_SHORT).show();
            return;
        }

        // 统一做 MD5 加密后再存库：
        // 新增：密码必填，明文先加密；修改：留空则沿用原密码，填了新密码则加密。
        // 同时把历史明文密码在保存时自动迁移为密文。
        String finalPwd;
        if (editSno == null) {
            finalPwd = Md5Util.isMd5(pwd) ? pwd : Md5Util.md5(pwd);
        } else if (TextUtils.isEmpty(pwd)) {
            Student old = dao.getBySno(editSno);
            String oldPwd = (old != null && old.getPwd() != null) ? old.getPwd() : "123456";
            finalPwd = Md5Util.isMd5(oldPwd) ? oldPwd : Md5Util.md5(oldPwd);
        } else {
            finalPwd = Md5Util.isMd5(pwd) ? pwd : Md5Util.md5(pwd);
        }

        Student s = new Student(sno, name, sex, age, dept, finalPwd, phone);
        if (editSno == null) {
            long r = dao.insert(s);
            if (r == -1) {
                Toast.makeText(this, "新增失败：学号可能已存在", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "新增成功", Toast.LENGTH_SHORT).show();
        } else {
            dao.update(editSno, s); // 用旧学号定位，连带更新选课表
            Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
