package com.example.studentmanagersystem;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

// 学生端：查看本人信息（名片 + 六行信息卡）
public class StudentInfoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_info);

        String sno = getIntent().getStringExtra("sno");
        Student s = new StudentDao(this).getBySno(sno);

        if (s == null) {
            Toast.makeText(this, "未找到本人信息", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String name = nz(s.getSname());
        String sex = nz(s.getSsex());
        String dept = nz(s.getSdept());
        String phone = nz(s.getPhone());

        // 顶部名片
        ((TextView) findViewById(R.id.tvAvatar)).setText(
                name.isEmpty() ? "?" : name.substring(0, 1));
        ((TextView) findViewById(R.id.tvName)).setText(name);
        ((TextView) findViewById(R.id.tvClassChip)).setText(dept);
        ((TextView) findViewById(R.id.tvSnoSub)).setText("学号 " + nz(s.getSno()));

        // 性别图标：女 ♀ / 男 ♂
        TextView sexIcon = findViewById(R.id.tvSexIcon);
        if (sex.contains("男")) {
            sexIcon.setText("♂");
            sexIcon.setTextColor(0xFF3E8FC7);
        } else {
            sexIcon.setText("♀");
            sexIcon.setTextColor(0xFF9B59B6);
        }

        // 信息卡六行
        ((TextView) findViewById(R.id.tvValSno)).setText(nz(s.getSno()));
        ((TextView) findViewById(R.id.tvValName)).setText(name);
        ((TextView) findViewById(R.id.tvValSex)).setText(sex);
        ((TextView) findViewById(R.id.tvValAge)).setText(String.valueOf(s.getSage()));
        ((TextView) findViewById(R.id.tvValDept)).setText(dept);
        ((TextView) findViewById(R.id.tvValPhone)).setText(
                phone.isEmpty() ? "未填写" : phone);
    }

    // null 安全转字符串
    private String nz(String v) {
        return v == null ? "" : v.trim();
    }
}
