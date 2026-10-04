package com.example.studentmanagersystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// 课程列表适配器：彩色图标卡；单击=录入成绩，长按=修改/删除
public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.VH> {

    // 图标底色按位置轮换
    private static final int[] ICON_BGS = {
            R.drawable.bg_icon_teal, R.drawable.bg_icon_blue,
            R.drawable.bg_icon_orange, R.drawable.bg_icon_cyan
    };

    private List<Course> data;
    private final OnClick click;
    private final OnLongClick longClick;

    public interface OnClick { void on(Course c); }
    public interface OnLongClick { boolean on(Course c); }

    public CourseAdapter(List<Course> data, OnClick click, OnLongClick longClick) {
        this.data = data;
        this.click = click;
        this.longClick = longClick;
    }

    public void setData(List<Course> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Course c = data.get(position);
        h.tvIcon.setBackgroundResource(ICON_BGS[position % ICON_BGS.length]);
        h.tvIcon.setText(CourseIcons.iconFor(c.getCname(), position));
        h.tvTitle.setText(c.getCname());
        h.tvCourseNo.setText(c.getCno());
        h.tvChipCredit.setText(trimNum(c.getCcredit()) + " 学分");
        h.tvChipHours.setText(c.getChours() + " 学时");
        h.tvChipTerm.setText(c.getSemester());
        h.itemView.setOnClickListener(v -> { if (click != null) click.on(c); });
        h.itemView.setOnLongClickListener(v -> {
            if (longClick != null) return longClick.on(c);
            return false;
        });
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    // 整数不带 .0
    private String trimNum(double v) {
        return v == Math.floor(v) ? String.valueOf((int) v) : String.valueOf(v);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvIcon, tvTitle, tvCourseNo, tvChipCredit, tvChipHours, tvChipTerm;
        VH(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvCourseNo = itemView.findViewById(R.id.tvCourseNo);
            tvChipCredit = itemView.findViewById(R.id.tvChipCredit);
            tvChipHours = itemView.findViewById(R.id.tvChipHours);
            tvChipTerm = itemView.findViewById(R.id.tvChipTerm);
        }
    }
}
