package com.example.studentmanagersystem;

import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// 成绩单适配器：右侧等级胶囊（优秀/良好/中等/及格/不及格/未知）
public class TranscriptAdapter extends RecyclerView.Adapter<TranscriptAdapter.VH> {

    private List<ScItem> data;

    public TranscriptAdapter(List<ScItem> data) {
        this.data = data;
    }

    public void setData(List<ScItem> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transcript, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ScItem it = data.get(position);
        // 标题只显示课程名，副标题显示“课程号 · 学分”
        h.tvTitle.setText(it.getCname());
        h.tvDetail.setText(it.getCno() + " · " + fmtCredit(it.getCcredit()) + " 学分");

        Double g = it.getGrade();
        if (g == null) {
            h.tvGrade.setText("未知");
            h.tvGrade.setBackgroundResource(R.drawable.bg_badge_gray);
            h.tvGrade.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.accent_gray));
            return;
        }
        double score = g;
        String label;
        int bgRes;
        int colorRes;
        if (score >= 90) {
            label = "优秀"; bgRes = R.drawable.bg_badge_green; colorRes = R.color.accent_green;
        } else if (score >= 80) {
            label = "良好"; bgRes = R.drawable.bg_badge_blue; colorRes = R.color.blue_600;
        } else if (score >= 70) {
            label = "中等"; bgRes = R.drawable.bg_badge_orange; colorRes = R.color.accent_orange;
        } else if (score >= 60) {
            label = "及格"; bgRes = R.drawable.bg_badge_teal; colorRes = R.color.accent_teal;
        } else {
            label = "不及格"; bgRes = R.drawable.bg_badge_red; colorRes = R.color.accent_red;
        }
        h.tvGrade.setText(fmtScore(score) + " " + label);
        h.tvGrade.setBackgroundResource(bgRes);
        h.tvGrade.setTextColor(ContextCompat.getColor(h.itemView.getContext(), colorRes));
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    // 学分：整数显示 3，小数显示 3.5
    private String fmtCredit(double c) {
        if (c == Math.floor(c)) return String.valueOf((int) c);
        return String.valueOf(c);
    }

    // 成绩：整数显示 92，小数显示 88.5
    private String fmtScore(double s) {
        if (s == Math.floor(s)) return String.valueOf((int) s);
        return String.valueOf(s);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDetail, tvGrade;
        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTrTitle);
            tvDetail = itemView.findViewById(R.id.tvTrDetail);
            tvGrade = itemView.findViewById(R.id.tvTrGrade);
        }
    }
}
