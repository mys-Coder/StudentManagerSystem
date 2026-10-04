package com.example.studentmanagersystem;

import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// 学生列表适配器：圆形姓氏头像 + 姓名/学号行 + 班级胶囊；单击修改，长按删除
public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.VH> {

    private List<Student> data;
    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;

    // 单击=修改，长按=删除
    public interface OnItemClickListener {
        void onClick(Student s);
    }
    public interface OnItemLongClickListener {
        boolean onLongClick(Student s);
    }

    // 头像底色 / 文字色按行号轮换
    private static final int[] AVATAR_BG = {
            R.drawable.bg_avatar_blue, R.drawable.bg_avatar_green,
            R.drawable.bg_avatar_orange, R.drawable.bg_avatar_gray
    };
    private static final int[] AVATAR_TEXT = {
            R.color.blue_600, R.color.accent_green, R.color.accent_orange, R.color.accent_gray
    };

    public StudentAdapter(List<Student> data,
                          OnItemClickListener clickListener,
                          OnItemLongClickListener longClickListener) {
        this.data = data;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    // 刷新数据
    public void setData(List<Student> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Student s = data.get(position);
        String name = s.getSname();

        // 圆形头像：姓名首字 + 轮换底色
        h.tvAvatar.setText((name == null || name.isEmpty()) ? "?" : name.substring(0, 1));
        int ci = position % 4;
        h.tvAvatar.setBackgroundResource(AVATAR_BG[ci]);
        h.tvAvatar.setTextColor(ContextCompat.getColor(h.itemView.getContext(), AVATAR_TEXT[ci]));

        // 主标题姓名，副标题“学号 · 性别 · 年龄”
        h.tvTitle.setText(name);
        h.tvDetail.setText(s.getSno() + " · " + s.getSsex() + " · " + s.getSage() + "岁");

        // 右侧班级胶囊（为空则隐藏）
        String cls = s.getSdept();
        if (cls == null || cls.trim().isEmpty()) {
            h.tvClass.setVisibility(View.GONE);
        } else {
            h.tvClass.setVisibility(View.VISIBLE);
            h.tvClass.setText(cls);
        }

        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(s);
        });
        h.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) return longClickListener.onLongClick(s);
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvTitle, tvDetail, tvClass;
        VH(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDetail = itemView.findViewById(R.id.tvDetail);
            tvClass = itemView.findViewById(R.id.tvClass);
        }
    }
}
