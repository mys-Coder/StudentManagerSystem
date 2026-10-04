package com.example.studentmanagersystem;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

// 学生端：本学期成绩单（GPA 总览卡 + 等级胶囊；未考显示“未知”；可导出 CSV）
public class TranscriptActivity extends BaseActivity {

    private String sno;

    // 创建文件（导出成绩单）
    private final ActivityResultLauncher<Intent> createLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null
                        && result.getData().getData() != null) {
                    Uri uri = result.getData().getData();
                    try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                        String csv = new CsvUtil(this).exportTranscript(sno);
                        os.write(csv.getBytes(StandardCharsets.UTF_8));
                        Toast.makeText(this, "成绩单已导出", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "导出失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcript);
        setTitle("本学期成绩单");

        sno = getIntent().getStringExtra("sno");
        ScDao scDao = new ScDao(this);
        List<ScItem> items = scDao.listTranscript(sno);

        View gpaCard = findViewById(R.id.gpaCard);
        TextView tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rv = findViewById(R.id.rvTranscript);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new TranscriptAdapter(items));

        if (items.isEmpty()) {
            // 本学期没有课程：隐藏 GPA 卡，显示空提示
            gpaCard.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            gpaCard.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            fillGpaCard(items);
        }

        Button btnExport = findViewById(R.id.btnExportTranscript);
        btnExport.setOnClickListener(v -> {
            Intent it = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            it.addCategory(Intent.CATEGORY_OPENABLE);
            it.setType("text/csv");
            it.putExtra(Intent.EXTRA_TITLE, "成绩单_" + sno + ".csv");
            createLauncher.launch(it);
        });
    }

    // 计算并填充 GPA 总览卡
    private void fillGpaCard(List<ScItem> items) {
        int graded = 0, ungraded = 0, passN = 0;
        double sum = 0;                 // 算术总分
        double gradedCredit = 0;       // 已出成绩课程的学分
        double earnedCredit = 0;       // 已获得学分（及格）
        double weightedSum = 0;        // Σ(成绩×学分)
        double gpaSum = 0;             // Σ(绩点×学分)

        for (ScItem it : items) {
            Double g = it.getGrade();
            if (g == null) {
                ungraded++;
                continue;
            }
            graded++;
            double score = g;
            double credit = it.getCcredit();
            gradedCredit += credit;
            sum += score;
            weightedSum += score * credit;
            gpaSum += gradeToPoint(score) * credit;
            if (score >= 60) {
                passN++;
                earnedCredit += credit;
            }
        }

        ((TextView) findViewById(R.id.tvGpaValue)).setText(
                graded > 0 ? fmt(gpaSum / gradedCredit) : "--");
        ((TextView) findViewById(R.id.tvWeighted)).setText(
                "加权平均 " + (graded > 0 ? fmt(weightedSum / gradedCredit) : "--"));
        ((TextView) findViewById(R.id.tvArith)).setText(
                "算术平均 " + (graded > 0 ? fmt(sum / graded) : "--"));
        ((TextView) findViewById(R.id.tvPassRate)).setText(
                "及格率 " + (graded > 0 ? String.format("%.1f%%", passN * 100.0 / graded) : "--"));

        ((TextView) findViewById(R.id.tvEnrolled)).setText("已选 " + items.size() + " 门");
        ((TextView) findViewById(R.id.tvEarned)).setText("已获 " + fmt(earnedCredit) + " 学分");
        ((TextView) findViewById(R.id.tvUngraded)).setText("未考 " + ungraded + " 门");
    }

    // 百分制 -> 5.0 制绩点：绩点 = 分数/10 - 5；60 分以下记 0
    private double gradeToPoint(double score) {
        if (score < 60) return 0.0;
        return Math.min(5.0, score / 10.0 - 5.0);
    }

    // 保留两位小数
    private String fmt(double v) {
        return String.format("%.2f", v);
    }
}
