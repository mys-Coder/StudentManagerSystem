package com.example.studentmanagersystem;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

// 学生端主页：学业概览四格 + 学分进度 + 各科成绩迷你柱图 + 四个功能入口
public class StudentMainActivity extends BaseActivity {

    private static final double TARGET_CREDIT = 12.0; // 本学期目标学分
    private static final int[] GRADED_COLORS = {
            Color.parseColor("#58ADE0"), Color.parseColor("#2F80B5"),
            Color.parseColor("#86BD7A"), Color.parseColor("#63B6E3"),
            Color.parseColor("#3BA9A5")
    };
    private static final int UNK_COLOR = Color.parseColor("#C3CDD3");

    private String sno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);

        String name = getIntent().getStringExtra("name");
        sno = getIntent().getStringExtra("sno");

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("欢迎您，" + name + "同学！");
        TextView tvAvatar = findViewById(R.id.tvAvatar);
        tvAvatar.setText((name == null || name.isEmpty()) ? "生" : name.substring(0, 1));

        bindListeners();
        loadDashboard();
    }

    private void bindListeners() {
        findViewById(R.id.btnMyInfo).setOnClickListener(v -> {
            Intent it = new Intent(this, StudentInfoActivity.class);
            it.putExtra("sno", sno);
            startActivity(it);
        });
        findViewById(R.id.btnSelectCourse).setOnClickListener(v -> {
            Intent it = new Intent(this, CourseSelectActivity.class);
            it.putExtra("sno", sno);
            startActivity(it);
        });
        findViewById(R.id.btnTranscript).setOnClickListener(v -> {
            Intent it = new Intent(this, TranscriptActivity.class);
            it.putExtra("sno", sno);
            startActivity(it);
        });
        findViewById(R.id.btnChangePwd).setOnClickListener(v -> {
            Intent it = new Intent(this, ChangePasswordActivity.class);
            it.putExtra("role", "student");
            it.putExtra("id", sno);
            startActivity(it);
        });
    }

    // 查询本学期数据并填充概览 / 进度 / 迷你图
    private void loadDashboard() {
        ScDao scDao = new ScDao(this);
        List<ScItem> items = scDao.listTranscript(sno);

        int graded = 0, passN = 0;
        double gpaSum = 0, gradedCredit = 0, earned = 0;

        for (ScItem it : items) {
            Double g = it.getGrade();
            double credit = it.getCcredit();
            if (g == null) continue;
            graded++;
            gradedCredit += credit;
            gpaSum += gradeToPoint(g) * credit;
            if (g >= 60) {
                passN++;
                earned += credit;
            }
        }

        // ---- 概览四格 ----
        ((TextView) findViewById(R.id.tvOvGpa)).setText(
                graded > 0 ? String.format("%.2f", gpaSum / gradedCredit) : "--");
        ((TextView) findViewById(R.id.tvOvCredit)).setText(fmtCredit(earned));
        ((TextView) findViewById(R.id.tvOvCount)).setText(String.valueOf(items.size()));
        ((TextView) findViewById(R.id.tvOvPass)).setText(
                graded > 0 ? String.format("%.0f%%", passN * 100.0 / graded) : "--");

        // ---- 学分进度 ----
        int prog = (int) Math.round(earned / TARGET_CREDIT * 100);
        ((ProgressBar) findViewById(R.id.pbCredit)).setProgress(prog);
        ((TextView) findViewById(R.id.tvProgText)).setText(
                "已获 " + fmtCredit(earned) + " / 目标 " + fmtCredit(TARGET_CREDIT) + " 学分");
        ((TextView) findViewById(R.id.tvProgPct)).setText(prog + "%");

        // ---- 迷你柱图 ----
        BarChart chart = findViewById(R.id.miniChart);
        if (items.isEmpty()) {
            chart.setNoDataText("暂未选课，去「课程查询与选课」看看吧");
            chart.clear();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        final boolean[] isUnk = new boolean[items.size()];
        List<String> xLabels = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            ScItem it = items.get(i);
            Double g = it.getGrade();
            xLabels.add(it.getCno());
            if (g == null) {
                isUnk[i] = true;
                entries.add(new BarEntry(i, 6f)); // 未考：灰色矮柱占位，不计任何统计
                colors.add(UNK_COLOR);
            } else {
                entries.add(new BarEntry(i, g.floatValue()));
                colors.add(GRADED_COLORS[i % GRADED_COLORS.length]);
            }
        }

        BarDataSet set = new BarDataSet(entries, "");
        set.setColors(colors);
        set.setValueTextSize(11f);
        set.setValueTextColor(Color.parseColor("#1F3A4D"));
        set.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry e) {
                int i = (int) e.getX();
                if (isUnk[i]) return "未知";
                Double g = items.get(i).getGrade();
                return g == null ? "未知" : trimNum(g);
            }
        });

        BarData data = new BarData(set);
        data.setBarWidth(0.5f);
        chart.setData(data);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setFitBars(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(xLabels.size());
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.parseColor("#8AA0AE"));
        xAxis.setTextSize(10f);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.animateY(600);
    }

    // 百分制 -> 5.0 制绩点：分数/10 - 5，60 以下记 0
    private double gradeToPoint(double score) {
        if (score < 60) return 0.0;
        return Math.min(5.0, score / 10.0 - 5.0);
    }

    // 学分保留一位小数
    private String fmtCredit(double v) {
        return String.format("%.1f", v);
    }

    // 整数成绩不带 .0
    private String trimNum(double v) {
        return v == Math.floor(v) ? String.valueOf((int) v) : String.valueOf(v);
    }
}
