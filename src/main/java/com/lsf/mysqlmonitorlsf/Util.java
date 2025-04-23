package com.lsf.mysqlmonitorlsf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
    public Util() {
    }

    public static String ftime() {
        SimpleDateFormat ftime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return ftime.format(new Date());
    }

    public static String getEcho(String password) {
        String echo = "";
        if (password != null && !password.equals("")) {
            int length = password.length();
            StringBuffer stringBuffer = new StringBuffer();

            for(int i = 0; i < length; ++i) {
                stringBuffer.append("*");
            }

            echo = stringBuffer.toString();
        }

        return echo;
    }

    public static Connection getConn(String dbhost, int dbport, String dbuser, String dbpass) {
        Connection conn = null;
        String JDBC_DRIVER = "com.mysql.jdbc.Driver";
        String DB_URL = null;

        try {
            DB_URL = String.format("jdbc:mysql://%s:%s/mysql?serverTimezone=UTC", dbhost, dbport);
            Class.forName(JDBC_DRIVER);
        } catch (Exception var11) {
            JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
            DB_URL = String.format("jdbc:mysql://%s:%s/mysql?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", dbhost, dbport);

            try {
                Class.forName(JDBC_DRIVER);
            } catch (ClassNotFoundException classNotFoundException) {
                classNotFoundException.printStackTrace();
            }
        }

        try {
            conn = DriverManager.getConnection(DB_URL, dbuser, dbpass);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return conn;
    }

}
