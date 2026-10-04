package com.example.studentmanagersystem;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanagersystem.db.DbHelper;

// 修改本人密码：校验原密码 -> 两次新密码一致 -> MD5 加密后存库（老师/学生共用）
public class ChangePasswordActivity extends BaseActivity {

    private String role; // teacher / student
    private String id;   // 工号 / 学号
    private DbHelper dbHelper;
    private EditText etOld, etNew, etConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        setTitle("修改密码");

        role = getIntent().getStringExtra("role");
        id = getIntent().getStringExtra("id");
        dbHelper = new DbHelper(this);

        etOld = findViewById(R.id.etOldPwd);
        etNew = findViewById(R.id.etNewPwd);
        etConfirm = findViewById(R.id.etConfirmPwd);
        Button btnSave = findViewById(R.id.btnSavePwd);
        btnSave.setOnClickListener(v -> save());

        // 眼睛图标：切换密码明文 / 密文显示
        bindEye(R.id.eyeOld, etOld);
        bindEye(R.id.eyeNew, etNew);
        bindEye(R.id.eyeConfirm, etConfirm);
    }

    private void bindEye(int eyeId, EditText target) {
        TextView eye = findViewById(eyeId);
        eye.setOnClickListener(v -> {
            boolean shown = target.getTransformationMethod()
                    instanceof HideReturnsTransformationMethod;
            if (shown) {
                target.setTransformationMethod(PasswordTransformationMethod.getInstance());
                eye.setText("👁");
            } else {
                target.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                eye.setText("🙈");
            }
            target.setSelection(target.getText().length());
        });
    }

    private void save() {
        String oldPwd = etOld.getText().toString().trim();
        String newPwd = etNew.getText().toString().trim();
        String confirm = etConfirm.getText().toString().trim();

        if (TextUtils.isEmpty(oldPwd) || TextUtils.isEmpty(newPwd) || TextUtils.isEmpty(confirm)) {
            Toast.makeText(this, "请把三项都填写完整", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPwd.length() < 6) {
            Toast.makeText(this, "新密码至少 6 位", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPwd.equals(confirm)) {
            Toast.makeText(this, "两次输入的新密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        // 按角色确定要更新的表和主键列
        String table, keyCol;
        if ("teacher".equals(role)) {
            table = "teacher";
            keyCol = "tno";
        } else {
            table = "student";
            keyCol = "sno";
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT pwd FROM " + table + " WHERE " + keyCol + "=?",
                new String[]{id});
        String stored = null;
        if (c.moveToFirst()) stored = c.getString(0);
        c.close();
        db.close();

        if (stored == null) {
            Toast.makeText(this, "账号不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Md5Util.check(oldPwd, stored)) {
            Toast.makeText(this, "原密码不正确", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Md5Util.check(newPwd, stored)) {
            Toast.makeText(this, "新密码不能与原密码相同", Toast.LENGTH_SHORT).show();
            return;
        }

        // 新密码 MD5 加密后更新
        SQLiteDatabase wdb = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("pwd", Md5Util.md5(newPwd));
        wdb.update(table, cv, keyCol + "=?", new String[]{id});
        wdb.close();

        Toast.makeText(this, "密码修改成功，请用新密码重新登录", Toast.LENGTH_LONG).show();
        finish();
    }
}
