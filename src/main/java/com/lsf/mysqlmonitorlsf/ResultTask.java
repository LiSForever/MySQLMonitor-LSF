package com.lsf.mysqlmonitorlsf;

public class ResultTask {
    private int index;
    private String date;
    private String sql;

    public ResultTask() {
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSql() {
        return this.sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}