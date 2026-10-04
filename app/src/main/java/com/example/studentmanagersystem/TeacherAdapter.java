package com.example.studentmanagersystem;

import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// 教师列表适配器：圆形姓氏头像 + 姓名/工号行 + 职称胶囊；长按删除
public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.VH> {

    private List<Teacher> data;
    private final OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        boolean onLongClick(Teacher t);
    }

    // 头像底色 / 文字色按行号轮换
    private static final int[] AVATAR_BG = {
            R.drawable.bg_avatar_blue, R.drawable.bg_avatar_green,
            R.drawable.bg_avatar_orange, R.drawable.bg_avatar_gray
    };
    private static final int[] AVATAR_TEXT = {
            R.color.blue_600, R.color.accent_green, R.color.accent_orange, R.color.accent_gray
    };

    public TeacherAdapter(List<Teacher> data, OnItemLongClickListener longClickListener) {
        this.data = data;
        this.longClickListener = longClickListener;
    }

    public void setData(List<Teacher> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Teacher t = data.get(position);
        String name = t.getTname();

        h.tvAvatar.setText((name == null || name.isEmpty()) ? "?" : name.substring(0, 1));
        int ci = position % 4;
        h.tvAvatar.setBackgroundResource(AVATAR_BG[ci]);
        h.tvAvatar.setTextColor(ContextCompat.getColor(h.itemView.getContext(), AVATAR_TEXT[ci]));

        h.tvTitle.setText(name);
        String sex = t.getTsex() == null ? "" : t.getTsex();
        h.tvDetail.setText(t.getTno() + " · " + sex);

        String title = t.getTitle();
        if (title == null || title.trim().isEmpty()) {
            h.tvBadge.setVisibility(View.GONE);
        } else {
            h.tvBadge.setVisibility(View.VISIBLE);
            h.tvBadge.setText(title);
        }

        h.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) return longClickListener.onLongClick(t);
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvTitle, tvDetail, tvBadge;
        VH(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDetail = itemView.findViewById(R.id.tvDetail);
            tvBadge = itemView.findViewById(R.id.tvBadge);
        }
    }
}
