package com.example.studentmanagersystem;

// 成绩单/选课列表里的一行：课程信息 + 该生该课成绩（grade 为 null=未考）
public class ScItem {
    private String cno;
    private String cname;
    private double ccredit;
    private int chours;
    private String semester;
    private Double grade;

    public ScItem(String cno, String cname, double ccredit, int chours,
                  String semester, Double grade) {
        this.cno = cno;
        this.cname = cname;
        this.ccredit = ccredit;
        this.chours = chours;
        this.semester = semester;
        this.grade = grade;
    }

    public String getCno() { return cno; }
    public String getCname() { return cname; }
    public double getCcredit() { return ccredit; }
    public int getChours() { return chours; }
    public String getSemester() { return semester; }
    public Double getGrade() { return grade; }
}
