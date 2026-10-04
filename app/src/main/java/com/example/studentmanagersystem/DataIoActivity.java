package com.example.studentmanagersystem;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

// 老师端：CSV 数据导入 / 查询结果导出（系统文件选择器，免存储权限）
public class DataIoActivity extends BaseActivity {

    private static final int TYPE_STUDENT = 1;
    private static final int TYPE_COURSE = 2;
    private static final int TYPE_TEACHER = 3;

    private int importType = 0;
    private int exportType = 0;
    private CsvUtil csv;

    // 选文件（导入）
    private final ActivityResultLauncher<Intent> openLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null
                        && result.getData().getData() != null) {
                    doImport(result.getData().getData());
                }
            });

    // 建文件（导出）
    private final ActivityResultLauncher<Intent> createLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null
                        && result.getData().getData() != null) {
                    doExport(result.getData().getData());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_io);
        setTitle("数据导入 / 导出");
        csv = new CsvUtil(this);

        findViewById(R.id.btnImportStudent).setOnClickListener(v -> pickImport(TYPE_STUDENT));
        findViewById(R.id.btnImportCourse).setOnClickListener(v -> pickImport(TYPE_COURSE));
        findViewById(R.id.btnImportTeacher).setOnClickListener(v -> pickImport(TYPE_TEACHER));
        findViewById(R.id.btnExportStudent).setOnClickListener(v -> pickExport(TYPE_STUDENT, "学生信息.csv"));
        findViewById(R.id.btnExportCourse).setOnClickListener(v -> pickExport(TYPE_COURSE, "课程信息.csv"));
        findViewById(R.id.btnExportTeacher).setOnClickListener(v -> pickExport(TYPE_TEACHER, "教师信息.csv"));
    }

    private void pickImport(int type) {
        importType = type;
        Intent it = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        it.addCategory(Intent.CATEGORY_OPENABLE);
        it.setType("*/*"); // 放宽类型，避免 .csv 被系统归成别的类型而看不到
        openLauncher.launch(it);
    }

    private void pickExport(int type, String fileName) {
        exportType = type;
        Intent it = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        it.addCategory(Intent.CATEGORY_OPENABLE);
        it.setType("text/csv");
        it.putExtra(Intent.EXTRA_TITLE, fileName);
        createLauncher.launch(it);
    }

    private void doImport(Uri uri) {
        int count;
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            if (importType == TYPE_STUDENT) count = csv.importStudents(is);
            else if (importType == TYPE_COURSE) count = csv.importCourses(is);
            else count = csv.importTeachers(is);
            Toast.makeText(this, "导入完成，成功 " + count + " 条", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "导入失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void doExport(Uri uri) {
        String content;
        if (exportType == TYPE_COURSE) content = csv.exportCourses();
        else if (exportType == TYPE_TEACHER) content = csv.exportTeachers();
        else content = csv.exportStudents();
        try (OutputStream os = getContentResolver().openOutputStream(uri)) {
            os.write(content.getBytes(StandardCharsets.UTF_8));
            Toast.makeText(this, "导出成功", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "导出失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
