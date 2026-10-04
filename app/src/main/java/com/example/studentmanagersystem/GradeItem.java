package com.example.studentmanagersystem;

// 成绩录入列表的一行：某个学生在某门课的成绩（grade 为 null=未考/暂无）
public class GradeItem {
    private String sno;
    private String sname;
    private Double grade; // 包装类型 Double，允许 null

    public GradeItem(String sno, String sname, Double grade) {
        this.sno = sno;
        this.sname = sname;
        this.grade = grade;
    }
    public String getSno() { return sno; }
    public String getSname() { return sname; }
    public Double getGrade() { return grade; }
    public void setGrade(Double grade) { this.grade = grade; }
}
