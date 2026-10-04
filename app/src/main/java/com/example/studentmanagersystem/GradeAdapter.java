package com.example.studentmanagersystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// 成绩录入列表适配器：每个学生一行，填成绩后单独保存
public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.VH> {

    public interface OnSaveListener {
        void onSave(GradeItem item, String gradeText);
    }

    private final List<GradeItem> data;
    private final OnSaveListener listener;

    public GradeAdapter(List<GradeItem> data, OnSaveListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grade, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        GradeItem item = data.get(position);
        h.tvName.setText(item.getSname() + "（" + item.getSno() + "）");
        h.etGrade.setText(item.getGrade() == null ? "" : String.valueOf(item.getGrade()));
        h.btnSave.setOnClickListener(v ->
                listener.onSave(item, h.etGrade.getText().toString().trim()));
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        EditText etGrade;
        Button btnSave;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvGradeName);
            etGrade = itemView.findViewById(R.id.etGrade);
            btnSave = itemView.findViewById(R.id.btnSaveGrade);
        }
    }
}
