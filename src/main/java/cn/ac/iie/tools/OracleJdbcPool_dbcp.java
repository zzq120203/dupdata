package cn.ac.iie.tools;

import cn.ac.iie.configs.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OracleJdbcPool_dbcp {

    private static final List<Connection> sessionList = new ArrayList<Connection>();
    private static Logger log = LoggerFactory.getLogger(OracleJdbcPool_dbcp.class);
    static {
        try {
            Class.forName(Config.oracleDriver);
        } catch (Throwable e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection s = null;
        synchronized (sessionList) {
            if (sessionList.size() > 0)
                s = sessionList.remove(0);
        }
        if (s == null || s.isClosed()) {
            s = DriverManager.getConnection(Config.oracleUrl, Config.oracleUser, Config.oraclePwd);
        }
        return s;
    }

    public static void closeConn(Connection session) {
        synchronized (sessionList) {
            try {
                if (sessionList.size() >= 5) {
                    session.close();
                } else if (!session.isClosed()) {
                    sessionList.add(session);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
