package com.example.studentmanagersystem;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

// 老师端：某门课成绩统计（指标方块 + 分数段柱状图 + 及格/不及格/未考环形图）
public class CourseStatsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_stats);
        setTitle("成绩统计");

        String cno = getIntent().getStringExtra("cno");
        String tno = getIntent().getStringExtra("tno");
        ScDao scDao = new ScDao(this);
        CourseDao courseDao = new CourseDao(this);
        Course course = courseDao.getByCno(cno);

        // 归属校验：课程不存在，或不是当前登录教师名下的课，一律拒绝进入
        if (course == null || (tno != null && !tno.equals(course.getTno()))) {
            Toast.makeText(this, "只能查看本人名下课程的成绩统计", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<GradeItem> list = scDao.listCourseGrades(cno);

        // ---- 统计 ----
        int total = list.size();
        int graded = 0;
        int excellent = 0, good = 0, medium = 0, passBand = 0, fail = 0;
        double sum = 0, max = -1;

        for (GradeItem it : list) {
            Double g = it.getGrade();
            if (g == null) continue;
            graded++;
            double v = g;
            sum += v;
            if (v > max) max = v;
            if (v >= 90) excellent++;
            else if (v >= 80) good++;
            else if (v >= 70) medium++;
            else if (v >= 60) passBand++;
            else fail++;
        }
        int ungraded = total - graded;
        int passTotal = excellent + good + medium + passBand;

        // ---- 课程信息卡 ----
        ((TextView) findViewById(R.id.tvCourseName)).setText(
                course == null ? cno : course.getCno() + " " + course.getCname());
        ((TextView) findViewById(R.id.tvCourseSub)).setText(
                "学分 " + (course == null ? "--" : String.valueOf(course.getCcredit()))
                        + " · 学时 " + (course == null ? "--" : course.getChours())
                        + " · " + (course == null ? "--" : course.getSemester()));

        ((TextView) findViewById(R.id.tvMTotal)).setText(String.valueOf(total));
        ((TextView) findViewById(R.id.tvMGraded)).setText(String.valueOf(graded));
        ((TextView) findViewById(R.id.tvMUngraded)).setText(String.valueOf(ungraded));
        ((TextView) findViewById(R.id.tvMAvg)).setText(
                graded > 0 ? String.format("%.1f", sum / graded) : "--");
        ((TextView) findViewById(R.id.tvMMax)).setText(max >= 0 ? trimNum(max) : "--");
        ((TextView) findViewById(R.id.tvMPassRate)).setText(
                graded > 0 ? String.format("%.1f%%", passTotal * 100.0 / graded) : "--");

        // ---- 柱状图：分数段人数 ----
        BarChart barChart = findViewById(R.id.barChart);
        List<BarEntry> barEntries = new ArrayList<>();
        float[] counts = {excellent, good, medium, passBand, fail};
        for (int i = 0; i < counts.length; i++) barEntries.add(new BarEntry(i, counts[i]));

        BarDataSet barSet = new BarDataSet(barEntries, "");
        barSet.setColors(
                Color.parseColor("#4F8A4D"), // 优秀 深绿
                Color.parseColor("#86BD7A"), // 良好 浅绿
                Color.parseColor("#EFC75A"), // 中等 黄
                Color.parseColor("#EC9240"), // 及格 橙
                Color.parseColor("#D2504A"));// 不及格 红
        barSet.setValueTextSize(13f);
        barSet.setValueTextColor(Color.parseColor("#1F3A4D"));
        barSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry e) {
                return e.getY() <= 0 ? "" : String.valueOf((int) e.getY()); // 0 不显示
            }
        });

        BarData barData = new BarData(barSet);
        barData.setBarWidth(0.55f);
        barChart.setData(barData);
        // 圆角渲染器已撤销（第 1 步状态：直角柱）

        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setTouchEnabled(false);
        barChart.setFitBars(true);
        barChart.setNoDataText("暂无成绩数据");

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(
                new String[]{"优秀", "良好", "中等", "及格", "不及格"}));
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(5);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.parseColor("#627B8C"));
        xAxis.setTextSize(12f);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.animateY(700);

        // ---- 环形饼图：及格 / 不及格 / 未考 ----
        PieChart pieChart = findViewById(R.id.pieChart);
        List<PieEntry> pieEntries = new ArrayList<>();
        if (passTotal > 0) pieEntries.add(new PieEntry(passTotal, "及格"));
        if (fail > 0) pieEntries.add(new PieEntry(fail, "不及格"));
        if (ungraded > 0) pieEntries.add(new PieEntry(ungraded, "未考"));

        PieDataSet pieSet = new PieDataSet(pieEntries, "");
        pieSet.setColors(
                Color.parseColor("#6FB587"), // 及格 柔和绿
                Color.parseColor("#D9706A"), // 不及格 柔和红
                Color.parseColor("#C3CDD3"));// 未考 灰
        pieSet.setSliceSpace(2f);

        PieData pieData = new PieData(pieSet);
        pieData.setDrawValues(false); // 扇区内不写数字
        pieChart.setData(pieData);

        pieChart.getDescription().setEnabled(false);
        pieChart.setTouchEnabled(false);
        pieChart.setHoleRadius(56f);
        pieChart.setTransparentCircleRadius(60f);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.setDrawCenterText(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setNoDataText("暂无成绩数据");

        // 右侧自定义图例：“及格 X 人 · xx%”（占比按全班应考人数）
        Legend legend = pieChart.getLegend();
        List<LegendEntry> legendEntries = new ArrayList<>();
        legendEntries.add(new LegendEntry("及格 " + passTotal + " 人 · " + pct(passTotal, total),
                Legend.LegendForm.SQUARE, 14f, 2f, null, Color.parseColor("#6FB587")));
        legendEntries.add(new LegendEntry("不及格 " + fail + " 人 · " + pct(fail, total),
                Legend.LegendForm.SQUARE, 14f, 2f, null, Color.parseColor("#D9706A")));
        legendEntries.add(new LegendEntry("未考 " + ungraded + " 人 · " + pct(ungraded, total),
                Legend.LegendForm.SQUARE, 14f, 2f, null, Color.parseColor("#C3CDD3")));
        legend.setCustom(legendEntries);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setTextSize(13f);
        legend.setFormSize(14f);
        legend.setXEntrySpace(8f);
        legend.setYEntrySpace(14f);
        legend.setTextColor(Color.parseColor("#1F3A4D"));
        pieChart.animateY(700);
    }

    private String pct(int n, int total) {
        return total > 0 ? String.format("%.0f%%", n * 100.0 / total) : "0%";
    }

    private String trimNum(double v) {
        return v == Math.floor(v) ? String.valueOf((int) v) : String.valueOf(v);
    }
}
