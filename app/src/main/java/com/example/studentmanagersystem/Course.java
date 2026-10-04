package com.example.studentmanagersystem;

// 课程实体类：对应 course 表一行
public class Course {
    private String cno;       // 课程号
    private String cname;     // 课程名
    private double ccredit;   // 学分
    private int chours;       // 学时
    private String tno;       // 授课老师工号
    private String semester;  // 学期，如 2026-1

    public Course() {}

    public Course(String cno, String cname, double ccredit, int chours,
                  String tno, String semester) {
        this.cno = cno;
        this.cname = cname;
        this.ccredit = ccredit;
        this.chours = chours;
        this.tno = tno;
        this.semester = semester;
    }

    public String getCno() { return cno; }
    public void setCno(String cno) { this.cno = cno; }
    public String getCname() { return cname; }
    public void setCname(String cname) { this.cname = cname; }
    public double getCcredit() { return ccredit; }
    public void setCcredit(double ccredit) { this.ccredit = ccredit; }
    public int getChours() { return chours; }
    public void setChours(int chours) { this.chours = chours; }
    public String getTno() { return tno; }
    public void setTno(String tno) { this.tno = tno; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
}
