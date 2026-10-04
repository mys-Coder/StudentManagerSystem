package com.example.studentmanagersystem;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.studentmanagersystem.db.DbHelper;

// 登录页：先查老师表，再查学生表，按角色跳到不同主页
public class MainActivity extends BaseActivity {

    private EditText etAccount, etPwd;
    private DbHelper dbHelper;

    @Override
    protected boolean fitSystemBars() {
        return false; // 壁纸全屏，卡片和欢迎语自己避让状态栏 / 导航栏
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etAccount = findViewById(R.id.etAccount);
        etPwd = findViewById(R.id.etPwd);
        Button btnLogin = findViewById(R.id.btnLogin);
        dbHelper = new DbHelper(this);

        btnLogin.setOnClickListener(v -> login());

        // 壁纸保持全屏：只把登录卡片下移到状态栏之下、欢迎语上移到导航栏之上
        View card = findViewById(R.id.loginPanel);
        View welcome = findViewById(R.id.tvWelcome);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.statusBars()
                            | WindowInsetsCompat.Type.navigationBars());
            ViewGroup.MarginLayoutParams cp = (ViewGroup.MarginLayoutParams) card.getLayoutParams();
            cp.topMargin = bars.top + dp(28);
            card.setLayoutParams(cp);
            ViewGroup.MarginLayoutParams wp = (ViewGroup.MarginLayoutParams) welcome.getLayoutParams();
            wp.bottomMargin = bars.bottom + dp(24);
            welcome.setLayoutParams(wp);
            return insets;
        });
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void login() {
        String acc = etAccount.getText().toString().trim();
        String pwd = etPwd.getText().toString().trim();

        if (TextUtils.isEmpty(acc) || TextUtils.isEmpty(pwd)) {
            Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 1) 先按老师工号查（库里存 MD5，用 Md5Util 校验，兼容历史明文）
        Cursor tc = db.rawQuery(
                "SELECT tname,pwd FROM teacher WHERE tno=?",
                new String[]{acc});
        if (tc.moveToFirst()) {
            String name = tc.getString(0);
            String storedPwd = tc.getString(1);
            tc.close();
            if (Md5Util.check(pwd, storedPwd)) {
                Intent i = new Intent(this, TeacherMainActivity.class);
                i.putExtra("name", name);
                i.putExtra("tno", acc);
                startActivity(i);
                return;
            }
        } else {
            tc.close();
        }

        // 2) 再按学生学号查
        Cursor sc = db.rawQuery(
                "SELECT sname,pwd FROM student WHERE sno=?",
                new String[]{acc});
        if (sc.moveToFirst()) {
            String name = sc.getString(0);
            String storedPwd = sc.getString(1);
            sc.close();
            if (Md5Util.check(pwd, storedPwd)) {
                Intent i = new Intent(this, StudentMainActivity.class);
                i.putExtra("name", name);
                i.putExtra("sno", acc);
                startActivity(i);
                return;
            }
        } else {
            sc.close();
        }

        // 3) 账号不存在或密码不对
        Toast.makeText(this, "账号或密码错误", Toast.LENGTH_SHORT).show();
    }
}
