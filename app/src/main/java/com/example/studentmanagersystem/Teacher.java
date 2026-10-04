package com.example.studentmanagersystem;

// 教师实体类：对应 teacher 表
public class Teacher {
    private String tno;     // 工号
    private String tname;   // 姓名
    private String tsex;    // 性别
    private String title;   // 职称
    private String pwd;     // 密码（MD5 密文）
    private String phone;   // 手机号

    public Teacher(String tno, String tname, String tsex, String title, String pwd, String phone) {
        this.tno = tno;
        this.tname = tname;
        this.tsex = tsex;
        this.title = title;
        this.pwd = pwd;
        this.phone = phone;
    }

    public String getTno() { return tno; }
    public void setTno(String tno) { this.tno = tno; }

    public String getTname() { return tname; }
    public void setTname(String tname) { this.tname = tname; }

    public String getTsex() { return tsex; }
    public void setTsex(String tsex) { this.tsex = tsex; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPwd() { return pwd; }
    public void setPwd(String pwd) { this.pwd = pwd; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
