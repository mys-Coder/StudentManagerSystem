package com.example.studentmanagersystem;

// 学生实体类：对应数据库 student 表的一行
public class Student {
    private String sno;    // 学号
    private String sname;  // 姓名
    private String ssex;   // 性别
    private int sage;      // 年龄
    private String sdept;  // 班级/院系
    private String pwd;    // 登录密码
    private String phone;  // 手机号

    public Student() {}

    public Student(String sno, String sname, String ssex, int sage,
                   String sdept, String pwd, String phone) {
        this.sno = sno;
        this.sname = sname;
        this.ssex = ssex;
        this.sage = sage;
        this.sdept = sdept;
        this.pwd = pwd;
        this.phone = phone;
    }

    public String getSno() { return sno; }
    public void setSno(String sno) { this.sno = sno; }
    public String getSname() { return sname; }
    public void setSname(String sname) { this.sname = sname; }
    public String getSsex() { return ssex; }
    public void setSsex(String ssex) { this.ssex = ssex; }
    public int getSage() { return sage; }
    public void setSage(int sage) { this.sage = sage; }
    public String getSdept() { return sdept; }
    public void setSdept(String sdept) { this.sdept = sdept; }
    public String getPwd() { return pwd; }
    public void setPwd(String pwd) { this.pwd = pwd; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
