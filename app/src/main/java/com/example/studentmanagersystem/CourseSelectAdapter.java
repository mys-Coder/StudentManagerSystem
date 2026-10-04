package com.example.studentmanagersystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Set;

// 课程查询/选课列表适配器：彩色图标卡；未选显示“选课”，已选显示“✓已选 + 退课”
public class CourseSelectAdapter extends RecyclerView.Adapter<CourseSelectAdapter.VH> {

    public interface OnToggle {
        void onEnroll(Course c);
        void onDrop(Course c);
    }

    // 图标底色按位置轮换
    private static final int[] ICON_BGS = {
            R.drawable.bg_icon_teal, R.drawable.bg_icon_blue,
            R.drawable.bg_icon_orange, R.drawable.bg_icon_cyan
    };

    private final List<Course> data;
    private Set<String> enrolled;
    private final OnToggle listener;

    public CourseSelectAdapter(List<Course> data, Set<String> enrolled, OnToggle listener) {
        this.data = data;
        this.enrolled = enrolled;
        this.listener = listener;
    }

    public void setEnrolled(Set<String> enrolled) {
        this.enrolled = enrolled;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course_select, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Course c = data.get(position);
        boolean is = enrolled != null && enrolled.contains(c.getCno());

        h.tvIcon.setBackgroundResource(ICON_BGS[position % ICON_BGS.length]);
        h.tvIcon.setText(CourseIcons.iconFor(c.getCname(), position));
        h.tvTitle.setText(c.getCname());
        h.tvSelectNo.setText(c.getCno() + " · 任课教师 " + c.getTno());
        h.tvChipCredit.setText(trimNum(c.getCcredit()) + " 学分");
        h.tvChipHours.setText(c.getChours() + " 学时");

        // 按是否已选切换右侧操作区
        h.btnEnroll.setVisibility(is ? View.GONE : View.VISIBLE);
        h.enrolledBox.setVisibility(is ? View.VISIBLE : View.GONE);
        h.btnEnroll.setOnClickListener(v -> listener.onEnroll(c));
        h.btnDrop.setOnClickListener(v -> listener.onDrop(c));
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    // 整数不带 .0
    private String trimNum(double v) {
        return v == Math.floor(v) ? String.valueOf((int) v) : String.valueOf(v);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvIcon, tvTitle, tvSelectNo, tvChipCredit, tvChipHours;
        View btnEnroll, enrolledBox;
        TextView btnDrop;
        VH(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvTitle = itemView.findViewById(R.id.tvSelectTitle);
            tvSelectNo = itemView.findViewById(R.id.tvSelectNo);
            tvChipCredit = itemView.findViewById(R.id.tvChipCredit);
            tvChipHours = itemView.findViewById(R.id.tvChipHours);
            btnEnroll = itemView.findViewById(R.id.btnEnroll);
            enrolledBox = itemView.findViewById(R.id.enrolledBox);
            btnDrop = itemView.findViewById(R.id.btnDrop);
        }
    }
}
